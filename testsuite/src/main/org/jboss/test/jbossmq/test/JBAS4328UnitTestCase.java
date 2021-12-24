/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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

import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.ServerSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Category;
import org.jboss.test.JBossTestCase;

/**
 * This test checks the expiration mechanism on an application server message listener.
 * 
 * @author carlo
 *
 */
public class JBAS4328UnitTestCase extends JBossTestCase
{
   /** The default TopicFactory jndi name */
   static String TOPIC_FACTORY = "ConnectionFactory";
   /** The default QueueFactory jndi name */
   static String QUEUE_FACTORY = "ConnectionFactory";

   static String TEST_QUEUE = "queue/testQueue";

   static Context context;
   static QueueConnection queueConnection;
   static TopicConnection topicConnection;

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new JBAS4328UnitTestCase("testApplicationServerStuff"));
      suite.addTest(new JBAS4328UnitTestCase("testApplicationServerExpiration"));
      
      return suite;
   }
   
   public JBAS4328UnitTestCase(String name)
   {
      super(name);
   }

   protected void connect() throws Exception
   {

      if (context == null)
      {

         context = new InitialContext();

      }
      QueueConnectionFactory queueFactory = (QueueConnectionFactory) context.lookup(QUEUE_FACTORY);
      queueConnection = queueFactory.createQueueConnection();

      TopicConnectionFactory topicFactory = (TopicConnectionFactory) context.lookup(TOPIC_FACTORY);
      topicConnection = topicFactory.createTopicConnection();

      getLog().debug("Connection to spyderMQ established.");

   }

   protected void disconnect() throws Exception
   {
      queueConnection.close();
      topicConnection.close();
   }

   private int processed = 0;
   
   /**
    * Copy of JBossMQUnitTest.testApplicationServerStuff with a sleep of 5 seconds
    * during processing of the message in the message listener.
    * 
    * @throws Exception
    */
   public void testApplicationServerExpiration() throws Exception
   {
      getLog().debug("Starting testing app server expiration");
      connect();

      Queue testQueue = (Queue) context.lookup(TEST_QUEUE);
      final QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

      session.setMessageListener(new MessageListener()
      {
         @SuppressWarnings("deprecation")
         public void onMessage(Message mess)
         {
            Category log = Category.getInstance(getClass().getName());
            log.debug("Processing message");
            try
            {
               if (System.currentTimeMillis() > mess.getJMSExpiration())
                  log.warn("*** message is expired *** (" + System.currentTimeMillis() + " > " + mess.getJMSExpiration() + ")");
               
               if (mess instanceof TextMessage)
                  log.debug(((TextMessage) mess).getText());
               
               // block consuming for 5 seconds
               Thread.sleep(5000);
               processed++;
            }
            catch (Exception e)
            {
               log.error("Error", e);
            }
         }
      });

      QueueSender sender = session.createSender(testQueue);
      sender.send(session.createTextMessage("Hi"), DeliveryMode.NON_PERSISTENT, 4, 1000);
      sender.send(session.createTextMessage("There"), DeliveryMode.NON_PERSISTENT, 4, 1000);
      sender.send(session.createTextMessage("Guys"), DeliveryMode.NON_PERSISTENT, 4, 1000);
      queueConnection.createConnectionConsumer(testQueue, null, new ServerSessionPool()
      {
         @SuppressWarnings("deprecation")
         public ServerSession getServerSession()
         {
            Category.getInstance(getClass().getName()).debug("Getting server session.");
            return new ServerSession()
            {
               public Session getSession()
               {
                  return session;
               }
               public void start()
               {
                  Category.getInstance(getClass().getName()).debug("Starting server session.");
                  session.run();
               }
            };
         }
      }, 10);

      queueConnection.start();

      try
      {
         Thread.sleep(3 * 5000 + 5000);
      }
      catch (Exception e)
      {
      }

      disconnect();
      
      assertEquals("Expected only one message, the rest should have expired", 1, processed);
      
      getLog().debug("Testing app server expiration passed");
   }

   /**
    * Copy of JBossMQUnitTest.testApplicationServerStuff.
    * 
    * @throws Exception
    */
   public void testApplicationServerStuff() throws Exception
   {
      getLog().debug("Starting testing app server stuff");
      connect();

      Queue testQueue = (Queue) context.lookup(TEST_QUEUE);
      final QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

      session.setMessageListener(new MessageListener()
      {
         @SuppressWarnings("deprecation")
         public void onMessage(Message mess)
         {
            Category log = Category.getInstance(getClass().getName());
            log.debug("Processing message");
            try
            {
               if (mess instanceof TextMessage)
                  log.debug(((TextMessage) mess).getText());
            }
            catch (Exception e)
            {
               log.error("Error", e);
            }
         }
      });

      QueueSender sender = session.createSender(testQueue);
      sender.send(session.createTextMessage("Hi"));
      sender.send(session.createTextMessage("There"));
      sender.send(session.createTextMessage("Guys"));
      queueConnection.createConnectionConsumer(testQueue, null, new ServerSessionPool()
      {
         @SuppressWarnings("deprecation")
         public ServerSession getServerSession()
         {
            Category.getInstance(getClass().getName()).debug("Getting server session.");
            return new ServerSession()
            {
               public Session getSession()
               {
                  return session;
               }
               public void start()
               {
                  Category.getInstance(getClass().getName()).debug("Starting server session.");
                  session.run();
               }
            };
         }
      }, 10);

      queueConnection.start();

      try
      {
         Thread.sleep(5 * 1000);
      }
      catch (Exception e)
      {
      }

      disconnect();
      getLog().debug("Testing app server stuff passed");
   }


}
