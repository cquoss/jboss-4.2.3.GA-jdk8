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
package org.jboss.jms.server.container;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.metadata.SimpleMetaData;
import org.jboss.jms.client.BrowserDelegate;
import org.jboss.jms.client.ConsumerDelegate;
import org.jboss.jms.client.ProducerDelegate;
import org.jboss.jms.client.SessionDelegate;
import org.jboss.jms.container.Container;

/**
 * The interceptor to create server containers
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class ServerFactoryInterceptor
   implements Interceptor
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   public static final ServerFactoryInterceptor singleton = new ServerFactoryInterceptor();

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // Interceptor implementation -----------------------------------

   public String getName()
   {
      return "ServerFactoryInterceptor";
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      Object result = invocation.invokeNext();
      
      MethodInvocation mi = (MethodInvocation) invocation;
      String methodName = mi.getMethod().getName();
      if (methodName.equals("createSession"))
         return createSession(mi, (SessionDelegate) result);
      else if (methodName.equals("createBrowser"))
         return createBrowser(mi, (BrowserDelegate) result);
      else if (methodName.equals("createConsumer"))
         return createConsumer(mi, (ConsumerDelegate) result);
      else if (methodName.equals("createProducer"))
         return createProducer(mi, (ProducerDelegate) result);
      else
         return result;
   }

   // Protected ------------------------------------------------------
   
   protected SessionDelegate createSession(MethodInvocation invocation, SessionDelegate target)
      throws Throwable
   {
      Client client = Client.getClient(invocation);
      SimpleMetaData metaData = client.createSession(invocation);
      Interceptor[] interceptors = new Interceptor[]
      {
         singleton,
         ServerSessionInterceptor.singleton 
      };
      Container connection = Container.getContainer(invocation); 
      return ServerContainerFactory.getSessionContainer(connection, interceptors, metaData);
   }
   
   protected BrowserDelegate createBrowser(MethodInvocation invocation, BrowserDelegate target)
      throws Throwable
   {
      Client client = Client.getClient(invocation);
      SimpleMetaData metaData = client.createBrowser(invocation);
      Interceptor[] interceptors = new Interceptor[]
      {
         ServerBrowserInterceptor.singleton 
      };
      Container session = Container.getContainer(invocation); 
      return ServerContainerFactory.getBrowserContainer(session, interceptors, metaData);
   }
   
   protected ConsumerDelegate createConsumer(MethodInvocation invocation, ConsumerDelegate target)
      throws Throwable
   {
      Client client = Client.getClient(invocation);
      SimpleMetaData metaData = client.createConsumer(invocation);
      Interceptor[] interceptors = new Interceptor[]
      {
         ServerConsumerInterceptor.singleton 
      };
      Container session = Container.getContainer(invocation); 
      return ServerContainerFactory.getConsumerContainer(session, interceptors, metaData);
   }
   
   protected ProducerDelegate createProducer(MethodInvocation invocation, ProducerDelegate target)
      throws Throwable
   {
      Client client = Client.getClient(invocation);
      SimpleMetaData metaData = client.createProducer(invocation);
      Interceptor[] interceptors = new Interceptor[]
      {
         ServerProducerInterceptor.singleton 
      };
      Container session = Container.getContainer(invocation); 
      return ServerContainerFactory.getProducerContainer(session, interceptors, metaData);
   }

   // Package Private ------------------------------------------------

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------

}
