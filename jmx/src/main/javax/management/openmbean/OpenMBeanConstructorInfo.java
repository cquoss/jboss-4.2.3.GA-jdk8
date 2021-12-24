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

import javax.management.MBeanParameterInfo;

/**
 * An open MBean constructor info implements this interface as well as extending
 * MBeanConstructorInfo.<p>
 * 
 * {@link OpenMBeanConstructorInfoSupport} is an example of such a class.
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 *
 * @version $Revision: 57200 $
 *
 */
public interface OpenMBeanConstructorInfo
{
   // Attributes ----------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   /**
    * Retrieve a human readable description of the open MBean constructor the
    * implementation of this interface describes.
    *
    * @return the description.
    */
   String getDescription();

   /**
    * Retrieve the name of the constructor described.
    *
    * @return the name.
    */
   String getName();

   /**
    * Returns an array of the parameters passed to the constructor<p>
    *
    * The parameters must be OpenMBeanParameterInfos.
    *
    * @return the constructor's parameters.
    */
   MBeanParameterInfo[] getSignature();

   /**
    * Compares an object for equality with the implementing class.<p>
    *
    * The object is not null<br>
    * The object implements the open mbean constructor info interface<br>
    * The constructor names are equal<br>
    * The signatures are equal<br>
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
    * A string representation of the open mbean constructor info.<p>
    *
    * It is made up of implementation class and the values mentioned
    * in the equals method
    *
    * @return the string
    */
   String toString();
}
