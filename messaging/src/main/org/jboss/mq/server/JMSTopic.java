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
import java.util.Iterator;
import java.util.TreeMap;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;

import org.jboss.mq.DestinationFullException;
import org.jboss.mq.DurableSubscriptionID;
import org.jboss.mq.SpyDestination;
import org.jboss.mq.SpyJMSException;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.SpyTopic;
import org.jboss.mq.Subscription;
import org.jboss.mq.pm.NewPersistenceManager;
import org.jboss.mq.pm.PersistenceManager;
import org.jboss.mq.pm.Tx;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;

/**
 *  This class is a message queue which is stored (hashed by Destination) on the
 *  JMS provider
 *
 * @author     Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author     Hiram Chirino (Cojonudo14@hotmail.com)
 * @author     David Maplesden (David.Maplesden@orion.co.nz)
 * @author     Adrian Brock (adrian@jboss.com)
 * @created    August 16, 2001
 * @version    $Revision: 61446 $
 */
public class JMSTopic extends JMSDestination
{

   //Hashmap of ExclusiveQueues
   ConcurrentReaderHashMap durQueues = new ConcurrentReaderHashMap();
   ConcurrentReaderHashMap tempQueues = new ConcurrentReaderHashMap();

   public JMSTopic(SpyDestination dest, ClientConsumer temporary, JMSDestinationManager server, BasicQueueParameters parameters) throws JMSException
   {
      super(dest, temporary, server, parameters);

      // This is a bit of a hack, for backwards compatibility
      PersistenceManager pm = server.getPersistenceManager();
      parameters.lateClone = (pm instanceof NewPersistenceManager);
   }

   public void addSubscriber(Subscription sub) throws JMSException
   {
      SpyTopic topic = (SpyTopic) sub.destination;
      DurableSubscriptionID id = topic.getDurableSubscriptionID();

      if (id == null)
      {
         // create queue
         ExclusiveQueue q = new ExclusiveQueue(server, destination, sub, parameters);

         // create topic queue message counter
         q.createMessageCounter(destination.getName(), q.getDescription(), true, false, parameters.messageCounterHistoryDayLimit);
         
         tempQueues.put(sub, q);
         q.addSubscriber(sub);
      }
      else
      {
         PersistentQueue q = (PersistentQueue) durQueues.get(id);

         // Check for already in use
         if (q != null && q.isInUse())
            throw new IllegalStateException("The durable subscription is already in use. " + id);
         
         // Check for a changed selector
         boolean selectorChanged = false;
         if (q != null)
         {
            String newSelector = sub.messageSelector;
            String oldSelector = null;
            if (q instanceof SelectorPersistentQueue)
               oldSelector = ((SelectorPersistentQueue) q).selectorString;
            if ((newSelector == null && oldSelector != null)
               || (newSelector != null && newSelector.equals(oldSelector) == false))
               selectorChanged = true;
         }

         if (q == null || //Brand new durable subscriber
         !q.destination.equals(topic) || selectorChanged)
         {
            //subscription changed to new topic
            server.getStateManager().setDurableSubscription(server, id, topic);

            // Pickup the new queue
            synchronized (durQueues)
            {
               q = (PersistentQueue) durQueues.get(id);
            }
         }
         q.addSubscriber(sub);
      }
   }

   public void removeSubscriber(Subscription sub) throws JMSException
   {
      BasicQueue queue = null;
      SpyTopic topic = (SpyTopic) sub.destination;
      DurableSubscriptionID id = topic.getDurableSubscriptionID();
      if (id == null)
         queue = (BasicQueue) tempQueues.get(sub);
      else
         queue = (BasicQueue) durQueues.get(id);
      // The queue may be null if the durable subscription
      // is destroyed before the consumer is unsubscribed!
      if (queue == null)
         ((ClientConsumer) sub.clientConsumer).removeRemovedSubscription(sub.subscriptionId);
      else
         queue.removeSubscriber(sub);
   }

   public void nackMessages(Subscription sub) throws JMSException
   {
      BasicQueue queue = null;
      SpyTopic topic = (SpyTopic) sub.destination;
      DurableSubscriptionID id = topic.getDurableSubscriptionID();
      if (id == null)
         queue = (BasicQueue) tempQueues.get(sub);
      else
         queue = (BasicQueue) durQueues.get(id);
      if (queue != null)
      {
         queue.nackMessages(sub);
      }
   }

   void cleanupSubscription(Subscription sub)
   {
      //just try to remove from tempQueues, don't worry if its not there
      BasicQueue queue = (BasicQueue) tempQueues.remove(sub);
      try
      {
         if (queue != null)
            queue.removeAllMessages();
      }
      catch (JMSException e)
      {
         cat.debug("Error removing messages for subscription " + sub, e);
      }
   }

   public void addReceiver(Subscription sub) throws JMSException
   {
      getQueue(sub).addReceiver(sub);
   }

   public void removeReceiver(Subscription sub)
   {
      try
      {
         getQueue(sub).removeReceiver(sub);
      }
      catch (JMSException e)
      {
         cat.trace("Subscription is not registered: " + sub, e);
      }
   }

   public void restoreMessage(MessageReference messageRef)
   {
      try
      {
         SpyMessage spyMessage = messageRef.getMessage();
         updateNextMessageId(spyMessage);
         if (spyMessage.header.durableSubscriberID == null)
         {
            cat.debug("Trying to restore message with null durableSubscriberID");
         }
         else
         {
            BasicQueue queue = ((BasicQueue) durQueues.get(spyMessage.header.durableSubscriberID));
            messageRef.queue = queue;
            queue.restoreMessage(messageRef);
         }
      }
      catch (JMSException e)
      {
         cat.error("Could not restore message:", e);
      }
   }

   public void restoreMessage(SpyMessage message, Tx txid, int type)
   {
      try
      {
         updateNextMessageId(message);
         if (message.header.durableSubscriberID == null)
         {
            cat.debug("Trying to restore message with null durableSubscriberID");
         }
         else
         {
            BasicQueue queue = (BasicQueue) durQueues.get(message.header.durableSubscriberID);
            MessageReference messageRef = server.getMessageCache().add(message, queue, MessageReference.STORED);
            queue.restoreMessage(messageRef, txid, type);
         }
      }
      catch (JMSException e)
      {
         cat.error("Could not restore message:", e);
      }
   }

   public void restoreMessage(SpyMessage message, DurableSubscriptionID id)
   {
      try
      {
         updateNextMessageId(message);
         if (id == null)
         {
            cat.debug("Trying to restore message with null durableSubscriberID");
         }
         else
         {
            BasicQueue queue = (BasicQueue) durQueues.get(id);
            MessageReference messageRef = server.getMessageCache().add(message, queue, MessageReference.STORED, id);
            queue.restoreMessage(messageRef);
         }
      }
      catch (JMSException e)
      {
         cat.error("Could not restore message:", e);
      }
   }

   //called by state manager when a durable sub is created
   public void createDurableSubscription(DurableSubscriptionID id) throws JMSException
   {
      if (temporaryDestination != null)
         throw new JMSException("Not a valid operation on a temporary topic");

      SpyTopic dstopic = new SpyTopic((SpyTopic) destination, id);

      Throwable error = null;
      for (int i = 0; i <= parameters.recoveryRetries; ++i)
      {
         // Create a subscription 
         BasicQueue queue;
         if (id.getSelector() == null)
            queue = new PersistentQueue(server, dstopic, parameters);
         else
            queue = new SelectorPersistentQueue(server, dstopic, id.getSelector(), parameters);

         // create topic queue message counter
         queue.createMessageCounter(destination.getName(), id.toString(), true, true, parameters.messageCounterHistoryDayLimit);

         durQueues.put(id, queue);

         try
         {
            // restore persistent queue data         
            server.getPersistenceManager().restoreQueue(this, dstopic);
            
            // done
            break;
         }
         catch (Throwable t)
         {
            if (i < parameters.recoveryRetries)
               cat.warn("Error restoring topic subscription " + queue + " retries=" + i + " of " + parameters.recoveryRetries, t);
            else
               error = t;
            try
            {
               queue.stop();
            }
            catch (Throwable ignored)
            {
               cat.trace("Ignored error stopping topic subscription " + queue, ignored);
            }
            finally
            {
               durQueues.remove(id);
               queue = null;
            }
         }
      }
      
      if (error != null)
         SpyJMSException.rethrowAsJMSException("Unable to recover topic subscription " + id + " retries=" + parameters.recoveryRetries, error);
   }

   //called by JMSServer when a destination is being closed.
   public void close() throws JMSException
   {
      if (temporaryDestination != null)
         throw new JMSException("Not a valid operation on a temporary topic");
      
      Iterator i = tempQueues.values().iterator();
      while (i.hasNext())
      {
         ExclusiveQueue queue = (ExclusiveQueue) i.next();
         queue.stop();
      }

      i = durQueues.values().iterator();
      while (i.hasNext())
      {
         PersistentQueue queue = (PersistentQueue) i.next();
         queue.stop();
         server.getPersistenceManager().closeQueue(this, queue.getSpyDestination());
      }
   }

   //called by state manager when a durable sub is deleted
   public void destroyDurableSubscription(DurableSubscriptionID id) throws JMSException
   {
      boolean inUse = false;
      BasicQueue queue = null;
      synchronized (durQueues)
      {
         queue = (BasicQueue) durQueues.get(id);
         if (queue != null && queue.isInUse())
            inUse = true;
         else
            durQueues.remove(id);
      }
      if (queue == null)
         throw new IllegalStateException("Unable to find durable subscription: " + id);
      if (inUse)
         throw new IllegalStateException("The durable subscription is in use: " + id);
      queue.removeAllMessages();
   }

   public SpyMessage receive(Subscription sub, boolean wait) throws javax.jms.JMSException
   {
      return getQueue(sub).receive(sub, wait);
   }

   public void acknowledge(org.jboss.mq.AcknowledgementRequest req, Subscription sub, org.jboss.mq.pm.Tx txId)
      throws JMSException
   {
      getQueue(sub).acknowledge(req, txId);
   }

   public void addMessage(SpyMessage message, org.jboss.mq.pm.Tx txId) throws JMSException
   {
      StringBuffer errorMessage = null;

      // Whether the message was added to a persistence queue
      boolean added = false;

      //Number the message so that we can preserve order of delivery.
      long messageId = nextMessageId();
         
      if (parameters.lateClone)
         message.header.messageId = messageId;

      Iterator iter = durQueues.keySet().iterator();
      while (iter.hasNext())
      {
         DurableSubscriptionID id = (DurableSubscriptionID) iter.next();
         PersistentQueue q = (PersistentQueue) durQueues.get(id);
         MessageReference ref;
         if (parameters.lateClone)
         {
            ref = server.getMessageCache().add(message, q, MessageReference.NOT_STORED, id);
         }
         else
         {
            SpyMessage clone = message.myClone();
            clone.header.durableSubscriberID = id;
            clone.header.messageId = messageId;
            clone.setJMSDestination(q.getSpyDestination());
            ref = server.getMessageCache().add(clone, q, MessageReference.NOT_STORED);
         }
         try
         {
            // For shared blob write it for the first durable subscription
            if (added == false && parameters.lateClone && ref.isPersistent())
            {
               NewPersistenceManager pm = (NewPersistenceManager) server.getPersistenceManager();
               pm.addMessage(message);
               added = true;
            }
            q.addMessage(ref, txId);
         }
         catch (DestinationFullException e)
         {
            if (errorMessage == null)
               errorMessage = new StringBuffer(e.getText());
            else
               errorMessage.append(", ").append(e.getText());
         }
      }
         
      iter = tempQueues.values().iterator();
      while (iter.hasNext())
      {
         BasicQueue q = (BasicQueue) iter.next();
         MessageReference ref;
         if (parameters.lateClone)
         {
            ref = server.getMessageCache().add(message, q, MessageReference.NOT_STORED);
         }
         else
         {
            SpyMessage clone = message.myClone();
            clone.header.messageId = messageId;
            ref = server.getMessageCache().add(clone, q, MessageReference.NOT_STORED);
         }
         try
         {
            q.addMessage(ref, txId);
         }
         catch (DestinationFullException e)
         {
            if (errorMessage == null)
               errorMessage = new StringBuffer(e.getText());
            else
               errorMessage.append(", ").append(e.getText());
         }
      }
      
      if (errorMessage != null)
         throw new DestinationFullException(errorMessage.toString());
   }

   public int getAllMessageCount()
   {
      return calculateMessageCount(getAllQueues());
   }

   public int getDurableMessageCount()
   {
      return calculateMessageCount(getPersistentQueues());
   }

   public int getNonDurableMessageCount()
   {
      return calculateMessageCount(getTemporaryQueues());
   }

   public ArrayList getAllQueues()
   {
      ArrayList result = new ArrayList(getAllSubscriptionsCount());
      result.addAll(getPersistentQueues());
      result.addAll(getTemporaryQueues());
      return result;
   }

   public ArrayList getTemporaryQueues()
   {
      return new ArrayList(tempQueues.values());
   }

   public ArrayList getPersistentQueues()
   {
      return new ArrayList(durQueues.values());
   }

   public int getAllSubscriptionsCount()
   {
      return durQueues.size() + tempQueues.size();
   }

   public int getDurableSubscriptionsCount()
   {
      return durQueues.size();
   }

   public int getNonDurableSubscriptionsCount()
   {
      return tempQueues.size();
   }

   public ArrayList getAllSubscriptions()
   {
      ArrayList result = new ArrayList(getAllSubscriptionsCount());
      result.addAll(getDurableSubscriptions());
      result.addAll(getNonDurableSubscriptions());
      return result;
   }

   public ArrayList getDurableSubscriptions()
   {
      return new ArrayList(durQueues.keySet());
   }

   public ArrayList getNonDurableSubscriptions()
   {
      return new ArrayList(tempQueues.keySet());
   }

   public PersistentQueue getDurableSubscription(DurableSubscriptionID id)
   {
      return (PersistentQueue) durQueues.get(id);
   }

   public BasicQueue getQueue(Subscription sub) throws JMSException
   {
      SpyTopic topic = (SpyTopic) sub.destination;
      DurableSubscriptionID id = topic.getDurableSubscriptionID();
      BasicQueue queue = null;
      if (id != null)
         queue = getDurableSubscription(id);
      else
         queue = (BasicQueue) tempQueues.get(sub);
      
      if (queue != null)
         return queue;
      else
         throw new JMSException("Subscription not found: " + sub);
   }

   // Package protected ---------------------------------------------

   /*
    * @see JMSDestination#isInUse()
    */
   public boolean isInUse()
   {
      if (tempQueues.size() > 0)
         return true;
      Iterator iter = durQueues.values().iterator();
      while (iter.hasNext())
      {
         PersistentQueue q = (PersistentQueue) iter.next();
         if (q.isInUse())
            return true;
      }
      return false;
   }
   /**
    * @see JMSDestination#destroy()
    */
   public void removeAllMessages() throws JMSException
   {
      Iterator i = durQueues.values().iterator();
      while (i.hasNext())
      {
         PersistentQueue queue = (PersistentQueue) i.next();
         queue.removeAllMessages();
      }
   }

   private int calculateMessageCount(ArrayList queues)
   {
      int count = 0;
      for (Iterator i = queues.listIterator(); i.hasNext();)
      {
         BasicQueue queue = (BasicQueue) i.next();
         count += queue.getQueueDepth();
      }
      return count;
   }

   /**
    * Get message counter of all topic internal queues
    * 
    * @return MessageCounter[]  topic queue message counter array
    */
   public MessageCounter[] getMessageCounter()
   {
      TreeMap map = new TreeMap();

      Iterator i = durQueues.values().iterator();

      while (i.hasNext())
      {
         BasicQueue queue = (BasicQueue) i.next();
         MessageCounter counter = queue.getMessageCounter();

         if (counter != null)
         {
            String key = counter.getDestinationName() + counter.getDestinationSubscription();
            map.put(key, counter);
         }
      }

      i = tempQueues.values().iterator();

      while (i.hasNext())
      {
         BasicQueue queue = (BasicQueue) i.next();
         MessageCounter counter = queue.getMessageCounter();

         if (counter != null)
         {
            String key = counter.getDestinationName() + counter.getDestinationSubscription();
            map.put(key, counter);
         }
      }

      return (MessageCounter[]) map.values().toArray(new MessageCounter[0]);
   }
}
