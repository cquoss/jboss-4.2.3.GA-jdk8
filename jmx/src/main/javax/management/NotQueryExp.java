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
 * A NOT Query Expression.<p>
 *
 * Returns true when either expression is false.
 *
 * <p><b>Revisions:</b>
 * <p><b>20020314 Adrian Brock:</b>
 * <ul>
 * <li>Fix the human readable expression
 * </ul>
 * <p><b>20020317 Adrian Brock:</b>
 * <ul>
 * <li>Make queries thread safe
 * </ul>
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 */
class NotQueryExp extends QueryEval implements QueryExp
{
   // Constants ---------------------------------------------------

   private static final long serialVersionUID = 5269643775896723397L;

   // Attributes --------------------------------------------------

   /**
    * The query expression to negate
    */
   private QueryExp exp;

   // Static ------------------------------------------------------

   // Constructors ------------------------------------------------

   public NotQueryExp()
   {
   }

   /**
    * Create a new NOT query Expression
    * 
    * @param expression the query expression to negate
    */
   public NotQueryExp(QueryExp expression)
   {
      this.exp = expression;
   }

   // Public ------------------------------------------------------

   // QueryExp implementation -------------------------------------

   public boolean apply(ObjectName name)
      throws BadStringOperationException,
      BadBinaryOpValueExpException,
      BadAttributeValueExpException,
      InvalidApplicationException
   {
      return !exp.apply(name); 
   }

   // Object overrides --------------------------------------------

   public String toString()
   {
      return new String("!(" + exp.toString() + ")" );
   }

   // Protected ---------------------------------------------------

   // Private -----------------------------------------------------

   // Inner classes -----------------------------------------------
}
