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
import java.util.ArrayList;
import java.util.Iterator;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.jms.Queue;
import javax.naming.Context;

import org.jboss.test.JBossTestCase;

/**
 * JBossMQPerfStressTestCase.java Some simple tests of JBossMQ
 *
 * @author
 * @version
 */
public class SendReplyPerfStressTestCase extends JBossTestCase
{
   // Provider specific
   static String TOPIC_FACTORY = "ConnectionFactory";
   static String QUEUE_FACTORY = "ConnectionFactory";

   static String TEST_QUEUE = "queue/testQueue";
   static String TEST_TOPIC = "topic/testTopic";

   static byte[] PERFORMANCE_TEST_DATA_PAYLOAD = new byte[10];

   //JMSProviderAdapter providerAdapter;
   static Context context;
   static QueueConnection queueConnection;
   static TopicConnection topicConnection;

   public SendReplyPerfStressTestCase(String name) throws Exception
   {
      super(name);
   }

   /**
    * The main entry-point for the JBossMQPerfStressTestCase class
    *
    * @param args  The command line arguments
    */
   public static void main(String[] args)
   {

      String newArgs[] = {"org.jboss.test.jbossmq.perf.SendReplyPerfStressTestCase"};
      junit.swingui.TestRunner.main(newArgs);
   }

   public static class State
   {
      public int expected;
      public int finished = 0;
      public ArrayList errors = new ArrayList();
      public State(int expected)
      {
         this.expected = expected;
      }
      public synchronized void addError(Throwable t)
      {
         errors.add(t);
      }
      public synchronized void finished()
      {
         ++finished;
         if (finished == expected)
            notifyAll();
      }
      public synchronized void waitForFinish() throws Exception
      {
         if (finished == expected)
            return;
         wait();
      }
   }

   public static class MessageQueueSender
      implements Runnable
   {
      State state;
      public MessageQueueSender(State state)
      {
         this.state = state;
      }

      public void run()
      {
         try
         {
            Queue queue = (Queue)context.lookup(TEST_QUEUE);
            QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            TemporaryQueue temp = session.createTemporaryQueue();
            Message message = session.createTextMessage();
            message.setJMSReplyTo(temp);

            QueueSender sender = session.createSender(queue);
            sender.send(message);

            QueueReceiver receiver = session.createReceiver(temp);
            receiver.receive();
            receiver.close();
            temp.delete();
            
            session.close();
         }
         catch (Throwable t)
         {
            state.addError(t);
         }
         finally
         {
            state.finished();
         }
      }
   }

   public static class MessageTopicSender
      implements Runnable
   {
      State state;
      public MessageTopicSender(State state)
      {
         this.state = state;
      }

      public void run()
      {
         try
         {
            Topic topic = (Topic)context.lookup(TEST_TOPIC);
            TopicSession session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            Message message = session.createTextMessage();

            QueueSession qsession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            TemporaryQueue temp = qsession.createTemporaryQueue();
            message.setJMSReplyTo(temp);

            TopicPublisher publisher = session.createPublisher(topic);
            publisher.publish(message);

            QueueReceiver receiver = qsession.createReceiver(temp);
            receiver.receive();
            receiver.close();
            
            session.close();
         }
         catch (Throwable t)
         {
            state.addError(t);
         }
         finally
         {
            state.finished();
         }
      }
   }

   public static class MessageReplier
      implements MessageListener
   {
      State state;
      public MessageReplier(State state)
      {
         this.state = state;
      }
      public void onMessage(Message message)
      {
         try
         {
            QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue replyQueue = session.createQueue(((Queue)message.getJMSReplyTo()).getQueueName());
            QueueSender sender = session.createSender(replyQueue);
            sender.send(message);
            sender.close();
            session.close();
         }
         catch (Throwable t)
         {
            state.addError(t);
         }
      }
   }

   public void testSendReplyQueue() throws Exception
   {
      drainQueue();

      // Set up the workers
      State state = new State(getThreadCount());
      MessageReplier replier = new MessageReplier(state);
      Thread[] threads = new Thread[getThreadCount()];
      for (int i = 0; i < threads.length; ++i)
          threads[i] = new Thread(new MessageQueueSender(state));

      // Register the message listener
      Queue queue = (Queue)context.lookup(TEST_QUEUE);
      QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      QueueReceiver receiver = session.createReceiver(queue);
      receiver.setMessageListener(replier);
      queueConnection.start();

      // Start the senders
      for (int i = 0; i < threads.length; ++i)
          threads[i].start();

      // Wait for it to finish
      state.waitForFinish();

      // Report the result
      for (Iterator i = state.errors.iterator(); i.hasNext();)
         getLog().error("Error", (Throwable) i.next());
      if (state.errors.size() > 0)
         throw new RuntimeException("Test failed with " + state.errors.size() + " errors");
   }

   public void testSendReplyTopic() throws Exception
   {
      // Set up the workers
      State state = new State(getThreadCount());
      MessageReplier replier = new MessageReplier(state);

      Thread[] threads = new Thread[getThreadCount()];
      for (int i = 0; i < threads.length; ++i)
          threads[i] = new Thread(new MessageTopicSender(state));


      // Register the message listener
      Topic topic = (Topic)context.lookup(TEST_TOPIC);
      TopicSession session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      TopicSubscriber subscriber = session.createSubscriber(topic);
      subscriber.setMessageListener(replier);
      topicConnection.start();
      queueConnection.start();

      // Start the senders
      for (int i = 0; i < threads.length; ++i)
          threads[i].start();

      // Wait for it to finish
      state.waitForFinish();

      // Report the result
      for (Iterator i = state.errors.iterator(); i.hasNext();)
         getLog().error("Error", (Throwable) i.next());
      if (state.errors.size() > 0)
         throw new RuntimeException("Test failed with " + state.errors.size() + " errors");
   }

   protected void setUp() throws Exception
   {
      getLog().info("Starting test: " + getName());

      context = getInitialContext();

      QueueConnectionFactory queueFactory = (QueueConnectionFactory)context.lookup(QUEUE_FACTORY);
      queueConnection = queueFactory.createQueueConnection();

      TopicConnectionFactory topicFactory = (TopicConnectionFactory)context.lookup(TOPIC_FACTORY);
      topicConnection = topicFactory.createTopicConnection();

      getLog().debug("Connection to JBossMQ established.");
   }

   protected void tearDown() throws Exception
   {
      getLog().info("Ended test: " + getName());
      queueConnection.close();
      topicConnection.close();
   }   

   private void drainQueue() throws Exception
   {
      QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue)context.lookup(TEST_QUEUE);

      QueueReceiver receiver = session.createReceiver(queue);
      queueConnection.start();
      Message message = receiver.receive(50);
      int c = 0;
      while (message != null)
      {
         message = receiver.receive(50);
         c++;
      }

      if (c != 0)
         getLog().debug("  Drained " + c + " messages from the queue");
      session.close();
      queueConnection.stop();

   }
}
