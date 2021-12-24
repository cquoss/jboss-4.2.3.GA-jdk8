/*
* JBoss, a division of Red Hat
* Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
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
*/
package org.jboss.test.web.test;

import java.security.Principal;
import java.util.Set;
import java.util.Vector;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.transaction.TransactionManager;

import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.session.StandardSession;
import org.jboss.cache.AbstractTreeCacheListener;
import org.jboss.cache.Fqn;
import org.jboss.cache.TransactionManagerLookup;
import org.jboss.cache.TreeCache;
import org.jboss.cache.transaction.BatchModeTransactionManager;
import org.jboss.cache.transaction.DummyTransactionManager;
import org.jboss.security.SimplePrincipal;
import org.jboss.test.JBossTestCase;
import org.jboss.util.threadpool.BasicThreadPool;
import org.jboss.web.tomcat.service.sso.ClusteredSingleSignOn;
import org.jboss.web.tomcat.service.sso.TreeCacheSSOClusterManager;
import org.jgroups.View;
import org.jgroups.ViewId;
import org.jgroups.stack.IpAddress;

import EDU.oswego.cs.dl.util.concurrent.ReentrantLock;

/**
 * Test of the TreeCacheSSOClusterManager class.
 * 
 * @author Brian Stansberry
 */
public class TreeCacheSSOClusterManagerUnitTestCase extends JBossTestCase
{
   private static IpAddress LOCAL_ADDRESS;
   private static IpAddress REMOTE_ADDRESS;
   
   public TreeCacheSSOClusterManagerUnitTestCase(String name)
   {
      super(name);
   }
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      LOCAL_ADDRESS  = new IpAddress("127.0.0.1", 11111);
      REMOTE_ADDRESS = new IpAddress("192.168.0.1", 11111);
   }

   public void testDeadMemberCleanupWithPool() throws Exception
   {
      deadMemberCleanupTest(true);
   }
   
   public void testDeadMemberCleanupWithoutPool() throws Exception
   {
      deadMemberCleanupTest(false);
   }
   
   private void deadMemberCleanupTest(boolean usePool) throws Exception
   {
      log.debug("+++ testDeadMemberCleanup()");
      
      MBeanServer mbeanServer = 
         MBeanServerFactory.createMBeanServer("deadMemberTest");
      try 
      {
         // Register a cache
         MockTreeCache cache = new MockTreeCache();
         // JBAS-4097 -- don't use a TransactionManagerLookup that will
         // bind DummyTransactionManager into JNDI, as that will screw
         // up other tests
         cache.setTransactionManagerLookup(new MockTransactionManagerLookup());
         mbeanServer.registerMBean(cache, new ObjectName(TreeCacheSSOClusterManager.DEFAULT_GLOBAL_CACHE_NAME));
         cache.startService();
         
         if (usePool)
         {
            BasicThreadPool pool = new BasicThreadPool();
            mbeanServer.registerMBean(pool, new ObjectName(TreeCacheSSOClusterManager.DEFAULT_THREAD_POOL_NAME));
         }
         
         // Build up an SSO infrastructure based on LOCAL_ADDRESS         
         TreeCacheSSOClusterManager localManager = new TreeCacheSSOClusterManager(mbeanServer);
         localManager.setCacheName(TreeCacheSSOClusterManager.DEFAULT_GLOBAL_CACHE_NAME);
         
         MockSSOValve localValve = new MockSSOValve();
         localValve.setClusterManager(localManager);
         localManager.setSingleSignOnValve(localValve);
         localManager.start();
         
         assertEquals("Thread pool usage as expected", usePool, localManager.isUsingThreadPool());
         
         //  Build up a second SSO infrastructure based on LOCAL_ADDRESS
         // It uses the same mock cache, but we change the cache address
         // so it thinks it's a different address when it starts
         cache.setOurAddress(REMOTE_ADDRESS);
         
         TreeCacheSSOClusterManager remoteManager = new TreeCacheSSOClusterManager(mbeanServer);
         remoteManager.setCacheName(TreeCacheSSOClusterManager.DEFAULT_GLOBAL_CACHE_NAME);
         
         MockSSOValve remoteValve = new MockSSOValve();
         remoteValve.setClusterManager(remoteManager);
         remoteManager.setSingleSignOnValve(localValve);
         remoteManager.start();
         
         
         // Create an SSO that will have sessions from both valves
         localManager.register("1", "FORM", "Brian", "password");
         
         Manager localSessMgr1 = new StandardManager();
         Session sess1 = new MockSession(localSessMgr1, "1");
         localManager.addSession("1", sess1);
         
         Manager remoteSessMgr1 = new StandardManager();
         Session sess2 = new MockSession(remoteSessMgr1, "2");
         remoteManager.addSession("1", sess2);
         
         
         // Create another SSO with sessions only from remote
         remoteManager.register("2", "FORM", "Brian", "password");
         
         Manager remoteSessMgr2 = new StandardManager();
         Session sess3 = new MockSession(remoteSessMgr2, "3");
         remoteManager.addSession("2", sess3);
         
         
         // Create a third SSO that will have sessions from both valves
         localManager.register("3", "FORM", "Brian", "password");
         
         Manager localSessMgr2 = new StandardManager();
         Session sess4 = new MockSession(localSessMgr2, "4");
         localManager.addSession("3", sess4);
         
         Manager remoteSessMgr3 = new StandardManager();
         Session sess5 = new MockSession(remoteSessMgr3, "5");
         remoteManager.addSession("3", sess5);
         
         
         // Create a fourth SSO that will have sessions from both valves
         localManager.register("4", "FORM", "Brian", "password");
         
         Manager localSessMgr3 = new StandardManager();
         Session sess6 = new MockSession(localSessMgr3, "6");
         localManager.addSession("4", sess6);
         
         Manager remoteSessMgr4 = new StandardManager();
         Session sess7 = new MockSession(remoteSessMgr4, "7");
         remoteManager.addSession("4", sess7);
         
         
         // Confirm that data is cached properly
         Set sso1 = (Set) cache.get("/SSO/1/sessions", "key");
         assertNotNull("SSO 1 exists", sso1);
         assertEquals("SSO 1 has correct number of sessions", 2, sso1.size());
         Set sso2 = (Set) cache.get("/SSO/2/sessions", "key");
         assertNotNull("SSO 2 exists", sso2);
         assertEquals("SSO 2 has correct number of sessions", 1, sso2.size());
         Set sso3 = (Set) cache.get("/SSO/3/sessions", "key");
         assertNotNull("SSO 3 exists", sso3);
         assertEquals("SSO 3 has correct number of sessions", 2, sso3.size());
         Set sso4 = (Set) cache.get("/SSO/4/sessions", "key");
         assertNotNull("SSO 4 exists", sso4);
         assertEquals("SSO 4 has correct number of sessions", 2, sso4.size());
         
         // Put in a new view with REMOTE_ADDRESS dead
         ViewId viewId = new ViewId(LOCAL_ADDRESS, 1);
         Vector v = new Vector();
         v.add(LOCAL_ADDRESS);
         localManager.viewChange(new View(viewId, v));
         
         
         // Test that a regular remove call cleans up dead members
         // (BES: the cleanup thread may have dealt with this; I see no
         //  clean way to force this being done by the caller thread)
         localManager.removeSession("3", sess4);
         sso3 = (Set) cache.get("/SSO/3/sessions", "key");
         assertNotNull("SSO 3 exists", sso3);
         assertEquals("SSO 3 has correct number of sessions", 0, sso3.size());
         
         // Test that a regular add call cleans up dead members
         // (BES: the cleanup thread may have dealt with this; I see no
         //  clean way to force this being done by the caller thread)
         Manager localSessMgr4 = new StandardManager();
         Session sess8 = new MockSession(localSessMgr4, "8");
         localManager.addSession("4", sess8);
         sso4 = (Set) cache.get("/SSO/4/sessions", "key");
         assertNotNull("SSO 4 exists", sso4);
         assertEquals("SSO 4 has correct number of sessions", 2, sso4.size());
         
         // Give the cleanup thread time to finish
         Thread.sleep(300);
         
         // Confirm that cached data is properly cleaned up
         sso1 = (Set) cache.get("/SSO/1/sessions", "key");
         assertNotNull("SSO 1 exists", sso1);
         assertEquals("SSO has correct number of sessions", 1, sso1.size());
         sso2 = (Set) cache.get("/SSO/2/sessions", "key");
         assertNotNull("SSO 2 exists", sso2);
         assertEquals("SSO has correct number of sessions", 0, sso2.size());
         sso3 = (Set) cache.get("/SSO/3/sessions", "key");
         assertNotNull("SSO 3 exists", sso3);
         assertEquals("SSO 3 has correct number of sessions", 0, sso3.size());
         sso4 = (Set) cache.get("/SSO/4/sessions", "key");
         assertNotNull("SSO 4 exists", sso4);
         assertEquals("SSO 4 has correct number of sessions", 2, sso4.size());         
      }
      finally
      {
         MBeanServerFactory.releaseMBeanServer(mbeanServer);
      }      
   }
   
   
   static class MockTreeCache extends TreeCache
   {
      private IpAddress ourAddress = LOCAL_ADDRESS;
      
      public MockTreeCache() throws Exception
      {
         super();
         setCacheMode(TreeCache.LOCAL);
      }

      @Override
      public Object getLocalAddress()
      {
         return ourAddress;
      }
      
      void setOurAddress(IpAddress address)
      {
         ourAddress = address;
      }

      @Override
      public Vector getMembers()
      {
         Vector v = new Vector();
         v.add(LOCAL_ADDRESS);
         v.add(REMOTE_ADDRESS);
         return v;
      }      
      
   }
   
   /**
    * Override ClusteredSingleSignOn to suppress the empty SSO callbacks
    */
   static class MockSSOValve extends ClusteredSingleSignOn
   {
      @Override
      protected void notifySSOEmpty(String ssoId)
      {
         // no-op
      }

      @Override
      protected void notifySSONotEmpty(String ssoId)
      {
         // no-op
      }      
   }
   
   static class MockSession extends StandardSession
   {
      private static final long serialVersionUID = 1L;
      
      private String ourId;
      
      MockSession(Manager manager, String id)
      {
         super(manager);
         ourId = id;
      }
      
      @Override
      public String getId()
      {
         return ourId;
      }
   }
   
   static class MockTransactionManagerLookup implements TransactionManagerLookup
   {
      public TransactionManager getTransactionManager() throws Exception
      {
         return new BatchModeTransactionManager();
      }
      
   }

}
