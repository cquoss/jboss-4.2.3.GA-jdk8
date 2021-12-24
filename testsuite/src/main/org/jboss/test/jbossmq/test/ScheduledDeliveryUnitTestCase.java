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
import javax.jms.DeliveryMode;
import javax.jms.QueueSession;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.QueueBrowser;
import javax.management.ObjectName;

import org.jboss.mq.server.MessageCacheMBean;
import org.jboss.test.JBossTestSetup;

import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * JBossMQUnitTestCase.java
 *
 * Some simple tests of spyderMQ
 *
 * @author Hiram Chirino <Cojonudo14@hotmail.com>
 * @version $Revision: 57211 $
 */
public class ScheduledDeliveryUnitTestCase
   extends JBossMQUnitTest
{
   
   public ScheduledDeliveryUnitTestCase(String name) throws Exception
   {
      super(name);
   }
   
   /**
    * Test that messages are ordered by scheduled date.
    * Tests vendor property.
    * <code>SpyMessage.PROPERTY_SCHEDULED_DELIVERY</code>
    */
   public void testScheduledDelivery() throws Exception
   {
      getLog().debug("Starting ScheduledDelivery test");
      
      connect();
      
      queueConnection.start();
      
      drainQueue();
      
      QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue)context.lookup(TEST_QUEUE);
      QueueSender sender = session.createSender(queue);

      long now = System.currentTimeMillis();
      
      TextMessage message = session.createTextMessage();
      message.setText("normal");
      message.setLongProperty("JMS_JBOSS_SCHEDULED_DELIVERY", 0);
      sender.send(message);

      message.setText("late");
      message.setLongProperty("JMS_JBOSS_SCHEDULED_DELIVERY", now + 5000);
      sender.send(message, DeliveryMode.PERSISTENT, 10, 0);

      message.setText("early");
      message.setLongProperty("JMS_JBOSS_SCHEDULED_DELIVERY", now + 1000);
      sender.send(message, DeliveryMode.PERSISTENT, 7, 0);
      
      QueueBrowser browser = session.createBrowser( queue );
      Enumeration i = browser.getEnumeration();
      i.nextElement();
      if (i.hasMoreElements())
        fail("Should only find two messages now");

      Thread.sleep(3000);

      i = browser.getEnumeration();
      message = (TextMessage)i.nextElement();
      if (!message.getText().equals("early"))
        throw new Exception("Queue is not scheduling messages correctly. Unexpected Message:"+message);
      i.nextElement();
      if (i.hasMoreElements())
        fail("Should only find three messages now");

      Thread.sleep(3000);
      
      i = browser.getEnumeration();
      message = (TextMessage)i.nextElement();
      if (!message.getText().equals("late"))
        throw new Exception("Queue is not scheduling messages correctly. Unexpected Message:"+message);
      i.nextElement();
      i.nextElement();

      disconnect();
      getLog().debug("ScheduledDelivery passed");
   }

   public void testScheduledDeliveryRemoveAll() throws Exception
   {
      getLog().debug("Starting ScheduledDelivery remove All test");
      
      getServer().invoke(new ObjectName("jboss.mq.destination:service=Queue,name=testQueue"), "removeAllMessages", null, null);
      Integer before = (Integer) getServer().getAttribute(MessageCacheMBean.OBJECT_NAME, "TotalCacheSize");
      
      connect();
      try
      {
         
         QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
         Queue queue = (Queue)context.lookup(TEST_QUEUE);
         QueueSender sender = session.createSender(queue);

         long now = System.currentTimeMillis();
         
         TextMessage message = session.createTextMessage();
         message.setText("scheduled");
         message.setLongProperty("JMS_JBOSS_SCHEDULED_DELIVERY", now + 10000);
         sender.send(message, DeliveryMode.PERSISTENT, 10, 0);
         
         getServer().invoke(new ObjectName("jboss.mq.destination:service=Queue,name=testQueue"), "removeAllMessages", null, null);

         Integer after = (Integer) getServer().getAttribute(MessageCacheMBean.OBJECT_NAME, "TotalCacheSize");
         assertEquals("Message should have been removed ", before, after);
      }
      finally
      {
         disconnect();
      }
      getLog().debug("ScheduledDelivery remove All passed");
   }
   
   protected void setUp() throws Exception
   {
      ScheduledDeliveryUnitTestCase.TOPIC_FACTORY = "ConnectionFactory";
      ScheduledDeliveryUnitTestCase.QUEUE_FACTORY = "ConnectionFactory";
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new JBossTestSetup(new ScheduledDeliveryUnitTestCase("testScheduledDelivery")));
      suite.addTest(new JBossTestSetup(new ScheduledDeliveryUnitTestCase("testScheduledDeliveryRemoveAll")));
      return suite;
   }

   static public void main( String []args )
   {
      String newArgs[] = { "org.jboss.test.jbossmq.test.ScheduledDeliveryUnitTestCase" };
      junit.swingui.TestRunner.main(newArgs);
   }
}
