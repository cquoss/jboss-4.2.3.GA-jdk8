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

import java.lang.reflect.Constructor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.StreamCorruptedException;

import java.util.Arrays;

import javax.management.Descriptor;
import javax.management.DescriptorAccess;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanParameterInfo;
import javax.management.RuntimeOperationsException;

import org.jboss.mx.modelmbean.ModelMBeanConstants;
import org.jboss.mx.util.Serialization;

/**
 * Represents constructor.
 *
 * @see javax.management.modelmbean.ModelMBeanInfo
 * @see javax.management.modelmbean.ModelMBeanInfoSupport
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>.
 * @author  <a href="mailto:thomas.diesler@jboss.com">Thomas Diesler</a>.
 * @version $Revision: 57200 $
 *
 * <p><b>20020715 Adrian Brock:</b>
 * <ul>
 * <li> Serialization
 * </ul>  
 */
public class ModelMBeanConstructorInfo
   extends MBeanConstructorInfo
   implements DescriptorAccess, Cloneable
{

   // Attributes ----------------------------------------------------
   
   /**
    * The descriptor associated with this constructor.
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
         serialVersionUID = -4440125391095574518L;
         break;
      default:
         serialVersionUID = 3862947819818064362L;
      }
      serialPersistentFields = new ObjectStreamField[]
      {
         new ObjectStreamField("consDescriptor", Descriptor.class)
      };
   }
   
   // Constructors --------------------------------------------------
   /**
    * Creates a new constructor info with a default descriptor.
    *
    * @param   description human readable description string
    * @param   constructorMethod a <tt>Constructor</tt> instance representing the MBean constructor
    */
   public ModelMBeanConstructorInfo(String description, Constructor constructorMethod)
   {
      super(description, constructorMethod);
      setDescriptor(createDefaultDescriptor());
   }

   /**
    * Creates a new constructor info with a given descriptor. If a <tt>null</tt> or invalid descriptor
    * is passed as a parameter, a default descriptor will be created for the constructor.
    *
    * @param   description human readable description string
    * @param   constructorMethod a <tt>Constructor</tt> instance representing the MBean constructor
    * @param   descriptor a descriptor to associate with this constructor
    */
   public ModelMBeanConstructorInfo(String description,
      Constructor constructorMethod, Descriptor descriptor)
   {
      super(description, constructorMethod);
      setDescriptor(descriptor);
   }

   /**
    * Creates a new constructor info with default descriptor.
    *
    * @param   name  name for the constructor
    * @param   description human readable description string
    * @param   signature constructor signature
    */
   public ModelMBeanConstructorInfo(String name, String description,
      MBeanParameterInfo[] signature)
   {
      super(name, description, signature);
      setDescriptor(createDefaultDescriptor());
   }

   /**
    * Creates a new constructor info with a given descriptor. If a <tt>null</tt> or invalid descriptor
    * is passed as a parameter, a default descriptor will be created for the constructor.
    *
    * @param name name for the constructor
    * @param description human readable description string
    * @param signature constructor signature
    * @param descriptor a descriptor to associate with this constructor
    */
   public ModelMBeanConstructorInfo(String name, String description,
      MBeanParameterInfo[] signature, Descriptor descriptor)
   {
      this(name, description, signature);
      setDescriptor(descriptor);
   }
   
   // DescriptorAccess implementation -------------------------------
   
   /**
    * Returns a copy of the descriptor associated with this constructor.
    *
    * @return a copy of this constructor's descriptor instance
    */
   public Descriptor getDescriptor()
   {
      return (Descriptor)descriptor.clone();
   }
   
   /**
    * Replaces the descriptor associated with this constructor. If the <tt>inDescriptor</tt>
    * argument is <tt>null</tt> then the existing descriptor is replaced with a default
    * descriptor.
    *
    * @param   inDescriptor   descriptor used for replacing the existing constructor descriptor
    * @throws  IllegalArgumentException if the new descriptor is not valid
    */
   public void setDescriptor(Descriptor inDescriptor)
   {
      if (inDescriptor == null)
         inDescriptor = createDefaultDescriptor();
         
      if (inDescriptor.isValid() && isConstructorDescriptorValid(inDescriptor))
         this.descriptor = inDescriptor;
   }

   /**
    * Validate the descriptor in the context of an attribute
    */
   private boolean isConstructorDescriptorValid(Descriptor inDescriptor)
   {
      String name = (String)inDescriptor.getFieldValue(ModelMBeanConstants.NAME);
      if (name.equals(getName()) == false)
         throw new RuntimeOperationsException (new IllegalArgumentException("Invalid name, expected '" + getName() + "' but got: " + name));

      String descriptorType = (String)inDescriptor.getFieldValue(ModelMBeanConstants.DESCRIPTOR_TYPE);
      if (ModelMBeanConstants.OPERATION_DESCRIPTOR.equalsIgnoreCase(descriptorType) == false)
         throw new RuntimeOperationsException (new IllegalArgumentException("Invalid descriptorType, for constructor '" + name + "' expected 'operation' but got: " + descriptorType));

      String role = (String)inDescriptor.getFieldValue(ModelMBeanConstants.ROLE);
      if (ModelMBeanConstants.ROLE_CONSTRUCTOR.equals(role) == false)
         throw new RuntimeOperationsException (new IllegalArgumentException("Invalid role, for constructor '" + name + "' expected 'constructor' but got: " + role));

      return true;
   }

   // Cloneable implementation --------------------------------------
   
   public synchronized Object clone()
   {      
      return (ModelMBeanConstructorInfo)super.clone();
   }

   // Object overrides ----------------------------------------------

   /**
    * @return a human readable string
    */
   public String toString()
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append(getClass().getName()).append(":");
      buffer.append(" name=").append(getName());
      buffer.append(" description=").append(getDescription());
      buffer.append(" signature=").append(Arrays.asList(getSignature()));
      buffer.append(" descriptor=").append(descriptor);
      return buffer.toString();
   }
   
   // Private -------------------------------------------------------
   private Descriptor createDefaultDescriptor()
   {
      DescriptorSupport descr = new DescriptorSupport();
      descr.setField(ModelMBeanConstants.NAME, super.getName());
      descr.setField(ModelMBeanConstants.DESCRIPTOR_TYPE, ModelMBeanConstants.OPERATION_DESCRIPTOR);
      descr.setField(ModelMBeanConstants.DISPLAY_NAME, super.getName());
      descr.setField(ModelMBeanConstants.ROLE, ModelMBeanConstants.ROLE_CONSTRUCTOR);
      return descr;
   }

   private void readObject(ObjectInputStream ois)
      throws IOException, ClassNotFoundException
   {
      ObjectInputStream.GetField getField = ois.readFields();
      descriptor = (Descriptor) getField.get("consDescriptor", null);
      if (descriptor == null)
         throw new StreamCorruptedException("Null descriptor?");
   }

   private void writeObject(ObjectOutputStream oos)
      throws IOException
   {
      ObjectOutputStream.PutField putField = oos.putFields();
      putField.put("consDescriptor", descriptor);
      oos.writeFields();
   }
   
}
