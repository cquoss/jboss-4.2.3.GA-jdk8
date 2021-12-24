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
package org.jboss.varia.scheduler;

import javax.management.ObjectName;

import org.jboss.system.ServiceMBean;

/**
 * AbstractScheduleProvider MBean interface
 * 
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author <a href="mailto:ivelin@apache.org">Ivelin Ivanov</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 57210 $
 */
public interface AbstractScheduleProviderMBean extends ServiceMBean
{
   // Attributes ----------------------------------------------------
   
   /** The Schedule Manager Name */
   void setScheduleManagerName(ObjectName name);   
   ObjectName getScheduleManagerName();
   
   // Operations ----------------------------------------------------
   
   /**
    * Callback from the ScheduleManager indicating that Schedules can be provided
    */
   void startProviding() throws Exception;

   /**
    * Callback from the ScheduleManager indicating that all Schedules should be removed
    */
   void stopProviding();

}