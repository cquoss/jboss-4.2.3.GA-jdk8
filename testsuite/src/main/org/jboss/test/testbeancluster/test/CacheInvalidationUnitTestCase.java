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
package org.jboss.test.testbeancluster.test;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.testbean.interfaces.AComplexPK;
import org.jboss.test.testbeancluster.interfaces.SessionToEntityHome;
import org.jboss.test.testbeancluster.interfaces.SessionToEntity;
import org.jboss.test.testbeancluster.interfaces.NodeAnswer;

/** Tests of the clustering cache invalidation framework
 *
 * @author  Scott.Stark@jboss.org
 * @version $Revision: 57211 $
 */
public class CacheInvalidationUnitTestCase extends JBossClusteredTestCase
{
   public CacheInvalidationUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      Test t1 = getDeploySetup(CacheInvalidationUnitTestCase.class,
         "test-cif.ear");

      suite.addTest(t1);

      // Create an initializer for the test suite
      DBSetup wrapper = new DBSetup(suite);
      return wrapper;
   }

   public void testCacheInvalidation()
      throws Exception
   {
      log.info("+++ testCacheInvalidation");

      // Connect to the server0 JNDI
      String[] urls = getNamingURLs();
      Properties env1 = new Properties();
      env1.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jnp.interfaces.NamingContextFactory");
      env1.setProperty(Context.PROVIDER_URL, urls[0]);
      InitialContext ctx1 = new InitialContext(env1);

      SessionToEntityHome home1 =
         (SessionToEntityHome) ctx1.lookup("cif.StatefulSession");
      AComplexPK key = new AComplexPK(true, 0, 0, 0, "testCacheInvalidation");
      SessionToEntity bean1 = home1.create(key);
      String msg = bean1.createEntity();
      log.info("create#1, "+msg);
      // Call accessEntity twice to validate data is consistent on both nodes
      NodeAnswer answer1 = bean1.accessEntity();
      log.info("Answer1: "+answer1);
      NodeAnswer answer2 = bean1.accessEntity();
      log.info("Answer2: "+answer2);
      assertTrue("accessCount == 2", bean1.getAccessCount() == 2);
      assertTrue("answer1.nodeId != answer2.nodeId",
         answer1.nodeId.equals(answer2.nodeId) == false);

      // Call validateAccessCount twice to validate data is consistent on both nodes
      answer1 = bean1.validateAccessCount(2);
      log.info(answer1);
      answer2 = bean1.validateAccessCount(2);
      log.info(answer2);
      assertTrue("answer1.nodeId != answer2.nodeId",
         answer1.nodeId.equals(answer2.nodeId) == false);
      bean1.remove();
   }

}
