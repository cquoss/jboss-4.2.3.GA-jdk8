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

import java.lang.ref.SoftReference;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;

import org.jboss.logging.Logger;
import org.jboss.mq.DurableSubscriptionID;
import org.jboss.mq.SpyMessage;

/**
 * This class holds a reference to an actual Message.  Where it is actually
 * at may vary.  The reference it holds may be a:
 * <ul>
 * <li>Hard Reference - The message is consider recently used and should not be paged out
 * <li>Soft Reference - The message is consider old and CAN be removed from memory by the GC
 * <li>No Reference - The message was removed from memory by the GC, but we can load it from a file.
 * </ul>
 *
 * @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 * @author <a href="mailto:pra@tim.se">Peter Antman</a>
 * @version    $Revision: 66418 $
 */
public class MessageReference implements Comparable
{
   static Logger log = Logger.getLogger(MessageReference.class);

   /**
    * The message is not persisted
    */
   public static final int NOT_STORED = 1;

   /**
    * The message is persisted
    */
   public static final int STORED = 2;

   /**
    * It was a persistent message for a joint
    * cache store/persistent manager.
    * This states guards against double
    * removal from the cache while keeping
    * error checking for incorrect double removal
    * No message should be at this state for very long
    */
   public static final int REMOVED = 3;

   public long referenceId;
   public SpyMessage hardReference;

   // These fields are copied over from the messae itself..
   // they are used too often to not have them handy.
   public byte jmsPriority;
   public long messageId;
   public int jmsDeliveryMode;
   public long messageScheduledDelivery;
   public long messageExpiration;

   public boolean redelivered;
   public long redeliveryDelay;
   public int redeliveryCount;

   // Data used on the server
   public BasicQueue queue;
   public MessageCache messageCache;
   public SoftReference softReference;
   public DurableSubscriptionID durableSubscriberID;
   public int stored;
   transient public Object persistData;

   MessageReference()
   {
   }

   //init and reset methods for use by object pool
   void init(MessageCache messageCache, long referenceId, SpyMessage message, BasicQueue queue, DurableSubscriptionID id) throws JMSException
   {
      this.messageCache = messageCache;
      this.hardReference = message;
      this.referenceId = referenceId;
      this.jmsPriority = (byte) message.getJMSPriority();
      this.messageId = message.header.messageId;
      this.stored = NOT_STORED;

      this.jmsDeliveryMode = message.header.jmsDeliveryMode;
      if (message.propertyExists(SpyMessage.PROPERTY_SCHEDULED_DELIVERY))
         this.messageScheduledDelivery = message.getLongProperty(SpyMessage.PROPERTY_SCHEDULED_DELIVERY);
      if (message.propertyExists(SpyMessage.PROPERTY_REDELIVERY_DELAY))
         this.redeliveryDelay = message.getLongProperty(SpyMessage.PROPERTY_REDELIVERY_DELAY);
      // If redelivery delay is not specified, then use the value specified
      // in queue configuration.
      else if (queue.parameters.redeliveryDelay > 0)
         this.redeliveryDelay = queue.parameters.redeliveryDelay;

      // If redelivery limit is not specified, then use the value specified
      // in queue configuration.
      if (queue.parameters.redeliveryLimit > -1 && !message.propertyExists(SpyMessage.PROPERTY_REDELIVERY_LIMIT))
         message.header.jmsProperties.put(SpyMessage.PROPERTY_REDELIVERY_LIMIT,
                     new Integer(queue.parameters.redeliveryLimit));

      this.messageExpiration = message.getJMSExpiration();
      this.redelivered = message.getJMSRedelivered();
      if (message.propertyExists(SpyMessage.PROPERTY_REDELIVERY_COUNT))
         this.redeliveryCount = message.getIntProperty(SpyMessage.PROPERTY_REDELIVERY_COUNT);

      this.durableSubscriberID = id;

      this.queue = queue;
      this.persistData = null;
   }

   void reset()
   {
      //clear refs so gc can collect unused objects
      if (softReference != null && softReference.get() != null)
         messageCache.softRefCacheSize--;
      this.messageCache = null;
      this.hardReference = null;
      this.softReference = null;
      this.queue = null;
   }

   public SpyMessage getMessageForDelivery() throws JMSException
   {
      SpyMessage message = getMessage();
      if (queue.parameters.lateClone)
      {
         message = message.myClone();
         message.header.durableSubscriberID = durableSubscriberID;
         message.header.jmsRedelivered = redelivered;
         message.header.jmsProperties.put(SpyMessage.PROPERTY_REDELIVERY_COUNT, new Integer(redeliveryCount));
      }
      return message;
   }

   public SpyMessage getMessage() throws JMSException
   {
      SpyMessage result = null;

      synchronized (this)
      {
         if (hardReference == null)
         {
            makeHard();
            result = hardReference;
            messageCache.messageReferenceUsedEvent(this, false);
         }
         else
         {
            result = hardReference;
            messageCache.cacheHits++;
            messageCache.messageReferenceUsedEvent(this, true);
         }
         return result;
      }
   }

   /**
    * The message is being redelivered
    */
   public synchronized void redelivered() throws JMSException
   {
      this.redelivered = true;

      if (redeliveryDelay != 0)
      {
         log.trace("message has redelivery delay");
         messageScheduledDelivery = System.currentTimeMillis() + redeliveryDelay;
      }

      ++redeliveryCount;
      
      if (isLateClone() == false)
      {
         SpyMessage message = getMessage();
         message.setJMSRedelivered(redelivered);

         message.header.jmsProperties.put(SpyMessage.PROPERTY_REDELIVERY_COUNT, new Integer(redeliveryCount));
      }
   }

   /**
    * Revert the redelivered status
    */
   public synchronized void revertRedelivered() throws JMSException
   {
      this.redelivered = true;

      messageScheduledDelivery = 0;

      --redeliveryCount;
      if (redeliveryCount == 0)
         redelivered = false;
      
      if (isLateClone() == false)
      {
         SpyMessage message = getMessage();
         message.setJMSRedelivered(redelivered);

         message.header.jmsProperties.put(SpyMessage.PROPERTY_REDELIVERY_COUNT, new Integer(redeliveryCount));
      }
   }

   /**
    * Returns true if this message reference has expired.
    */
   public boolean isExpired()
   {
      if (messageExpiration == 0)
         return false;
      long ts = System.currentTimeMillis();
      return messageExpiration < ts;
   }

   /**
    * Determines whether the message is persistent in the sense
    * that it survives a crash
    */
   public boolean isPersistent()
   {
      return queue instanceof PersistentQueue && jmsDeliveryMode == DeliveryMode.PERSISTENT;
   }

   /**
    * Are we entirely in memory?
    *
    * @return true when in memory, false otherwise
    */
   public boolean inMemory()
   {
      return queue.parameters.inMemory;
   }
   
   /**
    * Determines the persistent for storing the message
    */
   public String getPersistentKey()
   {
      return queue.getDescription();
   }

   /**
    * Are we late cloning messages?
    */
   public boolean isLateClone()
   {
      return queue.parameters.lateClone;
   }

   /**
    * We could optimize caching by keeping the headers but not the body.
    * The server will uses the headers more often than the body and the
    * headers take up much message memory than the body
    *
    * For now just return the message.
    */
   public SpyMessage.Header getHeaders() throws JMSException
   {
      return getMessage().header;
   }

   void clear() throws JMSException
   {
      synchronized (this)
      {
         if (stored == STORED)
            messageCache.removeFromStorage(this);
         stored = MessageReference.REMOVED;
      }
   }

   public void invalidate() throws JMSException
   {
      synchronized (this)
      {
         if (stored == STORED)
         {
            if (hardReference == null)
            {
               makeHard();
               messageCache.messageReferenceUsedEvent(this, false);
            }
            messageCache.removeFromStorage(this);
         }
      }
   }

   public void removeDelayed() throws JMSException
   {
      messageCache.removeDelayed(this);
   }

   void makeSoft() throws JMSException
   {
      boolean trace = log.isTraceEnabled();
      synchronized (this)
      {
         // Attempt to soften a removed message
         if (stored == REMOVED)
            throw new JMSException("CACHE ERROR: makeSoft() on a removed message " + this);

         // It is already soft
         if (softReference != null)
         {
            // Sanity check
            if (stored == NOT_STORED)
               throw new JMSException("CACHE ERROR: soft reference to unstored message " + this);
            return;
         }

         if (stored == NOT_STORED)
            messageCache.saveToStorage(this, hardReference);

         // HACK: allow the jdbc2 driver to reject saveToStorage for persistent messages
         // it is just about to persist the message when it gets some cpu
         if (stored != STORED)
         {
            if (trace)
               log.trace("saveToStorage rejected by cache " + toString());
            return;
         }

         if (messageCache.getMakeSoftReferences())
            softReference = new SoftReference(hardReference, messageCache.referenceQueue);

         // We don't need the hard ref anymore..
         messageCache.soften(this);
         hardReference = null;
      }
   }

   /**
    * Called from A PeristenceManager/CacheStore, 
    * to let us know that this message is already stored on disk.
    */
   public void setStored(int stored)
   {
      this.stored = stored;
   }

   void makeHard() throws JMSException
   {
      synchronized (this)
      {
         // Attempt to harden a removed message
         if (stored == REMOVED)
            throw new JMSException("CACHE ERROR: makeHard() on a removed message " + this);

         // allready hard
         if (hardReference != null)
            return;

         // Get the object via the softref
         if (softReference != null)
            hardReference = (SpyMessage) softReference.get();

         // It might have been removed from the cache due to memory constraints
         if (hardReference == null)
         {
            // load it from disk.
            hardReference = messageCache.loadFromStorage(this);
            messageCache.cacheMisses++;
         }
         else
         {
            messageCache.cacheHits++;
         }

         // Since we have hard ref, we do not need the soft one.
         if (softReference != null && softReference.get() != null)
            messageCache.softRefCacheSize--;
         softReference = null;
      }
   }

   public boolean equals(Object o)
   {
      try
      {
         return referenceId == ((MessageReference) o).referenceId;
      }
      catch (Throwable e)
      {
         return false;
      }
   }

   /**
    * This method allows message to be order on the server queues
    * by priority and the order that they came in on.
    *
    * @see Comparable#compareTo(Object)
    */
   public int compareTo(Object o)
   {
      MessageReference sm = (MessageReference) o;
      if (jmsPriority > sm.jmsPriority)
      {
         return -1;
      }
      if (jmsPriority < sm.jmsPriority)
      {
         return 1;
      }
      return (int) (messageId - sm.messageId);
   }

   /**
    * For debugging
    */
   public String toString()
   {
      StringBuffer buffer = new StringBuffer(100);
      if (messageCache == null)
         buffer.append(" NOT IN CACHE hashCode=").append(hashCode());

      else
      {
         buffer.append(referenceId);
         buffer.append(" msg=").append(messageId);
         if (hardReference != null)
            buffer.append(" hard");
         if (softReference != null)
            buffer.append(" soft");
         switch (stored)
         {
            case NOT_STORED :
               buffer.append(" NOT_STORED");
               break;
            case STORED :
               buffer.append(" STORED");
               break;
            case REMOVED :
               buffer.append(" REMOVED");
               break;
         }
         switch (jmsDeliveryMode)
         {
            case DeliveryMode.NON_PERSISTENT :
               buffer.append(" NON_PERSISTENT");
               break;
            case DeliveryMode.PERSISTENT :
               buffer.append(" PERSISTENT");
               break;
         }
         if (persistData != null)
            buffer.append(" persistData=").append(persistData);
         if (queue != null)
            buffer.append(" queue=").append(queue.getDescription());
         else
            buffer.append(" NO_QUEUE");
         buffer.append(" priority=").append(jmsPriority);
         buffer.append(" lateClone=").append(isLateClone());
         buffer.append(" hashCode=").append(hashCode());
      }
      return buffer.toString();
   }
}
