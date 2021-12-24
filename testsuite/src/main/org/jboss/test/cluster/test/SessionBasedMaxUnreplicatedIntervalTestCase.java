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

package org.jboss.test.cluster.test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import org.jboss.cache.Fqn;
import org.jboss.cache.aop.PojoCache;
import org.jboss.logging.Logger;
import org.jboss.metadata.WebMetaData;
import org.jboss.test.JBossTestCase;
import org.jboss.test.cluster.web.util.BasicRequestHandler;
import org.jboss.test.cluster.web.util.CacheConfigTestSetup;
import org.jboss.test.cluster.web.util.MutableObject;
import org.jboss.test.cluster.web.util.SetAttributesRequestHandler;
import org.jboss.test.cluster.web.util.WebSessionTestUtil;
import org.jboss.web.tomcat.service.session.JBossCacheManager;
import org.jboss.web.tomcat.service.session.JBossCacheService;

/**
 * Tests of handling of ClusteredSession.maxUnreplicatedInterval.  This base
 * test is run with SESSION granularity.
 * 
 * @author Brian Stansberry
 */
public class SessionBasedMaxUnreplicatedIntervalTestCase extends JBossTestCase
{
   protected static PojoCache[] pojoCaches = new PojoCache[2];

   protected static long testId = System.currentTimeMillis();
   
   protected static boolean useBuddyRepl = Boolean.valueOf(System.getProperty("jbosstest.cluster.web.cache.br")).booleanValue();
   
   protected Logger log = Logger.getLogger(getClass());   
   
   protected Set<JBossCacheManager> managers = new HashSet<JBossCacheManager>();
   
   protected Map<String, Object> allAttributes;
   protected Map<String, Object> immutables;
   protected Map<String, Object> mutables;
   
   public SessionBasedMaxUnreplicatedIntervalTestCase(String name)
   {
      super(name);
   }
   
   public static Test suite() throws Exception
   {
      return CacheConfigTestSetup.getTestSetup(SessionBasedMaxUnreplicatedIntervalTestCase.class, pojoCaches, false, !useBuddyRepl, false);
   }

   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      allAttributes = new HashMap<String, Object>();
      immutables = new HashMap<String, Object>();
      mutables = new HashMap<String, Object>();
      
      allAttributes.put("IMMUTABLE", "IMMUTABLE");
      immutables.put("IMMUTABLE", "IMMUTABLE");
      
      MutableObject mo = new MutableObject("MUTABLE");
      allAttributes.put("MUTABLE", mo);
      mutables.put("MUTABLE", mo);
      
      allAttributes = Collections.unmodifiableMap(allAttributes);
      immutables = Collections.unmodifiableMap(immutables);
      mutables = Collections.unmodifiableMap(mutables);
   }

   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
      
      for (JBossCacheManager manager : managers)      
         manager.stop();
      
      managers.clear();
   }
   
   protected int getReplicationGranularity()
   {
      return WebMetaData.REPLICATION_GRANULARITY_SESSION;
   }
   
   protected int getReplicationTrigger()
   {
      return WebMetaData.SESSION_INVALIDATE_SET_AND_NON_PRIMITIVE_GET;
   }
   
   public void testBasicMaxIntervalPreventsExpiration() throws Exception
   {
      log.info("++++ Starting testBasicMaxIntervalPreventsExpiration ++++");
      
      maxIntervalPreventsExpirationTest(false);
   }
   
   public void testZeroMaxIntervalPreventsExpiration() throws Exception
   {
      log.info("++++ Starting testZeroMaxIntervalPreventsExpiration ++++");
      
      maxIntervalPreventsExpirationTest(false);
   }
   
   private void maxIntervalPreventsExpirationTest(boolean testZero) throws Exception
   {
      String warname = String.valueOf(++testId);
      
      int maxUnrep = testZero ? 0 : 1;
      
      // A war with a maxInactive of 3 secs and a maxUnreplicated of 0 or 1
      JBossCacheManager[] mgrs = getCacheManagers(warname, 3, maxUnrep);
      JBossCacheManager jbcm0 = mgrs[0];
      JBossCacheManager jbcm1 = mgrs[1];
      
      SetAttributesRequestHandler setHandler = new SetAttributesRequestHandler(allAttributes, false);
      WebSessionTestUtil.invokeRequest(jbcm0, setHandler, null);
      
      validateNewSession(setHandler);
      
      Thread.sleep(1050);
      
      // Now make a request that will not trigger replication unless the interval is exceeded
      BasicRequestHandler getHandler = new BasicRequestHandler(immutables.keySet(), false);
      WebSessionTestUtil.invokeRequest(jbcm0, getHandler, setHandler.getSessionId());
      
      validateExpectedAttributes(immutables, getHandler);
      
      // Sleep long enough that the session will be expired on other server
      // if previous request didn't keep it alive
      Thread.sleep(2000);
      
      // Fail over and confirm all is well
      getHandler = new BasicRequestHandler(allAttributes.keySet(), false);
      WebSessionTestUtil.invokeRequest(jbcm1, getHandler, setHandler.getSessionId());
      
      validateExpectedAttributes(allAttributes, getHandler);
   }
   
   public void testMaxIntervalPreventsReplication() throws Exception
   {
      log.info("++++ Starting testMaxIntervalPreventsReplication ++++");
      
      String warname = String.valueOf(++testId);
      
      // A war with a maxInactive of 3 secs and a maxUnreplicated of 1
      JBossCacheManager[] mgrs = getCacheManagers(warname, 3, 1);
      JBossCacheManager jbcm0 = mgrs[0];
      JBossCacheManager jbcm1 = mgrs[1];
      
      SetAttributesRequestHandler setHandler = new SetAttributesRequestHandler(allAttributes, false);
      WebSessionTestUtil.invokeRequest(jbcm0, setHandler, null);
      
      // Sleep less than the maxUnreplicated time so next request shouldn't trigger timestamp repl
      Thread.sleep(900);
      
      // Now make a request that will not trigger replication unless the interval is exceeded
      BasicRequestHandler getHandler = new BasicRequestHandler(immutables.keySet(), false);
      WebSessionTestUtil.invokeRequest(jbcm0, getHandler, setHandler.getSessionId());
      
      validateExpectedAttributes(immutables, getHandler);
      
      // Sleep long enough that the session will be expired on other server
      // if previous request didn't keep it alive
      Thread.sleep(2150);
      
      // Fail over and confirm the session was expired
      getHandler = new BasicRequestHandler(allAttributes.keySet(), false);
      WebSessionTestUtil.invokeRequest(jbcm1, getHandler, setHandler.getSessionId());
      
      validateNewSession(getHandler);
   }
   
   /**
    * Confirms that the "grace period" that maxUnreplicatedInterval adds to the
    * removal of overaged unloaded sessions in remote caches delays their
    * removal.
    * 
    * @throws Exception
    */
   public void testRemoteExpirationGracePeriod() throws Exception
   {
      log.info("++++ Starting testRemoteExpirationGracePeriod ++++");
      
      String warname = String.valueOf(++testId);
      
      JBossCacheManager[] mgrs = getCacheManagers(warname, 3, 2);
      JBossCacheManager jbcm0 = mgrs[0];
      JBossCacheManager jbcm1 = mgrs[1];
      
      SetAttributesRequestHandler setHandler1 = new SetAttributesRequestHandler(allAttributes, false);
      WebSessionTestUtil.invokeRequest(jbcm0, setHandler1, null);
      
      Fqn session1Fqn = Fqn.fromString("/JSESSION/localhost/" + warname + "/" + setHandler1.getSessionId());
      
      SetAttributesRequestHandler setHandler2 = new SetAttributesRequestHandler(allAttributes, false);
      WebSessionTestUtil.invokeRequest(jbcm0, setHandler2, null);
      
      Fqn session2Fqn = Fqn.fromString("/JSESSION/localhost/" + warname + "/" + setHandler2.getSessionId());
      
      // Overage the sessions
      Thread.sleep(3010);
      // Try to force out the overaged sessions
      jbcm1.backgroundProcess();
      // Confirm they are still there
      assertNotNull(pojoCaches[1].get(session1Fqn, JBossCacheService.VERSION_KEY));
      assertNotNull(pojoCaches[1].get(session2Fqn, JBossCacheService.VERSION_KEY));
      
      // Access one to prove it gets expired once the manager can see its real timestamp
      BasicRequestHandler getHandler = new BasicRequestHandler(allAttributes.keySet(), false);
      WebSessionTestUtil.invokeRequest(jbcm1, getHandler, setHandler1.getSessionId());      
      validateNewSession(getHandler);
      
      // Sleep past the grace period
      Thread.sleep(2010);
      // The get restored a new fresh session with the first id, but the 2nd 
      // one is still there and overaged. Try to force it out
      jbcm1.backgroundProcess();
      assertNull(pojoCaches[1].get(session2Fqn, JBossCacheService.VERSION_KEY));
   }
   
   protected JBossCacheManager[] getCacheManagers(String warname, int maxInactive, int maxUnreplicated)
      throws Exception
   {
      JBossCacheManager jbcm0 = WebSessionTestUtil.createManager(warname, maxInactive, pojoCaches[0], null);
      WebMetaData metadata = WebSessionTestUtil.getWebMetaData(getReplicationGranularity(), getReplicationTrigger(), true, maxUnreplicated);
      jbcm0.init(warname, metadata, false, true);
      this.managers.add(jbcm0);
      jbcm0.start();
      
      JBossCacheManager jbcm1 = WebSessionTestUtil.createManager(warname, maxInactive, pojoCaches[1], null);
      metadata = WebSessionTestUtil.getWebMetaData(getReplicationGranularity(), getReplicationTrigger(), true, maxUnreplicated);
      jbcm1.init(warname, metadata, false, true);
      this.managers.add(jbcm1);
      jbcm1.start();
      
      return new JBossCacheManager[]{jbcm0, jbcm1};
   }
   
   protected void validateExpectedAttributes(Map<String, Object> expected, BasicRequestHandler handler)
   {
      assertFalse(handler.isNewSession());
      
      if (handler.isCheckAttributeNames())
      {
         assertEquals(expected.size(), handler.getAttributeNames().size());
      }
      Map<String, Object> checked = handler.getCheckedAttributes();
      assertEquals(expected.size(), checked.size());
      for (Map.Entry<String, Object> entry : checked.entrySet())
         assertEquals(entry.getKey(), expected.get(entry.getKey()), entry.getValue());
      
   }
   
   protected void validateNewSession(BasicRequestHandler handler)
   {
      assertTrue(handler.isNewSession());
      assertEquals(handler.getCreationTime(), handler.getLastAccessedTime());
      if (handler.isCheckAttributeNames())
      {
         assertEquals(0, handler.getAttributeNames().size());
      }
      Map<String, Object> checked = handler.getCheckedAttributes();
      for (Map.Entry<String, Object> entry : checked.entrySet())
         assertNull(entry.getKey(), entry.getValue());
   }
   

}
