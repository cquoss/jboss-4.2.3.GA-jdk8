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

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;

import org.jboss.test.JBossTestCase;

/**
 * Tests for receivers impl
 *
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version <tt>$Revision: 57211 $</tt>
 */
public abstract class ReceiversImplStressTest extends JBossTestCase
{
   static String QUEUE_FACTORY = "ConnectionFactory";
   static String QUEUE = "queue/ReceiversImpl";
   static int messages = 0;
   
   QueueConnection queueConnection;
   Queue queue;
   
   public ReceiversImplStressTest(String name) throws Exception
   {
      super(name);
      messages = getThreadCount() * getBeanCount();
   }

   public abstract class TestRunnable implements Runnable
   {
      public Throwable error = null;
      
      public abstract void doRun() throws Exception;
      
      public void run()
      {
         try
         {
            doRun();
         }
         catch (Throwable t)
         {
            log.error("Error in " + Thread.currentThread(), t);
            error = t;
         }
      }
   }

   public class ReceiverRunnable extends TestRunnable
   {
      int integer;
      
      MessageConsumer consumer;
      
      public ReceiverRunnable(int integer) throws Exception
      {
         this.integer = integer;
         QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
         consumer = session.createConsumer(queue);
      }
      
      public void doRun() throws Exception
      {
         int count = getBeanCount();
         while (count > 0)
         {
            consumer.receive();
            --count;
         }
      }
   }

   public class ReceiverNoWaitRunnable extends TestRunnable
   {
      int integer;
      
      MessageConsumer consumer;
      
      public ReceiverNoWaitRunnable(int integer) throws Exception
      {
         this.integer = integer;
         QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
         consumer = session.createConsumer(queue);
      }
      
      public void doRun() throws Exception
      {
         int count = getBeanCount();
         while (count > 0)
         {
            if (consumer.receiveNoWait() != null)
               --count;
         }
      }
   }
   
   public void testReceiversImplReceive() throws Exception
   {
      connect();
      try
      {
         ReceiverRunnable[] receivers = new ReceiverRunnable[getThreadCount()];
         Thread[] consumerThreads = new Thread[getThreadCount()];
         for (int i = 0; i < consumerThreads.length; ++i)
         {
            receivers[i] = new ReceiverRunnable(i);
            consumerThreads[i] = new Thread(receivers[i], "Consumer" + i);
         }
         for (int i = 0; i < consumerThreads.length; ++i)
            consumerThreads[i].start();

         QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageProducer producer = session.createProducer(queue);
         for (int i = 0; i < messages; ++i)
         {
            ObjectMessage message = session.createObjectMessage(new Integer(i));
            producer.send(message);
         }

         
         for (int i = 0; i < consumerThreads.length; ++i)
            consumerThreads[i].join();
         for (int i = 0; i < consumerThreads.length; ++i)
            assertNull(receivers[i].error);

         // Drain the queue
         MessageConsumer consumer = session.createConsumer(queue);
         while (consumer.receiveNoWait() != null);
      }
      finally
      {
         disconnect();
      }
   }
   
   public void testReceiversImplReceiveNoWait() throws Exception
   {
      connect();
      try
      {
         ReceiverNoWaitRunnable[] receivers = new ReceiverNoWaitRunnable[getThreadCount()];
         Thread[] consumerThreads = new Thread[getThreadCount()];
         for (int i = 0; i < consumerThreads.length; ++i)
         {
            receivers[i] = new ReceiverNoWaitRunnable(i);
            consumerThreads[i] = new Thread(receivers[i], "Consumer" + i);
         }
         for (int i = 0; i < consumerThreads.length; ++i)
            consumerThreads[i].start();

         QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageProducer producer = session.createProducer(queue);
         for (int i = 0; i < messages; ++i)
         {
            ObjectMessage message = session.createObjectMessage(new Integer(i));
            producer.send(message);
         }

         
         for (int i = 0; i < consumerThreads.length; ++i)
            consumerThreads[i].join();
         for (int i = 0; i < consumerThreads.length; ++i)
            assertNull(receivers[i].error);

         // Drain the queue
         MessageConsumer consumer = session.createConsumer(queue);
         while (consumer.receiveNoWait() != null);
      }
      finally
      {
         disconnect();
      }
   }

   protected void connect() throws Exception
   {
      Context context = getInitialContext();
      queue = (Queue) context.lookup(QUEUE);
      QueueConnectionFactory queueFactory = (QueueConnectionFactory) context.lookup(QUEUE_FACTORY);
      queueConnection = queueFactory.createQueueConnection();
      queueConnection.start();

      getLog().debug("Connection established.");
   }

   protected void disconnect()
   {
      try
      {
         if (queueConnection != null)
            queueConnection.close();
      }
      catch (Exception ignored)
      {
      }

      getLog().debug("Connection closed.");
   }
}
