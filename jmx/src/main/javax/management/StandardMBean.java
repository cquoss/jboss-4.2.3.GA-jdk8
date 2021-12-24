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

import org.jboss.logging.Logger;
import org.jboss.mx.metadata.StandardMetaData;
import org.jboss.mx.server.ExceptionHandler;
import org.jboss.mx.loading.LoaderRepository;

import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * A helper class to allow standard mbeans greater control over their
 * management interface.<p>
 *
 * Extending this class actually makes the mbean a dynamic mbean, but
 * with the convenience of a standard mbean.
 *
 * @todo make this more dynamic, somehow delegate to an XMBean?
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @author  <a href="mailto:thomas.diesler@jboss.com">Thomas Diesler</a>.
 * @version $Revision: 57200 $
 */
public class StandardMBean
   implements DynamicMBean
{
   // Constants ---------------------------------------------------

   private static final Logger log = Logger.getLogger(StandardMBean.class);

   // Attributes --------------------------------------------------

   /**
    * The implementation object
    */
   private Object implementation;

   /**
    * The management interface
    */
   private Class mbeanInterface;

   /**
    * The cached mbeaninfo
    */
   private MBeanInfo cachedMBeanInfo;

   // Static  -----------------------------------------------------

   // Constructors ------------------------------------------------

   /**
    * Construct a DynamicMBean from the given implementation object
    * and the passed management interface class.
    *
    * @param implementation the object implementing the mbean
    * @param mbeanInterface the management interface of the mbean
    * @exception IllegalArgumentException for a null implementation
    * @exception NotCompliantMBeanException if the management interface
    *            does not follow the JMX design patterns or the implementation
    *            does not implement the interface
    */
   public StandardMBean(Object implementation, Class mbeanInterface)
      throws NotCompliantMBeanException
   {
      this.implementation = implementation;
      this.mbeanInterface = mbeanInterface;
      MBeanInfo info = buildMBeanInfo(implementation, mbeanInterface);
      cacheMBeanInfo(info);
   }

   /**
    * Construct a DynamicMBean from this object
    * and the passed management interface class.<p>
    *
    * Used in subclassing
    *
    * @param mbeanInterface the management interface of the mbean
    * @exception NotCompliantMBeanException if the management interface
    *            does not follow the JMX design patterns or this
    *            does not implement the interface
    */
   protected StandardMBean(Class mbeanInterface)
      throws NotCompliantMBeanException
   {
      this.implementation = this;
      this.mbeanInterface = mbeanInterface;
      MBeanInfo info = buildMBeanInfo(implementation, mbeanInterface);
      cacheMBeanInfo(info);
   }

   // Public ------------------------------------------------------

   /**
    * Retrieve the implementation object
    *
    * @return the implementation
    */
   public Object getImplementation()
   {
      return implementation;
   }

   /**
    * Replace the implementation object
    *
    * @todo make this work after the mbean is registered
    * @param implementation the new implementation
    * @exception IllegalArgumentException for a null parameter
    * @exception NotCompliantMBeanException if the new implementation
    *            does not implement the interface supplied at
    *            construction
    */
   public void setImplementation(Object implementation) 
        throws NotCompliantMBeanException
   {
      if (implementation == null)
         throw new IllegalArgumentException("Null implementation");
      this.implementation = implementation;
   }

   /**
    * Retrieve the implementation class
    *
    * @return the class of the implementation
    */
   public Class getImplementationClass()
   {
      return implementation.getClass();
   }

   /**
    * Retrieve the management interface
    *
    * @return the management interface
    */
   public final Class getMBeanInterface()
   {
      return mbeanInterface;
   }

   // DynamicMBean Implementation ---------------------------------

   public Object getAttribute(String attribute)
      throws AttributeNotFoundException, MBeanException, ReflectionException
   {
      try
      {
         Method method = implementation.getClass().getMethod("get" + attribute, null);
         return method.invoke(implementation, new Object[0]);
      }
      catch (Exception e)
      {
         JMException result = ExceptionHandler.handleException(e);
         if (result instanceof AttributeNotFoundException)
            throw (AttributeNotFoundException)result;
         if (result instanceof MBeanException)
            throw (MBeanException)result;
         if (result instanceof ReflectionException)
            throw (ReflectionException)result;
         throw new MBeanException(e, "Cannot get attribute: " + attribute);
      }
   }

   public void setAttribute(Attribute attribute)
      throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
   {
      try
      {
         Class[] clArr = null;
         if (attribute.getValue() != null)
         {
            clArr = new Class[]{attribute.getValue().getClass()};
         }
         Method method = implementation.getClass().getMethod("set" + attribute.getName(), clArr);
         method.invoke(implementation, new Object[]{attribute.getValue()});
      }
      catch (Exception e)
      {
         JMException result = ExceptionHandler.handleException(e);
         if (result instanceof AttributeNotFoundException)
            throw (AttributeNotFoundException)result;
         if (result instanceof InvalidAttributeValueException)
            throw (InvalidAttributeValueException)result;
         if (result instanceof MBeanException)
            throw (MBeanException)result;
         if (result instanceof ReflectionException)
            throw (ReflectionException)result;
         throw new MBeanException(e, "Cannot set attribute: " + attribute);
      }
   }

   public AttributeList getAttributes(String[] attributes)
   {
      try
      {
         AttributeList attrList = new AttributeList(attributes.length);
         for (int i = 0; i < attributes.length; i++)
         {
            String name = attributes[i];
            Object value = getAttribute(name);
            attrList.add(new Attribute(name, value));
         }
         return attrList;
      }
      catch (Exception e)
      {
         JMException result = ExceptionHandler.handleException(e);
         // Why is this not throwing the same exceptions as getAttribute(String)
         throw new RuntimeException("Cannot get attributes", result);
      }
   }


   public AttributeList setAttributes(AttributeList attributes)
   {
      try
      {
         AttributeList attrList = new AttributeList(attributes.size());
         Iterator it = attributes.iterator();
         while (it.hasNext())
         {
            Attribute attr = (Attribute) it.next();
            setAttribute(attr);
            String name = attr.getName();
            Object value = getAttribute(name);
            attrList.add(new Attribute(name, value));
         }
         return attrList;
      }
      catch (Exception e)
      {
         JMException result = ExceptionHandler.handleException(e);
         // Why is this not throwing the same exceptions as setAttribute(Attribute)
         throw new RuntimeException("Cannot set attributes", result);
      }
   }

   public Object invoke(String actionName, Object[] params, String[] signature)
      throws MBeanException, ReflectionException
   {
      try
      {
         Class[] sigcl = new Class[signature.length];
         for (int i = 0; i < signature.length; i++)
         {
            sigcl[i] = loadClass(signature[i]);
         }
         Method method = implementation.getClass().getMethod(actionName, sigcl);
         return method.invoke(implementation, params);
      }
      catch (Exception e)
      {
         JMException result = ExceptionHandler.handleException(e);
         if (result instanceof MBeanException)
            throw (MBeanException)result;
         if (result instanceof ReflectionException)
            throw (ReflectionException)result;
         throw new MBeanException(e, "Cannot invoke: " + actionName);
      }
   }

   /**
    * Load a class from the classloader that loaded this MBean
    */
   private Class loadClass(String className) throws ClassNotFoundException
   {
      Class clazz = LoaderRepository.getNativeClassForName(className);
      if (clazz == null) {
         ClassLoader cl = getClass().getClassLoader();
         clazz = cl.loadClass(className);
      }
      return clazz;
   }

   public MBeanInfo getMBeanInfo()
   {
      MBeanInfo info = getCachedMBeanInfo();
      if (info == null)
      {
         try
         {
            info = buildMBeanInfo(implementation, mbeanInterface);
            cacheMBeanInfo(info);
         }
         catch (NotCompliantMBeanException e)
         {
            log.error("Unexcepted exception", e);
            throw new IllegalStateException("Unexcepted exception " + e.toString());
         }

      }
      return info;
   }

   // Y overrides -------------------------------------------------

   // Protected ---------------------------------------------------

   /**
    * Retrieve the class name of the mbean
    *
    * @param info the default mbeaninfo derived by reflection
    * @return the class name
    */
   protected String getClassName(MBeanInfo info)
   {
      return info.getClassName();
   }

   /**
    * Retrieve the description of the mbean
    *
    * @param info the default mbeaninfo derived by reflection
    * @return the description
    */
   protected String getDescription(MBeanInfo info)
   {
      return info.getDescription();
   }

   /**
    * Retrieve the description of the mbean feature
    *
    * @param info the default mbeanfeatureinfo derived by reflection
    * @return the description
    */
   protected String getDescription(MBeanFeatureInfo info)
   {
      return info.getDescription();
   }

   /**
    * Retrieve the description of the mbean attribute
    *
    * @param info the default mbeanattributeinfo derived by reflection
    * @return the description
    */
   protected String getDescription(MBeanAttributeInfo info)
   {
      return getDescription((MBeanFeatureInfo) info);
   }

   /**
    * Retrieve the description of the mbean constructor
    *
    * @param info the default mbeanconstructorinfo derived by reflection
    * @return the description
    */
   protected String getDescription(MBeanConstructorInfo info)
   {
      return getDescription((MBeanFeatureInfo) info);
   }

   /**
    * Retrieve the description of the mbean operation
    *
    * @param info the default mbeanoperationinfo derived by reflection
    * @return the description
    */
   protected String getDescription(MBeanOperationInfo info)
   {
      return getDescription((MBeanFeatureInfo) info);
   }

   /**
    * Retrieve the description of the mbean constructor parameter
    *
    * @param info the default mbeanconstructorinfo derived by reflection
    * @param param the parameter information
    * @param sequence the parameter index, starting with zero
    * @return the description
    */
   protected String getDescription(MBeanConstructorInfo info, MBeanParameterInfo param, int sequence)
   {
      return param.getDescription();
   }

   /**
    * Retrieve the description of the mbean operation parameter
    *
    * @param info the default mbeanoperationinfo derived by reflection
    * @param param the parameter information
    * @param sequence the parameter index, starting with zero
    * @return the description
    */
   protected String getDescription(MBeanOperationInfo info, MBeanParameterInfo param, int sequence)
   {
      return param.getDescription();
   }

   /**
    * Retrieve the parameter name for a constructor
    *
    * @param info the default mbeanconstructorinfo derived by reflection
    * @param param the parameter information
    * @param sequence the parameter index, starting with zero
    * @return the parameter name
    */
   protected String getParameterName(MBeanConstructorInfo info, MBeanParameterInfo param, int sequence)
   {
      return param.getName();
   }

   /**
    * Retrieve the parameter name for an operation
    *
    * @param info the default mbeanoperationinfo derived by reflection
    * @param param the parameter information
    * @param sequence the parameter index, starting with zero
    * @return the parameter name
    */
   protected String getParameterName(MBeanOperationInfo info, MBeanParameterInfo param, int sequence)
   {
      return param.getName();
   }

   /**
    * Retrieve the impact of the mbean operation
    *
    * @param info the default mbeanoperationinfo derived by reflection
    * @return the impact
    */
   protected int getImpact(MBeanOperationInfo info)
   {
      return info.getImpact();
   }

   /**
    * Retrieve the constructors 
    *
    * @param constructors the default constructors derived by reflection
    * @param implementation the implementation
    * @return the constructors if the implementation is this, otherwise null
    */
   protected MBeanConstructorInfo[] getConstructors(MBeanConstructorInfo[] constructors, Object implementation)
   {
      if (implementation == this)
         return constructors;
      else
         return null;
   }

   /**
    * Retrieve the cached mbean info
    *
    * @return the cached mbean info
    */
   protected MBeanInfo getCachedMBeanInfo()
   {
      return cachedMBeanInfo;
   }

   /**
    * Sets the cached mbean info
    *
    * @todo make this work after the mbean is registered
    * @param info the mbeaninfo to cache, can be null to erase the cache
    */
   protected void cacheMBeanInfo(MBeanInfo info)
   {
      cachedMBeanInfo = info;
   }

   // Package Private ---------------------------------------------

   // Private -----------------------------------------------------

   /**
    * Builds a default MBeanInfo for this MBean, using the Management Interface specified for this MBean.
    *
    * While building the MBeanInfo, this method calls the customization hooks that make it possible for subclasses to
    * supply their custom descriptions, parameter names, etc...
    */
   private final MBeanInfo buildMBeanInfo(Object implementation, Class mbeanInterface)
      throws NotCompliantMBeanException
   {
      if (implementation == null)
         throw new IllegalArgumentException("Null implementation");

      StandardMetaData metaData = new StandardMetaData(implementation, mbeanInterface);
      this.mbeanInterface = metaData.getMBeanInterface();
      MBeanInfo info = metaData.build();

      String className = getClassName(info);
      String mainDescription = getDescription(info);
      MBeanAttributeInfo[] attributes = info.getAttributes();
      MBeanConstructorInfo[] constructors = info.getConstructors();
      MBeanOperationInfo[] operations = info.getOperations();
      MBeanNotificationInfo[] notifications = info.getNotifications();

      for (int i = 0; i < attributes.length; i++)
      {
         MBeanAttributeInfo attribute = attributes[i];
         String description = getDescription(attribute);
         attributes[i] = new MBeanAttributeInfo(attribute.getName(), attribute.getType(),
                                                description, attribute.isReadable(),
                                                attribute.isWritable(), attribute.isIs());
      }

      // If this StandardMBean is a wrapper of another resource we don't expose constructors
      if (implementation == this)
      {
         constructors = getConstructors(constructors, this);
         for (int i = 0; i < constructors.length; i++)
         {
            MBeanConstructorInfo constructor = constructors[i];
            MBeanParameterInfo[] parameters = constructor.getSignature();
            for (int j = 0; j < parameters.length; j++)
            {
               MBeanParameterInfo param = parameters[j];
               String description = getDescription(constructor, param, j);
               String name = getParameterName(constructor, param, j);
               parameters[j] = new MBeanParameterInfo(name, param.getType(), description);
            }
            String description = getDescription(constructor);
            constructors[i] = new MBeanConstructorInfo(constructor.getName(), description, parameters);
         }
      }
      else
      {
         constructors = new MBeanConstructorInfo[0];
      }

      for (int i = 0; i < operations.length; i++)
      {
         MBeanOperationInfo operation = operations[i];
         MBeanParameterInfo[] parameters = operation.getSignature();
         for (int j = 0; j < parameters.length; j++)
         {
            MBeanParameterInfo param = parameters[j];
            String description = getDescription(operation, param, j);
            String name = getParameterName(operation, param, j);
            parameters[j] = new MBeanParameterInfo(name, param.getType(), description);
         }
         String description = getDescription(operation);
         int impact = getImpact(operation);
         operations[i] = new MBeanOperationInfo(operation.getName(), description, parameters,
                                                operation.getReturnType(), impact);
      }

      info = new MBeanInfo(className, mainDescription, attributes, constructors, operations, notifications);
      return info;
   }

   // Inner Classes -----------------------------------------------
}
