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

import org.jboss.mx.modelmbean.ModelMBeanConstants;
import org.jboss.mx.util.Serialization;

import javax.management.Descriptor;
import javax.management.DescriptorAccess;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.RuntimeOperationsException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.StreamCorruptedException;
import java.lang.reflect.Method;

/**
 * Represents Model MBean operation.
 *
 * @see javax.management.modelmbean.ModelMBeanInfo
 * @see javax.management.modelmbean.ModelMBeanInfoSupport
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>.
 * @author  <a href="mailto:thomas.diesler@jboss.com">Thomas Diesler</a>.
 * @version $Revision: 57200 $
 *
 */
public class ModelMBeanOperationInfo
   extends MBeanOperationInfo
   implements DescriptorAccess
{
           
   // Attributes ----------------------------------------------------

   /**
    * The descriptor associated with this operation.
    */
   private Descriptor descriptor = null;

   /**
    * The role of this operation
    * Is constructor, getter, setter, operation
    */
   private transient String operationRole;

   // Static --------------------------------------------------------

   private static final long serialVersionUID;
   private static final ObjectStreamField[] serialPersistentFields;

   static
   {
      switch (Serialization.version)
      {
      case Serialization.V1R0:
         serialVersionUID = 9087646304346171239L;
         serialPersistentFields = new ObjectStreamField[]
         {
            new ObjectStreamField("operDescriptor", Descriptor.class)
         };
         break;
      default:
         serialVersionUID = 6532732096650090465L;
         serialPersistentFields = new ObjectStreamField[]
         {
            new ObjectStreamField("operationDescriptor", Descriptor.class)
         };
      }
   }

   // Constructors --------------------------------------------------
   /**
    * Creates a new operation info with a default descriptor.
    *
    * @param   description human readable description string
    * @param   operationMethod a <tt>Method</tt> instance representing the
    *          management operation
    */
   public ModelMBeanOperationInfo(String description, Method operationMethod)
   {
      super(description, operationMethod);
      this.operationRole = getOperationRole();
      setDescriptor(createDefaultDescriptor());
   }

   /**
    * Creates a new operation info with a given descriptor. If a <tt>null</tt> or
    * invalid descriptor is passed as a paramter, a default descriptor will be created
    * for the operation.
    *
    * @param   description human readable description string
    * @param   operationMethod a <tt>Method</tt> instance representing the management
    *          operation
    * @param   descriptor a descriptor to associate with this operation
    */
   public ModelMBeanOperationInfo(String description, Method operationMethod, Descriptor descriptor)
   {
      super(description, operationMethod);
      this.operationRole = getOperationRole();
      setDescriptor(descriptor);
   }

   /**
    * Creates a new operation info with a default descriptor.
    *
    * @param   name name of the operation
    * @param   description human readable description string
    * @param   signature operation signature
    * @param   type a fully qualified name of the operations return type
    * @param   impact operation impact: {@link #INFO INFO}, {@link #ACTION ACTION}, {@link #ACTION_INFO ACTION_INFO}, {@link #UNKNOWN UNKNOWN}
    */
   public ModelMBeanOperationInfo(String name, String description, MBeanParameterInfo[] signature,
                                  String type, int impact)
   {
      super(name, description, signature, type, impact);
      this.operationRole = getOperationRole();
      setDescriptor(createDefaultDescriptor());
   }

   /**
    * Creates a new operation info with a given descriptor. If a <tt>null</tt> or invalid
    * descriptor is passed as a parameter, a default descriptor will be created for the operation.
    *
    * @param   name name of the operation
    * @param   description human readable description string
    * @param   signature operation signature
    * @param   type a fully qualified name of the oeprations return type
    * @param   impact operation impact: {@link #INFO INFO}, {@link #ACTION ACTION}, {@link #ACTION_INFO ACTION_INFO}, {@link #UNKNOWN UNKNOWN}
    * @param   descriptor a descriptor to associate with this operation
    */
   public ModelMBeanOperationInfo(String name, String description, MBeanParameterInfo[] signature,
                                  String type, int impact, Descriptor descriptor)
   {
      super(name, description, signature, type, impact);
      this.operationRole = getOperationRole();
      setDescriptor(descriptor);
   }

   /**
    * Copy constructor.
    *
    * @param  info the operation info to copy
    */
   public ModelMBeanOperationInfo(ModelMBeanOperationInfo info)
   {
      this(info.getName(), info.getDescription(), info.getSignature(),
           info.getReturnType(), info.getImpact(), info.getDescriptor());
   }

   /**
    * Get the role of this operation
    */
   private String getOperationRole()
   {
      if (getName().startsWith("get") && getReturnType().equals("void") == false && getSignature().length == 0)
         return ModelMBeanConstants.ROLE_GETTER;
      else if (getName().startsWith("is") && (getReturnType().equals("boolean") || getReturnType().equals("java.lang.Boolean")) && getSignature().length == 0)
         return ModelMBeanConstants.ROLE_GETTER;
      else if (getName().startsWith("set") && getReturnType().equals("void") && getSignature().length == 1)
         return ModelMBeanConstants.ROLE_SETTER;
      else
         return ModelMBeanConstants.ROLE_OPERATION;
   }

   // DescriptorAccess implementation -------------------------------

   /**
    * Returns a copy of the descriptor associated with this operation.
    *
    * @return a copy of this operation's associated descriptor
    */
   public Descriptor getDescriptor()
   {
      return (Descriptor)descriptor.clone();
   }

   /**
    * Replaces the descriptor associated with this operation. If the <tt>inDescriptor</tt>
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

      if (inDescriptor.isValid() && isOperationDescriptorValid(inDescriptor))
         this.descriptor = inDescriptor;
   }

   /**
    * Validate the descriptor in the context of an attribute
    */
   private boolean isOperationDescriptorValid(Descriptor inDescriptor)
   {
      String name = (String)inDescriptor.getFieldValue(ModelMBeanConstants.NAME);
      if (name.equals(getName()) == false)
         throw new RuntimeOperationsException (new IllegalArgumentException("Invalid name, expected '" + getName() + "' but got: " + name));

      String descriptorType = (String)inDescriptor.getFieldValue(ModelMBeanConstants.DESCRIPTOR_TYPE);
      if (ModelMBeanConstants.OPERATION_DESCRIPTOR.equalsIgnoreCase(descriptorType) == false)
         throw new RuntimeOperationsException (new IllegalArgumentException("Invalid descriptorType, for operation '" + name + "' expected 'operation' but got: " + descriptorType));

      String role = (String) inDescriptor.getFieldValue(ModelMBeanConstants.ROLE);
      if (role != null && operationRole.equals(role) == false)
         throw new RuntimeOperationsException(new IllegalArgumentException("Invalid role, for operation '" + name + "' expected '" + operationRole + "' but got: " + role));

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
      ModelMBeanOperationInfo clone = (ModelMBeanOperationInfo)super.clone();
      clone.descriptor = (Descriptor)this.descriptor.clone();
      return clone;
   }

   // Object overrides ----------------------------------------------
   /**
    * Returns a string representation of this Model MBean operation info object.
    * The returned string is in the form: <pre>
    *
    *   ModelMBeanOperationInfo[&lt;return type&gt; &lt;operation name&gt;(&lt;signature&gt;),
    *   Impact=ACTION | INFO | ACTION_INFO | UNKNOWN,
    *   Descriptor=(fieldName1=fieldValue1, ... , fieldName&lt;n&gt;=fieldValue&lt;n&gt;)]
    *
    * </pre>
    *
    * @return string representation of this object
    */
   public String toString()
   {
      return "ModelMBeanOperationInfo[" +
             getReturnType() + " " + getName() + getSignatureString() +
             ",Impact=" + getImpactString() +
             ",Descriptor(" + getDescriptor() + ")]";
   }

   // Private -------------------------------------------------------
   private String getSignatureString() 
   {
      StringBuffer sbuf = new StringBuffer(400);
      sbuf.append("(");
      
      MBeanParameterInfo[] sign = getSignature();
      
      if (sign.length > 0)
      {
         for (int i = 0; i < sign.length; ++i)
         {
            sbuf.append(sign[i].getType());
            sbuf.append(" ");
            sbuf.append(sign[i].getName());
         
            sbuf.append(",");
         }
      
         sbuf.delete(sbuf.length() - 1, sbuf.length());
      }
      sbuf.append(")");
      
      return sbuf.toString();
   }
   
   private String getImpactString()
   {
      int impact = getImpact();
      if (impact == MBeanOperationInfo.ACTION)
         return "ACTION";
      else if (impact == MBeanOperationInfo.INFO)
         return "INFO";
      else if (impact == MBeanOperationInfo.ACTION_INFO)
         return "ACTION_INFO";
      else
         return "UNKNOWN";
   }

   /**
    * The default descriptor will have name, descriptorType, displayName and role fields set.
    */
   private Descriptor createDefaultDescriptor()
   {
      DescriptorSupport descr = new DescriptorSupport();
      descr.setField(ModelMBeanConstants.NAME, super.getName());
      descr.setField(ModelMBeanConstants.DISPLAY_NAME, super.getName());
      descr.setField(ModelMBeanConstants.DESCRIPTOR_TYPE, ModelMBeanConstants.OPERATION_DESCRIPTOR);
      descr.setField(ModelMBeanConstants.ROLE, operationRole);
      return descr;
   }

   private void readObject(ObjectInputStream ois)
      throws IOException, ClassNotFoundException
   {
      ObjectInputStream.GetField getField = ois.readFields();
      switch (Serialization.version)
      {
      case Serialization.V1R0:
         descriptor = (Descriptor) getField.get("operDescriptor", null);
         break;
      default:
         descriptor = (Descriptor) getField.get("operationDescriptor", null);
      }
      if (descriptor == null)
         throw new StreamCorruptedException("Null descriptor?");
   }

   private void writeObject(ObjectOutputStream oos)
      throws IOException
   {
      ObjectOutputStream.PutField putField = oos.putFields();
      switch (Serialization.version)
      {
      case Serialization.V1R0:
         putField.put("operDescriptor", descriptor);
         break;
      default:
         putField.put("operationDescriptor", descriptor);
      }
      oos.writeFields();
   }
}

