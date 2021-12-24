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
package org.jboss.jms.container;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;

/**
 * An interceptor for providing standard object methods
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class ContainerObjectOverridesInterceptor
   implements Interceptor
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   public static ContainerObjectOverridesInterceptor singleton = new ContainerObjectOverridesInterceptor();

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // Interceptor implementation -----------------------------------

   public String getName()
   {
      return "ContainerObjectOverridesInterceptor";
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      MethodInvocation mi = (MethodInvocation) invocation;
      String methodName = mi.getMethod().getName();
      if (methodName.equals("equals"))
         return equals(mi);
      else if (methodName.equals("hashCode"))
         return hashCode(mi);
      else if (methodName.equals("toString"))
         return toString(mi);
      else
         return invocation.invokeNext();
   }

   // Protected ------------------------------------------------------

   protected String toString(MethodInvocation mi)
   {
      Object proxy = Container.getProxy(mi);
      String className = proxy.getClass().getInterfaces()[0].getName();
      StringBuffer buffer = new StringBuffer(20);
      buffer.append(className).append('@').append(System.identityHashCode(proxy));
      return buffer.toString();
   }

   protected Boolean equals(MethodInvocation mi)
   {
      return new Boolean(Container.getProxy(mi).equals(mi.getArguments()[0]));
   }

   protected Integer hashCode(MethodInvocation mi)
   {
      return new Integer(System.identityHashCode(Container.getProxy(mi)));
   }
   
   // Package Private ------------------------------------------------

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------

}
