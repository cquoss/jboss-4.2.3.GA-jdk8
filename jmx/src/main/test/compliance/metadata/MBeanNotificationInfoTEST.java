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
package test.compliance.metadata;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import javax.management.MBeanNotificationInfo;

/**
 * MBean Notification Info tests.<p>
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 */
public class MBeanNotificationInfoTEST
  extends TestCase
{
   // Static --------------------------------------------------------------------

   private String[] types1 = new String[] { "type1", "type2" };
   private String[] types2 = new String[] { "typex", "type2" };

   // Attributes ----------------------------------------------------------------

   // Constructor ---------------------------------------------------------------

   /**
    * Construct the test
    */
   public MBeanNotificationInfoTEST(String s)
   {
      super(s);
   }

   // Tests ---------------------------------------------------------------------

   public void testMBeanNotificationInfo()
      throws Exception
   {
      MBeanNotificationInfo info = new MBeanNotificationInfo(types1,
         "name", "description");
      assertEquals("name", info.getName());
      assertEquals("description", info.getDescription());
      assertEquals(Arrays.asList(types1), Arrays.asList(info.getNotifTypes()));
   }

   public void testHashCode()
      throws Exception
   {
      MBeanNotificationInfo info1 = new MBeanNotificationInfo(types1, "name", "description");
      MBeanNotificationInfo info2 = new MBeanNotificationInfo(types1, "name", "description");

      assertTrue("Different instances with the same hashcode are equal", info1.hashCode() == info2.hashCode());
   }

   public void testEquals()
      throws Exception
   {
      MBeanNotificationInfo info = new MBeanNotificationInfo(types1,
         "name", "description");

      assertTrue("Null should not be equal", info.equals(null) == false);
      assertTrue("Only MBeanNotificationInfo should be equal", info.equals(new Object()) == false);

      MBeanNotificationInfo info2 = new MBeanNotificationInfo(types1,
         "name", "description");

      assertTrue("Different instances of the same data are equal", info.equals(info2));
      assertTrue("Different instances of the same data are equal", info2.equals(info));

      info2 = new MBeanNotificationInfo(types1,
         "name", "description2");

      assertTrue("Different instances with different descriptions are not equal", info.equals(info2) == false);
      assertTrue("Different instances with different descritpions are not equal", info2.equals(info) == false);

      info2 = new MBeanNotificationInfo(types1,
         "name2", "description");

      assertTrue("Instances with different names are not equal", info.equals(info2) == false);
      assertTrue("Instances with different names are not equal", info2.equals(info) == false);

      info2 = new MBeanNotificationInfo(types2,
         "name", "description");

      assertTrue("Instances with different types are not equal", info.equals(info2) == false);
      assertTrue("Instances with different types are not equal", info2.equals(info) == false);
   }

   public void testSerialization()
      throws Exception
   {
      MBeanNotificationInfo info = new MBeanNotificationInfo(types1,
         "name", "description");

      // Serialize it
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(info);
    
      // Deserialize it
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bais);
      Object result = ois.readObject();

      assertEquals(info, result);
   }

   public void testErrors()
      throws Exception
   {
      boolean caught = false;
      try
      {
         MBeanNotificationInfo info = new MBeanNotificationInfo(types1,
            null, "description");
      }
      catch (IllegalArgumentException e)
      {
         caught = true;
      }
      if (caught == false)
         fail("Expected IllegalArgumentException for null name");

      caught = false;
      try
      {
         MBeanNotificationInfo info = new MBeanNotificationInfo(types1,
            "", "description");
      }
      catch (IllegalArgumentException e)
      {
         caught = true;
      }
      if (caught == false)
         fail("Expected IllegalArgumentException for an empty name");

      caught = false;
      try
      {
         MBeanNotificationInfo info = new MBeanNotificationInfo(types1,
            "invalid name", "description");
      }
      catch (IllegalArgumentException e)
      {
         caught = true;
      }
      if (caught == false)
         fail("Expected IllegalArgumentException for an 'invalid name'");
   }
}
