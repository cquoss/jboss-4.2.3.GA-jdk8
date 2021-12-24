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
 * The string monitor service MBean interface. <p>
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 *
 */
public interface StringMonitorMBean
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
  public String getDerivedGauge();

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
  public String getDerivedGauge(ObjectName name);

  /**
   * Retrieves the derived gauge timestamp.
   *
   * @param name the object name of the mbean.
   * @return the derived gauge timestamp.
   */
  public long getDerivedGaugeTimeStamp(ObjectName name);

  /**
   * Retrieves the string to compare with the observed attribute.
   *
   * @return the comparison string.
   */
  public String getStringToCompare();

  /**
   * Sets the string to compare with the observed attribute.
   *
   * @param value the comparison string.
   * @exception IllegalArgumentException when specified string is null.
   */
  public void setStringToCompare(String value)
    throws IllegalArgumentException;

  /**
   * Retrieves the matching on/off switch.
   *
   * @return true if the notification occurs when the string matches, false
   *         otherwise.
   */
  public boolean getNotifyMatch();

  /**
   * Sets the matching on/off switch.
   *
   * @param value pass true for a notification when the string matches, false
   *        otherwise.
   */
  public void setNotifyMatch(boolean value);

  /**
   * Retrieves the differs on/off switch.
   *
   * @return true if the notification occurs when the string differs, false
   *         otherwise.
   */
  public boolean getNotifyDiffer();

  /**
   * Sets the differs on/off switch.
   *
   * @param value pass true for a notification when the string differs, false
   *        otherwise.
   */
  public void setNotifyDiffer(boolean value);
}