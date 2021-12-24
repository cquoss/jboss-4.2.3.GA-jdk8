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

/**
 * Describes an argument of an operation exposed by an MBean
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
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
public class MBeanParameterInfo extends MBeanFeatureInfo
   implements java.io.Serializable, Cloneable
{
   // Constants -----------------------------------------------------

   private static final long serialVersionUID = 7432616882776782338L;

   // Attributes ----------------------------------------------------
   private String type = null;

   /**
    * The cached string
    */
   private transient String cacheString;

   /**
    * The cached hashCode
    */
   private transient int cacheHashCode;

   // Constructors --------------------------------------------------
   public MBeanParameterInfo(java.lang.String name, java.lang.String type, java.lang.String description)
           throws IllegalArgumentException
   {
      super(name, description);
      /* Removed as of the 1.2 management release
      if (MetaDataUtil.isValidJavaType(type) == false)
         throw new IllegalArgumentException("type is not a valid java type (or is a reserved word): " + type);
      */

      this.type = type;
   }


   // Public --------------------------------------------------------
   public java.lang.String getType()
   {
      return type;
   }

   public boolean equals(Object object)
   {
      if (this == object)
         return true;
      if (object == null || (object instanceof MBeanParameterInfo) == false)
         return false;

      MBeanParameterInfo other = (MBeanParameterInfo) object;

      if (super.equals(other) == false)
         return false;
      if (this.getType().equals(other.getType()) == false)
         return false;

      return true;
   }

   public int hashCode()
   {
      if (cacheHashCode == 0)
      {
         cacheHashCode =  super.hashCode();
         cacheHashCode += getType().hashCode();
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
         buffer.append(" type=").append(getType());
         cacheString = buffer.toString();
      }
      return cacheString;
   }

   // Cloneable implementation --------------------------------------
   public Object clone()
   {
      MBeanParameterInfo clone = null;
      try
      {
         clone = (MBeanParameterInfo) super.clone();
         clone.type = getType();
      }
      catch(CloneNotSupportedException e)
      {
      }

      return clone;
   }
}
