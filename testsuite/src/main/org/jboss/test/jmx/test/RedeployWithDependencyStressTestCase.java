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
package org.jboss.test.jmx.test;

import java.util.Collection;
import java.util.Collections;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.monitor.EntityLockMonitor;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.system.ServiceMBean;
import org.jboss.test.JBossTestCase;

/**
 * Stresses redeployment
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57211 $
 */

public class RedeployWithDependencyStressTestCase extends JBossTestCase
{
   public RedeployWithDependencyStressTestCase(String name)
   {
      super(name);
   }

   public void testEarRedeployWithDependency() throws Exception
   {
      ObjectName dependent = new ObjectName("jboss.test:test=RedeployWithDependency");
      ObjectName dependee = new ObjectName("jboss.security.tests:service=WebIntegrationLoginConfig");
      String testUrl = "jbosstest-web.ear";
      
      MBeanServerConnection server = getServer();
      
      createMBean(server, dependent, dependee);
      try
      {
         assertState(ServiceMBean.REGISTERED, dependent);
         for (int i = 0; i < getIterationCount(); ++i)
         {
            try
            {
               deploy(testUrl);
               assertState(ServiceMBean.STARTED, dependent);
            }
            finally
            {
               undeploy(testUrl);
            }
            assertState(ServiceMBean.DESTROYED, dependent);
         }
      }
      finally
      {
         removeMBean(server, dependent);
      }
   }

   protected void assertState(int expected, ObjectName service) throws Exception
   {
      MBeanServerConnection server = getServer();
      Integer actual = (Integer) server.getAttribute(service, "State");
      assertEquals("Expected state=" + expected + " actual=" + actual, expected, actual.intValue());
   }
   
   protected void createMBean(MBeanServerConnection server, ObjectName dependent, ObjectName dependee) throws Exception
   {
      server.createMBean(EntityLockMonitor.class.getName(), dependent);
      try
      {
         server.invoke(ServiceControllerMBean.OBJECT_NAME, "create",
         new Object[]
         {
            dependent,
            Collections.singleton(dependee)
         },
         new String[]
         {
            ObjectName.class.getName(),
            Collection.class.getName()
         }
         );
         server.invoke(ServiceControllerMBean.OBJECT_NAME, "create",
         new Object[]
         {
            dependent
         },
         new String[]
         {
             ObjectName.class.getName()
         }
         );
      }
      catch (Exception e)
      {
         server.unregisterMBean(dependent);
         throw e;
      }
   }
   
   protected void removeMBean(MBeanServerConnection server, ObjectName dependent) throws Exception
   {
      server.unregisterMBean(dependent);
   }
}
