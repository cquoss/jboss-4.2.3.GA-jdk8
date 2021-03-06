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
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.Topic;
import javax.jms.TopicPublisher;

import org.jboss.jms.JMSValidator;
import org.jboss.jms.message.JBossMessage;

/**
 * A producer
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class JBossProducer 
   implements MessageProducer, QueueSender, TopicPublisher
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** The producer delegate */
   private ProducerDelegate delegate;

   /** The default destination for this producer */ 
   private Destination defaultDestination;

   /** The default delivery mode */ 
   private int defaultDeliveryMode = Message.DEFAULT_DELIVERY_MODE;

   /** The default priorty */ 
   private int defaultPriority = Message.DEFAULT_PRIORITY;

   /** The default time to live */ 
   private long defaultTimeToLive = Message.DEFAULT_TIME_TO_LIVE;

   /** The disable message id flag */ 
   private boolean disableMessageID = false;

   /** The disable message timestamp flag */ 
   private boolean disableTimestamp = false;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Create a new JBossProducer
    * 
    * @param delegate the delegate
    * @param destination the destination
    * @throws JMSException for any error
    */
   public JBossProducer(ProducerDelegate delegate, Destination destination)
      throws JMSException
   {
      this.delegate = delegate;
      this.defaultDestination = destination;
   }

   // Public --------------------------------------------------------

   // MessageProducer implementation --------------------------------

	public void close() throws JMSException
	{
      delegate.closing();
      delegate.close();
	}

	public int getDeliveryMode() throws JMSException
	{
      return defaultDeliveryMode;
	}

	public Destination getDestination() throws JMSException
	{
      return defaultDestination;
	}

	public boolean getDisableMessageID() throws JMSException
	{
      return disableMessageID;
	}

	public boolean getDisableMessageTimestamp() throws JMSException
	{
      return disableTimestamp;
	}

	public int getPriority() throws JMSException
	{
      return defaultPriority;
	}

	public long getTimeToLive() throws JMSException
	{
      return defaultTimeToLive;
	}

	public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive)
		throws JMSException
	{
      if (destination == null)
         throw new JMSException("Null destination");
      if (message == null)
         throw new JMSException("Null message");
      JMSValidator.validateDeliveryMode(deliveryMode);
      JMSValidator.validatePriority(priority);
      JMSValidator.validateTimeToLive(timeToLive);

      JBossMessage msg;
      if ((message instanceof JBossMessage))
         msg = (JBossMessage) message;
      else
         msg = delegate.encapsulateMessage(message);

      if (disableMessageID == false)
         msg.generateMessageID();

      if (disableTimestamp == false)
         msg.generateTimestamp();

      msg.setJMSDestination(destination);
      msg.setJMSDeliveryMode(deliveryMode);
      msg.setJMSPriority(priority);
      if (disableTimestamp == false && timeToLive != 0)
         msg.setJMSExpiration(msg.getJMSTimestamp() + timeToLive);

      delegate.send(msg);
	}

	public void send(Destination destination, Message message) throws JMSException
	{
      send(destination, message, defaultDeliveryMode, defaultPriority, defaultTimeToLive);
	}

	public void send(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException
	{
      send(defaultDestination, message, deliveryMode, priority, timeToLive);
	}

	public void send(Message message) throws JMSException
	{
      send(defaultDestination, message, defaultDeliveryMode, defaultPriority, defaultTimeToLive);
	}

	public void setDeliveryMode(int deliveryMode) throws JMSException
	{
      JMSValidator.validateDeliveryMode(deliveryMode);
      this.defaultDeliveryMode = deliveryMode;
	}

	public void setDisableMessageID(boolean value) throws JMSException
	{
      this.disableMessageID = value;
	}

	public void setDisableMessageTimestamp(boolean value) throws JMSException
	{
      this.disableTimestamp = value;
	}

	public void setPriority(int defaultPriority) throws JMSException
	{
      JMSValidator.validatePriority(defaultPriority);
      this.defaultPriority = defaultPriority;
	}

	public void setTimeToLive(long timeToLive) throws JMSException
	{
      JMSValidator.validateTimeToLive(timeToLive);
      this.defaultTimeToLive = timeToLive;
	}

   // QueueReceiver implementation ----------------------------------

   public Queue getQueue() throws JMSException
   {
      return (Queue) getDestination();
   }

   public void send(Queue queue, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException
   {
      send(queue, message, deliveryMode, priority, timeToLive);
   }

   public void send(Queue queue, Message message) throws JMSException
   {
      send(queue, message);
   }

   // TopicPublisher implementation ---------------------------------

	public Topic getTopic() throws JMSException
	{
      return (Topic) getDestination();
	}

	public void publish(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException
	{
      send(defaultDestination, message, deliveryMode, priority, timeToLive);
	}

	public void publish(Message message) throws JMSException
	{
      send(message);
	}

	public void publish(Topic topic, Message message, int deliveryMode, int priority, long timeToLive)
		throws JMSException
	{
      send(topic, message, deliveryMode, priority, timeToLive);
	}

	public void publish(Topic topic, Message message) throws JMSException
	{
      send(topic, message);
	}

   // Protected ------------------------------------------------------

   // Package Private ------------------------------------------------

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------

}
