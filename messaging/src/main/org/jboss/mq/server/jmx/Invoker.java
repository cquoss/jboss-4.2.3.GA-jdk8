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
import org.jboss.mq.server.JMSServerInvoker;
import org.jboss.system.ServiceMBeanSupport;
/**
 * Adapts JBossMQService to deliver JMSServerInvoker.
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 * 
 * @author <a href="Peter Antman">Peter Antman</a>
 * @version $Revision: 57198 $
 */

public class Invoker  
   extends ServiceMBeanSupport
   implements InvokerMBean
{
   /**
    * The next interceptor this interceptor invokes.
    */
   private JMSServerInterceptor nextInterceptor;
   private ObjectName nextInterceptorObjName;
   private JMSServerInvoker invoker;
  

   /**
    * @jmx:managed-attribute
    */
   public ObjectName getNextInterceptor() 
   {
      return this.nextInterceptorObjName;
   }
   
   /**
    * @jmx:managed-attribute
    */
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
      invoker.setNext(nextInterceptor);
   }
   

   /**
    * @see ServiceMBeanSupport#createService()
    */
   protected void createService() throws Exception
   {
      super.createService();
      invoker = new JMSServerInvoker();
   }

   /**
    * @jmx:managed-attribute
    * @see InvokerMBean#getInvoker()
    */
   public JMSServerInvoker getInvoker()
   {
      return invoker;
   }

} 
