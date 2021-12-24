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
package javax.management;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

/**
 * An implementation of the {@link NotificationFilter} interface.<p>
 *
 * It filters on the notification type. It Maintains a list of enabled
 * notification types. By default no notifications are enabled.<p>
 *
 * The enabled types are prefixes. That is a notification is enabled if
 * it starts with an enabled string.
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>.
 * @version $Revision: 57200 $
 */
public class NotificationFilterSupport
   implements NotificationFilter, Serializable
{
   // Constants ---------------------------------------------------

   private static final long serialVersionUID = 6579080007561786969L;

   // Attributes --------------------------------------------------

   /**
    * Enabled notification types.
    */
   private Vector enabledTypes;

   // Static ------------------------------------------------------

   // Constructors ------------------------------------------------

   /**
    * Create a filter that filters out all notification types.
    */
   public NotificationFilterSupport()
   {
      enabledTypes = new Vector();
   }

   // Public ------------------------------------------------------

   /**
    * Disable all notification types. Rejects all notifications.
    */
   public synchronized void disableAllTypes()
   {
      enabledTypes = new Vector();
   }

   /**
    * Disable a notification type.
    *
    * @param type the notification type to disable.
    */
   public synchronized void disableType(String type)
   {
      // Null won't be in the list anyway.
      enabledTypes.removeElement(type);
   }

   /**
    * Enable a notification type.
    *
    * @param type the notification type to enable.
    * @exception IllegalArgumentException for a null type
    */
   public synchronized void enableType(String type)
      throws IllegalArgumentException
   {
      if (type == null)
         throw new IllegalArgumentException("null notification type");
      if (enabledTypes.contains(type) == false)
         enabledTypes.addElement(type);
   }

   /**
    * Get all the enabled notification types.<p>
    *
    * Returns a vector of enabled notification type.<br>
    * An empty vector means all types disabled.
    *
    * @return the vector of enabled types.
    */
   public synchronized Vector getEnabledTypes()
   {
      return (Vector) enabledTypes.clone();
   }

   /**
    * @return human readable string.
    */
   public String toString()
   {
      StringBuffer sb = new StringBuffer(100);
      
      sb.append(getClass().getName()).append(':');
      sb.append(" enabledTypes=").append(getEnabledTypes());
      
      return sb.toString();
   }
   
   // NotificationFilter implementation ---------------------------

   /**
    * Test to see whether this notification is enabled
    *
    * @param notification the notification to filter
    * @return true when the notification should be sent, false otherwise
    * @exception IllegalArgumentException for null notification.
    */
   public synchronized boolean isNotificationEnabled(Notification notification)
   {
      if (notification == null)
         throw new IllegalArgumentException("null notification");
      // Is it enabled?
      String notificationType = notification.getType();
      for (Enumeration e = enabledTypes.elements(); e.hasMoreElements();)
      {
         String type = (String) e.nextElement();
         if (notificationType.startsWith(type))
            return true;
      }
      return false;
   }

   // Private -----------------------------------------------------
 
}
