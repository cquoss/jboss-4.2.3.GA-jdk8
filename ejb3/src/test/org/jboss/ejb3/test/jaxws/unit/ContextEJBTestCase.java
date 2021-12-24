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
package org.jboss.ejb3.test.jaxws.unit;

// $Id: ContextEJBTestCase.java 1874 2007-01-09 14:28:41Z thomas.diesler@jboss.com $

import junit.framework.Test;

import org.jboss.ejb3.test.JBossWithKnownIssuesTestCase;
import org.jboss.ejb3.test.jaxws.EndpointInterface;
import org.jboss.ejb3.test.jaxws.RemoteInterface;

import javax.ejb.EJBException;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceFactory;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;

import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;

/**
 * Test JAXWS WebServiceContext
 *
 * @author Thomas.Diesler@jboss.org
 * @since 29-Apr-2005
 */
public class ContextEJBTestCase extends JBossWithKnownIssuesTestCase
{
   public ContextEJBTestCase(String name)
   {
      super(name);
      // TODO Auto-generated constructor stub
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ContextEJBTestCase.class, "jaxws-context.jar");
   }
   
   public void testClientAccessWS() throws Exception
   {
      log.info("In case of connection exception, there is a host name defined in the wsdl, which used to be '@jbosstest.host.name@'");
      URL wsdlURL = new File("../src/resources/test/jaxws/TestService.wsdl").toURL();
      QName qname = new QName("http://org.jboss.ws/jaxws/context", "TestService");
      Service service = Service.create(wsdlURL, qname);
      EndpointInterface port = (EndpointInterface)service.getPort(EndpointInterface.class);
      
      String helloWorld = "Hello world!";
      try
      {
         Object retObj = port.echoWS(helloWorld);
         assertEquals(helloWorld, retObj);
      }
      catch(SOAPFaultException e)
      {
         assertEquals("java.lang.IllegalStateException: wsCtx was not injected", e.getMessage());
         showKnownIssue("EJBTHREE-900");
      }
   }

   public void testClientAccessRPC() throws Exception
   {
      log.info("In case of connection exception, there is a host name defined in the wsdl, which used to be '@jbosstest.host.name@'");
      URL wsdlURL = new URL("http://localhost:8080/jaxws-context/testService?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/context", "TestService");
      ServiceFactory factory = ServiceFactory.newInstance();
      javax.xml.rpc.Service service = factory.createService(wsdlURL, qname);
      EndpointInterface port = (EndpointInterface)service.getPort(EndpointInterface.class);
      
      String helloWorld = "Hello world!";
      try
      {
         Object retObj = port.echoRPC(helloWorld);
         assertEquals(helloWorld, retObj);
      }
      catch(RemoteException e)
      {
         assertNotNull(e.getCause());
         assertTrue(e.getCause() instanceof javax.xml.rpc.soap.SOAPFaultException);
         assertEquals("java.lang.IllegalStateException: No message context found", e.getCause().getMessage());
         showKnownIssue("EJBTHREE-757");
      }
   }
   
   public void testClientAccessWSThroughRPC() throws Exception
   {
      log.info("In case of connection exception, there is a host name defined in the wsdl, which used to be '@jbosstest.host.name@'");
      URL wsdlURL = new URL("http://localhost:8080/jaxws-context/testService?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/context", "TestService");
      ServiceFactory factory = ServiceFactory.newInstance();
      javax.xml.rpc.Service service = factory.createService(wsdlURL, qname);
      EndpointInterface port = (EndpointInterface)service.getPort(EndpointInterface.class);
      
      String helloWorld = "Hello world!";
      try
      {
         Object retObj = port.echoWS(helloWorld);
         assertEquals(helloWorld, retObj);
      }
      catch(RemoteException e)
      {
         assertNotNull(e.getCause());
         assertTrue(e.getCause() instanceof javax.xml.rpc.soap.SOAPFaultException);
         assertEquals("java.lang.IllegalStateException: No message context found", e.getCause().getMessage());
         showKnownIssue("EJBTHREE-757");
      }
   }
   
   public void testRemoteInterface() throws Exception
   {
      RemoteInterface bean = (RemoteInterface) getInitialContext().lookup("EndpointEJB/remote");
      try
      {
         bean.someMethod();
         fail("Accessing WebServiceContext outside a WS call is not valid");
      }
      catch(IllegalStateException e)
      {
         // perfect
      }
      catch(EJBException e)
      {
         assertNotNull(e.getCause());
         assertTrue(e.getCause() instanceof IllegalStateException);
         // less than perfect, but currently the case
      }
   }
   
   public void testService2() throws Exception
   {
      log.info("In case of connection exception, there is a host name defined in the wsdl, which used to be '@jbosstest.host.name@'");
      URL wsdlURL = new File("../src/resources/test/jaxws/TestService.wsdl").toURL();
      QName qname = new QName("http://org.jboss.ws/jaxws/context", "TestService2");
      Service service = Service.create(wsdlURL, qname);
      EndpointInterface port = (EndpointInterface)service.getPort(EndpointInterface.class);
      
      String helloWorld = "Hello world!";
      try
      {
         Object retObj = port.echoWS(helloWorld);
         assertEquals(helloWorld + "2", retObj);
      }
      catch(SOAPFaultException e)
      {
         assertEquals("java.lang.IllegalStateException: wsCtx was not injected in post construct", e.getMessage());
         showKnownIssue("EJBTHREE-1055");
      }
   }

}
