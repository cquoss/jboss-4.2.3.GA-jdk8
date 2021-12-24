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
package org.jboss.mq.server.jmx;
import javax.jms.IllegalStateException;
import javax.management.ObjectName;

import org.jboss.mq.server.JMSServerInterceptor;
import org.jboss.system.ServiceMBeanSupport;
/**
 * Adapts JBossMQService to deliver JMSServerInvoker.
 *
 * @author <a href="Peter Antman">Peter Antman</a>
 * @version $Revision: 57198 $
 */

abstract public class InterceptorMBeanSupport  
   extends ServiceMBeanSupport
   implements InterceptorMBean
{
   /**
    * The next interceptor this interceptor invokes.
    */
   private JMSServerInterceptor nextInterceptor;
   private ObjectName nextInterceptorObjName;
   
  
   public ObjectName getNextInterceptor() 
   {
      return this.nextInterceptorObjName;
   }
   
   public void setNextInterceptor(ObjectName  jbossMQService) 
   {
      this.nextInterceptorObjName = jbossMQService;
   }
   
   protected void startService() throws Exception
   {
      if( nextInterceptorObjName != null ) {
         nextInterceptor = (JMSServerInterceptor)getServer().getAttribute(nextInterceptorObjName, "Interceptor");
         if (nextInterceptor == null) 
            throw new IllegalStateException("The next interceptor was invalid.");         
      }
      getInterceptor().setNext(nextInterceptor);
   }
   
} 
