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
package org.jboss.test.classloader.test;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;

/** Unit tests for the org.jboss.mx.loading.UnifiedLoaderRepository
 * @author Scott.Stark@jboss.org
 * @version $Revision: 60355 $
 */
public class CircularityUnitTestCase extends JBossTestCase
{
   private static String JMX_NAME = "jboss.test:name=CircularityError";
   private ObjectName testObjectName;
   private Object[] args = {};
   private String[] sig = {};
   MBeanServerConnection server;

   public CircularityUnitTestCase(String name) throws Exception
   {
      super(name);
      testObjectName = new ObjectName(JMX_NAME);
      server = getServer();
   }

   /** Test the UnifiedLoaderRepository for ClassCircularityError
    */
   public void testDuplicateClass() throws Exception
   {
      server.invoke(testObjectName, "testDuplicateClass", args, sig);
   }
   public void testUCLOwner() throws Exception
   {
      server.invoke(testObjectName, "testUCLOwner", args, sig);
   }
   public void testMissingSuperClass() throws Exception
   {
      server.invoke(testObjectName, "testMissingSuperClass", args, sig);
   }
   public void testLoading() throws Exception
   {
      server.invoke(testObjectName, "testLoading", args, sig);
   }
   public void testPackageProtected() throws Exception
   {
      server.invoke(testObjectName, "testPackageProtected", args, sig);
   }
   public void testDeadlockCase1() throws Exception
   {
      server.invoke(testObjectName, "testDeadlockCase1", args, sig);
   }
   public void testRecursiveLoadMT() throws Exception
   {
      server.invoke(testObjectName, "testRecursiveLoadMT", args, sig);
   }

   /**
    * Setup the test suite by deploying the sar with the
    * {@link org.jboss.test.classloader.circularity.Starter} mbean
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(CircularityUnitTestCase.class));

      // Create an initializer for the test suite
      TestSetup wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            deploy("circularity.sar");
         }
         protected void tearDown() throws Exception
         {
            undeploy("circularity.sar");
            super.tearDown();
         }
      };
      return wrapper;
   }

}
