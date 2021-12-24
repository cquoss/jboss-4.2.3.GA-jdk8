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
package org.jboss.jms.client.container;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;

/**
 * An interceptor for checking the client id.
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class ClientIDInterceptor
   implements Interceptor
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** The client id */
   private String clientID;
   
   /** Can we set the client id */
   private boolean determinedClientID = false;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // Interceptor implementation -----------------------------------

   public String getName()
   {
      return "ClientIDInterceptor";
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      String methodName = ((MethodInvocation) invocation).getMethod().getName();
      if (methodName.equals("getClientID"))
         return clientID;

      if (methodName.equals("setClientID"))
      {
         setClientID(invocation);
         return null;
      }

      addClientID(invocation);
      try
      {
         return invocation.invokeNext();
      }
      finally
      {
         if (determinedClientID == false)
         {
            clientID = (String) invocation.getResponseAttachment("JMSClientID");
            if (clientID == null)
               throw new IllegalStateException("Unable to determine clientID");
            determinedClientID = true;
         }
      }
   }

   // Protected ------------------------------------------------------

   /**
    * Add the client id or piggy back the request
    * for a client id on this invocation
    * 
    * @param invocation the invocation
    */
   protected void addClientID(Invocation invocation)
      throws Throwable
   {
      if (determinedClientID)
         invocation.getMetaData().addMetaData("JMS", "clientID", clientID);
      else
         invocation.getMetaData().addMetaData("JMS", "clientID", null);
   }

   /**
    * Set the client id
    * 
    * @param invocation the invocation
    */
   protected void setClientID(Invocation invocation)
      throws Throwable
   {
      if (determinedClientID)
         throw new IllegalStateException("Client id is already set");

      MethodInvocation mi = (MethodInvocation) invocation;
      clientID = (String) mi.getArguments()[0];
      if (clientID == null)
         throw new IllegalArgumentException("Null client id");

      invocation.invokeNext();
      determinedClientID = true;
   }

   // Package Private ------------------------------------------------

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------

}
