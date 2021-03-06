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
package org.jboss.test.util.test;

import java.net.URL;
import javax.management.ObjectName;

import org.jboss.test.JBossTestCase;

/**
 * Test case for the Scheduler Utility. The test
 * checks if multiple scheduler can be created,
 * that the notifications goes to the right target
 * and that the reuse of the Scheduler works.
 *
 * @see org.jboss.util.Scheduler
 * @see org.jboss.util.SchedulerMBean
 *
 * @author Andreas Schaefer
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57211 $
 */
public class SchedulerUnitTestCase
   extends JBossTestCase
{
   /**
    * Constructor for the SchedulerUnitTestCase object
    *
    * @param name Test case name
    */
   public SchedulerUnitTestCase(String name)
   {
      super(name);
   }

   // Public --------------------------------------------------------

   /**
    * Checks if the Scheduler is deployed and if not then
    * deployed the default one now.
    */
   public void testDefaultScheduler()
      throws Exception
   {
      // The class loader used to locate the configuration file
      ClassLoader lLoader = Thread.currentThread().getContextClassLoader();
      assertTrue("ContextClassloader missing", lLoader != null);
      //Get URL for deployable *service.xml file in resources
      URL serviceURL = lLoader.getResource("util/test-default-scheduler-service.xml");
      if (serviceURL == null)
      {
         //if we're running from the jmxtest.jar, it should be here instead
         serviceURL = lLoader.getResource("test-default-scheduler-service.xml");
      }
      assertTrue("resource test-default-scheduler-service.xml not found", serviceURL != null);
      try
      {
         deploy(serviceURL.toString());
         ObjectName scheduler = new ObjectName("test:service=Scheduler");
         assertTrue("test:service=Scheduler isRegistered",
            getServer().isRegistered(scheduler));
         ObjectName ex1 = new ObjectName("test:name=SchedulableMBeanExample");
         assertTrue("test:name=SchedulableMBeanExample isRegistered",
            getServer().isRegistered(ex1));
         ObjectName scheduler2 = new ObjectName("test:service=Scheduler,name=SchedulableMBeanExample");
         assertTrue("test:service=Scheduler,name=SchedulableMBeanExample isRegistered",
            getServer().isRegistered(scheduler2));
      }
      finally
      {
         undeploy(serviceURL.toString());
      } // end of try-finally
   }

   /** Test the deployment of a ear containing a sar which creates an
    * instance of the org.jboss.varia.scheduler.Scheduler service with a
    * Schedulable class that exists in an external jar referenced by the
    * sar manifest.
    *
    * @throws Exception
    */
   public void testExternalServiceJar() throws Exception
   {
      // Deploy the external jar containg the Schedulable
      deploy("scheduler.jar");
      // Deploy the ear/sar
      deploy("scheduler.ear");

      try
      {
         ObjectName scheduler = new ObjectName("test:service=TestScheduler");
         assertTrue("test:service=TestScheduler isRegistered",
            getServer().isRegistered(scheduler));
      }
      finally
      {
         undeploy("scheduler.ear");
         undeploy("scheduler.jar");
      }
   }
}
