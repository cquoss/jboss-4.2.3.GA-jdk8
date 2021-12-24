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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.HashMap;
import javax.jms.JMSException;
import javax.management.MBeanRegistration;
import javax.management.ObjectName;
import org.jboss.mq.DurableSubscriptionID;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.pm.CacheStore;
import org.jboss.system.ServiceMBeanSupport;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedLong;

/**
 * This class implements a Message cache so that larger amounts of messages
 * can be processed without running out of memory.  When memory starts getting tight
 * it starts moving messages out of memory and into a file so that they can be recovered
 * later.
 *
 * The locks should be obtained in the following order:<br>
 * mr, the relevent message we are working with<br>
 * lruCache, when maintaining the usage order
 *
 * @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 * @author <a href="mailto:David.Maplesden@orion.co.nz">David Maplesden</a>
 * @author <a href="mailto:pra@tim.se">Peter Antman</a>
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version    $Revision: 57198 $
 *
 * @jmx.mbean name="jboss.mq:service=MessageCache"
 *      extends="org.jboss.system.ServiceMBean"
 */
public class MessageCache extends ServiceMBeanSupport implements MessageCacheMBean, MBeanRegistration, Runnable
{
   public static final long ONE_MEGABYTE = 1024L * 1000;
   public static final long DEFAULT_HIGH_MEMORY_MARK = ONE_MEGABYTE * 50;
   public static final long DEFAULT_MAX_MEMORY_MARK = ONE_MEGABYTE * 60;

   // The cached messages are orded in a LRU linked list
   private LRUCache lruCache = new LRUCache();

   // Provides a Unique ID to MessageHanles
   private SynchronizedLong messageCounter = new SynchronizedLong(0);
   long cacheHits = 0;
   long cacheMisses = 0;

   protected CacheStore cacheStore;
   ObjectName cacheStoreName;

   private Thread referenceSoftner;

   private long highMemoryMark = DEFAULT_HIGH_MEMORY_MARK;
   private long maxMemoryMark = DEFAULT_MAX_MEMORY_MARK;

   /** Whether to make soft references */
   private boolean makeSoftReferences = true;

   /** The last time we softened */
   private long lastSoften = 0L;

   /** soften no more than often */
   private long softenNoMoreOftenThanMillis = 0L;

   /** soften at least every */
   private long softenAtLeastEveryMillis = 0L;

   /** The length of time to wait before checking whether we should soften messages */
   private long softenWaitMillis = 1000L;

   /** The minimum number of hard messages */
   private int minimumHard = 1;

   /** The maximum number of hard messages */
   private int maximumHard = 0;
   
   int softRefCacheSize = 0;
   int totalCacheSize = 0;

   // Used to get notified when message are being deleted by GC
   ReferenceQueue referenceQueue = new ReferenceQueue();

   // The historical number of softenings
   long softenedSize = 0;

   // Check the soft reference depth
   boolean checkSoftReferenceDepth = false;

   /**
    * The <code>getInstance</code> method
    *
    * @return a <code>MessageCache</code> value
    *
    * @jmx.managed-attribute
    */
   public MessageCache getInstance()
   {
      return this;
   }

   /**
    * Adds a message to the cache.
    */
   public MessageReference add(SpyMessage message, BasicQueue queue, int stored) throws javax.jms.JMSException
   {
      DurableSubscriptionID id = message.header.durableSubscriberID;
      return addInternal(message, queue, stored, id);
   }

   /**
    * Adds a message to the cache.
    */
   public MessageReference add(SpyMessage message, BasicQueue queue, int stored, DurableSubscriptionID id) throws javax.jms.JMSException
   {
      return addInternal(message, queue, stored, id);
   }

   /**
    * Adds a message to the cache.
    */
   public MessageReference addInternal(SpyMessage message, BasicQueue queue, int stored, DurableSubscriptionID id) throws javax.jms.JMSException
   {
      // Create the message reference
      MessageReference mh = new MessageReference();
      mh.init(this, messageCounter.increment(), message, queue, id);
      mh.setStored(stored);

      // Add it to the cache
      synchronized (mh)
      {
         synchronized (lruCache)
         {
            lruCache.addMostRecent(mh);
            totalCacheSize++;
         }
      }
      validateSoftReferenceDepth();

      return mh;
   }

   /**
    * removes a message from the cache
    */
   public void remove(MessageReference mr) throws JMSException
   {
      // Remove if not done already
      removeInternal(mr, true, true);
   }

   /**
    * removes a message from the cache without returning it to the pool
    * used in two phase removes for joint cache/persistence
    */
   public void removeDelayed(MessageReference mr) throws JMSException
   {
      // Remove from the cache
      removeInternal(mr, true, false);
   }

   /**
    * removes a message from the cache but does not clear it,
    * used in softening
    */
   void soften(MessageReference mr) throws JMSException
   {
      // Remove from the cache
      removeInternal(mr, false, false);

      if (makeSoftReferences)
         softRefCacheSize++;
   }

   /**
    * removes a message from the cache
    */
   protected void removeInternal(MessageReference mr, boolean clear, boolean reset) throws JMSException
   {
      synchronized (mr)
      {
         if (mr.stored != MessageReference.REMOVED)
         {
            synchronized (lruCache)
            {
               if (mr.hardReference != null) //If message is not hard, dont do lru stuff
                  lruCache.remove(mr);
               if (clear)
                  totalCacheSize--;
            }
            if (clear)
               mr.clear();
            //Will remove it from storage if stored
         }

         if (reset)
            mr.reset();
         //Return to the pool
      }
   }

   /**
    * The strategy is that we keep the most recently used messages as
    * Hard references.  Then we make the older ones soft references.  Making
    * something a soft reference stores it to disk so we need to avoid making
    * soft references if we can avoid it.  But once it is made a soft reference does
    * not mean that it is removed from memory.  Depending on how agressive the JVM's
    * GC is, it may stay around long enough for it to be used by a client doing a read,
    * saving us read from the file system.  If memory gets tight the GC will remove
    * the soft references.  What we want to do is make sure there are at least some
    * soft references available so that the GC can reclaim memory.
    * @see Runnable#run()
    */
   public void run()
   {
      try
      {
         while (true)
         {
            // Get the next soft reference that was canned by the GC
            Reference r = null;
            if (checkSoftReferenceDepth)
               r = referenceQueue.poll();
            else
               r = referenceQueue.remove(softenWaitMillis);
            if (r != null)
            {
               softRefCacheSize--;
               // the GC will free a set of messages together, so we poll them
               // all before we validate the soft reference depth.
               while ((r = referenceQueue.poll()) != null)
               {
                  softRefCacheSize--;
               }
               if (log.isTraceEnabled())
                  log.trace("soft reference cache size is now: " + softRefCacheSize);

               checkSoftReferenceDepth = true;
            }

            long now = System.currentTimeMillis();
            
            // Don't try to soften too often
            if (softenNoMoreOftenThanMillis > 0 && (now - lastSoften < softenNoMoreOftenThanMillis))
               checkSoftReferenceDepth = false;

            // Is it a while since we last softened?
            else if (softenAtLeastEveryMillis > 0 && (now - lastSoften > softenAtLeastEveryMillis))
               checkSoftReferenceDepth = true;

            // Should we check for softening
            if (checkSoftReferenceDepth)
            {
               checkSoftReferenceDepth = validateSoftReferenceDepth();

               // Did the softening complete?
               if (checkSoftReferenceDepth == false)
                  lastSoften = now;
            }
         }
      }
      catch (InterruptedException e)
      {
         // Signal to exit the thread.
      }
      catch (Throwable t)
      {
         log.error("Message Cache Thread Stopped: ", t);
      }
      log.debug("Thread exiting.");
   }

   /**
    * This method is in charge of determining if it time to convert some
    * hard references over to soft references.
    */
   boolean validateSoftReferenceDepth() throws JMSException
   {
      boolean trace = log.isTraceEnabled();
      
      // Loop until softening is not required or we find a message we can soften
      while (getState() == ServiceMBeanSupport.STARTED)
      {
         MessageReference messageToSoften = null;

         synchronized (lruCache)
         {
            // howmany to change over to soft refs
            int softenCount = 0;
            int hardCount = getHardRefCacheSize();
            int softCount = getSoftRefCacheSize();

            // Only soften down to a minimum
            if (hardCount <= minimumHard)
               return false;

            long currentMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            if (currentMem > highMemoryMark)
            {
               // we need to get more aggresive... how much?? lets get
               // a mesurment from 0 to 1
               float severity = ((float) (currentMem - highMemoryMark)) / (maxMemoryMark - highMemoryMark);
               severity = Math.min(severity, 1.0F);
               if (trace)
                  log.trace("Memory usage serverity=" + severity);
               int totalMessageInMem = hardCount + softCount;
               int howManyShouldBeSoft = (int) ((totalMessageInMem) * severity);
               softenCount = howManyShouldBeSoft - softCount;
            }

            // Are there too many messages in memory?
            if (maximumHard > 0)
            {
               int removeCount = hardCount - maximumHard; 
               if (removeCount > 0 && removeCount > softenCount)
                  softenCount = removeCount;
            }
            
            // We can only do so much, somebody else is using all the memory?
            if (softenCount > hardCount)
            {
               if (trace)
                  log.trace("Soften count " + softenCount + " greater than hard references " + hardCount);
               softenCount = hardCount;
            }

            // Ignore soften counts of 1 since this will happen too often even
            // if the serverity is low since it will round up.
            if (softenCount > 1 ||  (maximumHard > 0 && hardCount > maximumHard))
            {
               if (trace)
                  log.trace("Need to soften " + softenCount + " messages");
               Node node = lruCache.getLeastRecent();
               messageToSoften = (MessageReference) node.data;
            }
         }

         // No softening required
         if (messageToSoften == null)
            return false;

         synchronized (messageToSoften)
         {
            // Soften unless it was removed
            if (messageToSoften.messageCache != null && messageToSoften.stored != MessageReference.REMOVED)
            {
               messageToSoften.makeSoft();
               if (messageToSoften.stored == MessageReference.STORED)
               {
                  softenedSize++;
                  return true;
               }
               else if (messageToSoften.isPersistent())
               {
                  // Avoid going into a cpu loop if there are persistent
                  // messages just about to be persisted
                  return false;
               }
            }
            else if (trace)
               log.trace("not softening removed message " + messageToSoften);
         }
      }
      return false;
   }

   /**
    * This gets called when a MessageReference is de-referenced.
    * We will pop it to the top of the RLU
    */
   void messageReferenceUsedEvent(MessageReference mh, boolean wasHard) throws JMSException
   {
      synchronized (mh)
      {
         synchronized (lruCache)
         {
            if (wasHard)
               lruCache.makeMostRecent(mh);
            else
            {
               lruCache.addMostRecent(mh);
            }
         }
      }

      if (wasHard == false)
         checkSoftReferenceDepth = true;
   }

   //////////////////////////////////////////////////////////////////////////////////
   // Perisitence methods used by the MessageReference.
   //////////////////////////////////////////////////////////////////////////////////
   SpyMessage loadFromStorage(MessageReference mh) throws JMSException
   {
      return cacheStore.loadFromStorage(mh);
   }

   void saveToStorage(MessageReference mh, SpyMessage message) throws JMSException
   {
      cacheStore.saveToStorage(mh, message);
   }

   void removeFromStorage(MessageReference mh) throws JMSException
   {
      cacheStore.removeFromStorage(mh);
   }

   //////////////////////////////////////////////////////////////////////////////////
   //
   // The following section deals the the JMX interface to manage the Cache
   //
   //////////////////////////////////////////////////////////////////////////////////

   /**
    * This gets called to start the cache service. Synch. by start
    */
   protected void startService() throws Exception
   {
      setupCacheStore();

      referenceSoftner = new Thread(this, "JBossMQ Cache Reference Softner");
      referenceSoftner.setDaemon(true);
      referenceSoftner.start();
   }

   protected void setupCacheStore() throws Exception
   {
      cacheStore = (CacheStore) getServer().getAttribute(cacheStoreName, "Instance");
   }

   /**
    * This gets called to stop the cache service.
    */
   protected void stopService()
   {
      synchronized (lruCache)
      {
         referenceSoftner.interrupt();
         referenceSoftner = null;
      }
      cacheStore = null;
   }

   /**
    * Gets the hardRefCacheSize
    * @return Returns a int
    *
    * @jmx.managed-attribute
    */
   public int getHardRefCacheSize()
   {
      synchronized (lruCache)
      {
         return lruCache.size();
      }
   }

   /**
    * The <code>getSoftenedSize</code> method
    *
    * @return a <code>long</code> value
    *
    * @jmx.managed-attribute
    */
   public long getSoftenedSize()
   {
      return softenedSize;
   }

   /**
    * Gets the softRefCacheSize
    * @return Returns a int
    *
    * @jmx.managed-attribute
    */
   public int getSoftRefCacheSize()
   {
      return softRefCacheSize;
   }

   /**
    * Gets the totalCacheSize
    * @return Returns a int
    *
    * @jmx.managed-attribute
    */
   public int getTotalCacheSize()
   {
      return totalCacheSize;
   }

   /**
    * Gets the cacheMisses
    * @return Returns a int
    *
    * @jmx.managed-attribute
    */
   public long getCacheMisses()
   {
      return cacheMisses;
   }

   /**
    * Gets the cacheHits
    * @return Returns a long
    *
    * @jmx.managed-attribute
    */
   public long getCacheHits()
   {
      return cacheHits;
   }

   /**
    * Gets whether to make soft references
    *
    * @jmx.managed-attribute
    * @return true when making soft references
    */
   public boolean getMakeSoftReferences()
   {
      return makeSoftReferences;
   }
   
   /**
    * Sets whether to make soft references
    * 
    * @jmx.managed-attribute
    * @param true to make soft references
    */
   public void setMakeSoftReferences(boolean makeSoftReferences)
   {
      this.makeSoftReferences = makeSoftReferences;
   }

   /**
    * Gets the minimum number of hard messages
    *
    * @jmx.managed-attribute
    * @return the minimum number of hard messages
    */
   public int getMinimumHard()
   {
      return minimumHard;
   }
   
   /**
    * Sets the minimum number of hard messages
    * 
    * @jmx.managed-attribute
    * @param minimumHard the minimum number of hard messages
    */
   public void setMinimumHard(int minimumHard)
   {
      if (minimumHard < 1)
         this.minimumHard = 1;
      else
         this.minimumHard = minimumHard;
   }

   /**
    * Gets the maximum number of hard messages
    *
    * @jmx.managed-attribute
    * @return the minimum number of hard messages
    */
   public int getMaximumHard()
   {
      return maximumHard;
   }
   
   /**
    * Sets the maximum number of hard messages
    * 
    * @jmx.managed-attribute
    * @param maximumHard the maximum number of hard messages
    */
   public void setMaximumHard(int maximumHard)
   {
      if (maximumHard < 0)
         this.maximumHard = 0;
      else
         this.maximumHard = maximumHard;
   }

   /**
    * Gets the length of time to wait before checking whether we should soften
    *
    * @jmx.managed-attribute
    * @return the time to wait
    */
   public long getSoftenWaitMillis()
   {
      return softenWaitMillis;
   }
   
   /**
    * Sets the length of time to wait before checking whether we should soften
    * 
    * @jmx.managed-attribute
    * @param millis the time to wait in millis
    */
   public void setSoftenWaitMillis(long millis)
   {
      if (millis < 1000)
         softenWaitMillis = 1000;
      else
         softenWaitMillis = millis;
   }

   /**
    * Gets the minimum length between softening checks
    *
    * @jmx.managed-attribute
    * @return the time to wait
    */
   public long getSoftenNoMoreOftenThanMillis()
   {
      return softenNoMoreOftenThanMillis;
   }
   
   /**
    * Sets the minimum length between softening checks
    * 
    * @jmx.managed-attribute
    * @param wait the time between checks
    */
   public void setSoftenNoMoreOftenThanMillis(long millis)
   {
      if (millis < 0)
         softenNoMoreOftenThanMillis = 0;
      else
         softenNoMoreOftenThanMillis = millis;
   }

   /**
    * Gets the maximum length between softening checks
    *
    * @jmx.managed-attribute
    * @return the time
    */
   public long getSoftenAtLeastEveryMillis()
   {
      return softenAtLeastEveryMillis;
   }
   
   /**
    * Sets the minimum length between softening checks
    * 
    * @jmx.managed-attribute
    * @param wait the time between checks
    */
   public void setSoftenAtLeastEveryMillis(long millis)
   {
      if (millis < 0)
         softenAtLeastEveryMillis = 0;
      else
         softenAtLeastEveryMillis = millis;
   }

   /**
    * Gets the highMemoryMark
    * @return Returns a long
    *
    * @jmx.managed-attribute
    */
   public long getHighMemoryMark()
   {
      return highMemoryMark / ONE_MEGABYTE;
   }
   /**
    * Sets the highMemoryMark
    * @param highMemoryMark The highMemoryMark to set
    *
    * @jmx.managed-attribute
    */
   public void setHighMemoryMark(long highMemoryMark)
   {
      if (highMemoryMark > 0)
         this.highMemoryMark = highMemoryMark * ONE_MEGABYTE;
      else
         this.highMemoryMark = 0;
   }

   /**
    * Gets the maxMemoryMark
    * @return Returns a long
    *
    * @jmx.managed-attribute
    */
   public long getMaxMemoryMark()
   {
      return maxMemoryMark / ONE_MEGABYTE;
   }

   /**
    * Sets the maxMemoryMark
    * @param maxMemoryMark The maxMemoryMark to set
    *
    * @jmx.managed-attribute
    */
   public void setMaxMemoryMark(long maxMemoryMark)
   {
      if (maxMemoryMark > 0)
         this.maxMemoryMark = maxMemoryMark * ONE_MEGABYTE;
      else
         this.maxMemoryMark = 0; 
   }

   /**
    * Gets the CurrentMemoryUsage
    * @return Returns a long
    *
    * @jmx.managed-attribute
    */
   public long getCurrentMemoryUsage()
   {
      return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / ONE_MEGABYTE;
   }

   /**
    * @see ServiceMBeanSupport#getName()
    */
   public String getName()
   {
      return "MessageCache";
   }

   /**
    * @see MessageCacheMBean#setCacheStore(ObjectName)
    *
    * @jmx.managed-attribute
    */
   public void setCacheStore(ObjectName cacheStoreName)
   {
      this.cacheStoreName = cacheStoreName;
   }

   /**
    * The <code>getCacheStore</code> method
    *
    * @return an <code>ObjectName</code> value
    *
    * @jmx.managed-attribute
    */
   public ObjectName getCacheStore()
   {
      return cacheStoreName;
   }

   /**
    * This class implements a simple, efficient LRUCache.  It is pretty much a
    * cut down version of the code in org.jboss.pool.cache.LeastRecentlyUsedCache
    */
   class LRUCache
   {
      int currentSize = 0;
      //maps objects to their nodes
      HashMap map = new HashMap();
      Node mostRecent = null;
      Node leastRecent = null;
      public void addMostRecent(Object o)
      {
         Node newNode = new Node();
         newNode.data = o;
         //insert into map
         Object oldNode = map.put(o, newNode);
         if (oldNode != null)
         {
            map.put(o, oldNode);
            throw new RuntimeException("Can't add object '" + o + "' to LRUCache that is already in cache.");
         }
         //insert into linked list
         if (mostRecent == null)
         {
            //first element
            mostRecent = newNode;
            leastRecent = newNode;
         }
         else
         {
            newNode.lessRecent = mostRecent;
            mostRecent.moreRecent = newNode;
            mostRecent = newNode;
         }
         ++currentSize;
      }
      // Not used anywhere!!
      public void addLeastRecent(Object o)
      {
         Node newNode = new Node();
         newNode.data = o;
         //insert into map
         Object oldNode = map.put(o, newNode);
         if (oldNode != null)
         {
            map.put(o, oldNode);
            throw new RuntimeException("Can't add object '" + o + "' to LRUCache that is already in cache.");
         }
         //insert into linked list
         if (leastRecent == null)
         {
            //first element
            mostRecent = newNode;
            leastRecent = newNode;
         }
         else
         {
            newNode.moreRecent = leastRecent;
            leastRecent.lessRecent = newNode;
            leastRecent = newNode;
         }
         ++currentSize;
      }
      public void remove(Object o)
      {
         //remove from map
         Node node = (Node) map.remove(o);
         if (node == null)
            throw new RuntimeException("Can't remove object '" + o + "' that is not in cache.");
         //remove from linked list
         Node more = node.moreRecent;
         Node less = node.lessRecent;
         if (more == null)
         { //means node is mostRecent
            mostRecent = less;
            if (mostRecent != null)
            {
               mostRecent.moreRecent = null; //Mark it as beeing at the top of tree
            }
         }
         else
         {
            more.lessRecent = less;
         }
         if (less == null)
         { //means node is leastRecent
            leastRecent = more;
            if (leastRecent != null)
            {
               leastRecent.lessRecent = null; //Mark it last in tree
            }
         }
         else
         {
            less.moreRecent = more;
         }
         --currentSize;
      }
      public void makeMostRecent(Object o)
      {
         //get node from map
         Node node = (Node) map.get(o);
         if (node == null)
            throw new RuntimeException("Can't make most recent object '" + o + "' that is not in cache.");
         //reposition in linked list, first remove
         Node more = node.moreRecent;
         Node less = node.lessRecent;
         if (more == null) //means node is mostRecent
            return;
         else
            more.lessRecent = less;
         if (less == null) //means node is leastRecent
            leastRecent = more;
         else
            less.moreRecent = more;
         //now add back in at most recent position
         node.lessRecent = mostRecent;
         node.moreRecent = null; //We are at the top
         mostRecent.moreRecent = node;
         mostRecent = node;
      }
      public int size()
      {
         return currentSize;
      }
      public Node getMostRecent()
      {
         return mostRecent;
      }
      public Node getLeastRecent()
      {
         return leastRecent;
      }
   }

   static class Node
   {
      Node moreRecent = null;
      Node lessRecent = null;
      Object data = null;
   }
}
