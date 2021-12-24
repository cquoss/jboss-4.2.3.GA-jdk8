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
package org.jboss.test.jmx.test;

import javax.management.Attribute;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.security.auth.login.LoginContext;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.util.AppCallbackHandler;

//$Id: RMIAdaptorAuthorizationUnitTestCase.java 61376 2007-03-16 16:49:18Z anil.saldhana@jboss.com $

/**
 *  Authorization of the RMI Adaptor
 *  Especially tests the usage of the authorization delegate
 *  called as org.jboss.jmx.connector.invoker.ExternalizableRolesAuthorization
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  May 10, 2006
 *  @version $Revision: 61376 $
 */
public class RMIAdaptorAuthorizationUnitTestCase extends JBossTestCase
{ 
   public RMIAdaptorAuthorizationUnitTestCase(String name)
   {
      super(name); 
   } 
   
   /**
    * Test that a valid jmx-console domain user can invoke operations
    * through the jmx/invoker/AuthenticatedRMIAdaptor
    * @throws Exception
    */ 
   public void testConfigurableRolesAuthorizedAccess() throws Exception
   {
      LoginContext lc = login("admin", "admin".toCharArray());
      InitialContext ctx = getInitialContext();
      MBeanServerConnection conn = (MBeanServerConnection) ctx.lookup("jmx/invoker/ConfigurableAuthorizedRMIAdaptor");
      ObjectName server = new ObjectName("jboss.system:type=Server");
      String version = (String) conn.getAttribute(server, "Version");
      log.info("Obtained server version: "+version);
      MBeanInfo info = conn.getMBeanInfo(server); 
      assertNotNull("MBeanInfo != null", info);
      Integer mbeanCount = conn.getMBeanCount();
      assertNotNull("mbeanCount != null", mbeanCount);
      
      //JBAS-4101: Accomodate setAttribute calls
      ObjectName jaasService = new ObjectName("jboss.security:service=JaasSecurityManager");
      conn.setAttribute(jaasService, new Attribute("DefaultCacheTimeout", Integer.valueOf(1000))); 
      lc.logout();
   }
   
   /**
    * Test invalid access
    * @throws Exception
    */ 
   public void testUnAuthorizedAccess() throws Exception
   {
      InitialContext ctx = getInitialContext();
      MBeanServerConnection conn = (MBeanServerConnection) ctx.lookup("jmx/invoker/ConfigurableAuthorizedRMIAdaptor");
      ObjectName server = new ObjectName("jboss.system:type=Server");
      try
      {
         String version = (String) conn.getAttribute(server, "Version");
         log.info("Obtained server version: "+version);
         fail("Was able to get server Version attribute");
      }
      catch(Exception e)
      {
         log.info("Access failed as expected", e);
      }
   }
   
   public static Test suite()
   throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(RMIAdaptorAuthorizationUnitTestCase.class));
      
      JBossTestSetup wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            deploymentException = null;
            try
            {
               this.delegate.init();
               redeploy("jmxinvoker-authorization-test.jar");
               // deploy the comma seperated list of jars 
               redeploy(getResourceURL("jmx/jmxadaptor/authorization-jmx-invoker-service.xml"));
               redeploy(getResourceURL("jmx/jmxadaptor/jaas-service.xml")); 
            }
            catch (Exception ex)
            {
               // Throw this in testServerFound() instead.
               deploymentException = ex;
            }
         }
         
         protected void tearDown() throws Exception
         {            
            undeploy(getResourceURL("jmx/jmxadaptor/authorization-jmx-invoker-service.xml"));
            undeploy("jmxinvoker-authorization-test.jar"); 
            undeploy(getResourceURL("jmx/jmxadaptor/jaas-service.xml")); 
         }
      };
      return wrapper; 
   } 

   private LoginContext login(String username, char[] password) throws Exception
   { 
      String confName = System.getProperty("conf.name", "other");
      AppCallbackHandler handler = new AppCallbackHandler(username, password);
      log.debug("Creating LoginContext("+confName+")");
      LoginContext lc = new LoginContext(confName, handler);
      lc.login();
      log.debug("Created LoginContext, subject="+lc.getSubject());
      return lc;
   }
}
