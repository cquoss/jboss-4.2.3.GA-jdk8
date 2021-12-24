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
package javax.management.modelmbean;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.StreamCorruptedException;
import java.lang.reflect.Method;

import javax.management.Descriptor;
import javax.management.DescriptorAccess;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.RuntimeOperationsException;

import org.jboss.mx.modelmbean.ModelMBeanConstants;
import org.jboss.mx.util.Serialization;


/**
 * Represents a Model MBean's management attribute.
 *
 * @see javax.management.MBeanAttributeInfo
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>.
 * @author  <a href="mailto:thomas.diesler@jboss.com">Thomas Diesler</a>.
 * @version $Revision: 57200 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20020320 Juha Lindfors:</b>
 * <ul>
 * <li>toString() implementation</li>
 *
 * <li>Changed the default descriptor to include field <tt>currencyTimeLimit</tt>
 *     with a value -1. Since default descriptors do not include method mapping
 *     this automatically caches attribute values in the Model MBean.
 * </li>
 * </ul>
 *
 * <p><b>20020715 Adrian Brock:</b>
 * <ul>
 * <li> Serialization
 * </ul>
 */
public class ModelMBeanAttributeInfo
   extends MBeanAttributeInfo
   implements DescriptorAccess, Cloneable
{

   // Attributes ----------------------------------------------------
   /**
    * The descriptor associated with this attribute.
    */
   private Descriptor descriptor = null;

   // Static --------------------------------------------------------

   private static final long serialVersionUID;
   private static final ObjectStreamField[] serialPersistentFields;

   static
   {
      switch (Serialization.version)
      {
      case Serialization.V1R0:
         serialVersionUID = 7098036920755973145L;
         break;
      default:
         serialVersionUID = 6181543027787327345L;
      }
      serialPersistentFields = new ObjectStreamField[]
      {
         new ObjectStreamField("attrDescriptor", Descriptor.class)
      };
   }


   // Constructors --------------------------------------------------
   /**
    * Creates a new attribute info with a default descriptor.
    *
    * @param  name name of the attribute
    * @param  description human readable description string
    * @param  getter a <tt>Method</tt> instance representing a read method for this attribute
    * @param  setter a <tt>Method</tt> instance representing a write method for this attribute
    *
    * @throws IntrospectionException if the accessor methods are not valid for this attribute
    */
   public ModelMBeanAttributeInfo(String name, String description, Method getter, Method setter)
         throws IntrospectionException
   {
      // NOTE:  This constructor provides the method mapping for
      //        a Model MBean attribute so the 'setMethod' and 'getMethod'
      //        descriptor field should be set, but the javadoc dictates a
      //        default descriptor...?

      super(name, description, getter, setter);
      setDescriptor(createDefaultDescriptor());
   }

   /**
    * Creates a new attribute info object. If a <tt>null</tt> or
    * invalid descriptor is passed as a parameter, a default descriptor will be created
    * for the attribute.
    *
    * @param  name name of the attribute
    * @param  description human readable description string
    * @param  getter a <tt>Method</tt> instance representing a read method for this attribute
    * @param  setter a <tt>Method</tt> instance representing a write method for this attribute
    * @param  descriptor a descriptor to associate with this attribute
    *
    * @throws IntrospectionException if the accessor methods are not valid for this attribute
    */
   public ModelMBeanAttributeInfo(String name, String description, Method getter, Method setter, Descriptor descriptor)
         throws IntrospectionException
   {
      super(name, description, getter, setter);
      setDescriptor(descriptor);
   }

   /**
    * Creates a new attribute info object with a default descriptor.
    *
    * @param   name  name of the attribute
    * @param   type  fully qualified class name of the attribute's type
    * @param   description human readable description string
    * @param   isReadable true if attribute is readable; false otherwise
    * @param   isWritable true if attribute is writable; false otherwise
    * @param   isIs (not used for Model MBeans; false)
    */
   public ModelMBeanAttributeInfo(String name, String type, String description,
                                  boolean isReadable, boolean isWritable, boolean isIs)
   {
      super(name, type, description, isReadable, isWritable, isIs);
      setDescriptor(createDefaultDescriptor());
   }

   /**
    * Creates a new attribute info object with a given descriptor. If a <tt>null</tt> or invalid
    * descriptor is passed as a parameter, a default descriptor will be created for the attribute.
    *
    * @param  name   name of the attribute
    * @param  type   fully qualified class name of the attribute's type
    * @param  description human readable description string
    * @param  isReadable true if the attribute is readable; false otherwise
    * @param  isWritable true if the attribute is writable; false otherwise
    * @param  isIs  (not used for Model MBeans; false)
    */
   public ModelMBeanAttributeInfo(String name, String type, String description,
                                  boolean isReadable, boolean isWritable, boolean isIs, Descriptor descriptor)
   {
      super(name, type, description, isReadable, isWritable, isIs);
      setDescriptor(descriptor);
   }

   /**
    * Copy constructor.
    *
    * @param info the attribute info to copy
    */
   public ModelMBeanAttributeInfo(ModelMBeanAttributeInfo info)
   {
      // THS - javadoc says a default descriptor will be created but that's not
      // consistent with the other *Info classes.
      // I'm also assuming that getDescriptor returns a clone.
      this(info.getName(), info.getType(), info.getDescription(), info.isReadable(),
           info.isWritable(), info.isIs(), info.getDescriptor());
   }

   // DescriptorAccess implementation -------------------------------
   /**
    * Returns a copy of the descriptor associated with this attribute.
    *
    * @return a copy of this attribute's descriptor
    */
   public Descriptor getDescriptor()
   {
      return (Descriptor)descriptor.clone();
   }

   /**
    * Replaces the descriptor associated with this attribute. If the <tt>inDescriptor</tt>
    * argument is <tt>null</tt> then the existing descriptor is replaced with a default
    * descriptor.
    *
    * @param   inDescriptor   descriptor used for replacing the existing operation descriptor
    * @throws IllegalArgumentException if the new descriptor is not valid
    */   
   public void setDescriptor(Descriptor inDescriptor)
   {
      if (inDescriptor == null)
         inDescriptor = createDefaultDescriptor();

      if (inDescriptor.isValid() && isAttributeDescriptorValid(inDescriptor))
         this.descriptor = inDescriptor;
   }

   /**
    * Validate the descriptor in the context of an attribute
    */
   private boolean isAttributeDescriptorValid(Descriptor inDescriptor)
   {
      String name = (String)inDescriptor.getFieldValue(ModelMBeanConstants.NAME);
      if (name.equals(getName()) == false)
         throw new RuntimeOperationsException (new IllegalArgumentException("Invalid name, expected '" + getName() + "' but got: " + name));

      String descriptorType = (String)inDescriptor.getFieldValue(ModelMBeanConstants.DESCRIPTOR_TYPE);
      if (ModelMBeanConstants.ATTRIBUTE_DESCRIPTOR.equalsIgnoreCase(descriptorType) == false)
         throw new RuntimeOperationsException (new IllegalArgumentException("Invalid descriptorType, for attribute '" + name + "' expected 'attribute' but got: " + descriptorType));

      return true;
   }

   // Cloneable implementation --------------------------------------
   /**
    * Creates a copy of this object.
    *
    * @return clone of this object
    */
   public synchronized Object clone()
   {
      ModelMBeanAttributeInfo clone = (ModelMBeanAttributeInfo)super.clone();
      clone.descriptor  = (Descriptor)this.descriptor.clone();

      return clone;
   }

   // Object override -----------------------------------------------
   /**
    * Returns a string representation of this Model MBean attribute info object.
    * The returned string is in the form: <pre>
    *
    *   ModelMBeanAttributeInfo[Name=&lt;attribute name&gt;,
    *   Type=&lt;class name of the attribute type&gt;,
    *   Access= RW | RO | WO,
    *   Descriptor=(fieldName1=fieldValue1, ... , fieldName&lt;n&gt;=fieldValue&lt;n&gt;)]
    *
    * </pre>
    *
    * @return string representation of this object
    */
   public String toString()
   {
      return "ModelMBeanAttributeInfo[" +
             "Name=" + getName() +
             ",Type=" + getType() +
             ",Access=" + ((isReadable() && isWritable()) ? "RW" : (isReadable()) ? "RO" : "WO") +
             ",Descriptor(" + getDescriptor() + ")]";
   }

   // Private -------------------------------------------------------
   
   /**
    * Creates a default descriptor.
    */
   private Descriptor createDefaultDescriptor()
   {
      DescriptorSupport descr = new DescriptorSupport();
      descr.setField(ModelMBeanConstants.NAME, super.getName());
      descr.setField(ModelMBeanConstants.DESCRIPTOR_TYPE, ModelMBeanConstants.ATTRIBUTE_DESCRIPTOR);
      descr.setField(ModelMBeanConstants.DISPLAY_NAME, super.getName());
      return descr;
   }

   private void readObject(ObjectInputStream ois)
      throws IOException, ClassNotFoundException
   {
      ObjectInputStream.GetField getField = ois.readFields();
      descriptor = (Descriptor) getField.get("attrDescriptor", null);
      if (descriptor == null)
         throw new StreamCorruptedException("Null descriptor?");
      if( descriptor.getFieldValue(ModelMBeanConstants.ATTRIBUTE_VALUE) == null )
      {
         // Check for 3.2.x legacy "value"
         Object value = descriptor.getFieldValue("value");
         if( value != null )
            descriptor.setField(ModelMBeanConstants.ATTRIBUTE_VALUE, value);
      }
   }

   private void writeObject(ObjectOutputStream oos)
      throws IOException
   {
      ObjectOutputStream.PutField putField = oos.putFields();
      putField.put("attrDescriptor", descriptor);
      oos.writeFields();
   }

}

