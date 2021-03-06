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
package test.compliance.openmbean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;

import junit.framework.TestCase;

/**
 * Open type tests.<p>
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 */
public class OpenTypeTestCase
  extends TestCase
{
   // Attributes ----------------------------------------------------------------

   // Constructor ---------------------------------------------------------------

   /**
    * Construct the test
    */
   public OpenTypeTestCase(String s)
   {
      super(s);
   }

   // Tests ---------------------------------------------------------------------

   /**
    * Test Allowed Classes.
    */
   public void testAllowedClasses()
      throws Exception
   {
      String[] allowedClassNames = OpenType.ALLOWED_CLASSNAMES;
      assertEquals(16, allowedClassNames.length);
      List names = Arrays.asList(allowedClassNames);
      checkOpenType(names, java.lang.Void.class);
      checkOpenType(names, java.lang.Boolean.class);
      checkOpenType(names, java.lang.Character.class);
      checkOpenType(names, java.lang.Byte.class);
      checkOpenType(names, java.lang.Short.class);
      checkOpenType(names, java.lang.Integer.class);
      checkOpenType(names, java.lang.Long.class);
      checkOpenType(names, java.lang.Float.class);
      checkOpenType(names, java.lang.Double.class);
      checkOpenType(names, java.lang.String.class);
      checkOpenType(names, java.util.Date.class);
      checkOpenType(names, java.math.BigDecimal.class);
      checkOpenType(names, java.math.BigInteger.class);
      checkOpenType(names, javax.management.ObjectName.class);
      checkOpenType(names, javax.management.openmbean.CompositeData.class);
      checkOpenType(names, javax.management.openmbean.TabularData.class);
   }

   public void testConstructorSimple()
      throws Exception
   {
      OpenType test = new MyOpenType("java.lang.Void", "type", "description");
      assertEquals("java.lang.Void", test.getClassName());
      assertEquals("type", test.getTypeName());
      assertEquals("description", test.getDescription());
      assertEquals(false, test.isArray());
   }

   public void testConstructorArray()
      throws Exception
   {
      OpenType test = new MyOpenType("[[Ljava.lang.Void;", "type", "description");
      assertEquals("[[Ljava.lang.Void;", test.getClassName());
      assertEquals("type", test.getTypeName());
      assertEquals("description", test.getDescription());
      assertEquals(true, test.isArray());
   }

   public void testSerializationSimple()
      throws Exception
   {
      testSerialization("java.lang.Void", "type", "description");
   }
 
   public void testSerializationArray()
      throws Exception
   {
      testSerialization("[[Ljava.lang.Void;", "type", "description");
   }

   /**
    * Test Errors.
    */
   public void testErrors()
      throws Exception
   {
      boolean caught = false;
      try
      {
         new MyOpenType(null, "dummy", "dummy");
      }
      catch (IllegalArgumentException e)
      {
         caught = true;
      }
      assertTrue("className cannot be null", caught);

      caught = false;
      try
      {
         new MyOpenType("", "dummy", "dummy");
      }
      catch (IllegalArgumentException e)
      {
         caught = true;
      }
      assertTrue("className cannot be empty", caught);

      caught = false;
      try
      {
         new MyOpenType("java.lang.Void", null, "dummy");
      }
      catch (IllegalArgumentException e)
      {
         caught = true;
      }
      assertTrue("typeName cannot be null", caught);

      caught = false;
      try
      {
         new MyOpenType("java.lang.Void", null, "dummy");
      }
      catch (IllegalArgumentException e)
      {
         caught = true;
      }
      assertTrue("typeName cannot be empty", caught);

      caught = false;
      try
      {
         new MyOpenType("java.lang.Void", "dummy", null);
      }
      catch (IllegalArgumentException e)
      {
         caught = true;
      }
      assertTrue("description cannot be null", caught);

      caught = false;
      try
      {
         new MyOpenType("java.lang.Void", "dummy", "");
      }
      catch (IllegalArgumentException e)
      {
         caught = true;
      }
      assertTrue("description cannot be empty", caught);

      caught = false;
      try
      {
         new MyOpenType("java.lang.Class", "dummy", "dummy");
      }
      catch (OpenDataException e)
      {
         caught = true;
      }
      assertTrue("className must be an OpenDataType", caught);

      caught = false;
      try
      {
         new MyOpenType("[Ljava.lang.Void", "dummy", "dummy");
      }
      catch (OpenDataException e)
      {
         caught = true;
      }
      assertTrue("[Ljava.lang.Void is not a valid array", caught);
   }

   /**
    * Test Errors that fail in the RI.
    */
   public void testErrors2()
      throws Exception
   {
      boolean caught = false;
      try
      {
         new MyOpenType("[L", "dummy", "dummy");
      }
      catch (StringIndexOutOfBoundsException e)
      {
         fail("FAILS IN RI: [L open type should be an OpenDataException not a StringIndexOutOfBoundsException");
      }
      catch (OpenDataException e)
      {
         caught = true;
      }
      assertTrue("[L is not a valid array", caught);
   }

   /**
    * Test Errors that fail in the RI.
    */
   public void testErrors3()
      throws Exception
   {
      boolean caught = false;
      try
      {
         new MyOpenType("[Xjava.lang.Void;", "dummy", "dummy");
      }
      catch (OpenDataException e)
      {
         caught = true;
      }
      assertTrue("FAILS IN RI: [Xjava.lang.Void; is not a valid array", caught);
   }

   // Support -------------------------------------------------------------------

   private void checkOpenType(List names, Class clazz)
      throws Exception
   {
      String name = clazz.getName();
      assertTrue(name + " is an OpenType", names.contains(name));

      new MyOpenType(name, "dummy", "dummy");

      new MyOpenType("[L"+name+";", "dummy", "dummy");

      new MyOpenType("[[[[[L"+name+";", "dummy", "dummy");
   }

   private void testSerialization(String className, String type, String description)
      throws Exception
   {
      // Create the role
      OpenType original = new MyOpenType(className, type, description);

      // Serialize it
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(original);
    
      // Deserialize it
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bais);
      OpenType result = (OpenType) ois.readObject();

      // Did it work?
      assertEquals(original.getClassName(), result.getClassName());
      assertEquals(original.getTypeName(), result.getTypeName());
      assertEquals(original.getDescription(), result.getDescription());
      assertEquals(original.isArray(), result.isArray());
   }

   public static class MyOpenType
      extends OpenType
   {
      public MyOpenType(String className, String typeName, String description)
         throws OpenDataException
      {
         super(className, typeName, description);
      }
      public boolean equals(Object other)
      {
         throw new UnsupportedOperationException("irrelevent");
      }
      public int hashCode()
      {
         throw new UnsupportedOperationException("irrelevent");
      }
      public boolean isValue(Object other)
      {
         throw new UnsupportedOperationException("irrelevent");
      }
      public String toString()
      {
         throw new UnsupportedOperationException("irrelevent");
      }
   }
}
