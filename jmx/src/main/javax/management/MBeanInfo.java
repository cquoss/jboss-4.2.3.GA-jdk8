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

import java.util.Arrays;

/**
 * Describes an MBeans' management interface.
 *
 * This implementation protects its immutability by taking shallow clones of all arrays
 * supplied in constructors and by returning shallow array clones in getXXX() methods.
 *
 * @see javax.management.MBeanServer
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 *
 * @version $Revision: 57200 $
 *
 * <p><b>Revisions:</b>
 * <p><b>20020711 Adrian Brock:</b>
 * <ul>
 * <li> Serialization </li>
 * </ul>
 *
 */
public class MBeanInfo
   implements Cloneable, java.io.Serializable
{
   // Constants -----------------------------------------------------
   private static final long serialVersionUID = -6451021435135161911L;

   // Attributes ----------------------------------------------------
   private String className = null;
   private String description = null;
   private MBeanAttributeInfo[] attributes = null;
   private MBeanConstructorInfo[] constructors = null;
   private MBeanOperationInfo[] operations = null;
   private MBeanNotificationInfo[] notifications = null;

   /**
    * The cached string
    */
   private transient String cacheString;

   /**
    * The cached hashCode
    */
   private transient int cacheHashCode;

   // Constructors --------------------------------------------------
   public MBeanInfo(String className, String description,
                    MBeanAttributeInfo[] attributes,
                    MBeanConstructorInfo[] constructors,
                    MBeanOperationInfo[] operations,
                    MBeanNotificationInfo[] notifications)  throws IllegalArgumentException
   {
      this.className = className;
      this.description = description;
      this.attributes = (null == attributes) ? new MBeanAttributeInfo[0] : (MBeanAttributeInfo[]) attributes.clone();
      this.constructors = (null == constructors) ? new MBeanConstructorInfo[0] : (MBeanConstructorInfo[]) constructors.clone();
      this.operations = (null == operations) ? new MBeanOperationInfo[0] : (MBeanOperationInfo[]) operations.clone();
      this.notifications = (null == notifications) ? new MBeanNotificationInfo[0] : (MBeanNotificationInfo[]) notifications.clone();
   }

   // Public --------------------------------------------------------
   public String getClassName()
   {
      return className;
   }

   public String getDescription()
   {
      return description;
   }

   public MBeanAttributeInfo[] getAttributes()
   {
      return (MBeanAttributeInfo[]) attributes.clone();
   }

   public MBeanOperationInfo[] getOperations()
   {
      return (MBeanOperationInfo[]) operations.clone();
   }

   public MBeanConstructorInfo[] getConstructors()
   {
      return (MBeanConstructorInfo[]) constructors.clone();
   }

   public MBeanNotificationInfo[] getNotifications()
   {
      return (MBeanNotificationInfo[]) notifications.clone();
   }

   public boolean equals(Object object)
   {
      if (this == object)
         return true;
      if (object == null || (object instanceof MBeanInfo) == false)
         return false;

      MBeanInfo other = (MBeanInfo) object;

      if (this.getClassName().equals(other.getClassName()) == false)
         return false;
      if (this.getDescription() != null && this.getDescription().equals(other.getDescription()) == false)
         return false;
       if (this.getDescription() == null && other.getDescription() != null)
          return false;

      MBeanAttributeInfo[] thisAttrs = this.getAttributes();
      MBeanAttributeInfo[] otherAttrs = other.getAttributes();
      if (thisAttrs.length != otherAttrs.length)
         return false;
      for (int i = 0; i < thisAttrs.length; ++i)
         if (thisAttrs[i].equals(otherAttrs[i]) == false)
            return false;

      MBeanConstructorInfo[] thisCons = this.getConstructors();
      MBeanConstructorInfo[] otherCons = other.getConstructors();
      if (thisCons.length != otherCons.length)
         return false;
      for (int i = 0; i < thisCons.length; ++i)
         if (thisCons[i].equals(otherCons[i]) == false)
            return false;

      MBeanNotificationInfo[] thisNotfs = this.getNotifications();
      MBeanNotificationInfo[] otherNotfs = other.getNotifications();
      if (thisNotfs.length != otherNotfs.length)
         return false;
      for (int i = 0; i < thisNotfs.length; ++i)
         if (thisNotfs[i].equals(otherNotfs[i]) == false)
            return false;

      MBeanOperationInfo[] thisOpers = this.getOperations();
      MBeanOperationInfo[] otherOpers = other.getOperations();
      if (thisOpers.length != otherOpers.length)
         return false;
      for (int i = 0; i < thisOpers.length; ++i)
         if (thisOpers[i].equals(otherOpers[i]) == false)
            return false;

      return true;
   }

   public int hashCode()
   {
      if (cacheHashCode == 0)
      {
         cacheHashCode =  getClassName().hashCode();
         cacheHashCode += (getDescription() != null) ? getDescription().hashCode() : 0;
      }
      return cacheHashCode;
   }

   /**
    * @return a human readable string
    */
   public String toString()
   {
      if (cacheString == null)
      {
         StringBuffer buffer = new StringBuffer(100);
         buffer.append(getClass().getName()).append(":");
         buffer.append(" className=").append(getClassName());
         buffer.append(" description=").append(getDescription());
         buffer.append(" attributes=").append(Arrays.asList(attributes));
         buffer.append(" constructors=").append(Arrays.asList(constructors));
         buffer.append(" notifications=").append(Arrays.asList(notifications));
         buffer.append(" operations=").append(Arrays.asList(operations));
         cacheString = buffer.toString();
      }
      return cacheString;
   }

   // Cloneable implementation --------------------------------------
   public Object clone()
   {
      MBeanInfo clone = null;
      try
      {
         clone = (MBeanInfo) super.clone();
         clone.className = getClassName();
         clone.description = getDescription();
   
         clone.attributes = getAttributes();
         clone.constructors = getConstructors();
         clone.operations = getOperations();
         clone.notifications = getNotifications();
      }
      catch(CloneNotSupportedException e)
      {         
      }

      return clone;
   }

}
