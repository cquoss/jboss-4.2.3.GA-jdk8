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
 * Describes a notification emitted by an MBean
 *
 * This implementation protects its immutability by taking shallow clones of all arrays
 * supplied in constructors and by returning shallow array clones in getXXX() methods.
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
 */
public class MBeanNotificationInfo extends MBeanFeatureInfo
   implements Cloneable, java.io.Serializable
{
   // Constants -----------------------------------------------------

   private static final long serialVersionUID = -3888371564530107064L;

   // Attributes ----------------------------------------------------
   private String[] types = null;

   /**
    * The cached string
    */
   private transient String cacheString;

   /**
    * The cached hashCode
    */
   private transient int cacheHashCode;

   // Constructors --------------------------------------------------
   public MBeanNotificationInfo(String[] notifsType, String name, String description)
      throws IllegalArgumentException
   {
      super(name, description);

      this.types = (null == notifsType) ? new String[0] : (String[]) notifsType.clone();
   }

   // Public -------------------------------------------------------
   public String[] getNotifTypes()
   {
      return (String[]) types.clone();
   }

   public boolean equals(Object object)
   {
      if (this == object)
         return true;
      if (object == null || (object instanceof MBeanNotificationInfo) == false)
         return false;

      MBeanNotificationInfo other = (MBeanNotificationInfo) object;

      if (super.equals(other) == false)
         return false;

      String[] thisTypes = this.getNotifTypes();
      String[] otherTypes = other.getNotifTypes();
      if (thisTypes.length != otherTypes.length)
         return false;
      for (int i = 0; i < thisTypes.length; ++i)
         if (thisTypes[i].equals(otherTypes[i]) == false)
            return false;

      return true;
   }

   public int hashCode()
   {
      if (cacheHashCode == 0)
      {
         cacheHashCode =  super.hashCode();
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
         buffer.append(" name=").append(getName());
         buffer.append(" description=").append(getDescription());
         buffer.append(" types=").append(Arrays.asList(types));
         cacheString = buffer.toString();
      }
      return cacheString;
   }

   // CLoneable implementation -------------------------------------
   public Object clone()
   {
      MBeanNotificationInfo clone = null;
      try
      {
         clone = (MBeanNotificationInfo) super.clone();
         clone.types = getNotifTypes();
      }
      catch(CloneNotSupportedException e)
      {  
      }

      return clone;
   }
}
