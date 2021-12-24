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

import javax.management.Descriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;

import javax.management.MBeanOperationInfo;
import javax.management.RuntimeOperationsException;

/**
 * Interface for manipulating the meta data of a Model MBean.
 *
 * @author     <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version    $Revision: 57200 $
 */
public interface ModelMBeanInfo
{
   /**
    * Returns all descriptors with a requested type from a Model MBean.
    * The descriptor type may be one of the following:  <ul>
    *
    * <li>{@link org.jboss.mx.modelmbean.ModelMBeanConstants#MBEAN_DESCRIPTOR MBEAN_DESCRIPTOR}</li>
    * <li>{@link org.jboss.mx.modelmbean.ModelMBeanConstants#ATTRIBUTE_DESCRIPTOR ATTRIBUTE_DESCRIPTOR}</li>
    * <li>{@link org.jboss.mx.modelmbean.ModelMBeanConstants#OPERATION_DESCRIPTOR OPERATION_DESCRIPTOR}</li>
    * <li>{@link org.jboss.mx.modelmbean.ModelMBeanConstants#NOTIFICATION_DESCRIPTOR NOTIFICATION_DESCRIPTOR}</li>
    * <li>{@link org.jboss.mx.modelmbean.ModelMBeanConstants#CONSTRUCTOR_DESCRIPTOR CONSTRUCTOR_DESCRIPTOR}</li>
    * <li>{@link org.jboss.mx.modelmbean.ModelMBeanConstants#ALL_DESCRIPTORS ALL_DESCRIPTORS}</li>
    * </ul>
    *
    * @param   descriptorType descriptor type string
    *
    * @return  array of descriptors
    *
    * @throws  MBeanException for no good reason
    * @throws  RuntimeOperationsException for no good reason
    */
   public Descriptor[] getDescriptors(String descriptorType)
       throws MBeanException, RuntimeOperationsException;


   public void setDescriptors(Descriptor[] inDescriptors)
       throws MBeanException, RuntimeOperationsException;


   public Descriptor getDescriptor(String inDescriptorName, String inDescriptorType)
       throws MBeanException, RuntimeOperationsException;


   public void setDescriptor(Descriptor inDescriptor, String inDescriptorType)
       throws MBeanException, RuntimeOperationsException;


   public Descriptor getMBeanDescriptor()
       throws MBeanException, RuntimeOperationsException;


   public void setMBeanDescriptor(Descriptor inDescriptor)
       throws MBeanException, RuntimeOperationsException;


   public ModelMBeanAttributeInfo getAttribute(String inName)
       throws MBeanException, RuntimeOperationsException;


   public ModelMBeanOperationInfo getOperation(String inName)
       throws MBeanException, RuntimeOperationsException;


   public ModelMBeanNotificationInfo getNotification(String inName)
       throws MBeanException, RuntimeOperationsException;


   public Object clone();


   public MBeanAttributeInfo[] getAttributes();


   public String getClassName();


   public MBeanConstructorInfo[] getConstructors();


   public String getDescription();


   public MBeanNotificationInfo[] getNotifications();


   public MBeanOperationInfo[] getOperations();

}

