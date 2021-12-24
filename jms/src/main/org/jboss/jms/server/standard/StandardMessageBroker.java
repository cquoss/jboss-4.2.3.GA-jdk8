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
package org.jboss.jms.server.standard;

import org.jboss.jms.destination.JBossDestination;
import org.jboss.jms.message.JBossMessage;
import org.jboss.jms.server.BrowserEndpointFactory;
import org.jboss.jms.server.DeliveryEndpointFactory;
import org.jboss.jms.server.MessageBroker;
import org.jboss.jms.server.MessageReference;
import org.jboss.jms.server.list.memory.MemoryMessageList;

/**
 * The standard message broker
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class StandardMessageBroker
   implements MessageBroker
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** The message list */
   private MemoryMessageList list = new MemoryMessageList();

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // MessageBroker implementation ----------------------------------

   public BrowserEndpointFactory getBrowserEndpointFactory(JBossDestination destination, String selector)
   {
      return new QueueBrowserEndpointFactory(list, selector);
   }

   public DeliveryEndpointFactory getDeliveryEndpointFactory(JBossDestination destination)
   {
      return new QueueDeliveryEndpointFactory(this, list);
   }

   public MessageReference getMessageReference(JBossMessage message)
   {
      return new StandardMessageReference(message);
   }

   // Protected ------------------------------------------------------

   // Package Private ------------------------------------------------

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------

}
