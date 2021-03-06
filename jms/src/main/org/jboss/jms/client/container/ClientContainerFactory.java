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

import java.lang.reflect.Proxy;

import javax.jms.JMSException;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.metadata.SimpleMetaData;
import org.jboss.jms.client.BrowserDelegate;
import org.jboss.jms.client.ConnectionDelegate;
import org.jboss.jms.client.ConsumerDelegate;
import org.jboss.jms.client.ImplementationDelegate;
import org.jboss.jms.client.ProducerDelegate;
import org.jboss.jms.client.SessionDelegate;
import org.jboss.jms.container.Container;
import org.jboss.jms.container.ContainerObjectOverridesInterceptor;
import org.jboss.jms.container.DispatchInterceptor;
import org.jboss.jms.container.ForwardInterceptor;
import org.jboss.jms.container.LogInterceptor;

/**
 * The in jvm implementation
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class ClientContainerFactory
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   public static ConnectionDelegate getConnectionContainer
   (
      ImplementationDelegate delegate,
      ConnectionDelegate server,
      Interceptor[] interceptors,
      SimpleMetaData metadata      
   )
      throws JMSException
   {
      return (ConnectionDelegate) getContainer
      (
         ConnectionDelegate.class,
         null,
         server,
         interceptors,
         metadata
      );
   }

   public static SessionDelegate getSessionContainer
   (
      Container parent,
      SessionDelegate server,
      Interceptor[] interceptors,
      SimpleMetaData metadata      
   )
      throws JMSException
   {
      return (SessionDelegate) getContainer
      (
         SessionDelegate.class,
         parent,
         server,
         interceptors,
         metadata
      );
   }

   public static BrowserDelegate getBrowserContainer
   (
      Container parent,
      BrowserDelegate server,
      Interceptor[] interceptors,
      SimpleMetaData metadata      
   )
      throws JMSException
   {
      return (BrowserDelegate) getContainer
      (
         BrowserDelegate.class,
         parent,
         server,
         interceptors,
         metadata
      );
   }

   public static ConsumerDelegate getConsumerContainer
   (
      Container parent,
      ConsumerDelegate server,
      Interceptor[] interceptors,
      SimpleMetaData metadata      
   )
      throws JMSException
   {
      return (ConsumerDelegate) getContainer
      (
         ConsumerDelegate.class,
         parent,
         server,
         interceptors,
         metadata
      );
   }

   public static ProducerDelegate getProducerContainer
   (
      Container parent,
      ProducerDelegate server,
      Interceptor[] interceptors,
      SimpleMetaData metadata      
   )
      throws JMSException
   {
      return (ProducerDelegate) getContainer
      (
         ProducerDelegate.class,
         parent,
         server,
         interceptors,
         metadata
      );
   }
   
   public static Object getContainer(
      Class clazz,
      Container parent,
      Object delegate,
      Interceptor[] interceptors, 
      SimpleMetaData metadata
   )
      throws JMSException
   {
	   Interceptor[] standard = getStandard();
	
      int stackSize = standard.length + interceptors.length + 1;
      Interceptor[] stack = new Interceptor[stackSize];
   	System.arraycopy(standard, 0, stack, 0, standard.length);
	   System.arraycopy(interceptors, 0, stack, standard.length, interceptors.length);

      if (delegate instanceof Proxy)
      {
         try
         {
            stack[stackSize-1] = new ForwardInterceptor(Container.getContainer(delegate));
         }
         catch (Throwable ignored)
         {
         }
      }
      if (stack[stackSize-1] == null)
         stack[stackSize-1] = new DispatchInterceptor(delegate);

	   Container container = new Container(parent, stack, metadata);
	   Object result = Proxy.newProxyInstance
      (
			Thread.currentThread().getContextClassLoader(),
			new Class[] { clazz },
			container
      );
	   container.setProxy(result);
	   return result;
   }

   public static Interceptor[] getStandard()
   {
      return new Interceptor[]
      {
         ContainerObjectOverridesInterceptor.singleton,
         JMSExceptionInterceptor.singleton,
         LogInterceptor.singleton,
         new ClosedInterceptor()
      };
   }

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // Protected ------------------------------------------------------

   // Package Private ------------------------------------------------

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------

}
