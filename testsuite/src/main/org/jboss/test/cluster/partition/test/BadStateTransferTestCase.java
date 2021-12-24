/*
* JBoss, a division of Red Hat
* Copyright 2005, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
*/package org.jboss.test.cluster.partition.test;

import java.util.Vector;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.cluster.partition.BadHAPartitionStateException;

public class BadStateTransferTestCase extends JBossClusteredTestCase
{

   public BadStateTransferTestCase(String name)
   {
      super(name);
   }        

   public static Test suite() throws Exception
   {
      Test t1 = JBossClusteredTestCase.getDeploySetup(BadStateTransferTestCase.class, "badstatetransfer.sar");
      return t1;
   } 

   protected void setUp() throws Exception
   {
      super.setUp();
   }

   protected void tearDown() throws Exception
   {
      super.tearDown();
   }
   
   public void testFailedStateProvider() throws Exception
   {
      MBeanServerConnection[] adaptors = getAdaptors();
      
      ObjectName partition = new ObjectName("jboss:service=BadProviderPartition");
      
      Exception e = (Exception) adaptors[1].getAttribute(partition, "StartupException");      

      assertNotNull("Partition caught exception", e);
      
      Throwable parent = e;
      Throwable cause = e.getCause();
      while (cause != null)
      {
         parent = cause;
         cause = parent.getCause();
      }
      
      assertTrue("Correct type of Exception caught", parent instanceof IllegalStateException);
      
      // Confirm the bad partition is removed from the current view
      Vector view = (Vector) adaptors[0].getAttribute(partition, "CurrentView");
      assertEquals("View size after failure is correct", 1, view.size());
   }

   public void testBadStateIntegration() throws Exception
   {
      MBeanServerConnection[] adaptors = getAdaptors();
      
      ObjectName partition = new ObjectName("jboss:service=BadStatePartition");
      
      Exception e = (Exception) adaptors[1].getAttribute(partition, "StartupException");

      assertNotNull("Partition caught exception", e);
      
      Throwable parent = e;
      Throwable cause = e.getCause();
      while (cause != null)
      {
         parent = cause;
         cause = parent.getCause();
      }
      
      assertTrue("Correct type of Exception caught", parent instanceof BadHAPartitionStateException);
      
      // Confirm the bad partition is removed from the current view
      Vector view = (Vector) adaptors[0].getAttribute(partition, "CurrentView");
      assertEquals("View size after failure is correct", 1, view.size());
   }

   /**
    * In this subclass this is a no-op because we are deliberately
    * deploying a sar that will fail in deployment
    */
   public void testServerFound() throws Exception
   {
      // do nothing
   }
   
   
}
