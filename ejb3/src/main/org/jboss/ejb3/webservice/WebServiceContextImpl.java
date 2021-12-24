/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.webservice;

import java.security.Principal;

import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.jboss.ejb3.stateless.StatelessBeanContext;
import org.w3c.dom.Element;

import org.jboss.util.NotImplementedException;


/**
 * An injectable web service context.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class WebServiceContextImpl implements WebServiceContext
{
   private StatelessBeanContext ctx;
   
   public WebServiceContextImpl(StatelessBeanContext ctx)
   {
      assert ctx != null : "ctx must be set";
      
      this.ctx = ctx;
   }
   
   protected WebServiceContext getDelegate()
   {
      WebServiceContext wsc = ctx.getWebServiceContext();
      if(wsc == null)
         throw new IllegalStateException("No web service context associated with this bean");
      return wsc;
   }
   
   public MessageContext getMessageContext()
   {
      return getDelegate().getMessageContext();
   }

   public Principal getUserPrincipal()
   {
      return getDelegate().getUserPrincipal();
   }

   public boolean isUserInRole(String role)
   {
      return getDelegate().isUserInRole(role);
   }
   
   public EndpointReference getEndpointReference(Element... referenceParameters)
   {
	   throw new NotImplementedException();
   }
   
   public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, Element... referenceParameters)
   {
	   throw new NotImplementedException();
   }

}
