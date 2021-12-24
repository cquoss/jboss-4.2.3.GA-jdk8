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
package javax.management.monitor;

import javax.management.ObjectName;

/**
 * The monitor service MBean interface. <p>
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 */
public interface MonitorMBean
{
   // Constants -----------------------------------------------------

   // Static --------------------------------------------------------

   // Public --------------------------------------------------------

   /**
    * Starts the monitor.
    */
   public void start();

   /**
    * Stops the monitor.
    */
   public void stop();

   /**
    * Add the object name of the MBean monitored.<p>
    *
    * @param object the object name.
    */
   public void addObservedObject(ObjectName object)
           throws IllegalArgumentException;

   /**
    * Remove the object name of the MBean monitored.<p>
    *
    * @param object the object name.
    */
   public void removeObservedObject(ObjectName object);

   /**
    * Checks whether the object name is monitored.<p>
    *
    * @param object the object name.
    * @return true when it is monitored.
    */
   public boolean containsObservedObject(ObjectName object);

   /**
    * Retrieves the object names of the MBeans monitored.
    *
    * @return the object names.
    */
   public ObjectName[] getObservedObjects();

   /**
    * Retrieves the object name of the MBean monitored.
    *
    * @return the object name.
    * @deprecated use {@link #getObservedObjects()}
    */
   public ObjectName getObservedObject();

   /**
    * Sets the object name of the MBean monitored.<p>
    * <p/>
    * The default value is null.
    *
    * @param object the object name.
    * @deprecated use {@link #addObservedObject(ObjectName)}
    */
   public void setObservedObject(ObjectName object);

   /**
    * Retrieves the name of the attribute monitored.
    *
    * @return the attribute monitored.
    */
   public String getObservedAttribute();

   /**
    * Sets the name of the attribute monitored.<p>
    * <p/>
    * The default value is null.
    *
    * @param attribute the attribute monitored.
    * @throws IllegalArgumentException when the period is not positive.
    */
   public void setObservedAttribute(String attribute);

   /**
    * Retrieves the granularity period in milliseconds.<p>
    * <p/>
    * The monitoring takes place once per granularity period.
    *
    * @return the granularity period.
    */
   public long getGranularityPeriod();

   /**
    * Sets the granularity period in milliseconds.<p>
    * <p/>
    * The monitoring takes place once per granularity period.<p>
    * <p/>
    * The default value is 10 seconds.
    *
    * @param period the granularity period.
    * @throws IllegalArgumentException when the period is not positive.
    */
   public void setGranularityPeriod(long period)
           throws IllegalArgumentException;

   /**
    * Tests whether this monitoring service is active.
    *
    * @return true when the service is active, false otherwise.
    */
   public boolean isActive();

}
