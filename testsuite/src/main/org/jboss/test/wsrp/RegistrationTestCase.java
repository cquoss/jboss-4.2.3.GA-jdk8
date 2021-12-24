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
package org.jboss.test.wsrp;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.rpc.Service;

import junit.framework.Test;

import org.jboss.test.webservice.WebserviceTestBase;
import org.jboss.test.wsrp.core.Extension;
import org.jboss.test.wsrp.core.ModifyRegistration;
import org.jboss.test.wsrp.core.Property;
import org.jboss.test.wsrp.core.RegistrationContext;
import org.jboss.test.wsrp.core.RegistrationData;
import org.jboss.test.wsrp.core.RegistrationState;
import org.jboss.test.wsrp.core.ReturnAny;

//$Id: RegistrationTestCase.java 57211 2006-09-26 12:39:46Z dimitris@jboss.org $

/**
 *  Testcase for the WSRP Registration service
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Mar 27, 2006
 *  @version $Revision: 57211 $
 */
public class RegistrationTestCase extends WebserviceTestBase
{ 
   public RegistrationTestCase(String name)
   {
      super(name); 
   } 

   /**
    * deploy the test archives
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(RegistrationTestCase.class, "wsrp.war, wsrp-client.jar");
   }
   
   public void testRegister() throws Exception
   {
      Service service = getService();
      WSRP_v1_Registration_PortType endpoint =
         (WSRP_v1_Registration_PortType)service.getPort(WSRP_v1_Registration_PortType.class);
      RegistrationData cp = new RegistrationData("consumerName", "consumerAgent", true, 
            new String[]{}, new String[]{}, new String[]{}, new String[]{}, 
            new Property[] {} , new Extension[] {}) ; 
      //Invoke the Web Service
      RegistrationContext ra = endpoint.register(cp);
      assertNotNull("RegistrationContext != null", ra);
   } 
   
   public void testDeRegister() throws Exception
   {
      Service service = getService();
      WSRP_v1_Registration_PortType endpoint =
         (WSRP_v1_Registration_PortType)service.getPort(WSRP_v1_Registration_PortType.class);
      RegistrationContext cp = new RegistrationContext(); 
      //Invoke the Web Service
      ReturnAny ra = endpoint.deregister(cp);
      assertNotNull("ReturnAny != null", ra);
   } 
   
   public void testModifyRegistration() throws Exception
   {
      Service service = getService();
      WSRP_v1_Registration_PortType endpoint =
         (WSRP_v1_Registration_PortType)service.getPort(WSRP_v1_Registration_PortType.class);
      ModifyRegistration cp = new ModifyRegistration(); 
      //Invoke the Web Service
      RegistrationState ra = endpoint.modifyRegistration(cp);
      assertNotNull("RegistrationState != null", ra);
   }
   
   private Service getService() throws NamingException
   {
      InitialContext iniCtx = getClientContext("wsrp-client");
      Service service = (Service)iniCtx.lookup("java:comp/env/service/RegistrationService");
      assertNotNull("Service != null", service);
      return service;
   }
}
