/*
  * JBoss, Home of Professional Open Source
  * Copyright 2007, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
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
package org.jboss.test.security.test.ejb;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.security.auth.login.Configuration; 

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.security.auth.login.XMLLoginConfigImpl;
import org.jboss.test.JBossTestCase; 
import org.jboss.test.JBossTestSetup;
import org.jboss.test.security.interfaces.StatefulSession;
import org.jboss.test.security.interfaces.StatefulSessionHome;  

/**
 *  Stateful Session Beans Integration Tests
 *  JBAS-3976: Null security context exception thrown for no login
 *  JBAS-3781: Do not push null subject context when caller RAI is present
 *  @author Anil.Saldhana@redhat.com
 *  @since  Mar 15, 2007 
 *  @version $Revision$
 */
public class SFSBIntegrationTestCase extends JBossTestCase
{   
   public SFSBIntegrationTestCase(String name)
   {
      super(name); 
   } 
  
   /**
    * Call a SFSB method that has container transaction and each of
    * the SessionSynchronization callback methods call the getCallerPrincipal
    * 
    * Also the SFSB has a ejb ref to another SFSB which is secured and declares
    * a run-as role
    * @throws Exception
    */
   public void testCallerPrincipalInSessionSynchronization() throws Exception
   { 
      InitialContext jndiContext = new InitialContext();
      Object obj = jndiContext.lookup("spec.StatefulSession");
      obj = PortableRemoteObject.narrow(obj, StatefulSessionHome.class);
      StatefulSessionHome home = (StatefulSessionHome) obj;
      log.debug("Found StatefulSessionHome");
      // The create should be allowed to call getCallerPrincipal
      StatefulSession bean = home.create("testStatefulCreateCaller");
      // Need to invoke a method to ensure an ejbCreate call
      bean.echo("testStatefulCreateCaller"); 
   } 
   
   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(SFSBIntegrationTestCase.class));

      // Create an initializer for the test suite
      TestSetup wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            Configuration.setConfiguration(new XMLLoginConfigImpl());
            redeploy("sfsb-security-integration.jar");
            flushAuthCache();
         }
         protected void tearDown() throws Exception
         {
            undeploy("sfsb-security-integration.jar");
            super.tearDown();
         
         }
      };
      return wrapper;
   } 
}
