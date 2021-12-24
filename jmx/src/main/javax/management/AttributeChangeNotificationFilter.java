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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;

import java.util.Set;
import java.util.HashSet;
import java.util.Vector;


/**
 * Notification filter support for attribute change notifications.
 *
 * @see javax.management.AttributeChangeNotification
 * @see javax.management.NotificationFilter
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 *
 * <p><b>Revisions:</b>
 * <p><b>20020710 Adrian Brock:</b>
 * <ul>
 * <li> Serialization </li>
 * </ul>
 */
public class AttributeChangeNotificationFilter
   implements NotificationFilter, java.io.Serializable
{
   
   // Constants -----------------------------------------------------

   private static final long serialVersionUID = -6347317584796410029L;
   private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]
   {
      new ObjectStreamField("enabledAttributes", Vector.class)
   };
   
   // Attributes ----------------------------------------------------
   private Set attributes = new HashSet();
   
   
   // Constructors --------------------------------------------------
   
   /**
    * Constructs an attribute change notification filter. All attribute
    * notifications are filtered by default. Use {@link #enableAttribute}
    * to enable notifications of a given attribute to pass this filter.
    */
   public AttributeChangeNotificationFilter()
   {
   }

   // NotificationFilter implementation -----------------------------

   public synchronized boolean isNotificationEnabled(Notification notification)
   {
      if (notification != null && notification instanceof AttributeChangeNotification)
      {
         AttributeChangeNotification notif = (AttributeChangeNotification)notification;
         if (attributes.contains(notif.getAttributeName()))
            return true;
      }

      return false;
   }

   // Public --------------------------------------------------------
   
   /**
    * Enables the attribute change notifications of the given attribute to be
    * sent to the listener.
    *
    * @param   name  name of the management attribute
    */
   public synchronized void enableAttribute(String name) throws IllegalArgumentException
   {
      if (name == null)
         throw new IllegalArgumentException("Null attribute name");
      
      attributes.add(name);
   }

   /**
    * Disable the attribute change notifications of the given attribute.
    * Attribute change notifications for this attribute will not be sent to
    * the listener.
    *
    * @param   name name of the management attribute
    */
   public synchronized void disableAttribute(String name)
   {
      attributes.remove(name);
   }

   /**
    * Disables all attribute change notifications.
    */
   public synchronized void disableAllAttributes()
   {
      attributes.clear();
   }

   /**
    * Returns the names of the attributes whose notifications are allowed to
    * pass this filter.
    *
    * @return  a vector containing the name strings of the enabled attributes
    */
   public synchronized Vector getEnabledAttributes()
   {
      return new Vector(attributes);
   }

   /**
    * @return human readable string.
    */
   public String toString()
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append(getClass().getName()).append(":");
      buffer.append(" enabledAttributes=").append(getEnabledAttributes());
      return buffer.toString();
   }

   // Private -------------------------------------------------------

   private void readObject(ObjectInputStream ois)
      throws IOException, ClassNotFoundException
   {
      ObjectInputStream.GetField getField = ois.readFields();
      Vector enabled = (Vector) getField.get("enabledAttributes", null);
      attributes = new HashSet(enabled);
   }

   private void writeObject(ObjectOutputStream oos)
      throws IOException
   {
      ObjectOutputStream.PutField putField = oos.putFields();
      putField.put("enabledAttributes", new Vector(attributes));
      oos.writeFields();
   }
}

