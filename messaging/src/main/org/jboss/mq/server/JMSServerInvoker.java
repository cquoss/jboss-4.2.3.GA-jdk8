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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;
import org.jboss.mq.AcknowledgementRequest;
import org.jboss.mq.ConnectionToken;
import org.jboss.mq.DurableSubscriptionID;
import org.jboss.mq.Recoverable;
import org.jboss.mq.SpyDestination;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.SpyTopic;
import org.jboss.mq.Subscription;
import org.jboss.mq.TransactionRequest;
import org.jboss.mq.il.Invoker;

/**
 * A pass through JMSServerInvoker.
 *
 * @author <a href="mailto:pra@tim.se">Peter Antman</a>
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class JMSServerInvoker implements Invoker, Recoverable
{
   protected Logger log;
   /**
    * Next invoker in chain.
    */
   protected JMSServerInterceptor nextInterceptor = null;

   public JMSServerInvoker()
   {
      log = Logger.getLogger(this.getClass().getName());
   }

   /**
    * Set next invoker in chain to be called. Is mot often the real JMSServer
    */
   public void setNext(JMSServerInterceptor server)
   {
      this.nextInterceptor = server;
   }

   /**
    * @see JMSServerInterceptor#getNext()
    */
   public JMSServerInterceptor getNext()
   {
      return this.nextInterceptor;
   }

   public ThreadGroup getThreadGroup()
   {
      return nextInterceptor.getThreadGroup();
   }

   public String getID() throws JMSException
   {
      return nextInterceptor.getID();
   }

   public TemporaryTopic getTemporaryTopic(ConnectionToken dc) throws JMSException
   {
      return nextInterceptor.getTemporaryTopic(dc);
   }

   public TemporaryQueue getTemporaryQueue(ConnectionToken dc) throws JMSException
   {
      return nextInterceptor.getTemporaryQueue(dc);
   }

   public void connectionClosing(ConnectionToken dc) throws JMSException
   {
      nextInterceptor.connectionClosing(dc);
   }

   public void checkID(String ID) throws JMSException
   {
      nextInterceptor.checkID(ID);
   }

   public void addMessage(ConnectionToken dc, SpyMessage message) throws JMSException
   {
      nextInterceptor.addMessage(dc, message);
   }

   public Queue createQueue(ConnectionToken dc, String dest) throws JMSException
   {
      return nextInterceptor.createQueue(dc, dest);
   }

   public Topic createTopic(ConnectionToken dc, String dest) throws JMSException
   {
      return nextInterceptor.createTopic(dc, dest);
   }

   public void deleteTemporaryDestination(ConnectionToken dc, SpyDestination dest) throws JMSException
   {
      nextInterceptor.deleteTemporaryDestination(dc, dest);
   }

   public void transact(ConnectionToken dc, TransactionRequest t) throws JMSException
   {
      nextInterceptor.transact(dc, t);
   }

   public void acknowledge(ConnectionToken dc, AcknowledgementRequest item) throws JMSException
   {
      nextInterceptor.acknowledge(dc, item);
   }

   public SpyMessage[] browse(ConnectionToken dc, Destination dest, String selector) throws JMSException
   {
      return nextInterceptor.browse(dc, dest, selector);
   }

   public SpyMessage receive(ConnectionToken dc, int subscriberId, long wait) throws JMSException
   {
      return nextInterceptor.receive(dc, subscriberId, wait);
   }

   public void setEnabled(ConnectionToken dc, boolean enabled) throws JMSException
   {
      nextInterceptor.setEnabled(dc, enabled);
   }

   public void unsubscribe(ConnectionToken dc, int subscriptionId) throws JMSException
   {
      nextInterceptor.unsubscribe(dc, subscriptionId);
   }

   public void destroySubscription(ConnectionToken dc, DurableSubscriptionID id) throws JMSException
   {
      nextInterceptor.destroySubscription(dc, id);
   }

   public String checkUser(String userName, String password) throws JMSException
   {
      return nextInterceptor.checkUser(userName, password);
   }

   public String authenticate(String userName, String password) throws JMSException
   {
      return nextInterceptor.authenticate(userName, password);
   }

   public void subscribe(org.jboss.mq.ConnectionToken dc, org.jboss.mq.Subscription s) throws JMSException
   {
      nextInterceptor.subscribe(dc, s);
   }

   public void ping(ConnectionToken dc, long clientTime) throws JMSException
   {
      nextInterceptor.ping(dc, clientTime);
   }

   public SpyTopic getDurableTopic(DurableSubscriptionID sub) throws JMSException
   {
      return nextInterceptor.getDurableTopic(sub);
   }

   public Subscription getSubscription(ConnectionToken dc, int subscriberId) throws JMSException
   {
      return nextInterceptor.getSubscription(dc, subscriberId);
   }

   public Xid[] recover(ConnectionToken dc, int flags) throws Exception
   {
      JMSServerInterceptor next = nextInterceptor;
      while (next != null && next instanceof Recoverable == false)
         next = next.getNext();
      if (next == null)
         throw new IllegalStateException("No interceptor implements " + Recoverable.class.getName());
      Recoverable recoverable = (Recoverable) next;
      return recoverable.recover(dc, flags);
   }
}
