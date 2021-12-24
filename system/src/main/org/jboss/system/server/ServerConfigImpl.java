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
package org.jboss.system.server;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.jboss.util.NestedRuntimeException;
import org.jboss.util.Null;
import org.jboss.util.Primitives;
import org.jboss.util.platform.Java;

/**
 * A container for the basic configuration elements required to create
 * a Server instance.
 *
 * <p>MalformedURLException are rethrown as NestedRuntimeExceptions, so that
 *    code that needs to access these values does not have to directly
 *    worry about problems with lazy construction of final URL values.
 *
 * <p>Most values are determined durring first call to getter.  All values
 *    when determined will have equivilent system properties set.
 *
 * <p>Clients are not meant to use this class directly.  Instead use
 *    {@link ServerConfigLocator} to get an instance of {@link ServerConfig}
 *    and then use it to get the server's configuration bits.
 *
 * @jmx:mbean name="jboss.system:type=ServerConfig"
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author Scott.Stark@jboss.org
 * @version <tt>$Revision: 59410 $</tt>
 */
public class ServerConfigImpl
      implements ServerConfig, ServerConfigImplMBean
{
   /** The configuration properties to pull data from. */
   private Properties props;

   private File homeDir;
   private URL homeURL;
   private URL libraryURL;

   /**
    * The base URL where patch files will be loaded from. This is
    * typed as an Object to allow its value to contain Null.VALUE
    * or a URL.  If value is Null.VALUE then we have determined
    * that there is no user configuration for this value and it will
    * be passed back as null to the requesting client.
    */
   private Object patchURL;

   private String serverSpecificationVersion;

   private String serverName;
   private File serverBaseDir;
   private File serverHomeDir;
   private File serverLogDir;
   private File serverTempDir;
   private File serverDataDir;
   private URL serverBaseURL;
   private URL serverHomeURL;
   private URL serverLibraryURL;
   private URL serverConfigURL;
   
   /** Exit on shutdown flag. */
   private Boolean exitOnShutdown;
   private Boolean blockingShutdown;
   private Boolean requireJBossURLStreamHandlerFactory;
   private Boolean platformMBeanServer;

   private String rootDeployableFilename;

   /**
    * Construct a new <tt>ServerConfigImpl</tt> instance.
    *
    * @param props    Configuration properties.
    *
    * @throws Exception    Missing or invalid configuration.
    */
   public ServerConfigImpl(final Properties props) throws Exception
   {
      this.props = props;

      // Must have HOME_DIR
      homeDir = getFile(ServerConfig.HOME_DIR);
      if (homeDir == null)
         throw new Exception("Missing configuration value for: " + ServerConfig.HOME_DIR);
      System.setProperty(ServerConfig.HOME_DIR, homeDir.toString());
      // Setup the SERVER_HOME_DIR system property
      getServerHomeDir();
      
      Package thisPackage = getClass().getPackage();
      serverSpecificationVersion = thisPackage.getSpecificationVersion();
   }

   /** Breakout the initialization of URLs from the constructor as we need
    * the ServerConfig.HOME_DIR set for log setup, but we cannot create any
    * file URLs prior to the
    */
   public void initURLs()
      throws MalformedURLException
   {
      // If not set then default to homeDir
      homeURL = getURL(ServerConfig.HOME_URL);
      if (homeURL == null)
         homeURL = homeDir.toURL();
      System.setProperty(ServerConfig.HOME_URL, homeURL.toString());
   }

   /////////////////////////////////////////////////////////////////////////
   //                             Typed Access                            //
   /////////////////////////////////////////////////////////////////////////

   /**
    * Get the server Specification-Version
    * @jmx:managed-attribute
    */
   public String getSpecificationVersion()
   {
      return serverSpecificationVersion;
   }

   /**
    * Get the local home directory which the server is running from.
    *
    * @jmx:managed-attribute
    */
   public File getHomeDir()
   {
      return homeDir;
   }

   /**
    * Get the home URL which the server is running from.
    *
    * @jmx:managed-attribute
    */
   public URL getHomeURL()
   {
      return homeURL;
   }

   /**
    * Get the home URL which the server is running from.
    *
    * @jmx:managed-attribute
    */
   public URL getLibraryURL()
   {
      if (libraryURL == null)
      {
         try
         {
            libraryURL = getURL(ServerConfig.LIBRARY_URL);
            if (libraryURL == null)
            {
               libraryURL = new URL(homeURL, ServerConfig.LIBRARY_URL_SUFFIX);
            }
            System.setProperty(ServerConfig.LIBRARY_URL, libraryURL.toString());
         }
         catch (MalformedURLException e)
         {
            throw new NestedRuntimeException(e);
         }
      }
      return libraryURL;
   }

   /**
    * Get the patch URL for the server.
    *
    * @jmx:managed-attribute
    */
   public URL getPatchURL()
   {
      if (patchURL == null)
      {
         try
         {
            patchURL = getURL(ServerConfig.PATCH_URL);
            if (patchURL == null)
            {
               patchURL = Null.VALUE;
            }
            else
            {
               System.setProperty(ServerConfig.PATCH_URL, patchURL.toString());
            }
         }
         catch (MalformedURLException e)
         {
            throw new NestedRuntimeException(e);
         }
      }

      if (patchURL == Null.VALUE)
         return null;

      return (URL) patchURL;
   }

   /**
    * Get the name of the server.
    *
    * @jmx:managed-attribute
    */
   public String getServerName()
   {
      if (serverName == null)
      {
         serverName = props.getProperty(ServerConfig.SERVER_NAME, ServerConfig.DEFAULT_SERVER_NAME);
         System.setProperty(ServerConfig.SERVER_NAME, serverName);
      }
      return serverName;
   }

   /**
    * Get the base directory for calculating server home directories.
    *
    * @jmx:managed-attribute
    */
   public File getServerBaseDir()
   {
      if (serverBaseDir == null)
      {
         serverBaseDir = getFile(ServerConfig.SERVER_BASE_DIR);
         if (serverBaseDir == null)
         {
            serverBaseDir = new File(homeDir, ServerConfig.SERVER_BASE_DIR_SUFFIX);
            System.setProperty(ServerConfig.SERVER_BASE_DIR, serverBaseDir.toString());
         }
      }
      return serverBaseDir;
   }

   /**
    * Get the server home directory.
    *
    * @jmx:managed-attribute
    */
   public File getServerHomeDir()
   {
      if (serverHomeDir == null)
      {
         serverHomeDir = getFile(ServerConfig.SERVER_HOME_DIR);
         if (serverHomeDir == null)
         {
            serverHomeDir = new File(getServerBaseDir(), getServerName());
            System.setProperty(ServerConfig.SERVER_HOME_DIR, serverHomeDir.toString());
         }
      }
      return serverHomeDir;
   }

   /**
    * Get the directory where temporary files will be stored. The associated
    * ServerConfig.SERVER_LOG_DIR system property needs to be set before
    * the logging framework is used.
    *
    * @see ServerConfig.SERVER_LOG_DIR
    * @return the writable temp directory
    */
   public File getServerLogDir()
   {
      if (serverLogDir == null)
      {
         serverLogDir = getFile(ServerConfig.SERVER_LOG_DIR);
         if (serverLogDir == null)
         {
            serverLogDir = new File(getServerHomeDir(), ServerConfig.SERVER_LOG_DIR_SUFFIX);
            System.setProperty(ServerConfig.SERVER_LOG_DIR, serverLogDir.toString());
         }
      }
      return serverLogDir;
   }

   /**
    * Get the directory where temporary files will be stored.
    *
    * @jmx:managed-attribute
    * @return the writable temp directory
    */
   public File getServerTempDir()
   {
      if (serverTempDir == null)
      {
         serverTempDir = getFile(ServerConfig.SERVER_TEMP_DIR);
         if (serverTempDir == null)
         {
            serverTempDir = new File(getServerHomeDir(), ServerConfig.SERVER_TEMP_DIR_SUFFIX);
            System.setProperty(ServerConfig.SERVER_TEMP_DIR, serverTempDir.toString());
         }
      }
      return serverTempDir;
   }

   /**
    * Get the directory where local data will be stored.
    *
    * @jmx:managed-attribute
    * @return the data directory
    */
   public File getServerDataDir()
   {
      if (serverDataDir == null)
      {
         serverDataDir = getFile(ServerConfig.SERVER_DATA_DIR);
         if (serverDataDir == null)
         {
            serverDataDir = new File(getServerHomeDir(), ServerConfig.SERVER_DATA_DIR_SUFFIX);
            System.setProperty(ServerConfig.SERVER_DATA_DIR, serverDataDir.toString());
         }
      }
      return serverDataDir;
   }

   /**
    * Get the native dir for unpacking
    * 
    * @jmx:managed-attribute
    * @return the directory
    */
   public File getServerNativeDir()
   {
      String fileName = System.getProperty(NATIVE_DIR_PROPERTY);
      if (fileName != null)
         return new File(fileName);
      return new File(getServerTempDir(), "native");
   }

   /**
    * Get the temporary deployment dir for unpacking
    * 
    * @jmx:managed-attribute
    * @return the directory
    */
   public File getServerTempDeployDir()
   {
      return new File(getServerTempDir(), "deploy");
   }

   /**
    * Get the base directory for calculating server home URLs.
    *
    * @jmx:managed-attribute
    */
   public URL getServerBaseURL()
   {
      if (serverBaseURL == null)
      {
         try
         {
            serverBaseURL = getURL(ServerConfig.SERVER_BASE_URL);
            if (serverBaseURL == null)
            {
               serverBaseURL = new URL(homeURL, ServerConfig.SERVER_BASE_URL_SUFFIX);
            }
            System.setProperty(ServerConfig.SERVER_BASE_URL, serverBaseURL.toString());
         }
         catch (MalformedURLException e)
         {
            throw new NestedRuntimeException(e);
         }
      }
      return serverBaseURL;
   }

   /**
    * Get the server home URL.
    *
    * @jmx:managed-attribute
    */
   public URL getServerHomeURL()
   {
      if (serverHomeURL == null)
      {
         try
         {
            serverHomeURL = getURL(ServerConfig.SERVER_HOME_URL);
            if (serverHomeURL == null)
            {
               serverHomeURL = new URL(getServerBaseURL(), getServerName() + "/");
            }
            System.setProperty(ServerConfig.SERVER_HOME_URL, serverHomeURL.toString());
         }
         catch (MalformedURLException e)
         {
            throw new NestedRuntimeException(e);
         }
      }
      return serverHomeURL;
   }

   /**
    * Get the server library URL.
    *
    * @jmx:managed-attribute
    */
   public URL getServerLibraryURL()
   {
      if (serverLibraryURL == null)
      {
         try
         {
            serverLibraryURL = getURL(ServerConfig.SERVER_LIBRARY_URL);
            if (serverLibraryURL == null)
            {
               serverLibraryURL = new URL(getServerHomeURL(), ServerConfig.LIBRARY_URL_SUFFIX);
            }
            System.setProperty(ServerConfig.SERVER_LIBRARY_URL, serverLibraryURL.toString());
         }
         catch (MalformedURLException e)
         {
            throw new NestedRuntimeException(e);
         }
      }
      return serverLibraryURL;
   }

   /**
    * Get the server configuration URL.
    *
    * @jmx:managed-attribute
    */
   public URL getServerConfigURL()
   {
      if (serverConfigURL == null)
      {
         try
         {
            serverConfigURL = getURL(ServerConfig.SERVER_CONFIG_URL);
            if (serverConfigURL == null)
            {
               serverConfigURL = new URL(getServerHomeURL(), ServerConfig.SERVER_CONFIG_URL_SUFFIX);
            }
            System.setProperty(ServerConfig.SERVER_CONFIG_URL, serverConfigURL.toString());
         }
         catch (MalformedURLException e)
         {
            throw new NestedRuntimeException(e);
         }
      }
      return serverConfigURL;
   }

   /**
    * Get the current value of the flag that indicates if we are
    * using the platform MBeanServer as the main jboss server.
    * Both the {@link ServerConfig.PLATFORM_MBEANSERVER}
    * property must be set, and the jvm must be jdk1.5+
    * 
    * @return true if jboss runs on the jvm platfrom MBeanServer
    * 
    * @jmx:managed-attribute
    */
   public boolean getPlatformMBeanServer()
   {
      if (platformMBeanServer == null)
      {
         if (Java.isCompatible(Java.VERSION_1_5))
         {
            // get whatever the user has specified or the default
            String value = props.getProperty(ServerConfig.PLATFORM_MBEANSERVER,
               (new Boolean(ServerConfig.DEFAULT_PLATFORM_MBEANSERVER)).toString());
            
            // treat empty string as true
            value = "".equals(value) ? "true" : value;
            
            // true or false
            platformMBeanServer = new Boolean(value);
         }
         else
         {
            // negative :)
            platformMBeanServer = Boolean.FALSE;
         }
      }
      return platformMBeanServer.booleanValue();
   }
   
   /**
    * Enable or disable exiting the JVM when {@link Server#shutdown} is called.
    * If enabled, then shutdown calls {@link Server#exit}.  If disabled, then
    * only the shutdown hook will be run.
    *
    * @param flag    True to enable calling exit on shutdown.
    *
    * @jmx:managed-attribute
    */
   public void setExitOnShutdown(final boolean flag)
   {
      exitOnShutdown = Primitives.valueOf(flag);
   }

   /**
    * Get the current value of the exit on shutdown flag.
    *
    * @return    The current value of the exit on shutdown flag.
    *
    * @jmx:managed-attribute
    */
   public boolean getExitOnShutdown()
   {
      if (exitOnShutdown == null)
      {
         String value = props.getProperty(ServerConfig.EXIT_ON_SHUTDOWN, null);
         if (value == null)
         {
            exitOnShutdown = Primitives.valueOf(ServerConfig.DEFAULT_EXIT_ON_SHUTDOWN);
         }
         else
         {
            exitOnShutdown = new Boolean(value);
         }
      }
      return exitOnShutdown.booleanValue();
   }

   /**
    * Enable or disable blocking when {@link Server#shutdown} is
    * called.  If enabled, then shutdown will be called in the current
    * thread.  If disabled, then the shutdown hook will be run
    * ansynchronously in a separate thread.
    *
    * @param flag    True to enable blocking shutdown.
    *
    * @jmx:managed-attribute
    */
   public void setBlockingShutdown(final boolean flag)
   {
      blockingShutdown = Primitives.valueOf(flag);
   }

   /**
    * Get the current value of the blocking shutdown flag.
    *
    * @return    The current value of the blocking shutdown flag.
    *
    * @jmx:managed-attribute
    */
   public boolean getBlockingShutdown()
   {
      if (blockingShutdown == null)
      {
         String value = props.getProperty(ServerConfig.BLOCKING_SHUTDOWN, null);
         if (value == null)
         {
            blockingShutdown = Primitives.valueOf(ServerConfig.DEFAULT_BLOCKING_SHUTDOWN);
         }
         else
         {
            blockingShutdown = new Boolean(value);
         }
      }
      return blockingShutdown.booleanValue();
   }


   /**
    * Set the RequireJBossURLStreamHandlerFactory flag.  if false,
    * exceptions when setting the URLStreamHandlerFactory will be
    * logged and ignored.
    *
    * @param flag    True to enable blocking shutdown.
    *
    * @jmx:managed-attribute
    */
   public void setRequireJBossURLStreamHandlerFactory(final boolean flag)
   {
      requireJBossURLStreamHandlerFactory = Primitives.valueOf(flag);
   }

   /**
    * Get the current value of the requireJBossURLStreamHandlerFactory flag.
    *
    * @return    The current value of the requireJBossURLStreamHandlerFactory flag.
    *
    * @jmx:managed-attribute
    */
   public boolean getRequireJBossURLStreamHandlerFactory()
   {
      if (requireJBossURLStreamHandlerFactory == null)
      {
         String value = props.getProperty(ServerConfig.REQUIRE_JBOSS_URL_STREAM_HANDLER_FACTORY, null);
         if (value == null)
         {
            requireJBossURLStreamHandlerFactory = Primitives.valueOf(ServerConfig.DEFAULT_REQUIRE_JBOSS_URL_STREAM_HANDLER_FACTORY);
         }
         else
         {
            requireJBossURLStreamHandlerFactory = new Boolean(value);
         }
      }
      return requireJBossURLStreamHandlerFactory.booleanValue();
   }

   /**
    * Set the filename of the root deployable that will be used to finalize
    * the bootstrap process.
    *
    * @param filename    The filename of the root deployable.
    *
    * @jmx:managed-attribute
    */
   public void setRootDeploymentFilename(final String filename)
   {
      this.rootDeployableFilename = filename;
   }

   /**
    * Get the filename of the root deployable that will be used to finalize
    * the bootstrap process.
    *
    * @return    The filename of the root deployable.
    *
    * @jmx:managed-attribute
    */
   public String getRootDeploymentFilename()
   {
      if (rootDeployableFilename == null)
      {
         rootDeployableFilename = props.getProperty(ServerConfig.ROOT_DEPLOYMENT_FILENAME,
               ServerConfig.DEFAULT_ROOT_DEPLOYMENT_FILENAME);
      }

      return rootDeployableFilename;
   }

   /**
    * Get a URL from configuration.
    */
   private URL getURL(final String name) throws MalformedURLException
   {
      String value = props.getProperty(name, null);
      if (value != null)
      {
         if (!value.endsWith("/")) value += "/";
         return new URL(value);
      }

      return null;
   }

   /**
    * Get a File from configuration.
    * @return the CanonicalFile form for the given name.
    */
   private File getFile(final String name)
   {
      String value = props.getProperty(name, null);
      if (value != null)
      {
         try
         {
            File f = new File(value);
            return f.getCanonicalFile();
         }
         catch(IOException e)
         {
            return new File(value);            
         }
      }

      return null;
   }
}
