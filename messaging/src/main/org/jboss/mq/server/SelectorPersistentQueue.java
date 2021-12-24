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

import org.jboss.mq.SpyTopic;
import org.jboss.mq.pm.Tx;
import org.jboss.mq.selectors.Selector;

/**
 *  This class adds a selector to a persistent queue.
 *
 *  Factored out of JMSTopic.
 *
 * @author     Adrian Brock (Adrian.Brock@HappeningTimes.com)
 * @created    24th October 2002
 */
public class SelectorPersistentQueue
    extends PersistentQueue
{
   // Attributes ----------------------------------------------------

   /**
    * The string representation of the selector
    */
   String selectorString;

   /**
    * The implementation of the selector
    */
   Selector selector;

   // Constructor ---------------------------------------------------

   /**
    * Create a new persistent queue with a selector
    *
    * @param server the destination manager
    * @param dstopic the topic with a durable subscription
    * @param selector the selector string
    * @exception JMSException for an error
    */
   public SelectorPersistentQueue(JMSDestinationManager server, SpyTopic dstopic, String selector, BasicQueueParameters parameters)
      throws JMSException
   {
      super(server, dstopic, parameters);
      this.selectorString = selector;
      this.selector = new Selector(selector);
   }

   // Public --------------------------------------------------------
      	  
   /**
    * Filters the message with the selector before adding to the queue

    * @param mesRef the message
    * @param txId the transaction
    * @exception JMSException for an error
    */
   public void addMessage(MessageReference mesRef, Tx txId)
      throws JMSException
   {
      if (selector.test(mesRef.getHeaders()))
         super.addMessage( mesRef, txId );
      else
         server.getMessageCache().remove(mesRef);
   }
}
