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
package org.jboss.test.jbossmx.compliance.standard;

import org.jboss.test.jbossmx.compliance.TestCase;
import org.jboss.test.jbossmx.compliance.standard.support.Trivial;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 */

public class TrivialTestCase
   extends TestCase
{
   public TrivialTestCase(String s)
   {
      super(s);
   }

   public void testRegistration()
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      Trivial trivial = new Trivial();

      ObjectName name = null;
      try
      {
         name = new ObjectName("trivial:key=val");
         ObjectInstance instance = server.registerMBean(trivial, name);
      }
      catch (Exception e)
      {
         fail("registration failed: " + e.getMessage());
      }
      assertTrue("expected server to report it as registered", server.isRegistered(name));
   }

   public void testConstructorInfo()
   {
      MBeanInfo info = getTrivialInfo();

      MBeanConstructorInfo[] constructors = info.getConstructors();
      assertEquals("constructor list length", 1, constructors.length);

      // I really don't feel like reflecting to get the name of the constructor,
      // it should just be the name of the class right?
      assertEquals("constructor name", Trivial.class.getName(), constructors[0].getName());

      MBeanParameterInfo[] params = constructors[0].getSignature();
      assertEquals("constructor signature length", 0, params.length);
   }

   public void testAttributeInfo()
   {
      MBeanInfo info = getTrivialInfo();

      MBeanAttributeInfo[] attributes = info.getAttributes();
      assertEquals("attribute list length", 1, attributes.length);
      assertEquals("attribute name", "Something", attributes[0].getName());
      assertEquals("attribute type", String.class.getName(), attributes[0].getType());
      assertEquals("attribute readable", true, attributes[0].isReadable());
      assertEquals("attribute writable", true, attributes[0].isWritable());
      assertEquals("attribute isIs", false, attributes[0].isIs());
   }

   public void testOperationInfo()
   {
      MBeanInfo info = getTrivialInfo();

      MBeanOperationInfo[] operations = info.getOperations();
      assertEquals("operations list length", 1, operations.length);
      assertEquals("operation name", "doOperation", operations[0].getName());
      assertEquals("operation return type", Void.TYPE.getName(), operations[0].getReturnType());
      assertEquals("operation impact", MBeanOperationInfo.UNKNOWN, operations[0].getImpact());

      MBeanParameterInfo[] params = operations[0].getSignature();
      assertEquals("signature length", 1, params.length);
      assertEquals("parameter type", String.class.getName(), params[0].getType());
   }

   public void testNotificationInfo()
   {
      MBeanInfo info = getTrivialInfo();

      MBeanNotificationInfo[] notifications = info.getNotifications();
      assertEquals("notification list length", 0, notifications.length);
   }


   private MBeanInfo getTrivialInfo()
   {
      MBeanInfo info = null;

      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         Trivial trivial = new Trivial();

         ObjectName name = new ObjectName("trivial:key=val");
         ObjectInstance instance = server.registerMBean(trivial, name);
         info = server.getMBeanInfo(name);
      }
      catch (MalformedObjectNameException e)
      {
         fail("got spurious MalformedObjectNameException");
      }
      catch (InstanceAlreadyExistsException e)
      {
         fail("got spurious InstanceAlreadyExistsException");
      }
      catch (MBeanRegistrationException e)
      {
         fail("got spurious MBeanRegistrationException");
      }
      catch (NotCompliantMBeanException e)
      {
         fail("got spurious NotCompliantMBeanException");
      }
      catch (InstanceNotFoundException e)
      {
         fail("got spurious InstanceNotFoundException");
      }
      catch (IntrospectionException e)
      {
         fail("got spurious IntrospectionException");
      }
      catch (ReflectionException e)
      {
         fail("got spurious ReflectionException");
      }

      return info;
   }
}
