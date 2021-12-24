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

import org.jboss.mq.AcknowledgementRequest;
import org.jboss.mq.ConnectionToken;
import org.jboss.mq.DurableSubscriptionID;
import org.jboss.mq.SpyDestination;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.SpyTopic;
import org.jboss.mq.Subscription;
import org.jboss.mq.TransactionRequest;

/**
 * A pass through Interceptor, wich will trace all calls.
 *
 * @author <a href="mailto:pra@tim.se">Peter Antman</a>
 * @version $Revision: 57198 $
 */
public class TracingInterceptor extends JMSServerInterceptorSupport
{
   public ThreadGroup getThreadGroup()
   {
      if (!log.isTraceEnabled())
      {
         return getNext().getThreadGroup();
      }

      try
      {
         log.trace("CALLED : getThreadGroup");
         return getNext().getThreadGroup();
      }
      finally
      {
         log.trace("RETURN : getThreadGroup");
      }
   }


   public String getID() throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         return getNext().getID();
      }

      try
      {
         log.trace("CALLED : getID");
         return getNext().getID();
      }
      catch (JMSException e)
      {
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : getID: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : getID");
      }
   }

   public TemporaryTopic getTemporaryTopic(ConnectionToken dc) throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         return getNext().getTemporaryTopic(dc);
      }

      try
      {
         log.trace("CALLED : getTemporaryTopic");
         return getNext().getTemporaryTopic(dc);
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : getTemporaryTopic: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : getTemporaryTopic: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : getTemporaryTopic");
      }

   }

   public TemporaryQueue getTemporaryQueue(ConnectionToken dc) throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         return getNext().getTemporaryQueue(dc);
      }

      try
      {
         log.trace("CALLED : getTemporaryQueue");
         return getNext().getTemporaryQueue(dc);
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : getTemporaryQueue: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : getTemporaryQueue: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : getTemporaryQueue");
      }

   }

   public void connectionClosing(ConnectionToken dc) throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         getNext().connectionClosing(dc);
         return;
      }

      try
      {
         log.trace("CALLED : connectionClosing");
         getNext().connectionClosing(dc);
         return;
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : connectionClosing: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : receive: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : connectionClosing");
      }

   }

   public void checkID(String ID) throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         getNext().checkID(ID);
         return;
      }

      try
      {
         log.trace("CALLED : checkID");
         log.trace("ARG    : " + ID);
         getNext().checkID(ID);
         return;
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : checkID: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : checkID: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : checkID");
      }

   }

   public void addMessage(ConnectionToken dc, SpyMessage message) throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         getNext().addMessage(dc, message);
         return;
      }

      try
      {
         log.trace("CALLED : addMessage");
         log.trace("ARG    : " + message);
         getNext().addMessage(dc, message);
         return;
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : addMessage: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : addMessage: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : addMessage");
      }

   }

   public Queue createQueue(ConnectionToken dc, String dest) throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         return getNext().createQueue(dc, dest);
      }

      try
      {
         log.trace("CALLED : createQueue");
         log.trace("ARG    : " + dest);
         return getNext().createQueue(dc, dest);
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : createQueue: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : createQueue: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : createQueue");
      }

   }

   public Topic createTopic(ConnectionToken dc, String dest) throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         return getNext().createTopic(dc, dest);
      }

      try
      {
         log.trace("CALLED : createTopic");
         log.trace("ARG    : " + dest);
         return getNext().createTopic(dc, dest);
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : createTopic: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : createTopic: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : createTopic");
      }
   }

   public void deleteTemporaryDestination(ConnectionToken dc, SpyDestination dest) throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         getNext().deleteTemporaryDestination(dc, dest);
         return;
      }

      try
      {
         log.trace("CALLED : deleteTemporaryDestination");
         log.trace("ARG    : " + dest);
         getNext().deleteTemporaryDestination(dc, dest);
         return;
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : deleteTemporaryDestination: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : deleteTemporaryDestination: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : deleteTemporaryDestination");
      }
   }

   public void transact(ConnectionToken dc, TransactionRequest t) throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         getNext().transact(dc, t);
         return;
      }

      try
      {
         log.trace("CALLED : transact");
         log.trace("ARG    : " + t);
         getNext().transact(dc, t);
         return;
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : transact: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : transact: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : transact");
      }

   }

   public void acknowledge(ConnectionToken dc, AcknowledgementRequest item) throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         getNext().acknowledge(dc, item);
         return;
      }

      try
      {
         log.trace("CALLED : acknowledge");
         log.trace("ARG    : " + item);
         getNext().acknowledge(dc, item);
         return;
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : acknowledge: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : acknowledge: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : acknowledge");
      }

   }

   public SpyMessage[] browse(ConnectionToken dc, Destination dest, String selector) throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         return getNext().browse(dc, dest, selector);
      }

      try
      {
         log.trace("CALLED : browse");
         log.trace("ARG    : " + dest);
         log.trace("ARG    : " + selector);
         return getNext().browse(dc, dest, selector);
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : browse: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : browse: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : browse");
      }

   }

   public SpyMessage receive(ConnectionToken dc, int subscriberId, long wait) throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         return getNext().receive(dc, subscriberId, wait);
      }

      try
      {
         log.trace("CALLED : receive");
         log.trace("ARG    : " + subscriberId);
         log.trace("ARG    : " + wait);
         return getNext().receive(dc, subscriberId, wait);
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : receive: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : receive: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : receive");
      }

   }

   public void setEnabled(ConnectionToken dc, boolean enabled) throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         getNext().setEnabled(dc, enabled);
         return;
      }

      try
      {
         log.trace("CALLED : setEnabled");
         log.trace("ARG    : " + enabled);
         getNext().setEnabled(dc, enabled);
         return;
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : setEnabled: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : setEnabled: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : setEnabled");
      }

   }

   public void unsubscribe(ConnectionToken dc, int subscriptionId) throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         getNext().unsubscribe(dc, subscriptionId);
         return;
      }

      try
      {
         log.trace("CALLED : unsubscribe");
         log.trace("ARG    : " + subscriptionId);
         getNext().unsubscribe(dc, subscriptionId);
         return;
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : unsubscribe: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : unsubscribe: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : unsubscribe");
      }

   }

   public void destroySubscription(ConnectionToken dc, DurableSubscriptionID id) throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         getNext().destroySubscription(dc, id);
         return;
      }

      try
      {
         log.trace("CALLED : destroySubscription");
         log.trace("ARG    : " + id);
         getNext().destroySubscription(dc, id);
         return;
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : destroySubscription: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : destroySubscription: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : destroySubscription");
      }

   }

   public String checkUser(String userName, String password) throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         return getNext().checkUser(userName, password);
      }

      try
      {
         log.trace("CALLED : checkUser");
         log.trace("ARG    : " + userName);
         log.trace("ARG    : (password not shown)");
         return getNext().checkUser(userName, password);
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : checkUser: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : checkUser: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : checkUser");
      }

   }

   public String authenticate(String userName, String password) throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         return getNext().authenticate(userName, password);
      }

      try
      {
         log.trace("CALLED : authenticate");
         return getNext().authenticate(userName, password);
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : authenticate: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : authenticate: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : authenticate");
      }

   }

   public void subscribe(org.jboss.mq.ConnectionToken dc, org.jboss.mq.Subscription s) throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         getNext().subscribe(dc, s);
         return;
      }

      try
      {
         log.trace("CALLED : subscribe");
         log.trace("ARG    : " + s);
         getNext().subscribe(dc, s);
         return;
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : subscribe: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : subscribe: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : subscribe");
      }

   }

   public void ping(ConnectionToken dc, long clientTime) throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         getNext().ping(dc, clientTime);
         return;
      }

      try
      {
         log.trace("CALLED : ping");
         log.trace("ARG    : " + clientTime);
         getNext().ping(dc, clientTime);
         return;
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : ping: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : ping: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : ping");
      }

   }

   public SpyTopic getDurableTopic(DurableSubscriptionID sub) throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         return getNext().getDurableTopic(sub);
      }

      try
      {
         log.trace("CALLED : getDurableTopic");
         log.trace("ARG    : " + sub);
         return getNext().getDurableTopic(sub);
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : getDurableTopic: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : getDurableTopic: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : getDurableTopic");
      }

   }

   public Subscription getSubscription(ConnectionToken dc, int subscriberId) throws JMSException
   {

      if (!log.isTraceEnabled())
      {
         return getNext().getSubscription(dc, subscriberId);
      }

      try
      {
         log.trace("CALLED : getSubscription");
         log.trace("ARG    : " + subscriberId);
         return getNext().getSubscription(dc, subscriberId);
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : getSubscription: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : getSubscription: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : getSubscription");
      }

   }

   public Xid[] recover(ConnectionToken dc, int flags) throws Exception
   {
      if (log.isTraceEnabled() == false)
         return super.recover(dc, flags);

      try
      {
         log.trace("CALLED : recover");
         log.trace("ARG    : " + flags);
         return super.recover(dc, flags);
      }
      catch (JMSException e)
      {
         log.trace("EXCEPTION : recover: ", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.trace("EXCEPTION : recover: ", e);
         throw e;
      }
      finally
      {
         log.trace("RETURN : recover");
      }
   }
}
