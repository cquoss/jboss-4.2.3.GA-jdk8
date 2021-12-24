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

import javax.jms.JMSException;

import org.jboss.logging.Logger;
import org.jboss.mq.SpyDestination;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.Subscription;
import org.jboss.mq.pm.Tx;

/**
 *  This class is a message queue which is stored (hashed by Destination) on the
 *  JMS provider
 *
 * @author     Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author     Hiram Chirino (Cojonudo14@hotmail.com)
 * @author     David Maplesden (David.Maplesden@orion.co.nz)
 * @author     Adrian Brock (adrian@jboss.com)
 * @created    August 16, 2001
 * @version    $Revision: 57198 $
 */
public abstract class JMSDestination
{
   //the Destination of this queue
   SpyDestination destination;
   //If this is a temporaryDestination, temporaryDestination=ClientConsumer of the owner, otherwise it's null
   ClientConsumer temporaryDestination;
   //The JMSServer object
   JMSDestinationManager server;

   //Counter used to number incomming messages. (Used to order the messages.)
   long nextMessageIdCounter = 0;
   Object nextMessageIdLock = new Object();

   static long nextSharedMessageIdCounter = 0;
   static Object nextSharedMessageIdLock = new Object();

   /** The basic queue parameters */
   public BasicQueueParameters parameters;

   static Logger cat = Logger.getLogger(JMSDestination.class);

   JMSDestination(SpyDestination dest, ClientConsumer temporary, JMSDestinationManager server, BasicQueueParameters parameters) throws JMSException
   {
      destination = dest;
      temporaryDestination = temporary;
      this.server = server;
      this.parameters = parameters;
   }

   public SpyDestination getSpyDestination()
   {
      return destination;
   }

   public abstract void addSubscriber(Subscription sub) throws JMSException;

   public abstract void removeSubscriber(Subscription sub) throws JMSException;

   public abstract void nackMessages(Subscription sub) throws JMSException;

   public abstract SpyMessage receive(Subscription sub, boolean wait) throws JMSException;

   public abstract void addReceiver(Subscription sub) throws JMSException;

   public abstract void removeReceiver(Subscription sub);

   public abstract void restoreMessage(MessageReference message);

   public void restoreMessage(SpyMessage message)
   {
      restoreMessage(message, null, Tx.UNKNOWN);
   }
   
   /**
    * Restore a message
    * 
    * @param message the message
    * @param tx any transaction
    * @param type the type of restoration
    */
   public abstract void restoreMessage(SpyMessage message, Tx tx, int type);

   public abstract boolean isInUse();

   public abstract void close() throws JMSException;
   public abstract void removeAllMessages() throws JMSException;

   /**
    * @param  req                         org.jboss.mq.AcknowledgementRequest
    * @param  sub                         org.jboss.mq.Subscription
    * @param  txId                        org.jboss.mq.pm.Tx
    * @exception  javax.jms.JMSException  The exception description.
    */
   public abstract void acknowledge(
      org.jboss.mq.AcknowledgementRequest req,
      org.jboss.mq.Subscription sub,
      org.jboss.mq.pm.Tx txId)
      throws javax.jms.JMSException;

   /**
    * @param  mes                         org.jboss.mq.SpyMessage
    * @param  txId                        org.jboss.mq.pm.Tx
    * @exception  javax.jms.JMSException  The exception description.
    */
   public abstract void addMessage(org.jboss.mq.SpyMessage mes, org.jboss.mq.pm.Tx txId) throws javax.jms.JMSException;

   public abstract MessageCounter[] getMessageCounter();

   protected static long nextSharedMessageId()
   {
      synchronized (nextSharedMessageIdLock)
      {
         return nextSharedMessageIdCounter++;
      }
   }

   protected static void updateSharedNextMessageId(SpyMessage message)
   {
      synchronized (nextSharedMessageIdLock)
      {
         nextSharedMessageIdCounter = Math.max(nextSharedMessageIdCounter, message.header.messageId+1);
      }
   }

   protected long nextMessageId()
   {
      if (parameters.lateClone)
         return nextSharedMessageId();

      synchronized (nextMessageIdLock)
      {
         return nextMessageIdCounter++;
      }
   }

   protected void updateNextMessageId(SpyMessage message)
   {
      if (parameters.lateClone)
      {
         updateSharedNextMessageId(message);
         return;
      }

      synchronized (nextMessageIdLock)
      {
         nextMessageIdCounter = Math.max(nextMessageIdCounter, message.header.messageId+1);
      }
   }
}
