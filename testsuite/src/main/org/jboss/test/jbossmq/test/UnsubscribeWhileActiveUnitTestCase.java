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

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.test.JBossTestCase;

/**
 * A test to make sure topic subscriptions are tidied up correctly
 *
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version <tt>$Revision: 57211 $</tt>
 */
public class UnsubscribeWhileActiveUnitTestCase extends JBossTestCase
{
   static String TOPIC_FACTORY = "ConnectionFactory";
   static ObjectName destinationManager = ObjectNameFactory.create("jboss.mq:service=DestinationManager");

   static String TOPIC_NAME = "UnsubscribeWhileActive";
   
   static String NAME1 = "UnsubscribeWhileActive1";
   static String NAME2 = "UnsubscribeWhileActive2";
   
   TopicConnection topicConnection;
   Topic topic;
   
   public UnsubscribeWhileActiveUnitTestCase(String name) throws Exception
   {
      super(name);
   }

   public void testUnsubscribeWhileActive() throws Throwable
   {
      createTopic();
      TopicSession session = null;
      TopicSubscriber c1 = null;
      TopicSubscriber c2 = null;
      try
      {
         connect();
         try
         {
            session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            c1 = session.createDurableSubscriber(topic, NAME1);
            c2 = session.createDurableSubscriber(topic, NAME2);
            topicConnection.start();
            
            try
            {
               session.unsubscribe(NAME1);
               fail("Should not be here! There is still a consumer for the subscription.");
            }
            catch (IllegalStateException expected)
            {
            }
            
            c2.close();
            session.unsubscribe(NAME2);
            
            c1.close();
            session.unsubscribe(NAME1);
         }
         finally
         {
            try
            {
               if (c1 != null)
                  c1.close();
            }
            catch (Exception ignored)
            {
            }
            try
            {
               if (session != null)
                  session.unsubscribe(NAME1);
            }
            catch (Exception ignored)
            {
            }
            try
            {
               if (c2 != null)
                  c2.close();
            }
            catch (Exception ignored)
            {
            }
            try
            {
               if (session != null)
                  session.unsubscribe(NAME2);
            }
            catch (Exception ignored)
            {
            }
            
            disconnect();
         }
      }
      catch (Throwable t)
      {
         getLog().error("Error ", t);
         throw t;
      }
      finally
      {
         try
         {
            removeTopic();
         }
         catch (Throwable ignored)
         {
         }
      }
   }

   public void testUnsubscribeWhileInTransaction() throws Throwable
   {
      createTopic();
      TopicSession session = null;
      TopicSubscriber c1 = null;
      try
      {
         connect();
         try
         {
            session = topicConnection.createTopicSession(true, Session.SESSION_TRANSACTED);
            c1 = session.createDurableSubscriber(topic, NAME1);
            topicConnection.start();

            TopicSession s1 = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            TopicPublisher p = s1.createPublisher(topic);
            Message m = s1.createMessage();
            p.publish(m);
            
            m = c1.receiveNoWait();
            if (m == null)
               fail("Should have got a message!");

            c1.close();
            
            try
            {
               session.unsubscribe(NAME1);
               fail("Should not be here! The consumer is closed but there is a message in the transaction.");
            }
            catch (IllegalStateException expected)
            {
            }
            
            session.commit();
            session.unsubscribe(NAME1);
         }
         finally
         {
            try
            {
               if (c1 != null)
                  c1.close();
            }
            catch (Exception ignored)
            {
            }
            try
            {
               if (session != null)
                  session.unsubscribe(NAME1);
            }
            catch (Exception ignored)
            {
            }
            
            disconnect();
         }
      }
      catch (Throwable t)
      {
         getLog().error("Error ", t);
         throw t;
      }
      finally
      {
         try
         {
            removeTopic();
         }
         catch (Throwable ignored)
         {
         }
      }
   }

   protected void connect() throws Exception
   {
      Context context = getInitialContext();
      TopicConnectionFactory topicFactory = (TopicConnectionFactory) context.lookup(TOPIC_FACTORY);
      topicConnection = topicFactory.createTopicConnection();

      getLog().debug("Connection established.");
   }

   protected void disconnect()
   {
      try
      {
         if (topicConnection != null)
            topicConnection.close();
      }
      catch (Throwable ignored)
      {
         getLog().warn("Ignored", ignored);
      }

      getLog().debug("Connection closed.");
   }

   protected void createTopic() throws Exception
   {
      getLog().debug("Create topic");
      MBeanServerConnection server = getServer();
      server.invoke(destinationManager, "createTopic",
         new Object[]
         {
            TOPIC_NAME,
            "topic/" + TOPIC_NAME
         },
         new String[]
         {
            String.class.getName(),
            String.class.getName()
         }
      );
      Context context = getInitialContext();
      topic = (Topic) context.lookup("topic/" + TOPIC_NAME);
      
      log.debug("Got topic " + topic);
   }

   protected void removeTopic() throws Exception
   {
      getLog().debug("Remove topic");
      MBeanServerConnection server = getServer();
      server.invoke(destinationManager, "destroyTopic",
         new Object[]
         {
            TOPIC_NAME,
         },
         new String[]
         {
            String.class.getName()
         }
      );
   }
}
