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
package javax.management.openmbean;

import java.io.Serializable;

import java.util.Arrays;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;

/**
 * OpenMBeanInfo implementation
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 *
 * @version $Revision: 57200 $
 *
 */
public class OpenMBeanInfoSupport
   extends MBeanInfo
   implements OpenMBeanInfo, Serializable
{
   // Constants -----------------------------------------------------

   private static final long serialVersionUID = 4349395935420511492L;

   // Attributes ----------------------------------------------------

   private transient int cachedHashCode;

   private transient String cachedToString;

   // Static --------------------------------------------------------

   private static MBeanAttributeInfo[] convertArray(OpenMBeanAttributeInfo[] array)
   {
      if (array == null)
         return null;
      MBeanAttributeInfo[] result = new MBeanAttributeInfo[array.length];
      System.arraycopy(array, 0, result, 0, array.length);
      return result;
   }

   private static MBeanConstructorInfo[] convertArray(OpenMBeanConstructorInfo[] array)
   {
      if (array == null)
         return null;
      MBeanConstructorInfo[] result = new MBeanConstructorInfo[array.length];
      System.arraycopy(array, 0, result, 0, array.length);
      return result;
   }

   private static MBeanOperationInfo[] convertArray(OpenMBeanOperationInfo[] array)
   {
      if (array == null)
         return null;
      MBeanOperationInfo[] result = new MBeanOperationInfo[array.length];
      System.arraycopy(array, 0, result, 0, array.length);
      return result;
   }

   // Constructors --------------------------------------------------

   /**
    * Contruct an OpenMBeanInfoSupport<p>
    *
    * @param className cannot be null or empty
    * @param description cannot be null or empty
    * @param attributes the open mbean's attributes
    * @param constructors the open mbean's constructors
    * @param operations the open mbean's operations
    * @param notifications the open mbean's notifications
    * @exception IllegalArgumentException when one of the above
    *            constraints is not satisfied
    */
   public OpenMBeanInfoSupport(String className, String description,
                               OpenMBeanAttributeInfo[] attributes,
                               OpenMBeanConstructorInfo[] constructors,
                               OpenMBeanOperationInfo[] operations,
                               MBeanNotificationInfo[] notifications)
   {
      super(className, description, convertArray(attributes), convertArray(constructors),
            convertArray(operations), notifications);
   }

   // Public --------------------------------------------------------

   // OpenMBeanInfo Implementation ----------------------------------

   // Object Overrides ----------------------------------------------

   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null || !(obj instanceof OpenMBeanInfoSupport))
         return false;
      OpenMBeanInfo other = (OpenMBeanInfo) obj;

      if (getClassName().equals(other.getClassName()) == false)
         return false;

      if (compareArray(getAttributes(), other.getAttributes()) == false)
         return false;

      if (compareArray(getConstructors(), other.getConstructors()) == false)
         return false;

      if (compareArray(getNotifications(), other.getNotifications()) == false)
         return false;

      if (compareArray(getOperations(), other.getOperations()) == false)
         return false;

      return true;
   }

   public int hashCode()
   {
      if (cachedHashCode != 0)
        return cachedHashCode;
      cachedHashCode = getClassName().hashCode();
      MBeanAttributeInfo[] attrs = getAttributes();
      for (int i = 0; i < attrs.length; i++)
         cachedHashCode += attrs[i].hashCode();
      MBeanConstructorInfo[] ctors = getConstructors();
      for (int i = 0; i < ctors.length; i++)
         cachedHashCode += ctors[i].hashCode();
      MBeanNotificationInfo[] notify = getNotifications();
      for (int i = 0; i < notify.length; i++)
         cachedHashCode += notify[i].hashCode();
      MBeanOperationInfo[] ops = getOperations();
      for (int i = 0; i < ops.length; i++)
         cachedHashCode += ops[i].hashCode();
      return cachedHashCode;
   }

   public String toString()
   {
      if (cachedToString != null)
         return cachedToString;
      StringBuffer buffer = new StringBuffer(getClass().getName());
      buffer.append(": className=");
      buffer.append(getClassName());
      buffer.append(", attributes=");
      buffer.append(Arrays.asList(getAttributes()));
      buffer.append(", constructors=");
      buffer.append(Arrays.asList(getConstructors()));
      buffer.append(", notifications=");
      buffer.append(Arrays.asList(getNotifications()));
      buffer.append(", operations=");
      buffer.append(Arrays.asList(getOperations()));
      cachedToString = buffer.toString();
      return cachedToString;
   }

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   private boolean compareArray(Object[] one, Object[] two)
   {
      if (one.length != two.length)
         return false;
      if (Arrays.asList(one).containsAll(Arrays.asList(two)) == false)
         return false;
      return true;
   }

   // Inner Classes -------------------------------------------------
}
