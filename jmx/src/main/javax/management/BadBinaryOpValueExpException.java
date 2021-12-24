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
 * Thrown when an invalid expression is passed to a query construction
 * method.
 *
 * @see javax.management.ValueExp
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 */
public class BadBinaryOpValueExpException
   extends Exception
{
   // Constants -----------------------------------------------------

   private static final long serialVersionUID = 5068475589449021227L;

   // Attributes ----------------------------------------------------

   /**
    * The invalid expression.
    */
   private ValueExp exp = null;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Construct a new BadBinaryValueExpException with the given expression.
    *
    * @param exp the invalid expression
    */
   public BadBinaryOpValueExpException(ValueExp exp)
   {
      super();
      this.exp = exp;
   }

   // Public --------------------------------------------------------

   /**
    * Retrieve the bad binary value expression.
    *
    * @return the expression.
    */
   public ValueExp getExp()
   {
      return exp;
   }

   // Exception Overrides -------------------------------------------

   /**
    * Returns a string representing the error.
    *
    * @return the error string.
    */
    public String toString()
    {
       return "Bad binary operation value expression: " + exp.toString();
    }
}
