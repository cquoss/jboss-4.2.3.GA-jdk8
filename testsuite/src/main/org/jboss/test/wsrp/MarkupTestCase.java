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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.rpc.Service;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import junit.framework.Test;

import org.jboss.test.webservice.WebserviceTestBase;
import org.jboss.test.wsrp.core.BlockingInteractionResponse;
import org.jboss.test.wsrp.core.Extension;
import org.jboss.test.wsrp.core.GetMarkup;
import org.jboss.test.wsrp.core.InitCookie;
import org.jboss.test.wsrp.core.InteractionParams;
import org.jboss.test.wsrp.core.MarkupParams;
import org.jboss.test.wsrp.core.MarkupResponse;
import org.jboss.test.wsrp.core.PerformBlockingInteraction;
import org.jboss.test.wsrp.core.PortletContext;
import org.jboss.test.wsrp.core.RegistrationContext;
import org.jboss.test.wsrp.core.ReleaseSessions;
import org.jboss.test.wsrp.core.ReturnAny;
import org.jboss.test.wsrp.core.RuntimeContext;
import org.jboss.test.wsrp.core.UserContext;
import org.jboss.test.wsrp.core.UserProfile;

//$Id: MarkupTestCase.java 57211 2006-09-26 12:39:46Z dimitris@jboss.org $
/**
 *  Test Case that tests the MarkUp WSRP service
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Mar 27, 2006
 *  @version $Revision: 57211 $
 */
public class MarkupTestCase extends WebserviceTestBase
{ 
   private String url = "http://" 
                 + getServerHost() + ":" 
                 + Integer.getInteger("web.port", 8080) + "/wsrp/MarkupService"; 
   
   public MarkupTestCase(String name)
   {
      super(name); 
   }
   /**
    * deploy the test archives
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(MarkupTestCase.class, "wsrp.war, wsrp-client.jar");
   }

   public void testGetMarkup() throws Exception
   {
      Service service = getService();
      WSRP_v1_Markup_PortType endpoint =
         (WSRP_v1_Markup_PortType)service.getPort(WSRP_v1_Markup_PortType.class);
      GetMarkup gm = new GetMarkup(new RegistrationContext(), 
            new PortletContext("portletHandle", "portletState".getBytes(), new Extension[]{}), 
            new RuntimeContext(), 
            new UserContext("userContextKey", new String[]{},new UserProfile(), new Extension[]{}), 
            new MarkupParams()); 
      //Invoke the Web Service
      MarkupResponse gmr = endpoint.getMarkup(gm);  
      assertNotNull("gmr != null", gmr);
   }
   
   public void testPerformBlockingInteraction() throws Exception
   {
      Service service = getService();
      WSRP_v1_Markup_PortType endpoint =
         (WSRP_v1_Markup_PortType)service.getPort(WSRP_v1_Markup_PortType.class);
      PerformBlockingInteraction pbi = new PerformBlockingInteraction(new RegistrationContext(), 
            new PortletContext("portletHandle", "portletState".getBytes(), new Extension[]{}), 
            new RuntimeContext(),
            new UserContext("userContextKey", new String[]{},new UserProfile(), new Extension[]{}), 
            new MarkupParams(), 
            new InteractionParams()); 
      //Invoke the Web Service
      BlockingInteractionResponse bir = endpoint.performBlockingInteraction(pbi);  
      assertNotNull("bir != null", bir);
   }
   
   public void testReleaseSessions() throws Exception
   {
      Service service = getService();
      WSRP_v1_Markup_PortType endpoint =
         (WSRP_v1_Markup_PortType)service.getPort(WSRP_v1_Markup_PortType.class);
      ReleaseSessions rs = new ReleaseSessions(new RegistrationContext(), new String[]{}); 
      //Invoke the Web Service
      ReturnAny ra = endpoint.releaseSessions(rs); 
      assertNotNull("ra != null", ra);
   } 
   
   public void testInitCookie() throws Exception
   {
      Service service = getService();
      WSRP_v1_Markup_PortType endpoint =
         (WSRP_v1_Markup_PortType)service.getPort(WSRP_v1_Markup_PortType.class);
      InitCookie ic = new InitCookie(new RegistrationContext()); 
      //Invoke the Web Service
      ReturnAny ra = endpoint.initCookie(ic);
      assertNotNull("ra != null", ra);
   } 
   
   /**
    * Send a blank soap message to the wsrp endpoint
    * 
    * @throws Exception
    */
   public void testBlankSOAPMessage() throws Exception
   { 
      String xmlMessage = " ";
      //construct the SOAP message
      MessageFactory factory = MessageFactory.newInstance();
      SOAPMessage msg = factory.createMessage(null,new ByteArrayInputStream(xmlMessage.getBytes()));
      assertNotNull("SOAPMessage is not null", msg);
      msg.writeTo(System.out);
      
      //Send the SOAPMessage
      SOAPConnectionFactory smFactory = SOAPConnectionFactory.newInstance();
      SOAPConnection con = smFactory.createConnection();
      SOAPMessage reply = con.call(msg, new URL(url));
      reply.writeTo(System.out);
      assertNotNull("Reply SOAPMessage is not null", reply);
      assertNotNull("Reply SOAPPart is not null", reply.getSOAPPart());
      assertNotNull("Reply SOAPEnvelope is not null", reply.getSOAPPart().getEnvelope());
      assertNotNull("Reply SOAPBody is not null", reply.getSOAPPart().getEnvelope().getBody());
      SOAPFault soapFault = reply.getSOAPPart().getEnvelope().getBody().getFault();
      assertNull("SOAP Fault is null", soapFault);
   }
   
   /**
    * Send a soap message to the wsrp markup service 
    * that has a jaxrpc handler fitted to strip the
    * wsrp extensions. The jaxrpc handler checks to 
    * see if there is a SOAP header element asking for
    * the strip.
    * 
    * @throws Exception
    */
   public void testWSRPExtensionInteropWithHandler() throws Exception
   {
      checkWSRPService(url, true);
   }
   
   /**
    * Send a soap message to the wsrp markup service
    * 
    * @throws Exception
    */
   public void testWSRPExtensionInterop() throws Exception
   {
      checkWSRPService(url, false);
   }
   
   private void checkWSRPService(String endpointURL, boolean needStrippingHeader) 
   throws Exception 
   {
      String fileurl = "resources/wsrp/messages/extension_markup.txt";
      File file = new File(fileurl);
      assertNotNull(file);
      //construct the SOAP message
      MessageFactory factory = MessageFactory.newInstance();
      SOAPMessage msg = factory.createMessage(null,new FileInputStream(file));
      if(needStrippingHeader)
         addStrippingHeader(msg);
      assertNotNull("SOAPMessage is not null", msg); 
      
      //Send the SOAPMessage
      SOAPConnectionFactory smFactory = SOAPConnectionFactory.newInstance();
      SOAPConnection con = smFactory.createConnection();
      SOAPMessage reply = con.call(msg, new URL(endpointURL)); 
      assertNotNull("Reply SOAPMessage is not null", reply);
      SOAPBody soapBody = reply.getSOAPPart().getEnvelope().getBody();
      assertNotNull("Reply SoapBody is not null", soapBody);
      assertFalse("SOAPFault is null", soapBody.hasFault()); 
      assertNull("SOAP Fault is null", soapBody.getFault());
   }
   
   private Service getService() throws NamingException
   {
      InitialContext iniCtx = getClientContext("wsrp-client");
      Service service = (Service)iniCtx.lookup("java:comp/env/service/MarkupService");
      return service;
   } 
   
   /**
    * Add a soap header element to the soap message
    * 
    * @param soapMessage
    * @throws Exception
    */
   private void addStrippingHeader(SOAPMessage soapMessage) throws Exception
   { 
      SOAPEnvelope env = soapMessage.getSOAPPart().getEnvelope();
      SOAPHeader header = env.getHeader();
      if(header == null)
         header = env.addHeader();
      header.addChildElement("jboss_wsrp_remove_extension"); 
   }
}
