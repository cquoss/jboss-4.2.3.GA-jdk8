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
package org.jboss.mq;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueSender;

/**
 * This class implements javax.jms.QueueSender
 * 
 * A sender created with a null Queue will now be interpreted as created as an
 * unidentifyed sender and follows the spec in throwing
 * UnsupportedOperationException at the correct places.
 * 
 * @author Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author <a href="pra@tim.se">Peter Antman</a>
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class SpyQueueSender extends SpyMessageProducer implements QueueSender
{
   /**
    * Create a new SpyQueueSender
    *
    * @param session the session
    * @param queue the queue
    */
   SpyQueueSender(SpySession session, Queue queue)
   {
      super(session, queue);
   }

   public Queue getQueue() throws JMSException
   {
      return (Queue) getDestination();
   }

   public void send(Queue queue, Message message) throws JMSException
   {
      send((Destination) queue, message);
   }

   public void send(Queue queue, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException
   {
      send((Destination) queue, message, deliveryMode, priority, timeToLive);
   }
}