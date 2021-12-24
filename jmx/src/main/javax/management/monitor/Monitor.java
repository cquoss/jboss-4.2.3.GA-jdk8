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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.jboss.mx.util.ObservedObject;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;

/**
 * The monitor service.
 *
 * <p><b>Revisions:</b>
 * <p><b>20020319 Adrian Brock:</b>
 * <ul>
 * <li>Notify using the object name and fix the notification payloads
 * </ul>
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 */
public abstract class Monitor
   extends NotificationBroadcasterSupport
   implements MonitorMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------

   /**
    * The amount by which arrays are incremented.
    */
   protected static final int capacityIncrement = 16;

   /**
    * Used to reset errors in {@link #alreadyNotified}.
    */
   protected static final int RESET_FLAGS_ALREADY_NOTIFIED = 0;

   /**
    * An observed attribute type error has been notified.
    */
   protected static final int RUNTIME_ERROR_NOTIFIED = 8;

   /**
    * An observed object error has been notified.
    */
   protected static final int OBSERVED_OBJECT_ERROR_NOTIFIED = 1;

   /**
    * An observed attribute error has been notified.
    */
   protected static final int OBSERVED_ATTRIBUTE_ERROR_NOTIFIED = 2;

   /**
    * An observed attribute type error has been notified.
    */
   protected static final int OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED = 4;

   // Attributes ----------------------------------------------------

   /**
    * The number of valid elements in the arrays.
    */
   protected int elementCount = 0;

   /**
    * The granularity period.
    */
   long granularityPeriod = 10000;

   /**
    * The observed attribute.
    */
   String observedAttribute = null;

   /**
    * The observed objects.
    */
   ConcurrentHashMap observedObjects = new ConcurrentHashMap();

   /**
    * Whether the service is active.
    */
   boolean active = false;

   /**
    * The server this service is registered in.
    */
   protected MBeanServer server;

   /**
    * The object name of this monitor.
    */
   ObjectName objectName;

   /**
    * The errors that have already been notified.
    * REVIEW: Check
    * @deprecated use {@link #alreadyNotifieds}[0]
    */
   protected int alreadyNotified = 0;

   /**
    * The errors that have already been notified.
    */
   protected int[] alreadyNotifieds = new int[0];

   /** @deprecated No replacement. */
   protected String dbgTag = null;

   /**
    * The notification sequence number.
    */
   private long sequenceNumber;
  
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // MonitorMBean implementation -----------------------------------

   public long getGranularityPeriod()
   {
      return granularityPeriod;
   }

   public String getObservedAttribute()
   {
      return observedAttribute;
   }

   public ObjectName getObservedObject()
   {
      ObservedObject object = getFirstObservedObject();
      if (object != null)
         return object.getObjectName();
      else
         return null;
   }

   public ObjectName[] getObservedObjects()
   {
      Set set = new HashSet(observedObjects.values());
      elementCount = set.size();
      ObjectName[] result = new ObjectName[set.size()];
      alreadyNotifieds = new int[set.size()];
      int count = 0;
      for (Iterator i = set.iterator(); i.hasNext();)
      {
         ObservedObject object = (ObservedObject) i.next();
         result[count] = object.getObjectName();
         alreadyNotifieds[count++] = object.getAlreadyNotified();
      }
      return result;
   }

   public synchronized boolean isActive()
   {
      return active;
   }

   public void setGranularityPeriod(long period)
      throws IllegalArgumentException
   {
      if (period <= 0)
         throw new IllegalArgumentException("Period must be positive.");
      granularityPeriod = period;
   }

   public void setObservedAttribute(String attribute)
      throws IllegalArgumentException
   {
      observedAttribute = attribute;
   }

   public void setObservedObject(ObjectName object)
      throws IllegalArgumentException
   {
      observedObjects.clear();
      addObservedObject(object);
   }

   public void addObservedObject(ObjectName object)
      throws IllegalArgumentException
   {
      if (object == null)
         throw new IllegalArgumentException("null object name");
      ObservedObject o = new ObservedObject(object);
      initObservedObject(o);
      observedObjects.put(object, o);
   }

   public void removeObservedObject(ObjectName object)
   {
      if (object == null)
         throw new IllegalArgumentException("null object name");
      observedObjects.remove(object);
   }

   public boolean containsObservedObject(ObjectName object)
   {
      if (object == null)
         throw new IllegalArgumentException("null object name");
      return observedObjects.containsKey(object);
   }

   public abstract void start();

   public abstract void stop();

   public String toString()
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append(getClass()).append(System.identityHashCode(this)).append(": {");
      buffer.append(" objectName=").append(objectName);
      return buffer.append("}").toString();
   }

   // MBeanRegistrationImplementation overrides ---------------------

   public ObjectName preRegister(MBeanServer server, ObjectName objectName)
      throws Exception
   {
      // Remember the server.
      this.server = server;

      // Remember the object name.
      this.objectName = objectName;

      // Use the passed object name.
      return objectName;
   }

   public void postRegister(Boolean registrationDone)
   {
   }

   public void preDeregister()
      throws Exception
   {
      // Stop the monitor before deregistration.
      stop();
   }

   public void postDeregister()
   {
   }

   // Package protected ---------------------------------------------

   /**
    * Get an observed object
    */
   ObservedObject retrieveObservedObject(ObjectName name)
   {
      return (ObservedObject) observedObjects.get(name);
   }

   /**
    * retrieve the observed objects
    */
   Map retrieveObservedObjects()
   {
      return observedObjects;
   }

   /**
    * retrieve the first observed objects
    */
   ObservedObject getFirstObservedObject()
   {
      Iterator i = observedObjects.values().iterator();
      if (i.hasNext())
         return (ObservedObject) i.next();
      else
         return null;
   }

   void initObservedObject(ObservedObject object)
   {
      object.resetAlreadyNotified();
      object.setDerivedGaugeTimeStamp(System.currentTimeMillis());
   }

   /**
    * Sends the notification
    *
    * @param object the observedObject.
    * @param type the notification type.
    * @param timestamp the time of the notification.
    * @param message the human readable message to send.
    * @param attribute the attribute name.
    * @param gauge the derived gauge.
    * @param trigger the trigger value.
    */
   void sendNotification(ObservedObject object, String type, long timestamp, String message,
      String attribute, Object gauge, Object trigger)
   {
      long seq = nextSeqNo();
      if (timestamp == 0)
         timestamp = System.currentTimeMillis();
      sendNotification(new MonitorNotification(type, objectName, seq,
         timestamp, message, gauge,
         attribute, object.getObjectName(), trigger));
   }

   /**
    * Send a runtime error notification.
    *
    * @param object the observedObject.
    * @param message the human readable message to send.
    */
   void sendRuntimeErrorNotification(ObservedObject object, String message)
   {
      if (object.notAlreadyNotified(RUNTIME_ERROR_NOTIFIED))
         sendNotification(object, MonitorNotification.RUNTIME_ERROR, 0,
            message, observedAttribute, null, null);
   }

   /**
    * Send an object error notification.
    *
    * @param object the observedObject.
    * @param message the human readable message to send.
    */
   void sendObjectErrorNotification(ObservedObject object, String message)
   {
      if (object.notAlreadyNotified(OBSERVED_OBJECT_ERROR_NOTIFIED))
         sendNotification(object, MonitorNotification.OBSERVED_OBJECT_ERROR, 0,
            message, observedAttribute, null, null);
   }

   /**
    * Send an attribute error notification.
    *
    * @param object the observedObject.
    * @param message the human readable message to send.
    */
   void sendAttributeErrorNotification(ObservedObject object, String message)
   {
      if (object.notAlreadyNotified(OBSERVED_ATTRIBUTE_ERROR_NOTIFIED))
         sendNotification(object, MonitorNotification.OBSERVED_ATTRIBUTE_ERROR, 0,
            message, observedAttribute, null, null);
   }

   /**
    * Send an attribute type error notification.
    *
    * @param object the observedObject.
    * @param message the human readable message to send.
    */
   void sendAttributeTypeErrorNotification(ObservedObject object, String message)
   {
      if (object.notAlreadyNotified(OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED))
         sendNotification(object, MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR, 0,
            message, observedAttribute, null, null);
   }

   /**
    * Reset the already notifieds
    */
   void resetAlreadyNotified()
   {
      for (Iterator i = observedObjects.values().iterator(); i.hasNext();)
         ((ObservedObject) i.next()).resetAlreadyNotified();
   }

   synchronized long nextSeqNo()
   {
      long nextSeqNo = sequenceNumber ++;
      return nextSeqNo;
   }
   
   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}
