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
package org.jboss.test.security.test;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.security.auth.login.Configuration;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.security.auth.login.XMLLoginConfigImpl; 
import org.jboss.test.JBossTestSetup;
import org.jboss.test.util.web.HttpUtils;

//$Id: CustomSecurityManagerTestCase.java 60917 2007-02-26 18:10:13Z anil.saldhana@jboss.com $

/**
 *  JBAS-2703 : Create a AuthenticationManager/AuthorizationManager 
 *  plugin testcase
 *  
 *  This testcase overrides test methods from EJBSpecUnitTestCase (that
 *  should not be tested) with noop implementation.
 *  
 *  Also tests the interaction of the web layer with the EJB layer, with
 *  a custom security manager plugin installed.
 *  
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Apr 3, 2006
 *  @version $Revision: 60917 $
 */
public class CustomSecurityManagerTestCase extends EJBSpecUnitTestCase 
{ 
   public static String REALM = "JBossTest Servlets";
   
   public CustomSecurityManagerTestCase(String name)
   {
      super(name); 
   }
   
   /**
    * Tests the access of a BASIC secured servlet that internally 
    * accesses a secured EJB
    * 
    * @throws Exception
    */
   public void testWebLayer() throws Exception
   {
      //Access the SecureServletSecureEJB servlet
      String baseURL = HttpUtils.getBaseURL("scott", "echoman");
      //Test the Restricted servlet
      URL url = new URL(baseURL+"custom-secmgr/SecureServlet"); 
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK, HttpUtils.POST); 
   }
   
   /**
    * Overriden test method that has noop impl
    */
   public void testDomainInteraction() throws Exception
   {
      log.debug("testDomainInteraction::We Do Not Test This!");
   }
   
   /**
    * JBAS-4801: JRockit wierdness:: Just calls the super class method
    */
   public void testGetCallerPrincipal() throws Exception
   {
      super.testGetCallerPrincipal();
   } 
   
   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite(); 
      suite.addTestSuite(CustomSecurityManagerTestCase.class);

      // Create an initializer for the test suite
      TestSetup wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            Configuration.setConfiguration(new XMLLoginConfigImpl());
            deploy(getResourceURL("security/custom-secmgr/queues-service.xml"));
            redeploy("custom-secmgrtests.ear"); 
            flushAuthCache();
         }
         protected void tearDown() throws Exception
         {
            undeploy("custom-secmgrtests.ear"); 
            undeploy(getResourceURL("security/custom-secmgr/queues-service.xml"));
            super.tearDown();
         
         }
      };
      return wrapper;
   } 
}
