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
 * A Binary Operation that is an arguement to a query.
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
class BinaryOpValueExp extends QueryEval implements ValueExp
{
   // Constants ---------------------------------------------------
   
   private static final long serialVersionUID = 1216286847881456786L;
   
   // Attributes --------------------------------------------------

   /**
    * The operation
    */
   private int op;

   /**
    * The first expression
    */
   private ValueExp exp1;

   /**
    * The second expression
    */
   private ValueExp exp2;


   // Constructors ------------------------------------------------

   public BinaryOpValueExp()
   {
   }
   
   /**
    * Construct a binary operation value
    *
    * @param operation the operation as defined in Query
    * @param first the first expression in the operation
    * @param second the second expression in the operation
    */
   public BinaryOpValueExp(int operation, ValueExp first, ValueExp second)
   {
      this.op = operation;
      this.exp1 = first;
      this.exp2 = second;
   }

   // Public ------------------------------------------------------

   // Value Exp Implementation ------------------------------------

   public ValueExp apply(ObjectName name)
      throws BadStringOperationException,
      BadBinaryOpValueExpException,
      BadAttributeValueExpException,
      InvalidApplicationException
   {
      ValueExp testFirst = exp1.apply(name);
      ValueExp testSecond = exp2.apply(name);

      if (testFirst instanceof NumericValueExp && testSecond instanceof NumericValueExp)
      {
         if (((NumericValueExp)testFirst).isInteger())
         {
            switch (op)
            {
            case Query.PLUS:
               return Query.value(((NumericValueExp)testFirst).getLongValue() + 
                                  ((NumericValueExp)testSecond).getLongValue());
            case Query.MINUS:
               return Query.value(((NumericValueExp)testFirst).getLongValue() -
                                  ((NumericValueExp)testSecond).getLongValue());
            case Query.TIMES:
               return Query.value(((NumericValueExp)testFirst).getLongValue() *
                                  ((NumericValueExp)testSecond).getLongValue());
            case Query.DIV:
               return Query.value(((NumericValueExp)testFirst).getLongValue() /
                                  ((NumericValueExp)testSecond).getLongValue());
            }
         }
         else
         {
            switch (op)
            {
            case Query.PLUS:
               return Query.value(((NumericValueExp)testFirst).getDoubleValue() + 
                                  ((NumericValueExp)testSecond).getDoubleValue());
            case Query.MINUS:
               return Query.value(((NumericValueExp)testFirst).getDoubleValue() -
                                  ((NumericValueExp)testSecond).getDoubleValue());
            case Query.TIMES:
               return Query.value(((NumericValueExp)testFirst).getDoubleValue() *
                                  ((NumericValueExp)testSecond).getDoubleValue());
            case Query.DIV:
               return Query.value(((NumericValueExp)testFirst).getDoubleValue() /
                                  ((NumericValueExp)testSecond).getDoubleValue());
            }
         }
      }
      else if (testFirst instanceof StringValueExp && testSecond instanceof StringValueExp)
      {
         switch (op)
         {
         case Query.PLUS:
            return Query.value(
               new String(testFirst.toString() + testSecond.toString())
            );
         }
         throw new BadStringOperationException("TODO");
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
      switch (op)
      {
      case Query.PLUS:
         buffer.append(" + "); break;
      case Query.MINUS:
         buffer.append(" - "); break;
      case Query.TIMES:
         buffer.append(" * "); break;
      case Query.DIV:
         buffer.append(" / ");
      }
      buffer.append("(");
      buffer.append(exp2);
      buffer.append(")");
      return buffer.toString();
   }

   // Protected ---------------------------------------------------

   // Package Private ---------------------------------------------

   // Private -----------------------------------------------------

   // Inner Classes -----------------------------------------------
}
