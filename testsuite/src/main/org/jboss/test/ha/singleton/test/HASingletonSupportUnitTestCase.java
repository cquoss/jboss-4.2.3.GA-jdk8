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
package org.jboss.test.ha.singleton.test;
 
import java.util.ArrayList;

import junit.framework.TestCase;

import org.jboss.ha.framework.interfaces.ClusterMergeStatus;
import org.jboss.test.ha.singleton.HASingletonSupportTester;

/**
 * 
 * @author  Ivelin Ivanov <ivelin@jboss.org>
 *
 */
public class HASingletonSupportUnitTestCase extends TestCase
{

  private HASingletonSupportTester singletonSupportTester = null;

  public HASingletonSupportUnitTestCase(String testCaseName)
  {
    super(testCaseName);
  }


  public void setUp()
  {
    singletonSupportTester = new HASingletonSupportTester();
    // We want to port this test from 4.2 to CP releases where 
    // the default for restartOnMerge will be false; so set
    // the property to the 4.2 default, allowing the remaining
    // tests to go unchanged yet still run as they do in 4.2
    singletonSupportTester.setRestartOnMerge(true);
  }
  
  public void tearDown() 
  {
    singletonSupportTester = null;
    ClusterMergeStatus.endMergeProcess();
  }
  
  public void testStartService() throws Exception
  {
    singletonSupportTester.start();

    // test that the correct start sequence was followed correctly  
    assertEquals("method not invoked as expected",
      singletonSupportTester.__invokationStack__.pop(), "registerDRMListener");  
    assertEquals("method not invoked as expected",
      singletonSupportTester.__invokationStack__.pop(), "registerRPCHandler");  
    assertEquals("method not invoked as expected",
      singletonSupportTester.__invokationStack__.pop(), "setupPartition");  
      
  }

  public void testStopService() throws Exception
  {
    singletonSupportTester.start();
    singletonSupportTester.stop();

    assertEquals("method not invoked as expected",
      singletonSupportTester.__invokationStack__.pop(), "unregisterRPCHandler");  
    assertEquals("method not invoked as expected",
      singletonSupportTester.__invokationStack__.pop(), "unregisterDRMListener");  
    
  }
  
  public void testBecomeMasterNode() throws Exception
  {
    singletonSupportTester.start();
    
    // register DRM Listener is expected to call back
    singletonSupportTester.__isDRMMasterReplica__ = true;
    singletonSupportTester.partitionTopologyChanged( new ArrayList(2), 1);

    // test whether it was elected    
    assertTrue("expected to become master", singletonSupportTester.isMasterNode());
    
    // test whether the election sequence was followed correctly
    assertEquals("method not invoked as expected",
      singletonSupportTester.__invokationStack__.pop(), "startSingleton");  
    //assertEquals("method not invoked as expected",
    //  singletonSupportTester.__invokationStack__.pop(), "callMethodOnCluster:_stopOldMaster");  
    assertEquals("method not invoked as expected",
      singletonSupportTester.__invokationStack__.pop(), "makeThisNodeMaster");      
  }
  
  public void testBecomeSlaveNodeWithAnotherMaster() throws Exception
  {
    singletonSupportTester.start();
    
    boolean savedIsMasterNode = singletonSupportTester.isMasterNode();
    
    // register DRM Listener is expected to call back
    singletonSupportTester.__isDRMMasterReplica__ = false;
    singletonSupportTester.partitionTopologyChanged(new ArrayList(2), 1);
    
    // this call back should not change the master/slave status
    assertEquals("expected to be still in old master/slave state", singletonSupportTester.isMasterNode(), savedIsMasterNode );
    
    // the new master is expected to call back
    singletonSupportTester._stopOldMaster();
    
    if (savedIsMasterNode)
    {
      assertEquals("this node was the old master, but method not invoked as expected",
        singletonSupportTester.__invokationStack__.pop(), "stopSingleton");  
    }
      
    // now it should be slave
    assertTrue("expected to be slave", !singletonSupportTester.isMasterNode());
            
  }

  public void testStopOnlyNode() throws Exception
  {
    singletonSupportTester.start();
    
    // register DRM Listener is expected to call back
    singletonSupportTester.__isDRMMasterReplica__ = true;
    singletonSupportTester.partitionTopologyChanged( new ArrayList(2), 1);

    // test whether it was elected for master    
    assertTrue("expected to become master", singletonSupportTester.isMasterNode());
    
    singletonSupportTester.stop();
    
    // register DRM Listener is expected to call back
    singletonSupportTester.__isDRMMasterReplica__ = false;
    // since the only node (this one) in the partition is now removed, the replicants list should be empty 
    singletonSupportTester.partitionTopologyChanged(new ArrayList(0), 1);
    
    assertTrue("expected to have made a call to _stopOldMaster(), thus become slave", !singletonSupportTester.isMasterNode() );
    
    assertEquals("method not invoked as expected",
      singletonSupportTester.__invokationStack__.pop(), "stopSingleton");  
      
  }
  
  public void testStartServiceWithRestartOff() throws Exception
  {
     singletonSupportTester.setRestartOnMerge(false);
     testStartService();
  }

  public void testStopServiceWithRestartOff() throws Exception
  {
     singletonSupportTester.setRestartOnMerge(false);
     testStopService();
  }
  
  public void testBecomeMasterNodeWithRestartOff() throws Exception
  {
    singletonSupportTester.setRestartOnMerge(false);
    testBecomeMasterNode();     
  }
  
  public void testBecomeSlaveNodeWithAnotherMasterWithRestartOff() throws Exception
  {
     singletonSupportTester.setRestartOnMerge(false);
     testBecomeSlaveNodeWithAnotherMaster();
  }
  
  public void testStartServiceWithBogusRestart() throws Exception
  {
     try
     {
        ClusterMergeStatus.startMergeProcess();
        testStartService();
     }
     finally
     {
        ClusterMergeStatus.endMergeProcess();
     }
  }
  
  public void testStopServiceWithBogusRestart() throws Exception
  {
     try
     {
        ClusterMergeStatus.startMergeProcess();
        testStopService();
     }
     finally
     {
        ClusterMergeStatus.endMergeProcess();
     }
  }
  
  public void testBecomeMasterNodeDuringMerge() throws Exception
  {  
     try
     {
        ClusterMergeStatus.startMergeProcess();
        testBecomeMasterNode();
     }
     finally
     {
        ClusterMergeStatus.endMergeProcess();
     }     
  }
  
  public void testMasterRestartDuringMerge() throws Exception
  {
     // Just run the BecomeMaster test to get ourself set up as master
     testBecomeMasterNode();
     
     // Drain off any un-popped events
     singletonSupportTester.__invokationStack__.clear();
     
     try
     {
        ClusterMergeStatus.startMergeProcess();
        singletonSupportTester.partitionTopologyChanged( new ArrayList(3), 2);
     }
     finally
     {
        ClusterMergeStatus.endMergeProcess();
     }

     // test whether it's still master    
     assertTrue("expected to remain master", singletonSupportTester.isMasterNode());
     
     // test whether the election sequence was followed correctly  
     assertEquals("method not invoked as expected",
           "startSingleton", singletonSupportTester.__invokationStack__.pop());  
     assertEquals("method not invoked as expected",
           "stopSingleton", singletonSupportTester.__invokationStack__.pop());
     assertEquals("method not invoked as expected",
           "restartMaster", singletonSupportTester.__invokationStack__.pop());
  }
  
  public void testBecomeSlaveNodeWithAnotherMasterDuringMerge() throws Exception
  {
     // Just run the BecomeMaster test to get ourself set up as master
     testBecomeMasterNode();
     
     // Drain off any un-popped events
     singletonSupportTester.__invokationStack__.clear();
     
     singletonSupportTester.__isDRMMasterReplica__ = false;
     try
     {
        ClusterMergeStatus.startMergeProcess();
        singletonSupportTester.partitionTopologyChanged( new ArrayList(3), 2);
     }
     finally
     {
        ClusterMergeStatus.endMergeProcess();
     }
     
     // now it should be slave
     assertFalse("expected to be slave", singletonSupportTester.isMasterNode());
     
     assertEquals("this node was the old master, but method not invoked as expected",
         singletonSupportTester.__invokationStack__.pop(), "stopSingleton");     
  }

  
  public void testMasterRestartDuringMergeWithRestartOff() throws Exception
  {
     singletonSupportTester.setRestartOnMerge(false);
     
     // Just run the BecomeMaster test to get ourself set up as master
     testBecomeMasterNode();
     
     // Drain off any un-popped events
     singletonSupportTester.__invokationStack__.clear();
     
     try
     {
        ClusterMergeStatus.startMergeProcess();
        singletonSupportTester.partitionTopologyChanged( new ArrayList(3), 2);
     }
     finally
     {
        ClusterMergeStatus.endMergeProcess();
     }

     // test whether it's still master    
     assertTrue("expected to remain master", singletonSupportTester.isMasterNode());
     
     // test whether the election sequence was followed correctly   
     assertEquals("method not invoked as expected",
           "isDRMMasterReplica", singletonSupportTester.__invokationStack__.pop());  
     assertEquals("method not invoked as expected",
                  0, singletonSupportTester.__invokationStack__.size()); 
  }
  
}
