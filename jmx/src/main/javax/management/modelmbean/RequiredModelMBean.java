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

import java.util.ArrayList;
import java.util.Arrays;

import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.loading.ClassLoaderRepository;

import org.jboss.mx.modelmbean.RequiredModelMBeanInstantiator;

/**
 * Mandatory Model MBean implementation. The Model MBean implementation
 * can be configured by setting a <tt>jbossmx.required.modelmbean.class</tt>
 * system property.
 *
 * @see javax.management.modelmbean.ModelMBean
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:thomas.diesler@jboss.com">Thomas Diesler</a>.
 * @version $Revision: 57200 $
 */
public class RequiredModelMBean
   implements ModelMBean, MBeanRegistration, NotificationEmitter
{
   // Attributes ----------------------------------------------------
   private ModelMBean delegate = null;
   private MBeanRegistration registrationInterface = null;
   private Boolean isRegistered = new Boolean(false);

   // Constructors --------------------------------------------------
   public RequiredModelMBean()
      throws MBeanException, RuntimeOperationsException
   {
      delegate = RequiredModelMBeanInstantiator.instantiate();
      if (delegate instanceof MBeanRegistration)
         registrationInterface = (MBeanRegistration)delegate;
   }

   /**
    * Constructs a RequiredModelMBean object using ModelMBeanInfo passed in.
    * As long as the RequiredModelMBean is not registered with the MBeanServer yet,
    * the RequiredModelMBean's MBeanInfo and Descriptors can be customized using the
    * setModelMBeanInfo(javax.management.modelmbean.ModelMBeanInfo) method.
    *
    * After the RequiredModelMBean's MBeanInfo and Descriptors are customized,
    * the RequiredModelMBean can be registered with the MBeanServer.
    */
   public RequiredModelMBean(ModelMBeanInfo info)
      throws MBeanException, RuntimeOperationsException
   {
      if (info == null)
         throw new RuntimeOperationsException(new IllegalArgumentException("Null model mbean info"));

      delegate = RequiredModelMBeanInstantiator.instantiate(info);
      if (delegate instanceof MBeanRegistration)
         registrationInterface = (MBeanRegistration)delegate;
   }
   
   // ModelMBean implementation -------------------------------------
   public void setModelMBeanInfo(ModelMBeanInfo info)
      throws MBeanException, RuntimeOperationsException
   {
      if (isRegistered.booleanValue())
         throw new RuntimeOperationsException(new IllegalStateException("Cannot set ModelMBeanInfo, mbean already registered"));
      delegate.setModelMBeanInfo(info);
   }
   
   public void setManagedResource(Object mr, String mr_type)
      throws MBeanException, RuntimeOperationsException,
      InstanceNotFoundException, InvalidTargetObjectTypeException
   {
      delegate.setManagedResource(mr, mr_type);
   }

   // PersistentMBean implementation --------------------------------
   public void load()
      throws MBeanException, RuntimeOperationsException, InstanceNotFoundException
   {
      delegate.load();   
   }

   public void store()
      throws MBeanException, RuntimeOperationsException, InstanceNotFoundException
   {
      delegate.store();  
   }
   
   // DynamicMBean implementation -----------------------------------
   public MBeanInfo getMBeanInfo()
   {
      return delegate.getMBeanInfo();
   }
   
   public Object invoke(String opName, Object[] opArgs, String[] sig)
         throws MBeanException, ReflectionException
   {
      return delegate.invoke(opName, opArgs, sig);   
   }

   public Object getAttribute(String attrName)
         throws AttributeNotFoundException, MBeanException, ReflectionException
   {
      return delegate.getAttribute(attrName);
   }

   public AttributeList getAttributes(String[] attrNames)
   {
      return delegate.getAttributes(attrNames);
   }
   
   public void setAttribute(Attribute attribute) throws AttributeNotFoundException,
         InvalidAttributeValueException, MBeanException, ReflectionException
   {
      delegate.setAttribute(attribute);  
   }

   public AttributeList setAttributes(AttributeList attributes)
   {
      return delegate.setAttributes(attributes);
   }
   
   // ModelMBeanNotificationBroadcaster implementation --------------
   public void addNotificationListener(NotificationListener inlistener,
      NotificationFilter infilter, Object inhandback)
      throws IllegalArgumentException
   {
      delegate.addNotificationListener(inlistener, infilter, inhandback);  
   }

   public void removeNotificationListener(NotificationListener inlistener)
      throws ListenerNotFoundException
   {
      delegate.removeNotificationListener(inlistener);   
   }

   public void removeNotificationListener(NotificationListener listener,
                                          NotificationFilter filter,
                                          Object handback)
      throws ListenerNotFoundException
   {
      if (delegate instanceof NotificationEmitter)
         ((NotificationEmitter)delegate).removeNotificationListener(listener, filter, handback);
      else
         throw new UnsupportedOperationException("Delegate is not a NotificationEmitter " + delegate.getClass());
   }

   public void sendNotification(Notification ntfyObj)
      throws MBeanException, RuntimeOperationsException
   {
      delegate.sendNotification(ntfyObj);   
   }

   public void sendNotification(String ntfyText)
      throws MBeanException, RuntimeOperationsException
   {
      delegate.sendNotification(ntfyText);
   }

   /** Returns the array of Notifications generated by the RequiredModelMBean.
    * RequiredModelMBean may always send also two additional notifications:
    * One with descriptor "name=GENERIC,descriptorType=notification,log=T,severity=5,displayName=jmx.modelmbean.generic"
    * Second is a standard attribute change notification with descriptor "name=ATTRIBUTE_CHANGE,descriptorType=notification,log=T,severity=5,displayName=jmx.attribute.change"
    * Thus these two notifications are always added to those specified by the application.
    * 
    * @return
    */ 
   public MBeanNotificationInfo[] getNotificationInfo()
   {
      MBeanNotificationInfo generic = null;
      MBeanNotificationInfo attrChange = null;
      MBeanNotificationInfo[] resourceInfo = delegate.getNotificationInfo();
      for(int n = 0; n < resourceInfo.length; n ++)
      {
         MBeanNotificationInfo notify = resourceInfo[n];
         String name = notify.getName();
         if( name.equals("GENERIC") )
            generic = notify;
         else if( name.equals("ATTRIBUTE_CHANGE") )
            attrChange = notify;
      }
      if( generic == null || attrChange == null )
      {
         ArrayList tmp = new ArrayList(Arrays.asList(resourceInfo));
         if( attrChange == null )
         {
            String[] types = {"jmx.attribute.change"};
            ModelMBeanNotificationInfo n = new ModelMBeanNotificationInfo(types,
               "ATTRIBUTE_CHANGE", "Attribute change notfication");
            n.getDescriptor().setField("log", "T");
            n.getDescriptor().setField("displayName", "jmx.attribute.change");
            tmp.add(0, n);
         }
         if( generic == null )
         {
            String[] types = {"jmx.modelmbean.generic"};
            ModelMBeanNotificationInfo n = new ModelMBeanNotificationInfo(types,
               "GENERIC", "Generic text msg");
            n.getDescriptor().setField("log", "T");
            n.getDescriptor().setField("displayName", "jmx.modelmbean.generic");
            tmp.add(0, n);
         }
         resourceInfo = new MBeanNotificationInfo[tmp.size()];
         tmp.toArray(resourceInfo);
      }

      return resourceInfo;
   }

   public void addAttributeChangeNotificationListener(NotificationListener inlistener,
         String inAttributeName, Object inhandback) throws MBeanException, RuntimeOperationsException, IllegalArgumentException
   {
      delegate.addAttributeChangeNotificationListener(inlistener, inAttributeName, inhandback);
   }

   public void removeAttributeChangeNotificationListener(NotificationListener inlistener, String inAttributeName)
         throws MBeanException, RuntimeOperationsException, ListenerNotFoundException
   {
      delegate.removeAttributeChangeNotificationListener(inlistener, inAttributeName);
   }

   public void sendAttributeChangeNotification(AttributeChangeNotification ntfyObj) throws MBeanException, RuntimeOperationsException
   {
      delegate.sendAttributeChangeNotification(ntfyObj);   
   }

   public void sendAttributeChangeNotification(Attribute inOldVal, Attribute inNewVal)
         throws MBeanException, RuntimeOperationsException
   {
      delegate.sendAttributeChangeNotification(inOldVal, inNewVal);   
   }

   // MBeanRegistration implementation ------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception
   {
      if (registrationInterface != null)
         return registrationInterface.preRegister(server, name);
         
      return name;
   }

   public void postRegister(Boolean registrationDone)
   {
      if (registrationInterface != null)
         registrationInterface.postRegister(registrationDone);
      isRegistered = registrationDone;
   }
   
   public void preDeregister() throws Exception
   {
      if (registrationInterface != null)
         registrationInterface.preDeregister();
   }
   
   public void postDeregister()
   {
      isRegistered = new Boolean(false);
      if (registrationInterface != null)
         registrationInterface.postDeregister();
   }

   protected ClassLoaderRepository getClassLoaderRepository()
   {
      return null;
   }
}
