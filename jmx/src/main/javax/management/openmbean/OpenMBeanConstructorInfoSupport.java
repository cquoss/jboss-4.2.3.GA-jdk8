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
package javax.management.openmbean;

import java.io.Serializable;

import java.util.Arrays;

import javax.management.MBeanConstructorInfo;
import javax.management.MBeanParameterInfo;

/**
 * OpenMBeanConstructorInfo implementation
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 *
 * @version $Revision: 57200 $
 *
 */
public class OpenMBeanConstructorInfoSupport
   extends MBeanConstructorInfo
   implements OpenMBeanConstructorInfo, Serializable
{
   // Constants -----------------------------------------------------

   private static final long serialVersionUID = -4400441579007477003L;

   // Attributes ----------------------------------------------------

   private transient int cachedHashCode;

   private transient String cachedToString;

   // Static --------------------------------------------------------

   private static MBeanParameterInfo[] convertArray(OpenMBeanParameterInfo[] array)
   {
      if (array == null)
         return null;
      MBeanParameterInfo[] result = new MBeanParameterInfo[array.length];
      System.arraycopy(array, 0, result, 0, array.length);
      return result;
   }

   // Constructors --------------------------------------------------

   /**
    * Contruct an OpenMBeanConstructorInfoSupport<p>
    *
    * @param name cannot be null or empty
    * @param description cannot be null or empty
    * @param signature the parameters of the constructor
    * @exception IllegalArgumentException when one of the above
    *            constraints is not satisfied
    */
   public OpenMBeanConstructorInfoSupport(String name, String description,
                                          OpenMBeanParameterInfo[] signature)
   {
      super(name, description, convertArray(signature));
      if (name == null)
         throw new IllegalArgumentException("null name");
      if (name.trim().length() == 0)
         throw new IllegalArgumentException("empty name");
      if (description == null)
         throw new IllegalArgumentException("null description");
      if (description.trim().length() == 0)
         throw new IllegalArgumentException("empty description");
   }

   // Public --------------------------------------------------------

   // OpenMBeanConstructorInfo Implementation -----------------------

   // Object Overrides ----------------------------------------------

   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null || !(obj instanceof OpenMBeanConstructorInfoSupport))
         return false;
      OpenMBeanConstructorInfo other = (OpenMBeanConstructorInfo) obj;

      if (getName().equals(other.getName()) == false)
         return false;

      if (Arrays.asList(getSignature()).equals(Arrays.asList(other.getSignature())) == false)
         return false;

      return true;
   }

   public int hashCode()
   {
      if (cachedHashCode != 0)
        return cachedHashCode;
      cachedHashCode = getName().hashCode();
      cachedHashCode += Arrays.asList(getSignature()).hashCode();
      return cachedHashCode;
   }

   public String toString()
   {
      if (cachedToString != null)
         return cachedToString;
      StringBuffer buffer = new StringBuffer(getClass().getName());
      buffer.append(": name=");
      buffer.append(getName());
      buffer.append(", signature=");
      buffer.append(Arrays.asList(getSignature()));
      cachedToString = buffer.toString();
      return cachedToString;
   }

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner Classes -------------------------------------------------
}
