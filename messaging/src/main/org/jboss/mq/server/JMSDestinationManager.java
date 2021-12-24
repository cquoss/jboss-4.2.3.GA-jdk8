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
package org.jboss.mq.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.jms.Destination;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;
import javax.transaction.xa.Xid;

import org.jboss.mq.AcknowledgementRequest;
import org.jboss.mq.ConnectionToken;
import org.jboss.mq.DurableSubscriptionID;
import org.jboss.mq.SpyDestination;
import org.jboss.mq.SpyJMSException;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.SpyQueue;
import org.jboss.mq.SpyTemporaryQueue;
import org.jboss.mq.SpyTemporaryTopic;
import org.jboss.mq.SpyTopic;
import org.jboss.mq.SpyTransactionRolledBackException;
import org.jboss.mq.Subscription;
import org.jboss.mq.TransactionRequest;
import org.jboss.mq.pm.PersistenceManager;
import org.jboss.mq.pm.Tx;
import org.jboss.mq.pm.TxManager;
import org.jboss.mq.sm.StateManager;
import org.jboss.util.threadpool.ThreadPool;
import org.jboss.util.timeout.TimeoutFactory;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;

/**
 * This class implements the JMS provider
 *
 * @author    Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author    Hiram Chirino (Cojonudo14@hotmail.com)
 * @author    David Maplesden (David.Maplesden@orion.co.nz)
 * @author <a href="mailto:pra@tim.se">Peter Antman</a>
 * @version   $Revision: 66791 $
 */
public class JMSDestinationManager extends JMSServerInterceptorSupport
{
   /** The version */
   public final static String JBOSS_VERSION = "JBossMQ Version 4.0";

   /** Destinations SpyDestination -> JMSDestination */
   public Map destinations = new ConcurrentReaderHashMap();

   /** Destinations being closed SpyDestination -> JMSDestination */
   public Map closingDestinations = new ConcurrentReaderHashMap();

   /** Thread pool */
   public ThreadPool threadPool;

   /** Thread group */
   public ThreadGroup threadGroup;

   /** Timeout factory */
   public TimeoutFactory timeoutFactory;

   /** The list of ClientConsumers hased by ConnectionTokens */
   Map clientConsumers = new ConcurrentReaderHashMap();

   /** last id given to a client */
   private int lastID = 1;

   /** last id given to a temporary topic */
   private int lastTemporaryTopic = 1;

   private Object lastTemporaryTopicLock = new Object();

   /** last id given to a temporary queue */
   private int lastTemporaryQueue = 1;

   private Object lastTemporaryQueueLock = new Object();
   
   /** The security manager */
   private StateManager stateManager;

   /** The persistence manager */
   private PersistenceManager persistenceManager;

   /** The Cache Used to hold messages */
   private MessageCache messageCache;

   /** Whether to do the redelivery update lazily */
   private boolean lazyRedeliveryUpdate;

   private Object stateLock = new Object();

   private Object idLock = new Object();

   /**
    * Because there can be a delay between killing the JMS service and the
    * service actually dying, this field is used to tell external classes that
    * that server has actually stopped.
    */
   private boolean stopped = true;

   /** Temporary queue/topic parameters */
   BasicQueueParameters parameters;

   /**
    * Constructor for the JMSServer object
    */
   public JMSDestinationManager(BasicQueueParameters parameters)
   {
      this.parameters = parameters;
   }

   /**
    * Sets the Enabled attribute of the JMSServer object
    *
    * @param dc                The new Enabled value
    * @param enabled           The new Enabled value
    * @exception JMSException  Description of Exception
    */
   public void setEnabled(ConnectionToken dc, boolean enabled) throws JMSException
   {
      ClientConsumer ClientConsumer = getClientConsumer(dc);
      ClientConsumer.setEnabled(enabled);
   }

   /**
    * Sets the StateManager attribute of the JMSServer object
    *
    * @param newStateManager  The new StateManager value
    */
   public void setStateManager(StateManager newStateManager)
   {
      stateManager = newStateManager;
   }

   /**
    * Sets the PersistenceManager attribute of the JMSServer object
    *
    * @param newPersistenceManager  The new PersistenceManager value
    */
   public void setPersistenceManager(org.jboss.mq.pm.PersistenceManager newPersistenceManager)
   {
      persistenceManager = newPersistenceManager;
   }

   /**
    * Get the lazyRedeliveryUpdate.
    * 
    * @return the lazyRedeliveryUpdate.
    */
   public boolean isLazyRedeliveryUpdate()
   {
      return lazyRedeliveryUpdate;
   }

   /**
    * Set the lazyRedeliveryUpdate.
    * 
    * @param lazyRedeliveryUpdate the lazyRedeliveryUpdate.
    */
   public void setLazyRedeliveryUpdate(boolean lazyRedeliveryUpdate)
   {
      this.lazyRedeliveryUpdate = lazyRedeliveryUpdate;
   }

   /**
    * Returns <code>false</code> if the JMS server is currently running and
    * handling requests, <code>true</code> otherwise.
    *
    * @return   <code>false</code> if the JMS server is currently running and
    *      handling requests, <code>true</code> otherwise.
    */
   public boolean isStopped()
   {
      synchronized (stateLock)
      {
         return this.stopped;
      }
   }

   protected void checkStopped() throws IllegalStateException
   {
      if (isStopped())
         throw new IllegalStateException("Server is stopped.");
   }
   
   /**
    *
    * @return the current client count
    */
   public int getClientCount()
   {
      return clientConsumers.size();
   }

   /** 
    * Obtain a copy of the current clients
    * 
    * @return a HashMap<ConnectionToken, ClientConsumer> of current clients
    */
   public HashMap getClients()
   {
      return new HashMap(clientConsumers);
   }

   public void setThreadPool(ThreadPool threadPool)
   {
      this.threadPool = threadPool;
   }

   public ThreadPool getThreadPool()
   {
      return threadPool;
   }

   public void setThreadGroup(ThreadGroup threadGroup)
   {
      this.threadGroup = threadGroup;
   }

   public ThreadGroup getThreadGroup()
   {
      return threadGroup;
   }

   public TimeoutFactory getTimeoutFactory()
   {
      return timeoutFactory;
   }

   /**
    * Gets the ID attribute of the JMSServer object
    *
    * @return   The ID value
    */
   public String getID()
   {
      String ID = null;

      while (isStopped() == false)
      {
         if (stateManager == null)
            throw new IllegalStateException("No statemanager");
         try
         {
            synchronized (idLock)
            {
               ID = "ID:" + (new Integer(lastID++).toString());
            }
            stateManager.addLoggedOnClientId(ID);
            break;
         }
         catch (Exception e)
         {
         }
      }

      checkStopped();
      
      return ID;
   }

   public TemporaryTopic getTemporaryTopic(ConnectionToken dc) throws JMSException
   {
      checkStopped();
      
      String topicName;
      synchronized (lastTemporaryTopicLock)
      {
         topicName = "JMS_TT" + (new Integer(lastTemporaryTopic++).toString());
      }
      SpyTemporaryTopic topic = new SpyTemporaryTopic(topicName, dc);

      ClientConsumer ClientConsumer = getClientConsumer(dc);
      JMSDestination queue = new JMSTopic(topic, ClientConsumer, this, parameters);
      destinations.put(topic, queue);

      return topic;
   }

   public TemporaryQueue getTemporaryQueue(ConnectionToken dc) throws JMSException
   {
      checkStopped();
      
      String queueName;
      synchronized (lastTemporaryQueueLock)
      {
         queueName = "JMS_TQ" + (new Integer(lastTemporaryQueue++).toString());
      }
      SpyTemporaryQueue newQueue = new SpyTemporaryQueue(queueName, dc);

      ClientConsumer ClientConsumer = getClientConsumer(dc);
      JMSDestination queue = new JMSQueue(newQueue, ClientConsumer, this, parameters);
      destinations.put(newQueue, queue);

      return newQueue;
   }

   public ClientConsumer getClientConsumer(ConnectionToken dc) throws JMSException
   {
      ClientConsumer cq = (ClientConsumer) clientConsumers.get(dc);
      if (cq == null)
      {
         cq = new ClientConsumer(this, dc);
         clientConsumers.put(dc, cq);
      }
      return cq;
   }

   public JMSDestination getJMSDestination(SpyDestination dest)
   {
      return (JMSDestination) destinations.get(dest);
   }

   /**
    * Gets the JMSDestination attribute of the JMSServer object
    * which might be being closed 
    *
    * @param dest  Description of Parameter
    * @return      The JMSDestination value
    */
   protected JMSDestination getPossiblyClosingJMSDestination(SpyDestination dest)
   {
      JMSDestination result = (JMSDestination) destinations.get(dest);
      if (result == null)
         result = (JMSDestination) closingDestinations.get(dest);
      return result;
   }

   /**
    * Gets the StateManager attribute of the JMSServer object
    *
    * @return   The StateManager value
    */
   public StateManager getStateManager()
   {
      return stateManager;
   }

   /**
    * Gets the PersistenceManager attribute of the JMSServer object
    *
    * @return   The PersistenceManager value
    */
   public PersistenceManager getPersistenceManager()
   {
      return persistenceManager;
   }

   /**
    * Start the server
    */
   public void startServer()
   {
      synchronized (stateLock)
      {
         this.stopped = false;
         this.timeoutFactory = new TimeoutFactory(this.threadPool);
      }
   }

   /**
    * Stop the server
    */
   public void stopServer()
   {
      synchronized (stateLock)
      {
         this.stopped = true;
         this.timeoutFactory.cancel();
         
         for (Iterator i = clientConsumers.keySet().iterator(); i.hasNext();)
         {
            ConnectionToken token = (ConnectionToken) i.next();
            try
            {
               connectionClosing(token);
            }
            catch (Throwable t)
            {
               log.trace("Ignored error closing client connection " + token, t);
            }
         }
      }
   }

   public void checkID(String ID) throws JMSException
   {
      checkStopped();
      stateManager.addLoggedOnClientId(ID);
   }

   public void addMessage(ConnectionToken dc, SpyMessage val) throws JMSException
   {
      addMessage(dc, val, null);
   }

   public void addMessage(ConnectionToken dc, SpyMessage val, Tx txId) throws JMSException
   {
      checkStopped();
      JMSDestination queue = (JMSDestination) destinations.get(val.getJMSDestination());
      if (queue == null)
         throw new InvalidDestinationException("This destination does not exist! " + val.getJMSDestination());

      // Reset any redelivered information
      val.setJMSRedelivered(false);
      val.header.jmsProperties.remove(SpyMessage.PROPERTY_REDELIVERY_COUNT);
      val.header.jmsProperties.remove(SpyMessage.PROPERTY_DELIVERY_COUNT);

      //Add the message to the queue
      val.setReadOnlyMode();
      queue.addMessage(val, txId);
   }

   public void transact(ConnectionToken dc, TransactionRequest t) throws JMSException
   {
      checkStopped();
      boolean trace = log.isTraceEnabled();
      TxManager txManager = persistenceManager.getTxManager();
      if (t.requestType == TransactionRequest.ONE_PHASE_COMMIT_REQUEST)
      {
         Tx txId = txManager.createTx();
         if (trace)
            log.trace(dc + " 1PC " + t.xid + " txId=" + txId.longValue());
         try
         {
            if (t.messages != null)
            {
               for (int i = 0; i < t.messages.length; i++)
               {
                  addMessage(dc, t.messages[i], txId);
               }
            }
            if (t.acks != null)
            {
               for (int i = 0; i < t.acks.length; i++)
               {
                  acknowledge(dc, t.acks[i], txId);
               }
            }
            txManager.commitTx(txId);
         }
         catch (JMSException e)
         {
            log.debug("Exception occured, rolling back transaction: ", e);
            txManager.rollbackTx(txId);
            throw new SpyTransactionRolledBackException("Transaction was rolled back.", e);
         }
      }
      else if (t.requestType == TransactionRequest.TWO_PHASE_COMMIT_PREPARE_REQUEST)
      {
         Tx txId = txManager.createTx(dc, t.xid);
         if (trace)
            log.trace(dc + " 2PC PREPARE " + t.xid + " txId=" + txId.longValue());
         try
         {
            if (t.messages != null)
            {
               for (int i = 0; i < t.messages.length; i++)
               {
                  addMessage(dc, t.messages[i], txId);
               }
            }
            if (t.acks != null)
            {
               for (int i = 0; i < t.acks.length; i++)
               {
                  acknowledge(dc, t.acks[i], txId);
               }
            }
            
            txManager.markPrepared(dc, t.xid, txId);
         }
         catch (JMSException e)
         {
            log.debug("Exception occured, rolling back transaction: ", e);
            txManager.rollbackTx(txId);
            throw new SpyTransactionRolledBackException("Transaction was rolled back.", e);
         }
      }
      else if (t.requestType == TransactionRequest.TWO_PHASE_COMMIT_ROLLBACK_REQUEST)
      {
         if (trace)
            log.trace(dc + " 2PC ROLLBACK " + t.xid);
         txManager.rollbackTx(dc, t.xid);
      }
      else if (t.requestType == TransactionRequest.TWO_PHASE_COMMIT_COMMIT_REQUEST)
      {
         if (trace)
            log.trace(dc + " 2PC COMMIT " + t.xid);
         txManager.commitTx(dc, t.xid);
      }
   }

   public Xid[] recover(ConnectionToken dc, int flags) throws Exception
   {
      checkStopped();
      TxManager txManager = persistenceManager.getTxManager();
      return txManager.recover(dc, flags);
   }

   public void acknowledge(ConnectionToken dc, AcknowledgementRequest item) throws JMSException
   {
      acknowledge(dc, item, null);
   }

   public void acknowledge(ConnectionToken dc, AcknowledgementRequest item, Tx txId) throws JMSException
   {
      checkStopped();
      ClientConsumer cc = getClientConsumer(dc);
      cc.acknowledge(item, txId);
   }

   public void connectionClosing(ConnectionToken dc) throws JMSException
   {
      if (dc == null)
         return;

      // Close it's ClientConsumer
      ClientConsumer cq = (ClientConsumer) clientConsumers.remove(dc);
      if (cq != null)
         cq.close();

      //unregister its clientID
      if (dc.getClientID() != null)
         stateManager.removeLoggedOnClientId(dc.getClientID());

      //Remove any temporary destinations the consumer may have created.
      Iterator i = destinations.entrySet().iterator();
      while (i.hasNext())
      {
         Map.Entry entry = (Map.Entry) i.next();
         JMSDestination sq = (JMSDestination) entry.getValue();
         if (sq != null)
         {
            ClientConsumer cc = sq.temporaryDestination;
            if (cc != null && dc.equals(cc.connectionToken))
            {
               i.remove();
               deleteTemporaryDestination(dc, sq);
            }
         }
      }
      // Close the clientIL
      try
      {
         if (dc.clientIL != null)
            dc.clientIL.close();
      }
      catch (Exception ex)
      {
         // We skip warning, to often the client will always
         // have gone when we get here
         //log.warn("Could not close clientIL: " +ex,ex);
      }
   }

   public void connectionFailure(ConnectionToken dc) throws JMSException
   {
      //We should try again :) This behavior should under control of a Failure-Plugin
      log.error("The connection to client " + dc.getClientID() + " failed.");
      connectionClosing(dc);
   }

   public void subscribe(ConnectionToken dc, Subscription sub) throws JMSException
   {
      checkStopped();
      ClientConsumer clientConsumer = getClientConsumer(dc);
      clientConsumer.addSubscription(sub);
   }

   public void unsubscribe(ConnectionToken dc, int subscriptionId) throws JMSException
   {
      checkStopped();
      ClientConsumer clientConsumer = getClientConsumer(dc);
      clientConsumer.removeSubscription(subscriptionId);
   }

   public void destroySubscription(ConnectionToken dc, DurableSubscriptionID id) throws JMSException
   {
      checkStopped();
      getStateManager().setDurableSubscription(this, id, null);
   }

   public SpyMessage[] browse(ConnectionToken dc, Destination dest, String selector) throws JMSException
   {
      checkStopped();
      JMSDestination queue = (JMSDestination) destinations.get(dest);
      if (queue == null)
         throw new InvalidDestinationException("That destination does not exist! " + dest);
      if (!(queue instanceof JMSQueue))
         throw new JMSException("That destination is not a queue");

      return ((JMSQueue) queue).browse(selector);
   }

   public SpyMessage receive(ConnectionToken dc, int subscriberId, long wait) throws JMSException
   {
      checkStopped();
      ClientConsumer clientConsumer = getClientConsumer(dc);
      SpyMessage msg = clientConsumer.receive(subscriberId, wait);
      return msg;
   }

   public Queue createQueue(ConnectionToken dc, String name) throws JMSException
   {
      checkStopped();
      SpyQueue newQueue = new SpyQueue(name);
      if (!destinations.containsKey(newQueue))
         throw new JMSException("This destination does not exist !" + newQueue);
      return newQueue;
   }

   public Topic createTopic(ConnectionToken dc, String name) throws JMSException
   {
      checkStopped();
      SpyTopic newTopic = new SpyTopic(name);
      if (!destinations.containsKey(newTopic))
         throw new JMSException("This destination does not exist !" + newTopic);
      return newTopic;
   }
   
   public Queue createQueue(String queueName) throws JMSException
   {
      checkStopped();
      
      SpyTemporaryQueue newQueue = new SpyTemporaryQueue(queueName, null);

      JMSDestination queue = new JMSQueue(newQueue, null, this, parameters);
      destinations.put(newQueue, queue);

      return newQueue;
   }
   
   public Topic createTopic(String topicName) throws JMSException
   {
      checkStopped();
      
      SpyTemporaryTopic topic = new SpyTemporaryTopic(topicName, null);

      JMSDestination queue = new JMSTopic(topic, null, this, parameters);
      destinations.put(topic, queue);

      return topic;
   }

   public void deleteTemporaryDestination(ConnectionToken dc, SpyDestination dest) throws JMSException
   {
      checkStopped();
      JMSDestination destination = (JMSDestination) destinations.get(dest);
      if (destination == null)
         throw new InvalidDestinationException("That destination does not exist! " + destination);

      if (destination.isInUse())
         throw new JMSException("Cannot delete temporary queue, it is in use.");

      destinations.remove(dest);
      deleteTemporaryDestination(dc, destination);
   }

   protected void deleteTemporaryDestination(ConnectionToken dc, JMSDestination destination) throws JMSException
   {
      try
      {
         destination.removeAllMessages();
      }
      catch (Exception e)
      {
         log.error("An exception happened while removing all messages from temporary destination "
               + destination.getSpyDestination().getName(), e);
      }

   }

   public String checkUser(String userName, String password) throws JMSException
   {
      checkStopped();
      return stateManager.checkUser(userName, password);
   }

   public String authenticate(String id, String password) throws JMSException
   {
      checkStopped();
      // do nothing
      return null;
   }

   public void addDestination(JMSDestination destination) throws JMSException
   {
      if (destinations.containsKey(destination.getSpyDestination()))
         throw new JMSException("This destination has already been added to the server!");

      //Add this new destination to the list
      destinations.put(destination.getSpyDestination(), destination);

      // Restore the messages
      if (destination instanceof JMSTopic)
      {
         Collection durableSubs = getStateManager().getDurableSubscriptionIdsForTopic((SpyTopic) destination.getSpyDestination());
         for (Iterator i = durableSubs.iterator(); i.hasNext();)
         {
            DurableSubscriptionID sub = (DurableSubscriptionID) i.next();
            log.debug("creating the durable subscription for :" + sub);
            ((JMSTopic) destination).createDurableSubscription(sub);
         }
      }
   }

   /**
    * Closed a destination that was opened previously
    *
    * @param dest              the destionation to close
    * @exception JMSException  Description of Exception
    */
   public void closeDestination(SpyDestination dest) throws JMSException
   {
      JMSDestination destination = (JMSDestination) destinations.remove(dest);
      if (destination == null)
         throw new InvalidDestinationException("This destination is not open! " + dest);

      log.debug("Closing destination " + dest);

      // Add it to the closing list
      closingDestinations.put(dest, destination);
      try
      {
         destination.close();
      }
      finally
      {
         closingDestinations.remove(dest);
      }
   }

   public String toString()
   {
      return JBOSS_VERSION;
   }

   public void ping(ConnectionToken dc, long clientTime) throws JMSException
   {
      checkStopped();
      try
      {
         dc.clientIL.pong(System.currentTimeMillis());
      }
      catch (Exception e)
      {
         throw new SpyJMSException("Could not pong", e);
      }
   }

   /**
    * Gets the messageCache
    * @return Returns a MessageCache
    */
   public MessageCache getMessageCache()
   {
      return messageCache;
   }

   /**
    * Sets the messageCache
    * @param messageCache The messageCache to set
    */
   public void setMessageCache(MessageCache messageCache)
   {
      this.messageCache = messageCache;
   }

   public SpyTopic getDurableTopic(DurableSubscriptionID sub) throws JMSException
   {
      checkStopped();
      return getStateManager().getDurableTopic(sub);
   }

   public Subscription getSubscription(ConnectionToken dc, int subscriberId) throws JMSException
   {
      checkStopped();
      ClientConsumer clientConsumer = getClientConsumer(dc);
      return clientConsumer.getSubscription(subscriberId);
   }

   /**
    * Gets message counters of all configured destinations
    *
    * @return MessageCounter[]      message counter array sorted by name
    */
   public MessageCounter[] getMessageCounter()
   {
      TreeMap map = new TreeMap(); // for sorting

      Iterator i = destinations.values().iterator();

      while (i.hasNext())
      {
         JMSDestination dest = (JMSDestination) i.next();

         MessageCounter[] counter = dest.getMessageCounter();

         for (int j = 0; j < counter.length; j++)
         {
            // sorting order name + subscription + type
            String key = counter[j].getDestinationName() + "-" + counter[j].getDestinationSubscription() + "-"
                  + (counter[j].getDestinationTopic() ? "Topic" : "Queue");

            map.put(key, counter[j]);
         }
      }

      return (MessageCounter[]) map.values().toArray(new MessageCounter[0]);
   }

   /**
    * Resets message counters of all configured destinations
    */
   public void resetMessageCounter()
   {
      Iterator i = destinations.values().iterator();

      while (i.hasNext())
      {
         JMSDestination dest = (JMSDestination) i.next();

         MessageCounter[] counter = dest.getMessageCounter();

         for (int j = 0; j < counter.length; j++)
         {
            counter[j].resetCounter();
         }
      }
   }

   public BasicQueueParameters getParameters()
   {
      return parameters;
   }
}
