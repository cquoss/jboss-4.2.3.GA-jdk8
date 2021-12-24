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

import java.util.Arrays;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.logging.Logger;

/**
 * An interceptor for logging invocations.
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class LogInterceptor
   implements Interceptor
{
   // Constants -----------------------------------------------------

   private static final Logger log = Logger.getLogger(LogInterceptor.class);

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   public static LogInterceptor singleton = new LogInterceptor();

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // Interceptor implementation -----------------------------------

   public String getName()
   {
      return "LogInterceptor";
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      StringBuffer desc = getDescription(invocation);
      log.info("invoke:" + desc);
      Object result = null;
      try
      {
         result = invocation.invokeNext();
         log.info("result: " + result + " of invoke:" + desc);
         return result;
      }
      catch (Throwable t)
      {
         log.info("error in invoke:" + desc, t);
         throw t;
      }
   }

   // Protected ------------------------------------------------------

   protected StringBuffer getDescription(Invocation invocation)
   {
      MethodInvocation mi = (MethodInvocation) invocation;
      StringBuffer buffer = new StringBuffer(50);
      buffer.append(" method=").append(mi.getMethod().getName());
      buffer.append(" params=");
      if (mi.getArguments() == null)
         buffer.append("[]");
      else
         buffer.append(Arrays.asList(mi.getArguments()));
      buffer.append(" object=").append(Container.getProxy(invocation));
      return buffer;
   }
   
   // Package Private ------------------------------------------------

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------

}
