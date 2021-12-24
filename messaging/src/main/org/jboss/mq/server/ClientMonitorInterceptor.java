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

import java.util.Iterator;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;

import org.jboss.mq.AcknowledgementRequest;
import org.jboss.mq.ConnectionToken;
import org.jboss.mq.DurableSubscriptionID;
import org.jboss.mq.SpyDestination;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.Subscription;
import org.jboss.mq.TransactionRequest;
import org.jboss.mq.il.jvm.JVMClientIL;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;

/**
 * A pass through Interceptor, which keeps track of when a
 * client was last active.  If a client is inactive for too long,
 * then it is disconnected from the server.<p>
 *
 * This is only necessary for stateless transports like HTTP
 *
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 * @author adrian@jboss.org
 */
public class ClientMonitorInterceptor extends JMSServerInterceptorSupport
{
   //The list of Clients by ConnectionTokens
   ConcurrentReaderHashMap clients = new ConcurrentReaderHashMap();

   private static class ClientStats
   {
      private long lastUsed = System.currentTimeMillis();

      boolean disconnectIfInactive = true;
   }

   public void disconnectInactiveClients(long disconnectTime)
   {
      log.debug("Checking for timedout clients.");
      Iterator i = clients.keySet().iterator();
      while (i.hasNext())
      {
         ConnectionToken dc = (ConnectionToken) i.next();
         ClientStats cs = (ClientStats) clients.get(dc);
         if (cs.disconnectIfInactive && cs.lastUsed < disconnectTime)
         {
            try
            {
               log.debug("Disconnecting client due to inactivity timeout: " + dc);
               connectionClosing(dc);
            }
            catch (Throwable e)
            {
            }
         }
      }
   }

   /**
    * Peek the stats. For testing.
    * 
    * @param dc the connection token
    * @return the stats
    */
   public ClientStats peekClientStats(ConnectionToken dc)
   {
      return (ClientStats) clients.get(dc);
   }

   public ClientStats getClientStats(ConnectionToken dc) throws JMSException
   {
      ClientStats cq = (ClientStats) clients.get(dc);
      if (cq != null)
         return cq;

      // Remove any previous token with a null client id and remove it
      if (dc.getClientID() != null)
      {
         ConnectionToken withoutID = new ConnectionToken(null, dc.clientIL, dc.getSessionId());
         clients.remove(withoutID);
      }

      cq = new ClientStats();

      // The JVM clientil does not ping.
      if (dc.clientIL instanceof JVMClientIL)
         cq.disconnectIfInactive = false;

      clients.put(dc, cq);
      return cq;
   }

   public TemporaryTopic getTemporaryTopic(ConnectionToken dc) throws JMSException
   {
      getClientStats(dc).lastUsed = System.currentTimeMillis();
      return getNext().getTemporaryTopic(dc);
   }

   public TemporaryQueue getTemporaryQueue(ConnectionToken dc) throws JMSException
   {
      getClientStats(dc).lastUsed = System.currentTimeMillis();
      return getNext().getTemporaryQueue(dc);
   }

   public void connectionClosing(ConnectionToken dc) throws JMSException
   {
      clients.remove(dc);
      getNext().connectionClosing(dc);
   }

   public void addMessage(ConnectionToken dc, SpyMessage message) throws JMSException
   {
      getClientStats(dc).lastUsed = System.currentTimeMillis();
      getNext().addMessage(dc, message);
   }

   public Queue createQueue(ConnectionToken dc, String dest) throws JMSException
   {
      getClientStats(dc).lastUsed = System.currentTimeMillis();
      return getNext().createQueue(dc, dest);

   }

   public Topic createTopic(ConnectionToken dc, String dest) throws JMSException
   {
      getClientStats(dc).lastUsed = System.currentTimeMillis();
      return getNext().createTopic(dc, dest);
   }

   public void deleteTemporaryDestination(ConnectionToken dc, SpyDestination dest) throws JMSException
   {
      getClientStats(dc).lastUsed = System.currentTimeMillis();
      getNext().deleteTemporaryDestination(dc, dest);
   }

   public void transact(ConnectionToken dc, TransactionRequest t) throws JMSException
   {
      getClientStats(dc).lastUsed = System.currentTimeMillis();
      getNext().transact(dc, t);

   }

   public void acknowledge(ConnectionToken dc, AcknowledgementRequest item) throws JMSException
   {
      getClientStats(dc).lastUsed = System.currentTimeMillis();
      getNext().acknowledge(dc, item);
   }

   public SpyMessage[] browse(ConnectionToken dc, Destination dest, String selector) throws JMSException
   {
      getClientStats(dc).lastUsed = System.currentTimeMillis();
      return getNext().browse(dc, dest, selector);
   }

   public SpyMessage receive(ConnectionToken dc, int subscriberId, long wait) throws JMSException
   {
      getClientStats(dc).lastUsed = System.currentTimeMillis();
      return getNext().receive(dc, subscriberId, wait);
   }

   public void setEnabled(ConnectionToken dc, boolean enabled) throws JMSException
   {
      getClientStats(dc).lastUsed = System.currentTimeMillis();
      getNext().setEnabled(dc, enabled);
   }

   public void unsubscribe(ConnectionToken dc, int subscriptionId) throws JMSException
   {
      getClientStats(dc).lastUsed = System.currentTimeMillis();
      getNext().unsubscribe(dc, subscriptionId);
   }

   public void destroySubscription(ConnectionToken dc, DurableSubscriptionID id) throws JMSException
   {
      getClientStats(dc).lastUsed = System.currentTimeMillis();
      getNext().destroySubscription(dc, id);
   }

   public void subscribe(org.jboss.mq.ConnectionToken dc, org.jboss.mq.Subscription s) throws JMSException
   {

      getClientStats(dc).lastUsed = System.currentTimeMillis();
      getNext().subscribe(dc, s);

   }

   public void ping(ConnectionToken dc, long clientTime) throws JMSException
   {
      getClientStats(dc).lastUsed = System.currentTimeMillis();
      getNext().ping(dc, clientTime);
   }

   public Subscription getSubscription(ConnectionToken dc, int subscriberId) throws JMSException
   {
      getClientStats(dc).lastUsed = System.currentTimeMillis();
      return getNext().getSubscription(dc, subscriberId);

   }
}
