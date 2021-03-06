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
package org.jboss.test.aop.test;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id: AOPUnitTestCase.java 57211 2006-09-26 12:39:46Z dimitris@jboss.org $
 */

public class AOPUnitTestCase
        extends JBossTestCase
{
   org.apache.log4j.Category log = getLog();

   static boolean deployed = false;
   static int test = 0;

   public AOPUnitTestCase(String name)
   {

      super(name);

   }

   public void testAspect() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AOPTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testAspect", params, sig);
   }

   public void testBasic() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AOPTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testBasic", params, sig);
   }

   public void testCallerPointcut() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AOPTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testCallerPointcut", params, sig);
   }

   public void testInheritance() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AOPTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testInheritance", params, sig);
   }

   public void testMetadata() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AOPTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testMetadata", params, sig);
   }

   public void testDynamicInterceptors() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AOPTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testDynamicInterceptors", params, sig);
   }

   public void testFieldInterception() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AOPTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testFieldInterception", params, sig);
   }

   public void testMixin() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AOPTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testMixin", params, sig);
   }


   public void testMethodInterception() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AOPTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testMethodInterception", params, sig);
   }

   public void testConstructorInterception() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AOPTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testConstructorInterception", params, sig);
   }

   public void testExceptions() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AOPTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testExceptions", params, sig);
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(AOPUnitTestCase.class));

      AOPTestSetup setup = new AOPTestSetup(suite, "aoptest.sar");
      return setup; 
   }

}
