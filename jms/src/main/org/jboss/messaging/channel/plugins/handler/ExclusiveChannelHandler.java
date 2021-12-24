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
 * A channel handler that has only one consumer
 * 
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class ExclusiveChannelHandler extends AbstractChannelHandler
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   /** Set when the consumer is waiting for a message */
   private Consumer consumer;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   /**
    * Create a new ExclusiveChannelHandler.
    *
    * @param messages the message set
    */
   public ExclusiveChannelHandler(MessageSet messages)
   {
      super(messages);
   }
   
   // Public --------------------------------------------------------

   // AbstractChannelHandler overrides ------------------------------
   
   protected void addConsumer(Consumer consumer, long wait)
   {
      this.consumer = consumer;
   }

   protected Consumer findConsumer(MessageReference reference)
   {
      // The messages are checked at addition to the channel
      if (consumer != null)
      {
         Consumer result = consumer;
         consumer = null;
         return result;
      }
      return null;
   }

   protected void removeConsumer(Consumer consumer)
   {
      consumer = null;
   }
   
   // Protected -----------------------------------------------------
   
   // Package Private -----------------------------------------------

   // Private -------------------------------------------------------

   // Inner Classes -------------------------------------------------
}
