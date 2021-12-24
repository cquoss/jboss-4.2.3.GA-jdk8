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

import javax.management.MBeanNotificationInfo;
import javax.management.Descriptor;
import javax.management.DescriptorAccess;
import javax.management.RuntimeOperationsException;

import org.jboss.mx.modelmbean.ModelMBeanConstants;
import org.jboss.mx.util.Serialization;

/**
 * Represents a notification in a Model MBean's management interface.
 *
 * @see javax.management.modelmbean.ModelMBeanInfo
 * @see javax.management.modelmbean.ModelMBeanAttributeInfo
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>.
 * @author  <a href="mailto:thomas.diesler@jboss.com">Thomas Diesler</a>.
 * @version $Revision: 57200 $
 *
 */
public class ModelMBeanNotificationInfo
         extends MBeanNotificationInfo
         implements DescriptorAccess, Cloneable
{

   // Attributes ----------------------------------------------------
   private Descriptor descriptor = null;

   // Static --------------------------------------------------------

   private static final long serialVersionUID;
   private static final ObjectStreamField[] serialPersistentFields;

   static
   {
      switch (Serialization.version)
      {
      case Serialization.V1R0:
         serialVersionUID = -5211564525059047097L;
         serialPersistentFields = new ObjectStreamField[]
         {
            new ObjectStreamField("ntfyDescriptor", Descriptor.class)
         };
         break;
      default:
         serialVersionUID = -7445681389570207141L;
         serialPersistentFields = new ObjectStreamField[]
         {
            new ObjectStreamField("notificationDescriptor", Descriptor.class)
         };
      }
   }

   // Constructors --------------------------------------------------
   public ModelMBeanNotificationInfo(String[] notifTypes, String name, String description)
   {
      super(notifTypes, name, description);
      setDescriptor(createDefaultDescriptor());
   }

   public ModelMBeanNotificationInfo(String[] notifTypes, String name, String description,
                                     Descriptor descriptor)
   {
      super(notifTypes, name, description);
      setDescriptor(descriptor);
   }

   public ModelMBeanNotificationInfo(ModelMBeanNotificationInfo info)
   {
      this(info.getNotifTypes(), info.getName(), info.getDescription(), info.getDescriptor());
   }

   // Public --------------------------------------------------------
   public Descriptor getDescriptor()
   {
      return (Descriptor)descriptor.clone();
   }

   public void setDescriptor(Descriptor inDescriptor)
   {
      if (inDescriptor == null)
         inDescriptor = createDefaultDescriptor();

      if (inDescriptor.isValid() && isNotificationDescriptorValid(inDescriptor))
         this.descriptor = inDescriptor;
   }

   /**
    * Validate the descriptor in the context of an attribute
    */
   private boolean isNotificationDescriptorValid(Descriptor inDescriptor)
   {
      String name = (String)inDescriptor.getFieldValue(ModelMBeanConstants.NAME);
      if (name.equals(getName()) == false)
         throw new RuntimeOperationsException (new IllegalArgumentException("Invalid name, expected '" + getName() + "' but got: " + name));

      String descriptorType = (String)inDescriptor.getFieldValue(ModelMBeanConstants.DESCRIPTOR_TYPE);
      if (ModelMBeanConstants.NOTIFICATION_DESCRIPTOR.equalsIgnoreCase(descriptorType) == false)
         throw new RuntimeOperationsException (new IllegalArgumentException("Invalid descriptorType, for notification '" + name + "' expected 'notification' but got: " + descriptorType));

      return true;
   }

   // Cloneable implementation --------------------------------------
   public Object clone()
   {
      ModelMBeanNotificationInfo clone = (ModelMBeanNotificationInfo) super.clone();
      clone.descriptor = (Descriptor) this.descriptor.clone();
      return clone;
   }

   // Object overrides ----------------------------------------------
   public String toString()
   {
      // FIXME: human readable string
      return super.toString();
   }

   // Private -------------------------------------------------------

   /**
    * The default descriptor contains the name, descriptorType, displayName and severity(=5) fields.
    */
   private Descriptor createDefaultDescriptor()
   {
      DescriptorSupport descr = new DescriptorSupport();
      descr.setField(ModelMBeanConstants.NAME, getName());
      descr.setField(ModelMBeanConstants.DISPLAY_NAME, getName());
      descr.setField(ModelMBeanConstants.SEVERITY, ModelMBeanConstants.SEVERITY_WARNING);
      descr.setField(ModelMBeanConstants.DESCRIPTOR_TYPE, ModelMBeanConstants.NOTIFICATION_DESCRIPTOR);
      return descr;
   }

   private void readObject(ObjectInputStream ois)
      throws IOException, ClassNotFoundException
   {
      ObjectInputStream.GetField getField = ois.readFields();
      switch (Serialization.version)
      {
      case Serialization.V1R0:
         descriptor = (Descriptor) getField.get("ntfyDescriptor", null);
         break;
      default:
         descriptor = (Descriptor) getField.get("notificationDescriptor", null);
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
         putField.put("ntfyDescriptor", descriptor);
         break;
      default:
         putField.put("notificationDescriptor", descriptor);
      }
      oos.writeFields();
   }
}




