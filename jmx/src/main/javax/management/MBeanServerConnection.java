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

import java.io.IOException;
import java.util.Set;

/**
 * An interface used to talk to an MBeanServer that is either remote or
 * local. The local interface MBeanServer extends this one.
 *
 * @see javax.management.MBeanServer
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 */
public interface MBeanServerConnection
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
    * @exception IOException for a communication problem during this operation
    */   
   public ObjectInstance createMBean(String className, ObjectName name) 
      throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, 
             NotCompliantMBeanException, IOException;
   
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
    * @exception IOException for a communication problem during this operation
    */   
   public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName)
      throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, 
             NotCompliantMBeanException, InstanceNotFoundException, IOException;
   
   /**
    * Create an MBean registered using the given object name.<p>
    *
    * Uses the specified constructor.
    *
    * @param className the class name of the mbean
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
    * @exception NotCompliantMBeanException if the class name does not correspond to
    *            a valid MBean
    * @exception RuntimeOperationsException wrapping an IllegalArgumentException for a
    *            null class name, the ObjectName could not be determined or it is a pattern
    * @exception IOException for a communication problem during this operation
    */   
   public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature)
      throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException,
             NotCompliantMBeanException, IOException;
            
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
    * @exception IOException for a communication problem during this operation
    */   
   public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params, 
                                     String[] signature)
      throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, 
             NotCompliantMBeanException, InstanceNotFoundException, IOException;

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
    * @exception IOException for a communication problem during this operation
    */   
   public void unregisterMBean(ObjectName name) 
      throws InstanceNotFoundException, MBeanRegistrationException, IOException;

   /**
    * Retrieve an MBean's registration information.
    *
    * @param name the object name of the mbean
    * @exception InstanceNotFoundException if the mbean is not registered
    *            in the MBeanServer
    * @exception IOException for a communication problem during this operation
    */   
   public ObjectInstance getObjectInstance(ObjectName name) 
      throws InstanceNotFoundException, IOException;

   /**
    * Retrieve a set of Object instances
    *
    * @param name an ObjectName pattern, can be null for all mbeans
    * @param query a query expression to further filter the mbeans, can be null
    *        for no query
    * @exception IOException for a communication problem during this operation
    */   
   public Set queryMBeans(ObjectName name, QueryExp query)
      throws IOException;

   /**
    * Retrieve a set of Object names
    *
    * @param name an ObjectName pattern, can be null for all mbeans
    * @param query a query expression to further filter the mbeans, can be null
    *        for no query
    * @exception IOException for a communication problem during this operation
    */   
   public Set queryNames(ObjectName name, QueryExp query)
      throws IOException;

   /**
    * Test whether an mbean is registered.
    *
    * @param name the object name of the mbean
    * @return true when the mbean is registered, false otherwise
    * @exception RuntimeOperationsException wrapping an IllegalArgumentException for a
    *            null name
    * @exception IOException for a communication problem during this operation
    */   
   public boolean isRegistered(ObjectName name)
      throws IOException;

   /**
    * Retrieve the number of mbeans registered in the server.
    *
    * @return true the number of registered mbeans
    * @exception IOException for a communication problem during this operation
    */   
   public Integer getMBeanCount()
      throws IOException;

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
    * @exception IOException for a communication problem during this operation
    */   
   public Object getAttribute(ObjectName name, String attribute)
      throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException,
             IOException;

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
    * @exception IOException for a communication problem during this operation
    */   
   public AttributeList getAttributes(ObjectName name, String[] attributes) 
      throws InstanceNotFoundException, ReflectionException, IOException;

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
    * @exception IOException for a communication problem during this operation
    */   
   public void setAttribute(ObjectName name, Attribute attribute)
      throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException,
             MBeanException, ReflectionException, IOException;

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
    * @exception IOException for a communication problem during this operation
    */   
   public AttributeList setAttributes(ObjectName name, AttributeList attributes) 
      throws InstanceNotFoundException, ReflectionException, IOException;

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
    * @exception IOException for a communication problem during this operation
    */   
   public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
      throws InstanceNotFoundException, MBeanException, ReflectionException, IOException;

   /**
    * Retrieve the default domain of the mbeanserver.
    *
    * @return the default domain
    * @exception IOException for a communication problem during this operation
    */   
   public String getDefaultDomain()
      throws IOException;

   /**
    * Retrieve the domains of the mbeanserver.
    *
    * @return the domains
    * @exception IOException for a communication problem during this operation
    */   
   public String[] getDomains()
      throws IOException;

   /**
    * Add a notification listener to an MBean.
    *
    * @param name the name of the MBean broadcasting notifications
    * @param listener the listener to add
    * @param filter a filter to preprocess notifications
    * @param handback a object to add to any notifications
    * @exception InstanceNotFoundException if the broadcaster is not registered
    * @exception IOException for a communication problem during this operation
    */   
   public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, 
                                       Object handback) 
      throws InstanceNotFoundException, IOException;

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
    * @exception IOException for a communication problem during this operation
    */   
   public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback)
      throws InstanceNotFoundException, IOException;

   /**
    * Removes a listener from an mbean.<p>
    *
    * All registrations of the listener are removed.
    *
    * @param name the name of the MBean broadcasting notifications
    * @param listener the object name of the listener to remove
    * @exception InstanceNotFoundException if the broadcaster or listener is not registered
    * @exception ListenerNotFoundException if the listener is not registered against the broadcaster
    * @exception IOException for a communication problem during this operation
    */
   public void removeNotificationListener(ObjectName name, ObjectName listener)
      throws InstanceNotFoundException, ListenerNotFoundException, IOException;

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
    * @exception IOException for a communication problem during this operation
    */   
   public void removeNotificationListener(ObjectName name, ObjectName listener,
                                          NotificationFilter filter, Object handback)
      throws InstanceNotFoundException, ListenerNotFoundException, IOException;

   /**
    * Removes a listener from an mbean.<p>
    *
    * All registrations of the listener are removed.
    *
    * @param name the name of the MBean broadcasting notifications
    * @param listener the listener to remove
    * @exception InstanceNotFoundException if the broadcaster is not registered
    * @exception ListenerNotFoundException if the listener is not registered against the broadcaster
    * @exception IOException for a communication problem during this operation
    */
   public void removeNotificationListener(ObjectName name, NotificationListener listener)
      throws InstanceNotFoundException, ListenerNotFoundException, IOException;

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
    * @exception IOException for a communication problem during this operation
    */
   public void removeNotificationListener(ObjectName name, NotificationListener listener,
                                          NotificationFilter filter, Object handback)
      throws InstanceNotFoundException, ListenerNotFoundException, IOException;

   /**
    * Retrieves the jmx metadata for an mbean
    *
    * @param name the name of the mbean
    * @return the metadata
    * @exception IntrospectionException for any error during instrospection
    * @exception InstanceNotFoundException if the mbean is not registered
    * @exception ReflectionException for any error trying to invoke the operation on the mbean
    * @exception IOException for a communication problem during this operation
    */   
   public MBeanInfo getMBeanInfo(ObjectName name) 
      throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException;

   /**
    * Tests whether an mbean can be cast to the given type
    *
    * @param name the name of the mbean
    * @param className the class name to check
    * @return true when it is of that type, false otherwise
    * @exception InstanceNotFoundException if the mbean is not registered
    * @exception IOException for a communication problem during this operation
    */   
   public boolean isInstanceOf(ObjectName name, String className) 
      throws InstanceNotFoundException, IOException;
}
