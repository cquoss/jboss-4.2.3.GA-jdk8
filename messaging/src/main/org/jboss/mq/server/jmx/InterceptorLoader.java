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
import org.jboss.mq.server.JMSServerInterceptor;

/**
 * Adapts JBossMQService to deliver JMSServerInvoker.
 *
 *
 * @jmx:mbean extends="org.jboss.mq.server.jmx.InterceptorMBean"
 *
 * @author <a href="Peter Antman">Peter Antman</a>
 * @version $Revision: 57198 $
 */
public class InterceptorLoader extends InterceptorMBeanSupport implements InterceptorLoaderMBean
{
   /**
    * The invoker this service boostraps
    */
   private JMSServerInterceptor interceptor;
   /**
    * Gets the JMSServer attribute of the JBossMQService object, here we
    * wrap the server in the invoker loaded.
    *
    * @return    The JMSServer value
    */
   public JMSServerInterceptor getInterceptor()
   {
      return interceptor;
   }
   /**
    * @jmx:managed-attribute
    */
   public void setInterceptorClass(String c) throws Exception
   {
      interceptor = (JMSServerInterceptor) Thread.currentThread().getContextClassLoader().loadClass(c).newInstance();
   }
   /**
    * @jmx:managed-attribute
    */
   public String getInterceptorClass() throws Exception
   {
      return interceptor.getClass().getName();
   }
 
   protected void startService() throws Exception
   {
      if (interceptor == null)
         throw new IllegalStateException("The interceptor class was not set (null)");
      super.startService();
   }
}
