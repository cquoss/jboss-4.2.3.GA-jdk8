/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb3.test.clusteredsession.unit;

import java.rmi.dgc.VMID;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Assert;
import junit.framework.Test;

import org.jboss.ejb3.test.JBossWithKnownIssuesTestCase;
import org.jboss.ejb3.test.stateful.nested.base.std.NestedBeanMonitor;
import org.jboss.ejb3.test.stateful.nested.base.std.NestedStateful;
import org.jboss.ejb3.test.stateful.nested.base.std.ParentStatefulRemote;
import org.jboss.ejb3.test.stateful.unit.NestedBeanTestRunner;
import org.jboss.ejb3.test.stateful.unit.NestedBeanTestRunner.NestedBeanSet;
import org.jboss.logging.Logger;
import org.jboss.test.JBossClusteredTestCase;

/**
 * Overrides the parent test to use clustered versions of
 * the beans.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 65850 $
 */
public class NestedBeanUnitTestCase 
   extends JBossClusteredTestCase
{
   private static final Logger log = Logger.getLogger(NestedBeanUnitTestCase.class);
   
   private NestedBeanTestRunner runner;
   
   
   public NestedBeanUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      final String jarName = "clusteredsession-test.jar";
      Test t1 = getDeploySetup(NestedBeanUnitTestCase.class,
                               jarName);
      return t1;
   }
   
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      runner = new NestedBeanTestRunner(getInitialContext(0), log);
      runner.setUp();
      // Use a sleep time equal to 2 thread runs + a 100 ms fudge
      runner.setSleepTime(10100L);
      // For clustered beans, an invocation is a passivation
      runner.setPassivationPerInvocation(1);
      // For clustered beans, passivation occurs after already called
      // @PrePassivate for replication, so don't get a 2nd event
      runner.setPassivationPerSleep(0);
   }

   private InitialContext getInitialContext(int node) throws Exception {
      // Connect to the serverX JNDI
      String[] urls = getNamingURLs();
      Properties env1 = new Properties();
      env1.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jnp.interfaces.NamingContextFactory");
      env1.setProperty(Context.PROVIDER_URL, urls[node]);
      return new InitialContext(env1);
   }

   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
      
      if (runner != null)
         runner.tearDown();
   }

   public void testBasic()
   throws Exception
   {
      runner.testBasic();
   }
   
   public void testDependentLifecycle()
   throws Exception
   {
      runner.testDependentLifecycle();      
   }

   public void testStatefulPassivation()
   throws Exception
   {
      runner.testStatefulPassivation();
   }


   public void testStatefulBeanCounterFailoverWithRemote()
   throws Exception
   {
      getLog().debug("Test Nested Stateful Bean Counter Failover with Remote");
      getLog().debug("======================================================");
      
      NestedBeanSet beanSet = runner.getNestedBeanSet();
      ParentStatefulRemote stateful = beanSet.parent;
      VMID node1 = stateful.getVMID();
      assertNotNull("State node: ", node1);
      getLog ().debug ("Node 1 ID: " +node1);

      assertEquals("Counter: ", 1, stateful.increment());
      assertEquals("Counter: ", 2, stateful.increment());
      sleep(300);

      // Now we switch to the other node, simulating a failure on node 1
      stateful.setUpFailover("once");
      VMID node2 = stateful.getVMID();
      assertNotNull("State node: ", node2);
      getLog ().debug ("Node 2 ID : " +node2);

      assertFalse("Failover has occured", node1.equals(node2));

      assertEquals("Counter: ", 3, stateful.increment());
      assertEquals("Counter: ", 4, stateful.increment());

      runner.removeBean(stateful);
      getLog().debug("ok");
   }


   public void testStatefulBeanCounterFailover()
   throws Exception
   {
      getLog().debug("Test Nested Stateful Bean Counter Failover");
      getLog().debug("==========================================");
      
      NestedBeanSet beanSet = runner.getNestedBeanSet();
      ParentStatefulRemote stateful = beanSet.parent;
      VMID node1 = stateful.getVMID();
      assertNotNull("State node: ", node1);
      getLog ().debug ("Node 1 ID: " +node1);

      assertEquals("Counter: ", 1, stateful.incrementLocal());
      assertEquals("Counter: ", 2, stateful.incrementLocal());
      sleep(300);

      // Now we switch to the other node, simulating a failure on node 1
      stateful.setUpFailover("once");
      VMID node2 = stateful.getVMID();
      assertNotNull("State node: ", node2);
      getLog ().debug ("Node 2 ID : " +node2);

      assertFalse("Failover has occured", node1.equals(node2));

      assertEquals("Counter: ", 3, stateful.incrementLocal());
      assertEquals("Counter: ", 4, stateful.incrementLocal());

      runner.removeBean(stateful);
      getLog().debug("ok");
   }

   /**
    * FIXME This is a very weak test for EJBTHREE-1053; 
    * replace with something better when EJBTHREE-1053 is fixed 
    * 
    * @throws Exception
    */
   public void testConsistentPassivatedState()
   throws Exception
   {
      getLog().debug("Running testConsistentPassivatedState()");
      getLog().debug("=======================================");
      
      NestedBeanSet beanSet = runner.getNestedBeanSet();
      NestedBeanMonitor monitor = beanSet.monitor;
      ParentStatefulRemote parent = beanSet.parent;
      NestedStateful nested = beanSet.nested;
      
      int parentInv = beanSet.parentInvocations;
      int nestedInv = beanSet.nestedInvocations;

      Assert.assertEquals("Remote counter: ", 1, parent.increment());
      parentInv++;
      nestedInv++;
      Assert.assertEquals("Remote counter: ", 2, parent.increment());
      parentInv++;
      nestedInv++;
      Assert.assertEquals("Local counter: ", 1, parent.incrementLocal());
      parentInv++;
      Assert.assertEquals("Local counter: ", 2, parent.incrementLocal());
      parentInv++;
      
      sleep(runner.getSleepTime());  // should passivate
      
      // Invoke on nested bean using *our* proxy.
      // NOTE: these calls do not trigger replication due to how 
      // they implement Optimized
      Assert.assertEquals("Parent passivate count: ",
                          runner.getExpectedPassivations(1, parentInv), 
                          parent.getPrePassivate());
      parentInv++;
      Assert.assertEquals("Parent activate count: ",
                          runner.getExpectedPassivations(1, parentInv), 
                          parent.getPostActivate());
      parentInv++;
      Assert.assertEquals("Remote nested passivate count: ",
                          runner.getExpectedPassivations(1, nestedInv), 
                          nested.getPrePassivate());
      Assert.assertEquals("Remote nested activate count: ",
                          runner.getExpectedPassivations(1, nestedInv), 
                          nested.getPostActivate());
      
      // Use the monitor to check the deep nested beans.  In a cluster these
      // are marked not to treat replication as passivation, so we ignore 
      // the number of invocations
      
      // NOTE: here the invocation goes through the monitor, which has never
      // used its proxy before.  It may pick a different target server than that
      // used by our 'nested' variable's proxy.  If so, we then test if the 
      // state is as expected on the 2nd server.
      // If by chance it picks the same target as 'nested', this test is meaningless
      // So, any failures will be transient
      // TODO if JBCACHE-1190 is fixed this test may need some re-work
      int dnp = monitor.getDeepNestedPassivations();
      if (monitor.getDeepNestedPassivations() != 1)
      {
         JBossWithKnownIssuesTestCase.showKnownIssue("EJBTHREE-1053", "Deep nested passivate count incorrect: expected 1, was " + dnp);
      }
      
      getLog().debug("ok");
   }

}
