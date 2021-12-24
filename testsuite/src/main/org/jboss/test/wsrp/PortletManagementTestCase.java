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
import org.jboss.test.wsrp.core.ClonePortlet;
import org.jboss.test.wsrp.core.DestroyPortlets;
import org.jboss.test.wsrp.core.DestroyPortletsResponse;
import org.jboss.test.wsrp.core.Extension;
import org.jboss.test.wsrp.core.GetPortletDescription;
import org.jboss.test.wsrp.core.GetPortletProperties;
import org.jboss.test.wsrp.core.GetPortletPropertyDescription;
import org.jboss.test.wsrp.core.PortletContext;
import org.jboss.test.wsrp.core.PortletDescriptionResponse;
import org.jboss.test.wsrp.core.PortletPropertyDescriptionResponse;
import org.jboss.test.wsrp.core.PropertyList;
import org.jboss.test.wsrp.core.RegistrationContext;
import org.jboss.test.wsrp.core.SetPortletProperties;
import org.jboss.test.wsrp.core.UserContext;
import org.jboss.test.wsrp.core.UserProfile;

//$Id: PortletManagementTestCase.java 57211 2006-09-26 12:39:46Z dimitris@jboss.org $

/**
 *  Test Case that tests WSRP Portlet Management
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Mar 27, 2006
 *  @version $Revision: 57211 $
 */
public class PortletManagementTestCase extends WebserviceTestBase
{ 
   public PortletManagementTestCase(String name)
   {
      super(name); 
   } 

   /**
    * deploy the test archives
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(PortletManagementTestCase.class, "wsrp.war, wsrp-client.jar");
   }
   
   public void testGetPortletDescription() throws Exception
   {
      Service service = getService();
      WSRP_v1_PortletManagement_PortType endpoint =
         (WSRP_v1_PortletManagement_PortType)service.getPort(WSRP_v1_PortletManagement_PortType.class);
      GetPortletDescription ic = new GetPortletDescription(new RegistrationContext(), 
                    new PortletContext("portletHandle", "portletState".getBytes(), new Extension[]{}), 
                    new UserContext("userContextKey", new String[]{},new UserProfile(), new Extension[]{}), 
                    new String[]{}); 
      //Invoke the Web Service
      PortletDescriptionResponse ra = endpoint.getPortletDescription(ic);
      assertNotNull("PortletDescriptionResponse != null", ra);
   } 
   
   public void testGetPortletDescriptionDynamicEndpoint() throws Exception
   {
      Service service = getService();
      WSRP_v1_PortletManagement_PortType endpoint =
         (WSRP_v1_PortletManagement_PortType)service.getPort(WSRP_v1_PortletManagement_PortType.class);
      ((javax.xml.rpc.Stub)endpoint)._setProperty("javax.xml.rpc.service.endpoint.address",
                 "http://" + getServerHost() +":8080/wsrp/PortletManagementService"); 
      
      GetPortletDescription ic = new GetPortletDescription(new RegistrationContext(), 
                    new PortletContext("portletHandle", "portletState".getBytes(), new Extension[]{}), 
                    new UserContext("userContextKey", new String[]{},new UserProfile(), new Extension[]{}), 
                    new String[]{}); 
      //Invoke the Web Service
      PortletDescriptionResponse ra = endpoint.getPortletDescription(ic);
      assertNotNull("PortletDescriptionResponse != null", ra);
   } 

   public void testClonePortlet() throws Exception
   {
      Service service = getService();
      WSRP_v1_PortletManagement_PortType endpoint =
         (WSRP_v1_PortletManagement_PortType)service.getPort(WSRP_v1_PortletManagement_PortType.class);
      ClonePortlet cp = new ClonePortlet(new RegistrationContext(), new PortletContext(), 
            new UserContext() );
      //Invoke the Web Service
      PortletContext ra = endpoint.clonePortlet(cp);
      assertNotNull("PortletContext != null", ra);
   } 
   
   public void testDestroyPortlets() throws Exception
   {
      Service service = getService();
      WSRP_v1_PortletManagement_PortType endpoint =
         (WSRP_v1_PortletManagement_PortType)service.getPort(WSRP_v1_PortletManagement_PortType.class);
      DestroyPortlets cp = new DestroyPortlets(new RegistrationContext(), new String[]{}); 
      //Invoke the Web Service
      DestroyPortletsResponse ra = endpoint.destroyPortlets(cp);
      assertNotNull("DestroyPortletsResponse != null", ra);
   } 
   
   public void testSetPortletProperties() throws Exception
   {
      Service service = getService();
      WSRP_v1_PortletManagement_PortType endpoint =
         (WSRP_v1_PortletManagement_PortType)service.getPort(WSRP_v1_PortletManagement_PortType.class);
      SetPortletProperties cp = new SetPortletProperties(new RegistrationContext(), 
            new PortletContext(), new UserContext() , new PropertyList()); 
      //Invoke the Web Service
      PortletContext ra = endpoint.setPortletProperties(cp);
      assertNotNull("PortletContext != null", ra);
   } 
   
   public void testGetPortletProperties() throws Exception
   {
      Service service = getService();
      WSRP_v1_PortletManagement_PortType endpoint =
         (WSRP_v1_PortletManagement_PortType)service.getPort(WSRP_v1_PortletManagement_PortType.class);
      GetPortletProperties cp = new GetPortletProperties(new RegistrationContext(), 
            new PortletContext(),new UserContext(), new String[]{}); 
      //Invoke the Web Service
      PropertyList ra = endpoint.getPortletProperties(cp);
      assertNotNull("PropertyList != null", ra);
   } 
   
   public void testGetPortletPropertyDescription() throws Exception
   {
      Service service = getService();
      WSRP_v1_PortletManagement_PortType endpoint =
         (WSRP_v1_PortletManagement_PortType)service.getPort(WSRP_v1_PortletManagement_PortType.class);
      GetPortletPropertyDescription cp = new GetPortletPropertyDescription(new RegistrationContext(), 
             new PortletContext(), new UserContext(), new String[]{}); 
      //Invoke the Web Service
      PortletPropertyDescriptionResponse ra = endpoint.getPortletPropertyDescription(cp);
      assertNotNull("PortletPropertyDescriptionResponse != null", ra);
   }
   
   private Service getService() throws NamingException
   {
      InitialContext iniCtx = getClientContext("wsrp-client");
      Service service = (Service)iniCtx.lookup("java:comp/env/service/PortletManagementService");
      return service;
   }  
}
