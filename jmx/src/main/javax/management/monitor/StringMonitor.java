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
 * The string monitor service.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 *
 */
public class StringMonitor
  extends Monitor
  implements StringMonitorMBean
{
  // Constants -----------------------------------------------------

  /**
   * The string match has been notified.
   */
  int STRING_MATCHES_NOTIFIED = 16;

  /**
   * The string differs has been notified.
   */
  int STRING_DIFFERS_NOTIFIED = 32;

  // Attributes ----------------------------------------------------

  /**
   * The comparison string.
   */
  String stringToCompare = new String();

  /**
   * Notify Matches.
   */
  boolean notifyMatch = false;

  /**
   * Notify Differs.
   */
  boolean notifyDiffer = false;
   /**
    * The runnable monitor.
    */
   private MonitorRunnable monitorRunnable;

  // Static --------------------------------------------------------

  // Constructors --------------------------------------------------

  /**
   * Default Constructor
   */
  public StringMonitor()
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
      MonitorNotification.STRING_TO_COMPARE_VALUE_MATCHED,
      MonitorNotification.STRING_TO_COMPARE_VALUE_DIFFERED
    };
    result[0] = new MBeanNotificationInfo(types,
      "javax.management.monitor.MonitorNotification",
      "Notifications sent by the String Monitor Service MBean");
    return result;
  }

  // StringMonitorMBean implementation -----------------------------

  public String getDerivedGauge()
  {
    ObservedObject object = getFirstObservedObject();
    if (object != null)
       return (String) object.getDerivedGauge();
    else
       return null;
  }

  public String getDerivedGauge(ObjectName name)
  {
     ObservedObject object = retrieveObservedObject(name);
    if (object != null)
       return (String) object.getDerivedGauge();
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

  public String getStringToCompare()
  {
    return stringToCompare;
  }

  public void setStringToCompare(String value)
    throws IllegalArgumentException
  {
    if (value == null)
      throw new IllegalArgumentException("Null string to compare.");
    this.stringToCompare = value;
  }

  public boolean getNotifyMatch()
  {
    return notifyMatch;
  }

  public void setNotifyMatch(boolean value)
  {
    notifyMatch = value;
  }

  public boolean getNotifyDiffer()
  {
    return notifyDiffer;
  }

  public void setNotifyDiffer(boolean value)
  {
    notifyDiffer = value;
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
     object.setDerivedGauge(new String());
  }

  void monitor(ObservedObject object, MBeanAttributeInfo attributeInfo, Object value)
    throws Exception
  {
    // Must be a string.
    if (!(value instanceof String))
    {
      sendAttributeTypeErrorNotification(object, "Not a string attribute");
      return;
    }

    // Get the gauge and record when we got it.
    object.setDerivedGauge(value);
    object.setDerivedGaugeTimeStamp(System.currentTimeMillis());

    // Try to fire the relevant event.
    if (value.equals(stringToCompare))
      sendStringMatchesNotification(object, (String) value);
    else
      sendStringDiffersNotification(object, (String) value);
  }

  /**
   * Send an string matches notification.<p>
   *
   * This is only performed when requested and it has not already been sent.
   *
   * @param value the attribute value.
   */
  void sendStringMatchesNotification(ObservedObject object, String value)
  {
    if (notifyMatch && object.notAlreadyNotified(STRING_MATCHES_NOTIFIED))
      sendNotification(object, MonitorNotification.STRING_TO_COMPARE_VALUE_MATCHED,
        object.getDerivedGaugeTimeStamp(), "matches", observedAttribute, value, 
        stringToCompare);
    object.setNotAlreadyNotified(STRING_DIFFERS_NOTIFIED);
  }

  /**
   * Send an string differs notification.<p>
   *
   * This is only performed when requested and it has not already been sent.
   *
   * @param value the attribute value.
   */
  void sendStringDiffersNotification(ObservedObject object, String value)
  {
    if (notifyDiffer && object.notAlreadyNotified(STRING_DIFFERS_NOTIFIED))
      sendNotification(object, MonitorNotification.STRING_TO_COMPARE_VALUE_DIFFERED,
        object.getDerivedGaugeTimeStamp(), "differs", observedAttribute, value, 
        stringToCompare);
    object.setNotAlreadyNotified(STRING_MATCHES_NOTIFIED);
  }

  // Protected -----------------------------------------------------

  // Private -------------------------------------------------------

  // Inner classes -------------------------------------------------
}
