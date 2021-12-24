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

import org.jboss.messaging.channel.interfaces.Channel;
import org.jboss.messaging.interfaces.Consumer;
import org.jboss.messaging.interfaces.MessageReference;

/**
 * An abstract channel
 * 
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public abstract class AbstractChannel implements Channel
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** The consumer */
   protected Consumer consumer;
   
   /** The channel handler */
   protected ChannelHandler handler;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Create a new AbstractChannel.
    *
    * @param consumer the consumer
    * @param handler the handler
    */
   public AbstractChannel(Consumer consumer, ChannelHandler handler)
   {
      this.consumer = consumer;
      this.handler = handler;
   }

   // Public --------------------------------------------------------

   // Channel implementation ----------------------------------------

   public void send(MessageReference message)
   {
      handler.addMessage(message);
   }

   public MessageReference receive(long wait)
   {
      // There must be a message immediately available
      if (wait == -1)
         return handler.removeMessage(consumer);

      // Wait for a message
      handler.waitMessage(consumer, wait);
      return null;
   }
   
   public void close()
   {
      handler.stopWaitMessage(consumer);
   }
   
   // Protected -----------------------------------------------------

   // Package Private -----------------------------------------------

   // Private -------------------------------------------------------

   // Inner Classes -------------------------------------------------
}
