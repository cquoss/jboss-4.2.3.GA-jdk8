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
package javax.jms;

/** A client uses a <CODE>QueueSender</CODE> object to send messages to a queue.
 * 
 * <P>Normally, the <CODE>Queue</CODE> is specified when a 
 * <CODE>QueueSender</CODE> is created.  In this case, an attempt to use
 * the <CODE>send</CODE> methods for an unidentified 
 * <CODE>QueueSender</CODE> will throw a 
 * <CODE>java.lang.UnsupportedOperationException</CODE>.
 * 
 * <P>If the <CODE>QueueSender</CODE> is created with an unidentified 
 * <CODE>Queue</CODE>, an attempt to use the <CODE>send</CODE> methods that 
 * assume that the <CODE>Queue</CODE> has been identified will throw a
 * <CODE>java.lang.UnsupportedOperationException</CODE>.
 *
 * <P>During the execution of its <CODE>send</CODE> method, a message 
 * must not be changed by other threads within the client. 
 * If the message is modified, the result of the <CODE>send</CODE> is 
 * undefined.
 * 
 * <P>After sending a message, a client may retain and modify it
 * without affecting the message that has been sent. The same message
 * object may be sent multiple times.
 * 
 * <P>The following message headers are set as part of sending a 
 * message: <code>JMSDestination</code>, <code>JMSDeliveryMode</code>, 
 * <code>JMSExpiration</code>, <code>JMSPriority</code>, 
 * <code>JMSMessageID</code> and <code>JMSTimeStamp</code>.
 * When the message is sent, the values of these headers are ignored. 
 * After the completion of the <CODE>send</CODE>, the headers hold the values 
 * specified by the method sending the message. It is possible for the 
 * <code>send</code> method not to set <code>JMSMessageID</code> and 
 * <code>JMSTimeStamp</code> if the 
 * setting of these headers is explicitly disabled by the 
 * <code>MessageProducer.setDisableMessageID</code> or
 * <code>MessageProducer.setDisableMessageTimestamp</code> method.
 *
 * <P>Creating a <CODE>MessageProducer</CODE> provides the same features as
 * creating a <CODE>QueueSender</CODE>. A <CODE>MessageProducer</CODE> object is 
 * recommended when creating new code. The  <CODE>QueueSender</CODE> is
 * provided to support existing code.
 *
 * @see         javax.jms.MessageProducer
 * @see         javax.jms.Session#createProducer(Destination)
 * @see         javax.jms.QueueSession#createSender(Queue)
 */

public interface QueueSender extends MessageProducer
{

   /** Gets the queue associated with this <CODE>QueueSender</CODE>.
    *  
    * @return this sender's queue 
    *  
    * @exception JMSException if the JMS provider fails to get the queue for
    *                         this <CODE>QueueSender</CODE>
    *                         due to some internal error.
    */

   Queue getQueue() throws JMSException;

   /** Sends a message to the queue. Uses the <CODE>QueueSender</CODE>'s 
    * default delivery mode, priority, and time to live.
    *
    * @param message the message to send 
    *  
    * @exception JMSException if the JMS provider fails to send the message 
    *                         due to some internal error.
    * @exception MessageFormatException if an invalid message is specified.
    * @exception InvalidDestinationException if a client uses
    *                         this method with a <CODE>QueueSender</CODE> with
    *                         an invalid queue.
    * @exception java.lang.UnsupportedOperationException if a client uses this
    *                         method with a <CODE>QueueSender</CODE> that did
    *                         not specify a queue at creation time.
    * 
    * @see javax.jms.MessageProducer#getDeliveryMode()
    * @see javax.jms.MessageProducer#getTimeToLive()
    * @see javax.jms.MessageProducer#getPriority()
    */

   void send(Message message) throws JMSException;

   /** Sends a message to the queue, specifying delivery mode, priority, and 
    * time to live.
    *
    * @param message the message to send
    * @param deliveryMode the delivery mode to use
    * @param priority the priority for this message
    * @param timeToLive the message's lifetime (in milliseconds)
    *  
    * @exception JMSException if the JMS provider fails to send the message 
    *                         due to some internal error.
    * @exception MessageFormatException if an invalid message is specified.
    * @exception InvalidDestinationException if a client uses
    *                         this method with a <CODE>QueueSender</CODE> with
    *                         an invalid queue.
    * @exception java.lang.UnsupportedOperationException if a client uses this
    *                         method with a <CODE>QueueSender</CODE> that did
    *                         not specify a queue at creation time.
    */

   void send(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException;

   /** Sends a message to a queue for an unidentified message producer.
    * Uses the <CODE>QueueSender</CODE>'s default delivery mode, priority,
    * and time to live.
    *
    * <P>Typically, a message producer is assigned a queue at creation 
    * time; however, the JMS API also supports unidentified message producers,
    * which require that the queue be supplied every time a message is
    * sent.
    *  
    * @param queue the queue to send this message to
    * @param message the message to send
    *  
    * @exception JMSException if the JMS provider fails to send the message 
    *                         due to some internal error.
    * @exception MessageFormatException if an invalid message is specified.
    * @exception InvalidDestinationException if a client uses
    *                         this method with an invalid queue.
    * 
    * @see javax.jms.MessageProducer#getDeliveryMode()
    * @see javax.jms.MessageProducer#getTimeToLive()
    * @see javax.jms.MessageProducer#getPriority()
    */

   void send(Queue queue, Message message) throws JMSException;

   /** Sends a message to a queue for an unidentified message producer, 
    * specifying delivery mode, priority and time to live.
    *  
    * <P>Typically, a message producer is assigned a queue at creation 
    * time; however, the JMS API also supports unidentified message producers,
    * which require that the queue be supplied every time a message is
    * sent.
    *  
    * @param queue the queue to send this message to
    * @param message the message to send
    * @param deliveryMode the delivery mode to use
    * @param priority the priority for this message
    * @param timeToLive the message's lifetime (in milliseconds)
    *  
    * @exception JMSException if the JMS provider fails to send the message 
    *                         due to some internal error.
    * @exception MessageFormatException if an invalid message is specified.
    * @exception InvalidDestinationException if a client uses
    *                         this method with an invalid queue.
    */

   void send(Queue queue, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException;
}
