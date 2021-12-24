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

import org.jboss.mx.util.QueryExpSupport;


/**
 * A Between Query Expression.<p>
 *
 * Returns true only when the test expression is between the lower and
 * upper bounds inclusive.
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
class BetweenQueryExp extends QueryEval implements QueryExp
{
   // Constants ---------------------------------------------------
   
   private static final long serialVersionUID = -2933597532866307444L;
   
   // Attributes --------------------------------------------------

   /**
    * The value to test
    */
   private ValueExp exp1;

   /**
    * The lower bound
    */
   private ValueExp exp2;

   /**
    * The upper bound
    */
   private ValueExp exp3;

   // Static ------------------------------------------------------

   // Constructors ------------------------------------------------

   public BetweenQueryExp()
   {
   }
   
   /**
    * Create a new BETWEEN query Expression
    * 
    * @param test the value to test
    * @param lower the lower bound
    * @param upper the upper bound
    */
   public BetweenQueryExp(ValueExp test, ValueExp lower, ValueExp upper)
   {
      this.exp1 = test;
      this.exp2 = lower;
      this.exp3 = upper;
   }

   // Public ------------------------------------------------------

   // QueryExp implementation -------------------------------------

   public boolean apply(ObjectName name)
      throws BadStringOperationException,
      BadBinaryOpValueExpException,
      BadAttributeValueExpException,
      InvalidApplicationException
   {
      ValueExp calcTest = exp1.apply(name);
      ValueExp calcLower = exp2.apply(name);
      ValueExp calcUpper = exp3.apply(name);

      // Number
      if (calcTest instanceof NumericValueExp && calcLower instanceof NumericValueExp && calcUpper instanceof NumericValueExp)
      {
         // REVIEW: Exceptions for cast problems, which one?
         double valueTest = ((NumericValueExp) calcTest).getDoubleValue();
         double valueLower = ((NumericValueExp) calcLower).getDoubleValue();
         double valueUpper = ((NumericValueExp) calcUpper).getDoubleValue();
         return (valueLower <= valueTest && valueTest <= valueUpper);
      }
      // String
      else if (calcTest instanceof StringValueExp && calcLower instanceof StringValueExp && calcUpper instanceof StringValueExp)
      {
         // REVIEW: Exceptions for cast problems, which one?
         String valueTest = calcTest.toString();
         String valueLower = calcLower.toString();
         String valueUpper = calcUpper.toString();
         return (valueLower.compareTo(valueTest) <= 0 && 
                 valueUpper.compareTo(valueTest) >= 0);
      }
      // Review What happens now?
      throw new BadBinaryOpValueExpException(calcTest);
   }

   public void setMBeanServer(MBeanServer server)
   {
      QueryExpSupport.server.set(server);
   }

   // Object overrides --------------------------------------------

   public String toString()
   {
      return new String("(" +exp2.toString() + ") <= (" + exp1.toString() +
                        ") <= (" + exp3.toString()) + ")";
   }

   // Protected ---------------------------------------------------

   // Private -----------------------------------------------------

   // Inner classes -----------------------------------------------
}
