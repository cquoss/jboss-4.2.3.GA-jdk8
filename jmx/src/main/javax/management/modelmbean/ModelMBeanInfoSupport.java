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
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.Descriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.RuntimeOperationsException;

import org.jboss.mx.modelmbean.ModelMBeanConstants;
import org.jboss.mx.util.Serialization;

/**
 * Support class for <tt>ModelMBeanInfo</tt> interface.
 *
 * @see javax.management.modelmbean.ModelMBeanInfo
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>.
 * @author  <a href="mailto:thomas.diesler@jboss.com">Thomas Diesler</a>.
 * @version $Revision: 57200 $
 *
 */
public class ModelMBeanInfoSupport
      extends MBeanInfo
      implements ModelMBeanInfo, Serializable
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   /**
    * MBean descriptor for this Model MBean.
    */
   private Descriptor mbeanDescriptor  = null;

   // Static --------------------------------------------------------

   private static final long serialVersionUID;
   private static final ObjectStreamField[] serialPersistentFields;

   private ModelMBeanAttributeInfo[] modelAttributes = null;
   private ModelMBeanConstructorInfo[] modelConstructors = null;
   private ModelMBeanOperationInfo[] modelOperations = null;
   private ModelMBeanNotificationInfo[] modelNotifications = null;

   static
   {
      switch (Serialization.version)
      {
      case Serialization.V1R0:
         serialVersionUID = -3944083498453227709L;
         // REVIEW: This is not in the spec, constructed from exceptions in testing
         serialPersistentFields = new ObjectStreamField[]
         {
            new ObjectStreamField("mmbAttributes", new MBeanAttributeInfo[0].getClass()),
            new ObjectStreamField("mmbConstructors", new MBeanConstructorInfo[0].getClass()),
            new ObjectStreamField("mmbNotifications", new MBeanNotificationInfo[0].getClass()),
            new ObjectStreamField("mmbOperations", new MBeanOperationInfo[0].getClass()),
            new ObjectStreamField("modelMBeanDescriptor", Descriptor.class)
         };
         break;
      default:
         serialVersionUID = -1935722590756516193L;
         serialPersistentFields = new ObjectStreamField[]
         {
            new ObjectStreamField("modelMBeanAttributes", new MBeanAttributeInfo[0].getClass()),
            new ObjectStreamField("modelMBeanConstructors", new MBeanConstructorInfo[0].getClass()),
            new ObjectStreamField("modelMBeanNotifications", new MBeanNotificationInfo[0].getClass()),
            new ObjectStreamField("modelMBeanOperations", new MBeanOperationInfo[0].getClass()),
            new ObjectStreamField("modelMBeanDescriptor", Descriptor.class)
         };
      }
   }
   
   // Constructors --------------------------------------------------
   
   /**
    * Copy constructor for Model MBean info. This instance is initialized with
    * the values of the given Model MBean info.
    *
    * @param   mbi  Model MBean info used to initialize this instance
    */
   public ModelMBeanInfoSupport(ModelMBeanInfo mbi)
   {
      super(mbi.getClassName(), mbi.getDescription(), mbi.getAttributes(),
            mbi.getConstructors(), mbi.getOperations(), mbi.getNotifications());
      modelAttributes = (ModelMBeanAttributeInfo[]) mbi.getAttributes();
      modelConstructors = (ModelMBeanConstructorInfo[]) mbi.getConstructors();
      modelOperations = (ModelMBeanOperationInfo[]) mbi.getOperations();
      modelNotifications = (ModelMBeanNotificationInfo[]) mbi.getNotifications();
      try
      {
         setMBeanDescriptor(mbi.getMBeanDescriptor());
      }
      catch (MBeanException e)
      {
         throw new RuntimeException("Cannot set MBean descriptor", e);
      }
   }

   /**
    * Creates an instance of Model MBean info implementation based on the given
    * values. The Model MBean is configured with a default MBean descriptor.
    *
    * @param   className    name of the Model MBean implementation class
    * @param   description  human readable description string for this Model MBean
    * @param   modelAttributes   an array of Model MBean attribute metadata to describe
    *                       the management modelAttributes of this Model MBean
    * @param   modelConstructors an array of Model MBean constructor metadata that
    *                       describes the modelConstructors of this Model MBean
    *                       implementation class
    * @param   modelOperations   an array of Model MBean operation metadata to describe
    *                       the management modelOperations of this Model MBean
    * @param   modelNotifications an array of Model MBean notification metadata to
    *                        describe the management modelNotifications of this 
    *                        Model MBean
    */
   public ModelMBeanInfoSupport(String className, String description,
                                ModelMBeanAttributeInfo[] modelAttributes,
                                ModelMBeanConstructorInfo[] modelConstructors,
                                ModelMBeanOperationInfo[] modelOperations,
                                ModelMBeanNotificationInfo[] modelNotifications)
   {
      super(className, description,
            (null == modelAttributes) ? new ModelMBeanAttributeInfo[0] : modelAttributes,
            (null == modelConstructors) ? new ModelMBeanConstructorInfo[0] : modelConstructors,
            (null == modelOperations) ? new ModelMBeanOperationInfo[0] : modelOperations,
            (null == modelNotifications) ? new ModelMBeanNotificationInfo[0] : modelNotifications);

      this.modelAttributes = (ModelMBeanAttributeInfo[]) super.getAttributes();
      this.modelConstructors = (ModelMBeanConstructorInfo[]) super.getConstructors();
      this.modelOperations = (ModelMBeanOperationInfo[]) super.getOperations();
      this.modelNotifications = (ModelMBeanNotificationInfo[]) super.getNotifications();
      try
      {
         setMBeanDescriptor(createDefaultDescriptor());
      }
      catch(MBeanException e)
      {
         throw new RuntimeException("Cannot set MBean descriptor", e);
      }
   }

   /**
    * Creates an instance of Model MBean info implementation based on the given
    * values and descriptor.
    *
    * @param   className    name of the Model MBean implementation class
    * @param   description  human readable description string for this Model MBean
    * @param   modelAttributes   an array of Model MBean attribute metadata to describe
    *                       the management modelAttributes of this Model MBean
    * @param   modelConstructors an array of Model MBean constructor metadata that
    *                       describes the modelConstructors of this Model MBean
    *                       implementation class
    * @param   modelOperations   an array of Model MBean operation metadata to describe
    *                       the management modelOperations of this Model MBean
    * @param   modelNotifications an array of Model MBean notification metadata to
    *                        describe the management modelNotifications of this 
    *                        Model MBean
    * @param   mbeandescriptor descriptor for the MBean
    */
   public ModelMBeanInfoSupport(String className, String description,
      ModelMBeanAttributeInfo[] modelAttributes,
      ModelMBeanConstructorInfo[] modelConstructors,
      ModelMBeanOperationInfo[] modelOperations,
      ModelMBeanNotificationInfo[] modelNotifications, Descriptor mbeandescriptor)
           throws RuntimeOperationsException
   {
      super(className, description,
            (null == modelAttributes) ? new ModelMBeanAttributeInfo[0] : modelAttributes,
            (null == modelConstructors) ? new ModelMBeanConstructorInfo[0] : modelConstructors,
            (null == modelOperations) ? new ModelMBeanOperationInfo[0] : modelOperations,
            (null == modelNotifications) ? new ModelMBeanNotificationInfo[0] : modelNotifications);
      this.modelAttributes = (ModelMBeanAttributeInfo[]) super.getAttributes();
      this.modelConstructors = (ModelMBeanConstructorInfo[]) super.getConstructors();
      this.modelOperations = (ModelMBeanOperationInfo[]) super.getOperations();
      this.modelNotifications = (ModelMBeanNotificationInfo[]) super.getNotifications();
      try
      {
         setMBeanDescriptor(mbeandescriptor);
      }
      catch(MBeanException e)
      {
         throw new RuntimeException("Cannot set MBean descriptor", e);
      }
   }


   // ModelMBeanInfo interface implementation -----------------------
   
   /**
    * Returns the descriptors of an Model MBean for a given management
    * interface element type. The descriptor type must be one of the following:  <br><pre>
    *
    *   - {@link org.jboss.mx.modelmbean.ModelMBeanConstants#MBEAN_DESCRIPTOR MBEAN_DESCRIPTOR}
    *   - {@link org.jboss.mx.modelmbean.ModelMBeanConstants#ATTRIBUTE_DESCRIPTOR ATTRIBUTE_DESCRIPTOR}
    *   - {@link org.jboss.mx.modelmbean.ModelMBeanConstants#OPERATION_DESCRIPTOR OPERATION_DESCRIPTOR}
    *   - {@link org.jboss.mx.modelmbean.ModelMBeanConstants#NOTIFICATION_DESCRIPTOR NOTIFICATION_DESCRIPTOR}
    *   - {@link org.jboss.mx.modelmbean.ModelMBeanConstants#CONSTRUCTOR_DESCRIPTOR CONSTRUCTOR_DESCRIPTOR}
    *   - {@link org.jboss.mx.modelmbean.ModelMBeanConstants#ALL_DESCRIPTORS ALL_DESCRIPTORS}
    *
    * </pre>
    * 
    * Using <tt>ALL_DESCRIPTORS</tt> returns descriptors for the MBean, and all
    * its modelAttributes, modelOperations, modelNotifications and modelConstructors.
    *
    * @param   descrType   descriptor type string
    * 
    * @return  MBean descriptors.
    */
   public Descriptor[] getDescriptors(String descrType) throws MBeanException
   {
      if (descrType == null)
      {
         List list = new ArrayList(100);
         list.add(mbeanDescriptor);
         list.addAll(getAttributeDescriptors().values());
         list.addAll(getOperationDescriptors().values());
         list.addAll(getNotificationDescriptors().values());
         list.addAll(getConstructorDescriptors().values());
         return (Descriptor[])list.toArray(new Descriptor[0]);
      }

      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.MBEAN_DESCRIPTOR))
         return new Descriptor[] { mbeanDescriptor };

      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.ATTRIBUTE_DESCRIPTOR))
         return (Descriptor[])getAttributeDescriptors().values().toArray(new Descriptor[0]);

      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.OPERATION_DESCRIPTOR))
         return (Descriptor[])getOperationDescriptors().values().toArray(new Descriptor[0]);

      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.NOTIFICATION_DESCRIPTOR))
         return (Descriptor[])getNotificationDescriptors().values().toArray(new Descriptor[0]);

      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.CONSTRUCTOR_DESCRIPTOR))
         return (Descriptor[])getConstructorDescriptors().values().toArray(new Descriptor[0]);

      throw new IllegalArgumentException("unknown descriptor type: " + descrType);
   }

   /**
    * Returns a descriptor of a management interface element matching the given
    * name and type. The descriptor type string must be one of the following:   <br><pre>
    *
    *   - {@link org.jboss.mx.modelmbean.ModelMBeanConstants#MBEAN_DESCRIPTOR MBEAN_DESCRIPTOR}
    *   - {@link org.jboss.mx.modelmbean.ModelMBeanConstants#ATTRIBUTE_DESCRIPTOR ATTRIBUTE_DESCRIPTOR}
    *   - {@link org.jboss.mx.modelmbean.ModelMBeanConstants#OPERATION_DESCRIPTOR OPERATION_DESCRIPTOR}
    *   - {@link org.jboss.mx.modelmbean.ModelMBeanConstants#NOTIFICATION_DESCRIPTOR NOTIFICATION_DESCRIPTOR}
    *   - {@link org.jboss.mx.modelmbean.ModelMBeanConstants#CONSTRUCTOR_DESCRIPTOR CONSTRUCTOR_DESCRIPTOR}
    *
    * </pre>
    *
    * @param   descrName   name of the descriptor
    * @param   descrType   type of the descriptor
    *
    * @return  the requested descriptor or <tt>null</tt> if it was not found
    *
    * @throws  RuntimeOperationsException if an illegal descriptor type was given 
    */
   public Descriptor getDescriptor(String descrName, String descrType) throws MBeanException
   {
      if (descrType == null)
         throw new RuntimeOperationsException(new IllegalArgumentException("null descriptor type"));

      if (descrType.equalsIgnoreCase(ModelMBeanConstants.MBEAN_DESCRIPTOR))
         return mbeanDescriptor;
      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.ATTRIBUTE_DESCRIPTOR))
         return getAttributeDescriptor(descrName);
      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.OPERATION_DESCRIPTOR))
         return getOperationDescriptor(descrName);
      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.CONSTRUCTOR_DESCRIPTOR))
         return getConstructorDescriptor(descrName);
      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.NOTIFICATION_DESCRIPTOR))
         return getNotificationDescriptor(descrName);

      throw new RuntimeOperationsException(new IllegalArgumentException("unknown descriptor type: " + descrType));
   }


   /**
    * Adds or replaces the descriptors in this Model MBean. All descriptors
    * must be valid. <tt>Null</tt> references will be ignored.
    *
    * @param   inDescriptors  array of descriptors
    */
   public void setDescriptors(Descriptor[] inDescriptors) throws MBeanException
   {
      for (int i = 0; i < inDescriptors.length; ++i)
      {
         if (inDescriptors[i] != null && inDescriptors[i].isValid())
         {
            setDescriptor(
                  inDescriptors[i],  
                  (String)inDescriptors[i].getFieldValue(ModelMBeanConstants.DESCRIPTOR_TYPE)
            );
         }
      }
   }
   
   /**
    * Adds or replaces the descriptor in this Model MBean. Descriptor must be
    * valid. If <tt>descrType</tt> is not specified, the <tt>descriptorType</tt>
    * field of the given descriptor is used.   <p>
    *
    * The <tt>descriptorType</tt> must contain one of the following values:   <br><pre>
    *
    *   - {@link org.jboss.mx.modelmbean.ModelMBeanConstants#MBEAN_DESCRIPTOR MBEAN_DESCRIPTOR}
    *   - {@link org.jboss.mx.modelmbean.ModelMBeanConstants#ATTRIBUTE_DESCRIPTOR ATTRIBUTE_DESCRIPTOR}
    *   - {@link org.jboss.mx.modelmbean.ModelMBeanConstants#OPERATION_DESCRIPTOR OPERATION_DESCRIPTOR}
    *   - {@link org.jboss.mx.modelmbean.ModelMBeanConstants#NOTIFICATION_DESCRIPTOR NOTIFICATION_DESCRIPTOR}
    *   - {@link org.jboss.mx.modelmbean.ModelMBeanConstants#CONSTRUCTOR_DESCRIPTOR CONSTRUCTOR_DESCRIPTOR}
    *
    * </pre>
    *
    * @param   descr     descriptor to set
    * @param   descrType descriptor type string, can be <tt>null</tt>
    *
    * @throws RuntimeOperationsException if <tt>descr</tt> is <tt>null</tt>, or
    *         descriptor is not valid.
    */
   public void setDescriptor(Descriptor descr, String descrType) throws MBeanException
   {
      if (descr == null)
         throw new RuntimeOperationsException(new IllegalArgumentException("null descriptor"));
         
      if (!descr.isValid())
         throw new RuntimeOperationsException(new IllegalArgumentException("not a valid descriptor"));
         
      if (descrType == null)
         descrType = (String)descr.getFieldValue(ModelMBeanConstants.DESCRIPTOR_TYPE);
         
      if (descrType.equalsIgnoreCase(ModelMBeanConstants.MBEAN_DESCRIPTOR))
      {
         setMBeanDescriptor(descr);
      }
      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.ATTRIBUTE_DESCRIPTOR))
      {
         ModelMBeanAttributeInfo info = getAttribute((String)descr.getFieldValue(ModelMBeanConstants.NAME));
         info.setDescriptor(descr);
      }
      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.OPERATION_DESCRIPTOR))
      {
         ModelMBeanOperationInfo info = getOperation((String)descr.getFieldValue(ModelMBeanConstants.NAME));
         info.setDescriptor(descr);
      }
      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.CONSTRUCTOR_DESCRIPTOR))
      {
         ModelMBeanConstructorInfo info = getConstructor((String)descr.getFieldValue(ModelMBeanConstants.NAME));
         info.setDescriptor(descr);
      }
      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.NOTIFICATION_DESCRIPTOR))
      {
         ModelMBeanNotificationInfo info = getNotification((String)descr.getFieldValue(ModelMBeanConstants.NAME));
         info.setDescriptor(descr);
      }
      else
         throw new RuntimeOperationsException(new IllegalArgumentException("unknown descriptor type: " + descrType));
   }

   /**
    * Returns the attribute info for the named attribute, or null if there is none.
    */
   public ModelMBeanAttributeInfo getAttribute(String inName) throws MBeanException
   {
      if (inName == null)
         throw new RuntimeOperationsException(new IllegalArgumentException("Null attribute name"));

      for (int i = 0; i < modelAttributes.length; ++i)
      {
         if (modelAttributes[i].getName().equals(inName))
            return modelAttributes[i];
      }

      return null;
   }

   /**
    * Returns the operation info for the named attribute, or null if there is none.
    */
   public ModelMBeanOperationInfo getOperation(String inName) throws MBeanException
   {
      if (inName == null)
         throw new RuntimeOperationsException(new IllegalArgumentException("Null operation name"));

      for (int i = 0; i < modelOperations.length; ++i)
         if (modelOperations[i].getName().equals(inName))
            return modelOperations[i];

      return null;
   }

   /**
    * Returns the constructor info for the named attribute, or null if there is none.
    */
   public ModelMBeanConstructorInfo getConstructor(String inName) throws MBeanException
   {
      if (inName == null)
         throw new RuntimeOperationsException(new IllegalArgumentException("Null constructor name"));

      for (int i = 0; i < modelConstructors.length; ++i)
         if (modelConstructors[i].getName().equals(inName))
            return modelConstructors[i];

      return null;
   }

   /**
    * Returns the attribute info for the named attribute, or null if there is none.
    */
   public ModelMBeanNotificationInfo getNotification(String inName) throws MBeanException
   {
      if (inName == null)
         throw new RuntimeOperationsException(new IllegalArgumentException("Null notification name"));

      for (int i = 0; i < modelNotifications.length; ++i)
         if (modelNotifications[i].getName().equals(inName))
            return modelNotifications[i];

      return null;
   }

   public MBeanAttributeInfo[] getAttributes()
   {
      return super.getAttributes();
   }

   public MBeanOperationInfo[] getOperations()
   {
      return super.getOperations();
   }

   public MBeanConstructorInfo[] getConstructors()
   {
      return super.getConstructors();
   }

   public MBeanNotificationInfo[] getNotifications()
   {
      return super.getNotifications();
   }

   public Descriptor getMBeanDescriptor() throws MBeanException
   {
      return mbeanDescriptor;
   }

   /**
    * Sets the ModelMBean's descriptor. This descriptor contains default, MBean wide metadata about the MBean and
    * default policies for persistence and caching. This operation does a complete replacement of the descriptor,
    * no merging is done.
    *
    * If the descriptor to set to is null then the default descriptor will be created.
    *
    * The default descriptor is: name=mbeanName,descriptorType=mbean, displayName=this.getClassName(), persistPolicy=never,log=F,export=F,visiblity=1
    * If the descriptor does not contain all these fields, they will be added with these default values.
    * See getMBeanDescriptor method javadoc for description of valid field names.
    */
   public void setMBeanDescriptor(Descriptor inDescriptor)
      throws MBeanException, RuntimeOperationsException
   {
      if (inDescriptor == null)
         inDescriptor = createDefaultDescriptor();

      if (inDescriptor.isValid() && isMBeanDescriptorValid(inDescriptor))
      {
         addDefaultMBeanDescriptorFields(inDescriptor);
         this.mbeanDescriptor = inDescriptor;
      }
   }

   /**
    * Validate the descriptor in the context of an attribute
    */
   private boolean isMBeanDescriptorValid(Descriptor inDescriptor)
   {
      String name = (String)inDescriptor.getFieldValue(ModelMBeanConstants.NAME);
      if (name == null)
         throw new RuntimeOperationsException (new IllegalArgumentException("Invalid null name"));

      String descriptorType = (String)inDescriptor.getFieldValue(ModelMBeanConstants.DESCRIPTOR_TYPE);
      if (ModelMBeanConstants.MBEAN_DESCRIPTOR.equalsIgnoreCase(descriptorType) == false)
         throw new RuntimeOperationsException (new IllegalArgumentException("Invalid descriptorType, for mbean '" + name + "' expected 'MBean' but got: " + descriptorType));

      return true;
   }

   // Public --------------------------------------------------------
   
   /**
    * @deprecated use {@link #getDescriptor(String, String)} instead.
    */
   public Descriptor getDescriptor(String descrName) throws MBeanException
   {
      
      /*
       * NOTE:  this method is not part of the ModelMBeanInfo interface but is
       *        included in the RI javadocs so it is also here for the sake
       *        of completeness. The problem here though is that this method
       *        to work without the descriptor type string assumes unique name
       *        for all descriptors regardless their type (something that is
       *        not mandated by the spec). Hence the deprecated tag.   [JPL]
       */
      if (descrName.equals(mbeanDescriptor.getFieldValue(ModelMBeanConstants.NAME)))
         return mbeanDescriptor;

      Descriptor descr = null;

      descr = (Descriptor)getAttributeDescriptors().get(descrName);
      if (descr != null)
         return descr;

      descr = (Descriptor)getOperationDescriptors().get(descrName);
      if (descr != null)
         return descr;

      descr = (Descriptor)getNotificationDescriptors().get(descrName);
      if (descr != null)
         return descr;

      descr = (Descriptor)getConstructorDescriptors().get(descrName);
      if (descr != null)
         return descr;

      return null;
   }

   
   // Y overrides ---------------------------------------------------
   public synchronized Object clone()
   {
      ModelMBeanInfoSupport clone = (ModelMBeanInfoSupport)super.clone();
      clone.mbeanDescriptor = (Descriptor)mbeanDescriptor.clone();
      return clone;
   }

   // Private -------------------------------------------------------
   private void addDefaultMBeanDescriptorFields(Descriptor descr)
   {
      if (descr.getFieldValue(ModelMBeanConstants.NAME) == null || descr.getFieldValue(ModelMBeanConstants.NAME).equals(""))
         descr.setField(ModelMBeanConstants.NAME, getClassName());
      if (descr.getFieldValue(ModelMBeanConstants.DESCRIPTOR_TYPE) == null)
         descr.setField(ModelMBeanConstants.DESCRIPTOR_TYPE, ModelMBeanConstants.MBEAN_DESCRIPTOR);
      if (!(((String)descr.getFieldValue(ModelMBeanConstants.DESCRIPTOR_TYPE)).equalsIgnoreCase(ModelMBeanConstants.MBEAN_DESCRIPTOR)))
         descr.setField(ModelMBeanConstants.DESCRIPTOR_TYPE, ModelMBeanConstants.MBEAN_DESCRIPTOR);
      if (descr.getFieldValue(ModelMBeanConstants.DISPLAY_NAME) == null)
         descr.setField(ModelMBeanConstants.DISPLAY_NAME, getClassName());
      if (descr.getFieldValue(ModelMBeanConstants.PERSIST_POLICY) == null)
         descr.setField(ModelMBeanConstants.PERSIST_POLICY, ModelMBeanConstants.PP_NEVER);
      if (descr.getFieldValue(ModelMBeanConstants.LOG) == null)
         descr.setField(ModelMBeanConstants.LOG, "F");
      if (descr.getFieldValue(ModelMBeanConstants.VISIBILITY) == null)
         descr.setField(ModelMBeanConstants.VISIBILITY, ModelMBeanConstants.HIGH_VISIBILITY);
   }

   /**
    * The default descriptor is:
    * name=mbeanName
    * descriptorType=mbean
    * displayName=this.getClassName()
    * persistPolicy=never
    * log=F
    * visiblity=1
    */
   private Descriptor createDefaultDescriptor()
   {
      return new DescriptorSupport(new String[] {
            ModelMBeanConstants.NAME            + "=" + getClassName() ,
            ModelMBeanConstants.DESCRIPTOR_TYPE + "=" + ModelMBeanConstants.MBEAN_DESCRIPTOR,
            ModelMBeanConstants.DISPLAY_NAME    + "=" + getClassName(),
            ModelMBeanConstants.PERSIST_POLICY  + "=" + ModelMBeanConstants.PP_NEVER,
            ModelMBeanConstants.LOG             + "=" + "F",
            ModelMBeanConstants.VISIBILITY      + "=" + ModelMBeanConstants.HIGH_VISIBILITY
      });
   }

   private Map getAttributeDescriptors()
   {
      Map map = new HashMap();
      for (int i = 0; i < modelAttributes.length; ++i)
         map.put(modelAttributes[i].getName(), (modelAttributes[i]).getDescriptor());
      return map;
   }

   private Descriptor getAttributeDescriptor(String descrName)
   {
      for (int i = 0; i < modelAttributes.length; ++i)
         if (modelAttributes[i].getName().equals(descrName))
            return modelAttributes[i].getDescriptor();
      return null;
   }
   
   private Map getOperationDescriptors()
   {
      Map map = new HashMap();
      for (int i = 0; i < modelOperations.length; ++i)
         map.put(modelOperations[i].getName(), (modelOperations[i]).getDescriptor());
      return map;
   }

   private Descriptor getOperationDescriptor(String descrName)
   {
      for (int i = 0; i < modelOperations.length; ++i)
         if (modelOperations[i].getName().equals(descrName))
            return modelOperations[i].getDescriptor();
      return null;
   }

   private Map getConstructorDescriptors()
   {
      Map map = new HashMap();
      for (int i = 0; i < modelConstructors.length; ++i)
         map.put(modelConstructors[i].getName(), (modelConstructors[i]).getDescriptor());
      return map;
   }

   private Descriptor getConstructorDescriptor(String descrName)
   {
      for (int i = 0; i < modelConstructors.length; ++i)
         if (modelConstructors[i].getName().equals(descrName))
            return modelConstructors[i].getDescriptor();
      return null;
   }

   private Map getNotificationDescriptors()
   {
      Map map = new HashMap();
      for (int i = 0; i < modelNotifications.length; ++i)
         map.put(modelNotifications[i].getName(), (modelNotifications[i]).getDescriptor());
      return map;
   }

   private Descriptor getNotificationDescriptor(String descrName)
   {
      for (int i = 0; i < modelNotifications.length; ++i)
         if (modelNotifications[i].getName().equals(descrName))
            return modelNotifications[i].getDescriptor();
      return null;
   }

   private void readObject(ObjectInputStream ois)
      throws IOException, ClassNotFoundException
   {
      ModelMBeanAttributeInfo[] attrInfo;
      ModelMBeanConstructorInfo[] consInfo;
      ModelMBeanOperationInfo[] operInfo;
      ModelMBeanNotificationInfo[] notifyInfo;
      Descriptor desc;

      ObjectInputStream.GetField getField = ois.readFields();
      switch (Serialization.version)
      {
      case Serialization.V1R0:
         attrInfo = (ModelMBeanAttributeInfo[]) getField.get("mmbAttributes", null);
         consInfo = (ModelMBeanConstructorInfo[]) getField.get("mmbConstructors", null);
         notifyInfo = (ModelMBeanNotificationInfo[]) getField.get("mmbNotifications", null);
         operInfo = (ModelMBeanOperationInfo[]) getField.get("mmbOperations", null);
         break;
      default:
         attrInfo = (ModelMBeanAttributeInfo[]) getField.get("modelMBeanAttributes", null);
         consInfo = (ModelMBeanConstructorInfo[]) getField.get("modelMBeanConstructors", null);
         notifyInfo = (ModelMBeanNotificationInfo[]) getField.get("modelMBeanNotifications", null);
         operInfo = (ModelMBeanOperationInfo[]) getField.get("modelMBeanOperations", null);
      }
      desc = (Descriptor) getField.get("modelMBeanDescriptor", null);
      if (desc == null)
         throw new StreamCorruptedException("Null descriptor?");
      this.modelAttributes = (null == attrInfo) ? new ModelMBeanAttributeInfo[0]
         : attrInfo;
      this.modelConstructors = (null == consInfo) ? new ModelMBeanConstructorInfo[0]
         : consInfo;
      this.modelOperations = (null == operInfo) ? new ModelMBeanOperationInfo[0]
         : operInfo;
      this.modelNotifications = (null == notifyInfo) ? new ModelMBeanNotificationInfo[0]
         : notifyInfo;

      try
      {
         setMBeanDescriptor(createDefaultDescriptor());
      }
      catch(MBeanException ignore)
      {
      }
   }

   private void writeObject(ObjectOutputStream oos)
      throws IOException
   {
      ObjectOutputStream.PutField putField = oos.putFields();
      switch (Serialization.version)
      {
      case Serialization.V1R0:
         putField.put("mmbAttributes", modelAttributes);
         putField.put("mmbConstructors", modelConstructors);
         putField.put("mmbNotifications", modelNotifications);
         putField.put("mmbOperations", modelOperations);
         break;
      default:
         putField.put("modelMBeanAttributes", modelAttributes);
         putField.put("modelMBeanConstructors", modelConstructors);
         putField.put("modelMBeanNotifications", modelNotifications);
         putField.put("modelMBeanOperations", modelOperations);
      }
      putField.put("modelMBeanDescriptor", mbeanDescriptor);
      oos.writeFields();
   }
}
