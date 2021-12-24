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
package org.jboss.varia.scheduler.example;

import java.security.InvalidParameterException;
import java.util.Date;

import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.timer.TimerNotification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;

import org.jboss.varia.scheduler.Schedulable;

/**
 * 
 * A sample SchedulableMBean
 * 
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 *
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author Cameron (camtabor)
 * @version $Revision: 57210 $
 *  
 **/
public class SchedulableMBeanExample
   extends ServiceMBeanSupport
   implements SchedulableMBeanExampleMBean
{
   /**
    * Default (no-args) Constructor
    **/
   public SchedulableMBeanExample()
   {
   }
   
   // -------------------------------------------------------------------------
   // SchedulableExampleMBean Methods
   // -------------------------------------------------------------------------
   
   /**
    * @jmx:managed-operation
    */
   public void hit( Notification lNotification, Date lDate, long lRepetitions, ObjectName lName, String lTest ) {
      log.info( "got hit"
         + ", notification: " + lNotification
         + ", date: " + lDate
         + ", remaining repetitions: " + lRepetitions
         + ", scheduler name: " + lName
         + ", test string: " + lTest
      );
      hitCount++;
   }

  /**
   * @jmx:managed-operation
   */   
   public int getHitCount()
   {
     return hitCount;
   }
   
   private int hitCount = 0;
   
}
