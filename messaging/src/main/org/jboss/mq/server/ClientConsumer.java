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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;

import org.jboss.logging.Logger;
import org.jboss.util.threadpool.ThreadPool;
import org.jboss.mq.AcknowledgementRequest;
import org.jboss.mq.ConnectionToken;
import org.jboss.mq.ReceiveRequest;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.Subscription;

/**
 *  This represent the clients queue which consumes messages from the
 *  destinations on the provider.
 *
 * @author     Hiram Chirino (Cojonudo14@hotmail.com)
 * @author <a href="mailto:pra@tim.se">Peter Antman</a>
 * @created    August 16, 2001
 * @version    $Revision: 71497 $
 */
public class ClientConsumer implements Runnable
{
   private static Logger log = Logger.getLogger(ClientConsumer.class);
   //The JMSServer object
   JMSDestinationManager server;
   //The connection this queue will send messages over
   ConnectionToken connectionToken;
   //Is this connection enabled (Can we transmit to the receiver)
   boolean enabled;
   //Has this connection been closed?
   boolean closed = false;
   //Maps a subscription id to a Subscription
   HashMap subscriptions = new HashMap();
   //Maps a subscription id to a Subscription for subscriptions that have finished receiving
   HashMap removedSubscriptions = new HashMap();

   LinkedList blockedSubscriptions = new LinkedList();

   //List of messages waiting to be transmitted to the client
   private LinkedList messages = new LinkedList();

   /**
    *  Flags that I am enqueued as work on my thread pool.
    */
   private boolean enqueued = false;

   // Static ---------------------------------------------------

   /**
    *  The {@link org.jboss.util.threadpool.ThreadPool} that
    *  does the actual message pushing for us.
    */
   private ThreadPool threadPool = null;

   // Constructor ---------------------------------------------------

   public ClientConsumer(JMSDestinationManager server, ConnectionToken connectionToken) throws JMSException
   {
      this.server = server;
      this.connectionToken = connectionToken;
      this.threadPool = server.getThreadPool();
   }

   public void setEnabled(boolean enabled) throws JMSException
   {
      if (log.isTraceEnabled())
         log.trace("" + this +"->setEnabled(enabled=" + enabled + ")");

      // queues might be waiting for messages.
      synchronized (blockedSubscriptions)
      {
         this.enabled = enabled;
         if (enabled)
         {
            for (Iterator it = blockedSubscriptions.iterator(); it.hasNext();)
            {
               Subscription sub = (Subscription) it.next();
               JMSDestination dest = server.getJMSDestination(sub.destination);
               if (dest != null)
                  dest.addReceiver(sub);
            }
            blockedSubscriptions.clear();
         }
      }
   }

   public void queueMessageForSending(RoutedMessage r)
   {

      synchronized (messages)
      {
         if (closed)
            return; // Wouldn't be delivered anyway

         messages.add(r);
         if (!enqueued)
         {
            enqueued = true;
            boolean interrupted = Thread.interrupted();
            try
            {
               threadPool.run(this);
            }
            catch (Exception e)
            {
               log.debug("Unable to use thread pool for delivering messages, will use the sending thread", e);
               run();
            }
            finally
            {
               if (interrupted)
                  Thread.currentThread().interrupt();
            }
         }
      }
   }

   public void addSubscription(Subscription req) throws JMSException
   {
      if (log.isTraceEnabled())
         log.trace("Adding subscription for: " + req);
      req.connectionToken = connectionToken;
      req.clientConsumer = this;

      JMSDestination jmsdest = server.getJMSDestination(req.destination);
      if (jmsdest == null)
         throw new InvalidDestinationException("The destination " + req.destination + " does not exist !");

      jmsdest.addSubscriber(req);

      synchronized (subscriptions)
      {
         subscriptions.put(new Integer(req.subscriptionId), req);
      }
   }

   public void close()
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("" + this +"->close()");

      synchronized (messages)
      {
         closed = true;
         if (enqueued)
         {
            enqueued = false;
         }
         messages.clear();
      }

      // Remove all the subscriptions for this client
      HashMap subscriptionsClone = null;
      synchronized (subscriptions)
      {
         subscriptionsClone = (HashMap) subscriptions.clone();
      }
      Iterator i = subscriptionsClone.keySet().iterator();
      while (i.hasNext())
      {
         Integer subscriptionId = (Integer) i.next();
         try
         {
            removeSubscription(subscriptionId.intValue());
         }
         catch (JMSException ignore)
         {
         }
      }

      // Nack the removed subscriptions, the connection is gone
      HashMap removedSubsClone = null;
      synchronized (subscriptions)
      {
         removedSubsClone = (HashMap) removedSubscriptions.clone();
      }
      i = removedSubsClone.values().iterator();
      while (i.hasNext())
      {
         Subscription removed = (Subscription) i.next();
         JMSDestination queue = server.getJMSDestination(removed.destination);
         if (queue == null)
            log.warn("The subscription was registered with a destination that does not exist: " + removed);
         else
         {
            try
            {
               queue.nackMessages(removed);
            }
            catch (JMSException e)
            {
               log.warn("Unable to nack removed subscription: " + removed, e);
            }
         }
         // Make sure the subscription is gone
         removeRemovedSubscription(removed.subscriptionId);
      }
   }

   public SpyMessage receive(int subscriberId, long wait) throws JMSException
   {
      Subscription req = getSubscription(subscriberId);
      if (req == null)
      {
         throw new JMSException("The provided subscription does not exist");
      }

      JMSDestination queue = server.getJMSDestination(req.destination);
      if (queue == null)
         throw new InvalidDestinationException("The subscription's destination " + req.destination + " does not exist");

      // Block the receiver if we are not enabled and it is not noWait, otherwise receive a message
      if (addBlockedSubscription(req, wait))
         return queue.receive(req, (wait != -1));

      return null;
   }

   public void removeSubscription(int subscriptionId) throws JMSException
   {
      if (log.isTraceEnabled())
         log.trace("" + this +"->removeSubscription(subscriberId=" + subscriptionId + ")");

      Integer subId = new Integer(subscriptionId);
      Subscription req;
      synchronized (subscriptions)
      {
         req = (Subscription) subscriptions.remove(subId);
         if (req != null)
            removedSubscriptions.put(subId, req);
      }

      if (req == null)
         throw new JMSException("The subscription had not been previously registered");

      JMSDestination queue = server.getPossiblyClosingJMSDestination(req.destination);
      if (queue == null)
         throw new InvalidDestinationException("The subscription was registered with a destination that does not exist !");

      queue.removeSubscriber(req);

   }

   /**
    *  Push some messages.
    */
   public void run()
   {
      try
      {

         ReceiveRequest[] job;

         synchronized (messages)
         {
            if (closed)
               return;

            job = new ReceiveRequest[messages.size()];
            Iterator iter = messages.iterator();
            for (int i = 0; iter.hasNext(); i++)
            {
               RoutedMessage rm = (RoutedMessage) iter.next();
               job[i] = rm.toReceiveRequest();
               iter.remove();
            }
            enqueued = false;
         }

         connectionToken.clientIL.receive(job);

      }
      catch (Throwable t)
      {
         synchronized (messages)
         {
            if (closed)
               log.warn("Could not send messages to a receiver.", t);
            else
               log.trace("Could not send messages to a receiver. It is closed.", t);
         }
         try
         {
            server.connectionFailure(connectionToken);
         }
         catch (Throwable ignore)
         {
            log.warn("Could not close the client connection..", ignore);
         }
      }
   }

   public String toString()
   {
      return "ClientConsumer:" + connectionToken.getClientID();
   }

   public void acknowledge(AcknowledgementRequest request, org.jboss.mq.pm.Tx txId) throws JMSException
   {
      Subscription sub = retrieveSubscription(request.subscriberId);

      if (sub == null)
      {
         //might be in removed subscriptions
         synchronized (subscriptions)
         {
            sub = (Subscription) removedSubscriptions.get(new Integer(request.subscriberId));
         }
      }

      if (sub == null)
      {
         throw new JMSException("The provided subscription does not exist");
      }

      JMSDestination queue = server.getJMSDestination(sub.destination);
      if (queue == null)
         throw new InvalidDestinationException("The subscription's destination " + sub.destination + " does not exist");

      queue.acknowledge(request, sub, txId);
   }

   boolean addBlockedSubscription(Subscription sub, long wait)
   {
      synchronized (blockedSubscriptions)
      {
         if (enabled == false && wait != -1)
            blockedSubscriptions.add(sub);
         return enabled;
      }
   }

   void removeRemovedSubscription(int subId)
   {
      Subscription sub = null;
      synchronized (subscriptions)
      {
         sub = (Subscription) removedSubscriptions.remove(new Integer(subId));
      }
      if (sub != null)
      {
         JMSDestination topic = server.getPossiblyClosingJMSDestination(sub.destination);
         if (topic != null && topic instanceof JMSTopic)
             ((JMSTopic) topic).cleanupSubscription(sub);
      }
   }

   /**
    * Get a subscription for the subscriberid
    *
    * @exception JMSException if it can not find the subscription.
    */
   public Subscription getSubscription(int subscriberId) throws JMSException
   {
      Subscription req = retrieveSubscription(subscriberId);
      if (req == null)
         throw new JMSException("The provided subscription does not exist");

      return req;
   }

   /**
    * Retrieve a subscription for the subscriberid
    */
   private Subscription retrieveSubscription(int subscriberId) throws JMSException
   {
      Integer id = new Integer(subscriberId);
      synchronized (subscriptions)
      {
         return (Subscription) subscriptions.get(id);
      }
   }
}
