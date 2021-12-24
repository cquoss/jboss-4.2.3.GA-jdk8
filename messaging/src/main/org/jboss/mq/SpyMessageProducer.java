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

import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;

import org.jboss.logging.Logger;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;

/**
 * This class implements javax.jms.MessageProducer
 * 
 * @author Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class SpyMessageProducer implements MessageProducer
{
   /** The log */
   static Logger log = Logger.getLogger(SpyMessageProducer.class);

   /** Is trace enabled */
   static boolean trace = log.isTraceEnabled();

   /** The session */
   protected SpySession session;
   /** The destination */
   protected Destination destination;
   /** The defaultDeliveryMode */
   protected int defaultDeliveryMode = SpyMessage.DEFAULT_DELIVERY_MODE;
   /** The defaultPriority */
   protected int defaultPriority = SpyMessage.DEFAULT_PRIORITY;
   /** The defaultTTL */
   protected long defaultTTL = SpyMessage.DEFAULT_TIME_TO_LIVE;
   /** Whether we are closed */
   private SynchronizedBoolean closed = new SynchronizedBoolean(false);
   /** Whether to disable MessageID generation */
   private boolean disableMessageID = false;
   /** Whether to disable timestamp generation */
   private boolean disableTS = false;

   /**
	 * Create a new SpyMessageProducer
	 * 
	 * @param session the session
	 * @param destination the destination
	 */
   SpyMessageProducer(SpySession session, Destination destination)
   {
      trace = log.isTraceEnabled();
      
      this.session = session;
      this.destination = destination;
      try
      {
         if (destination instanceof TemporaryQueue || destination instanceof TemporaryTopic)
            setDeliveryMode(DeliveryMode.NON_PERSISTENT);
         else
            setDeliveryMode(DeliveryMode.PERSISTENT);
      }
      catch (JMSException ignored)
      {
         log.debug("Ignored error during setDeliveryMode", ignored);
      }
      
      if (trace)
         log.trace("New message producer " + this);
   }

   public void setDisableMessageID(boolean value) throws JMSException
   {
      checkClosed();
      disableMessageID = value;
   }

   public void setDisableMessageTimestamp(boolean value) throws JMSException
   {
      checkClosed();
      disableTS = value;
   }

   public void setDeliveryMode(int deli) throws JMSException
   {
      checkClosed();
      if (deli != DeliveryMode.NON_PERSISTENT && deli != DeliveryMode.PERSISTENT)
         throw new JMSException("Bad DeliveryMode value");
      else
         defaultDeliveryMode = deli;
   }

   public void setPriority(int pri) throws JMSException
   {
      checkClosed();
      if (pri < 0 || pri > 9)
         throw new JMSException("Bad priority value");
      else
         defaultPriority = pri;
   }

   public void setTimeToLive(int timeToLive) throws JMSException
   {
      checkClosed();
      if (timeToLive < 0)
         throw new JMSException("Bad TimeToLive value");
      else
         defaultTTL = timeToLive;
   }

   public void setTimeToLive(long timeToLive) throws JMSException
   {
      checkClosed();
      if (timeToLive < 0)
         throw new JMSException("Bad TimeToLive value");
      else
         defaultTTL = timeToLive;
   }

   public boolean getDisableMessageID() throws JMSException
   {
      checkClosed();
      return disableMessageID;
   }

   public boolean getDisableMessageTimestamp() throws JMSException
   {
      checkClosed();
      return disableTS;
   }

   public int getDeliveryMode() throws JMSException
   {
      checkClosed();
      return defaultDeliveryMode;
   }

   public int getPriority() throws JMSException
   {
      checkClosed();
      return defaultPriority;
   }

   public long getTimeToLive() throws JMSException
   {
      checkClosed();
      return defaultTTL;
   }

   public void close() throws JMSException
   {
      if (closed.set(true))
         return;

      session.removeProducer(this);

      if (trace)
         log.trace("Closed " + this);
   }

   public Destination getDestination() throws JMSException
   {
      checkClosed();
      return destination;
   }

   public void send(Message message) throws JMSException
   {
      if (destination == null)
         throw new UnsupportedOperationException(
         "Not constructed with identifyed destination. Usage of method not allowed");
      send(destination, message, defaultDeliveryMode, defaultPriority, defaultTTL);
   }

   public void send(Destination destination, Message message) throws JMSException
   {
      send(destination, message, defaultDeliveryMode, defaultPriority, defaultTTL);
   }

   public void send(Message message, int deliveryMode, int priority, long ttl) throws JMSException
   {
      if (destination == null)
         throw new UnsupportedOperationException(
         "Not constructed with identifyed destination. Usage of method not allowed");
      send(destination, message, deliveryMode, priority, ttl);
   }

   public void send(Destination destination, Message message, int deliveryMode, int priority, long ttl)
      throws JMSException
   {
      checkClosed();

      if (this.destination != null && this.destination.equals(destination) == false)
         throw new UnsupportedOperationException("Sending to " + destination
               + " not allowed when producer created with " + this.destination);

      if (destination == null || (destination instanceof SpyDestination) == false)
         throw new InvalidDestinationException("Destination is not an instance of SpyDestination " + destination);

      // Encapsulate the message if not a SpyMessage
      SpyMessage sendMessage;
      if ((message instanceof SpyMessage) == false)
         sendMessage = encapsulateMessage(message);
      else
         sendMessage = (SpyMessage) message;

      //Set the header fields
      sendMessage.setJMSDestination(destination);
      sendMessage.setJMSDeliveryMode(deliveryMode);
      long ts = System.currentTimeMillis();
      sendMessage.setJMSTimestamp(ts);
      if (ttl == 0)
         sendMessage.setJMSExpiration(0);
      else
         sendMessage.setJMSExpiration(ttl + ts);
      sendMessage.setJMSPriority(priority);
      String id = session.getNewMessageID();
      sendMessage.setJMSMessageID(id);

      // If we encapsulated the message, update the original message
      if (message != sendMessage)
      {
         message.setJMSDestination(destination);
         message.setJMSDeliveryMode(deliveryMode);
         message.setJMSTimestamp(ts);
         if (ttl == 0)
            message.setJMSExpiration(0);
         else
            message.setJMSExpiration(ttl + ts);
         message.setJMSPriority(priority);
         message.setJMSMessageID(id);
      }

      if (trace)
         log.trace("Sending message " + this + " \n" + sendMessage);
      
      //Send the message.
      session.sendMessage(sendMessage);
   }   
   
   public String toString()
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append("SpyMessageProducer@").append(System.identityHashCode(this));
      buffer.append("[ dest=").append(destination);
      if (defaultDeliveryMode == DeliveryMode.PERSISTENT)
         buffer.append(" delivery=").append("persist");
      else
         buffer.append(" delivery=").append("besteffort");
      buffer.append(" priority=").append(defaultPriority);
      buffer.append(" ttl=").append(defaultTTL);
      buffer.append(" disableMessageID=").append(disableMessageID);
      buffer.append(" disableTS=").append(disableTS);
      buffer.append(" session=").append(session);
      buffer.append(']');
      return buffer.toString();
   }

   protected SpyMessage encapsulateMessage(Message message) throws JMSException
   {
      SpyMessage result;
      if (message instanceof BytesMessage)
      {
         result = MessagePool.getBytesMessage();
         BytesMessage original = (BytesMessage) message;
         original.reset();
         byte[] temp = new byte[1024];
         int bytes = original.readBytes(temp);
         while (bytes != -1)
         {
            ((BytesMessage) result).writeBytes(temp, 0, bytes);
            bytes = original.readBytes(temp);
         }
      }
      else if (message instanceof MapMessage)
      {
         result = MessagePool.getMapMessage();
         MapMessage original = (MapMessage) message;
         for (Enumeration en=original.getMapNames(); en.hasMoreElements();)
         {
            String key = (String) en.nextElement();
            try
            {
               ((MapMessage) result).setObject(key, original.getObject(key));
            }
            catch (JMSException ignored)
            {
               if (trace)
                  log.trace("Unable to copy map entry " + key, ignored);
            }
         }
      }
      else if (message instanceof StreamMessage)
      {
         result = MessagePool.getStreamMessage();
         StreamMessage original = (StreamMessage) message;
         original.reset();
         try
         {
            while (true)
            {
               ((StreamMessage) result).writeObject(original.readObject());
            }
         }
         catch (MessageEOFException expected)
         {
         }
      }
      else if (message instanceof ObjectMessage)
      {
         result = MessagePool.getObjectMessage();
         ((ObjectMessage) result).setObject(((ObjectMessage) message).getObject());
      }
      else if (message instanceof TextMessage)
      {
         result = MessagePool.getTextMessage();
         ((TextMessage) result).setText(((TextMessage) message).getText());
      }
      else
         result = MessagePool.getMessage();

      // Copy headers
      try
      {
         result.setJMSCorrelationID(message.getJMSCorrelationID());
      }
      catch (JMSException e)
      {
         //must be as bytes
         result.setJMSCorrelationIDAsBytes(message.getJMSCorrelationIDAsBytes());
      }
      result.setJMSReplyTo(message.getJMSReplyTo());
      result.setJMSType(message.getJMSType());

      // Copy properties
      for (Enumeration en=message.getPropertyNames(); en.hasMoreElements();)
      {
         String key = (String) en.nextElement();
         try
         {
            result.setObjectProperty(key, message.getObjectProperty(key));
         }
         catch (JMSException ignored)
         {
            if (trace)
               log.trace("Unable to copy property " + key, ignored);
         }
      }

      return result;
   }

   /**
    * Check whether we are closed
    * 
    * @throws IllegalStateException when the session is closed
    */
   protected void checkClosed() throws JMSException
   {
      if (closed.get())
         throw new IllegalStateException("Message producer is closed");
   }
}
