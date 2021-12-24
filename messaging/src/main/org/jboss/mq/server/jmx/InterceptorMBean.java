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

import javax.management.ObjectName;

import org.jboss.mq.server.JMSServerInterceptor;
import org.jboss.system.ServiceMBean;
/**
 * Mbean interface for JMBossMQ to help 
 * load an JMSServerInterceptor
 *
 * @author <a href="mailto:cojonudo14@hotmail.com">Hiram Chirino</a>
 * @author <a href="mailto:pra@tim.se">Peter Antman</a>
 * @version $Revision: 57198 $
 */

public interface InterceptorMBean extends ServiceMBean
{

   JMSServerInterceptor getInterceptor();

   /**
    * Gets the next interceptor in the chain
    * @param v  Value to assign to nextInterceptor.
    */
   ObjectName getNextInterceptor();

   /**
    * Set the next interceptor in the chain
    * @param v  Value to assign to nextInterceptor
    */
   void setNextInterceptor(ObjectName jbossMQService);

} // JBossMQServiceAdapterMBean
