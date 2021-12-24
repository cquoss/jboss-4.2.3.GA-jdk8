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
package org.jboss.mq.il;

/**
 * MBean interface.
 */
public interface ServerILJMXServiceMBean extends org.jboss.system.ServiceMBean
{

   /**
    * Get the value of JBossMQService.
    * @return value of JBossMQService.
    */
   javax.management.ObjectName getJBossMQService();

   /**
    * Set the value of JBossMQService.
    * @param v Value to assign to JBossMQService.
    */
   void setInvoker(javax.management.ObjectName jbossMQService);

   void setConnectionFactoryJNDIRef(java.lang.String newConnectionFactoryJNDIRef);

   void setXAConnectionFactoryJNDIRef(java.lang.String newXaConnectionFactoryJNDIRef);

   java.lang.String getConnectionFactoryJNDIRef();

   java.lang.String getXAConnectionFactoryJNDIRef();

   long getPingPeriod();

   void setPingPeriod(long period);

   /**
    * Get the client id for this connection factory
    * @return the client id    */
   java.lang.String getClientID();

   /**
    * Set the client id for this connection factory
    * @param clientID the client id    */
   void setClientID(java.lang.String clientID);

}
