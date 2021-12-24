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
import org.jboss.jms.client.BrowserDelegate;
import org.jboss.jms.client.ConsumerDelegate;
import org.jboss.jms.client.ProducerDelegate;
import org.jboss.jms.client.SessionDelegate;
import org.jboss.jms.container.Container;
import org.jboss.jms.message.standard.MessageFactoryInterceptor;

/**
 * An interceptor for creating delegates
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class FactoryInterceptor
   implements Interceptor
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   public static final FactoryInterceptor singleton = new FactoryInterceptor();

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // Interceptor implementation -----------------------------------

   public String getName()
   {
      return "FactoryInterceptor";
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      Object result = invocation.invokeNext();
      String methodName = ((MethodInvocation) invocation).getMethod().getName();
      if (methodName.equals("createSession"))
         return createSession(invocation, (SessionDelegate) result);
      else if (methodName.equals("createBrowser"))
         return createBrowser(invocation, (BrowserDelegate) result);
      else if (methodName.equals("createConsumer"))
         return createConsumer(invocation, (ConsumerDelegate) result);
      else if (methodName.equals("createProducer"))
         return createProducer(invocation, (ProducerDelegate) result);
      else
         return result;
   }

   // Protected ------------------------------------------------------

   protected SessionDelegate createSession(Invocation invocation, SessionDelegate server)
      throws Throwable
   {
      Interceptor[] interceptors = new Interceptor[]
      {
         MessageFactoryInterceptor.singleton,
         FactoryInterceptor.singleton
      };
      Container connection = Container.getContainer(invocation); 
      return ClientContainerFactory.getSessionContainer(connection, server, interceptors, null);
   }

   protected BrowserDelegate createBrowser(Invocation invocation, BrowserDelegate server)
      throws Throwable
   {
      Interceptor[] interceptors = new Interceptor[0];
      Container session = Container.getContainer(invocation); 
      return ClientContainerFactory.getBrowserContainer(session, server, interceptors, null);
   }

   protected ConsumerDelegate createConsumer(Invocation invocation, ConsumerDelegate server)
      throws Throwable
   {
      Interceptor[] interceptors = new Interceptor[0];
      Container session = Container.getContainer(invocation); 
      return ClientContainerFactory.getConsumerContainer(session, server, interceptors, null);
   }

   protected ProducerDelegate createProducer(Invocation invocation, ProducerDelegate server)
      throws Throwable
   {
      Interceptor[] interceptors = new Interceptor[0];
      Container session = Container.getContainer(invocation); 
      return ClientContainerFactory.getProducerContainer(session, server, interceptors, null);
   }

   // Package Private ------------------------------------------------

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------

}
