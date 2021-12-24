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

import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

/**
 * This class implements <tt>javax.jms.TopicSubscriber</tt>.
 * 
 * @author Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class SpyTopicSubscriber extends SpyMessageConsumer implements TopicSubscriber
{
   /**
	 * Create a new SpyTopicSubscriber
	 * 
	 * @param session the session
	 * @param topic the topic
	 * @param noLocal true for no local, false otherwise
	 * @param selector the selector
	 * @throws InvalidSelectorException for an invalid selector
	 */
   SpyTopicSubscriber(SpySession session, SpyTopic topic, boolean noLocal, String selector)
         throws InvalidSelectorException
   {
      super(session, false, topic, selector, noLocal);
   }

   public Topic getTopic() throws JMSException
   {
      return (Topic) getDestination();
   }

   public boolean getNoLocal() throws JMSException
   {
      return super.getNoLocal();
   }
}