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
 * The counter monitor service MBean interface. <p>
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 */
public interface CounterMonitorMBean
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
    * Retrieves the threshold.
    *
    * @return the threshold.
    * @deprecated use {@link #getThreshold(ObjectName)}
    */
   public Number getThreshold();

   /**
    * Sets the threshold.
    *
    * @param value the threshold value, pass zero for no threshold.
    * @throws IllegalArgumentException when the threshold is null or
    *                                  less than zero.
    * @deprecated use {@link #setInitThreshold(Number)}
    */
   public void setThreshold(Number value)
           throws IllegalArgumentException;

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
    * Retrieves the threshold.
    *
    * @param name the object name of the mbean.
    * @return the threshold.
    */
   public Number getThreshold(ObjectName name);

   /**
    * Retrieves the initial threshold.
    *
    * @return the initial threshold.
    */
   public Number getInitThreshold();

   /**
    * Sets the initial threshold.
    *
    * @param value the initial threshold.
    */
   public void setInitThreshold(Number value)
           throws IllegalArgumentException;

   /**
    * Retrieves the offset.
    *
    * @return the offset value, zero means no offset.
    */
   public Number getOffset();

   /**
    * Sets the offset.
    *
    * @param value the offset value, pass zero for no offset.
    * @throws IllegalArgumentException when the offset is null or
    *                                  less than zero.
    */
   public void setOffset(Number value)
           throws IllegalArgumentException;

   /**
    * Retrieves the modulus.
    *
    * @return the modulus value, zero means no modulus.
    */
   public Number getModulus();

   /**
    * Sets the modulus.
    *
    * @param value the modulus value, pass zero for no modulus.
    * @throws IllegalArgumentException when the modulus is null or
    *                                  less than zero.
    */
   public void setModulus(Number value)
           throws IllegalArgumentException;

   /**
    * Retrieves the notify on/off switch.
    *
    * @return true if notifications occur, false otherwise.
    */
   public boolean getNotify();

   /**
    * Sets the notify on/off switch.
    *
    * @param value pass true notifications, false otherwise.
    */
   public void setNotify(boolean value);

   /**
    * Retrieves the difference mode flag.
    *
    * @return true when in difference mode, false otherwise.
    */
   public boolean getDifferenceMode();

   /**
    * Sets the difference mode flag.
    *
    * @param value pass true for difference mode, false otherwise.
    */
   public void setDifferenceMode(boolean value);

}