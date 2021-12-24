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

import javax.jms.JMSException;

import org.jboss.mq.SpyDestination;
import org.jboss.mq.SpyJMSException;
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
public class JMSQueue extends JMSDestination
{

   public BasicQueue queue;

   public JMSQueue(SpyDestination dest, ClientConsumer temporary, JMSDestinationManager server, BasicQueueParameters parameters) throws JMSException
   {
      super(dest, temporary, server, parameters);

      // If this is a non-temp queue, then we should persist data
      if (temporaryDestination == null)
      {
         Throwable error = null;
         for (int i = 0; i <= parameters.recoveryRetries; ++i)
         {
            // create queue
            queue = new PersistentQueue(server, dest, parameters);

            try
            {
               // restore persistent queue data         
               server.getPersistenceManager().restoreQueue(this, dest);
               
               // done
               break;
            }
            catch (Throwable t)
            {
               if (i < parameters.recoveryRetries)
                  cat.warn("Error restoring queue " + queue + " retries=" + i + " of " + parameters.recoveryRetries, t);
               else
                  error = t;
               try
               {
                  queue.stop();
               }
               catch (Throwable ignored)
               {
                  cat.trace("Ignored error stopping queue " + queue, ignored);
               }
               finally
               {
                  queue = null;
               }
            }
         }
         
         if (error != null)
            SpyJMSException.rethrowAsJMSException("Unable to recover queue " + dest + " retries=" + parameters.recoveryRetries, error);
      }
      else
      {
         // create queue
         queue = new BasicQueue(server, destination.toString(), parameters);
      }

      // create queue message counter
      queue.createMessageCounter(dest.getName(), null, false, false, parameters.messageCounterHistoryDayLimit);
   }

   public void addSubscriber(Subscription sub) throws JMSException
   {
      queue.addSubscriber(sub);
   }

   public void removeSubscriber(Subscription sub)
   {
      queue.removeSubscriber(sub);
   }

   public void nackMessages(Subscription sub)
   {
      queue.nackMessages(sub);
   }

   public void addReceiver(Subscription sub) throws JMSException
   {
      queue.addReceiver(sub);
   }

   public void removeReceiver(Subscription sub)
   {
      queue.removeReceiver(sub);
   }

   public void restoreMessage(MessageReference messageRef)
   {
      try
      {
         SpyMessage spyMessage = messageRef.getMessage();
         updateNextMessageId(spyMessage);
         messageRef.queue = queue;
         queue.restoreMessage(messageRef);
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
         MessageReference messageRef = server.getMessageCache().add(message, queue, MessageReference.STORED);
         queue.restoreMessage(messageRef, txid, type);
      }
      catch (JMSException e)
      {
         cat.error("Could not restore message:", e);
      }
   }

   public SpyMessage[] browse(String selector) throws JMSException
   {
      return queue.browse(selector);
   }

   public String toString()
   {
      return "JMSDestination:" + destination;
   }

   public void acknowledge(org.jboss.mq.AcknowledgementRequest req, Subscription sub, org.jboss.mq.pm.Tx txId)
      throws JMSException
   {
      queue.acknowledge(req, txId);
   }

   public void addMessage(SpyMessage mes, org.jboss.mq.pm.Tx txId) throws JMSException
   {
      //Number the message so that we can preserve order of delivery.
      mes.header.messageId = nextMessageId();
      MessageReference message = server.getMessageCache().add(mes, queue, MessageReference.NOT_STORED);
      queue.addMessage(message, txId);
   }

   public org.jboss.mq.SpyMessage receive(org.jboss.mq.Subscription sub, boolean wait) throws javax.jms.JMSException
   {
      return queue.receive(sub, wait);
   }

   /*
    * @see JMSDestination#isInUse()
    */
   public boolean isInUse()
   {
      return queue.isInUse();
   }

   /*
    * @see JMSDestination#close()
    */
   public void close() throws JMSException
   {
      queue.stop();
      server.getPersistenceManager().closeQueue(this, getSpyDestination());
   }

   /**
    * @see JMSDestination#destroy()
    */
   public void removeAllMessages() throws JMSException
   {
      queue.removeAllMessages();
   }

   /**
    * Get message counter of internal queue
    * 
    * @return MessageCounter[]  internal queue message counter
    */
   public MessageCounter[] getMessageCounter()
   {
      ArrayList array = new ArrayList();

      MessageCounter counter = queue.getMessageCounter();

      if (counter != null)
         array.add(counter);

      return (MessageCounter[]) array.toArray(new MessageCounter[0]);
   }
}
