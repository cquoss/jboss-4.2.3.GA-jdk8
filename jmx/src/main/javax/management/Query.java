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
 * This class is a factory for constructing queries.<p>
 *
 * REVIEW: Full explanation. See the spec for now for what it's worth.
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 */
public class Query
{
   // Constants ---------------------------------------------------

   /**
    * Divide expression
    */
   public static final int DIV = 3;

   /**
    * Equals expression
    */
   public static final int EQ = 4;

   /**
    * Greater than or equals expression
    */
   public static final int GE = 2;

   /**
    * Greater than expression
    */
   public static final int GT = 0;

   /**
    * Less than or equals expression
    */
   public static final int LE = 3;

   /**
    * Less than expression
    */
   public static final int LT = 1;

   /**
    * Minus expression
    */
   public static final int MINUS = 1;

   /**
    * Plus expression
    */
   public static final int PLUS = 0;

   /**
    * Times expression
    */
   public static final int TIMES = 2;

   // Attributes --------------------------------------------------

   // Static  -----------------------------------------------------

   /**
    * And Query expression. Return true only when both expressions are true.
    *
    * @param first the first expression
    * @param second the second expression
    * @return the expression
    */
   public static QueryExp and(QueryExp first, QueryExp second)
   {
      return new AndQueryExp(first, second);
   }

   /**
    * Tests an attribute contains a string as a subset. Return true
    * when this is the case, false otherwise.
    *
    * @param attr the attribute 
    * @param string the string
    * @return the expression
    */
   public static QueryExp anySubString(AttributeValueExp attr, 
                                       StringValueExp string)
   {
      return new MatchQueryExp(attr, "*" + string.getValue() + "*");
   }

   /**
    * An attribute expression
    *
    * @param value the name of the attribute
    * @return the expression
    */
   public static AttributeValueExp attr(String value)
   {
      return new AttributeValueExp(value);
   }

   /**
    * An attribute expression restricted to a specific class
    *
    * @param className the name of the class
    * @param value the name of the attribute
    * @return the expression
    */
   public static AttributeValueExp attr(String className, String value)
   {
      return new QualifiedAttributeValueExp(className, value);
   }

   /**
    * Tests a value is between two other values. Returns true when this is
    * case, false otherwise.
    *
    * @param test the value to test
    * @param lower the lower bound
    * @param higher the higer bound
    * @return the expression
    */
   public static QueryExp between(ValueExp test, ValueExp lower,
                                  ValueExp higher)
   {
      return new BetweenQueryExp(test, lower, higher);
   }

   /**
    * What is this?
    *
    * @return the expression
    */
   public static AttributeValueExp classattr()
   {
      return new ClassAttributeValueExp();
   }

   /**
    * An expression that divides the first expression by the second
    *
    * @param first the first expression
    * @param second the second expression
    * @return the expression
    */
   public static ValueExp div(ValueExp first, ValueExp second)
   {
      return new BinaryOpValueExp(DIV, first, second);
   }

   /**
    * Equals Comparison.
    *
    * @param first the first expression
    * @param second the second expression
    * @return true when first equals second
    */
   public static QueryExp eq(ValueExp first, ValueExp second)
   {
      return new BinaryRelQueryExp(EQ, first, second);
   }

   /**
    * Tests an attribute ends with a string as a subset. Return true
    * when this is the case, false otherwise.
    *
    * @param attr the attribute 
    * @param string the string
    * @return the expression
    */
   public static QueryExp finalSubString(AttributeValueExp attr, 
                                         StringValueExp string)
   {
      return new MatchQueryExp(attr, "*" + string.getValue());
   }

   /**
    * Greater than or Equals Comparison.
    *
    * @param first the first expression
    * @param second the second expression
    * @return true when first >= second
    */
   public static QueryExp geq(ValueExp first, ValueExp second)
   {
      return new BinaryRelQueryExp(GE, first, second);
   }

   /**
    * Greater than.
    *
    * @param first the first expression
    * @param second the second expression
    * @return true when first > second
    */
   public static QueryExp gt(ValueExp first, ValueExp second)
   {
      return new BinaryRelQueryExp(GT, first, second);
   }

   /**
    * Tests a value is in one of the listed values. Returns true when this is
    * case, false otherwise.
    *
    * @param test the value to test
    * @param list an array of values
    * @return the expression
    */
   public static QueryExp in(ValueExp test, ValueExp[] list)
   {
      return new InQueryExp(test, list);
   }

   /**
    * Tests an attribute starts with a string as a subset. Return true
    * when this is the case, false otherwise.
    *
    * @param attr the attribute 
    * @param string the string
    * @return the expression
    */
   public static QueryExp initialSubString(AttributeValueExp attr, 
                                           StringValueExp string)
   {
      return new MatchQueryExp(attr, string.getValue() + "*");
   }

   /**
    * Less than or equal.
    *
    * @param first the first expression
    * @param second the second expression
    * @return true when first <= second
    */
   public static QueryExp leq(ValueExp first, ValueExp second)
   {
      return new BinaryRelQueryExp(LE, first, second);
   }

   /**
    * Less than.
    *
    * @param first the first expression
    * @param second the second expression
    * @return true when first < second
    */
   public static QueryExp lt(ValueExp first, ValueExp second)
   {
      return new BinaryRelQueryExp(LT, first, second);
   }

   /**
    * Tests an attribute equals a string value. Return true
    * when this is the case, false otherwise.
    *
    * @param attr the attribute 
    * @param string the string
    * @return the expression
    */
   public static QueryExp match(AttributeValueExp attr, 
                                StringValueExp string)
   {
      return new MatchQueryExp(attr, string.getValue());
   }

   /**
    * An expression that subtracts the second expression from the first
    *
    * @param first the first expression
    * @param second the second expression
    * @return the expression
    */
   public static ValueExp minus(ValueExp first, ValueExp second)
   {
      return new BinaryOpValueExp(MINUS, first, second);
   }

   /**
    * Not Query expression. Return true only when expression is false.
    *
    * @param expression the expression to negate
    * @return the expression
    */
   public static QueryExp not(QueryExp expression)
   {
      return new NotQueryExp(expression);
   }

   /**
    * Or Query expression. Return true when either expression is true.
    *
    * @param first the first expression
    * @param second the second expression
    * @return the expression
    */
   public static QueryExp or(QueryExp first, QueryExp second)
   {
      return new OrQueryExp(first, second);
   }

   /**
    * An expression that adds the second expression to the first
    *
    * @param first the first expression
    * @param second the second expression
    * @return the expression
    */
   public static ValueExp plus(ValueExp first, ValueExp second)
   {
      return new BinaryOpValueExp(PLUS, first, second);
   }

   /**
    * An expression that multiplies the first expression by the second
    *
    * @param first the first expression
    * @param second the second expression
    * @return the expression
    */
   public static ValueExp times(ValueExp first, ValueExp second)
   {
      return new BinaryOpValueExp(TIMES, first, second);
   }

   /**
    * Create a boolean value expression for use in a Query.
    *
    * @return the expression
    */
   public static ValueExp value(boolean value)
   {
      return new BooleanValueExp(new Boolean(value));
   }

   /**
    * Create a double value expression for use in a Query.
    *
    * @return the expression
    */
   public static ValueExp value(double value)
   {
      return new NumericValueExp(new Double(value));
   }

   /**
    * Create a float value expression for use in a Query.
    *
    * @return the expression
    */
   public static ValueExp value(float value)
   {
      return new NumericValueExp(new Double(value));
   }

   /**
    * Create an integer value expression for use in a Query.
    *
    * @return the expression
    */
   public static ValueExp value(int value)
   {
      return new NumericValueExp(new Long(value));
   }

   /**
    * Create a long value expression for use in a Query.
    *
    * @return the expression
    */
   public static ValueExp value(long value)
   {
      return new NumericValueExp(new Long(value));
   }

   /**
    * Create a number value expression for use in a Query.
    *
    * @return the expression
    */
   public static ValueExp value(Number value)
   {
      return new NumericValueExp(value);
   }

   /**
    * Create a string value expression for use in a Query.
    *
    * @return the expression
    */
   public static StringValueExp value(String value)
   {
      return new StringValueExp(value);
   }

   // Constructors ------------------------------------------------

   /**
    * Construct a new Query
    */
   public Query()
   {
   }

   // Public ------------------------------------------------------

   // X Implementation --------------------------------------------

   // Y overrides -------------------------------------------------

   // Protected ---------------------------------------------------

   // Package Private ---------------------------------------------

   // Private -----------------------------------------------------

   // Inner Classes -----------------------------------------------
}
