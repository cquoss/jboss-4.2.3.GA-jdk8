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
package org.jboss.messaging.channel.plugins.handler;

import org.jboss.messaging.interfaces.*;
import org.jboss.messaging.interfaces.Consumer;
import org.jboss.messaging.interfaces.MessageReference;

/**
 * An abstract channel handler
 * 
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public abstract class AbstractChannelHandler implements ChannelHandler
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   /** The message set */
   protected MessageSet messages;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   /**
    * Create a new AbstractChannelHandler.
    *
    * @param messages the messages
    */
   public AbstractChannelHandler(MessageSet messages)
   {
      this.messages = messages;
      messages.setConsumer(this);
   }
   
   // Public --------------------------------------------------------
   
   // Consumer implementation ---------------------------------------

   public boolean accepts(MessageReference reference, boolean active)
   {
      // We accept all messages
      return true;
   }
   
   public void onMessage(MessageReference reference)
   {
      Consumer consumer;
      messages.lock();
      try
      {
         consumer = findConsumer(reference);
      }
      finally
      {
         messages.unlock();
      }

      if (consumer != null)
         consumer.onMessage(reference);
   }
   
   // ChannelHandler implementation ---------------------------------

   public void addMessage(MessageReference reference)
   {
      Consumer consumer;
      messages.lock();
      try
      {
         consumer = findConsumer(reference);
         if (consumer == null)
            messages.add(reference);
      }
      finally
      {
         messages.unlock();
      }

      if (consumer != null)
         consumer.onMessage(reference);
   }
   
   public MessageReference removeMessage(Consumer consumer)
   {
      messages.lock();
      try
      {
         return messages.remove(consumer);
      }
      finally
      {
         messages.unlock();
      }
   }

   public void waitMessage(Consumer consumer, long wait)
   {
      MessageReference message;
      messages.lock();
      try
      {
         message = messages.remove(consumer);
         // Nothing found, wait
         if (message == null)
            addConsumer(consumer, wait);
      }
      finally
      {
         messages.unlock();
      }
      
      // We found a message, deliver it
      if (message != null)
         consumer.onMessage(message);
   }
   
   public void stopWaitMessage(Consumer consumer)
   {
      messages.lock();
      try
      {
         removeConsumer(consumer);
      }
      finally
      {
         messages.unlock();
      }
   }
   
   // Protected -----------------------------------------------------
   
   /**
    * Add a consumer 
    * 
    * @param consumer the consumer to wait for a message
    * @param wait the length of time to wait
    */
   protected abstract void addConsumer(Consumer consumer, long wait);
   
   /**
    * Remove a consumer 
    * 
    * @param consumer the consumer to remove
    */
   protected abstract void removeConsumer(Consumer consumer);
   
   /**
    * Find a consumer for a message 
    * 
    * @param reference the message
    * @return the consumer or null if there are none for the message
    */
   protected abstract Consumer findConsumer(MessageReference reference);
   
   // Package Private -----------------------------------------------

   // Private -------------------------------------------------------

   // Inner Classes -------------------------------------------------
}
