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

import java.util.ArrayList;

import org.jboss.messaging.interfaces.*;
import org.jboss.messaging.interfaces.Consumer;
import org.jboss.messaging.interfaces.MessageReference;

/**
 * A channel handler that has multiple consumers
 * 
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class SharedChannelHandler extends AbstractChannelHandler
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   /** The waiting consumers */
   private ArrayList consumers = new ArrayList();

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   /**
    * Create a new SharedChannelHandler.
    *
    * @param messages the message set
    */
   public SharedChannelHandler(MessageSet messages)
   {
      super(messages);
   }
   
   // Public --------------------------------------------------------

   // AbstractChannelHandler overrides ------------------------------
   
   protected void addConsumer(Consumer consumer, long wait)
   {
      consumers.add(consumer);
   }

   protected Consumer findConsumer(MessageReference reference)
   {
      for (int i = 0; i < consumers.size(); ++i)
      {
         Consumer consumer = (Consumer) consumers.get(i);
         if (consumer.accepts(reference, true))
         {
            consumers.remove(i);
            return consumer;
         }
      }
      return null;
   }

   protected void removeConsumer(Consumer consumer)
   {
      consumers.remove(consumer);
   }
   
   // Protected -----------------------------------------------------
   
   // Package Private -----------------------------------------------

   // Private -------------------------------------------------------

   // Inner Classes -------------------------------------------------
}
