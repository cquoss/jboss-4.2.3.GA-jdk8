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
package org.jboss.mq.server.jmx;

import java.util.Collection;
import java.util.List;

/**
 * MBean interface.
 */
public interface QueueMBean extends DestinationMBean
{
   /**
    * Get the queue name
    * 
    * @return the queue name
    */
   String getQueueName();

   /**
    * Gets the QueueDepth attribute of the BasicQueue object
    * 
    * @return The QueueDepth value
    * @exception Exception Description of Exception    
    */
   int getQueueDepth() throws Exception;

   /**
    * Gets the ScheduledMessageCount attribute of the BasicQueue object
    * 
    * @return The ScheduledMessageCount value
    * @exception Exception Description of Exception    
    */
   int getScheduledMessageCount() throws Exception;

   /**
    * Gets the InprocessMessageCount attribute of the BasicQueue object
    * 
    * @return The ScheduledMessageCount value
    * @exception Exception Description of Exception    
    */
   int getInProcessMessageCount() throws Exception;

   /**
    * Get the number of active receivers
    * 
    * @return the number of receivers
    */
   int getReceiversCount();

   /**
    * List the active receivers
    * 
    * @return the receivers
    */
   List listReceivers();

   /**
    * List the messages
    * 
    * @return the messages
    * @throws Exception for any error
    */
   List listMessages() throws Exception;

   /**
    * List the messages matching a selector
    * 
    * @param selector the selector
    * @return the messages
    * @throws Exception for any error
    */
   List listMessages(String selector) throws Exception;

   /**
    * List the scheduled messages
    * 
    * @return the messages
    * @throws Exception for any error
    */
   List listScheduledMessages() throws Exception;

   /**
    * List the scheduled messages matching a selector
    * 
    * @param selector the selector
    * @return the messages
    * @throws Exception for any error
    */
   List listScheduledMessages(String selector) throws Exception;

   /**
    * List the in process messages
    * 
    * @return the messages
    * @throws Exception for any error
    */
   List listInProcessMessages() throws Exception;

   /**
    * List the in process messages matching a selector
    * 
    * @param selector the selector
    * @return the messages
    * @throws Exception for any error
    */
   List listInProcessMessages(String selector) throws Exception;

   /**
    * Get the number of active subscribers
    * 
    * @return the number of subscribers
    */
   int getSubscribersCount();

   /**
    * List the active subscribers
    * 
    * @return the subscribers
    */
   Collection listSubscribers();
}
