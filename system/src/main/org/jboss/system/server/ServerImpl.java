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
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.LogManager;

import javax.management.Attribute;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.jboss.Version;
import org.jboss.deployment.IncompleteDeploymentException;
import org.jboss.deployment.MainDeployerMBean;
import org.jboss.logging.JBossJDKLogManager;
import org.jboss.logging.Logger;
import org.jboss.mx.loading.RepositoryClassLoader;
import org.jboss.mx.server.ServerConstants;
import org.jboss.mx.util.JBossNotificationBroadcasterSupport;
import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.net.protocol.URLStreamHandlerFactory;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.system.server.jmx.LazyMBeanServer;
import org.jboss.util.StopWatch;
import org.jboss.util.file.FileSuffixFilter;
import org.jboss.util.file.Files;

/**
 * The main container component of a JBoss server instance.
 *
 * <h3>Concurrency</h3>
 * This class is <b>not</b> thread-safe.
 *
 * @jmx:mbean name="jboss.system:type=Server"
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 59334 $
 */
public class ServerImpl
   implements Server, ServerImplMBean, NotificationEmitter
{
   private final static ObjectName DEFAULT_LOADER_NAME =
      ObjectNameFactory.create(ServerConstants.DEFAULT_LOADER_NAME);

   /** Instance logger. */
   private Logger log;

   /** Container for version information. */
   private final Version version = Version.getInstance();

   /** Package information for org.jboss */
   private final Package jbossPackage = Package.getPackage("org.jboss");

   /** The basic configuration for the server. */
   private ServerConfigImpl config;

   /** The JMX MBeanServer which will serve as our communication bus. */
   private MBeanServer server;

   /** When the server was started. */
   private Date startDate;

   /** Flag to indicate if we are started. */
   private boolean started;

   /** The JVM shutdown hook */
   private ShutdownHook shutdownHook;

   /** The JBoss Life Thread */
   private LifeThread lifeThread;

   /** The NotificationBroadcaster implementation delegate */
   private JBossNotificationBroadcasterSupport broadcasterSupport;
   
   /** The bootstrap UCL class loader ObjectName */
   private ObjectName bootstrapUCLName;
   /** A flag indicating if shutdown has been called */
   private boolean isInShutdown;

   /**
    * No-arg constructor for {@link ServerLoader}.
    */
   public ServerImpl()
   {
   }

   /**
    * Initialize the Server instance.
    *
    * @param props     The configuration properties for the server.
    *
    * @throws IllegalStateException    Already initialized.
    * @throws Exception                Failed to initialize.
    */
   public void init(final Properties props) throws IllegalStateException, Exception
   {
      if (props == null)
         throw new IllegalArgumentException("props is null");
      if (config != null)
         throw new IllegalStateException("already initialized");

      ClassLoader oldCL = Thread.currentThread().getContextClassLoader();

      try
      {
         Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
         doInit(props);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCL);
      }
   }

   /** Actually does the init'ing... */
   private void doInit(final Properties props) throws Exception
   {
      // Create a new config object from the give properties
      this.config = new ServerConfigImpl(props);

      /* Initialize the logging layer using the Server.class. The server log
      directory is initialized prior to this to ensure the jboss.server.log.dir
      system property is set in case its used by the logging configs.
      */
      config.getServerLogDir();
      log = Logger.getLogger(Server.class);

      // Create the NotificationBroadcaster delegate
      broadcasterSupport = new JBossNotificationBroadcasterSupport();
      
      // Set the VM temp directory to the server tmp dir
      boolean overrideTmpDir = Boolean.getBoolean("jboss.server.temp.dir.overrideJavaTmpDir");
      if( overrideTmpDir )
      {
         File serverTmpDir = config.getServerTempDir();
         System.setProperty("java.io.tmpdir", serverTmpDir.getCanonicalPath());
      }

      // Setup URL handlers - do this before initializing the ServerConfig
      initURLHandlers();
      config.initURLs();

      log.info("Starting JBoss (MX MicroKernel)...");

      if (jbossPackage != null)
      {
         // Show what release this is...
         log.info("Release ID: " +
                  jbossPackage.getImplementationTitle() + " " +
                  jbossPackage.getImplementationVersion());
      }
      else
      {
         log.warn("could not get package info to display release, either the " +
            "jar manifest in jboss-system.jar has been mangled or you're " +
            "running unit tests from ant outside of JBoss itself.");
      }

      log.debug("Using config: " + config);

      // make sure our impl type is exposed
      log.debug("Server type: " + getClass());

      // log the boot classloader
      ClassLoader cl = getClass().getClassLoader();
      log.debug("Server loaded through: " + cl.getClass().getName());
      
      // log the boot URLs
      if (cl instanceof NoAnnotationURLClassLoader)
      {
         NoAnnotationURLClassLoader nacl = (NoAnnotationURLClassLoader)cl;
         URL[] bootURLs = nacl.getAllURLs();
         log.debug("Boot URLs:");
         for (int i = 0; i < bootURLs.length; i++)
         {
            log.debug("  " + bootURLs[i]);
         }
      }
      
      // Log the basic configuration elements
      log.info("Home Dir: " + config.getHomeDir());
      log.info("Home URL: " + config.getHomeURL());
      log.debug("Library URL: " + config.getLibraryURL());
      log.info("Patch URL: " + config.getPatchURL());
      log.info("Server Name: " + config.getServerName());
      log.info("Server Home Dir: " + config.getServerHomeDir());
      log.info("Server Home URL: " + config.getServerHomeURL());
      log.info("Server Log Dir: " + config.getServerLogDir());
      log.debug("Server Data Dir: " + config.getServerDataDir());
      log.info("Server Temp Dir: " + config.getServerTempDir());
      log.debug("Server Config URL: " + config.getServerConfigURL());
      log.debug("Server Library URL: " + config.getServerLibraryURL());
      log.info("Root Deployment Filename: " + config.getRootDeploymentFilename());
   }

   /**
    * The <code>initURLHandlers</code> method calls
    * internalInitURLHandlers.  if requireJBossURLStreamHandlers is
    * false, any exceptions are logged and ignored.
    *
    */
   private void initURLHandlers()
   {
      if (config.getRequireJBossURLStreamHandlerFactory())
      {
         internalInitURLHandlers();
      } // end of if ()
      else
      {
         try
         {
            internalInitURLHandlers();
         }
         catch (SecurityException e)
         {
            log.warn("You do not have permissions to set URLStreamHandlerFactory", e);
         } // end of try-catch
         catch (Error e)
         {
            log.warn("URLStreamHandlerFactory already set", e);
         } // end of catch
      } // end of else
   }

   /**
    * Set up our only URLStreamHandlerFactory.
    * This is needed to ensure Sun's version is not used (as it leaves files
    * locked on Win2K/WinXP platforms.
    */
   private void internalInitURLHandlers()
   {
      try
      {
         // Install a URLStreamHandlerFactory that uses the TCL
         URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory());
   
         // Preload JBoss URL handlers
         URLStreamHandlerFactory.preload();
      }
      catch (Error error)
      { //very naughty but we HAVE to do this or
         //we'll fail if we ever try to do this again
         log.warn("Caught Throwable Error, this probably means " +
            "we've already set the URLStreamHAndlerFactory before"); 
         //Sys.out because we don't have logging yet
      }

      // Include the default JBoss protocol handler package
      String handlerPkgs = System.getProperty("java.protocol.handler.pkgs");
      if (handlerPkgs != null)
      {
         handlerPkgs += "|org.jboss.net.protocol";
      }
      else
      {
         handlerPkgs = "org.jboss.net.protocol";
      }
      System.setProperty("java.protocol.handler.pkgs", handlerPkgs);
   }

   /**
    * Get the typed server configuration object which the
    * server has been initalized to use.
    *
    * @return          Typed server configuration object.
    *
    * @throws IllegalStateException    Not initialized.
    */
   public ServerConfig getConfig() throws IllegalStateException
   {
      if (config == null)
         throw new IllegalStateException("not initialized");

      return config;
   }

   /**
    * Check if the server is started.
    *
    * @return   True if the server is started, else false.
    * @jmx:managed-attribute
    */
   public boolean isStarted()
   {
      return started;
   }

   /**
    Check if the shutdown operation has been called/is in progress.

    @return true if shutdown has been called, false otherwise
    */
   public boolean isInShutdown()
   {
      return isInShutdown;
   }

   /**
    * Start the Server instance.
    *
    * @throws IllegalStateException    Already started or not initialized.
    * @throws Exception                Failed to start.
    */
   public void start() throws IllegalStateException, Exception
   {
      // make sure we are initialized
      getConfig();

      // make sure we aren't started yet
      if (started)
         throw new IllegalStateException("already started");

      ClassLoader oldCL = Thread.currentThread().getContextClassLoader();

      try
      {
         Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

         // Deal with those pesky JMX throwables
         try
         {
            doStart();
         }
         catch (Exception e)
         {
            JMXExceptionDecoder.rethrow(e);
         }
      }
      catch (Throwable t)
      {
         log.debug("Failed to start", t);

         if (t instanceof Exception)
            throw (Exception)t;
         if (t instanceof Error)
            throw (Error)t;

         throw new org.jboss.util.UnexpectedThrowable(t);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCL);
      }
   }

   /** Actually does the starting... */
   private void doStart() throws Exception
   {
      // See how long it takes us to start up
      StopWatch watch = new StopWatch(true);

      // remeber when we we started
      startDate = new Date();

      log.debug("Starting General Purpose Architecture (GPA)...");

      // Create the MBeanServer
      String builder = System.getProperty(ServerConstants.MBEAN_SERVER_BUILDER_CLASS_PROPERTY,
                                          ServerConstants.DEFAULT_MBEAN_SERVER_BUILDER_CLASS);
      System.setProperty(ServerConstants.MBEAN_SERVER_BUILDER_CLASS_PROPERTY, builder);
      
      // Check if we'll use the platform MBeanServer or instantiate our own
      if (config.getPlatformMBeanServer() == true)
      {
         // jdk1.5+
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         Class clazz = cl.loadClass("java.lang.management.ManagementFactory");
         Method method = clazz.getMethod("getPlatformMBeanServer", null);
         server = (MBeanServer)method.invoke(null, null);
         // Tell the MBeanServerLocator to point to this server
         MBeanServerLocator.setJBoss(server);
         /* If the LazyMBeanServer was used, we need to reset to the jboss
         MBeanServer to use our implementation for the jboss services.
         */
         server = LazyMBeanServer.resetToJBossServer(server);
      }
      else
      {
         // Create our own MBeanServer
         server = MBeanServerFactory.createMBeanServer("jboss");
      }
      log.debug("Created MBeanServer: " + server);      

      // Register server components
      server.registerMBean(this, ServerImplMBean.OBJECT_NAME);
      server.registerMBean(config, ServerConfigImplMBean.OBJECT_NAME);

      // Initialize spine boot libraries
      RepositoryClassLoader ucl = initBootLibraries();
      bootstrapUCLName = ucl.getObjectName();
      server.registerMBean(ucl, bootstrapUCLName);

      // Set ServiceClassLoader as classloader for the construction of
      // the basic system
      Thread.currentThread().setContextClassLoader(ucl);

      // General Purpose Architecture information
      createMBean("org.jboss.system.server.ServerInfo",
         "jboss.system:type=ServerInfo");

      // Service Controller
      ObjectName controller = createMBean("org.jboss.system.ServiceController",
         "jboss.system:service=ServiceController");

      // Main Deployer
      ObjectName mainDeployer = startBootService(controller,
         "org.jboss.deployment.MainDeployer", "jboss.system:service=MainDeployer");
      server.setAttribute(mainDeployer,
                          new Attribute("ServiceController", controller));

      // Install the shutdown hook
      shutdownHook = new ShutdownHook(controller, mainDeployer);
      shutdownHook.setDaemon(true);

      try
      {
         Runtime.getRuntime().addShutdownHook(shutdownHook);
         log.debug("Shutdown hook added");
      }
      catch (Exception e)
      {
         log.warn("Failed to add shutdown hook; ignoring", e);
      }

      // JARDeployer, required to process <classpath>
      startBootService(controller, "org.jboss.deployment.JARDeployer",
         "jboss.system:service=JARDeployer");

      // SARDeployer, required to process *-service.xml
      startBootService(controller, "org.jboss.deployment.SARDeployer",
         "jboss.system:service=ServiceDeployer");

      log.info("Core system initialized");

      // Ok, now deploy the root deployable to finish the job

      MainDeployerMBean md = (MainDeployerMBean)
         MBeanProxyExt.create(MainDeployerMBean.class, mainDeployer, server);

      try
      {
         md.deploy(config.getServerConfigURL() + config.getRootDeploymentFilename());
      }
      catch (IncompleteDeploymentException e)
      {
         log.error("Root deployment has missing dependencies; continuing", e);
      }

      lifeThread = new LifeThread();
      lifeThread.start();

      started = true;
      
      // Send a notification that the startup is complete
      Notification msg = new Notification(START_NOTIFICATION_TYPE, this, 1);
      msg.setUserData(new Long(watch.getLapTime()));
      sendNotification(msg); 
      
      watch.stop();

      if (jbossPackage != null)
      {
         // Tell the world how fast it was =)
         log.info("JBoss (MX MicroKernel) [" + jbossPackage.getImplementationVersion() +
                  "] Started in " + watch);
      }
      else
      {
         log.warn("could not get package info to display release, either the " +
            "jar manifest in jboss-boot.jar has been mangled or you're " +
            "running unit tests from ant outside of JBoss itself.");
      }     
   }

   /**
    * Instantiate and register a service for the given classname into the MBean server.
    */
   private ObjectName createMBean(final String classname, String name)
      throws Exception
   {
      ObjectName mbeanName = null;
      if( name != null )
         mbeanName = new ObjectName(name);
      try
      {
         // See if there is an xmbean descriptor for the bootstrap mbean
         URL xmbeanDD = new URL("resource:xmdesc/"+classname+"-xmbean.xml");
         Object resource = server.instantiate(classname);
         // Create the XMBean
         Object[] args = {resource, xmbeanDD};
         String[] sig = {Object.class.getName(), URL.class.getName()};
         ObjectInstance instance = server.createMBean("org.jboss.mx.modelmbean.XMBean",
                                       mbeanName,
                                       bootstrapUCLName,
                                       args,
                                       sig);
         mbeanName = instance.getObjectName();
         log.debug("Created system XMBean: "+mbeanName);
      }
      catch(Exception e)
      {
         log.debug("Failed to create xmbean for: "+classname);
         mbeanName = server.createMBean(classname, mbeanName).getObjectName();
         log.debug("Created system MBean: " + mbeanName);
      }

      return mbeanName;
   }

   /**
    * Instantiate/register, create and start a service for the given classname.
    */
   private ObjectName startBootService(final ObjectName controllerName,
      final String classname, final String name)
      throws Exception
   {
      ObjectName mbeanName = createMBean(classname, name);

      // now go through the create/start sequence on the new service

      Object[] args = { mbeanName };
      String[] sig = { ObjectName.class.getName() };

      server.invoke(controllerName, "create", args, sig);
      server.invoke(controllerName, "start", args, sig);

      return mbeanName;
   }

   /**
    * Initialize the boot libraries.
    */
   private RepositoryClassLoader initBootLibraries() throws Exception
   {
      // Build the list of URL for the spine to boot
      List list = new ArrayList();

      // Add the patch URL.  If the url protocol is file, then
      // add the contents of the directory it points to
      URL patchURL = config.getPatchURL();
      if (patchURL != null)
      {
         if (patchURL.getProtocol().equals("file"))
         {
            File dir = new File(patchURL.getFile());
            if (dir.exists())
            {
               // Add the local file patch directory
               list.add(dir.toURL());

               // Add the contents of the directory too
               File[] jars = dir.listFiles(new FileSuffixFilter(new String[] { ".jar", ".zip" }, true));

               for (int j = 0; jars != null && j < jars.length; j++)
               {
                  list.add(jars[j].getCanonicalFile().toURL());
               }
            }
         }
         else
         {
            list.add(patchURL);
         }
      }

      // Add the server configuration directory to be able to load config files as resources
      list.add(config.getServerConfigURL());

      // Not needed, ServerImpl will have the basics on its classpath from ServerLoader
      // may want to bring this back at some point if we want to have reloadable core
      // components...

      // URL libraryURL = config.getLibraryURL();
      // list.add(new URL(libraryURL, "jboss-spine.jar"));

      log.debug("Boot url list: " + list);

      // Create loaders for each URL
      RepositoryClassLoader loader = null;
      for (Iterator iter = list.iterator(); iter.hasNext();)
      {
         URL url = (URL)iter.next();
         log.debug("Creating loader for URL: " + url);

         // This is a boot URL, so key it on itself.
         Object[] args = {url, Boolean.TRUE};
         String[] sig = {"java.net.URL", "boolean"};
         loader = (RepositoryClassLoader) server.invoke(DEFAULT_LOADER_NAME, "newClassLoader", args, sig);
      }
      return loader;
   }

   /**
    * Shutdown the Server instance and run shutdown hooks.
    *
    * <p>If the exit on shutdown flag is true, then {@link #exit}
    *    is called, else only the shutdown hook is run.
    *
    * @jmx:managed-operation
    *
    * @throws IllegalStateException    No started.
    */
   public void shutdown() throws IllegalStateException
   {
      if (log.isTraceEnabled())
         log.trace("Shutdown caller:", new Throwable("Here"));
      
      if (!started)
         throw new IllegalStateException("Server not started");

      isInShutdown = true;
      boolean exitOnShutdown = config.getExitOnShutdown();
      boolean blockingShutdown = config.getBlockingShutdown();
      log.info("Shutting down the server, blockingShutdown: " + blockingShutdown);
      
      if (exitOnShutdown)
      {
         exit(0);
      }
      else
      {
         // signal lifethread to exit; if no non-daemon threads
         // remain, the JVM will eventually exit
         lifeThread.interrupt();
         
         if (blockingShutdown)
         {
            shutdownHook.shutdown();
         }
         else
         {
            // start in new thread to give positive
            // feedback to requesting client of success.
            new Thread()
            {
               public void run()
               {
                  // just run the hook, don't call System.exit, as we may
                  // be embeded in a vm that would not like that very much
                  shutdownHook.shutdown();
               }
            }.start();
         }
      }
   }

   /**
    * Exit the JVM, run shutdown hooks, shutdown the server.
    *
    * @jmx:managed-operation
    *
    * @param exitcode   The exit code returned to the operating system.
    */
   public void exit(final int exitcode)
   {
      // exit() in new thread so that we might have a chance to give positive
      // feed back to requesting client of success.      
      new Thread()
      {
         public void run()
         {
            log.info("Server exit(" + exitcode + ") called");
            
            // Set exit code in the shutdown hook, in case halt is enabled
            shutdownHook.setHaltExitCode(exitcode); 
            
            // Initiate exiting, shutdown hook will be called
            Runtime.getRuntime().exit(exitcode);
         }
      }.start();
   }

   /**
    * Exit the JVM with code 1, run shutdown hooks, shutdown the server.
    *
    * @jmx:managed-operation
    */
   public void exit()
   {
      exit(1);
   }

   /**
    * Forcibly terminates the currently running Java virtual machine.
    *
    * @param exitcode   The exit code returned to the operating system.
    *
    * @jmx:managed-operation
    */
   public void halt(final int exitcode)
   {
      // halt() in new thread so that we might have a chance to give positive
      // feed back to requesting client of success.
      new Thread()
      {
         public void run()
         {
            System.err.println("Server halt(" + exitcode + ") called, halting the JVM now!");
            Runtime.getRuntime().halt(exitcode);
         }
      }.start();
   }

   /**
    * Forcibly terminates the currently running Java virtual machine.
    * Halts with code 1.
    *
    * @jmx:managed-operation
    */
   public void halt()
   {
      halt(1);
   }


   ///////////////////////////////////////////////////////////////////////////
   //                            Runtime Access                             //
   ///////////////////////////////////////////////////////////////////////////

   /** A simple helper used to log the Runtime memory information. */
   private void logMemoryUsage(final Runtime rt)
   {
      log.info("Total/free memory: " + rt.totalMemory() + "/" + rt.freeMemory());
   }

   /**
    * Hint to the JVM to run the garbage collector.
    *
    * @jmx:managed-operation
    */
   public void runGarbageCollector()
   {
      Runtime rt = Runtime.getRuntime();

      logMemoryUsage(rt);
      rt.gc();
      log.info("Hinted to the JVM to run garbage collection");
      logMemoryUsage(rt);
   }

   /**
    * Hint to the JVM to run any pending object finailizations.
    *
    * @jmx:managed-operation
    */
   public void runFinalization()
   {
      Runtime.getRuntime().runFinalization();
      log.info("Hinted to the JVM to run any pending object finalizations");
   }

   /**
    * Enable or disable tracing method calls at the Runtime level.
    *
    * @jmx:managed-operation
    */
   public void traceMethodCalls(final Boolean flag)
   {
      Runtime.getRuntime().traceMethodCalls(flag.booleanValue());
   }

   /**
    * Enable or disable tracing instructions the Runtime level.
    *
    * @jmx:managed-operation
    */
   public void traceInstructions(final Boolean flag)
   {
      Runtime.getRuntime().traceInstructions(flag.booleanValue());
   }


   ///////////////////////////////////////////////////////////////////////////
   //                          Server Information                           //
   ///////////////////////////////////////////////////////////////////////////

   /**
    * @jmx:managed-attribute
    */
   public Date getStartDate()
   {
      return startDate;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getVersion()
   {
      return version.toString();
   }

   /**
    * @jmx:managed-attribute
    */
   public String getVersionName()
   {
      return version.getName();
   }

   /**
    * @jmx:managed-attribute
    */
   public String getVersionNumber()
   {
      return version.getVersionNumber();
   }
   
   /**
    * @jmx:managed-attribute
    */
   public String getBuildNumber()
   {
      return version.getBuildNumber();
   }

   /**
    * @jmx:managed-attribute
    */
   public String getBuildJVM()
   {
      return version.getBuildJVM();
   }

   /**
    * @jmx:managed-attribute
    */
   public String getBuildOS()
   {
      return version.getBuildOS();
   }

   /**
    * @jmx:managed-attribute
    */
   public String getBuildID()
   {
      return version.getBuildID();
   }

   /**
    * @jmx:managed-attribute
    */
   public String getBuildDate()
   {
      return version.getBuildDate();
   }

   ///////////////////////////////////////////////////////////////////////////
   //                          NotificationEmitter                          //
   ///////////////////////////////////////////////////////////////////////////
   
   public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
   {
      broadcasterSupport.addNotificationListener(listener, filter, handback);
   }
   
   public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException
   {
      broadcasterSupport.removeNotificationListener(listener);
   }
   
   public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
      throws ListenerNotFoundException
   {
      broadcasterSupport.removeNotificationListener(listener, filter, handback);
   }
      
   public MBeanNotificationInfo[] getNotificationInfo()
   {
      return broadcasterSupport.getNotificationInfo();
   }
   
   public void sendNotification(Notification notification)
   {
      broadcasterSupport.sendNotification(notification);
   }
   
   ///////////////////////////////////////////////////////////////////////////
   //                             Lifecycle Thread                          //
   ///////////////////////////////////////////////////////////////////////////
   
   /** A simple thread that keeps the vm alive in the event there are no
    * other threads started.
    */ 
   private class LifeThread
      extends Thread
   {
      Object lock = new Object();
      LifeThread()
      {
         super("JBossLifeThread");
      }
      public void run()
      {
         synchronized (lock)
         {
            try
            {
               lock.wait();
            }
            catch (InterruptedException ignore)
            {
            }
         }
         log.debug("LifeThread.run() exits!");
      }
   }

   ///////////////////////////////////////////////////////////////////////////
   //                             Shutdown Hook                             //
   ///////////////////////////////////////////////////////////////////////////

   private class ShutdownHook
      extends Thread
   {
      /** The ServiceController which we will ask to shut things down with. */
      private ObjectName controller;

      /** The MainDeployer which we will ask to undeploy everything. */
      private ObjectName mainDeployer;

      /** Whether to halt the JMV at the end of the shutdown hook */
      private boolean forceHalt = true;
      
      /** The exit code to use if forceHalt is enabled */
      private int haltExitCode;

      /** Mark when shutdown has been performed */
      private boolean shutdownCalled;
      
      public ShutdownHook(final ObjectName controller, final ObjectName mainDeployer)
      {
         super("JBoss Shutdown Hook");

         this.controller = controller;
         this.mainDeployer = mainDeployer;

         String value = System.getProperty("jboss.shutdown.forceHalt", null);
         if (value != null)
         {
            forceHalt = new Boolean(value).booleanValue();
         }      
      }

      public void setHaltExitCode(int haltExitCode)
      {
         this.haltExitCode = haltExitCode;
      }
      
      public void run()
      {
         log.info("Runtime shutdown hook called, forceHalt: " + forceHalt);
         
         // shutdown the server
         shutdown();

         // Execute the jdk JBossJDKLogManager doReset
         LogManager lm = LogManager.getLogManager();
         if (lm instanceof JBossJDKLogManager)
         {
            JBossJDKLogManager jbosslm = (JBossJDKLogManager)lm;
            jbosslm.doReset();
         } 
         
         // later bitch - other shutdown hooks may be killed
         if (forceHalt)
         {
            System.out.println("Halting VM");
            Runtime.getRuntime().halt(haltExitCode);
         }
      }

      public void shutdown()
      {
         if (log.isTraceEnabled())
            log.trace("Shutdown caller:", new Throwable("Here"));
         
         // avoid entering twice; may happen when called directly
         // from ServerImpl.shutdown(), then called again when all
         // non-daemon threads have exited and the ShutdownHook runs. 
         if (shutdownCalled)
            return;
         else
            shutdownCalled = true;
         
         // Send a notification that server stop is initiated
         Notification msg = new Notification(STOP_NOTIFICATION_TYPE, this, 2);
         sendNotification(msg);
         
         // MainDeployer.shutdown()
         log.info("JBoss SHUTDOWN: Undeploying all packages");
         shutdownDeployments();
         
         // ServiceController.shutdown()
         log.debug("Shutting down all services");
         shutdownServices();

         // Make sure all mbeans are unregistered
         removeMBeans();

         // Cleanup tmp/deploy dir
         log.debug("Deleting server tmp/deploy directory");
         File tmp = config.getServerTempDir();
         File tmpDeploy = new File(tmp, "deploy");
         Files.delete(tmpDeploy);

         // Done
         log.info("Shutdown complete");
         System.out.println("Shutdown complete");
      }

      protected void shutdownDeployments()
      {
         try
         {
            // get the deployed objects from ServiceController
            server.invoke(mainDeployer,
                          "shutdown",
                          new Object[0],
                          new String[0]);
         }
         catch (Exception e)
         {
            Throwable t = JMXExceptionDecoder.decode(e);
            log.error("Failed to shutdown deployer", t);
         }
      }

      /**
       * The <code>shutdownServices</code> method calls the one and only
       * ServiceController to shut down all the mbeans registered with it.
       */
      protected void shutdownServices()
      {
         try
         {
            // get the deployed objects from ServiceController
            server.invoke(controller,
                          "shutdown",
                          new Object[0],
                          new String[0]);
         }
         catch (Exception e)
         {
            Throwable t = JMXExceptionDecoder.decode(e);
            log.error("Failed to shutdown services", t);
         }
      }

      /**
       * The <code>removeMBeans</code> method uses the mbean server to unregister
       * all the mbeans registered here.
       */
      protected void removeMBeans()
      {
         try
         {
            server.unregisterMBean(ServiceControllerMBean.OBJECT_NAME);
            server.unregisterMBean(ServerConfigImplMBean.OBJECT_NAME);
            server.unregisterMBean(ServerImplMBean.OBJECT_NAME);
         }
         catch (Exception e)
         {
            Throwable t = JMXExceptionDecoder.decode(e);
            log.error("Failed to unregister mbeans", t);
         }
         try
         {
            MBeanServer registeredServer = server;
            if (config.getPlatformMBeanServer() == true)
               registeredServer = LazyMBeanServer.getRegisteredMBeanServer(server);
            MBeanServerFactory.releaseMBeanServer(registeredServer);
         }
         catch (Exception e)
         {
            Throwable t = JMXExceptionDecoder.decode(e);
            log.error("Failed to release mbean server", t);
         }
      }
   }
}
