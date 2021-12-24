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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Topic;
import javax.jms.TopicPublisher;

/**
 * This class implements javax.jms.TopicPublisher
 * 
 * A publisher created with a null Topic will now be interpreted as created as
 * an unidentifyed publisher and follows the spec in throwing
 * UnsupportedOperationException at the correct places.
 * 
 * @author Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author <a href="pra@tim.se">Peter Antman</a>
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class SpyTopicPublisher extends SpyMessageProducer implements TopicPublisher
{
   /**
	 * Create a new SpyTopicPublisher
	 * 
	 * @param s the session
	 * @param t the topic
	 */
   SpyTopicPublisher(SpySession s, Topic t)
   {
      super(s, t);
   }

   public Topic getTopic() throws JMSException
   {
      return (Topic) getDestination();
   }

   public void publish(Message message) throws JMSException
   {
      send(message);
   }

   public void publish(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException
   {
      send(message, deliveryMode, priority, timeToLive);
   }

   public void publish(Topic topic, Message message) throws JMSException
   {
      send(topic, message);
   }

   public void publish(Topic topic, Message message, int deliveryMode, int priority, long timeToLive)
         throws JMSException
   {
      send(topic, message, deliveryMode, priority, timeToLive);
   }
}