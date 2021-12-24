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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;

import org.jboss.logging.Logger;
import org.jboss.mq.AcknowledgementRequest;
import org.jboss.mq.DestinationFullException;
import org.jboss.mq.SpyDestination;
import org.jboss.mq.SpyJMSException;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.Subscription;
import org.jboss.mq.pm.Tx;
import org.jboss.mq.pm.TxManager;
import org.jboss.mq.selectors.Selector;
import org.jboss.util.NestedRuntimeException;
import org.jboss.util.timeout.Timeout;
import org.jboss.util.timeout.TimeoutTarget;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;
import EDU.oswego.cs.dl.util.concurrent.CopyOnWriteArraySet;

/**
 *  This class represents a queue which provides it's messages exclusively to one
 *  consumer at a time.<p>
 *
 * Notes about synchronization: Much of the work is synchronized on
 * the receivers or messages depending on the work performed.
 * However, anything to do with unacknowledged messages and removed
 * subscriptions must be done synchronized on both (receivers first).
 * This is because there are multiple entry points with the possibility
 * that a message acknowledgement (or NACK) is being processed at
 * the same time as a network failure removes the subscription.
 *
 *
 * @author     Hiram Chirino (Cojonudo14@hotmail.com)
 * @author     Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author     David Maplesden (David.Maplesden@orion.co.nz)
 * @author     Adrian Brock (Adrian@jboss.org)
 * @created    August 16, 2001
 * @version    $Revision: 69770 $
 */
public class BasicQueue
{
   static final Logger log = Logger.getLogger(BasicQueue.class);

   /** List of messages waiting to be dispatched<p>
       synchronized access on itself */
   SortedSet messages = new TreeSet();

   /** Events by message id */
   ConcurrentHashMap events = new ConcurrentHashMap();
   
   /** The scheduled messages */
   CopyOnWriteArraySet scheduledMessages = new CopyOnWriteArraySet();

   /** The JMSServer object */
   JMSDestinationManager server;

   /** The subscribers waiting for messages - synchronized access on itself */
   Receivers receivers;

   /** The description used to seperate persistence for multiple subscriptions to a topic */
   String description;

   /** Simple Counter for gathering message add statistic data */
   MessageCounter counter;

   /** Unacknowledged messages AcknowledgementRequest -> UnackedMessageInfo<p>
       synchronized access on receivers and messages */
   HashMap unacknowledgedMessages = new HashMap();
   /** Unacknowledged messages MessageRef -> UnackedMessageInfo <p>
       synchronized access on receivers and messages */
   HashMap unackedByMessageRef = new HashMap();
   /** Unacknowledged messages Subscription -> UnackedMessageInfo <p>
       synchronized access on receivers and messages */
   HashMap unackedBySubscription = new HashMap();

   /** Subscribers <p>
    synchronized access on receivers */
   HashSet subscribers = new HashSet();
   
   /** Removed subscribers <p>
       synchronized access on receivers and messages */
   HashSet removedSubscribers = new HashSet();
   
   /** The basic queue parameters */
   BasicQueueParameters parameters;

   /** Have we been stopped */
   boolean stopped = false;

   /**
    * Construct a new basic queue
    * 
    * @param server the destination manager
    * @param description a description to uniquely identify the queue
    * @param parameters the basic queue parameters
    * @throws JMSException for any error
    */
   public BasicQueue(JMSDestinationManager server, String description, BasicQueueParameters parameters)
      throws JMSException
   {
      this.server = server;
      this.description = description;
      this.parameters = parameters;
      
      Class receiversImpl = parameters.receiversImpl;
      if (receiversImpl == null)
         receiversImpl = ReceiversImpl.class;
      
      try
      {
         receivers = (Receivers) receiversImpl.newInstance();
      }
      catch (Throwable t)
      {
         throw new SpyJMSException("Error instantiating receivers implementation: " + receiversImpl, t);
      }
   }

   /**
    * Retrieve the unique description for this queue
    *
    * @return the description
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * Retrieve the number of receivers waiting for a message
    *
    * @return the number of receivers
    */
   public int getReceiversCount()
   {
      return receivers.size();
   }

   /**
    * Retrieve the receivers waiting for a message
    *
    * @return an array of subscriptions
    */
   public ArrayList getReceivers()
   {
      synchronized (receivers)
      {
         return receivers.listReceivers();
      }
   }

   /**
    * Test whether the queue is in use
    *
    * @return true when there are subscribers
    */
   public boolean isInUse()
   {
      synchronized (receivers)
      {
         // In use if we have subscribers or there are unacknowledged messages
         return (subscribers.isEmpty() == false || getInProcessMessageCount() > 0);
      }
   }

   /**
    * Add a receiver to the queue
    *
    * @param sub the subscription to add
    * @throws JMSException for any error
    */
   public void addReceiver(Subscription sub) throws JMSException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("addReceiver " + sub + " " + this);
      
      synchronized(receivers)
      {
         MessageReference found = null;
         synchronized (messages)
         {
            if (messages.size() != 0)
            {
               for (Iterator it = messages.iterator(); it.hasNext();)
               {
                  MessageReference message = (MessageReference) it.next();
                  try
                  {
                     if (message.isExpired())
                     {
                        it.remove();
                        expireMessageAsync(message);
                     }
                     else if (sub.accepts(message.getHeaders()))
                     {
                        //queue message for sending to this sub
                        it.remove();
                        found = message;
                        break;
                     }
                  }
                  catch (JMSException ignore)
                  {
                     log.info("Caught unusual exception in addToReceivers.", ignore);
                  }
               }
            }
            if (found != null)
               setupMessageAcknowledgement(sub, found);
         }
         if (found != null)
         {
            updateRedeliveryFlags(found);
            queueMessageForSending(sub, found);
         }
         else
            addToReceivers(sub);
      }
   }

   /**
    * Get the subscribers
    * 
    * @return the subscribers
    */
   public Set getSubscribers()
   {
      synchronized (receivers)
      {
         return (Set) subscribers.clone();
      }
   }
   
   /**
    * Add a subscription from the queue
    *
    * @param sub the subscription to add
    * @throws JMSException for any error
    */
   public void addSubscriber(Subscription sub) throws JMSException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("addSubscriber " + sub + " " + this);
      synchronized (receivers)
      {
         if (stopped)
            throw new IllegalStateException("The destination is stopped " + getDescription());
         subscribers.add(sub);
      }
   }
      
   /**
    * Removes a subscription from the queue
    *
    * @param sub the subscription to remove
    */
   public void removeSubscriber(Subscription sub)
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("removeSubscriber " + sub + " " + this);
      synchronized (receivers)
      {
         removeReceiver(sub);
         synchronized (messages)
         {
            if (hasUnackedMessages(sub))
            {
               if (trace)
                  log.trace("Delaying removal of subscriber is has unacked messages " + sub);
               removedSubscribers.add(sub);
            }
            else
            {
               if (trace)
                  log.trace("Removing subscriber " + sub);
               subscribers.remove(sub);
               ((ClientConsumer) sub.clientConsumer).removeRemovedSubscription(sub.subscriptionId);
            }
         }
      }
   }

   /**
    * Retrieve the queue depth
    *
    * @return the number of messages in the queue
    */
   public int getQueueDepth()
   {
      return messages.size();
   }

   /**
    * Returns the number of scheduled messages in the queue
    * 
    * @return the scheduled message count
    */
   public int getScheduledMessageCount()
   {
      return scheduledMessages.size();
   }

   /**
    * Returns the number of in process messages for the queue
    * 
    * @return the in process count
    */
   public int getInProcessMessageCount()
   {
      synchronized (messages)
      {
         return unacknowledgedMessages.size();
      }
   }
   
   /**
    * Add a message to the queue
    *
    * @param mes the message reference
    * @param txId the transaction
    * @throws JMSException for any error
    */
   public void addMessage(MessageReference mes, Tx txId) throws JMSException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("addMessage " + mes + " " + txId + " " + this);

      try
      {
         synchronized (receivers)
         {
            if (stopped)
               throw new IllegalStateException("The destination is stopped " + getDescription());
         }
         
         if (parameters.maxDepth > 0)
         {
            synchronized (messages)
            {
               if (messages.size() >= parameters.maxDepth)
               {
                  dropMessage(mes);
                  String message = "Maximum size " + parameters.maxDepth +
                     " exceeded for " + description;
                  log.warn(message);
                  throw new DestinationFullException(message);
               }
            }
         }

         performOrPrepareAddMessage(mes, txId);
      }
      catch (Throwable t)
      {
         String error = "Error in addMessage " + mes;
         log.trace(error, t);
         dropMessage(mes, txId);
         SpyJMSException.rethrowAsJMSException(error, t);
      }
   }

   /**
    * Either perform or prepare the add message 
    * 
    * @param mes the message reference
    * @param txId the transaction id
    * @throws Exception for any error
    */
   protected void performOrPrepareAddMessage(MessageReference mes, Tx txId) throws Exception
   {
      TxManager txManager = server.getPersistenceManager().getTxManager();
      
      // The message is removed from the cache on a rollback
      Runnable task = new AddMessagePostRollBackTask(mes);
      txManager.addPostRollbackTask(txId, task);

      // The message gets added to the queue after the transaction commits
      task = new AddMessagePostCommitTask(mes);
      txManager.addPostCommitTask(txId, task);
   }
   
   /**
    * Restores a message.
    * 
    * @param mes the message reference
    */
   public void restoreMessage(MessageReference mes)
   {
      restoreMessage(mes, null, Tx.UNKNOWN);
   }

   /**
    * Restores a message.
    * 
    * @param mes the message reference
    * @param txid the transaction id
    * @param type the type of restoration
    */
   public void restoreMessage(MessageReference mes, Tx txid, int type)
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("restoreMessage " + mes + " " + this + " txid=" + txid + " type=" + type);

      try
      {
         if (txid == null)
         {
            internalAddMessage(mes);
         }
         else if (type == Tx.ADD)
         {
            performOrPrepareAddMessage(mes, txid);
         }
         else if (type == Tx.REMOVE)
         {
            performOrPrepareAcknowledgeMessage(mes, txid);
         }
         else
         {
            throw new IllegalStateException("Unknown restore type " + type + " for message " + mes + " txid=" + txid);
         }
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException("Unable to restore message " + mes, e);
      }
   }

   /**
    * Nacks a message.
    */
   protected void nackMessage(MessageReference message)
   {
      if (log.isTraceEnabled())
         log.trace("Restoring message: " + message);

      try
      {
         // Set redelivered, vendor-specific flags
         message.redelivered();
         // Old style update - doesn't survive a crash
         if (server.isLazyRedeliveryUpdate())
         {
            message.invalidate();
            // Update the persistent message outside the transaction
            // We want to know the message might have been delivered regardless
            if (message.isPersistent())
               server.getPersistenceManager().update(message, null);
         }
      }
      catch (JMSException e)
      {
         log.error("Caught unusual exception in nackMessage for " + message, e);
      }

      internalAddMessage(message);
   }

   /**
    * Browse the queue
    *
    * @param selector the selector to apply, pass null for
    *                 all messages
    * @return the messages
    * @throws JMSException for any error
    */
   public SpyMessage[] browse(String selector) throws JMSException
   {
      if (selector == null)
      {
         SpyMessage list[];
         synchronized (messages)
         {
            list = new SpyMessage[messages.size()];
            Iterator iter = messages.iterator();
            for (int i = 0; iter.hasNext(); i++)
               list[i] = ((MessageReference) iter.next()).getMessageForDelivery();
         }
         return list;
      }
      else
      {
         Selector s = new Selector(selector);
         LinkedList selection = new LinkedList();

         synchronized (messages)
         {
            Iterator i = messages.iterator();
            while (i.hasNext())
            {
               MessageReference m = (MessageReference) i.next();
               if (s.test(m.getHeaders()))
                  selection.add(m.getMessageForDelivery());
            }
         }

         SpyMessage list[];
         list = new SpyMessage[selection.size()];
         list = (SpyMessage[]) selection.toArray(list);
         return list;
      }
   }

   /**
    * Browse the scheduled messages
    *
    * @param selector the selector to apply, pass null for
    *                 all messages
    * @return the messages
    * @throws JMSException for any error
    */
   public List browseScheduled(String selector) throws JMSException
   {
      if (selector == null)
      {
         ArrayList list;
         synchronized (messages)
         {
            list = new ArrayList(scheduledMessages.size());
            Iterator iter = scheduledMessages.iterator();
            while (iter.hasNext())
            {
               MessageReference ref = (MessageReference) iter.next();
               list.add(ref.getMessageForDelivery());
            }
         }
         return list;
      }
      else
      {
         Selector s = new Selector(selector);
         LinkedList selection = new LinkedList();

         synchronized (messages)
         {
            Iterator iter = scheduledMessages.iterator();
            while (iter.hasNext())
            {
               MessageReference ref = (MessageReference) iter.next();
               if (s.test(ref.getHeaders()))
                  selection.add(ref.getMessageForDelivery());
            }
         }
         
         return selection;
      }
   }

   /**
    * Browse the in process messages
    *
    * @param selector the selector to apply, pass null for
    *                 all messages
    * @return the messages
    * @throws JMSException for any error
    */
   public List browseInProcess(String selector) throws JMSException
   {
      if (selector == null)
      {
         ArrayList list;
         synchronized (messages)
         {
            list = new ArrayList(unacknowledgedMessages.size());
            Iterator iter = unacknowledgedMessages.values().iterator();
            while (iter.hasNext())
            {
               UnackedMessageInfo unacked = (UnackedMessageInfo) iter.next();
               MessageReference ref = unacked.messageRef;
               list.add(ref.getMessageForDelivery());
            }
         }
         return list;
      }
      else
      {
         Selector s = new Selector(selector);
         LinkedList selection = new LinkedList();

         synchronized (messages)
         {
            Iterator iter = unacknowledgedMessages.values().iterator();
            while (iter.hasNext())
            {
               UnackedMessageInfo unacked = (UnackedMessageInfo) iter.next();
               MessageReference ref = unacked.messageRef;
               if (s.test(ref.getHeaders()))
                  selection.add(ref.getMessageForDelivery());
            }
         }
         
         return selection;
      }
   }

   /**
    * Receive a message from the queue
    *
    * @param sub the subscription requiring a message
    * @param wait whether to wait for a message
    * @return the message
    * @throws JMSException for any error
    */
   public SpyMessage receive(Subscription sub, boolean wait) throws JMSException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("receive " + sub + " wait=" + wait + " " + this);

      MessageReference messageRef = null;
      synchronized (receivers)
      {
         if (stopped)
            throw new IllegalStateException("The destination is stopped " + getDescription());
         // If the subscription is not picky, the first message will be it
         if (sub.getSelector() == null && sub.noLocal == false)
         {
            synchronized (messages)
            {
               // find a non-expired message
               while (messages.size() != 0)
               {
                  messageRef = (MessageReference) messages.first();
                  messages.remove(messageRef);

                  if (messageRef.isExpired())
                  {
                     expireMessageAsync(messageRef);
                     messageRef = null;
                  }
                  else
                     break;
               }
               
               if (messageRef != null)
                  setupMessageAcknowledgement(sub, messageRef);
            }
         }
         else
         {
            // The subscription is picky, so we have to iterate.
            synchronized (messages)
            {
               Iterator i = messages.iterator();
               while (i.hasNext())
               {
                  MessageReference mr = (MessageReference) i.next();
                  if (mr.isExpired())
                  {
                     i.remove();
                     expireMessageAsync(mr);
                  }
                  else if (sub.accepts(mr.getHeaders()))
                  {
                     messageRef = mr;
                     i.remove();
                     break;
                  }
               }
               
               if (messageRef != null)
                  setupMessageAcknowledgement(sub, messageRef);
            }
         }

         if (messageRef == null)
         {
            if (wait)
               addToReceivers(sub);
         }
      }

      if (messageRef == null)
         return null;
      updateRedeliveryFlags(messageRef);
      return messageRef.getMessageForDelivery();
   }

   /**
    * Acknowledge a message
    *
    * @param item the acknowledgement request
    * @param txId the transaction
    * @throws JMSException for any error
    */
   public void acknowledge(AcknowledgementRequest item, Tx txId) throws JMSException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("acknowledge " + item + " " + txId + " " + this);

      UnackedMessageInfo unacked = null;
      synchronized (messages)
      {
         unacked = (UnackedMessageInfo) unacknowledgedMessages.remove(item);
         if (unacked == null)
            return;
         unackedByMessageRef.remove(unacked.messageRef);
         HashMap map = (HashMap) unackedBySubscription.get(unacked.sub);
         if (map != null)
            map.remove(unacked.messageRef);
         if (map == null || map.isEmpty())
           unackedBySubscription.remove(unacked.sub);
      }

      MessageReference m = unacked.messageRef;

      // Was it a negative acknowledge??
      if (!item.isAck)
      {
         Runnable task = new RestoreMessageTask(m);
         server.getPersistenceManager().getTxManager().addPostCommitTask(txId, task);
         server.getPersistenceManager().getTxManager().addPostRollbackTask(txId, task);
      }
      else
      {
         try
         {
            if (m.isPersistent())
               server.getPersistenceManager().remove(m, txId);
         }
         catch (Throwable t)
         {
            // Something is wrong with the persistence manager,
            // force a NACK with a rollback/error
            Runnable task = new RestoreMessageTask(m);
            server.getPersistenceManager().getTxManager().addPostCommitTask(txId, task);
            server.getPersistenceManager().getTxManager().addPostRollbackTask(txId, task);
            SpyJMSException.rethrowAsJMSException("Error during ACK ref=" + m, t);
         }

         performOrPrepareAcknowledgeMessage(m, txId);
      }

      synchronized (receivers)
      {
         synchronized (messages)
         {
            checkRemovedSubscribers(unacked.sub);
         }
      }
   }

   /**
    * Either perform or prepare the acknowledge message 
    * 
    * @param mes the message reference
    * @param txId the transaction id
    * @throws Exception for any error
    */
   protected void performOrPrepareAcknowledgeMessage(MessageReference mes, Tx txId) throws JMSException
   {
      TxManager txManager = server.getPersistenceManager().getTxManager();
      
      // The message is restored to the queue on a rollback
      Runnable task = new RestoreMessageTask(mes);
      txManager.addPostRollbackTask(txId, task);

      // The message is fully removed after the transaction commits
      task = new RemoveMessageTask(mes);
      txManager.addPostCommitTask(txId, task);
   }

   /**
    * Nack all messages for a subscription
    *
    * @param sub the subscription
    */
   public void nackMessages(Subscription sub)
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("nackMessages " + sub + " " + this);

      // Send nacks for unacknowledged messages
      synchronized (receivers)
      {
         synchronized (messages)
         {
            int count = 0;
            HashMap map = (HashMap) unackedBySubscription.get(sub);
            if (map != null)
            {
               Iterator i = ((HashMap) map.clone()).values().iterator();
               while (i.hasNext())
               {
                  AcknowledgementRequest item = (AcknowledgementRequest) i.next();
                  try
                  {
                     acknowledge(item, null);
                     count++;
                  }
                  catch (JMSException ignore)
                  {
                     log.debug("Unable to nack message: " + item, ignore);
                  }
               }
               if (log.isDebugEnabled())
                  log.debug("Nacked " + count + " messages for removed subscription " + sub);
            }
         }
      }
   }

   public void removeAllMessages() throws JMSException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("removeAllMessages " + this);

      // Drop scheduled messages
      for (Iterator i = events.entrySet().iterator(); i.hasNext();)
      {
         Map.Entry entry = (Map.Entry) i.next();
         MessageReference message = (MessageReference) entry.getKey();
         Timeout timeout = (Timeout) entry.getValue();
         if (timeout != null)
         {
            timeout.cancel();
            i.remove();
            dropMessage(message);
         }
      }
      scheduledMessages.clear();

      synchronized (receivers)
      {
         synchronized (messages)
         {
            Iterator i = ((HashMap) unacknowledgedMessages.clone()).keySet().iterator();
            while (i.hasNext())
            {
               AcknowledgementRequest item = (AcknowledgementRequest) i.next();
               try
               {
                  acknowledge(item, null);
               }
               catch (JMSException ignore)
               {
               }
            }

            // Remove all remaining messages
            i = messages.iterator();
            while (i.hasNext())
            {
               MessageReference message = (MessageReference) i.next();
               i.remove();
               dropMessage(message);
            }
         }
      }
   }
   
   public void stop()
   {
      HashSet subs; 
      synchronized (receivers)
      {
         stopped = true;
         subs = new HashSet(subscribers);
         if (log.isTraceEnabled())
            log.trace("Stopping " + this + " with subscribers " + subs);
         clearEvents();
      }
      
      for (Iterator i = subs.iterator(); i.hasNext();)
      {
         Subscription sub = (Subscription) i.next();
         ClientConsumer consumer = (ClientConsumer) sub.clientConsumer;
         try
         {
            consumer.removeSubscription(sub.subscriptionId);
         }
         catch (Throwable t)
         {
            log.warn("Error during stop - removing subscriber " + sub, t);
         }
         nackMessages(sub);
      }
      
      MessageCache cache = server.getMessageCache();
      synchronized (messages)
      {
         for (Iterator i = messages.iterator(); i.hasNext();)
         {
            MessageReference message = (MessageReference) i.next();
            try
            {
               cache.remove(message);
            }
            catch (JMSException ignored)
            {
               log.trace("Ignored error removing message from cache", ignored);
            }
         }
      }
      
      // Help the garbage collector
      messages.clear();
      unacknowledgedMessages.clear();
      unackedByMessageRef.clear();
      unackedBySubscription.clear();
      subscribers.clear();
      removedSubscribers.clear();
   }
   
   /**
    * Create message counter object
    * 
    * @param name             topic/queue name
    * @param subscription     topic subscription
    * @param topic            topic flag
    * @param durable          durable subscription flag
    * @param daycountmax      message history day count limit
    *                           0: disabled,
    *                          >0: max day count,
    *                          <0: unlimited
    */
   public void createMessageCounter(String name, String subscription, boolean topic, boolean durable, int daycountmax)
   {
      // create message counter object
      counter = new MessageCounter(name, subscription, this, topic, durable, daycountmax);
   }

   /**
    * Get message counter object
    *
    * @return MessageCounter     message counter object or null 
    */
   public MessageCounter getMessageCounter()
   {
      return counter;
   }
   
   public String toString()
   {
      return super.toString() + "{id=" + description + '}';
   }

   /**
    * Clear all the events
    */
   protected void clearEvents()
   {
      for (Iterator i = events.entrySet().iterator(); i.hasNext();)
      {
         Map.Entry entry = (Map.Entry) i.next();
         Timeout timeout = (Timeout) entry.getValue();
         if (timeout != null)
         {
            timeout.cancel();
            i.remove();
         }
      }
      scheduledMessages.clear();
   }
   
   /**
    * Clear the event for a message
    * 
    * @param message the message reference
    */
   protected void clearEvent(MessageReference message)
   {
      Timeout timeout = (Timeout) events.remove(message);
      if (timeout != null)
         timeout.cancel();
      scheduledMessages.remove(message);
   }
   
   /**
    * Add a receiver
    *
    * @param sub the receiver to add
    */
   protected void addToReceivers(Subscription sub) throws JMSException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("addReceiver " + " " + sub + " " + this);

      synchronized (receivers)
      {
         if (stopped)
            throw new IllegalStateException("The destination is stopped " + getDescription());
         receivers.add(sub);
      }
   }

   /**
    * Remove a receiver
    *
    * @param sub the receiver to remove
    */
   protected void removeReceiver(Subscription sub)
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("removeReceiver " + " " + sub + " " + this);

      synchronized (receivers)
      {
         receivers.remove(sub);
      }
   }

   private void addTimeout(MessageReference message, TimeoutTarget t, long ts)
   {
      Timeout timeout = server.getTimeoutFactory().schedule(ts, t);
      events.put(message, timeout);
   }

   /**
    * Add a message
    *
    * @param message the message to add
    */
   private void internalAddMessage(MessageReference message)
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("internalAddMessage " + " " + message + " " + this);

      // If scheduled, put in timer queue
      long ts = message.messageScheduledDelivery;
      if (ts > 0 && ts > System.currentTimeMillis())
      {
         scheduledMessages.add(message);
         addTimeout(message, new EnqueueMessageTask(message), ts);
         if (trace)
            log.trace("scheduled message at " + new Date(ts) + ": " + message);
         // Can't deliver now
         return;
      }

      // Don't bother with expired messages
      if (message.isExpired())
      {
         expireMessageAsync(message);
         return;
      }

      try
      {
         Subscription found = null;
         synchronized (receivers)
         {
            if (receivers.size() != 0)
            {
               for (Iterator it = receivers.iterator(); it.hasNext();)
               {
                  Subscription sub = (Subscription) it.next();
                  if (sub.accepts(message.getHeaders()))
                  {
                     it.remove();
                     found = sub;
                     break;
                  }
               }
            }

            synchronized (messages)
            {
               if (found == null)
               {
                  messages.add(message);

                  // If a message is set to expire, and nobody wants it, put its reaper in
                  // the timer queue 
                  if (message.messageExpiration > 0)
                  {
                     addTimeout(message, new ExpireMessageTask(message), message.messageExpiration);
                  }
               }
               else
               {
                  setupMessageAcknowledgement(found, message);
               }
            }
         }
         
         // Queue to the receiver
         if (found != null)
         {
            updateRedeliveryFlags(message);
            queueMessageForSending(found, message);
         }
      }
      catch (JMSException e)
      {
         // Could happen at the accepts() calls
         log.error("Caught unusual exception in internalAddMessage.", e);
         // And drop the message, otherwise we have a leak in the cache
         dropMessage(message);
      }
   }

   /**
    * Queue a message for sending through the client consumer
    *
    * @param sub the subscirption to receive the message
    * @param message the message reference to queue
    */
   protected void queueMessageForSending(Subscription sub, MessageReference message)
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("queueMessageForSending " + " " + sub  + " " + message + " " + this);

      try
      {
         RoutedMessage r = new RoutedMessage();
         r.message = message;
         r.subscriptionId = new Integer(sub.subscriptionId);
         ((ClientConsumer) sub.clientConsumer).queueMessageForSending(r);
      }
      catch (Throwable t)
      {
         log.warn("Caught unusual exception sending message to receiver.", t);
      }
   }

   /**
    * Setup a message acknowledgement
    *
    * @param sub the subscription receiving the message
    * @param messageRef the message to be acknowledged
    * @throws JMSException for any error
    */
   protected void setupMessageAcknowledgement(Subscription sub, MessageReference messageRef) throws JMSException
   {
      SpyMessage message = messageRef.getMessage();
      AcknowledgementRequest nack = new AcknowledgementRequest(false);
      nack.destination = message.getJMSDestination();
      nack.messageID = message.getJMSMessageID();
      nack.subscriberId = sub.subscriptionId;
      
      UnackedMessageInfo unacked = new UnackedMessageInfo(messageRef, sub);
      unacknowledgedMessages.put(nack, unacked);
      unackedByMessageRef.put(messageRef, nack);
      HashMap map = (HashMap) unackedBySubscription.get(sub);
      if (map == null)
      {
         map = new HashMap();
         unackedBySubscription.put(sub, map);
      }
      map.put(messageRef, nack);
   }

   /**
    * Update redelivery flags
    *
    * @param messageRef the message to be acknowledged
    * @throws JMSException for any error
    */
   protected void updateRedeliveryFlags(MessageReference messageRef) throws JMSException
   {
      // Need to update the message so it recovers from crash with redelivery=true 
      if (server.isLazyRedeliveryUpdate() == false && messageRef.isPersistent())
      {
         // Temporarily set the flags
         messageRef.redelivered();
         // Update the persistent message outside the transaction
         // We want to know the message might have been delivered regardless
         messageRef.invalidate();
         server.getPersistenceManager().update(messageRef, null);
         // Now revert the flags back again (but only in memory)
         messageRef.revertRedelivered();
      }
   }

   /**
    * Remove a message
    *
    * @param message the message to remove
    */
   protected void dropMessage(MessageReference message)
   {
      dropMessage(message, null);
   }
   
   /**
    * Remove a message
    *
    * @param message the message to remove
    * @param txid the transaction context for the removal
    */
   protected void dropMessage(MessageReference message, Tx txid)
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("dropMessage " + this + " txid=" + txid);

      clearEvent(message);
      try
      {
         if (message.isPersistent())
         {
            try
            {
               server.getPersistenceManager().remove(message, txid);
            }
            catch (JMSException e)
            {
               try
               {
                  log.warn("Message removed from queue, but not from the persistent store: " + message.getMessage(), e);
               }
               catch (JMSException x)
               {
                  log.warn("Message removed from queue, but not from the persistent store: " + message, e);
               }
            }
         }
         server.getMessageCache().remove(message);
      }
      catch (JMSException e)
      {
         log.warn("Error dropping message " + message, e);
      }
   }

   /**
    * Expire a message asynchronously.
    *
    * @param messageRef the message to remove
    */
   protected void expireMessageAsync(MessageReference messageRef)
   {
      server.getThreadPool().run(new ExpireMessageTask(messageRef));
   }
   
   /**
    * Expire a message
    *
    * @param messageRef the message to remove
    */
   protected void expireMessage(MessageReference messageRef)
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("message expired: " + messageRef);

      SpyDestination ed = parameters.expiryDestination;
      if (ed == null)
      {
         dropMessage(messageRef);
         return;
      }

      if (trace)
         log.trace("sending to: " + ed);

      try
      {
         SpyMessage orig = messageRef.getMessage();
         SpyMessage copy = orig.myClone();
         copy.header.jmsPropertiesReadWrite = true;
         copy.setJMSExpiration(0);
         copy.setJMSDestination(ed);
         copy.setLongProperty(SpyMessage.PROPERTY_ORIG_EXPIRATION, orig.getJMSExpiration());
         copy.setStringProperty(SpyMessage.PROPERTY_ORIG_DESTINATION, orig.getJMSDestination().toString());
         TxManager tm = server.getPersistenceManager().getTxManager();
         Tx tx = tm.createTx();
         try 
         {
            server.addMessage(null, copy, tx);
            dropMessage(messageRef, tx);
            tm.commitTx(tx);
         }
         catch (JMSException e)
         {
            tm.rollbackTx(tx);
            throw e;
         }
      }
      catch (JMSException e)
      {
         log.error("Could not move expired message: " + messageRef, e);
      }
   }

   /**
    * Check whether a removed subscription can be permenantly removed.
    * This method is private because it assumes external synchronization
    *
    * @param the subscription to check
    */
   private void checkRemovedSubscribers(Subscription sub)
   {
      boolean trace = log.isTraceEnabled();
      if (removedSubscribers.contains(sub) && hasUnackedMessages(sub) == false)
      {
         if (trace)
            log.trace("Removing subscriber " + sub);
         removedSubscribers.remove(sub);
         subscribers.remove(sub);
         ((ClientConsumer) sub.clientConsumer).removeRemovedSubscription(sub.subscriptionId);
      }
   }

   /**
    * Check whether a subscription has unacknowledged messages.
    * This method is private because it assumes external synchronization
    *
    * @param sub the subscription to check
    * @return true when it has unacknowledged messages
    */
   private boolean hasUnackedMessages(Subscription sub)
   {
      return unackedBySubscription.containsKey(sub);
   }

   /**
    * Rollback an add message
    */
   class AddMessagePostRollBackTask implements Runnable
   {
      MessageReference message;

      AddMessagePostRollBackTask(MessageReference m)
      {
         message = m;
      }

      public void run()
      {
         try
         {
            server.getMessageCache().remove(message);
         }
         catch (JMSException e)
         {
            log.error("Could not remove message from the message cache after an add rollback: ", e);
         }
      }
   }

   /**
    * Add a message to the queue
    */
   class AddMessagePostCommitTask implements Runnable
   {
      MessageReference message;

      AddMessagePostCommitTask(MessageReference m)
      {
         message = m;
      }

      public void run()
      {
         internalAddMessage(message);

         // update message counter
         if (counter != null)
         {
            counter.incrementCounter();
         }
      }
   }

   /**
    * Restore a message to the queue
    */
   class RestoreMessageTask implements Runnable
   {
      MessageReference message;

      RestoreMessageTask(MessageReference m)
      {
         message = m;
      }

      public void run()
      {
         nackMessage(message);
      }
   }

   /**
    * Remove a message
    */
   class RemoveMessageTask implements Runnable
   {
      MessageReference message;

      RemoveMessageTask(MessageReference m)
      {
         message = m;
      }

      public void run()
      {
         try
         {
            clearEvent(message);
            server.getMessageCache().remove(message);
         }
         catch (JMSException e)
         {
            log.error("Could not remove an acknowleged message from the message cache: ", e);
         }
      }
   }

   /**
    * Schedele message delivery
    */
   private class EnqueueMessageTask implements TimeoutTarget
   {
      private MessageReference messageRef;

      public EnqueueMessageTask(MessageReference messageRef)
      {
         this.messageRef = messageRef;
      }

      public void timedOut(Timeout timeout)
      {
         if (log.isTraceEnabled())
            log.trace("scheduled message delivery: " + messageRef);
         events.remove(messageRef);
         scheduledMessages.remove(messageRef);
         internalAddMessage(messageRef);
      }
   }

   /**
    * Drop a message when it expires
    */
   private class ExpireMessageTask implements TimeoutTarget, Runnable
   {
      private MessageReference messageRef;

      public ExpireMessageTask(MessageReference messageRef)
      {
         this.messageRef = messageRef;
      }

      public void timedOut(Timeout timout)
      {
         events.remove(messageRef);
         scheduledMessages.remove(messageRef);
         synchronized (messages)
         {
            // If the message was already sent, then do nothing
            // (This probably happens more than not)
            if (messages.remove(messageRef) == false)
               return;
         }
         expireMessage(messageRef);
      }

      public void run()
      {
         expireMessage(messageRef);
      }
   }

   /**
    * Information about unacknowledged messages
    */
   private static class UnackedMessageInfo
   {
      public MessageReference messageRef;
      public Subscription sub;
      public UnackedMessageInfo(MessageReference messageRef, Subscription sub)
      {
         this.messageRef = messageRef;
         this.sub = sub;
      }
   }
}
