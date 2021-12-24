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
package org.jboss.test.cluster.test;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Vector;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.Notification;

import junit.framework.Test;

import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.cluster.ds.IDistributedState;
import org.jboss.jmx.adaptor.rmi.RMIAdaptorExt;
import org.jboss.jmx.adaptor.rmi.RMINotificationListener;

/** Tests of http session replication
 *
 * @author  Scott.Stark@jboss.org
 * @version $Revision: 65056 $
 */
public class DistributedStateTestCase extends JBossClusteredTestCase
{
   static class TestListener extends UnicastRemoteObject
      implements RMINotificationListener
   {
      TestListener() throws RemoteException
      {
      }
      public void handleNotification(Notification notification, Object handback)
         throws RemoteException
      {
         System.out.println(notification);
      }
   }

   public static Test suite() throws Exception
   {
      Test t1 = getDeploySetup(DistributedStateTestCase.class, "ds-tests.sar");
      return t1;
   }

   public DistributedStateTestCase(String name)
   {
      super(name);
   }

   public void testStateReplication()
      throws Exception
   {
      log.debug("+++ testStateReplication");
      
      MBeanServerConnection[] adaptors = getAdaptors();
      RMIAdaptorExt server0 = (RMIAdaptorExt) adaptors[0];
      log.info("server0: "+server0);
      ObjectName clusterService = new ObjectName("jboss:service=DefaultPartition");
      Vector view0 = (Vector) server0.getAttribute(clusterService, "CurrentView");
      log.info("server0: CurrentView, "+view0);
      ObjectName dsService = new ObjectName("jboss.test:service=DistributedStateTestCase");
      IDistributedState ds0 = (IDistributedState)
         MBeanServerInvocationHandler.newProxyInstance(server0, dsService,
         IDistributedState.class, true);
      TestListener listener = new TestListener();
      server0.addNotificationListener(dsService, listener, null, null);
      ds0.put("key0", "value0");
      String value = (String) ds0.get("key0");
      log.info("server0: get(key0): "+value);
      assertTrue("server0: value == value0", value.equals("value0"));

      RMIAdaptorExt server1 = (RMIAdaptorExt) adaptors[1];
      log.info("server1: "+server1);
      Vector view1 = (Vector) server1.getAttribute(clusterService, "CurrentView");
      log.info("server1: CurrentView, "+view1);
      IDistributedState ds1 = (IDistributedState)
         MBeanServerInvocationHandler.newProxyInstance(server1, dsService,
         IDistributedState.class, true);
      server1.addNotificationListener(dsService, listener, null, null);
      value = (String) ds1.get("key0");
      log.info("server1: get(key0): "+value);
      assertTrue("server1: value == value0", value.equals("value0"));
      ds1.put("key0", "value1");
      value = (String) ds1.get("key0");
      assertTrue("server1: value == value1("+value+")", value.equals("value1"));
      value = (String) ds0.get("key0");
      assertTrue("server0: value == value1("+value+")", value.equals("value1"));
      
      ds1.put("key1", "value11");
      Collection categories = ds0.listAllCategories();
      assertEquals("server0 categories size", 1, categories.size());
      categories = ds1.listAllCategories();
      assertEquals("server1 categories size", 1, categories.size());
      
      String category = (String)categories.iterator().next();
      Collection keys = ds0.listAllKeys(category);
      assertEquals("server0 keys size", 2, keys.size());
      keys = ds1.listAllKeys(category);
      assertEquals("server1 keys size", 2, keys.size());
      Collection vals = ds0.listAllValues(category);
      assertEquals("server0 values size", 2, vals.size());
      vals = ds1.listAllValues(category);
      assertEquals("server1 values size", 2, vals.size());

      ds0.remove("key0");
      value = (String) ds1.get("key0");
      assertTrue("server1: value == null("+value+")", value == null);
      value = (String) ds0.get("key0");
      assertTrue("server0: value == null("+value+")", value == null);
      
   }

}
