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
package org.jboss.test.jacc.test.external;

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.util.web.HttpUtils; 

//$Id: AuthzDelegationUnitTestCase.java 57211 2006-09-26 12:39:46Z dimitris@jboss.org $

/**
 *  Tests delegation of authorization decisions to an external policy provider
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Apr 27, 2006
 *  @version $Revision: 57211 $
 */
public class AuthzDelegationUnitTestCase extends JBossTestCase
{
   private static Logger log = Logger.getLogger(AuthzDelegationUnitTestCase.class);
   
   private String baseURLNoAuth = HttpUtils.getBaseURLNoAuth(); 
   
   public AuthzDelegationUnitTestCase(String name)
   {
      super(name); 
   } 
   
   public void testSuccessfulWebResourceDelegation() throws Exception
   {
      //Try a successful access
      HttpUtils.accessURL(new URL(baseURLNoAuth + "/jacc-delegate/index.html"));
      //Try an unsuccessful access
      try
      {
         HttpUtils.accessURL(new URL(baseURLNoAuth + "/jacc-delegate/noResource.html")); 
         fail("Should have failed");
      }
      catch(Exception ignore)
      {  
         log.error("Ignorable error that should be access denied:",ignore);
      } 
   } 
    
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(AuthzDelegationUnitTestCase.class));

      // Create an initializer for the test suite
      Test wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            deploy("jacc-delegate.war");
            // Make sure the security cache is clear
            flushAuthCache();
         }
         protected void tearDown() throws Exception
         {
            undeploy("jacc-delegate.war");
            super.tearDown();
         }
      };
      return wrapper;
   }
}
