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

/**
 * @version <tt>$Revision: 57211 $</tt>
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 */
public class XMBeanAttributeMetaData
{
   private String access;
   private String getMethod;
   private String setMethod;
   private String description;
   private String name;
   private String type;

   public String getAccess()
   {
      return access;
   }

   public void setAccess(String access)
   {
      this.access = access;
   }

   public String getGetMethod()
   {
      return getMethod;
   }

   public void setGetMethod(String getMethod)
   {
      this.getMethod = getMethod;
   }

   public String getSetMethod()
   {
      return setMethod;
   }

   public void setSetMethod(String setMethod)
   {
      this.setMethod = setMethod;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getType()
   {
      return type;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public String toString()
   {
      return
         "[description=" + description +
         ", access=" + access +
         ", getMethod=" + getMethod +
         ", setMethod=" + setMethod +
         ", name=" + name +
         ", type=" + type + ']';
   }

   public boolean equals(Object o)
   {
      if(this == o) return true;
      if(!(o instanceof XMBeanAttributeMetaData)) return false;

      final XMBeanAttributeMetaData mBeanAttributeMetaData = (XMBeanAttributeMetaData)o;

      if(access != null ? !access.equals(mBeanAttributeMetaData.access) : mBeanAttributeMetaData.access != null) return false;
      if(description != null ? !description.equals(mBeanAttributeMetaData.description) : mBeanAttributeMetaData.description != null) return false;
      if(getMethod != null ? !getMethod.equals(mBeanAttributeMetaData.getMethod) : mBeanAttributeMetaData.getMethod != null) return false;
      if(name != null ? !name.equals(mBeanAttributeMetaData.name) : mBeanAttributeMetaData.name != null) return false;
      if(setMethod != null ? !setMethod.equals(mBeanAttributeMetaData.setMethod) : mBeanAttributeMetaData.setMethod != null) return false;
      if(type != null ? !type.equals(mBeanAttributeMetaData.type) : mBeanAttributeMetaData.type != null) return false;

      return true;
   }

   public int hashCode()
   {
      int result;
      result = (access != null ? access.hashCode() : 0);
      result = 29 * result + (getMethod != null ? getMethod.hashCode() : 0);
      result = 29 * result + (setMethod != null ? setMethod.hashCode() : 0);
      result = 29 * result + (description != null ? description.hashCode() : 0);
      result = 29 * result + (name != null ? name.hashCode() : 0);
      result = 29 * result + (type != null ? type.hashCode() : 0);
      return result;
   }
}
