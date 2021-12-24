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
 * Thrown when an invalid string operation is passed to a query construction
 * method.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 *
 * <p><b>Revisions:</b>
 * <p><b>20020711 Adrian Brock:</b>
 * <ul>
 * <li> Serialization </li>
 * </ul>
 */
public class BadStringOperationException
   extends Exception
{
   // Constants -----------------------------------------------------

   private static final long serialVersionUID = 7802201238441662100L;

   // Attributes ----------------------------------------------------

   /**
    * The operation
    */
   private String op;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Construct a new BadStringOperationException with the given operation.
    *
    * @param op the invalid operation.
    */
   public BadStringOperationException(String op)
   {
      super(op);
      this.op = op;
   }

   // Public --------------------------------------------------------

   // Exception Overrides -------------------------------------------

   /**
    * Returns a string representing the error.
    *
    * @return the error string.
    */
   public String toString()
   {
      return "Bad string operation: " + op;
   }
}
