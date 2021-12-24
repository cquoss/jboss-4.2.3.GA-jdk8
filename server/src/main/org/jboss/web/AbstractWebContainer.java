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
package org.jboss.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import javax.management.ObjectName;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.SubDeployerSupport;
import org.jboss.metadata.WebMetaData;
import org.jboss.metadata.XmlFileLoader;
import org.jboss.mx.loading.LoaderRepositoryFactory;
import org.jboss.mx.loading.LoaderRepositoryFactory.LoaderRepositoryConfig;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.util.file.FilenameSuffixFilter;
import org.jboss.util.file.JarUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** A template pattern class for web container integration into JBoss. This class
 should be subclasses by web container providers wishing to integrate their
 container into a JBoss server.

 @see org.jboss.web.AbstractWebDeployer

 @jmx:mbean extends="org.jboss.deployment.SubDeployerMBean"

 @author  Scott.Stark@jboss.org
 @author  Christoph.Jung@infor.de
 @author  Thomas.Diesler@arcor.de
 @version $Revision: 57209 $
 */
public abstract class AbstractWebContainer extends SubDeployerSupport
   implements AbstractWebContainerMBean
{
   public static final String DEPLOYER = "org.jboss.web.AbstractWebContainer.deployer";
   public static final String WEB_APP = "org.jboss.web.AbstractWebContainer.webApp";
   public static final String WEB_MODULE = "org.jboss.web.AbstractWebContainer.webModule";
   public static final String ERROR = "org.jboss.web.AbstractWebContainer.error";

   public static interface WebDescriptorParser
   {
      /** This method is called as part of subclass performDeploy() method implementations
       to parse the web-app.xml and jboss-web.xml deployment descriptors from a
       war deployment. The method creates the ENC(java:comp/env) env-entry,
       resource-ref, ejb-ref, etc element values. The creation of the env-entry
       values does not require a jboss-web.xml descriptor. The creation of the
       resource-ref and ejb-ref elements does require a jboss-web.xml descriptor
       for the JNDI name of the deployed resources/EJBs.

       Because the ENC context is private to the web application, the web
       application class loader is used to identify the ENC. The class loader
       is used because each war typically requires a unique class loader to
       isolate the web application classes/resources. This means that the
       ClassLoader passed to this method must be the thread context ClassLoader
       seen by the server/jsp pages during init/destroy/service/etc. method
       invocations if these methods interace with the JNDI ENC context.

       @param loader the ClassLoader for the web application. May not be null.
       @param metaData the WebMetaData from the WebApplication object passed to
       the performDeploy method.
       */
      public void parseWebAppDescriptors(ClassLoader loader, WebMetaData metaData) throws Exception;

      /** Get the DeploymentInfo for the war the triggered the deployment process.
       * The returned reference may be updated to affect the state of the
       * JBoss DeploymentInfo object. This can be used to assign ObjectNames
       * of MBeans created by the container.
       * @return The DeploymentInfo for the war being deployed.
       */
      public DeploymentInfo getDeploymentInfo();
   }

   /** The suffixes we accept, along with their relative order */
   private static final String[] DEFAULT_ENHANCED_SUFFIXES = new String[] {
         "500:.war"
   };
   
   /** A mapping of deployed warUrl strings to the WebApplication object */
   protected HashMap deploymentMap = new HashMap();
   /** The parent class loader first model flag */
   protected boolean java2ClassLoadingCompliance = false;
   /** A flag indicating if war archives should be unpacked */
   protected boolean unpackWars = true;
   /** A flag indicating if local dirs with WEB-INF/web.xml should be treated as wars
    */ 
   protected boolean acceptNonWarDirs = false;

   /** If true, ejb-links that don't resolve don't cause an error (fallback to jndi-name) */
   protected boolean lenientEjbLink = false;

   /** The default security-domain name to use */
   protected String defaultSecurityDomain = "java:/jaas/other";
   /** The request attribute name under which the JAAS Subject is store */
   private String subjectAttributeName = null;
   /** The ServiceController used to control web app startup dependencies */
   protected ServiceControllerMBean serviceController;

   public AbstractWebContainer()
   {
      setEnhancedSuffixes(DEFAULT_ENHANCED_SUFFIXES);
   }

   /** Get the flag indicating if the normal Java2 parent first class loading
    * model should be used over the servlet 2.3 web container first model.
    * @return true for parent first, false for the servlet 2.3 model
    * @jmx:managed-attribute
    */
   public boolean getJava2ClassLoadingCompliance()
   {
      return java2ClassLoadingCompliance;
   }

   /** Set the flag indicating if the normal Java2 parent first class loading
    * model should be used over the servlet 2.3 web container first model.
    * @param flag true for parent first, false for the servlet 2.3 model
    * @jmx:managed-attribute
    */
   public void setJava2ClassLoadingCompliance(boolean flag)
   {
      java2ClassLoadingCompliance = flag;
   }

   /** Set the flag indicating if war archives should be unpacked. This may
    * need to be set to false as long extraction paths under deploy can
    * show up as deployment failures on some platforms.
    * 
    * @jmx:managed-attribute
    * @return true is war archives should be unpacked
    */
   public boolean getUnpackWars()
   {
      return unpackWars;
   }

   /** Get the flag indicating if war archives should be unpacked. This may
    * need to be set to false as long extraction paths under deploy can
    * show up as deployment failures on some platforms.
    * 
    * @jmx:managed-attribute
    * @param flag , true is war archives should be unpacked
    */
   public void setUnpackWars(boolean flag)
   {
      this.unpackWars = flag;
   }

   /**
    * Get the flag indicating if local dirs with WEB-INF/web.xml should be
    * treated as wars
    * @return true if local dirs with WEB-INF/web.xml should be treated as wars
    * @jmx.managed-attribute
    */
   public boolean getAcceptNonWarDirs()
   {
      return acceptNonWarDirs;
   }
   /**
    * Set the flag indicating if local dirs with WEB-INF/web.xml should be
    * treated as wars
    * @param flag - true if local dirs with WEB-INF/web.xml should be treated as wars
    * @jmx.managed-attribute
    */ 
   public void setAcceptNonWarDirs(boolean flag)
   {
      this.acceptNonWarDirs = flag;
   }

   /**
    * Get the flag indicating if ejb-link errors should be ignored
    * in favour of trying the jndi-name in jboss-web.xml
    * @return the LenientEjbLink flag 
    *    
    * @jmx:managed-attribute
    */
   public boolean getLenientEjbLink()
   {
      return lenientEjbLink;
   }

   /**
    * Set the flag indicating if ejb-link errors should be ignored
    * in favour of trying the jndi-name in jboss-web.xml
    *    
    * @jmx:managed-attribute
    */
   public void setLenientEjbLink(boolean flag)
   {
      lenientEjbLink = flag;
   }

   /** Get the default security domain implementation to use if a war
    * does not declare a security-domain.
    *
    * @return jndi name of the security domain binding to use.
    * @jmx:managed-attribute
    */
   public String getDefaultSecurityDomain()
   {
      return defaultSecurityDomain;
   }
   /** Set the default security domain implementation to use if a war
    * does not declare a security-domain.
    *
    * @param defaultSecurityDomain - jndi name of the security domain binding
    * to use.
    * @jmx:managed-attribute
    */
   public void setDefaultSecurityDomain(String defaultSecurityDomain)
   {
      this.defaultSecurityDomain = defaultSecurityDomain;
   }

   /** Get the session attribute number under which the caller Subject is stored
    * @jmx:managed-attribute
    */ 
   public String getSubjectAttributeName()
   {
      return subjectAttributeName;
   }
   /** Set the session attribute number under which the caller Subject is stored
    * @jmx:managed-attribute
    */ 
   public void setSubjectAttributeName(String subjectAttributeName)
   {
      this.subjectAttributeName = subjectAttributeName;
   }


   public abstract AbstractWebDeployer getDeployer(DeploymentInfo di) throws Exception;

   public boolean accepts(DeploymentInfo sdi)
   {
     // Should be checking for .war and .war/
     boolean accepts = super.accepts(sdi);

     if (accepts == false && acceptNonWarDirs == true)
     {
        // Check for a local unpacked directory with a /WEB-INF/web.xml
        if (sdi.url.getProtocol().equalsIgnoreCase("file"))
        {
           File webXml = new File(sdi.url.getFile(), "WEB-INF/web.xml");
           accepts = webXml.exists();
        }
     }
     return accepts;
   }

   public synchronized void init(DeploymentInfo di)
      throws DeploymentException
   {
      log.debug("Begin init");
      this.server = di.getServer();
      try
      {
         if (di.url.getPath().endsWith("/"))
         {
            // the URL is a unpacked collection, watch the deployment descriptor
            di.watch = new URL(di.url, "WEB-INF/web.xml");
         }
         else
         {
            // just watch the original URL
            di.watch = di.url;
         }

         // We need to unpack the WAR if it has webservices, because we need
         // to manipulate th web.xml before deploying to the web container 
         boolean unpackWebservice = di.localCl.findResource("WEB-INF/webservices.xml") != null;
         // With JSR-181 annotated JSE endpoints we need to do it as well even if there is no webservices.xml
         unpackWebservice |= server.isRegistered(ObjectNameFactory.create("jboss.ws:service=ServiceEndpointManager"));

         // Make sure the war is unpacked if unpackWars is true
         File warFile = new File(di.localUrl.getFile());
         if (warFile.isDirectory() == false && (unpackWars || unpackWebservice))
         {
            // After findResource we cannot rename the WAR anymore, because
            // some systems keep an open reference to the file :(  
            String prefix = warFile.getCanonicalPath();
            prefix = prefix.substring(0, prefix.lastIndexOf(".war"));
            File expWarFile = new File(prefix + "-exp.war");
            if( expWarFile.mkdir() == false )
               throw new DeploymentException("Was unable to mkdir: "+expWarFile);
            log.debug("Unpacking war to: "+expWarFile);
            FileInputStream fis = new FileInputStream(warFile);
            JarUtils.unjar(fis, expWarFile);
            fis.close();
            log.debug("Replaced war with unpacked contents");
            if (warFile.delete() == false)
               log.debug("Was unable to delete war file");
            else
               log.debug("Deleted war archive");
            // Reset the localUrl to end in a '/'
            di.localUrl = expWarFile.toURL();
            // Reset the localCl to point to the file
            URL[] localCl = new URL[]{di.localUrl};
            di.localCl = new URLClassLoader(localCl);
         }

         WebMetaData metaData = new WebMetaData();
         metaData.setResourceClassLoader(di.localCl);
         metaData.setJava2ClassLoadingCompliance(this.java2ClassLoadingCompliance);
         di.metaData = metaData;

         String webContext = di.webContext;
         if( webContext != null )
         {
            if( webContext.length() > 0 && webContext.charAt(0) != '/' )
               webContext = "/" + webContext;
         }
         // Get the war URL
         URL warURL = di.localUrl != null ? di.localUrl : di.url;
         log.debug("webContext: " + webContext);
         log.debug("warURL: " + warURL);

         // Parse the web.xml and jboss-web.xml descriptors
         parseMetaData(webContext, warURL, di.shortName, metaData);

         // Check for a loader-repository
         LoaderRepositoryConfig config = metaData.getLoaderConfig();
         if (config != null)
            di.setRepositoryInfo(config);

         // Generate an event for the initialization
         super.init(di);
      }
      catch (DeploymentException e)
      {
         log.debug("Problem in init ", e);
         throw e;
      }
      catch (Exception e)
      {
         log.error("Problem in init ", e);
         throw new DeploymentException(e);
      }

      log.debug("End init");
   }

   /** Create a WebModule service, register it under the name
    "jboss.web.deployment:war="+di.shortName
    and invoke the ServiceController.create(jmxname, depends) using the depends
    found in the WebMetaData.

    @param di - The deployment info for the war
    @throws DeploymentException
    */ 
   public void create(DeploymentInfo di) throws DeploymentException
   {
      log.debug("create, " + di.shortName);
      try
      {
         // initialize the annotations loader
         URL loaderURL = (di.localUrl != null ? di.localUrl : di.url);
         File warFile = new File(di.localUrl.getFile());
         if (warFile.isDirectory())
         {
            List urlList = new ArrayList();
            urlList.add(new URL(loaderURL + "WEB-INF/classes/"));
            
            File libDir = new File(warFile, "WEB-INF/lib/");
            String[] jarArr = libDir.list(new FilenameSuffixFilter(".jar"));
            for (int i = 0; jarArr != null && i < jarArr.length; i++)
            {
               String urlStr = loaderURL + "WEB-INF/lib/" + jarArr[i];
               urlList.add(new URL(urlStr));
            }
            URL[] urlArr = new URL[urlList.size()];
            urlList.toArray(urlArr);
            di.annotationsCl = new URLClassLoader(urlArr, di.ucl);
         }
         else
         {
            List urlList = new ArrayList();
            urlList.add(new URL(warFile + "!WEB-INF/classes"));
            
            FileInputStream fis = new FileInputStream(warFile);
            JarInputStream jin = new JarInputStream(fis);
            ZipEntry entry = jin.getNextEntry();
            while (entry != null)
            {
               String entryName = entry.getName();
               if (entryName.startsWith("WEB-INF/lib"))
               {
                  urlList.add(new URL(warFile + "!" + entryName));
               }
               entry = jin.getNextEntry();
            }
            jin.close();
            
            URL[] urlArr = new URL[urlList.size()];
            urlList.toArray(urlArr);
            di.annotationsCl = new URLClassLoader(urlArr, di.ucl);
         }

         AbstractWebDeployer deployer = getDeployer(di);
         di.context.put(DEPLOYER, deployer);
         WebMetaData metaData = (WebMetaData) di.metaData;
         Collection depends = metaData.getDepends();
         WebModule module = new WebModule(di, this, deployer);
         ObjectName jmxName = new ObjectName("jboss.web.deployment:war="
            + di.shortName + ",id="+di.hashCode());
         server.registerMBean(module, jmxName);
         di.context.put(WEB_MODULE, jmxName);
         serviceController.create(jmxName, depends);
         // Generate an event for the create
         super.create(di);
      }
      catch(Exception e)
      {
         throw new DeploymentException("Failed to create web module", e);
      }
   }

   /** Invokes the ServiceController.start(jmxName) to start the WebModule
    after its dependencies are satisfied. 

    @param di - The deployment info for the war
    @throws DeploymentException
    */
   public synchronized void start(DeploymentInfo di) throws DeploymentException
   {
      ObjectName jmxName = (ObjectName) di.context.get(WEB_MODULE);
      try
      {
         serviceController.start(jmxName);
      }
      catch (DeploymentException e)
      {
         throw e;
      }
      catch(Exception e)
      {
         throw new DeploymentException("Unable to start web module", e);
      }
      // Check for a deployment error
      DeploymentException e = (DeploymentException) di.context.get(ERROR);
      if( e != null )
         throw e;

      // Generate an event for the startup
      super.start(di);
   }

   /** Invokes the ServiceController.start(jmxName) to stop the WebModule
    and its dependents. 

    @param di - The deployment info for the war
    @throws DeploymentException
    */
   public synchronized void stop(DeploymentInfo di)
      throws DeploymentException
   {
      ObjectName jmxName = (ObjectName) di.context.get(WEB_MODULE);
      try
      {
         if (jmxName != null)
            serviceController.stop(jmxName);
      }
      catch (DeploymentException e)
      {
         throw e;
      }
      catch(Exception e)
      {
         throw new DeploymentException("Unable to stop web module", e);
      }
      // Generate an event for the shutdown
      super.stop(di);
   }

   /** Invokes the ServiceController.destroy(jmxName) to destroy the WebModule
    and its dependents. 

    @param di - The deployment info for the war
    @throws DeploymentException
    */
   public synchronized void destroy(DeploymentInfo di)
      throws DeploymentException
   {
      ObjectName jmxName = (ObjectName) di.context.get(WEB_MODULE);
      try
      {
         if( jmxName != null )
         {
            try
            {
               serviceController.destroy(jmxName);
            }
            finally
            {
               serviceController.remove(jmxName);
            }
         }
      }
      catch (DeploymentException e)
      {
         throw e;
      }
      catch(Exception e)
      {
         throw new DeploymentException("Unable to stop web module", e);
      }
      // Generate an event for the shutdown
      super.destroy(di);
   }

   /** See if a war is deployed.
    @jmx:managed-operation
    */
   public boolean isDeployed(String warUrl)
   {
      return deploymentMap.containsKey(warUrl);
   }

   public void addDeployedApp(URL warURL,  WebApplication webApp)
   {
      deploymentMap.put(warURL, webApp);
   }
   /** Get the WebApplication object for a deployed war.
    @param warUrl the war url string as originally passed to deploy().
    @return The WebApplication created during the deploy step if the
    warUrl is valid, null if no such deployment exists.
    */
   public WebApplication getDeployedApp(String warUrl)
   {
      WebApplication appInfo = (WebApplication) deploymentMap.get(warUrl);
      return appInfo;
   }
   public WebApplication removeDeployedApp(URL warURL)
   {
      WebApplication appInfo = (WebApplication) deploymentMap.remove(warURL);
      return appInfo;
   }

   /** Returns the applications deployed by the web container subclasses.
    @jmx:managed-attribute
    @return An Iterator of WebApplication objects for the deployed wars.
    */
   public Iterator getDeployedApplications()
   {
      return deploymentMap.values().iterator();
   }

   /** An accessor for any configuration element set via setConfig. This
    method always returns null and must be overriden by subclasses to
    return a valid value.
    @jmx:managed-attribute
    */
   public Element getConfig()
   {
      return null;
   }

   /** This method is invoked to import an arbitrary XML configuration tree.
    Subclasses should override this method if they support such a configuration
    capability. This implementation does nothing.
    @jmx:managed-attribute
    */
   public void setConfig(Element config)
   {
   }

   /** Use reflection to access a URL[] getURLs method so that non-URLClassLoader
    *class loaders that support this method can provide info.
    */
   public static URL[] getClassLoaderURLs(ClassLoader cl)
   {
      URL[] urls = {};
      try
      {
         Class returnType = urls.getClass();
         Class[] parameterTypes = {};
         Method getURLs = cl.getClass().getMethod("getURLs", parameterTypes);
         if( returnType.isAssignableFrom(getURLs.getReturnType()) )
         {
            Object[] args = {};
            urls = (URL[]) getURLs.invoke(cl, args);
         }
         if( urls == null || urls.length == 0 )
         {
            getURLs = cl.getClass().getMethod("getAllURLs", parameterTypes);
            if( returnType.isAssignableFrom(getURLs.getReturnType()) )
            {
               Object[] args = {};
               urls = (URL[]) getURLs.invoke(cl, args);
            }
         }
      }
      catch(Exception ignore)
      {
      }
      return urls;
   }
   
   /** A utility method that walks up the ClassLoader chain starting at
    the given loader and queries each ClassLoader for a 'URL[] getURLs()'
    method from which a complete classpath of URL strings is built.
    */
   public String[] getCompileClasspath(ClassLoader loader)
   {
      HashSet tmp = new HashSet();
      ClassLoader cl = loader;
      while( cl != null )
      {
         URL[] urls = getClassLoaderURLs(cl);
         addURLs(tmp, urls);
         cl = cl.getParent();
      }
      try
      {
         URL[] globalUrls = (URL[])server.getAttribute(LoaderRepositoryFactory.DEFAULT_LOADER_REPOSITORY,
                                                         "URLs");
         addURLs(tmp, globalUrls);
      }
      catch (Exception e)
      {
         log.warn("Could not get global URL[] from default loader repository!");
      } // end of try-catch
      log.trace("JSP CompileClasspath: " + tmp);
      String[] cp = new String[tmp.size()];
      tmp.toArray(cp);
      return cp;
   }

   /** WARs do not have nested deployments
    * @param di
    */
   protected void processNestedDeployments(DeploymentInfo di) throws DeploymentException
   {
   }

   protected void startService() throws Exception
   {
      serviceController = (ServiceControllerMBean)
         MBeanProxyExt.create(ServiceControllerMBean.class,
                              ServiceControllerMBean.OBJECT_NAME,
                              server);
      super.startService();
   }

   /** This method creates a context-root string from either the
      WEB-INF/jboss-web.xml context-root element is one exists, or the
      filename portion of the warURL. It is called if the DeploymentInfo
      webContext value is null which indicates a standalone war deployment.
      A war name of ROOT.war is handled as a special case of a war that
      should be installed as the default web context.
    */
   protected void parseMetaData(String ctxPath, URL warURL, String warName,
      WebMetaData metaData)
      throws DeploymentException
   {
      InputStream jbossWebIS = null;
      InputStream webIS = null;

      // Parse the war deployment descriptors, web.xml and jboss-web.xml
      try
      {
         // See if the warUrl is a directory
         File warDir = new File(warURL.getFile());
         if( warURL.getProtocol().equals("file") && warDir.isDirectory() == true )
         {
            File webDD = new File(warDir, "WEB-INF/web.xml");
            if( webDD.exists() == true )
               webIS = new FileInputStream(webDD);
            File jbossWebDD = new File(warDir, "WEB-INF/jboss-web.xml");
            if( jbossWebDD.exists() == true )
               jbossWebIS = new FileInputStream(jbossWebDD);
         }
         else
         {
            // First check for a WEB-INF/web.xml and a WEB-INF/jboss-web.xml
            InputStream warIS = warURL.openStream();
            java.util.zip.ZipInputStream zipIS = new java.util.zip.ZipInputStream(warIS);
            java.util.zip.ZipEntry entry;
            byte[] buffer = new byte[512];
            int bytes;
            while( (entry = zipIS.getNextEntry()) != null )
            {
               if( entry.getName().equals("WEB-INF/web.xml") )
               {
                  ByteArrayOutputStream baos = new ByteArrayOutputStream();
                  while( (bytes = zipIS.read(buffer)) > 0 )
                  {
                     baos.write(buffer, 0, bytes);
                  }
                  webIS = new ByteArrayInputStream(baos.toByteArray());
               }
               else if( entry.getName().equals("WEB-INF/jboss-web.xml") )
               {
                  ByteArrayOutputStream baos = new ByteArrayOutputStream();
                  while( (bytes = zipIS.read(buffer)) > 0 )
                  {
                     baos.write(buffer, 0, bytes);
                  }
                  jbossWebIS = new ByteArrayInputStream(baos.toByteArray());
               }
            }
            zipIS.close();
         }

         XmlFileLoader xmlLoader = new XmlFileLoader();
         String warURI = warURL.toExternalForm();
         try
         {
            if( webIS != null )
            {
               Document webDoc = xmlLoader.getDocument(webIS, warURI+"/WEB-INF/web.xml");
               Element web = webDoc.getDocumentElement();
               metaData.importXml(web);
            }
         }
         catch(Exception e)
         {
            throw new DeploymentException("Failed to parse WEB-INF/web.xml", e);
         }
         try
         {
            if( jbossWebIS != null )
            {
               Document jbossWebDoc = xmlLoader.getDocument(jbossWebIS, warURI+"/WEB-INF/jboss-web.xml");
               Element jbossWeb = jbossWebDoc.getDocumentElement();
               metaData.importXml(jbossWeb);
            }
         }
         catch(Exception e)
         {
            throw new DeploymentException("Failed to parse WEB-INF/jboss-web.xml", e);
         }

      }
      catch(DeploymentException e)
      {
         throw e;
      }
      catch(Exception e)
      {
         log.warn("Failed to parse descriptors for war("+warURL+")", e);
      }

      // Build a war root context from the war name if one was not specified
      String webContext = ctxPath;
      if( webContext == null )
         webContext = metaData.getContextRoot();
      if( webContext == null )
      {
         // Build the context from the war name, strip the .war suffix
         webContext = warName;
         webContext = webContext.replace('\\', '/');
         if( webContext.endsWith("/") )
            webContext = webContext.substring(0, webContext.length()-1);
         int prefix = webContext.lastIndexOf('/');
         if( prefix > 0 )
            webContext = webContext.substring(prefix+1);
         int suffix = webContext.lastIndexOf(".war");
         if( suffix > 0 )
            webContext = webContext.substring(0, suffix);
          // Strip any '<int-value>.' prefix
          int index = 0;
          for(; index < webContext.length(); index ++)
          {
             char c = webContext.charAt(index);
             if( Character.isDigit(c) == false && c != '.' )
                break;
          }
          webContext = webContext.substring(index);
      }

      // Servlet containers are anal about the web context starting with '/'
      if( webContext.length() > 0 && webContext.charAt(0) != '/' )
         webContext = "/" + webContext;
      // And also the default root context must be an empty string, not '/'
      else if( webContext.equals("/") )
         webContext = "";
      metaData.setContextRoot(webContext);
   }

   private void addURLs(Set urlSet, URL[] urls)
   {
      for(int u = 0; u < urls.length; u ++)
      {
         URL url = urls[u];
         urlSet.add(url.toExternalForm());
      }
   }
}
