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
package org.jboss.mq.il.jvm;

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
import org.jboss.mq.Recoverable;
import org.jboss.mq.SpyDestination;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.TransactionRequest;
import org.jboss.mq.il.Invoker;
import org.jboss.mq.il.ServerIL;

/**
 * The JVM implementation of the ServerIL object
 *
 * @author    Hiram Chirino (Cojonudo14@hotmail.com)
 * @author    Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author    <a href="pra@tim.se">Peter Antman</a>
 * @author    <a href="adrian@jboss.org">Adrian Brock</a>
 * @version   $Revision: 57198 $
 * @created   August 16, 2001
 */
public class JVMServerIL implements ServerIL, Recoverable
{
   //The server implementation
   private final Invoker server;

   /**
    * Constructor for the JVMServerIL object
    *
    * @param s  Description of Parameter
    */
   public JVMServerIL(Invoker s)
   {
      if (s == null)
         throw new IllegalArgumentException("JMSServer must be non null in constructor");
      server = s;
   }

   public void setConnectionToken(ConnectionToken newConnectionToken)
   {
      // We cannot try to cache the token since this IL is stateless
   }

   public void setEnabled(ConnectionToken dc, boolean enabled) throws Exception
   {
      server.setEnabled(dc, enabled);
   }

   public String getID() throws JMSException
   {
      return server.getID();
   }

   public TemporaryQueue getTemporaryQueue(ConnectionToken dc) throws JMSException
   {
      return server.getTemporaryQueue(dc);
   }

   public TemporaryTopic getTemporaryTopic(ConnectionToken dc) throws JMSException
   {
      return server.getTemporaryTopic(dc);
   }

   public ServerIL cloneServerIL()
   {
      return this;
   }

   public void addMessage(ConnectionToken dc, SpyMessage val) throws JMSException
   {
      server.addMessage(dc, val.myClone());
   }

   public Topic createTopic(ConnectionToken dc, String dest) throws JMSException
   {
      return server.createTopic(dc, dest);
   }

   public Queue createQueue(ConnectionToken dc, String dest) throws JMSException
   {
      return server.createQueue(dc, dest);
   }

   public void deleteTemporaryDestination(ConnectionToken dc, SpyDestination dest) throws JMSException
   {
      server.deleteTemporaryDestination(dc, dest);
   }

   public void checkID(String ID) throws JMSException
   {
      server.checkID(ID);
   }

   public void connectionClosing(ConnectionToken dc) throws JMSException
   {
      server.connectionClosing(dc);
   }

   public void acknowledge(ConnectionToken dc, AcknowledgementRequest item) throws Exception
   {
      server.acknowledge(dc, item);
   }

   public SpyMessage[] browse(ConnectionToken dc, Destination dest, String selector) throws Exception
   {
      return server.browse(dc, dest, selector);
   }

   public SpyMessage receive(ConnectionToken dc, int subscriberId, long wait) throws Exception
   {
      SpyMessage message = server.receive(dc, subscriberId, wait);
      //copy message to avoid server side problems with persisted message if message is edited client side.
      if (message != null)
         message = message.myClone();
      return message;
   }

   public void unsubscribe(ConnectionToken dc, int subscriptionId) throws Exception
   {
      server.unsubscribe(dc, subscriptionId);
   }

   public void destroySubscription(ConnectionToken dc, DurableSubscriptionID id) throws Exception
   {
      server.destroySubscription(dc, id);
   }

   public String checkUser(String userName, String password) throws JMSException
   {
      return server.checkUser(userName, password);
   }

   public String authenticate(String userName, String password) throws JMSException
   {
      return server.authenticate(userName, password);
   }

   public void subscribe(ConnectionToken dc, org.jboss.mq.Subscription s) throws Exception
   {
      server.subscribe(dc, s.myClone());
   }

   public void transact(org.jboss.mq.ConnectionToken dc, TransactionRequest t) throws JMSException
   {
      server.transact(dc, t);
   }

   public void ping(org.jboss.mq.ConnectionToken dc, long clientTime) throws JMSException
   {
      server.ping(dc, clientTime);
   }

   public Xid[] recover(ConnectionToken dc, int flags) throws Exception
   {
      if (server instanceof Recoverable)
      {
         Recoverable recoverable = (Recoverable) server;
         return recoverable.recover(dc, flags);
      }
      throw new IllegalStateException("Invoker does not implement recoverable " + server);
   }
}
