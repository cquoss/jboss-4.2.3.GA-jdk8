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

import org.jboss.xb.binding.MarshallingContext;
import org.jboss.xb.binding.ObjectModelProvider;

/**
 * @version <tt>$Revision: 57211 $</tt>
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 */
public class XMBeanMetaDataProvider
   implements ObjectModelProvider
{
   public static final XMBeanMetaDataProvider INSTANCE = new XMBeanMetaDataProvider();

   private XMBeanMetaDataProvider() {}

   public Object getRoot(Object o, MarshallingContext ctx, String namespaceURI, String localName)
   {
      return o;
   }

   public Object getChildren(XMBeanMetaData xmbean, String namespaceUri, String localName)
   {
      Object children;
      if("mbean".equals(localName))
      {
         children = xmbean;
      }
      else if("constructor".equals(localName))
      {
         children = xmbean.getConstructors();
      }
      else if("attribute".equals(localName))
      {
         children = xmbean.getAttributes();
      }
      else if("operation".equals(localName))
      {
         children = xmbean.getOperations();
      }
      else if("notification".equals(localName))
      {
         children = xmbean.getNotifications();
      }
      else if("persistence".equals(localName))
      {
         children = xmbean.getPersistenceManager();
      }
      else
      {
         children = null;
      }
      return children;
   }

   public Object getElementValue(XMBeanMetaData xmbean, String namespaceUri, String localName)
   {
      Object value;
      if("description".equals(localName))
      {
         value = xmbean.getDescription();
      }
      else if("class".equals(localName))
      {
         value = xmbean.getMbeanClass();
      }
      else
      {
         value = null;
      }
      return value;
   }

   public Object getElementValue(XMBeanConstructorMetaData constructor, String namespaceUri, String localName)
   {
      Object value;
      if("description".equals(localName))
      {
         value = constructor.getDescription();
      }
      else if("name".equals(localName))
      {
         value = constructor.getName();
      }
      else
      {
         value = null;
      }
      return value;
   }

   public Object getAttributeValue(XMBeanAttributeMetaData attribute, String namespaceUri, String localName)
   {
      Object value;
      if("access".equals(localName))
      {
         value = attribute.getAccess();
      }
      else if("getMethod".equals(localName))
      {
         value = attribute.getGetMethod();
      }
      else if("setMethod".equals(localName))
      {
         value = attribute.getSetMethod();
      }
      else
      {
         value = null;
      }
      return value;
   }

   public Object getElementValue(XMBeanAttributeMetaData attribute, String namespaceUri, String localName)
   {
      Object value;
      if("description".equals(localName))
      {
         value = attribute.getDescription();
      }
      else if("name".equals(localName))
      {
         value = attribute.getName();
      }
      else if("type".equals(localName))
      {
         value = attribute.getType();
      }
      else
      {
         value = null;
      }
      return value;
   }

   public Object getElementValue(XMBeanOperationMetaData operation, String namespaceUri, String localName)
   {
      Object value;
      if("description".equals(localName))
      {
         value = operation.getDescription();
      }
      else if("name".equals(localName))
      {
         value = operation.getName();
      }
      else if("return-type".equals(localName))
      {
         value = operation.getReturnType();
      }
      else
      {
         value = null;
      }
      return value;
   }

   public Object getElementValue(XMBeanNotificationMetaData notification, String namespaceUri, String localName)
   {
      Object value;
      if("description".equals(localName))
      {
         value = notification.getDescription();
      }
      else if("name".equals(localName))
      {
         value = notification.getName();
      }
      else if("notification-type".equals(localName))
      {
         value = notification.getNotificationType();
      }
      else
      {
         value = null;
      }
      return value;
   }
}
