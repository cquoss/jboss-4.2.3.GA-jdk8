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
 * An In Query Expression.<p>
 *
 * Returns true only when any of the values are match.
 *
 * Returns true only when both expressions are true.
 *
 * <p><b>Revisions:</b>
 * <p><b>20020315 Adrian Brock:</b>
 * <ul>
 * <li>Don't put ; on the end of if statements :-)
 * </ul>
 * <p><b>20020317 Adrian Brock:</b>
 * <ul>
 * <li>Make queries thread safe
 * </ul>
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 */
class InQueryExp extends QueryEval implements QueryExp
{
   // Constants ---------------------------------------------------
   
   private static final long serialVersionUID = -5801329450358952434L;
   
   // Attributes --------------------------------------------------

   /**
    * The value to test
    */
   private ValueExp val;

   /**
    * The list of values 
    */
   private ValueExp[] valueList;

   // Static ------------------------------------------------------

   // Constructors ------------------------------------------------

   public InQueryExp()
   {
   }
   
   /**
    * Create a new IN query Expression
    * 
    * @param test the value to test
    * @param list the list of values
    */
   public InQueryExp(ValueExp test, ValueExp[] list)
   {
      this.val = test;
      this.valueList = list;
   }

   // Public ------------------------------------------------------

   // QueryExp implementation -------------------------------------

   public boolean apply(ObjectName name)
      throws BadStringOperationException,
      BadBinaryOpValueExpException,
      BadAttributeValueExpException,
      InvalidApplicationException
   {
      // REVIEW: Cast Exceptions
      ValueExp calcTest = val.apply(name);
      for (int i=0; i < valueList.length; i++)
      {
         ValueExp calcList = valueList[i].apply(name);
         // Number
         if (calcTest instanceof NumericValueExp)
         {
            if (((NumericValueExp)calcTest).getDoubleValue() ==
                ((NumericValueExp)calcList).getDoubleValue())
               return true;
         }
         // String
         else if (calcTest instanceof StringValueExp)
         {
            if ( calcTest.toString().equals(calcList.toString()) )
               return true;
         }
         // Single Value, includes Boolean
         else if (calcTest instanceof BooleanValueExp)
         {
            if (((BooleanValueExp) calcTest).getValue() ==
                ((BooleanValueExp) calcList).getValue())
               return true;
         }
      }
      // No match
      return false;
   }

   // Object overrides --------------------------------------------

   public String toString()
   {
      StringBuffer buffer = new StringBuffer("(");
      buffer.append(val.toString());
      buffer.append(" in ");
      for (int i = 1; i < valueList.length; i++)
      {
        buffer.append(valueList[i].toString());
        buffer.append(" ");
      }
      buffer.append(")");
      return buffer.toString();
   }

   // Protected ---------------------------------------------------

   // Private -----------------------------------------------------

   // Inner classes -----------------------------------------------
}
