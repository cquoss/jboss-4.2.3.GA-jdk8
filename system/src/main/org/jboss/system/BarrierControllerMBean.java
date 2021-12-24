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
package org.jboss.system;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;

/**
 * BarrierController MBean interface.
 * 
 * @see BarrierController
 * 
 * @author <a href="dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 59201 $
 */
public interface BarrierControllerMBean extends ListenerServiceMBean
{
   // The default object name
   static final ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss:service=BarrierController");

   // Attributes ----------------------------------------------------
   
   /** The controlled barrier StateString. */
   String getBarrierStateString();
   
   /** The controlled barrier ObjectName. */
   void setBarrierObjectName(ObjectName barrierName);   
   ObjectName getBarrierObjectName();
   
   /** Whether the Barrier should be created on startup */
   void setBarrierCreatedOnStartup(Boolean createdOnStartup);
   Boolean getBarrierCreatedOnStartup();
   
   /** Whether the Barrier should be started on startup. */
   void setBarrierEnabledOnStartup(Boolean startedOnStartup);
   Boolean getBarrierEnabledOnStartup();
   
   /** The notification subscription handback string that creates the barrier. */
   void setCreateBarrierHandback(String createHandback);
   String getCreateBarrierHandback();
   
   /** The notification subscription handback string that starts the barrier. */
   void setStartBarrierHandback(String startHandback);
   String getStartBarrierHandback();

   /** The notification subscription handback string that stops the barrier. */
   void setStopBarrierHandback(String stopHandback);
   String getStopBarrierHandback();
   
   /** The notification subscription handback string that destroys the barrier. */
   void setDestroyBarrierHandback(String destroyHandback);
   String getDestroyBarrierHandback();   

   /** The ability to dynamically subscribe for notifications. */
   void setDynamicSubscriptions(Boolean dynamicSubscriptions);
   Boolean getDynamicSubscriptions();

   // Operations ----------------------------------------------------
   
   /**
    * Manually create the controlled barrier
    */
   void createBarrier();
   
   /**
    * Manually start the controlled barrier
    */
   void startBarrier();

   /**
    * Manually stop the controlled barrier
    */
   void stopBarrier();

   /**
    * Manually destroy the controlled barrier
    */
   void destroyBarrier();
   
}
