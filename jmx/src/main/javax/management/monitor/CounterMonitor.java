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
 * The counter monitor service.
 *
 * <p><b>Revisions:</b>
 * <p><b>20020319 Adrian Brock:</b>
 * <ul>
 * <li>Reset the threshold when the value becomes negative in difference mode
 * </ul>
 * <p><b>20020326 Adrian Brock:</b>
 * <ul>
 * <li>The spec says the modulus should be *strictly* exceeded. It appears
 * from testing the RI, it is a mathematical definition of modulus. e.g.
 * 10 exceeds a modulus of 10
 * </ul>
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 *
 */
public class CounterMonitor extends Monitor implements CounterMonitorMBean
{
   // Constants -----------------------------------------------------

   /**
    * The counter threshold exceeded has been notified.
    */
   int THRESHOLD_EXCEEDED_NOTIFIED = 16;

   /**
    * The threshold type error has been notified.
    */
   int THRESHOLD_ERROR_NOTIFIED = 32;

   // Attributes ----------------------------------------------------

   /**
    * The offset.
    */
   Number offset = new Integer(0);

   /**
    * The modulus.
    */
   Number modulus = new Integer(0);

   /**
    * The last stated threshold.
    */
   Number initThreshold = new Integer(0);

   /**
    * Difference mode.
    */
   boolean differenceMode = false;

   /**
    * Notify.
    */
   boolean notify = false;
   /**
    * The runnable monitor.
    */
   private MonitorRunnable monitorRunnable;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Default Constructor
    */
   public CounterMonitor()
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
         MonitorNotification.THRESHOLD_VALUE_EXCEEDED
      };
      result[0] = new MBeanNotificationInfo(types,
         "javax.management.monitor.MonitorNotification",
         "Notifications sent by the Counter Monitor Service MBean");
      return result;
   }

   // CounterMonitorMBean implementation ----------------------------

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

   public Number getInitThreshold()
   {
      return initThreshold;
   }

   public void setInitThreshold(Number threshold)
      throws IllegalArgumentException
   {
      if (threshold == null)
         throw new IllegalArgumentException("Null threshold");
      if (threshold.longValue() < 0)
         throw new IllegalArgumentException("Negative threshold");
      initThreshold = threshold;
   }

   public Number getModulus()
   {
      return modulus;
   }

   public void setModulus(Number value)
      throws IllegalArgumentException
   {
      if (value == null)
         throw new IllegalArgumentException("Null modulus");
      if (value.longValue() < 0)
         throw new IllegalArgumentException("Negative modulus");
      modulus = value;
   }

   public boolean getNotify()
   {
      return notify;
   }

   public void setNotify(boolean value)
   {
      notify = value;
   }

   public Number getOffset()
   {
      return offset;
   }

   public void setOffset(Number value)
      throws IllegalArgumentException
   {
      if (value == null)
         throw new IllegalArgumentException("Null offset");
      if (value.longValue() < 0)
         throw new IllegalArgumentException("Negative offset");
      offset = value;
   }

   public Number getThreshold()
   {
      ObservedObject object = getFirstObservedObject();
      if (object != null)
         return (Number) object.getThreshold();
      else
         return null;
   }

   public Number getThreshold(ObjectName name)
   {
      ObservedObject object = retrieveObservedObject(name);
      if (object != null)
         return (Number) object.getThreshold();
      else
         return null;
   }

   public void setThreshold(Number value)
      throws IllegalArgumentException
   {
      setInitThreshold(value);
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
      object.setThreshold(initThreshold);
      object.setLastValue(null);
   }

   // REVIEW: This works but needs tidying up!
   void monitor(ObservedObject object, MBeanAttributeInfo attributeInfo, Object value)
      throws Exception
   {
      // Wrong type of attribute
      if (!(value instanceof Byte) && !(value instanceof Integer) &&
         !(value instanceof Short) && !(value instanceof Long))
      {
         sendAttributeTypeErrorNotification(object, "Attribute is not an integer type");
         return;
      }

      Number threshold = (Number) object.getThreshold();

      // Wrong threshold types
      if (threshold.getClass() != value.getClass()
         || offset.longValue() != 0 && offset.getClass() != value.getClass()
         || modulus.longValue() != 0 && modulus.getClass() != value.getClass())
      {
         sendThresholdErrorNotification(object, value);
         return;
      }

      // Cast the counter to a Number
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
         if (derivedGauge.longValue() < 0 && modulus.longValue() != 0)
         {
            derivedGauge = add(derivedGauge, modulus);
            threshold = initThreshold;
            object.setThreshold(threshold);
            object.setNotAlreadyNotified(THRESHOLD_EXCEEDED_NOTIFIED);
         }
      }
      else
         derivedGauge = number;

      object.setDerivedGauge(derivedGauge);
      object.setDerivedGaugeTimeStamp(System.currentTimeMillis());

      // Have we wrapped?
      if (lastValue != null && modulus.longValue() != 0 && offset.longValue() != 0 
            && derivedGauge.longValue() < lastValue.longValue())
      {
         object.setNotAlreadyNotified(THRESHOLD_EXCEEDED_NOTIFIED);
      }
      
      // Fire the event if the threshold has been exceeded
      if (derivedGauge.longValue() >= threshold.longValue())
      {
         if (object.notAlreadyNotified(THRESHOLD_EXCEEDED_NOTIFIED))
         {
            sendThresholdExceededNotification(object, derivedGauge);

            // Add any offsets required to get a new threshold
            if (offset.longValue() != 0)
            {
               while (threshold.longValue() <= derivedGauge.longValue())
                  threshold = add(threshold, offset);
               object.setThreshold(threshold);
               object.setNotAlreadyNotified(THRESHOLD_EXCEEDED_NOTIFIED);
            }
         }
      }
      else
      {
         // Reset notfication when it becomes less than threshold
         if (derivedGauge.longValue() < threshold.longValue()
            && offset.longValue() == 0)
            object.setNotAlreadyNotified(THRESHOLD_EXCEEDED_NOTIFIED);
      }

      // Restart when modulus exceeded
      if (modulus.longValue() != 0 && number.longValue() >= modulus.longValue())
      {
         object.setThreshold(initThreshold);
         object.setAlreadyNotified(THRESHOLD_EXCEEDED_NOTIFIED);
      }

      // Remember the last value
      object.setLastValue(number);
    }

   /**
    * Get zero for the type passed.
    * 
    * @param value - the reference object
    * @return zero for the correct type
    */
   Number getZero(Number value)
   {
      if (value instanceof Byte)
         return new Byte((byte) 0);
      if (value instanceof Integer)
         return new Integer(0);
      if (value instanceof Short)
         return new Short((short) 0);
      return new Long(0);
   }

   /**
    * Add two numbers together.
    * @param value1 the first value.
    * @param value2 the second value.
    * @return value1 + value2 of the correct type
    */
   Number add(Number value1, Number value2)
   {
      if (value1 instanceof Byte)
         return new Byte((byte) (value1.byteValue() + value2.byteValue()));
      if (value1 instanceof Integer)
         return new Integer(value1.intValue() + value2.intValue());
      if (value1 instanceof Short)
         return new Short((short) (value1.shortValue() + value2.shortValue()));
      return new Long(value1.longValue() + value2.longValue());
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
      if (value1 instanceof Short)
         return new Short((short) (value1.shortValue() - value2.shortValue()));
      return new Long(value1.longValue() - value2.longValue());
   }

   /**
    * Send a threshold exceeded event.<p>
    *
    * This is only performed when requested and it has not already been sent.
    *
    * @param value the attribute value.
    */
   void sendThresholdExceededNotification(ObservedObject object, Object value)
   {
      if (notify)
      {
         sendNotification(object, MonitorNotification.THRESHOLD_VALUE_EXCEEDED,
            object.getDerivedGaugeTimeStamp(), "threshold exceeded", observedAttribute, value,
            object.getThreshold());
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
            "Threshold, offset or modulus not the correct type",
            observedAttribute, null, null);
   }

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
