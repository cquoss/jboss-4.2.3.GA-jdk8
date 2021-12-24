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
package org.jboss.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Hashtable;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.security.auth.login.LoginContext;

import org.apache.log4j.Category;
import org.jboss.test.util.AppCallbackHandler;

/**
 * This is provides services for jboss junit test cases and TestSetups. It supplies
 * access to log4j logging, the jboss jmx server, jndi, and a method for
 * deploying ejb packages. You may supply the JNDI name under which the
 * RMIAdaptor interface is located via the system property jbosstest.server.name
 * default (jmx/rmi/RMIAdaptor) and the directory for deployable packages with
 * the system property jbosstest.deploy.dir (default ../lib).
 *
 * Should be subclassed to derive junit support for specific services integrated
 * into jboss.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:christoph.jung@jboss.org">Christoph G. Jung</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @version $Revision: 57221 $
 */
public class JBossTestServices
{
   // Constants -----------------------------------------------------
   public final static String DEPLOYER_NAME = "jboss.system:service=MainDeployer";
   public final static String DEFAULT_USERNAME = "jduke";
   public final static String DEFAULT_PASSWORD = "theduke";
   public final static String DEFAULT_LOGIN_CONFIG = "other";
   public final static int DEFAULT_THREADCOUNT = 10;
   public final static int DEFAULT_ITERATIONCOUNT = 1000;
   public final static int DEFAULT_BEANCOUNT = 100;

   // Attributes ----------------------------------------------------
   protected MBeanServerConnection server;
   protected Category log;
   protected InitialContext initialContext;
   protected Hashtable jndiEnv;
   protected LoginContext lc;

   /**
    * Constructor for the JBossTestCase object
    *
    * @param className  Test case name
    */
   public JBossTestServices(String className)
   {
      log = Category.getInstance(className);
      log.debug("JBossTestServices(), className=" + className);
   }
   
   // Public --------------------------------------------------------

   /**
    * The JUnit setup method
    */
   public void setUp() throws Exception
   {
      log.debug("JBossTestServices.setUp()");
      init();
      log.info("jbosstest.beancount: " + System.getProperty("jbosstest.beancount"));
      log.info("jbosstest.iterationcount: " + System.getProperty("jbosstest.iterationcount"));
      log.info("jbosstest.threadcount: " + System.getProperty("jbosstest.threadcount"));
      log.info("jbosstest.nodeploy: " + System.getProperty("jbosstest.nodeploy"));
      log.info("jbosstest.jndiurl: " + this.getJndiURL());
      log.info("jbosstest.jndifactory: " + this.getJndiInitFactory());
   }

   /**
    * The teardown method for JUnit
    */
   public void tearDown() throws Exception
   {
      // server = null;
      log.debug("JBossTestServices.tearDown()");
   }


   /**
    * Gets the InitialContext attribute of the JBossTestCase object
    *
    * @return   The InitialContext value
    */
   public InitialContext getInitialContext() throws Exception
   {
      return initialContext;
   }

   /**
    * Gets the Server attribute of the JBossTestCase object
    *
    * @return   The Server value
    */
   public MBeanServerConnection getServer() throws Exception
   {
      if (server == null)
      {
         String adaptorName = System.getProperty("jbosstest.server.name", "jmx/invoker/RMIAdaptor");
         server = (MBeanServerConnection)initialContext.lookup(adaptorName);
      }
      return server;
   }

   /**
    * Gets the Log attribute of the JBossTestCase object
    *
    * @return   The Log value
    */
   Category getLog()
   {
      return log;
   }

   /**
    * Gets the Main Deployer Name attribute of the JBossTestCase object
    *
    * @return                                  The Main DeployerName value
    * @exception MalformedObjectNameException  Description of Exception
    */
   ObjectName getDeployerName() throws MalformedObjectNameException
   {
      return new ObjectName(DEPLOYER_NAME);
   }

   /**
    * Returns the deployment directory to use. This does it's best to figure out
    * where you are looking. If you supply a complete url, it returns it.
    * Otherwise, it looks for jbosstest.deploy.dir or if missing ../lib. Then it
    * tries to construct a file url or a url.
    *
    * @param filename                   name of the file/url you want
    * @return                           A URL
    * @exception MalformedURLException  Description of Exception
    */
   protected URL getDeployURL(final String filename)
      throws MalformedURLException
   {
      // First see if it is already a complete url.
      try
      {
         return new URL(filename);
      }
      catch (MalformedURLException e)
      {
         log.debug(filename + " is not a valid URL, " + e.getMessage());
      }

      // OK, lets see if we can figure out what it might be.
      String deployDir = System.getProperty("jbosstest.deploy.dir");
      if (deployDir == null)
      {
         deployDir = "output/lib";
      }
      String url = deployDir + "/" + filename;
      log.debug("Testing file: " + url);
      // try to canonicalize the strings a bit.
      File file = new File(url);
      if (file.exists())
      {
         log.debug(file.getAbsolutePath() + " is a valid file");
         return file.toURL();
      }
      else
      {
         log.debug("File does not exist, creating url: " + url);
         return new URL(url);
      }
   }
   
   /**
    * invoke wraps an invoke call to the mbean server in a lot of exception
    * unwrapping.
    *
    * @param name           ObjectName of the mbean to be called
    * @param method         mbean method to be called
    * @param args           Object[] of arguments for the mbean method.
    * @param sig            String[] of types for the mbean methods parameters.
    * @return               Object returned by mbean method invocation.
    * @exception Exception  Description of Exception
    */
   protected Object invoke(ObjectName name, String method, Object[] args, String[] sig) throws Exception
   {
      return invoke(getServer(), name, method, args, sig);
   }

   protected Object invoke(MBeanServerConnection server, ObjectName name, String method, Object[] args, String[] sig)
      throws Exception
   {
      try
      {
         this.getLog().debug("Invoking " + name.getCanonicalName() + " method=" + method);
         if (args != null)
            this.getLog().debug("args=" + Arrays.asList(args));
         return server.invoke(name, method, args, sig);
      }
      catch (javax.management.MBeanException e)
      {
         log.error("MbeanException", e.getTargetException());
         throw e.getTargetException();
      }
      catch (javax.management.ReflectionException e)
      {
         log.error("ReflectionException", e.getTargetException());
         throw e.getTargetException();
      }
      catch (javax.management.RuntimeOperationsException e)
      {
         log.error("RuntimeOperationsException", e.getTargetException());
         throw e.getTargetException();
      }
      catch (javax.management.RuntimeMBeanException e)
      {
         log.error("RuntimeMbeanException", e.getTargetException());
         throw e.getTargetException();
      }
      catch (javax.management.RuntimeErrorException e)
      {
         log.error("RuntimeErrorException", e.getTargetError());
         throw e.getTargetError();
      }
   }


   /**
    * Deploy a package with the main deployer. The supplied name is
    * interpreted as a url, or as a filename in jbosstest.deploy.lib or ../lib.
    *
    * @param name           filename/url of package to deploy.
    * @exception Exception  Description of Exception
    */
   public void deploy(String name) throws Exception
   {
      if (Boolean.getBoolean("jbosstest.nodeploy") == true)
      {
         log.debug("Skipping deployment of: " + name);
         return;
      }

      URL deployURL = getDeployURL(name);
      log.debug("Deploying " + name + ", url=" + deployURL);
      invoke(getDeployerName(),
         "deploy",
         new Object[]{deployURL},
         new String[]{"java.net.URL"});
   }

   public void redeploy(String name) throws Exception
   {
      if (Boolean.getBoolean("jbosstest.nodeploy") == true)
      {
         log.debug("Skipping redeployment of: " + name);
         return;
      }

      URL deployURL = getDeployURL(name);
      log.debug("Deploying " + name + ", url=" + deployURL);
      invoke(getDeployerName(),
         "redeploy",
         new Object[]{deployURL},
         new String[]{"java.net.URL"});
   }

   /** Do a JAAS login with the current username, password and login config.
    * @throws Exception
    */
   public void login() throws Exception
   {
      flushAuthCache("other");
      String username = getUsername();
      String pass = getPassword();
      String config = getLoginConfig();
      char[] password = null;
      if (pass != null)
         password = pass.toCharArray();
      AppCallbackHandler handler = new AppCallbackHandler(username, password);
      getLog().debug("Creating LoginContext(" + config + ")");
      lc = new LoginContext(config, handler);
      lc.login();
      getLog().debug("Created LoginContext, subject=" + lc.getSubject());
   }

   public void logout()
   {
      try
      {
         getLog().debug("logout, LoginContext: " + lc);
         if (lc != null)
            lc.logout();
      }
      catch (Exception e)
      {
         getLog().error("logout error: ", e);
      }
   }

   /**
    * Undeploy a package with the main deployer. The supplied name is
    * interpreted as a url, or as a filename in jbosstest.deploy.lib or ../lib.
    *
    * @param name           filename/url of package to undeploy.
    * @exception Exception  Description of Exception
    */
   public void undeploy(String name) throws Exception
   {
      if (Boolean.getBoolean("jbosstest.nodeploy") == true)
         return;
      URL deployURL = getDeployURL(name);
      log.debug("Undeploying " + name + ", url=" + deployURL);
      Object[] args = {deployURL};
      String[] sig = {"java.net.URL"};
      invoke(getDeployerName(), "undeploy", args, sig);
   }

   /** Flush all authentication credentials for the java:/jaas/other security
    domain
    */
   void flushAuthCache(String domain) throws Exception
   {
      ObjectName jaasMgr = new ObjectName("jboss.security:service=JaasSecurityManager");
      Object[] params = {domain};
      String[] signature = {"java.lang.String"};
      invoke(jaasMgr, "flushAuthenticationCache", params, signature);
   }

   void restartDBPool() throws Exception
   {
      ObjectName dbPool = new ObjectName("jboss.jca:service=ManagedConnectionPool,name=DefaultDS");
      Object[] params = {};
      String[] signature = {};
      invoke(dbPool, "stop", params, signature);
      invoke(dbPool, "start", params, signature);
   }

   boolean isSecure()
   {
      return Boolean.getBoolean("jbosstest.secure");
   }

   String getUsername()
   {
      return System.getProperty("jbosstest.username", DEFAULT_USERNAME);
   }

   String getPassword()
   {
      return System.getProperty("jbosstest.password", DEFAULT_PASSWORD);
   }

   String getLoginConfig()
   {
      return System.getProperty("jbosstest.loginconfig", DEFAULT_LOGIN_CONFIG);
   }

   String getJndiURL()
   {
      String url = (String)jndiEnv.get(Context.PROVIDER_URL);
      return url;
   }

   String getJndiInitFactory()
   {
      String factory = (String)jndiEnv.get(Context.INITIAL_CONTEXT_FACTORY);
      return factory;
   }

   int getThreadCount()
   {
      int result = Integer.getInteger("jbosstest.threadcount", DEFAULT_THREADCOUNT).intValue();
      log.debug("jbosstest.threadcount=" + result);
      return result;
   }

   int getIterationCount()
   {
      int result = Integer.getInteger("jbosstest.iterationcount", DEFAULT_ITERATIONCOUNT).intValue();
      log.debug("jbosstest.iterationcount=" + result);
      return result;
   }

   int getBeanCount()
   {
      int result = Integer.getInteger("jbosstest.beancount", DEFAULT_BEANCOUNT).intValue();
      log.debug("jbosstest.beancount=" + result);
      return result;
   }

   /** 
    * Initializes the {@link InitialContext} if not set.
    */
   public void init() throws Exception
   {
      if (initialContext == null)
      {
         initialContext = new InitialContext();
         log.debug("initialContext.getEnvironment()=" + initialContext.getEnvironment());         
         jndiEnv = initialContext.getEnvironment();
      }
   }
   
   /** 
    * Re-initializes the {@link InitialContext}.
    */   
   public void reinit() throws Exception
   {
      initialContext = null;
      server = null;
      init();
   }

   /**
    * Returns the JBoss server host from system property "jbosstest.server.host"
    * This defaults to "localhost"
    */
   public String getServerHost()
   {
      String hostName = System.getProperty("jbosstest.server.host", "localhost");
      return hostName;
   }
}
