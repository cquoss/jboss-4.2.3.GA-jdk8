/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.jaxws;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;

import org.jboss.wsf.spi.annotation.WebContext;

/**
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 65795 $
 */
@WebService(endpointInterface = "org.jboss.ejb3.test.jaxws.EndpointInterface", serviceName = "TestService2", targetNamespace = "http://org.jboss.ws/jaxws/context")
@WebContext(contextRoot="jaxws-context", urlPattern="/testService2")
@Stateless
public class PostConstructEJB
{
   @Resource
   WebServiceContext wsCtx;

   @Resource
   SessionContext ejbCtx;

   public String echoWS(String input)
   {
      try
      {
         String retValue = getValueJAXWS();
         return retValue + "2";
      }
      catch (SOAPException ex)
      {
         throw new WebServiceException(ex);
      }
   }

   public String echoRPC(String input)
   {
      try
      {
         String retValue = getValueJAXRPC();
         return retValue + "2";
      }
      catch (SOAPException ex)
      {
         throw new WebServiceException(ex);
      }
   }

   private String getValueJAXWS() throws SOAPException
   {
      if(wsCtx == null)
         throw new IllegalStateException("wsCtx was not injected");
      javax.xml.ws.handler.soap.SOAPMessageContext jaxwsContext = (javax.xml.ws.handler.soap.SOAPMessageContext)wsCtx.getMessageContext();
      SOAPMessage soapMessage = jaxwsContext.getMessage();
      SOAPElement soapElement = (SOAPElement)soapMessage.getSOAPBody().getChildElements().next();
      soapElement = (SOAPElement)soapElement.getChildElements().next();
      return soapElement.getValue();
   }

   private String getValueJAXRPC() throws SOAPException
   {
      if(ejbCtx == null)
         throw new IllegalStateException("ejbCtx was not injected");
      javax.xml.rpc.handler.soap.SOAPMessageContext jaxrpcContext = (javax.xml.rpc.handler.soap.SOAPMessageContext)ejbCtx.getMessageContext();
      SOAPMessage soapMessage = jaxrpcContext.getMessage();
      SOAPElement soapElement = (SOAPElement)soapMessage.getSOAPBody().getChildElements().next();
      soapElement = (SOAPElement)soapElement.getChildElements().next();
      return soapElement.getValue();
   }
   
   @SuppressWarnings("unused")
   @PostConstruct
   private void postConstruct()
   {
      // injection must be finished here
      
      if(ejbCtx == null)
         throw new IllegalStateException("ejbCtx was not injected in post construct");
      
      if(wsCtx == null)
         throw new IllegalStateException("wsCtx was not injected in post construct");
      
      System.err.println("post construct done");
   }
}
