/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.test.jbossmq.support;

import java.io.IOException;

import javax.jms.Destination;
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
import org.jboss.mq.il.ClientIL;
import org.jboss.mq.il.ServerIL;
import org.jboss.util.id.UID;

/**
 * DummyServerIL.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class DummyServerIL implements ServerIL
{
   private ClientIL clientIL;

   private String when = "";
   
   public void setThrowError(String when)
   {
      this.when = when;
   }
   
   public void setClientIL(ClientIL clientIL)
   {
      this.clientIL = clientIL;
   }
   
   public void acknowledge(ConnectionToken dc, AcknowledgementRequest item) throws Exception
   {
      throw new org.jboss.util.NotImplementedException("acknowledge");
   }

   public void addMessage(ConnectionToken dc, SpyMessage message) throws Exception
   {
      throw new org.jboss.util.NotImplementedException("addMessage");
   }

   public String authenticate(String userName, String password) throws Exception
   {
      return UID.asString();
   }

   public SpyMessage[] browse(ConnectionToken dc, Destination dest, String selector) throws Exception
   {
      throw new org.jboss.util.NotImplementedException("browse");
   }

   public void checkID(String ID) throws Exception
   {
      throw new org.jboss.util.NotImplementedException("checkID");
   }

   public String checkUser(String userName, String password) throws Exception
   {
      throw new org.jboss.util.NotImplementedException("checkUser");
   }

   public ServerIL cloneServerIL() throws Exception
   {
      return this;
   }

   public void connectionClosing(ConnectionToken dc) throws Exception
   {
      throw new org.jboss.util.NotImplementedException("connectionClosing");
   }

   public Queue createQueue(ConnectionToken dc, String dest) throws Exception
   {
      throw new org.jboss.util.NotImplementedException("createQueue");
   }

   public Topic createTopic(ConnectionToken dc, String dest) throws Exception
   {
      throw new org.jboss.util.NotImplementedException("createTopic");
   }

   public void deleteTemporaryDestination(ConnectionToken dc, SpyDestination dest) throws Exception
   {
      throw new org.jboss.util.NotImplementedException("deleteTemporaryDestination");
   }

   public void destroySubscription(ConnectionToken dc, DurableSubscriptionID id) throws Exception
   {
      throw new org.jboss.util.NotImplementedException("destroySubscription");
   }

   public String getID() throws Exception
   {
      return UID.asString();
   }

   public TemporaryQueue getTemporaryQueue(ConnectionToken dc) throws Exception
   {
      throw new org.jboss.util.NotImplementedException("getTemporaryQueue");
   }

   public TemporaryTopic getTemporaryTopic(ConnectionToken dc) throws Exception
   {
      throw new org.jboss.util.NotImplementedException("getTemporaryTopic");
   }

   public void ping(ConnectionToken dc, long clientTime) throws Exception
   {
      clientIL.pong(clientTime);
   }

   public SpyMessage receive(ConnectionToken dc, int subscriberId, long wait) throws Exception
   {
      if ("receive".equals(when))
         throw new IOException("Error in receive");
      return null;
   }

   public void setConnectionToken(ConnectionToken newConnectionToken) throws Exception
   {
      // Nothing
   }

   public void setEnabled(ConnectionToken dc, boolean enabled) throws Exception
   {
      // Nothing
   }

   public void subscribe(ConnectionToken dc, Subscription s) throws Exception
   {
      // Nothing
   }

   public void transact(ConnectionToken dc, TransactionRequest t) throws Exception
   {
      throw new org.jboss.util.NotImplementedException("transact");
      
   }

   public void unsubscribe(ConnectionToken dc, int subscriptionId) throws Exception
   {
      throw new org.jboss.util.NotImplementedException("unsubscribe");
   }
}
