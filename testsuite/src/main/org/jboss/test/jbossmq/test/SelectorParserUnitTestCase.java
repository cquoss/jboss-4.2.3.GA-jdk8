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
package org.jboss.test.jbossmq.test;

import java.io.ByteArrayInputStream;
import java.util.HashMap;

import org.jboss.mq.selectors.ISelectorParser;
import org.jboss.mq.selectors.Identifier;
import org.jboss.mq.selectors.Operator;
import org.jboss.mq.selectors.SelectorParser;
import org.jboss.logging.Logger;
import junit.framework.TestCase;

/** Tests of the JavaCC LL(1) parser.
 
 @author Scott.Stark@jboss.org
 @author d_jencks@users.sourceforge.net
 
 @version $Revision: 57211 $
 
 * (david jencks)  Used constructor of SelectorParser taking a stream
 * to avoid reInit npe in all tests.  Changed to JBossTestCase and logging.
 */
public class SelectorParserUnitTestCase extends TestCase
{
   static Logger log = Logger.getLogger(SelectorParserUnitTestCase.class);
   static HashMap identifierMap = new HashMap();
   static ISelectorParser parser;
   
   public SelectorParserUnitTestCase(String name)
   {
      super(name);
   }
   
   protected void setUp() throws Exception
   {
      super.setUp();
      identifierMap.clear();
      if( parser == null )
      {
         parser = new SelectorParser(new ByteArrayInputStream(new byte[0]));
      }
   }
 
   public void testSimpleUnary() throws Exception
   {
      // Neg Long
      log.debug("parse(-12345 = -1 * 12345)");
      Operator result = (Operator) parser.parse("-12345 = -1 * 12345", identifierMap);
      log.debug("result -> "+result);
      Boolean b = (Boolean) result.apply();
      assertTrue("is true", b.booleanValue());

      // Neg Double
      log.debug("parse(-1 * 12345.67 = -12345.67)");
      result = (Operator) parser.parse("-1 * 12345.67 = -12345.67", identifierMap);
      log.debug("result -> "+result);
      b = (Boolean) result.apply();
      assertTrue("is true", b.booleanValue());

      log.debug("parse(-(1 * 12345.67) = -12345.67)");
      result = (Operator) parser.parse("-(1 * 12345.67) = -12345.67", identifierMap);
      log.debug("result -> "+result);
      b = (Boolean) result.apply();
      assertTrue("is true", b.booleanValue());
   }
   
   public void testPrecedenceNAssoc() throws Exception
   {
      log.debug("parse(4 + 2 * 3 / 2 = 7)");
      Operator result = (Operator) parser.parse("4 + 2 * 3 / 2 = 7", identifierMap);
      log.debug("result -> "+result);
      Boolean b = (Boolean) result.apply();
      assertTrue("is true", b.booleanValue());
      
      log.debug("parse(4 + ((2 * 3) / 2) = 7)");
      result = (Operator) parser.parse("4 + ((2 * 3) / 2) = 7", identifierMap);
      log.debug("result -> "+result);
      b = (Boolean) result.apply();
      assertTrue("is true", b.booleanValue());
      
      log.debug("parse(4 * -2 / -1 - 4 = 4)");
      result = (Operator) parser.parse("4 * -2 / -1 - 4 = 4", identifierMap);
      log.debug("result -> "+result);
      b = (Boolean) result.apply();
      assertTrue("is true", b.booleanValue());
      
      log.debug("parse(4 * ((-2 / -1) - 4) = -8)");
      result = (Operator) parser.parse("4 * ((-2 / -1) - 4) = -8", identifierMap);
      log.debug("result -> "+result);
      b = (Boolean) result.apply();
      assertTrue("is true", b.booleanValue());
   }
   
   public void testIds() throws Exception
   {
      log.debug("parse(a + b * c / d = e)");
      Operator result = (Operator) parser.parse("a + b * c / d = e", identifierMap);
      // 4 + 2 * 3 / 2 = 7
      Identifier a = (Identifier) identifierMap.get("a");
      a.setValue(new Long(4));
      Identifier b = (Identifier) identifierMap.get("b");
      b.setValue(new Long(2));
      Identifier c = (Identifier) identifierMap.get("c");
      c.setValue(new Long(3));
      Identifier d = (Identifier) identifierMap.get("d");
      d.setValue(new Long(2));
      Identifier e = (Identifier) identifierMap.get("e");
      e.setValue(new Long(7));
      log.debug("result -> "+result);
      Boolean bool = (Boolean) result.apply();
      assertTrue("is true", bool.booleanValue());
      
   }
   
   public void testTrueINOperator() throws Exception
   {
      log.debug("parse(Status IN ('new', 'cleared', 'acknowledged'))");
      Operator result = (Operator) parser.parse("Status IN ('new', 'cleared', 'acknowledged')", identifierMap);
      Identifier a = (Identifier) identifierMap.get("Status");
      a.setValue("new");
      log.debug("result -> "+result);
      Boolean bool = (Boolean) result.apply();
      assertTrue("is true", bool.booleanValue());
   }
   public void testFalseINOperator() throws Exception
   {
      log.debug("parse(Status IN ('new', 'cleared', 'acknowledged'))");
      Operator result = (Operator) parser.parse("Status IN ('new', 'cleared', 'acknowledged')", identifierMap);
      Identifier a = (Identifier) identifierMap.get("Status");
      a.setValue("none");
      log.debug("result -> "+result);
      Boolean bool = (Boolean) result.apply();
      assertTrue("is false", !bool.booleanValue());
   }
   
   public void testTrueOROperator() throws Exception
   {
      log.debug("parse((Status = 'new') OR (Status = 'cleared') OR (Status = 'acknowledged'))");
      Operator result = (Operator) parser.parse("(Status = 'new') OR (Status = 'cleared') OR (Status= 'acknowledged')", identifierMap);
      Identifier a = (Identifier) identifierMap.get("Status");
      a.setValue("new");
      log.debug("result -> "+result);
      Boolean bool = (Boolean) result.apply();
      assertTrue("is true", bool.booleanValue());
   }
   public void testFalseOROperator() throws Exception
   {
      log.debug("parse((Status = 'new') OR (Status = 'cleared') OR (Status = 'acknowledged'))");
      Operator result = (Operator) parser.parse("(Status = 'new') OR (Status = 'cleared') OR (Status = 'acknowledged')", identifierMap);
      Identifier a = (Identifier) identifierMap.get("Status");
      a.setValue("none");
      log.debug("result -> "+result);
      Boolean bool = (Boolean) result.apply();
      assertTrue("is false", !bool.booleanValue());
   }
   
   public void testInvalidSelector() throws Exception
   {
      log.debug("parse(definitely not a message selector!)");
      try
      {
         Object result = parser.parse("definitely not a message selector!", identifierMap);
         log.debug("result -> "+result);
         fail("Should throw an Exception.\n");
      }
      catch (Exception e)
      {
         log.info("testInvalidSelector failed as expected", e);
      }
   }
 
   /**
    * Test diffent syntax for approximate numeric literal (+6.2, -95.7, 7.)
    */
   public void testApproximateNumericLiteral1()
   {
      try
      {
         log.debug("parse(average = +6.2)");
         Object result = parser.parse("average = +6.2", identifierMap);
         log.debug("result -> "+result);
      } catch (Exception e)
      {
         fail(""+e);
      }
   }
   
   public void testApproximateNumericLiteral2()
   {
      try
      {
         log.debug("parse(average = -95.7)");
         Object result = parser.parse("average = -95.7", identifierMap);
         log.debug("result -> "+result);
      } catch (Exception e)
      {
         fail(""+e);
      }
   }
   public void testApproximateNumericLiteral3()
   {
      try
      {
         log.debug("parse(average = 7.)");
         Object result = parser.parse("average = 7.", identifierMap);
         log.debug("result -> "+result);
      } catch (Exception e)
      {
         fail(""+e);
      }
   }
   
   public void testGTExact()
   {
      try
      {
         log.debug("parse(weight > 2500)");
         Operator result = (Operator)parser.parse("weight > 2500", identifierMap);
         ((Identifier) identifierMap.get("weight")).setValue(new Integer(3000));
         log.debug("result -> "+result);
         Boolean bool = (Boolean) result.apply();
         assertTrue("is true", bool.booleanValue());
      } catch (Exception e)
      {
         log.debug("failed", e);
         fail(""+e);
      }
   }

   public void testGTFloat()
   {
      try
      {
         log.debug("parse(weight > 2500)");
         Operator result = (Operator)parser.parse("weight > 2500", identifierMap);
         ((Identifier) identifierMap.get("weight")).setValue(new Float(3000));
         log.debug("result -> "+result);
         Boolean bool = (Boolean) result.apply();
         assertTrue("is true", bool.booleanValue());
      } catch (Exception e)
      {
         log.debug("failed", e);
         fail(""+e);
      }
   }

   public void testLTDouble()
   {
      try
      {
         log.debug("parse(weight < 1.5)");
         Operator result = (Operator)parser.parse("weight < 1.5", identifierMap);
         ((Identifier) identifierMap.get("weight")).setValue(new Double(1.2));
         log.debug("result -> "+result);
         Boolean bool = (Boolean) result.apply();
         assertTrue("is true", bool.booleanValue());
      } catch (Exception e)
      {
         log.debug("failed", e);
         fail(""+e);
      }
   }

   public void testAndCombination()
   {
      try
      {
         log.debug("parse(JMSType = 'car' AND color = 'blue' AND weight > 2500)");
         Operator result = (Operator)parser.parse("JMSType = 'car' AND color = 'blue' AND weight > 2500", identifierMap);
         ((Identifier) identifierMap.get("JMSType")).setValue("car");
         ((Identifier) identifierMap.get("color")).setValue("blue");
         ((Identifier) identifierMap.get("weight")).setValue("3000");
         
         log.debug("result -> "+result);
         Boolean bool = (Boolean) result.apply();
         assertTrue("is false", !bool.booleanValue());
      } catch (Exception e)
      {
         log.debug("failed", e);
         fail(""+e);
      }
   }
   
   public void testINANDCombination()
   {
      try
      {
         log.debug("parse(Cateogry IN ('category1') AND Rating >= 2");
         Operator result = (Operator)parser.parse("Cateogry IN ('category1') AND Rating >= 2", identifierMap);
         ((Identifier) identifierMap.get("Cateogry")).setValue("category1");
         ((Identifier) identifierMap.get("Rating")).setValue(new Integer(3));
         log.debug("result -> "+result);
         Boolean bool = (Boolean) result.apply();
         assertTrue("is true", bool.booleanValue());
      } catch (Exception e)
      {
         log.debug("failed", e);
         fail(""+e);
      }
   }

   
   public static void main(java.lang.String[] args)
   {
      junit.textui.TestRunner.run(SelectorParserUnitTestCase.class);
   }
}
