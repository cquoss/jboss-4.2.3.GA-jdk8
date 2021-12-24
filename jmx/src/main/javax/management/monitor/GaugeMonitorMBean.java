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
 * The gauge monitor service MBean interface. <p>
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 */
public interface GaugeMonitorMBean
        extends MonitorMBean
{
   // Constants -----------------------------------------------------

   // Static --------------------------------------------------------

   // Public --------------------------------------------------------

   /**
    * Retrieves the derived gauge.
    *
    * @return the derived gauge.
    * @deprecated use {@link #getDerivedGauge(ObjectName)}
    */
   public Number getDerivedGauge();

   /**
    * Retrieves the derived gauge timestamp.
    *
    * @return the derived gauge timestamp.
    * @deprecated use {@link #getDerivedGaugeTimeStamp(ObjectName)}
    */
   public long getDerivedGaugeTimeStamp();

   /**
    * Retrieves the derived gauge.
    *
    * @param name the object name of the mbean.
    * @return the derived gauge.
    */
   public Number getDerivedGauge(ObjectName name);

   /**
    * Retrieves the derived gauge timestamp.
    *
    * @param name the object name of the mbean.
    * @return the derived gauge timestamp.
    */
   public long getDerivedGaugeTimeStamp(ObjectName name);

   /**
    * Retrieves the high threshold.
    * REVIEW: zero threshold
    *
    * @return the high threshold value, zero means no threshold.
    */
   public Number getHighThreshold();

   /**
    * Retrieves the low threshold.
    * REVIEW: zero threshold
    *
    * @return the low threshold value, zero means no threshold.
    */
   public Number getLowThreshold();

   /**
    * Sets the high and low threshold.
    * REVIEW: zero threshold
    *
    * @param highValue the high threshold value, pass zero for no high
    *                  threshold.
    * @param lowValue  the low threshold value, pass zero for no low
    *                  threshold.
    * @throws IllegalArgumentException when either threshold is null or
    *                                  the high threshold is lower than the low threshold or the.
    *                                  thresholds have different types.
    */
   public void setThresholds(Number highValue, Number lowValue)
           throws IllegalArgumentException;

   /**
    * Retrieves the high notify on/off switch.
    *
    * @return true if high notifications occur, false otherwise.
    */
   public boolean getNotifyHigh();

   /**
    * Sets the high notify on/off switch.
    *
    * @param value pass true for high notifications, false otherwise.
    */
   public void setNotifyHigh(boolean value);

   /**
    * Retrieves the low notify on/off switch.
    *
    * @return true if low notifications occur, false otherwise.
    */
   public boolean getNotifyLow();

   /**
    * Sets the low notify on/off switch.
    *
    * @param value pass true for low notifications, false otherwise.
    */
   public void setNotifyLow(boolean value);

   /**
    * Retrieves the difference mode flag.
    *
    * @return true when in difference mode, false otherwise.
    */
   public boolean getDifferenceMode();

   /**
    * Sets the difference mode flag.
    */
   public void setDifferenceMode(boolean value);
}