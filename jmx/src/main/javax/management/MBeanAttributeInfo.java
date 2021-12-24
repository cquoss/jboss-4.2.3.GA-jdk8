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

import java.lang.reflect.Method;
import java.io.Serializable;

import org.jboss.mx.util.Serialization;

/**
 * Represents a management attribute in an MBeans' management interface.
 *
 * @see javax.management.MBeanInfo
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 *
 * <p><b>Revisions:</b>
 * <p><b>20020711 Adrian Brock:</b>
 * <ul>
 * <li> Serialization </li>
 * </ul>
 *   
 */
public class MBeanAttributeInfo extends MBeanFeatureInfo
   implements Serializable, Cloneable
{
   // Constants -----------------------------------------------------

   private static final long serialVersionUID;

   static
   {
      switch (Serialization.version)
      {
      case Serialization.V1R0:
         serialVersionUID = 7043855487133450673L;
         break;
      default:
         serialVersionUID = 8644704819898565848L;
      }
   }

   // Attributes ----------------------------------------------------
   
   /**
    * Attribute type string. This is a fully qualified class name of the type.
    */
   private String attributeType        = null;
   
   /**
    * Is attribute readable.
    */
   private boolean isRead = false;
   
   /**
    * Is attribute writable.
    */
   private boolean isWrite = false;
   
   /**
    * Is attribute using the boolean <tt>isAttributeName</tt> naming convention.
    */
   private boolean is       = false;

   /**
    * The cached string
    */
   private transient String cacheString;

   /**
    * The cached hashCode
    */
   private transient int cacheHashCode;
   
   // Constructors --------------------------------------------------
   
   /**
    * Creates an MBean attribute info object.
    *
    * @param   name name of the attribute
    * @param   type the fully qualified class name of the attribute's type
    * @param   description human readable description string of the attribute
    * @param   isReadable if attribute is readable
    * @param   isWritable if attribute is writable
    * @param   isIs if attribute is using the boolean <tt>isAttributeName</tt> naming convention for its getter method
    * @exception IllegalArgumentException for a true isIs with a non boolean type or the type is not a valid
    *            java type or it is a reserved word
    */
   public MBeanAttributeInfo(String name, String type, String description,
                             boolean isReadable, boolean isWritable, boolean isIs)
      throws IllegalArgumentException
   {
      super(name, description);

      /* Removed as of the 1.2 management release
      if (MetaDataUtil.isValidJavaType(type) == false)
         throw new IllegalArgumentException("Type is not a valid java identifier (or is reserved): " + type);
      */
      if (isIs && type.equals(Boolean.TYPE.getName()) == false && type.equals(Boolean.class.getName()) == false)
         throw new IllegalArgumentException("Cannot have isIs for a non boolean/Boolean type");
   
      this.attributeType = type;
      this.isRead = isReadable;
      this.isWrite = isWritable;
      this.is = isIs;
   }

   /**
    * Creates an MBean attribute info object using the given accessor methods.
    *
    * @param   name        Name of the attribute.
    * @param   description Human readable description string of the attribute's type.
    * @param   getter      The attribute's read accessor. May be <tt>null</tt> if the attribute is write-only.
    * @param   setter      The attribute's write accessor. May be <tt>null</tt> if the attribute is read-only.
    *
    * @throws  IntrospectionException if the accessor methods are not valid for the attribute
    */
   public MBeanAttributeInfo(String name, String description, Method getter, Method setter)
         throws IntrospectionException
   {
      super(name, description);

      if (getter != null)
      {
         // getter must always be no args method, return type cannot be void
         if (getter.getParameterTypes().length != 0)
            throw new IntrospectionException("Expecting getter method to be of the form 'AttributeType getAttributeName()': found getter with " + getter.getParameterTypes().length + " parameters.");
         if (getter.getReturnType() == Void.TYPE)
            throw new IntrospectionException("Expecting getter method to be of the form 'AttributeType getAttributeName()': found getter with void return type.");
            
         this.isRead = true;
         
         if (getter.getName().startsWith("is"))
            this.is = true;
         
         this.attributeType = getter.getReturnType().getName();
      }

      if (setter != null)
      {
         // setter must have one argument, no less, no more. Return type must be void.
         if (setter.getParameterTypes().length != 1)
            throw new IntrospectionException("Expecting the setter method to be of the form 'void setAttributeName(AttributeType value)': found setter with " + setter.getParameterTypes().length + " parameters.");
         if (setter.getReturnType() != Void.TYPE)
            throw new IntrospectionException("Expecting the setter method to be of the form 'void setAttributeName(AttributeType value)': found setter with " + setter.getReturnType() + " return type.");
            
         this.isWrite = true;

         if (attributeType == null)
         {
            try
            {
               attributeType = setter.getParameterTypes() [0].getName();
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
               throw new IntrospectionException("Attribute setter is lacking type: " + name);
            }
         }

         if (!(attributeType.equals(setter.getParameterTypes() [0].getName())))
            throw new IntrospectionException("Attribute type mismatch: " + name);
      }
   }

   
   // Public --------------------------------------------------------

   /**
    * Creates a copy of this object.
    *
    * @return clone of this object
    */
   public synchronized Object clone()
   {
      MBeanAttributeInfo clone = null;
      try
      {
         clone = (MBeanAttributeInfo) super.clone();
         clone.attributeType = this.attributeType;
         clone.isRead        = this.isRead;
         clone.isWrite       = this.isWrite;
         clone.is            = this.is;
      }
      catch(CloneNotSupportedException e)
      {         
      }

      return clone;
   }

   /**
    * Returns the type string of this attribute.
    *
    * @return fully qualified class name of the attribute's type
    */
   public String getType()
   {
      return attributeType;
   }
   
   /**
    * If the attribute is readable.
    *
    * @return true if attribute is readable; false otherwise
    */
   public boolean isReadable()
   {
      return isRead;
   }

   /**
    * If the attribute is writable.
    *
    * @return true if attribute is writable; false otherwise
    */
   public boolean isWritable()
   {
      return isWrite;
   }

   /**
    * If the attribute is using the boolean <tt>isAttributeName</tt> naming convention
    * for its read accessor.
    *
    * @return true if using <tt>isAttributeName</tt> getter; false otherwise
    */
   public boolean isIs()
   {
      return is;
   }

   public boolean equals(Object object)
   {
      if (this == object)
         return true;
      if (object == null || (object instanceof MBeanAttributeInfo) == false)
         return false;

      MBeanAttributeInfo other = (MBeanAttributeInfo) object;

      if (super.equals(other) == false)
         return false;
      if (this.getType().equals(other.getType()) == false)
         return false;
      if (this.isReadable() != other.isReadable())
         return false;
      if (this.isWritable() != other.isWritable())
         return false;
      if (this.isIs() != other.isIs())
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

   public String toString()
   {
      if (cacheString == null)
      {
         StringBuffer buffer = new StringBuffer(100);
         buffer.append(getClass().getName()).append(":");
         buffer.append(" name=").append(getName());
         buffer.append(" description=").append(getDescription());
         buffer.append(" type=").append(getType());
         buffer.append(" Readable=").append(isReadable());
         buffer.append(" Writable=").append(isWritable());
         buffer.append(" isIs=").append(isIs());
         cacheString = buffer.toString();
      }
      return cacheString;
   }
   
}
