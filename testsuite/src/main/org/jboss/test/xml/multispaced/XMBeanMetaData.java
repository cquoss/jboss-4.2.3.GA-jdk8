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
package org.jboss.test.xml.multispaced;

import java.util.List;
import java.util.ArrayList;

/**
 * @version <tt>$Revision: 57211 $</tt>
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 */
public class XMBeanMetaData
{
   private String description;
   private String mbeanClass;
   private List constructors = new ArrayList();
   private List attributes = new ArrayList();
   private List operations = new ArrayList();
   private List notifications = new ArrayList();

   private Object persistenceManager;

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getMbeanClass()
   {
      return mbeanClass;
   }

   public void setMbeanClass(String mbeanClass)
   {
      this.mbeanClass = mbeanClass;
   }

   public List getConstructors()
   {
      return constructors;
   }

   public void addConstructor(XMBeanConstructorMetaData constructor)
   {
      constructors.add(constructor);
   }

   public List getAttributes()
   {
      return attributes;
   }

   public void addAttribute(XMBeanAttributeMetaData attribute)
   {
      attributes.add(attribute);
   }

   public List getOperations()
   {
      return operations;
   }

   public void addOperation(XMBeanOperationMetaData operation)
   {
      operations.add(operation);
   }

   public List getNotifications()
   {
      return notifications;
   }

   public void addNotification(XMBeanNotificationMetaData notification)
   {
      notifications.add(notification);
   }

   public Object getPersistenceManager()
   {
      return persistenceManager;
   }

   public void setPersistenceManager(Object persistenceManager)
   {
      this.persistenceManager = persistenceManager;
   }

   public String toString()
   {
      return
         "[description=" + description +
         ", mbeanClass=" + mbeanClass +
         ", constructors=" + constructors +
         ", attributes=" + attributes +
         ", operations=" + operations +
         ", notifications=" + notifications +
         ", persistence-manager=" + persistenceManager + ']';
   }

   public boolean equals(Object o)
   {
      if(this == o) return true;
      if(!(o instanceof XMBeanMetaData)) return false;

      final XMBeanMetaData xmBeanMetaData = (XMBeanMetaData)o;

      if(attributes != null ? !attributes.equals(xmBeanMetaData.attributes) : xmBeanMetaData.attributes != null) return false;
      if(constructors != null ? !constructors.equals(xmBeanMetaData.constructors) : xmBeanMetaData.constructors != null) return false;
      if(description != null ? !description.equals(xmBeanMetaData.description) : xmBeanMetaData.description != null) return false;
      if(mbeanClass != null ? !mbeanClass.equals(xmBeanMetaData.mbeanClass) : xmBeanMetaData.mbeanClass != null) return false;
      if(notifications != null ? !notifications.equals(xmBeanMetaData.notifications) : xmBeanMetaData.notifications != null) return false;
      if(operations != null ? !operations.equals(xmBeanMetaData.operations) : xmBeanMetaData.operations != null) return false;
      if(persistenceManager != null ? !persistenceManager.equals(xmBeanMetaData.persistenceManager) : xmBeanMetaData.persistenceManager != null) return false;

      return true;
   }

   public int hashCode()
   {
      int result;
      result = (description != null ? description.hashCode() : 0);
      result = 29 * result + (mbeanClass != null ? mbeanClass.hashCode() : 0);
      result = 29 * result + (constructors != null ? constructors.hashCode() : 0);
      result = 29 * result + (attributes != null ? attributes.hashCode() : 0);
      result = 29 * result + (operations != null ? operations.hashCode() : 0);
      result = 29 * result + (notifications != null ? notifications.hashCode() : 0);
      result = 29 * result + (persistenceManager != null ? persistenceManager.hashCode() : 0);
      return result;
   }
}
