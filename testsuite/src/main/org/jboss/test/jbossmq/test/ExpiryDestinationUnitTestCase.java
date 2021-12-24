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
package org.jboss.test.jbossmq.test;

import java.util.Enumeration;
import java.util.List;

import javax.jms.DeliveryMode;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.management.Attribute;
import javax.management.ObjectName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestSetup;
import org.jboss.util.collection.CollectionsUtil;

/**
 * ExpiryDestinationTestCase tests putting messages on a separate queue.
 *
 * @author Elias Ross <genman@noderunner.net>
 * @version $Revision: 62607 $
 */
public class ExpiryDestinationUnitTestCase
   extends JBossMQUnitTest
{

   static String DLQ_QUEUE = "queue/DLQ";
   int times;
   ObjectName dlq;
   ObjectName tq;
   
   public ExpiryDestinationUnitTestCase(String name) throws Exception
   {
      super(name);
      getLog().debug(getName());
   }

   private List list(QueueSession session, Queue queue) throws Exception
   {
      QueueBrowser browser = session.createBrowser( queue );
      Enumeration e = browser.getEnumeration();
      return CollectionsUtil.list(e);
   }

   private int size(QueueSession session, Queue queue) throws Exception
   {
      List l = list(session, queue);
      return l.size();
   }

   private void assertSize(QueueSession session, Queue queue, int size) throws Exception
   {
      assertEquals(size, size(session, queue));
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      times = getIterationCount();
      dlq = new ObjectName("jboss.mq.destination:service=Queue,name=DLQ");
      tq = new ObjectName("jboss.mq.destination:service=Queue,name=testQueue");
      getServer().invoke(dlq, "removeAllMessages", null, null);
      getServer().invoke(tq, "removeAllMessages", null, null);
      getServer().setAttribute(tq, new Attribute("ExpiryDestination", dlq));
      getServer().invoke(tq, "stop", null, null);
      getServer().invoke(tq, "start", null, null);

      connect();
      queueConnection.start();
      drainQueue();
   }

   protected void tearDown() throws Exception
   {
      super.tearDown();
      getServer().invoke(dlq, "removeAllMessages", null, null);
      getServer().invoke(tq, "removeAllMessages", null, null);
      getServer().setAttribute(tq, new Attribute("ExpiryDestination", null));
   }
   
   /**
    * Test that expired messages are moved to a separate queue.
    */
   public void testExpiredMessagesMove() throws Exception
   {
      QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue)context.lookup(TEST_QUEUE);
      QueueSender sender = session.createSender(queue);

      Queue queue2 = (Queue)context.lookup(DLQ_QUEUE);
      session.createBrowser( queue );

      long now = System.currentTimeMillis();

      TextMessage message = session.createTextMessage();
      message.setStringProperty("foo", "bar");
      String text = "expire on server";
      message.setText(text);
      sender.send(message, DeliveryMode.PERSISTENT, 4, 1000);

      Thread.sleep(2000);

      assertSize(session, queue,  0);
      assertSize(session, queue2, 1);

      QueueReceiver receiver = session.createReceiver(queue2);
      message = (TextMessage) receiver.receiveNoWait();
      assertEquals("QUEUE.testQueue", message.getStringProperty("JBOSS_ORIG_DESTINATION"));
      assertTrue(message.getLongProperty("JBOSS_ORIG_EXPIRATION") > now);
      assertEquals(0L, message.getJMSExpiration());
      assertEquals(text, message.getText());
      assertEquals("bar", message.getStringProperty("foo"));
      receiver.close();
      session.close();

      disconnect();
      getLog().debug("passed");
   }

   /**
    * Test that expired messages are moved to a separate queue.
    */
   public void testSomeLoad() throws Exception
   {
      QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue)context.lookup(TEST_QUEUE);
      Queue queue2 = (Queue)context.lookup(DLQ_QUEUE);
      QueueSender sender = session.createSender(queue);
      TextMessage message = session.createTextMessage();

      for (int i = 0; i < times; i++)
         sender.send(message, DeliveryMode.PERSISTENT, 4, 1000);

      for (int tries = 0; tries < 60; tries++) {
         Thread.sleep(1000);
         if (size(session, queue) == 0 &&
             size(session, queue2) == times)
            break;
      }
      assertSize(session, queue, 0);
      assertSize(session, queue2, times);

      getLog().debug("test case where expired messages fail to move");
      for (int i = 0; i < times; i++) {
        if (i % 10 == 0)
          getServer().invoke(dlq, "stop", null, null);
        if (i % 10 == 5)
          getServer().invoke(dlq, "start", null, null);
        sender.send(message, DeliveryMode.PERSISTENT, 4, 1);
      }

      Thread.sleep(1000);

      getServer().invoke(dlq, "start", null, null);
      getServer().invoke(tq, "stop", null, null);
      getServer().invoke(tq, "start", null, null);

      for (int tries = 0; tries < 60; tries++) {
         Thread.sleep(1000);
         if (size(session, queue) == 0 &&
             size(session, queue2) == times * 2)
            break;
      }
      assertSize(session, queue, 0);
      assertSize(session, queue2, times * 2);
   }
   
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new JBossTestSetup(new ExpiryDestinationUnitTestCase("testExpiredMessagesMove")));
      suite.addTest(new JBossTestSetup(new ExpiryDestinationUnitTestCase("testSomeLoad")));
      return suite;
   }
}
