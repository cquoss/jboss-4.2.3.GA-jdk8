/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.services.deployment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jboss.logging.Logger;
import org.jboss.services.deployment.metadata.ConfigInfo;
import org.jboss.services.deployment.metadata.ConfigInfoBinding;
import org.jboss.services.deployment.metadata.PropertyInfo;
import org.jboss.services.deployment.metadata.TemplateInfo;
import org.jboss.system.server.ServerConfigLocator;
import org.jboss.util.file.Files;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;

/**
 * Class handling JBoss module generation. Uses apache velocity
 * for generating deployment descriptors.
 * 
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * 
 * @version $Revision: 57210 $
 */
public class DeploymentManager
{
   // Constants -----------------------------------------------------
   
   /** the filename to look for in template subdirectories */
   public static final String TEMPLATE_CONFIG_FILE = "template-config.xml";
   
   /** an object to pass back from the template to trigger an error */
   public static final String TEMPLATE_ERROR_PARAM = "template-error";
   
   /** a helper object to pass in to the template */
   public static final String CONTEXT_HELPER = "helper";
   
   // Private Data --------------------------------------------------

   /** Logger */
   private Logger log;
   
   /** directory to hold the template subdirectories */
   private File templateDir;
   
   /** the directory to output generated modules */
   private File undeployDir;
   
   /** the directory to move modules for deployment */
   private File deployDir;
   
   /** config name string -> ConfigInfo */
   private Map configMap;

   /** the apache velocity engine */
   VelocityEngine ve;

   /**
    * @param templateDir the root dir where templates are stored
    * @param packageDir the directory to store generated packages
    */
   public DeploymentManager(String templateDir, String undeployDir, String deployDir, Logger log)
      throws Exception
   {
      this.log = log;
   
      // do the actuall initialization
      initialize(templateDir, undeployDir, deployDir);
   }
   
   // Public Interface ----------------------------------------------
   
   /**
    * Return the list of available templates
    */
   public Set listModuleTemplates()
   {
      Set keys = configMap.keySet();
      
      synchronized(configMap)
      {
         // return a new sorted copy
         return new TreeSet(keys);
      }
   }
   
   /**
    * Get property metadata information for a particular template
    * 
    * @param template
    * @return list with PropertyInfo objects associated with the template
    * @throws Exception if the template does not exist
    */
   public List getTemplatePropertyInfo(String template)
      throws Exception
   {
      ConfigInfo ci = (ConfigInfo)configMap.get(template);
      
      if (ci == null)
      {
         throw new Exception("template does not exist: " + template);
      }
      else 
      {  // return a copy
         List propertyList = ci.getPropertyInfoList();
         List newList = new ArrayList(propertyList.size());
         
         for (Iterator i = propertyList.iterator(); i.hasNext();) 
         {
            newList.add(new PropertyInfo((PropertyInfo)i.next()));
         }
         return newList; 
      }
   }

   public String createModule(String module, String template, HashMap properties)
      throws Exception
   {
      if (module == null || template == null || properties == null)
         throw new Exception("Null argument: module=" + module +
                                          ", template=" + template + ", properties=" + properties);
      
      // make sure proposed module name is filesystem friendly
      if (!module.equals(Files.encodeFileName(module)))
         throw new Exception("not a filesystem friendly module name: " + module);
      
      ConfigInfo ci = (ConfigInfo)configMap.get(template);
      
      if (ci == null)
         throw new Exception("template does not exist: " + template);
      
      // get optional package extension (e.g. .sar)
      // and enforce it on the output package (file or directory)
      File outputModule;
      
      String extension = ci.getExtension();
      if (extension == null || module.endsWith(extension))
         outputModule = new File(this.undeployDir, module);
      else 
         outputModule = new File(this.undeployDir, module + extension);
      
      // check if module already exists in output dir      
      if (outputModule.exists())
         throw new Exception("module already exist: " + outputModule);
      
      String vmTemplate = ci.getTemplate();
      
      // make sure we clean-up in case something goes wrong
      try
      {
         // simple case - single descriptor package (e.g. xxx-service.xml)
         if (vmTemplate != null )
         {
            VelocityContext ctx = createTemplateContext(ci, properties);
            
            BufferedWriter out = new BufferedWriter(new FileWriter(outputModule));
      
            try {
               boolean success = ve.mergeTemplate(template + '/' + vmTemplate, ctx, out);
               
               if (success == true)
               {
                  String errorMsg = (String)ctx.get(TEMPLATE_ERROR_PARAM);
                  
                  if (errorMsg.length() > 0)
                     throw new Exception("Template error: " + errorMsg);
                  else
                     log.debug("created module '" + outputModule.getName() + "' based on template '" + template + "'");
               }
               else
                  throw new Exception("Failed to create module '" + outputModule.getName());
            }
            finally 
            {
               out.close();
            }
         }
         else
         {
            // complex case - many descriptors and possibly files to copy
            // now output will be a directory instead of a plain descriptor (e.g. xxx.sar)
            VelocityContext ctx = createTemplateContext(ci, properties);
            
            // deep copy files if copydir specified
            String copydir = ci.getCopydir();
            
            File sourceDir = new File(this.templateDir, template + '/' + copydir);
            
            deepCopy(sourceDir, outputModule);
            
            // go through all declared templates
            List templateList = ci.getTemplateInfoList();
            
            for (Iterator i = templateList.iterator(); i.hasNext(); )
            {
               TemplateInfo ti = (TemplateInfo)i.next();
               
               File outputFile = new File(outputModule, ti.getOutput());
               File outputPath  = outputFile.getParentFile();
               
               if (!outputPath.exists())
                  if (!outputPath.mkdirs())
                     throw new IOException("cannot create directory: " + outputPath);
                  
               BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
      
               try {
                  boolean success = ve.mergeTemplate(template + '/' + ti.getInput(), ctx, out);
                  
                  if (success == true)
                  {
                     String errorMsg = (String)ctx.get(TEMPLATE_ERROR_PARAM);
                     
                     if (errorMsg.length() > 0)
                        throw new Exception("Template error: " + errorMsg);
                     else
                        log.debug("created module '" + outputModule.getName() + "' based on template '" + template + "'");
                  }
    
                  else
                     throw new Exception("Failed to create package '" + outputModule.getName());
               }
               finally
               {
                  out.close();
               } 
            }
         }
      }
      catch (Exception e)
      {
         if (outputModule.exists())
         {
            boolean deleted = Files.delete(outputModule);
            if (!deleted)
               log.warn("Failed to clean-up erroneous module: " + outputModule);
         }
         throw e;
      }
      return outputModule.getName();
   }
   
   /**
    * Remove a module if exists
    * 
    * @param module the module to remove
    * @return true if removed, false if module does not exist or an error occurs
    */
   public boolean removeModule(String module)
   {
      File target = new File(this.undeployDir, module);
      return Files.delete(target);
   }
   
   public void moveToDeployDir(String module)
      throws Exception
   {
      File source = new File(this.undeployDir, module);
      File target = new File(this.deployDir, module);
      
      if (source.exists())
      {
         boolean moved = source.renameTo(target);
         if (!moved)
            throw new Exception("cannot move module: " + module);
      }
      else
         throw new Exception("module does not exist: " + module);
   }
   
   public void moveToModuleDir(String module)
      throws Exception
   {
      File source = new File(this.deployDir, module);
      File target = new File(this.undeployDir, module);

      if (source.exists())
      {
         boolean moved = source.renameTo(target);
         if (!moved)
            throw new Exception("cannot move module: " + module);
      }
      else
         throw new Exception("module does not exist: " + module);
   }
      
   public URL getDeployedURL(String module)
      throws Exception
   {
      File target = new File(this.deployDir, module);
      
      if (!target.exists())
         throw new Exception("module does not exist: " + target);
      
      return target.toURL();
   }
   
   public URL getUndeployedURL(String module)
      throws Exception
   {
      File target = new File(this.undeployDir, module);
      
      if (!target.exists())
         throw new Exception("module does not exist: " + target);
      
      return target.toURL();
   }
   
   // Private Methods -----------------------------------------------
   
   /**
    * Performs the actual initialization
    */
   private void initialize(String templateDir, String undeployDir, String deployDir)
      throws Exception
   {
      boolean debug = log.isDebugEnabled();
      boolean trace = log.isTraceEnabled();
      
      // Find out template dir
      this.templateDir = initDir(templateDir, false);
      if (debug)
         log.debug("template dir=" + this.templateDir);
      
      // Initialize output dir
      this.undeployDir = initDir(undeployDir, true);
      if (debug)
         log.debug("undeployDir dir=" + this.undeployDir);
      
      this.deployDir = initDir(deployDir, false);
      if (debug)
         log.debug("deploy dir=" + this.deployDir);
      
      // Discover all template config files
      List configFiles = findTemplateConfigFiles(this.templateDir);
      
      if (debug)
         log.debug("template config files=" + configFiles);
      
      Map map = Collections.synchronizedMap(new TreeMap());
      
      // Parse each template config file and store metadata in configMap
      for (Iterator i = configFiles.iterator(); i.hasNext(); ) {
         File file = (File)i.next();
         ConfigInfo ci = parseXMLconfig(file);
         
         // derive template name from subdirectory name
         ci.setName(file.getParentFile().getName());
         
         if (trace)
            log.trace("file: " + file + " ConfigInfo: " + ci);
         
         Object existingValue = map.put(ci.getName(), ci);
         
         // make sure not two configuration templates with the same name
         if (existingValue != null)
            throw new Exception("Duplicate template configuration entry: " + ci.getName());
      }

      this.configMap = map;
      
      // Initialise velocity engine
      this.ve = new VelocityEngine();
      
      this.ve.setProperty("runtime.log.logsystem.class", 
                             "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
      this.ve.setProperty("runtime.log.logsystem.log4j.category",
                             log.getName() + ".VelocityEngine");
      this.ve.setProperty("file.resource.loader.path", this.templateDir.getCanonicalPath());
      
      this.ve.init();
   }

   /**
    * Check if directory exists as an absolute path,
    * otherwise, try to find it under the jboss server
    * directories (and optionally create it, if the
    * create flag has been set)
    */
   private File initDir(String targetDir, boolean create)
      throws Exception
   {
      File dir = null;
      
     // Check if this is an existing absolute path
     try {
        URL fileURL = new URL(targetDir);
         
        File file = new File(fileURL.getFile());
         
        if(file.isDirectory() && file.canRead() && file.canWrite()) {
           dir = file;
        }
     }
     catch(Exception e) {
       // Otherwise, try to see inside the jboss directory hierarchy
        
        File homeDir = ServerConfigLocator.locate().getServerHomeDir();
   
        dir = new File(homeDir, targetDir);
   
        if (create == true)
           dir.mkdirs();
         
        if (!dir.isDirectory())
           throw new Exception("The target directory is not valid: "
                               + dir.getCanonicalPath());
     }
     return dir;
   }

   /**
    * Find all files named TEMPLATE_CONFIG_FILE
    * one level below basedir, i.e.
    * 
    * basedir/YYY/template-config.xml
    * ...
    * 
    * @param basedir
    * @return
    */
   private List findTemplateConfigFiles(File basedir)
   {
      // return val
      List files = new ArrayList();
      
      // anonymous class
      FileFilter dirFilter = new FileFilter()
      {
         public boolean accept(File file)
         {
            return file.isDirectory() && !file.getName().startsWith(".");
         }
      };
      // return all dirs not starting with "."
      File[] dirs = basedir.listFiles(dirFilter);
      
      for (int i = 0; i < dirs.length; i++) {
         File file = new File(dirs[i], TEMPLATE_CONFIG_FILE);
         
         if (file.isFile() && file.canRead())
            files.add(file);
      }
      return files;
   }
   
   /**
    * Parse an XML template config file into
    * a ConfigInfo POJO model.
    * 
    * @param file
    * @return
    * @throws Exception
    */
   private ConfigInfo parseXMLconfig(File file)
      throws Exception
   {
      // get the XML stream
      InputStream is = new FileInputStream(file);

      // create unmarshaller
      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance()
            .newUnmarshaller();

      // create an instance of ObjectModelFactory
      ObjectModelFactory factory = new ConfigInfoBinding();

      // let the object model factory to create an instance of Book and populate it with data from XML
      ConfigInfo ci = (ConfigInfo)unmarshaller.unmarshal(is, factory, null);

      // close the XML stream
      is.close();

      return ci;
   }
   
   /**
    * Copy values from HashMap to VelocityContext, following the
    * metadata definition. Make sure types are correct, while
    * required properties are all there. Throw an exception
    * otherwise
    *  
    * @param ci
    * @param map
    * @return
    * @throws Exception
    */
   private VelocityContext createTemplateContext(ConfigInfo ci, HashMap map)
      throws Exception
   {
      VelocityContext vc;
      
      List propertyList = ci.getPropertyInfoList();
      
      if (propertyList.size() > 0)
      {
         vc = new VelocityContext();
         
         for (Iterator i = propertyList.iterator(); i.hasNext(); ) {
            PropertyInfo pi = (PropertyInfo)i.next();
            
            String name = pi.getName();
            String type = pi.getType();
            boolean optional = pi.isOptional();
            Object defaultValue = pi.getDefaultValue();
            
            if (name == null || name.length() == 0 || type == null || type.length() == 0)
               throw new Exception("Null or empty name/type property metadata for template: " + ci.getName());
            
            Object sentValue = map.get(name);
            
            // a value was sent - pass it over after checking its type
            if (sentValue != null)
            {
               if (!type.equals(sentValue.getClass().getName()))
                  throw new Exception("Expected type '" + type + "' for property '" + name +
                                      "', got '" + sentValue.getClass().getName());
               
               vc.put(name, sentValue);
            }
            else if (optional == false) {
               // a value was not sent - property is required
               // so use the default value (if exists) or throw an exception
               if (defaultValue != null) {
                  vc.put(name, defaultValue);
               }
               else {
                  throw new Exception("Required property missing: '" + name + "' of type '" + type + "'");
               }
            }
            // property is optional and value was not sent
            // do nothing even if a default is set
         }
      }
      else
      {
         // property list empty, allow everything
         // just embed the Hashmap
         vc = new VelocityContext(map);
      }
      // add a parameter to allow the templates to report errors
      vc.put(TEMPLATE_ERROR_PARAM, "");
      // add a context helper
      vc.put(CONTEXT_HELPER, new ContextHelper());
      
      return vc;
   }
   
   /**
    * Make sure sourceDir exist, then deep copy
    * all files/dirs from sourceDir to targetDir
    * 
    * @param sourceDir
    * @param targetDir
    */
   private void deepCopy(File sourceDir, File targetDir)
      throws IOException
   {
      if (!sourceDir.isDirectory())
         throw new IOException("sourceDir not a directory: " + sourceDir);
      
      if (!targetDir.mkdir())
         throw new IOException("could not create directory: " + targetDir);
      
      File[] files = sourceDir.listFiles();
      
      for (int i = 0; i < files.length; i++)
      {
         File source = files[i];
         
         if (!source.canRead())
            throw new IOException("cannot read: " + source);

         if (source.isFile())
            Files.copy(source, new File(targetDir, source.getName()));
         else
            deepCopy(source, new File(targetDir, source.getName()));
      }
   }
}
