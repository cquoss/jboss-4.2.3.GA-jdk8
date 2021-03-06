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
package org.jboss.ejb3.test.clusteredentity.unit;

import java.util.Properties;
import javax.naming.InitialContext;
import org.jboss.ejb3.test.clusteredentity.Customer;
import org.jboss.ejb3.test.clusteredentity.EntityTest;
import org.jboss.test.JBossClusteredTestCase;
import junit.framework.Test;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id: EntityUnitTestCase.java 60697 2007-02-20 05:08:31Z bstansberry@jboss.com $
 */

public class EntityUnitTestCase
extends JBossClusteredTestCase
{
   org.apache.log4j.Logger log = getLog();

   private static final long SLEEP_TIME = 300l;
   
   static boolean deployed = false;
   static int test = 0;

   public EntityUnitTestCase(String name)
   {

      super(name);

   }
   
   public void testAll() throws Exception
   {
      System.out.println("*** testServerFound()");
      String node0 = System.getProperty("jbosstest.cluster.node0");
      String node1 = System.getProperty("jbosstest.cluster.node1");
        
      Properties prop0 = new Properties();
      prop0.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
      prop0.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
      prop0.put("java.naming.provider.url", "jnp://" + node0 + ":1099");
      
      System.out.println("===== Node0 properties: ");
      System.out.println(prop0);
      
      Properties prop1 = new Properties();
      prop1.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
      prop1.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
      prop1.put("java.naming.provider.url", "jnp://" + node1 + ":1099");

      System.out.println("===== Node1 properties: ");
      System.out.println(prop1);

      System.out.println("Lookup node 0");
      InitialContext ctx0 = new InitialContext(prop0);
      
      System.out.println("Create node 0");
      EntityTest tester0 = (EntityTest)ctx0.lookup("EntityTestBean/remote");
      
      try
      {
         tester0.getCache(isOptimistic());
         
         Customer customer = tester0.createCustomer();
         
         //Call finder twice since Hibernate seems to not actually save collections 
         //into cache on persist(), so make sure it is put into cache on find.       
         System.out.println("Find node 0");
         customer = tester0.findByCustomerId(customer.getId());
         System.out.println("Find(2) node 0");
         customer = tester0.findByCustomerId(customer.getId());
         
         //Check everything was in cache
         System.out.println("Check cache 0");
         try
         {
            tester0.loadedFromCache();
         }
         catch (Exception e)
         {
            log.info("Call to tester0 failed", e);
            fail(e.getMessage());
         }
   
         // The above placement of the collection in the cache is replicated async
         // so pause a bit before checking node 1
         sleep(SLEEP_TIME);
         
         //Now connect to cache on node2 and make sure it is all there
         System.out.println("Lookup node 1");
         InitialContext ctx1 = new InitialContext(prop1);
         
         EntityTest tester1 = (EntityTest)ctx1.lookup("EntityTestBean/remote");
         tester1.getCache(isOptimistic());
         
         System.out.println("Find node 1");
         customer = tester1.findByCustomerId(customer.getId());
   
         //Check everything was in cache
         System.out.println("Check cache 1");
         try
         {
            tester1.loadedFromCache();
         }
         catch (Exception e)
         {
            log.info("Call to tester1 failed", e);
            fail(e.getMessage());
         }
      }
      finally
      {
         // cleanup the db so we can run this test multiple times w/o restarting the cluster
         tester0.cleanup();
      }
   }

   protected boolean isOptimistic()
   {
      return false;
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(EntityUnitTestCase.class, "clusteredentity-test.jar");
   }
}
