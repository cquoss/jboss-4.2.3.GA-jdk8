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
package org.jboss.test.jcaprops.support;

/**
 * A PropertyTestAdminObject.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57211 $
 */
public interface PropertyTestAdminObject
{
   Boolean getBooleanAO();

   void setBooleanAO(Boolean booleanAO);

   Boolean getBooleanAOMBean();

   void setBooleanAOMBean(Boolean booleanAOMBean);

   Byte getByteAO();

   void setByteAO(Byte byteAO);

   Byte getByteAOMBean();

   void setByteAOMBean(Byte byteAOMBean);

   Character getCharacterAO();

   void setCharacterAO(Character characterAO);

   Character getCharacterAOMBean();

   void setCharacterAOMBean(Character characterAOMBean);

   Double getDoubleAO();

   void setDoubleAO(Double doubleAO);

   Double getDoubleAOMBean();

   void setDoubleAOMBean(Double doubleAOMBean);

   Float getFloatAO();

   void setFloatAO(Float floatAO);

   Float getFloatAOMBean();

   void setFloatAOMBean(Float floatAOMBean);

   Integer getIntegerAO();

   void setIntegerAO(Integer integerAO);

   Integer getIntegerAOMBean();

   void setIntegerAOMBean(Integer integerAOMBean);

   Long getLongAO();

   void setLongAO(Long longAO);

   Long getLongAOMBean();

   void setLongAOMBean(Long longAOMBean);

   Short getShortAO();

   void setShortAO(Short shortAO);

   Short getShortAOMBean();

   void setShortAOMBean(Short shortAOMBean);

   String getStringAO();

   void setStringAO(String stringAO);

   String getStringAOMBean();

   void setStringAOMBean(String stringAOMBean);
}