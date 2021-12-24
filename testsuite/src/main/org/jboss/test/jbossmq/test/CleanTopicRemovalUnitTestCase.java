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

import javax.jms.Message;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
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
public class CleanTopicRemovalUnitTestCase extends JBossTestCase
{
   static String TOPIC_FACTORY = "ConnectionFactory";
   static ObjectName destinationManager = ObjectNameFactory.create("jboss.mq:service=DestinationManager");
   static ObjectName messageCache = ObjectNameFactory.create("jboss.mq:service=MessageCache");
   
   TopicConnection topicConnection;
   Topic topic;
   
   public CleanTopicRemovalUnitTestCase(String name) throws Exception
   {
      super(name);
   }

   public void testCleanTopicRemoval() throws Throwable
   {
      createTopic();
      try
      {
         connect();
         try
         {
            TopicSession session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            TopicPublisher publisher = session.createPublisher(topic);
            session.createSubscriber(topic);
            topicConnection.start();

            getLog().debug("Publish message");
            Message message = session.createMessage();
            publisher.publish(message);
            
            int beforeCacheCount = getCacheCount();
            getLog().debug("beforeCacheCount=" + beforeCacheCount);
            
            removeTopic();
            
            int afterCacheCount = getCacheCount();
            getLog().debug("afterCacheCount=" + afterCacheCount);
           
            assertEquals("Message should be removed ", afterCacheCount, beforeCacheCount - 1);
         }
         finally
         {
            disconnect();
         }
      }
      catch (Throwable t)
      {
         try
         {
            getLog().error("Error ", t);
            removeTopic();
         }
         catch (Throwable ignored)
         {
         }
         throw t;
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
            "cleanTopicRemovalTest",
            "topic/cleanTopicRemovalTest"
         },
         new String[]
         {
            String.class.getName(),
            String.class.getName()
         }
      );
      Context context = getInitialContext();
      topic = (Topic) context.lookup("topic/cleanTopicRemovalTest");
      
      log.debug("Got topic " + topic);
   }

   protected void removeTopic() throws Exception
   {
      getLog().debug("Remove topic");
      MBeanServerConnection server = getServer();
      server.invoke(destinationManager, "destroyTopic",
         new Object[]
         {
            "cleanTopicRemovalTest",
         },
         new String[]
         {
            String.class.getName()
         }
      );
   }

   protected int getCacheCount() throws Exception
   {
      MBeanServerConnection server = getServer();
      Integer cacheCount = (Integer) server.getAttribute(messageCache, "TotalCacheSize");
      return cacheCount.intValue();
   }
}

