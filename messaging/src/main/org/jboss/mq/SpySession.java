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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSubscriber;
import javax.jms.XASession;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;

/**
 * This class implements javax.jms.Session and javax.jms.XASession
 * 
 * @author Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author Hiram Chirino (Cojonudo14@hotmail.com) @created August 16, 2001
 * @version $Revision: 57198 $
 */
public class SpySession implements Session, XASession
{
   /** The log */
   static Logger log = Logger.getLogger(SpySession.class);

   /** Whether trace is enabled */
   static boolean trace = log.isTraceEnabled();

   /** The connection object to which this session is linked */
   public Connection connection;

   /** Is this session running right now? */
   public boolean running;
   /** Is this session transacted ? */
   protected boolean transacted;
   /** What is the type of acknowledgement ? */
   protected int acknowledgeMode;
   /** MessageConsumers created by this session */
   protected HashSet consumers;
   /** MessageProducers created by this session */
   protected HashSet producers;
   /** The delivery lock */
   protected Object deliveryLock = new Object();
   /** Whether we are doing asynchronous delivery */
   protected boolean inDelivery = false;
   
   /**
	 * This consumer is the consumer that receives messages for the
	 * MessageListener assigned to the session. The SpyConnectionConsumer
	 * delivers messages to
	 */
   SpyMessageConsumer sessionConsumer;

   /** Is the session closed ? */
   SynchronizedBoolean closed = new SynchronizedBoolean(false);

   /** Used to lock the run() method */
   Object runLock = new Object();

   /**
	 * The transctionId of the current transaction (registed with the
	 * SpyXAResourceManager).
	 */
   private Object currentTransactionId;

   /** If this is an XASession, we have an associated XAResource */
   SpyXAResource spyXAResource;

   /** Optional Connection consumer methods */
   LinkedList messages = new LinkedList();

   /** keep track of unacknowledged messages */
   ArrayList unacknowledgedMessages = new ArrayList();

   /**
	 * Create a new SpySession
	 * 
	 * @param conn the connection
	 * @param trans is the session transacted
	 * @param acknowledge the acknowledgement mode
	 * @param xaSession is the session an xa session
	 */
   SpySession(Connection conn, boolean trans, int acknowledge, boolean xaSession)
   {
      trace = log.isTraceEnabled();

      connection = conn;
      transacted = trans;
      acknowledgeMode = acknowledge;
      if (xaSession)
         spyXAResource = new SpyXAResource(this);

      running = true;
      consumers = new HashSet();
      producers = new HashSet();

      //Have a TX ready with the resource manager.
      if (spyXAResource == null && transacted)
         currentTransactionId = connection.spyXAResourceManager.startTx();

      if (trace)
         log.trace("New session " + this);
   }

   /**
	 * JMS 11.2.21.2 Note that the acknowledge method of Message acknowledges
	 * all messages received on that messages session.
	 * 
	 * JMS 11.3.2.2.3 Message.acknowledge method: Clarify that the method
	 * applies to all consumed messages of the session. Rationale for this
	 * change: A possible misinterpretation of the existing Java API
	 * documentation for Message.acknowledge assumed that only messages received
	 * prior to this message should be acknowledged. The updated Java API
	 * documentation statement emphasizes that message acknowledgement is really
	 * a session-level activity and that this message is only being used to
	 * identify the session in order to acknowledge all messages consumed by the
	 * session. The acknowledge method was placed in the message object only to
	 * enable easy access to acknowledgement capability within a message
	 * listeners onMessage method. This change aligns the specification and Java
	 * API documentation to define Message.acknowledge in the same manner.
	 * 
	 * @param message the message to acknowledge
	 * @param ack the acknowledgement request
	 * @throws JMSException for any error
	 */
   public void doAcknowledge(Message message, AcknowledgementRequest ack) throws JMSException
   {
      checkClosed();
      //if we are acking, ack all messages consumed by this session
      if (ack.isAck())
      {
         synchronized (unacknowledgedMessages)
         {
            if (trace)
               log.trace("Acknowledging message " + ack);

            //ack the current message
            connection.send(((SpyMessage) message).getAcknowledgementRequest(true));
            unacknowledgedMessages.remove(message);

            //ack the other messages consumed in this session
            Iterator i = unacknowledgedMessages.iterator();
            while (i.hasNext())
            {
               Message mess = (Message) i.next();
               i.remove();
               connection.send(((SpyMessage) mess).getAcknowledgementRequest(true));
            }
         }
      }
      //if we are nacking, only nack the one message
      else
      {
         if (trace)
            log.trace("Nacking message " + message.getJMSMessageID());

         //nack the current message
         unacknowledgedMessages.remove(message);
         connection.send(ack);
      }
   }

   /**
	 * Retrieve the XA resource manager
	 * 
	 * @return the resource manager
	 */
   public SpyXAResourceManager getXAResourceManager()
   {
      return connection.spyXAResourceManager;
   }

   public void setMessageListener(MessageListener listener) throws JMSException
   {
      checkClosed();

      if (trace)
         log.trace("Set message listener " + listener + " " + this);

      sessionConsumer = new SpyMessageConsumer(this, true);
      sessionConsumer.setMessageListener(listener);
   }

   public boolean getTransacted() throws JMSException
   {
      checkClosed();
      return transacted;
   }

   public MessageListener getMessageListener() throws JMSException
   {
      checkClosed();
      if (sessionConsumer == null)
         return null;

      return sessionConsumer.getMessageListener();
   }

   public BytesMessage createBytesMessage() throws JMSException
   {
      checkClosed();
      SpyBytesMessage message = MessagePool.getBytesMessage();
      message.header.producerClientId = connection.getClientID();
      return message;
   }

   public MapMessage createMapMessage() throws JMSException
   {
      checkClosed();
      SpyMapMessage message = MessagePool.getMapMessage();
      message.header.producerClientId = connection.getClientID();
      return message;
   }

   public Message createMessage() throws JMSException
   {
      checkClosed();
      SpyMessage message = MessagePool.getMessage();
      message.header.producerClientId = connection.getClientID();
      return message;
   }

   public ObjectMessage createObjectMessage() throws JMSException
   {
      checkClosed();
      SpyObjectMessage message = MessagePool.getObjectMessage();
      message.header.producerClientId = connection.getClientID();
      return message;
   }

   public ObjectMessage createObjectMessage(Serializable object) throws JMSException
   {
      checkClosed();
      SpyObjectMessage message = MessagePool.getObjectMessage();
      message.setObject(object);
      message.header.producerClientId = connection.getClientID();
      return message;
   }

   public StreamMessage createStreamMessage() throws JMSException
   {
      checkClosed();
      SpyStreamMessage message = MessagePool.getStreamMessage();
      message.header.producerClientId = connection.getClientID();
      return message;
   }

   public TextMessage createTextMessage() throws JMSException
   {
      checkClosed();
      SpyTextMessage message = MessagePool.getTextMessage();
      message.header.producerClientId = connection.getClientID();
      return message;
   }

   // Delivers messages queued by ConnectionConsumer to the message listener
   public void run()
   {
      synchronized (messages)
      {
         if (trace)
            log.trace("Run messages=" + messages.size() + " " + this);
         while (messages.size() > 0)
         {
            SpyMessage message = (SpyMessage) messages.removeFirst();
            try
            {
               if (sessionConsumer == null)
               {
                  log.warn("Session has no message listener set, cannot process message. " + this);
                  //Nack message
                  connection.send(message.getAcknowledgementRequest(false));
               }
               else
               {
                  sessionConsumer.addMessage(message);
               }
            }
            catch (Throwable ignore)
            {
               if (trace)
                  log.trace("Ignored error from session consumer", ignore);
            }
         }
      }
   }

   public void close() throws JMSException
   {
      if (closed.set(true))
         return;

      if (trace)
         log.trace("Session closing " + this);

      JMSException exception = null;

      if (trace)
         log.trace("Closing consumers " + this);

      Iterator i;
      synchronized (consumers)
      {
         //notify the sleeping synchronous listeners
         if (sessionConsumer != null)
         {
            try
            {
               sessionConsumer.close();
            }
            catch (Throwable t)
            {
               log.trace("Error closing session consumer", t);
            }
         }

         i = new ArrayList(consumers).iterator();
      }

      while (i.hasNext())
      {
         SpyMessageConsumer messageConsumer = (SpyMessageConsumer) i.next();
         try
         {
            messageConsumer.close();
         }
         catch (Throwable t)
         {
            log.trace("Error closing message consumer", t);
         }
      }

      synchronized (producers)
      {
         i = new ArrayList(producers).iterator();
      }

      while (i.hasNext())
      {
         SpyMessageProducer messageProducer = (SpyMessageProducer) i.next();
         try
         {
            messageProducer.close();
         }
         catch (InvalidDestinationException ignored)
         {
            log.warn(ignored.getMessage(), ignored);
         }
         catch (Throwable t)
         {
            log.trace("Error closing message producer", t);
         }
      }

      if (trace)
         log.trace("Close handling unacknowledged messages " + this);
      try
      {
         if (spyXAResource == null)
         {
            if (transacted)
               internalRollback();
            else
            {
               i = unacknowledgedMessages.iterator();
               while (i.hasNext())
               {
                  SpyMessage message = (SpyMessage) i.next();
                  connection.send(message.getAcknowledgementRequest(false));
                  i.remove();
               }
            }
         }
      }
      catch (Throwable t)
      {
         if (exception == null)
            exception = SpyJMSException.getAsJMSException("Error nacking message", t);
      }

      if (trace)
         log.trace("Informing connection of close " + this);
      connection.sessionClosing(this);

      // Throw the first exception
      if (exception != null)
         throw exception;
   }

   //Commit a transacted session
   public void commit() throws JMSException
   {
      checkClosed();
      trace = log.isTraceEnabled();

      //Don't deliver any more messages while commiting
      synchronized (runLock)
      {
         if (spyXAResource != null)
            throw new javax.jms.TransactionInProgressException("Should not be call from a XASession");
         if (!transacted)
            throw new IllegalStateException("The session is not transacted");

         if (trace)
            log.trace("Committing transaction " + this);
         try
         {
            connection.spyXAResourceManager.endTx(currentTransactionId, true);
            connection.spyXAResourceManager.commit(currentTransactionId, true);
         }
         catch (Throwable t)
         {
            SpyJMSException.rethrowAsJMSException("Could not commit", t);
         }
         finally
         {
            unacknowledgedMessages.clear();
            try
            {
               currentTransactionId = connection.spyXAResourceManager.startTx();

               if (trace)
                  log.trace("Current transaction id: " + currentTransactionId + " " + this);
            }
            catch (Throwable ignore)
            {
               if (trace)
                  log.trace("Failed to start tx " + this, ignore);
            }
         }
      }
   }

   public void rollback() throws JMSException
   {
      checkClosed();
      trace = log.isTraceEnabled();

      synchronized (runLock)
      {
         internalRollback();
      }
   }

   public void recover() throws JMSException
   {
      checkClosed();
      boolean stopped = connection.modeStop;
      
      synchronized (runLock)
      {
         if (currentTransactionId != null)
            throw new IllegalStateException("The session is transacted");

         if (trace)
            log.trace("Session recovery stopping delivery " + this);
         try
         {
            connection.stop();
            running = false;
         }
         catch (Throwable t)
         {
            SpyJMSException.rethrowAsJMSException("Could not stop message delivery", t);
         }

         // Loop over all consumers, check their unacknowledged messages, set
         // then as redelivered and add back to the list of messages
         try
         {
            synchronized (messages)
            {
               if (stopped == false)
               {
                  if (trace)
                     log.trace("Recovering: unacknowledged messages=" + unacknowledgedMessages + " " + this);
                  Iterator i = consumers.iterator();
                  while (i.hasNext())
                  {
                     SpyMessageConsumer consumer = (SpyMessageConsumer) i.next();

                     Iterator ii = unacknowledgedMessages.iterator();
                     while (ii.hasNext())
                     {
                        SpyMessage message = (SpyMessage) ii.next();

                        if (consumer.getSubscription().accepts(message.header))
                        {
                           message.setJMSRedelivered(true);
                           consumer.messages.addLast(message);
                           ii.remove();
                           if (trace)
                              log.trace("Recovered: message=" + message + " consumer=" + consumer);
                        }
                     }
                  }
               }

               // We no longer have consumers for the remaining messages
               Iterator i = unacknowledgedMessages.iterator();
               while (i.hasNext())
               {
                  SpyMessage message = (SpyMessage) i.next();
                  connection.send(message.getAcknowledgementRequest(false));
                  i.remove();
                  if (trace)
                     log.trace("Recovered: nacked with no consumer message=" + message + " " + this);
               }
            }
         }
         catch (Throwable t)
         {
            SpyJMSException.rethrowAsJMSException("Unable to recover session ", t);
         }
         // Restart the delivery sequence including all unacknowledged messages
         // that had
         // been previously delivered. Redelivered messages do not have to be
         // delivered
         // in exactly their original delivery order.

         if (stopped == false)
         {
            if (trace)
               log.trace("Recovery restarting message delivery " + this);
            try
            {
               running = true;
               connection.start();

               Iterator i = consumers.iterator();
               while (i.hasNext())
                  ((SpyMessageConsumer) i.next()).restartProcessing();
            }
            catch (Throwable t)
            {
               SpyJMSException.rethrowAsJMSException("Could not resume message delivery", t);
            }
         }
      }
   }

   public TextMessage createTextMessage(String string) throws JMSException
   {
      checkClosed();
      SpyTextMessage message = new SpyTextMessage();
      message.setText(string);
      message.header.producerClientId = connection.getClientID();
      return message;
   }

   public int getAcknowledgeMode() throws JMSException
   {
      return acknowledgeMode;
   }

   public MessageConsumer createConsumer(Destination destination) throws JMSException
   {
      return createConsumer(destination, null, false);
   }

   public MessageConsumer createConsumer(Destination destination, String messageSelector) throws JMSException
   {
      return createConsumer(destination, messageSelector, false);
   }

   public MessageConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal)
         throws JMSException
   {
      if (destination instanceof Topic)
         return createSubscriber((Topic) destination, messageSelector, noLocal);
      else
         return createReceiver((Queue) destination, messageSelector);
   }

   public MessageProducer createProducer(Destination destination) throws JMSException
   {
      if (destination instanceof Topic)
         return createPublisher((Topic) destination);
      else
         return createSender((Queue) destination);
   }

   public QueueBrowser createBrowser(Queue queue) throws JMSException
   {
      return createBrowser(queue, null);
   }

   public QueueBrowser createBrowser(Queue queue, String messageSelector) throws JMSException
   {
      checkClosed();
      if (this instanceof SpyTopicSession)
         throw new IllegalStateException("Not allowed for a TopicSession");
      if (queue == null)
         throw new InvalidDestinationException("Cannot browse a null queue.");
      return new SpyQueueBrowser(this, queue, messageSelector);
   }

   public QueueReceiver createReceiver(Queue queue) throws JMSException
   {
      return createReceiver(queue, null);
   }

   public QueueReceiver createReceiver(Queue queue, String messageSelector) throws JMSException
   {
      checkClosed();
      if (queue == null)
         throw new InvalidDestinationException("Queue cannot be null.");

      connection.checkTemporary(queue);
      SpyQueueReceiver receiver = new SpyQueueReceiver(this, queue, messageSelector);
      addConsumer(receiver);

      return receiver;
   }

   public QueueSender createSender(Queue queue) throws JMSException
   {
      checkClosed();
      SpyQueueSender producer = new SpyQueueSender(this, queue);
      addProducer(producer);
      return producer;
   }

   public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException
   {
      return createDurableSubscriber(topic, name, null, false);
   }

   public TopicSubscriber createDurableSubscriber(Topic topic, String name, String messageSelector, boolean noLocal)
         throws JMSException
   {
      checkClosed();
      if (this instanceof SpyQueueSession)
         throw new IllegalStateException("Not allowed for a QueueSession");
      if (topic == null)
         throw new InvalidDestinationException("Topic cannot be null");
      if (topic instanceof TemporaryTopic)
         throw new InvalidDestinationException("Attempt to create a durable subscription for a temporary topic");

      if (name == null || name.trim().length() == 0)
         throw new JMSException("Null or empty subscription");

      SpyTopic t = new SpyTopic((SpyTopic) topic, connection.getClientID(), name, messageSelector);
      SpyTopicSubscriber sub = new SpyTopicSubscriber(this, t, noLocal, messageSelector);
      addConsumer(sub);

      return sub;
   }

   public TopicSubscriber createSubscriber(Topic topic) throws JMSException
   {
      return createSubscriber(topic, null, false);
   }

   public TopicSubscriber createSubscriber(Topic topic, String messageSelector, boolean noLocal) throws JMSException
   {
      checkClosed();
      if (topic == null)
         throw new InvalidDestinationException("Topic cannot be null");

      connection.checkTemporary(topic);
      SpyTopicSubscriber sub = new SpyTopicSubscriber(this, (SpyTopic) topic, noLocal, messageSelector);
      addConsumer(sub);

      return sub;
   }

   public TopicPublisher createPublisher(Topic topic) throws JMSException
   {
      checkClosed();
      SpyTopicPublisher producer = new SpyTopicPublisher(this, topic);
      addProducer(producer);
      return producer;
   }

   public Queue createQueue(String queueName) throws JMSException
   {
      checkClosed();
      if (this instanceof SpyTopicSession)
         throw new IllegalStateException("Not allowed for a TopicSession");
      if (queueName == null)
         throw new InvalidDestinationException("Queue name cannot be null.");
      return ((SpyConnection) connection).createQueue(queueName);
   }

   public Topic createTopic(String topicName) throws JMSException
   {
      checkClosed();
      if (this instanceof SpyQueueSession)
         throw new IllegalStateException("Not allowed for a QueueSession");
      if (topicName == null)
         throw new InvalidDestinationException("The topic name cannot be null");

      return ((SpyConnection) connection).createTopic(topicName);
   }

   public TemporaryQueue createTemporaryQueue() throws JMSException
   {
      checkClosed();
      if (this instanceof SpyTopicSession)
         throw new IllegalStateException("Not allowed for a TopicSession");

      return ((SpyConnection) connection).getTemporaryQueue();
   }

   public TemporaryTopic createTemporaryTopic() throws JMSException
   {
      checkClosed();
      if (this instanceof SpyQueueSession)
         throw new IllegalStateException("Not allowed for a QueueSession");
      return ((SpyConnection) connection).getTemporaryTopic();
   }

   public void unsubscribe(String name) throws JMSException
   {
      checkClosed();
      if (this instanceof SpyQueueSession)
         throw new IllegalStateException("Not allowed for a QueueSession");

      // @todo Not yet implemented
      DurableSubscriptionID id = new DurableSubscriptionID(connection.getClientID(), name, null);
      connection.unsubscribe(id);
   }
   
   public XAResource getXAResource()
   {
      return spyXAResource;
   }
   
   public Session getSession() throws JMSException
   {
      checkClosed();
      return this;
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append("SpySession@").append(System.identityHashCode(this));
      buffer.append('[');
      buffer.append("tx=").append(transacted);
      if (transacted == false)
      {
         if (acknowledgeMode == AUTO_ACKNOWLEDGE)
            buffer.append(" ack=").append("AUTO");
         else if (acknowledgeMode == CLIENT_ACKNOWLEDGE)
            buffer.append(" ack=").append("CLIENT");
         else if (acknowledgeMode == DUPS_OK_ACKNOWLEDGE)
            buffer.append(" ack=").append("DUPSOK");
      }
      buffer.append(" txid=" + currentTransactionId);
      if (spyXAResource != null)
         buffer.append(" XA");
      if (running)
         buffer.append(" RUNNING");
      if (closed.get())
         buffer.append(" CLOSED");
      buffer.append(" connection=").append(connection);
      buffer.append(']');
      return buffer.toString();
   }

   /**
	 * Set the session's transaction id
	 * 
	 * @param xid the transaction id
	 */
   void setCurrentTransactionId(final Object xid)
   {
      if (xid == null)
         throw new org.jboss.util.NullArgumentException("xid");

      if (trace)
         log.trace("Setting current tx xid=" + xid + " previous: " + currentTransactionId + " " + this);

      this.currentTransactionId = xid;
   }

   /**
	 * Remove the session's transaction id
	 * 
	 * @param xid the transaction id
	 */
   void unsetCurrentTransactionId(final Object xid)
   {
      if (xid == null)
         throw new org.jboss.util.NullArgumentException("xid");

      if (trace)
         log.trace("Unsetting current tx  xid=" + xid + " previous: " + currentTransactionId + " " + this);

      // Don't unset the xid if it has previously been suspended
      // The session could have been recycled
      if (xid.equals(currentTransactionId))
         this.currentTransactionId = null;
   }

   /**
	 * Get the session's transaction id
	 * 
	 * @param xid the transaction id
	 */
   Object getCurrentTransactionId()
   {
      return currentTransactionId;
   }

   /**
	 * Get a new message
	 * 
	 * @return the new message id
	 * @throws JMSException for any error
	 */
   String getNewMessageID() throws JMSException
   {
      checkClosed();
      return connection.getNewMessageID();
   }

   /**
	 * Add a message tot the session
	 * 
	 * @param message the message
	 */
   void addMessage(SpyMessage message)
   {
      synchronized (messages)
      {
         if (trace)
            log.trace("Add message msgid=" + message.header.jmsMessageID + " " + this);
         messages.addLast(message);
      }
   }

   /**
	 * Add an unacknowledged message
	 * 
	 * @param message the message
	 */
   void addUnacknowlegedMessage(SpyMessage message)
   {
      if (!transacted)
      {
         synchronized (unacknowledgedMessages)
         {
            if (trace)
               log.trace("Add unacked message msgid=" + message.header.jmsMessageID + " " + this);

            unacknowledgedMessages.add(message);
         }
      }
   }

   /**
	 * Send a message
	 * 
	 * @param m the message
	 * @throws JMSException for any error
	 */
   void sendMessage(SpyMessage m) throws JMSException
   {
      checkClosed();

      // Make sure the message has the correct client id
      m.header.producerClientId = connection.getClientID();

      if (transacted)
      {
         if (trace)
            log.trace("Adding message to transaction " + m.header.jmsMessageID + " " + this);
         connection.spyXAResourceManager.addMessage(currentTransactionId, m.myClone());
      }
      else
      {
         if (trace)
            log.trace("Sending message to server " + m.header.jmsMessageID + " " + this);
         connection.sendToServer(m);
      }
   }

   /**
	 * Add a consumer
	 * 
	 * @param who the consumer
	 * @throws JMSException for any error
	 */
   void addConsumer(SpyMessageConsumer who) throws JMSException
   {
      checkClosed();

      synchronized (consumers)
      {
         if (trace)
            log.trace("Adding consumer " + who);

         consumers.add(who);
      }
      try
      {
         connection.addConsumer(who);
      }
      catch (JMSSecurityException ex)
      {
         removeConsumerInternal(who);
         throw ex;
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Error adding consumer", t);
      }
   }

   /**
	 * Remove a consumer
	 * 
	 * @param who the consumer
	 * @throws JMSException for any error
	 */
   void removeConsumer(SpyMessageConsumer who) throws JMSException
   {
      connection.removeConsumer(who);
      removeConsumerInternal(who);
   }

   /**
	 * Add a producer
	 * 
	 * @param who the producer
	 * @throws JMSException for any error
	 */
   void addProducer(SpyMessageProducer who) throws JMSException
   {
      checkClosed();

      synchronized (producers)
      {
         if (trace)
            log.trace("Adding producer " + who);

         producers.add(who);
      }
   }

   /**
	 * Remove a producer
	 * 
	 * @param who the producer
	 * @throws JMSException for any error
	 */
   void removeProducer(SpyMessageProducer who) throws JMSException
   {
      removeProducerInternal(who);
   }

   /**
    * Try to lock the session for asynchronous delivery
    * 
    * @return true when the lock was obtained
    */
   boolean tryDeliveryLock()
   {
      synchronized (deliveryLock)
      {
         if (inDelivery)
         {
            try
            {
               deliveryLock.wait();
            }
            catch (InterruptedException e)
            {
               log.trace("Ignored interruption waiting for delivery lock");
            }
         }
         // We got the lock
         if (inDelivery == false)
         {
            inDelivery = true;
            return true;
         }
      }
      return false;
   }

   /**
    * Release the delivery lock
    */
   void releaseDeliveryLock()
   {
      synchronized (deliveryLock)
      {
         inDelivery = false;
         deliveryLock.notifyAll();
      }
   }

   /**
    * Interrupt threads waiting for the delivery lock
    */
   void interruptDeliveryLockWaiters()
   {
      synchronized (deliveryLock)
      {
         deliveryLock.notifyAll();
      }
   }
   
   /**
    * Invoked to notify of asynchronous failure
    * 
    * @param message the message
    * @param t the throwable
    */
   void asynchFailure(String message, Throwable t)
   {
      connection.asynchFailure(message, t);
   }

   /**
	 * Rollback a transaction
	 * 
	 * @throws JMSException for any error
	 */
   private void internalRollback() throws JMSException
   {
      synchronized (runLock)
      {
         if (spyXAResource != null)
            throw new javax.jms.TransactionInProgressException("Should not be call from a XASession");
         if (!transacted)
            throw new IllegalStateException("The session is not transacted");

         if (trace)
            log.trace("Rollback transaction " + this);
         try
         {
            connection.spyXAResourceManager.endTx(currentTransactionId, true);
            connection.spyXAResourceManager.rollback(currentTransactionId);
         }
         catch (Throwable t)
         {
            SpyJMSException.rethrowAsJMSException("Could not rollback", t);
         }
         finally
         {
            unacknowledgedMessages.clear();
            try
            {
               currentTransactionId = connection.spyXAResourceManager.startTx();
               if (trace)
                  log.trace("Current transaction id: " + currentTransactionId + " " + this);
            }
            catch (Throwable ignore)
            {
               if (trace)
                  log.trace("Failed to start tx " + this, ignore);
            }
         }
      }
   }

   /**
	 * Remove a consumer
	 * 
	 * @param who the consumer
	 */
   private void removeConsumerInternal(SpyMessageConsumer who)
   {
      synchronized (consumers)
      {
         if (trace)
            log.trace("Remove consumer " + who);

         consumers.remove(who);
      }
   }

   /**
	 * Remove a producer
	 * 
	 * @param who the producer
	 */
   private void removeProducerInternal(SpyMessageProducer who)
   {
      synchronized (producers)
      {
         if (trace)
            log.trace("Remove producer " + who);

         producers.remove(who);
      }
   }
   
   /**
    * Check whether we are closed
    * 
    * @throws IllegalStateException when the session is closed
    */
   private void checkClosed() throws IllegalStateException
   {
      if (closed.get())
         throw new IllegalStateException("The session is closed");
   }
}
