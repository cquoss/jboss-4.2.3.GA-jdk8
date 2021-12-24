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

import java.util.Set;

/**
 * An open MBean parameter info implements this interface as well as extending
 * MBeanParameterInfo.<p>
 * 
 * {@link OpenMBeanParameterInfoSupport} is an example of such a class.
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 *
 * @version $Revision: 57200 $
 *
 */
public interface OpenMBeanParameterInfo
{
   // Attributes ----------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   /**
    * Retrieve a human readable description of the open MBean parameter the
    * implementation of this interface describes.
    *
    * @return the description.
    */
   String getDescription();

   /**
    * Retrieve the name of the parameter described.
    *
    * @return the name.
    */
   String getName();

   /**
    * Retrieve the open type for this parameter
    *
    * @return the open type.
    */
   OpenType getOpenType();

   /**
    * Retrieve the default value for this parameter or null if there is no
    * default
    *
    * @return the default value
    */
   Object getDefaultValue();

   /**
    * Retrieve the legal values values for this parameter or null if this
    * is not specified
    *
    * @return the legal value
    */
   Set getLegalValues();

   /**
    * Retrieve the minimum values values for this parameter or null if this
    * is not specified
    *
    * @return the minimum value
    */
   Comparable getMinValue();

   /**
    * Retrieve the maximum values values for this parameter or null if this
    * is not specified
    *
    * @return the maximum value
    */
   Comparable getMaxValue();

   /**
    * Discover wether this parameter has a default value specified
    *
    * @return true when a default value is specified or false otherwise
    */
   boolean hasDefaultValue();

   /**
    * Discover wether this parameter has legal values specified
    *
    * @return true when the legal values are specified or false otherwise
    */
   boolean hasLegalValues();

   /**
    * Discover wether this parameter has a minimum value specified
    *
    * @return true when a minimum value is specified or false otherwise
    */
   boolean hasMinValue();

   /**
    * Discover wether this parameter has a maximum value specified
    *
    * @return true when a maximum value is specified or false otherwise
    */
   boolean hasMaxValue();

   /**
    * Tests whether an object is acceptable as a paramter value
    *
    * @param obj the object to test
    * @return true when it is a valid value, or false otherwise
    */
   boolean isValue(Object obj);

   /**
    * Compares an object for equality with the implementing class.<p>
    *
    * The object is not null<br>
    * The object implements the open mbean attribute info interface<br>
    * The parameter names are equal<br>
    * The open types are equal<br>
    * The default, min, max and legal values are equal
    *
    * @param obj the object to test
    * @return true when above is true, false otherwise
    */
   boolean equals(Object obj);

   /**
    * Generates a hashcode for the implementation.<p>
    *
    * The sum of the hashCodes for the elements mentioned in the equals
    * method
    *
    * @return the calculated hashcode
    */
   int hashCode();

   /**
    * A string representation of the open mbean parameter info.<p>
    *
    * It is made up of implementation class and the values mentioned
    * in the equals method
    *
    * @return the string
    */
   String toString();
}
