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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

/**
 * This is the standard base test case for jboss junit test cases. It supplies
 * access to log4j logging, the jboss jmx server, jndi, and a method for
 * deploying ejb packages. You may supply the name of the machine the jboss
 * server is on with the system property jbosstest.server.name (default
 * getInetAddress().getLocalHost().getHostName()) and the directory for
 * deployable packages with the system property jbosstest.deploy.dir (default
 * ../lib).
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 60041 $
 */
public class JBossTestCase
   extends TestCase
{
   protected Logger log;

   /**
    *  Saved exception from deployment.
    *  Will be thrown from {@link #testServerFound}, if not <code>null</code>.
    */
   protected static Exception deploymentException = null;

   protected JBossTestServices delegate;


   // Static --------------------------------------------------------
   // Constructors --------------------------------------------------
   /**
    * Constructor for the JBossTestCase object
    *
    * @param name  Test case name
    */
   public JBossTestCase(String name)
   {
      super(name);
      /* Validate the build.testlog system property and set it it to the
       * location of the log resource
       */
      String testlog = System.getProperty("build.testlog");
      if( testlog == null )
      {
         // Try to set it to the log resource location
         try
         {
            testlog = getResourceURL("log");
            if( testlog != null )
            {
               System.setProperty("build.testlog", testlog);
            }
            else
            {
               // Default to cwd output/log
               System.setProperty("build.testlog", "output/log");
            }
         }
         catch(Exception ignore)
         {
         }
      }
      log = Logger.getLogger(getClass());
      initDelegate();
   }

   public void initDelegate()
   {
      delegate = new JBossTestServices(getClass().getName());
      try
      {
         delegate.init();
      }
      catch (Exception e)
      {
         log.error("Failed to init delegate", e);
      }
   }
   public void resetDelegate()
   {
      try
      {
         delegate.reinit();
      }
      catch(Exception e)
      {
         log.error("Failed to init delegate", e);
      }
   }

   // Public --------------------------------------------------------


   /**
    * This just checks the server is there... so you should get at least one
    * success!
    * Also checks if an exception occurred during deployment, and throws
    * any such exception from here.
    *
    * @exception Exception  Description of Exception
    */
   public void serverFound() throws Exception
   {
      if (deploymentException != null)
         throw deploymentException;
      assertTrue("Server was not found", getServer() != null);
   }

   //protected---------

   /**
    * Gets the InitialContext attribute of the JBossTestCase object
    *
    * @return   The InitialContext value
    */
   protected InitialContext getInitialContext() throws Exception
   {
      return delegate.getInitialContext();
   }

   /**
    * Gets the Server attribute of the JBossTestCase object
    *
    * @return   The Server value
    */
   protected MBeanServerConnection getServer() throws Exception
   {
      return delegate.getServer();
   }

   /**
    * Gets the Log attribute of the JBossTestCase object
    *
    * @return   The Log value
    */
   protected Logger getLog()
   {
      return log;
   }

   /**
    * Gets the DeployerName attribute of the JBossTestCase object
    *
    * @return                                  The DeployerName value
    * @exception MalformedObjectNameException  Description of Exception
    */
   protected ObjectName getDeployerName() throws MalformedObjectNameException
   {
      return delegate.getDeployerName();
   }


   /**
    * Returns the deployment directory to use. This does it's best to figure out
    * where you are looking. If you supply a complete url, it returns it.
    * Otherwise, it looks for jbosstest.deploy.dir or if missing ../lib. Then it
    * tries to construct a file url or a url.
    *
    * @param filename                   name of the file/url you want
    * @return                           A more or less canonical string for the url.
    * @exception MalformedURLException  Description of Exception
    */
   protected URL getDeployURL(final String filename) throws MalformedURLException
   {
      return delegate.getDeployURL(filename);
   }

   /** Get a URL string to a resource in the testsuite/output/resources dir.
    * This relies on the output/resources directory being in the
    * testcase classpath.
    *
    */
   protected String getResourceURL(final String resource) throws MalformedURLException
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      URL resURL = loader.getResource(resource);
      return resURL != null ? resURL.toString() : null;
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
      return delegate.invoke(name, method, args, sig);
   }

   /**
    * Deploy a package with the main deployer. The supplied name is
    * interpreted as a url, or as a filename in jbosstest.deploy.lib or ../lib.
    *
    * @param name           filename/url of package to deploy.
    * @exception Exception  Description of Exception
    */
   protected void deploy(String name) throws Exception
   {
      delegate.deploy(name);
   }

   /**
    * Redeploy a package with the main deployer. The supplied name is
    * interpreted as a url, or as a filename in jbosstest.deploy.lib or ../lib.
    *
    * @param name           filename/url of package to deploy.
    * @exception Exception  Description of Exception
    */
   protected void redeploy(String name) throws Exception
   {
      delegate.redeploy(name);
   }

   /**
    * Undeploy a package with the main deployer. The supplied name is
    * interpreted as a url, or as a filename in jbosstest.deploy.lib or ../lib.
    *
    * @param name           filename/url of package to undeploy.
    * @exception Exception  Description of Exception
    */
   protected void undeploy(String name) throws Exception
   {
      delegate.undeploy(name);
   }

   /**
    * Get a JBossTestSetup that does login and deployment in setUp/tearDown
    *
    * @param test a Test
    * @param jarNames is a comma seperated list of deployments
    */
   public static Test getDeploySetup(final Test test, final String jarNames)
      throws Exception
   {
      JBossTestSetup wrapper = new JBossTestSetup(test)
      {
         protected void setUp() throws Exception
         {
            deploymentException = null;
            try
            {
               this.delegate.init();

               if (this.delegate.isSecure())
                  this.delegate.login();

               if (jarNames == null) return;

               // deploy the comma seperated list of jars
               StringTokenizer st = new StringTokenizer(jarNames, ", ");
               while (st != null && st.hasMoreTokens())
               {
                  String jarName = st.nextToken();
                  this.redeploy(jarName);
                  this.getLog().debug("deployed package: " + jarName);
               }
            }
            catch (Exception ex)
            {
               // Throw this in testServerFound() instead.
               deploymentException = ex;
            }
         }

         protected void tearDown() throws Exception
         {
            if (jarNames == null) return; //Nothing to Undeploy
             
            // undeploy the comma seperated list of jars
            StringTokenizer st = new StringTokenizer(jarNames, ", ");
            String[] depoyments = new String[st.countTokens()];
            for (int i = depoyments.length - 1; i >= 0; i--)
               depoyments[i] = st.nextToken();
            for (int i = 0; i < depoyments.length; i++)
            {
               String jarName = depoyments[i];
               this.undeploy(jarName);
               this.getLog().debug("undeployed package: " + jarName);
            }

            if (this.delegate.isSecure())
               this.delegate.logout();
         }
      };
      return wrapper;
   }

   public static Test getDeploySetup(final Class clazz, final String jarName)
      throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(clazz));
      return getDeploySetup(suite, jarName);
   }

   protected String getJndiURL()
   {
      return delegate.getJndiURL();
   }

   protected String getJndiInitFactory()
   {
      return delegate.getJndiInitFactory();
   }

   protected int getThreadCount()
   {
      return delegate.getThreadCount();
   }

   protected int getIterationCount()
   {
      return delegate.getIterationCount();
   }

   protected int getBeanCount()
   {
      return delegate.getBeanCount();
   }

   /**
    * Get the JBoss server host from system property "jbosstest.server.host"
    * This defaults to "localhost"
    */
   public String getServerHost()
   {
      return delegate.getServerHost();
   }

   protected void flushAuthCache() throws Exception
   {
      flushAuthCache("other");
   }

   protected void flushAuthCache(String domain) throws Exception
   {
      delegate.flushAuthCache(domain);
   }

   /**
    * Validate the java.security.auth.login.config setting, and if not
    * found, set it to the security/auth.conf classpath resource value
    * if that exists.
    * @throws IllegalStateException if neither java.security.auth.login.config
    * is set and no security/auth.conf classpath resource exists.
    */
   protected void initDefaultLoginConfig()
   {
      String authConf = System.getProperty("java.security.auth.login.config");
      if( authConf == null )
      {
         try
         {
            String confURL = getResourceURL("security/auth.conf");
            if( confURL == null )
               throw new IllegalStateException("No java.security.auth.login.config specified and security/auth.conf not found");
            System.setProperty("java.security.auth.login.config", confURL);
         }
         catch(Exception ignore)
         {
         }
      }
   }

   /** Restart the connection pool associated with the DefaultDS
    * @throws Exception on failure
    */
   protected void restartDBPool() throws Exception
   {
      delegate.restartDBPool();
   }

   protected void sleep(long interval) throws InterruptedException
   {
      synchronized (this)
      {
         wait(interval);
      }
   }
}
