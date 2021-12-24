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
package org.jboss.test.ha.jmx.test;

import java.util.EmptyStackException;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;

import junit.framework.TestCase;

import org.jboss.test.ha.jmx.HAServiceMBeanSupportTester;

/**
 * 
 * @author  Ivelin Ivanov <ivelin@jboss.org>
 *
 */
public class HAServiceMBeanSupportUnitTestCase extends TestCase
{

  private HAServiceMBeanSupportTester haServiceMBeanSupportTester_ = null;

  public HAServiceMBeanSupportUnitTestCase(String name)
  {
    super(name);
  }
   
  public void setUp()
  {
    haServiceMBeanSupportTester_ = new HAServiceMBeanSupportTester();
  }

  
  public void tearDown() 
  {
    haServiceMBeanSupportTester_ = null;
  }


  /**
   * 
   * messages should be sent out to both remote and local listeners.
   *
   */
  public void testSendNotificationBroadcastsToClusterAndLocally()
  {
    Notification notification = new Notification("test.notification", "some:name=tester", 1);
    haServiceMBeanSupportTester_.sendNotification( notification );

    assertEquals("sendNotificationToLocalListeners() was not handed the original notification", 
      haServiceMBeanSupportTester_.__invokationStack__.pop(), notification );

    assertEquals("method not invoked as expected",
      haServiceMBeanSupportTester_.__invokationStack__.pop(), "sendNotificationToLocalListeners");      

    assertEquals("sendNotificationRemote() was not handed the original notification", 
      haServiceMBeanSupportTester_.__invokationStack__.pop(), notification );
    
    assertEquals("method not invoked as expected",
      haServiceMBeanSupportTester_.__invokationStack__.pop(), "sendNotificationRemote");      
  }

  /**
   * 
   * Even if the message cannot be sent out to the cluster,
   * it should still be delivered to local listeners.
   *
   */
  public void testSendNotificationAfterClusterFailureContinueWithLocal()
  {
    haServiceMBeanSupportTester_.__shouldSendNotificationRemoteFail__ = true;

    Notification notification = new Notification("test.notification", "some:name=tester", 1);
    haServiceMBeanSupportTester_.sendNotification( notification );
    
    assertEquals("sendNotificationToLocalListeners() was not handed the original notification", 
    haServiceMBeanSupportTester_.__invokationStack__.pop(), notification );

    assertEquals("method not invoked as expected",
      haServiceMBeanSupportTester_.__invokationStack__.pop(), "sendNotificationToLocalListeners");      
  }
  
  public void testSendLifecycleNotifications()
  {
     Notification notification = new AttributeChangeNotification(
           haServiceMBeanSupportTester_,
           1, System.currentTimeMillis(), "test",
           "State", "java.lang.Integer",
           new Integer(0), new Integer(1)
           );
     
     haServiceMBeanSupportTester_.setSendRemoteLifecycleNotifications(false);
     
     haServiceMBeanSupportTester_.sendNotification( notification );
     
     assertEquals("sendNotificationToLocalListeners() was handed the original notification", 
                 haServiceMBeanSupportTester_.__invokationStack__.pop(), notification );

     assertEquals("method invoked as expected",
       haServiceMBeanSupportTester_.__invokationStack__.pop(), "sendNotificationToLocalListeners");      

     try
     {
        haServiceMBeanSupportTester_.__invokationStack__.pop();
        fail("sendNotificationRemote() was not handed the original notification");
     }
     catch (EmptyStackException good) {}
     
     haServiceMBeanSupportTester_.setSendRemoteLifecycleNotifications(true);
     haServiceMBeanSupportTester_.setSendLocalLifecycleNotifications(false);
     
     haServiceMBeanSupportTester_.sendNotification( notification );     

     assertEquals("sendNotificationRemote() was handed the original notification", 
       haServiceMBeanSupportTester_.__invokationStack__.pop(), notification );
     
     assertEquals("method invoked as expected",
       haServiceMBeanSupportTester_.__invokationStack__.pop(), "sendNotificationRemote");
     
     try
     {
        haServiceMBeanSupportTester_.__invokationStack__.pop();
        fail("sendNotificationToLocalListeners() was not handed the original notification");
     }
     catch (EmptyStackException good) {}
  }

}
