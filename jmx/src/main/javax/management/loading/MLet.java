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
package javax.management.loading;


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ServiceNotFoundException;

import org.jboss.logging.Logger;
import org.jboss.mx.loading.MBeanElement;
import org.jboss.mx.loading.MBeanFileParser;
import org.jboss.mx.loading.MLetParser;
import org.jboss.mx.loading.RepositoryClassLoader;
import org.jboss.mx.server.ServerConstants;
import org.jboss.mx.util.MBeanInstaller;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.util.NestedRuntimeException;
import org.jboss.util.id.SerialVersion;


/**
 * URL classloader capable of parsing an MLet text file adhering to the file
 * format defined in the JMX specification (v1.0).
 *
 * @see javax.management.loading.MLetMBean
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 57200 $  
 */
public class MLet extends URLClassLoader 
   implements MLetMBean, MBeanRegistration, Externalizable
{
   // Constants --------------------------------------------------------

   private static final long serialVersionUID;
   static
   {
      if (SerialVersion.version == SerialVersion.LEGACY)
         serialVersionUID = -3671491454721196053L;
      else
         serialVersionUID = 3636148327800330130L;
   }

   /**
    * Log reference.
    */
   private static final Logger log = Logger.getLogger(MLet.class);

   // FIXME: (RI javadoc) Note -  The MLet class loader uses the DefaultLoaderRepository
   //        to load classes that could not be found in the loaded jar files.
   //
   // IOW we need to override findClass for this cl...
   // I think we can avoid the ugly dlr field hack from RI

   
   // Attributes ----------------------------------------------------
   
   /** Reference to the MBean server this loader is registered to. */
   private MBeanServer server    = null;
   
   /** MBean installer based on MLet version. */
   private MBeanInstaller installer = null;

   /** Our wrapping classloader */
   private RepositoryClassLoader rcl = null;

   /**
    * Boolean field that indicates whether the MLet should delegate classloading
    * to classloader repository in case the requested class is not found from
    * its own set of loaded classes. Defaults to true.
    */
   private boolean delegateToCLR = true;
   
   /**
    * Library directory for native libs.
    */
   private String libraryDir = null;
   
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------

   public MLet()
   {
      super(new URL[0], Thread.currentThread().getContextClassLoader());
   }

   public MLet(URL[] urls)
   {
      super(urls, Thread.currentThread().getContextClassLoader());
   }

   public MLet(URL[] urls, ClassLoader parent)
   {
      super(urls, parent);
   }

   public MLet(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory)
   {
      super(urls, parent, factory);
   }

   /*
    * JMX 1.2 spec change notes:
    *
    *    Each constructor for the MLet class acquires an overloaded version
    *    with a booleanparameter delegateToCLR, which defaults to true.  When
    *    false, the MLet does not delegate to the CLR when it fails to find
    *    classes but directly throws ClassNotFoundException.
    */

   public MLet(URL[] urls, boolean delegateToCLR)
   {
      super(urls, Thread.currentThread().getContextClassLoader());
      
      this.delegateToCLR = delegateToCLR;
   }

   public MLet(URL[] urls, ClassLoader parent, boolean delegateToCLR)
   {
      super(urls, parent);
      
      this.delegateToCLR = delegateToCLR;
   }

   public MLet(URL[] urls, ClassLoader parent,
               URLStreamHandlerFactory factory, boolean delegateToCLR)
   {
      super(urls, parent, factory);
      
      this.delegateToCLR = delegateToCLR;
   }

   
   // MBeanRegistration implementation ------------------------------
   
   public ObjectName preRegister(MBeanServer server, ObjectName name) throws  Exception
   {
      if (name == null)
      {
         String defaultDomain = server.getDefaultDomain();
         name = new ObjectName(defaultDomain + ":type=MLet");
      }

      this.server     = server;
      this.installer  = new MBeanInstaller(server, this, name);

      return name;
   }

   public void postRegister(Boolean registrationDone)
   {
   }

   public void preDeregister() throws Exception
   {
   }

   public void postDeregister()
   {
      server = null;
      rcl = null;
      installer = null;
   }

   
   // MLetMBean implementation --------------------------------------
   
   public Set getMBeansFromURL(String url) throws ServiceNotFoundException
   {
      try
      {
         return getMBeansFromURL(new URL(url));
      }
      catch (MalformedURLException e) 
      {
         throw new ServiceNotFoundException("Malformed URL:" + url);
      }
   }

   public Set getMBeansFromURL(URL url) throws ServiceNotFoundException
   {
      if (server == null)
         throw new ServiceNotFoundException("Loader must be registered to the server before loading the MBeans.");

      HashSet mbeans        = new HashSet();
      MBeanElement element  = null;

      try 
      {
         MBeanFileParser parser = new MLetParser();
         Set mlets              = parser.parseMBeanFile(url);
         
         if (mlets.size() == 0)
            throw new ServiceNotFoundException("The specified URL '" + url + "' does not contain MLET tags.");
            
         Iterator it = mlets.iterator();
         while (it.hasNext())
         {
            element = (MBeanElement)it.next();
            
            // pass delegateToCLR property to the MBean installer
            element.setProperty(MBeanElement.MLET_DELEGATE_TO_CLR,
                                new Boolean(delegateToCLR));
                                
            String codebase = element.getCodebase();
            
            // if no codebase is specified then the url of the mlet text file is used
            if (codebase == null)
               codebase = url.toString().substring(0, url.toString().lastIndexOf('/'));

            Iterator archives  = element.getArchives().iterator();
            String codebaseURL = null;
            
            while (archives.hasNext())
            {
               try
               {
                  codebaseURL = codebase + ((codebase.endsWith("/")) ? "" : "/") + archives.next();
                     
                  addURL(new URL(url, codebaseURL));
               }
               catch (MalformedURLException e)
               {
                  log.error("MLET ERROR: malformed codebase URL: '" + codebaseURL + "'");
               }
            }
               
            try
            {
               // FIXME: see the note at the beginning... we use an explicit loader
               //        in the createMBean() call to force this classloader to
               //        be used first to load all MLet classes. Normally this form
               //        of createMBean() call will not delegate to DLR even though
               //        the javadoc requires it. Therefore the findClass() should
               //        be overridden to delegate to the repository. 
               /*
               mbeans.add(server.createMBean(
                     element.getCode(),
                     (element.getName() != null) ? new ObjectName(element.getName()) : null,
                     objectName,
                     element.getConstructorValues(),
                     element.getConstructorTypes())
               );
               */

               // installer creates or upgrades this mbean based on the MLet version
               mbeans.add(installer.installMBean(element));
            }
            catch (Throwable t)
            {
               // if mbean can't be created, throwable is added to the return set
               mbeans.add(t);

               log.error("MLET ERROR: can't create MBean: ", t);
            }
         }
      }
      catch (ParseException e)
      {
         throw new ServiceNotFoundException(e.getMessage());
      }

      return mbeans;
   }

   public void addURL(URL url)
   {
      super.addURL(url);
      if (rcl == null && server != null)
         getRepositoryClassLoader();
      rcl.addURL(url);
   }

   public void addURL(String url) throws ServiceNotFoundException
   {
      try
      {
         this.addURL(new URL(url));
      }
      catch (MalformedURLException e)
      {
         throw new ServiceNotFoundException("Malformed URL: " + url);
      }
   }

   /** Returns the search path of URLs for loading classes and resources. This
    * includes the original list of URLs specified to the constructor, along
    * with any URLs subsequently appended by the addURL() method.
    *  
    * @return
    */ 
   public URL[] getURLs()
   {
      return super.getURLs();
   }

   public String getLibraryDirectory()
   {
      return libraryDir;
   }

   public void setLibraryDirectory(String libdir)
   {
      this.libraryDir = libdir;
   }

   
   // Externalizable implementation ---------------------------------
   
   /* Part of JMX 1.2 specification */
   
   // The spec does not require implementations of the externalizable interface
   // in which case the readExternal() and writeExternal() methods may throw
   // an unsupported operation exception
   
   /**
    * This implementation does not support externalizing an MLet.
    *
    * @throws UnsupportedOperationException
    */
   public void readExternal(ObjectInput in) throws IOException,
         ClassNotFoundException, UnsupportedOperationException
   {
      throw new UnsupportedOperationException("MLet serialization not supported.");   
   }
   
   /**
    * This implementation does not support externalizing an MLet.
    *
    * @throws UnsupportedOperationException
    */
   public void writeExternal(ObjectOutput out) 
         throws IOException, UnsupportedOperationException
   {
      throw new UnsupportedOperationException("MLet serialization not supported.");
   }
   
   
   // Classloader overrides -----------------------------------------

   /** Load a class, using the given ClassLoaderRepository if the class is not
    * found in this MLet's URLs. The given ClassLoaderRepository can be null,
    * in which case a ClassNotFoundException occurs immediately if the class is
    * not found in this MLet's URLs.
    * 
    * @param name
    * @param clr
    * @return
    * @throws ClassNotFoundException
    */ 
   public Class loadClass(String name, ClassLoaderRepository clr)
      throws ClassNotFoundException
   {
      Class c = null;
      try
      {
         c = loadClass(name);
      }
      catch(ClassNotFoundException e)
      {
         if( clr != null )
            c = clr.loadClass(name);
         else
            throw e;
      }
      return c;
   }

   /** This method is to be overridden when extending this service to support
    * caching and versioning. It is called from getMBeansFromURL when the
    * version, codebase, and jarfile have been extracted from the MLet file,
    * and can be used to verify that it is all right to load the given MBean,
    * or to replace the given URL with a different one.
    * 
    * The default implementation of this method returns codebase unchanged.
    * 
    * @param version
    * @param codebase
    * @param jarfile
    * @param mlet
    * @return
    * @throws Exception
    */ 
   protected URL check(String version, URL codebase, String jarfile,
      MLetContent mlet)
      throws Exception
   {
      return codebase;
   }


   protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("loadClass " + this + " name=" + name);
      Class clazz = null;
      try
      {
         clazz = super.loadClass(name, resolve);
         return clazz;
      }
      finally
      {
         if (trace)
         {
            if (clazz != null)
               log.trace("loadClass " + this + " name=" + name + " class=" + clazz + " cl=" + clazz.getClassLoader());
            else
               log.trace("loadClass " + this + " name=" + name + " not found");
         }
      }
   }

   protected Class findClass(final String name)
      throws ClassNotFoundException
   {
      try
      {
         return super.findClass(name);
      }
      catch (ClassNotFoundException e)
      {
         if (delegateToCLR)
         {
            try
            {
               return (Class) AccessController.doPrivileged(new PrivilegedExceptionAction()
               {
                  public Object run() throws ClassNotFoundException
                  {
                     return server.getClassLoaderRepository().loadClassBefore(MLet.this, name);
                  }
               });
            }
            catch (PrivilegedActionException pe)
            {
               throw (ClassNotFoundException) pe.getException();
            }
         }
         else
            throw e;
      }
   }

   protected String findLibrary(String libname)
   {
      return super.findLibrary(libname);
   }

   private void getRepositoryClassLoader()
   {
      try
      {
         ObjectName loader = ObjectNameFactory.create(ServerConstants.DEFAULT_LOADER_NAME);
         rcl = (RepositoryClassLoader) server.invoke(loader, "getWrappingClassLoader",
               new Object[] { this }, new String[] { ClassLoader.class.getName() });
         log.debug("MLet " + this + " using wrapper " + rcl);
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }
}
