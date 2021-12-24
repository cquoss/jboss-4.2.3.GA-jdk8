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
 * A Binary Comparison query.
 *
 * <p><b>Revisions:</b>
 * <p><b>20020314 Adrian Brock:</b>
 * <ul>
 * <li>Added human readable string representation.            
 * </ul>
 * <p><b>20020317 Adrian Brock:</b>
 * <ul>
 * <li>Make queries thread safe
 * </ul>
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 */
class BinaryRelQueryExp extends QueryEval implements QueryExp
{
   // Constants ---------------------------------------------------

   private static final long serialVersionUID = -5690656271650491000L;
   
   // Attributes --------------------------------------------------

   /**
    * The operation
    */
   private int relOp;

   /**
    * The first expression
    */
   private ValueExp exp1;

   /**
    * The second expression
    */
   private ValueExp exp2;


   // Constructors ------------------------------------------------

   public BinaryRelQueryExp()
   {
   }
   
   /**
    * Construct a binary comparison query
    *
    * @param operation the comparison as defined in Query
    * @param first the first expression in the query
    * @param second the second expression in the query
    */
   public BinaryRelQueryExp(int operation, ValueExp first, ValueExp second)
   {
      this.relOp = operation;
      this.exp1 = first;
      this.exp2 = second;
   }

   // Public ------------------------------------------------------

   // Query Exp Implementation ------------------------------------

   public boolean apply(ObjectName name)
      throws BadStringOperationException,
      BadBinaryOpValueExpException,
      BadAttributeValueExpException,
      InvalidApplicationException
   {
      ValueExp testFirst = exp1.apply(name);
      ValueExp testSecond = exp2.apply(name);

      if (testFirst instanceof NumericValueExp && testSecond instanceof NumericValueExp)
      {
         switch (relOp)
         {
         case Query.GT:
            return ((NumericValueExp)testFirst).getDoubleValue() > 
                   ((NumericValueExp)testSecond).getDoubleValue();
         case Query.GE:
            return ((NumericValueExp)testFirst).getDoubleValue() >=
                   ((NumericValueExp)testSecond).getDoubleValue();
         case Query.LT:
            return ((NumericValueExp)testFirst).getDoubleValue() <
                   ((NumericValueExp)testSecond).getDoubleValue();
         case Query.LE:
            return ((NumericValueExp)testFirst).getDoubleValue() <=
                   ((NumericValueExp)testSecond).getDoubleValue();
         case Query.EQ:
            return ((NumericValueExp)testFirst).getDoubleValue() ==
                   ((NumericValueExp)testSecond).getDoubleValue();
         default:
            // fall through to the exception at the end of the method
            break;
         }
      }
      else if (testFirst instanceof StringValueExp && testSecond instanceof StringValueExp)
      {
         switch (relOp)
         {
         case Query.GT:
            return testFirst.toString().compareTo( 
                   testSecond.toString()) > 0;
         case Query.GE:
            return testFirst.toString().compareTo( 
                   testSecond.toString()) >= 0;
         case Query.LT:
            return testFirst.toString().compareTo( 
                   testSecond.toString()) < 0;
         case Query.LE:
            return testFirst.toString().compareTo( 
                   testSecond.toString()) <= 0;
         case Query.EQ:
            return testFirst.toString().compareTo( 
                   testSecond.toString()) == 0;
         default:
            throw new BadStringOperationException("TODO");
         }
      }
      else if (testFirst instanceof BooleanValueExp && testSecond instanceof BooleanValueExp)
      {
         switch (relOp)
         {
         case Query.EQ:
            return ((BooleanValueExp) testFirst).getValue() == 
                   ((BooleanValueExp) testSecond).getValue();
         default:
            // fall through to the exception at the end of the method
            break;
         }
      }
      // Review What happens now?
      throw new BadBinaryOpValueExpException(testFirst);
   }

   // Object overrides --------------------------------------------

   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("(");
      buffer.append(exp1);
      buffer.append(")");
      switch (relOp)
      {
      case Query.GT:
         buffer.append(" > "); break;
      case Query.GE:
         buffer.append(" >= "); break;
      case Query.LT:
         buffer.append(" < "); break;
      case Query.LE:
         buffer.append(" <= "); break;
      case Query.EQ:
         buffer.append(" == ");
      }
      buffer.append("(");
      buffer.append(exp2);
      buffer.append(")");
      return buffer.toString();
   }

}
