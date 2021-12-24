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
package org.jboss.jms.client;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

/**
 * A consumer
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class JBossConsumer 
   implements MessageConsumer, QueueReceiver, TopicSubscriber
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** The delegate */
   private ConsumerDelegate delegate;

   /** The default destination */
   private Destination defaultDestination;

   /** The message listener */
   private MessageListener listener;

   /** The message selector */
   private String selector;

   /** The no local flag */
   private boolean noLocal;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Create a new JBossBrowser
    * 
    * @param delegate the delegate
    * @param destination the destination
    * @param selector the selector
    * @param noLocal the no local flag
    * @throws JMSException for any error
    */
   public JBossConsumer(ConsumerDelegate delegate, Destination destination, String selector, boolean noLocal)
      throws JMSException
   {
      this.delegate = delegate;
      this.defaultDestination = destination;
      this.selector = selector;
      this.noLocal = noLocal;
   }

   // Public --------------------------------------------------------

   public Destination getDestination() throws JMSException
   {
      return defaultDestination;
   }

   // MessageConsumer implementation --------------------------------

	public void close() throws JMSException
	{
      delegate.closing();
      delegate.close();
	}

	public MessageListener getMessageListener() throws JMSException
	{
      return listener;
	}

	public String getMessageSelector() throws JMSException
	{
      return selector;
	}

	public Message receive() throws JMSException
	{
      return receive(0);
	}

	public Message receive(long timeout) throws JMSException
	{
      return delegate.receive(timeout);
	}

	public Message receiveNoWait() throws JMSException
	{
      return receive(-1);
	}

	public void setMessageListener(MessageListener listener) throws JMSException
	{
      delegate.setMessageListener(listener);
      this.listener = listener;
	}

   // QueueReceiver implementation ----------------------------------

	public Queue getQueue() throws JMSException
	{
      return (Queue) getDestination();
	}

   // TopicSubscriber implementation --------------------------------

	public boolean getNoLocal() throws JMSException
	{
      return noLocal;
	}

	public Topic getTopic() throws JMSException
	{
      return (Topic) getDestination();
	}

   // Protected -----------------------------------------------------

   // Package Private -----------------------------------------------

   // Private -------------------------------------------------------

   // Inner Classes -------------------------------------------------
}
