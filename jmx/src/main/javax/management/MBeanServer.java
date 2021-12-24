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

import java.io.ObjectInputStream;
import java.util.Set;

import javax.management.loading.ClassLoaderRepository;

/**
 * The interface used to access the MBean server instances.
 *
 * @see javax.management.MBeanServerFactory
 * @see javax.management.loading.ClassLoaderRepository
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 */
public interface MBeanServer
   extends MBeanServerConnection
{

   /**
    * Create an MBean registered using the given object name.<p>
    *
    * Uses the default contructor.
    *
    * @param className the class name of the mbean
    * @param name the object name for registration, can be null
    * @return an ObjectInstance describing the registration
    * @exception ReflectionException for class not found or an exception
    *            invoking the contructor
    * @exception InstanceAlreadyExistsException for an MBean already registered
    *            with the passed or generated ObjectName
    * @exception MBeanRegistrationException for any exception thrown by the
    *            MBean's preRegister
    * @exception MBeanException for any exception thrown by the MBean's constructor
    * @exception NotCompliantMBeanException if the class name does not correspond to
    *            a valid MBean
    * @exception RuntimeOperationsException wrapping an IllegalArgumentException for a
    *            null class name, the ObjectName could not be determined or it is a pattern
    */   
   public ObjectInstance createMBean(String className, ObjectName name) 
            throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException;
   
   /**
    * Create an MBean registered using the given object name.<p>
    *
    * The MBean is loaded using the passed classloader. Uses the default contructor.
    *
    * @param className the class name of the mbean
    * @param loaderName an MBean that implements a classloader
    * @param name the object name for registration, can be null
    * @return an ObjectInstance describing the registration
    * @exception ReflectionException for class not found or an exception
    *            invoking the contructor
    * @exception InstanceAlreadyExistsException for an MBean already registered
    *            with the passed or generated ObjectName
    * @exception MBeanRegistrationException for any exception thrown by the
    *            MBean's preRegister
    * @exception MBeanException for any exception thrown by the MBean's constructor
    * @exception InstanceNotFoundException if the loaderName is not a classloader registered
    *            in the MBeanServer
    * @exception NotCompliantMBeanException if the class name does not correspond to
    *            a valid MBean
    * @exception RuntimeOperationsException wrapping an IllegalArgumentException for a
    *            null class name, the ObjectName could not be determined or it is a pattern
    */   
   public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName)
            throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException;
   
   /**
    * Create an MBean registered using the given object name.<p>
    *
    * Uses the specified constructor.
    *
    * @param className the class name of the mbean
    * @param loaderName an MBean that implements a classloader
    * @param params the parameters for the constructor
    * @param signature the signature of the constructor
    * @return an ObjectInstance describing the registration
    * @exception ReflectionException for class not found or an exception
    *            invoking the contructor
    * @exception InstanceAlreadyExistsException for an MBean already registered
    *            with the passed or generated ObjectName
    * @exception MBeanRegistrationException for any exception thrown by the
    *            MBean's preRegister
    * @exception MBeanException for any exception thrown by the MBean's constructor
    * @exception NotCompliantMBeanException if the class name does not correspond to
    *            a valid MBean
    * @exception RuntimeOperationsException wrapping an IllegalArgumentException for a
    *            null class name, the ObjectName could not be determined or it is a pattern
    */   
   public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature)
            throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException;
            
   /**
    * Create an MBean registered using the given object name.<p>
    *
    * The MBean is loaded using the passed classloader. Uses the specified constructor.
    *
    * @param className the class name of the mbean
    * @param loaderName an MBean that implements a classloader
    * @param name the object name for registration, can be null
    * @param params the parameters for the constructor
    * @param signature the signature of the constructor
    * @return an ObjectInstance describing the registration
    * @exception ReflectionException for class not found or an exception
    *            invoking the contructor
    * @exception InstanceAlreadyExistsException for an MBean already registered
    *            with the passed or generated ObjectName
    * @exception MBeanRegistrationException for any exception thrown by the
    *            MBean's preRegister
    * @exception MBeanException for any exception thrown by the MBean's constructor
    * @exception InstanceNotFoundException if the loaderName is not a classloader registered
    *            in the MBeanServer
    * @exception NotCompliantMBeanException if the class name does not correspond to
    *            a valid MBean
    * @exception RuntimeOperationsException wrapping an IllegalArgumentException for a
    *            null class name, the ObjectName could not be determined or it is a pattern
    */   
   public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature)
            throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException;

   /**
    * Registers an mbean.
    *
    * @param object the mbean implementation to register
    * @param name the object name of the mbean to register
    * @exception InstanceAlreadyExistsException if the object name is already registered
    *            in the MBeanServer
    * @exception MBeanRegistrationException for any exception thrown by the
    *            MBean's preDeregister
    * @exception NotCompliantMBeanException if the class name does not correspond to
    *            a valid MBean
    * @exception RuntimeOperationsException wrapping an IllegalArgumentException for a
    *            null name, or trying to register a JMX implementation MBean
    */   
   public ObjectInstance registerMBean(Object object, ObjectName name) 
            throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException;

   /**
    * Unregisters an mbean.
    *
    * @param name the object name of the mbean to unregister
    * @exception InstanceNotFoundException if the mbean is not registered
    *            in the MBeanServer
    * @exception MBeanRegistrationException for any exception thrown by the
    *            MBean's preDeregister
    * @exception RuntimeOperationsException wrapping an IllegalArgumentException for a
    *            null name, or trying to unregister a JMX implementation MBean
    */   
   public void unregisterMBean(ObjectName name) 
            throws InstanceNotFoundException, MBeanRegistrationException;

   /**
    * Retrieve an MBean's registration information.
    *
    * @param name the object name of the mbean
    * @exception InstanceNotFoundException if the mbean is not registered
    *            in the MBeanServer
    */   
   public ObjectInstance getObjectInstance(ObjectName name) 
            throws InstanceNotFoundException;

   /**
    * Retrieve a set of Object instances
    *
    * @param name an ObjectName pattern, can be null for all mbeans
    * @param query a query expression to further filter the mbeans, can be null
    *        for no query
    */   
   public Set queryMBeans(ObjectName name, QueryExp query);

   /**
    * Retrieve a set of Object names
    *
    * @param name an ObjectName pattern, can be null for all mbeans
    * @param query a query expression to further filter the mbeans, can be null
    *        for no query
    */   
   public Set queryNames(ObjectName name, QueryExp query);

   /**
    * Test whether an mbean is registered.
    *
    * @param name the object name of the mbean
    * @return true when the mbean is registered, false otherwise
    * @exception RuntimeOperationsException wrapping an IllegalArgumentException for a
    *            null name
    */   
   public boolean isRegistered(ObjectName name);

   /**
    * Retrieve the number of mbeans registered in the server.
    *
    * @return true the number of registered mbeans
    */   
   public Integer getMBeanCount();

   /**
    * Retrieve a value from an MBean.
    *
    * @param name the object name of the mbean
    * @param attribute the attribute name of the value to retrieve
    * @return the value
    * @exception ReflectionException for an exception invoking the mbean
    * @exception MBeanException for any exception thrown by the mbean
    * @exception InstanceNotFoundException if the mbean is not registered
    * @exception AttributeNotFoundException if the mbean has no such attribute
    * @exception RuntimeOperationsException wrapping an IllegalArgumentException for a
    *            null name or attribute
    */   
   public Object getAttribute(ObjectName name, String attribute)
            throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException;

   /**
    * Retrieve a list of values from an MBean.
    *
    * @param name the object name of the mbean
    * @param attributes the attribute names of the values to retrieve
    * @return the list of values, attributes with errors are ignored
    * @exception ReflectionException for an exception invoking the mbean
    * @exception InstanceNotFoundException if the mbean is not registered
    * @exception RuntimeOperationsException wrapping an IllegalArgumentException for a
    *            null name or attributes
    */   
   public AttributeList getAttributes(ObjectName name, String[] attributes) 
            throws InstanceNotFoundException, ReflectionException;

   /**
    * Set a value for an MBean.
    *
    * @param name the object name of the mbean
    * @param attribute the attribute name and value to set
    * @exception ReflectionException for an exception invoking the mbean
    * @exception MBeanException for any exception thrown by the mbean
    * @exception InstanceNotFoundException if the mbean is not registered
    * @exception AttributeNotFoundException if the mbean has no such attribute
    * @exception InvalidAttributeValueException if the new value has an incorrect type
    * @exception RuntimeOperationsException wrapping an IllegalArgumentException for a
    *            null name or attribute
    */   
   public void setAttribute(ObjectName name, Attribute attribute)
            throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException;

   /**
    * Set a list of values for an MBean.
    *
    * @param name the object name of the mbean
    * @param attributes the attribute names and values to set
    * @return the list of values, attributes with errors are ignored
    * @exception ReflectionException for an exception invoking the mbean
    * @exception InstanceNotFoundException if the mbean is not registered
    * @exception RuntimeOperationsException wrapping an IllegalArgumentException for a
    *            null name or attributes
    */   
   public AttributeList setAttributes(ObjectName name, AttributeList attributes) 
            throws InstanceNotFoundException, ReflectionException;

   /**
    * Invokes an operation on an mbean.
    *
    * @param name the object name of the mbean
    * @param operationName the operation to perform
    * @param params the parameters
    * @param signature the signature of the operation
    * @return any result of the operation
    * @exception ReflectionException for an exception invoking the mbean
    * @exception MBeanException for any exception thrown by the mbean
    * @exception InstanceNotFoundException if the mbean is not registered
    */   
   public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
            throws InstanceNotFoundException, MBeanException, ReflectionException;

   /**
    * Retrieve the default domain of the mbeanserver.
    *
    * @return the default domain
    */   
   public String getDefaultDomain();

   /**
    * Retrieve the domains of the mbeanserver.
    *
    * @return the domains
    */   
   public String[] getDomains();

   /**
    * Add a notification listener to an MBean.
    *
    * @param name the name of the MBean broadcasting notifications
    * @param listener the listener to add
    * @param filter a filter to preprocess notifications
    * @param handback a object to add to any notifications
    * @exception InstanceNotFoundException if the broadcaster is not registered
    */   
   public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) 
            throws InstanceNotFoundException;

   /**
    * Add a notification listener to an MBean.
    *
    * @param name the name of the MBean broadcasting notifications
    * @param listener the object name listener to add
    * @param filter a filter to preprocess notifications
    * @param handback a object to add to any notifications
    * @exception InstanceNotFoundException if the broadcaster or listener is not registered
    * @exception RuntimeOperationsException wrapping an IllegalArgumentException for a
    *            null listener or the listener does not implement the Notification Listener interface
    */   
   public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) 
            throws InstanceNotFoundException;

   /**
    * Removes a listener from an mbean.<p>
    *
    * All registrations of the listener are removed.
    *
    * @param name the name of the MBean broadcasting notifications
    * @param listener the object name of the listener to remove
    * @exception InstanceNotFoundException if the broadcaster or listener is not registered
    * @exception ListenerNotFoundException if the listener is not registered against the broadcaster
    */
   public void removeNotificationListener(ObjectName name, ObjectName listener)
            throws InstanceNotFoundException, ListenerNotFoundException;

   /**
    * Removes a listener from an mbean.<p>
    *
    * Only the listener that was registered with the same filter and handback is removed.
    *
    * @param name the name of the MBean broadcasting notifications
    * @param listener the object name of listener to remove
    * @param filter the filter of the listener to remove
    * @exception InstanceNotFoundException if the broadcaster or listener is not registered
    * @exception ListenerNotFoundException if the listener, filter, handback
    *            is not registered against the broadcaster
    */
   public void removeNotificationListener(ObjectName name, ObjectName listener,
                                          NotificationFilter filter, Object handback)
      throws InstanceNotFoundException, ListenerNotFoundException;

   /**
    * Removes a listener from an mbean.<p>
    *
    * All registrations of the listener are removed.
    *
    * @param name the name of the MBean broadcasting notifications
    * @param listener the listener to remove
    * @exception InstanceNotFoundException if the broadcaster is not registered
    * @exception ListenerNotFoundException if the listener is not registered against the broadcaster
    */   
   public void removeNotificationListener(ObjectName name, NotificationListener listener)
            throws InstanceNotFoundException, ListenerNotFoundException;

   /**
    * Removes a listener from an mbean.<p>
    *
    * Only the listener that was registered with the same filter and handback is removed.
    *
    * @param name the name of the MBean broadcasting notifications
    * @param listener the listener to remove
    * @param filter the filter of the listener to remove
    * @exception InstanceNotFoundException if the broadcaster is not registered
    * @exception ListenerNotFoundException if the listener, filter, handback 
    *            is not registered against the broadcaster
    */   
   public void removeNotificationListener(ObjectName name, NotificationListener listener,
                                          NotificationFilter filter, Object handback)
      throws InstanceNotFoundException, ListenerNotFoundException;

   /**
    * Retrieves the jmx metadata for an mbean
    *
    * @param name the name of the mbean
    * @return the metadata
    * @exception IntrospectionException for any error during instrospection
    * @exception InstanceNotFoundException if the mbean is not registered
    * @exception ReflectionException for any error trying to invoke the operation on the mbean
    */
   public MBeanInfo getMBeanInfo(ObjectName name) 
            throws InstanceNotFoundException, IntrospectionException, ReflectionException;

   /**
    * Tests whether an mbean can be cast to the given type
    *
    * @param name the name of the mbean
    * @param className the class name to check
    * @return true when it is of that type, false otherwise
    * @exception InstanceNotFoundException if the mbean is not registered
    */
   public boolean isInstanceOf(ObjectName name, String className) 
            throws InstanceNotFoundException;

   /**
    * Instantiates an object using the default loader repository and default
    * no-args constructor.
    *
    * @see  javax.management.loading.DefaultLoaderRepository
    *
    * @param className Class to instantiate. Must have a public no-args
    *        constructor. Cannot contain a <tt>null</tt> reference.
    *
    * @return  instantiated object
    *
    * @throws ReflectionException If there was an error while trying to invoke
    *         the class's constructor or the given class was not found. This
    *         exception wraps the actual exception thrown.
    * @throws MBeanException If the object constructor threw a checked exception
    *         during the initialization. This exception wraps the actual
    *         exception thrown.
    * @throws RuntimeMBeanException If the class constructor threw a runtime
    *         exception. This exception wraps the actual exception thrown.
    * @throws RuntimeErrorException If the class constructor threw an error.
    *         This exception wraps the actual error thrown.
    * @throws RuntimeOperationsException If the <tt>className</tt> is <tt>null</tt>.
    *         Wraps an <tt>IllegalArgumentException</tt> instance.
    */
   public Object instantiate(String className)
            throws ReflectionException, MBeanException;

   /**
    * Instantiates an object using the given class loader. If the loader name contains
    * a <tt>null</tt> reference, the class loader of the MBean server implementation
    * will be used. The object must have a default, no-args constructor.
    *
    * @param   className   Class to instantiate. Must have a public no args constructor.
    *                      Cannot contain a <tt>null</tt> reference.
    * @param   loaderName  Object name of a class loader that has been registered to the server.
    *                      If <tt>null</tt>, the class loader of the MBean server is used.
    * @return  instantiated object
    *
    * @throws ReflectionException If there was an error while trying to invoke
    *         the class's constructor or the given class was not found. This
    *         exception wraps the actual exception thrown.
    * @throws MBeanException If the object constructor threw a checked exception
    *         during the initialization. This exception wraps the actual exception
    *         thrown.
    * @throws InstanceNotFoundException if the specified class loader was not
    *         registered to the agent
    * @throws RuntimeMBeanException If the class constructor raised a runtime
    *         exception. This exception wraps the actual exception thrown.
    * @throws RuntimeErrorException If the class constructor raised an error.
    *         This exception wraps the actual error thrown.
    * @throws RuntimeOperationsException if the <tt>className</tt> is <tt>null</tt>.
    *         Wraps an <tt>IllegalArgumentException</tt> instance.
    */
   public Object instantiate(String className, ObjectName loaderName)
            throws ReflectionException, MBeanException, InstanceNotFoundException;

   /**
    * Instantiates an object using the default loader repository and a given constructor.
    * The class being instantiated must contain a constructor that matches the
    * signature given as an argument to this method call.
    *
    * @see javax.management.loading.DefaultLoaderRepository
    *
    * @param className  class to instantiate
    * @param params     argument values for the constructor call
    * @param signature  signature of the constructor as fully qualified class names
    *
    * @return instantiated object
    *
    * @throws ReflectionException If there was an error while trying to invoke
    *         the class's constructor or the given class was not found. This
    *         exception wraps the actual exception thrown.
    * @throws MBeanException If the object constructor raised a checked exception
    *         during the initialization. This exception wraps the actual exception
    *         thrown.
    * @throws RuntimeMBeanException If the class constructor raised a runtime
    *         exception. This exception wraps the actual exception thrown.
    * @throws RuntimeErrorException If the class constructor raised an error.
    *         This exception wraps the actual error thrown.
    * @throws RuntimeOperationsException if the <tt>className</tt> is <tt>null</tt>.
    *         Wraps an <tt>IllegalArgumentException</tt> instance.
    */
   public Object instantiate(String className, Object[] params, String[] signature)
            throws ReflectionException, MBeanException;

   /**
    * Instantiates an object using the given class loader. If the loader name contains
    * a <tt>null</tt> reference, the class loader of the MBean server implementation
    * will be used. The object must contain a constructor with a matching signature
    * given as a parameter to this call.
    *
    * @param   className   class to instantiate
    * @param   loaderName  object name of a registered class loader in the agent.
    * @param   params      argument values for the constructor call
    * @param   signature   signature of the constructor as fully qualified class name strings
    *
    * @return instantiated object
    *
    * @throws ReflectionException If there was an error while trying to invoke the
    *         class's constructor or the given class was not found. this exception
    *         wraps the actual exception thrown.
    * @throws MBeanException If the object constructor raised a checked exception
    *         during the initialization. This exception wraps the actual exception thrown.
    * @throws InstanceNotFoundException if the specified class loader was not
    *         registered to the agent.
    * @throws RuntimeMBeanException If the class constructor raised a runtime
    *         exception. This exception wraps the actual exception thrown.
    * @throws RuntimeErrorException If the class constructor raised an error.
    *         This exception wraps the actual error thrown.
    * @throws RuntimeOperationsException if the <tt>className</tt> argument is <tt>null</tt>.
    *         Wraps an <tt>IllegalArgumentException</tt> instance.
    */
   public Object instantiate(String className, ObjectName loaderName, Object[] params, String[] signature)
            throws ReflectionException, MBeanException, InstanceNotFoundException;

   /**
    * @deprecated use {@link #getClassLoaderFor(ObjectName)} to obtain the
    * appropriate classloader for deserialization
    */
   public ObjectInputStream deserialize(ObjectName name, byte[] data) 
            throws InstanceNotFoundException, OperationsException;

   /**
    * @deprecated use {@link #getClassLoaderFor(ObjectName)} to obtain the
    * appropriate classloader for deserialization
    */
   public ObjectInputStream deserialize(String className, byte[] data) 
            throws OperationsException, ReflectionException;

   /**
    * @deprecated use {@link #getClassLoaderFor(ObjectName)} to obtain the
    * appropriate classloader for deserialization
    */
   public ObjectInputStream deserialize(String className, ObjectName loaderName, byte[] data)
            throws InstanceNotFoundException, OperationsException, ReflectionException;

   /**
    * Retrieve the classloader for an mbean
    *
    * @param name the object name of the mbean
    * @return the classloader
    * @exception InstanceNotFoundException when the mbean is not registered
    */
   public ClassLoader getClassLoaderFor(ObjectName name)
      throws InstanceNotFoundException;

   /**
    * Retrieve the classloader registered as an MBean
    *
    * @param name the object name of the classloader
    * @return the classloader
    * @exception InstanceNotFoundException when the mbean is not registered
    */
   public ClassLoader getClassLoader(ObjectName name)
      throws InstanceNotFoundException;

   /**
    * Retrieve the classloader repository for this mbean server
    *
    * @return the classloader repository
    */
   public ClassLoaderRepository getClassLoaderRepository();
}

