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
package javax.management;

/**
 * Thrown when an attempt is made to apply either of the following.
 * A subquery expression to an MBean or a qualified expression to an
 * MBean of the wrong class.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 */
public class InvalidApplicationException
   extends Exception
{
   // Constants -----------------------------------------------------
   /** @since 4.0.1 */
   private static final long serialVersionUID = -3048022274675537269L;

   // Attributes ----------------------------------------------------

   private Object val = null;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Construct a new IntrospectionException with the specified object.
    *
    * @param val the specified object.
    */
   public InvalidApplicationException(Object val)
   {
      super();
      this.val = val;
   }

   // Exception Overrides -------------------------------------------

   /**
    * Get a string represention of the exception.
    *
    * @return the string representation of the exception.
    */
   public String toString()
   {
      return "Invalid Application: " + val.toString();
   }
}

