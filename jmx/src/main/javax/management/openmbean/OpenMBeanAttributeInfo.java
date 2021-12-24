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


/**
 * An open MBean attribute info implements this interface as well as extending
 * MBeanAttributeInfo.<p>
 * 
 * {@link OpenMBeanAttributeInfoSupport} is an example of such a class.
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 *
 * @version $Revision: 57200 $
 *
 */
public interface OpenMBeanAttributeInfo
   extends OpenMBeanParameterInfo
{
   // Attributes ----------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   /**
    * Test whether an attribute is readable.
    *
    * @return true when the attribute is readable or false otherwise
    */
   boolean isReadable();

   /**
    * Test whether an attribute is writable.
    *
    * @return true when the attribute is writable or false otherwise
    */
   boolean isWritable();

   /**
    * Test whether an attribute is accessed through an isXXX getter.
    *
    * @return the result of the above test
    */
   boolean isIs();

   /**
    * Compares an object for equality with the implementing class.<p>
    *
    * The object is not null<br>
    * The object implements the open mbean parameter info interface<br>
    * The parameter names are equal<br>
    * The open types are equal<br>
    * The access properties are the same<br>
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
