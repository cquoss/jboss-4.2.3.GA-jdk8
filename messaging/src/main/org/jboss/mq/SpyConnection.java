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
package org.jboss.mq;

import java.io.Serializable;

import javax.jms.ConnectionConsumer;
import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;

import org.jboss.util.UnreachableStatementException;

/**
 * This class implements javax.jms.QueueConnection and
 * javax.jms.TopicConnection
 * 
 * @author Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class SpyConnection extends Connection implements Serializable, TopicConnection, QueueConnection
{
   private static final long serialVersionUID = -6227193901482445607L;

   /** Unified */
   public static final int UNIFIED = 0; 

   /** Queue */
   public static final int QUEUE = 1; 

   /** Topic */
   public static final int TOPIC = 2; 
   
   /** The type of connection */
   private int type = UNIFIED;
   
   /**
	 * Create a new SpyConnection
	 * 
	 * @param userId the user
	 * @param password the password
	 * @param gcf the constructing class
	 * @throws JMSException for any error
	 */
   public SpyConnection(String userId, String password, GenericConnectionFactory gcf) throws JMSException
   {
      super(userId, password, gcf);
   }

   /**
	 * Create a new SpyConnection
	 * 
	 * @param gcf the constructing class
	 * @throws JMSException for any error
	 */
   public SpyConnection(GenericConnectionFactory gcf) throws JMSException
   {
      super(gcf);
   }

   /**
    * Create a new SpyConnection
    * 
    * @param type the type of connection 
    * @param userId the user
    * @param password the password
    * @param gcf the constructing class
    * @throws JMSException for any error
    */
   public SpyConnection(int type, String userId, String password, GenericConnectionFactory gcf) throws JMSException
   {
      super(userId, password, gcf);
      this.type = type;
   }

   /**
    * Create a new SpyConnection
    * @param type the type of connection 
    * @param gcf the constructing class
    * @throws JMSException for any error
    */
   public SpyConnection(int type, GenericConnectionFactory gcf) throws JMSException
   {
      super(gcf);
      this.type = type;
   }

   public ConnectionConsumer createConnectionConsumer(Destination destination, String messageSelector,
         ServerSessionPool sessionPool, int maxMessages) throws JMSException
   {
      checkClosed();
      if (destination == null)
         throw new InvalidDestinationException("Null destination");
      checkTemporary(destination);
      
      return new SpyConnectionConsumer(this, destination, messageSelector, sessionPool, maxMessages);
   }

   public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException
   {
      checkClosed();
      checkClientID();

      if (transacted)
         acknowledgeMode = 0;
      Session session = new SpySession(this, transacted, acknowledgeMode, false);

      //add the new session to the createdSessions list
      synchronized (createdSessions)
      {
         createdSessions.add(session);
      }

      return session;
   }

   public TopicSession createTopicSession(boolean transacted, int acknowledgeMode) throws JMSException
   {
      checkClosed();
      checkClientID();

      if (transacted)
         acknowledgeMode = 0;
      TopicSession session = new SpyTopicSession(this, transacted, acknowledgeMode);

      //add the new session to the createdSessions list
      synchronized (createdSessions)
      {
         createdSessions.add(session);
      }

      return session;
   }

   public ConnectionConsumer createConnectionConsumer(Topic topic, String messageSelector,
         ServerSessionPool sessionPool, int maxMessages) throws JMSException
   {
      checkClosed();
      if (type == QUEUE)
         throw new IllegalStateException("Cannot create a topic consumer on a QueueConnection");
      if (topic == null)
         throw new InvalidDestinationException("Null topic");
      checkClientID();
      checkTemporary(topic);

      return new SpyConnectionConsumer(this, topic, messageSelector, sessionPool, maxMessages);
   }

   public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String subscriptionName,
         String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException
   {
      checkClosed();
      if (type == QUEUE)
         throw new IllegalStateException("Cannot create a topic consumer on a QueueConnection");
      if (topic == null)
         throw new InvalidDestinationException("Null topic");
      if (topic instanceof TemporaryTopic)
         throw new InvalidDestinationException("Attempt to create a durable subscription for a temporary topic");

      if (subscriptionName == null || subscriptionName.trim().length() == 0)
         throw new JMSException("Null or empty subscription");

      SpyTopic t = new SpyTopic((SpyTopic) topic, getClientID(), subscriptionName, messageSelector);
      return new SpyConnectionConsumer(this, t, messageSelector, sessionPool, maxMessages);
   }

   public ConnectionConsumer createConnectionConsumer(Queue queue, String messageSelector,
         ServerSessionPool sessionPool, int maxMessages) throws JMSException
   {
      checkClosed();
      if (type == TOPIC)
         throw new IllegalStateException("Cannot create a queue consumer on a TopicConnection");
      if (queue == null)
         throw new InvalidDestinationException("Null queue");
      checkTemporary(queue);

      return new SpyConnectionConsumer(this, queue, messageSelector, sessionPool, maxMessages);
   }

   public QueueSession createQueueSession(boolean transacted, int acknowledgeMode) throws JMSException
   {
      checkClosed();
      checkClientID();
      if (transacted)
         acknowledgeMode = 0;
      QueueSession session = new SpyQueueSession(this, transacted, acknowledgeMode);

      //add the new session to the createdSessions list
      synchronized (createdSessions)
      {
         createdSessions.add(session);
      }

      return session;
   }
   
   TemporaryTopic getTemporaryTopic() throws JMSException
   {
      checkClosed();
      checkClientID();
      try
      {
         SpyTemporaryTopic temp = (SpyTemporaryTopic) serverIL.getTemporaryTopic(connectionToken);
         temp.setConnection(this);
         synchronized (temps)
         {
            temps.add(temp);
         }
         return temp;
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot create a Temporary Topic", t);
         throw new UnreachableStatementException();
      }
   }

   Topic createTopic(String name) throws JMSException
   {
      checkClosed();
      checkClientID();
      try
      {
         return serverIL.createTopic(connectionToken, name);
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot get the Topic from the provider", t);
         throw new UnreachableStatementException();
      }
   }

   TemporaryQueue getTemporaryQueue() throws JMSException
   {
      checkClosed();
      checkClientID();
      try
      {
         SpyTemporaryQueue temp = (SpyTemporaryQueue) serverIL.getTemporaryQueue(connectionToken);
         temp.setConnection(this);
         synchronized (temps)
         {
            temps.add(temp);
         }
         return temp;
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot create a Temporary Queue", t);
         throw new UnreachableStatementException();
      }
   }

   Queue createQueue(String name) throws JMSException
   {
      checkClosed();
      checkClientID();
      try
      {

         return serverIL.createQueue(connectionToken, name);
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Cannot get the Queue from the provider", t);
         throw new UnreachableStatementException();
      }
   }
}