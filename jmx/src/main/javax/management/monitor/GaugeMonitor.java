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

import java.util.Iterator;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.ObjectName;

import org.jboss.mx.util.MonitorRunnable;
import org.jboss.mx.util.ObservedObject;
import org.jboss.mx.util.MonitorCallback;

/**
 * The gauge monitor service.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 *
 */
public class GaugeMonitor
  extends Monitor
  implements GaugeMonitorMBean
{
  // Constants -----------------------------------------------------

  /**
   * The gauge high threshold exceeded has been notified.
   */
  int THRESHOLD_HIGH_EXCEEDED_NOTIFIED = 16;

  /**
   * The gauge low threshold exceeded has been notified.
   */
  int THRESHOLD_LOW_EXCEEDED_NOTIFIED = 32;

  /**
   * The threshold type error has been notified.
   */
  int THRESHOLD_ERROR_NOTIFIED = 64;

  // Attributes ----------------------------------------------------

  /**
   * Difference mode.
   */
  boolean differenceMode = false;

  /**
   * The high threshold.
   */
  Number highThreshold = new Integer(0);

  /**
   * The low threshold.
   */
  Number lowThreshold = new Integer(0);

  /**
   * High Notify.
   */
  boolean notifyHigh = false;

  /**
   * Low Notify.
   */
  boolean notifyLow = false;
   /**
    * The runnable monitor.
    */
   private MonitorRunnable monitorRunnable;

  // Static --------------------------------------------------------

  // Constructors --------------------------------------------------

  /**
   * Default Constructor
   */
  public GaugeMonitor()
  {
  }

  // Public --------------------------------------------------------

  public MBeanNotificationInfo[] getNotificationInfo()
  {
    MBeanNotificationInfo[] result = new MBeanNotificationInfo[1];
    String[] types = new String[]
    {
      MonitorNotification.RUNTIME_ERROR,
      MonitorNotification.OBSERVED_OBJECT_ERROR,
      MonitorNotification.OBSERVED_ATTRIBUTE_ERROR,
      MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR,
      MonitorNotification.THRESHOLD_ERROR,
      MonitorNotification.THRESHOLD_HIGH_VALUE_EXCEEDED,
      MonitorNotification.THRESHOLD_LOW_VALUE_EXCEEDED
    };
    result[0] = new MBeanNotificationInfo(types,
      "javax.management.monitor.MonitorNotification",
      "Notifications sent by the Gauge Monitor Service MBean");
    return result;
  }

  // GaugeMonitorMBean implementation ------------------------------

  public Number getDerivedGauge()
  {
    ObservedObject object = getFirstObservedObject();
    if (object != null)
       return (Number) object.getDerivedGauge();
    else
       return null;
  }

  public Number getDerivedGauge(ObjectName name)
  {
    ObservedObject object = retrieveObservedObject(name);
    if (object != null)
       return (Number) object.getDerivedGauge();
    else
       return null;
  }

  public long getDerivedGaugeTimeStamp()
  {
    ObservedObject object = getFirstObservedObject();
    if (object != null)
       return object.getDerivedGaugeTimeStamp();
    else
       return 0L;
  }

  public long getDerivedGaugeTimeStamp(ObjectName name)
  {
    ObservedObject object = retrieveObservedObject(name);
    if (object != null)
       return object.getDerivedGaugeTimeStamp();
    else
       return 0L;
  }

  public boolean getDifferenceMode()
  {
    return differenceMode;
  }

  public void setDifferenceMode(boolean value)
  {
    differenceMode = value;
  }

  public boolean getNotifyHigh()
  {
    return notifyHigh;
  }

  public void setNotifyHigh(boolean value)
  {
    notifyHigh = value;
  }

  public boolean getNotifyLow()
  {
    return notifyLow;
  }

  public void setNotifyLow(boolean value)
  {
    notifyLow = value;
  }

  public Number getHighThreshold()
  {
    return highThreshold;
  }

  public Number getLowThreshold()
  {
    return lowThreshold;
  }

  public void setThresholds(Number highValue, Number lowValue)
    throws IllegalArgumentException
  {
    if (highValue == null)
      throw new IllegalArgumentException("Null high threshold");
    if (lowValue == null)
      throw new IllegalArgumentException("Null low threshold");
    if (highValue.getClass() != lowValue.getClass())
      throw new IllegalArgumentException("High and low different types");
    if (highValue.doubleValue() < lowValue.doubleValue())
      throw new IllegalArgumentException("High less than low threshold");
    highThreshold = highValue;
    lowThreshold = lowValue;
  }

   public synchronized void start()
   {
      // Ignore if already active
      if (active)
         return;
      active = true;

      for (Iterator i = retrieveObservedObjects().values().iterator(); i.hasNext();)
      {
         ObservedObject object = (ObservedObject) i.next();
         initObservedObject(object);
      }

      // Start the monitor runnable
      MonitorCallback callback = new MonitorCallback()
      {
         public void monitorCallback(ObservedObject object,
            MBeanAttributeInfo attributeInfo, Object value) throws Exception
         {
            monitor(object, attributeInfo, value);
         }
         public MonitorNotification createNotification(String type, Object source, 
               long timeStamp, String message, Object derivedGauge,
               String observedAttribute, ObjectName observedObject,
               Object trigger)
         {
            return new MonitorNotification(type, source, nextSeqNo(), timeStamp,
               message, derivedGauge, observedAttribute, observedObject,
               trigger);
         }
      };
      monitorRunnable = new MonitorRunnable(this, objectName, callback,
         observedObjects, server);
   }

   public synchronized void stop()
   {
      // Ignore if not active
      if (!active)
         return;

      // Stop the monitor runnable
      active = false;
      monitorRunnable.setScheduler(null);
      monitorRunnable = null;
   }

  // Package protected ---------------------------------------------

  void initObservedObject(ObservedObject object)
  {
     super.initObservedObject(object);
     object.setDerivedGauge(new Integer(0));
     object.setLastValue(null);
  }

  // REVIEW: This works but needs tidying up!
  void monitor(ObservedObject object, MBeanAttributeInfo attributeInfo, Object value)
    throws Exception
  {
    // Wrong type of attribute
    if (!(value instanceof Number))
    {
       sendAttributeTypeErrorNotification(object, "Attribute is not a number");
       return;
    }

    // Wrong threshold types
    if (highThreshold.getClass() != value.getClass()
        || lowThreshold.getClass() != value.getClass())
    {
       sendThresholdErrorNotification(object, value);
       return;
    }

    // Cast the gauge to a Number
    Number number = (Number) value;
    Number lastValue = (Number) object.getLastValue();
    Number derivedGauge = (Number) object.getDerivedGauge();

    // Get the gauge and record when we got it.
    if (differenceMode)
    {
      if (lastValue == null)
        derivedGauge = getZero(number);
      else
        derivedGauge = sub(number, lastValue);
    }
    else
      derivedGauge = number;
    object.setDerivedGauge(derivedGauge);
    object.setDerivedGaugeTimeStamp(System.currentTimeMillis());

    // Fire the event if the low threshold has been exceeded
    if (derivedGauge.doubleValue() <= lowThreshold.doubleValue())
    {
      if (object.notAlreadyNotified(THRESHOLD_LOW_EXCEEDED_NOTIFIED))
      {
        // Reset high threshold
        object.setNotAlreadyNotified(THRESHOLD_HIGH_EXCEEDED_NOTIFIED);

        // Send the notification once
        sendThresholdLowExceededNotification(object, derivedGauge);
      }
    }

    // Fire the event if the high threshold has been exceeded
    if (derivedGauge.doubleValue() >= highThreshold.doubleValue())
    {
      if (object.notAlreadyNotified(THRESHOLD_HIGH_EXCEEDED_NOTIFIED))
      {
        // Reset low threshold
        object.setNotAlreadyNotified(THRESHOLD_LOW_EXCEEDED_NOTIFIED);

        // Send the notification once
        sendThresholdHighExceededNotification(object, derivedGauge);
      }
    }

    // Remember the last value
    object.setLastValue(number);
  }

  /**
   * Get zero for the type passed.
   * 
   * @param value reference object
   * @return zero for the correct type
   */
  Number getZero(Number value)
  {
     if (value instanceof Byte)
       return new Byte((byte) 0);
     if (value instanceof Integer)
       return new Integer(0);
     if (value instanceof Long)
       return new Long(0);
     if (value instanceof Short)
       return new Short((short) 0);
     if (value instanceof Float)
       return new Float(0);
     return new Double(0);
  }

  /**
   * Subtract two numbers.
   * @param value1 the first value.
   * @param value2 the second value.
   * @return value1 - value2 of the correct type
   */
  Number sub(Number value1, Number value2)
  {
     if (value1 instanceof Byte)
       return new Byte((byte) (value1.byteValue() - value2.byteValue()));
     if (value1 instanceof Integer)
       return new Integer(value1.intValue() - value2.intValue());
     if (value1 instanceof Long)
       return new Long((value1.longValue() - value2.longValue()));
     if (value1 instanceof Short)
       return new Short((short) (value1.shortValue() - value2.shortValue()));
     if (value1 instanceof Float)
       return new Float((value1.floatValue() - value2.floatValue()));
     return new Double(value1.doubleValue() - value2.doubleValue());
  }

  /**
   * Send a threshold low exceeded event.<p>
   *
   * This is only performed when requested and it has not already been sent.
   *
   * @param value the attribute value.
   */
  void sendThresholdLowExceededNotification(ObservedObject object, Object value)
  {
    if (notifyLow)
    {
      sendNotification(object, MonitorNotification.THRESHOLD_LOW_VALUE_EXCEEDED,
        object.getDerivedGaugeTimeStamp(), "low threshold exceeded", observedAttribute, 
        value, lowThreshold);
    }
  }

  /**
   * Send a high threshold exceeded event.<p>
   *
   * This is only performed when requested and it has not already been sent.
   *
   * @param value the attribute value.
   */
  void sendThresholdHighExceededNotification(ObservedObject object, Object value)
  {
    if (notifyHigh)
    {
      sendNotification(object, MonitorNotification.THRESHOLD_HIGH_VALUE_EXCEEDED,
        object.getDerivedGaugeTimeStamp(), "high threshold exceeded", observedAttribute, 
        value, highThreshold);
    }
  }

  /**
   * Send a threshold error event.<p>
   *
   * This is only performed when requested and it has not already been sent.
   *
   * @param value the attribute value.
   */
  void sendThresholdErrorNotification(ObservedObject object, Object value)
  {
    if (object.notAlreadyNotified(THRESHOLD_ERROR_NOTIFIED))
      sendNotification(object, MonitorNotification.THRESHOLD_ERROR,
        object.getDerivedGaugeTimeStamp(), 
        "High or Low Threshold not the correct type", 
        observedAttribute, null, null);
  }

  // Protected -----------------------------------------------------

  // Private -------------------------------------------------------

  // Inner classes -------------------------------------------------
}
