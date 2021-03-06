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
package org.jboss.test.jbossmq.perf;

import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Category;
import org.jboss.test.JBossTestCase;

import EDU.oswego.cs.dl.util.concurrent.Semaphore;

/**
 * JBossMQPerfStressTestCase.java Some simple tests of JBossMQ
 *
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 * @version   $Revision: 57211 $
 */

public class InvocationLayerStressTest extends JBossTestCase
{
   Context context;
   QueueConnection queueConnection;
   TopicConnection topicConnection;
   static final int WORKER_COUNT = Integer.parseInt(System.getProperty("jbosstest.threadcount", "10"));
   static final int MESSAGE_COUNT = Integer.parseInt(System.getProperty("jbosstest.iterationcount", "500"));
   Semaphore exitSemaphore;

   /**
    * Constructor for the JBossMQPerfStressTestCase object
    *
    * @param name           Description of Parameter
    * @exception Exception  Description of Exception
    */
   public InvocationLayerStressTest(String name) throws Exception
   {
      super(name);
   }


   public void createQueue(String name)
   {
      try
      {
         ObjectName objn = new ObjectName("jboss.mq:service=DestinationManager");
         getServer().invoke(objn, "createQueue", new Object[]{name, name}, new String[]{String.class.getName(), String.class.getName()});
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public void createTopic(String name)
   {
      try
      {
         ObjectName objn = new ObjectName("jboss.mq:service=DestinationManager");
         getServer().invoke(objn, "createTopic", new Object[]{name, name}, new String[]{String.class.getName(), String.class.getName()});
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public void deleteQueue(String name)
   {
      try
      {
         ObjectName objn = new ObjectName("jboss.mq:service=DestinationManager");
         getServer().invoke(objn, "destroyQueue", new Object[]{name}, new String[]{String.class.getName()});
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public void deleteTopic(String name)
   {
      try
      {
         ObjectName objn = new ObjectName("jboss.mq:service=DestinationManager");
         getServer().invoke(objn, "destroyTopic", new Object[]{name}, new String[]{String.class.getName()});
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   protected void connect(String queueLoc, String topicLoc) throws Exception
   {
      context = new InitialContext();
      QueueConnectionFactory queueFactory = (QueueConnectionFactory) context.lookup(queueLoc);
      queueConnection = queueFactory.createQueueConnection();

      TopicConnectionFactory topicFactory = (TopicConnectionFactory) context.lookup(topicLoc);
      topicConnection = topicFactory.createTopicConnection();
   }

   protected void disconnect() throws Exception
   {
      queueConnection.close();
      topicConnection.close();
   }


   class QueueWorker extends Thread
   {
      String queueName;
      Throwable exception;
      Object signal = new Object();
      Category log = Category.getInstance(QueueWorker.class);

      QueueWorker(String queueName, String ilType)
      {
         super(queueName);
         this.queueName = queueName;
         this.log = Category.getInstance("QueueWorker."+queueName+"."+ilType);
      }

      public void run()
      {
         log.info("QueueWorker Running: " + queueName);

         try
         {
            work();
         }
         catch (Throwable e)
         {
            exception = e;
            log.error("Exception:", e);
         }

         // Signal the main thread that we are done.
         log.debug("Notifying main thread: ");
         exitSemaphore.release();

         log.info("QueueWorker Done: " + queueName);
      }

      void work() throws Exception
      {
         createQueue(queueName);
         QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
         Queue queue = (Queue) context.lookup(queueName);

         // Send the messages
         QueueSender sender = session.createSender(queue);
         sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
         Message message = session.createTextMessage("Test Message");
         for (int i = 0; i < MESSAGE_COUNT; i++)
         {
            sender.send(message);
            log.debug("Sent message " + i + " to queue :" + queueName);
         }

         // Receive the messages
         QueueReceiver receiver = session.createReceiver(queue);
         for (int i = 0; i < MESSAGE_COUNT; i++)
         {
            message = receiver.receive(5000);
            log.debug("Received message " + i + " from queue :" + queueName);
            if( message == null )
               fail("Received of msg timedout");
         }
         session.close();
         deleteQueue(queueName);
      }

   }
}
