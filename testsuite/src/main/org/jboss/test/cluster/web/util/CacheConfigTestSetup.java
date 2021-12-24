/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.test.cluster.web.util;

import java.net.InetAddress;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.cache.aop.PojoCache;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestSetup;

/**
 * @author Brian Stansberry
 *
 */
public class CacheConfigTestSetup extends JBossTestSetup
{
   private static final Logger log = Logger.getLogger(CacheConfigTestSetup.class);
   
   public static Test getTestSetup(Class clazz, final PojoCache[] caches, final boolean local, final boolean totalReplication, final boolean marshalling)
      throws Exception
   {
      final TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(clazz));
      return new CacheConfigTestSetup(suite, caches, local, totalReplication, marshalling);
   }
   
   private PojoCache[] pojoCaches;
   private boolean local;
   private boolean totalReplication;
   private boolean marshalling;
   
   /**
    * Create a new CacheConfigTestSetup.
    * 
    * @param test
    * @throws Exception
    */
   public CacheConfigTestSetup(Test test, PojoCache[] caches, boolean local, boolean totalReplication, final boolean marshalling) throws Exception
   {
      super(test);
      this.pojoCaches = caches;
      this.local = local;
      this.totalReplication = totalReplication;
      this.marshalling = marshalling;
   }
   
   protected void setUp() throws Exception
   {
      if (pojoCaches == null) return;
      String bind_addr = System.getProperty("jgroups.bind_addr");
      String mcast_addr = System.getProperty("jgroups.udp.mcast_addr");
      String mcast_port =  System.getProperty("jgroups.udp.mcast_port");
      
      try
      {
         System.setProperty("jgroups.bind_addr", System.getProperty("jbosstest.cluster.node1", InetAddress.getLocalHost().getHostAddress()));
         String udpGroup = System.getProperty("jbosstest.udpGroup", "233.54.54.54");
         if (udpGroup.trim().length() ==0)
            udpGroup = "233.54.54.54";
         System.setProperty("jgroups.udp.mcast_addr", udpGroup);
         System.setProperty("jgroups.udp.mcast_port", String.valueOf(54545));
         
         for (int i = 0; i < pojoCaches.length; i++)
         {
            pojoCaches[i] = WebSessionTestUtil.createCache(local, totalReplication, marshalling, null);
         }
      }
      finally
      {
         if (bind_addr == null)
            System.clearProperty("jgroups.bind_addr");
         else
            System.setProperty("jgroups.bind_addr", bind_addr);
         if (mcast_addr == null)
            System.clearProperty("jgroups.udp.mcast_addr");
         else
            System.setProperty("jgroups.udp.mcast_addr", mcast_addr);
         if (mcast_port == null)
            System.clearProperty("jgroups.udp.mcast_port");
         else
            System.setProperty("jgroups.udp.mcast_port", mcast_port);
      }
      
      // wait a few seconds so that the cluster stabilize
      synchronized (this)
      {
         wait(2000);
      }
   }

   protected void tearDown() throws Exception
   {
      if (pojoCaches != null)
      {
         for (int i = 0; i < pojoCaches.length; i++)
         {
            try
            {   
               PojoCache pc = pojoCaches[i];
               pojoCaches[i] = null;
               pc.stop();
               pc.destroy();
            }
            catch (Exception ex)
            {
               log.error("Failed stopping cache " + i);
            }
         }         
      }
   }

}
