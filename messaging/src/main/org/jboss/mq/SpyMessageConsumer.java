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

import java.util.LinkedList;

import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.jboss.logging.Logger;
import org.jboss.util.UnreachableStatementException;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;

/**
 * This class implements <tt>javax.jms.MessageConsumer</tt>.
 * 
 * @author Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author David Maplesden (David.Maplesden@orion.co.nz)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 74987 $
 */
public class SpyMessageConsumer implements MessageConsumer, SpyConsumer, Runnable
{
   /** The log */
   static Logger log = Logger.getLogger(SpyMessageConsumer.class);

   /** Is trace enabled */
   static boolean trace = log.isTraceEnabled();

   /** Delivered once */
   static final Integer ONCE = new Integer(1);
   
   /** Link to my session */
   public SpySession session;
   /** The subscription structure should be fill out by the descendent */
   public Subscription subscription = new Subscription();
   /** Are we closed ? */
   private SynchronizedBoolean closed = new SynchronizedBoolean(false);
   /** The state lock */
   protected Object stateLock = new Object();
   /** Are we receiving a message */
   protected boolean receiving = false;
   /** Are we waiting for a message */
   protected boolean waitingForMessage = false;
   /** Are we listening */
   protected boolean listening = false;
   /** The listener thread */
   protected Thread listenerThread = null;
   /** My message listener (null if none) */
   MessageListener messageListener;
   /** List of Pending messages (not yet delivered) */
   LinkedList messages;
   /** Is this a session consumer? */
   boolean sessionConsumer;

   /**
	 * Create a new SpyMessageConsumer
	 * 
	 * @param s the session
	 * @param sessionConsumer true for a session consumer, false otherwise
	 */
   SpyMessageConsumer(SpySession s, boolean sessionConsumer)
   {
      trace = log.isTraceEnabled();

      session = s;
      this.sessionConsumer = sessionConsumer;
      messageListener = null;
      messages = new LinkedList();

      if (trace)
         log.trace("New message consumer " + this);
   }

   /**
    * Create a new SpyMessageConsumer
    * 
    * @param s the session
    * @param sessionConsumer true for a session consumer, false otherwise
    * @param destination the destination
    * @param selector the selector
    * @param noLocal true for noLocal, false otherwise
    */
   SpyMessageConsumer(SpySession s, boolean sessionConsumer, SpyDestination destination, String selector, boolean noLocal) throws InvalidSelectorException
   {
      trace = log.isTraceEnabled();

      session = s;
      this.sessionConsumer = sessionConsumer;
      subscription.destination = destination;
      subscription.messageSelector = selector;
      subscription.noLocal = noLocal;

      // If the selector is set, try to build it, throws an
      // InvalidSelectorException
      // if it is not valid.
      if (subscription.messageSelector != null)
         subscription.getSelector();
      
      messageListener = null;
      messages = new LinkedList();

      if (trace)
         log.trace("New message consumer " + this);
   }

   /**
    * Get the subscription
    * 
    * @return the subscription
    */
   public Subscription getSubscription()
   {
      return subscription;
   }

   /**
    * Add a message 
    * 
    * @param message the message to add
    * @throws JMSException for any error
    */
   public void addMessage(SpyMessage message) throws JMSException
   {
      if (isClosed())
      {
         if (trace)
            log.trace("WARNING: NACK issued. message=" + message.header.jmsMessageID + 
                      " The message consumer was closed. " + this);
         session.connection.send(message.getAcknowledgementRequest(false));
         return;
      }

      //Add a message to the queue

      //  Consider removing this test (subscription.accepts). I don't think it
      // will ever fail
      //  because the test is also done by the server before message is even
      // sent.
      if (subscription.accepts(message.header))
      {
         if (sessionConsumer)
            sessionConsumerProcessMessage(message);
         else
         {
            synchronized (messages)
            {
               if (waitingForMessage)
               {
                  if (trace)
                     log.trace("Adding message=" + message.header.jmsMessageID + " " + this);
                  messages.addLast(message);
                  messages.notifyAll();
               }
               else
               {
                  //unwanted message (due to consumer receive timing out) Nack
                  // it.
                  if (trace)
                     log.trace("WARNING: NACK issued. message=" + message.header.jmsMessageID + 
                           " The message consumer was not waiting for a message. " + this);
                  session.connection.send(message.getAcknowledgementRequest(false));
               }
            }
         }
      }
      else
      {
         if (trace)
            log.trace("WARNING: NACK issued. message=" + message.header.jmsMessageID + 
                  " The subscription did not accept the message. " + this);
         session.connection.send(message.getAcknowledgementRequest(false));
      }
   }

   /**
	 * Restarts the processing of the messages in case of a recovery
	 */
   public void restartProcessing()
   {
      synchronized (messages)
      {
         if (trace)
            log.trace("Restarting processing " + this);
         messages.notifyAll();
      }
   }

   public void setMessageListener(MessageListener listener) throws JMSException
   {
      checkClosed();

      synchronized (stateLock)
      {
         if (receiving)
            throw new JMSException("Another thread is already in receive.");

         if (trace)
            log.trace("Set message listener=" + listener + " old listener=" + messageListener + " " + this);

         boolean oldListening = listening;
         listening = (listener != null);
         messageListener = listener;

         if (!sessionConsumer && listening && !oldListening)
         {
            //Start listener thread (if one is not already running)
            if (listenerThread == null)
            {
               listenerThread = new Thread(this, "MessageListenerThread - " + subscription.destination.getName());
               listenerThread.start();
            }
         }
      }
   }

   public String getMessageSelector() throws JMSException
   {
      checkClosed();
      return subscription.messageSelector;
   }

   public MessageListener getMessageListener() throws JMSException
   {
      checkClosed();
      return messageListener;
   }

   public Message receive() throws JMSException
   {
      checkClosed();
      synchronized (stateLock)
      {
         if (receiving)
            throw new JMSException("Another thread is already in receive.");
         if (listening)
            throw new JMSException("A message listener is already registered");
         receiving = true;
         
         if (trace)
            log.trace("receive() " + this);
      }

      try
      {
         synchronized (messages)
         {
            //see if we have any undelivered messages before we go to the JMS
            //server to look.
            Message message = getMessage();
            if (message != null)
            {
               if (trace)
                  log.trace("receive() message in list " + message.getJMSMessageID() + " " + this);
               return message;
            }
            
            // Loop through expired messages
            while (true)
            {
               SpyMessage msg = session.connection.receive(subscription, 0);
               if (msg != null)
               {
                  Message mes = preProcessMessage(msg);
                  if (mes != null)
                  {
                     if (trace)
                        log.trace("receive() message from server " + mes.getJMSMessageID() + " " + this);
                     return mes;
                  }
               }
               else
                  break;
            }

            if (trace)
               log.trace("No message in receive(), waiting " + this);
            
            try
            {
               waitingForMessage = true;
               while (true)
               {
                  if (isClosed())
                  {
                     if (trace)
                        log.trace("Consumer closed in receive() " + this);
                     return null;
                  }
                  Message mes = getMessage();
                  if (mes != null)
                  {
                     if (trace)
                        log.trace("receive() message from list after wait " + this);
                     return mes;
                  }
                  messages.wait();
               }
            }
            catch (Throwable t)
            {
               SpyJMSException.rethrowAsJMSException("Receive interupted", t);
               throw new UnreachableStatementException();
            }
            finally
            {
               waitingForMessage = false;
            }
         }
      }
      finally
      {
         synchronized (stateLock)
         {
            receiving = false;
         }
      }
   }

   public Message receive(long timeOut) throws JMSException
   {
      if (timeOut == 0)
      {
         if (trace)
            log.trace("Timeout is zero in receive(long) using receive() " + this);
         return receive();
      }

      checkClosed();
      synchronized (stateLock)
      {
         if (receiving)
            throw new JMSException("Another thread is already in receive.");
         if (listening)
            throw new JMSException("A message listener is already registered");
         receiving = true;
         
         if (trace)
            log.trace("receive(long) " + this);
      }

      long endTime = System.currentTimeMillis() + timeOut;
      
      if (trace)
         log.trace("receive(long) endTime=" + endTime + " " + this);
      
      try
      {
         synchronized (messages)
         {
            //see if we have any undelivered messages before we go to the JMS
            //server to look.
            Message message = getMessage();
            if (message != null)
            {
               if (trace)
                  log.trace("receive(long) message in list " + message.getJMSMessageID() + " " + this);
               return message;
            }
            // Loop through expired messages
            while (true)
            {
               SpyMessage msg = session.connection.receive(subscription, timeOut);
               if (msg != null)
               {
                  Message mes = preProcessMessage(msg);
                  if (mes != null)
                  {
                     if (trace)
                        log.trace("receive(long) message from server " + mes.getJMSMessageID() + " " + this);
                     return mes;
                  }
               }
               else
                  break;
            }

            if (trace)
               log.trace("No message in receive(), waiting " + this);
            
            try
            {
               waitingForMessage = true;
               while (true)
               {
                  if (isClosed())
                  {
                     if (trace)
                        log.trace("Consumer closed in receive(long) " + this);
                     return null;
                  }

                  Message mes = getMessage();
                  if (mes != null)
                  {
                     if (trace)
                        log.trace("receive(long) message from list after wait " + this);
                     return mes;
                  }

                  long att = endTime - System.currentTimeMillis();
                  if (att <= 0)
                  {
                     if (trace)
                        log.trace("receive(long) timed out endTime=" + endTime + " " + this);
                     return null;
                  }

                  messages.wait(att);
               }
            }
            catch (Throwable t)
            {
               SpyJMSException.rethrowAsJMSException("Receive interupted", t);
               throw new UnreachableStatementException();
            }
            finally
            {
               waitingForMessage = false;
            }
         }
      }
      finally
      {
         synchronized (stateLock)
         {
            receiving = false;
         }
      }
   }

   public Message receiveNoWait() throws JMSException
   {
      checkClosed();
      synchronized (stateLock)
      {
         if (receiving)
            throw new JMSException("Another thread is already in receive.");
         if (listening)
            throw new JMSException("A message listener is already registered");
         receiving = true;
         
         if (trace)
            log.trace("receiveNoWait() " + this);
      }

      try
      {
         //see if we have any undelivered messages before we go to the JMS
         //server to look.
         synchronized (messages)
         {
            Message mes = getMessage();
            if (mes != null)
            {
               if (trace)
                  log.trace("receiveNoWait() message in list " + mes.getJMSMessageID() + " " + this);
               return mes;
            }
         }
         // Loop through expired messages
         while (true)
         {
            SpyMessage msg = session.connection.receive(subscription, -1);
            if (msg != null)
            {
               Message mes = preProcessMessage(msg);
               if (mes != null)
               {
                  if (trace)
                     log.trace("receiveNoWait() message from server " + mes.getJMSMessageID() + " " + this);
                  return mes;
               }
            }
            else
            {
               if (trace)
                  log.trace("receiveNoWait() no message " + this);
               return null;
            }
         }
      }
      finally
      {
         synchronized (stateLock)
         {
            receiving = false;
         }
      }
   }

   public void close() throws JMSException
   {
      synchronized (messages)
      {
         if (closed.set(true))
            return;

         if (trace)      
            log.trace("Message consumer closing. " + this);
         
         while (messages.isEmpty() == false)
         {
            SpyMessage mes = (SpyMessage) messages.removeFirst();
            if (trace)
               log.trace("close() nacking undelivered message mes=" + mes.getJMSMessageID() + " " + this); 
            try
            {
               session.connection.send(mes.getAcknowledgementRequest(false));
            }
            catch (Exception e)
            {
               log.debug("Error nacking message: " + mes.getJMSMessageID(), e);
            }
         }
         messages.notifyAll();
      }
      
      // Notification to break out of delivery lock loop
      session.interruptDeliveryLockWaiters();

      if (listenerThread != null && !Thread.currentThread().equals(listenerThread))
      {
         try
         {
            if (trace)      
               log.trace("Joining listener thread. " + this);
            listenerThread.join();
         }
         catch (InterruptedException e)
         {
         }
      }

      if (!sessionConsumer)
      {
         session.removeConsumer(this);
      }

      if (trace)      
         log.trace("Closed. " + this);
   }

   public void run()
   {
      SpyMessage mes = null;
      try
      {
         outer : while (true)
         {
            //get Message
            while (mes == null)
            {
               synchronized (messages)
               {
                  if (isClosed())
                  {
                     waitingForMessage = false;
                     if (trace)
                        log.trace("Consumer closed in run() " + this);
                     break outer;
                  }
                  if (messages.isEmpty())
                     mes = session.connection.receive(subscription, 0);
                  if (mes == null)
                  {
                     waitingForMessage = true;
                     if (trace)
                        log.trace("waiting in run() " + this);
                     while ((messages.isEmpty() && isClosed() == false) || (!session.running))
                     {
                        try
                        {
                           messages.wait();
                        }
                        catch (InterruptedException e)
                        {
                           log.trace("Ignored interruption waiting for messages");
                        }
                     }
                     if (isClosed())
                     {
                        waitingForMessage = false;
                        if (trace)
                           log.trace("Consumer closed while waiting in run() " + this);
                        break outer;
                     }
                     mes = (SpyMessage) messages.removeFirst();
                     waitingForMessage = false;
                  }
                  else
                  {
                     if (trace)
                        log.trace("run() message from server mes=" + mes.getJMSMessageID() + " " + this); 
                  }
               }
               mes.session = session;
            }

            MessageListener thisListener;
            synchronized (stateLock)
            {
               if (!isListening())
               {
                  //send NACK cause we have closed listener
                  if (mes != null)
                  {
                     if (trace)
                        log.trace("run() nacking not listening message mes=" + mes.getJMSMessageID() + " " + this); 
                     session.connection.send(mes.getAcknowledgementRequest(false));
                  }
                  //this thread is about to die, so we will need a new one if
                  // a new listener is added
                  listenerThread = null;
                  mes = null;
                  break;
               }
               thisListener = messageListener;
            }
            Message message = mes;
            if (mes instanceof SpyEncapsulatedMessage)
               message = ((SpyEncapsulatedMessage) mes).getMessage();

            // Try to obtain the session delivery lock
            // This avoids concurrent delivery to message listeners in the same session as per spec
            boolean gotDeliveryLock = false;
            while (gotDeliveryLock == false)
            {
               gotDeliveryLock = session.tryDeliveryLock();
               // We didn't get the lock, check whether we are closing
               if (gotDeliveryLock == false)
               {
                  synchronized (messages)
                  {
                     if (isClosed())
                        break;
                  }
               }
            }
            if (gotDeliveryLock == false)
            {
               if (trace)
                  log.trace("run() nacking didn't get delivery lock mes=" + mes.getJMSMessageID() + " " + this); 
               session.connection.send(mes.getAcknowledgementRequest(false));
            }
            else
            {
               //Handle runtime exceptions. These are handled as per the spec if
               // you assume
               //the number of times erroneous messages are redelivered in
               // auto_acknowledge mode
               //is 0. :)
               try
               {
                  if (session.transacted)
                  {
                     // REVIEW: for an XASession without a transaction this will ack the message
                     // before it has been processed. Plain message listeners
                     // are not supported in a j2ee environment, but what if somebody is trying 
                     // to be clever?
                     if (trace)
                        log.trace("run() acknowledging message in tx mes=" + mes.getJMSMessageID() + " " + this); 
                     session.connection.spyXAResourceManager.ackMessage(session.getCurrentTransactionId(), mes);
                  }

                  try
                  {
                     prepareDelivery((SpyMessage) message);
                     session.addUnacknowlegedMessage((SpyMessage) message);
                     thisListener.onMessage(message);
                  }
                  catch (Throwable t)
                  {
                     log.warn("Message listener " + thisListener + " threw a throwable.", t);
                  }
               }
               finally
               {
                  session.releaseDeliveryLock();
               }

               if (!session.transacted
                     && (session.acknowledgeMode == Session.AUTO_ACKNOWLEDGE || session.acknowledgeMode == Session.DUPS_OK_ACKNOWLEDGE))
               {
                  // Only acknowledge the message if the message wasn't recovered
                  boolean recovered;
                  synchronized (messages)
                  {
                     recovered = messages.contains(message);
                  }
                  if (recovered == false)
                     mes.doAcknowledge();
               }
               mes = null;
            }
         }
      }
      catch (Throwable t)
      {
         log.warn("Message consumer closing due to error in listening thread.", t);
         try
         {
            close();
         }
         catch (Throwable ignore)
         {
         }
         session.asynchFailure("Message consumer closing due to error in listening thread.", t);
      }
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append("SpyMessageConsumer@").append(System.identityHashCode(this));
      buffer.append("[sub=").append(subscription);
      if (isClosed())
         buffer.append(" CLOSED");
      buffer.append(" listening=").append(listening);
      buffer.append(" receiving=").append(receiving);
      buffer.append(" sessionConsumer=").append(sessionConsumer);
      buffer.append(" waitingForMessage=").append(waitingForMessage);
      buffer.append(" messages=").append(messages.size());
      if (listenerThread != null)
         buffer.append(" thread=").append(listenerThread);
      if (messageListener != null)
         buffer.append(" listener=").append(messageListener);
      buffer.append(" session=").append(session);
      buffer.append(']');
      return buffer.toString();
   }

   Message getMessage()
   {
      synchronized (messages)
      {
         if (trace)
            log.trace("Getting message from list " + this);
         while (true)
         {
            try
            {
               if (messages.size() == 0)
                  return null;

               SpyMessage mes = (SpyMessage) messages.removeFirst();

               Message rc = preProcessMessage(mes);
               // could happen if the message has expired.
               if (rc == null)
                  continue;

               return rc;
            }
            catch (Throwable t)
            {
               log.error("Ignoring error", t);
            }
         }
      }
   }

   Message preProcessMessage(SpyMessage message) throws JMSException
   {
      message.session = session;
      session.addUnacknowlegedMessage(message);

      prepareDelivery(message);
      
      // Should we try to ack before the message is processed?
      if (!isListening())
      {
         if (session.transacted)
         {
            if (trace)
               log.trace("preprocess() acking message in tx message=" + message.getJMSMessageID() + " " + this);
            session.connection.spyXAResourceManager.ackMessage(session.getCurrentTransactionId(), message);
         }
         else if (session.acknowledgeMode == Session.AUTO_ACKNOWLEDGE
               || session.acknowledgeMode == Session.DUPS_OK_ACKNOWLEDGE)
         {
            message.doAcknowledge();
         }

         if (message instanceof SpyEncapsulatedMessage)
         {
            return ((SpyEncapsulatedMessage) message).getMessage();
         }
         return message;
      }
      else
      {
         return message;
      }
   }

   /**
    * Prepare the message for delivery
    * 
    * @param message the message
    * @throws JMSException for any error
    */
   void prepareDelivery(SpyMessage message) throws JMSException
   {
      Integer delivery = ONCE;
      Integer redelivery = (Integer) message.header.jmsProperties.get(SpyMessage.PROPERTY_REDELIVERY_COUNT);
      if (redelivery != null)
      {
         int value = redelivery.intValue();
         if (value != 0)
            delivery = new Integer(value + 1);
      }
      message.header.jmsProperties.put(SpyMessage.PROPERTY_DELIVERY_COUNT, delivery);
   }
   
   protected Destination getDestination() throws JMSException
   {
      checkClosed();
      return subscription.destination;
   }

   protected boolean getNoLocal() throws JMSException
   {
      checkClosed();
      return subscription.noLocal;
   }

   /**
	 * Are we listening
	 * 
	 * @return true when listening, false otherwise
	 */
   protected boolean isListening()
   {
      synchronized (stateLock)
      {
         return listening;
      }
   }

   protected void sessionConsumerProcessMessage(SpyMessage message) throws JMSException
   {
      message.session = session;
      //simply pass on to messageListener (if there is one)
      MessageListener thisListener;
      synchronized (stateLock)
      {
         thisListener = messageListener;
      }

      // Add the message to XAResource manager before we call onMessages since
      // the
      // resource may get elisted IN the onMessage method.
      // This gives onMessage a chance to roll the message back.
      Object anonymousTXID = null;
      if (session.transacted)
      {
         // Only happens with XA transactions
         if (session.getCurrentTransactionId() == null)
         {
            anonymousTXID = session.connection.spyXAResourceManager.startTx();
            session.setCurrentTransactionId(anonymousTXID);
         }
         if (trace)
            log.trace("consumer() acking message in tx message=" + message.getJMSMessageID() + " " + this);
         session.connection.spyXAResourceManager.ackMessage(session.getCurrentTransactionId(), message);
      }

      if (thisListener != null)
      {
         Message mes = message;
         if (message instanceof SpyEncapsulatedMessage)
         {
            mes = ((SpyEncapsulatedMessage) message).getMessage();
         }
         session.addUnacknowlegedMessage((SpyMessage) mes);
         if (trace)
            log.trace("consumer() before onMessage=" + message.getJMSMessageID() + " " + this);
         thisListener.onMessage(mes);
         if (trace)
            log.trace("consumer() after onMessage=" + message.getJMSMessageID() + " " + this);
      }

      if (session.transacted)
      {
         // If we started an anonymous tx
         if (anonymousTXID != null)
         {
            if (session.getCurrentTransactionId() == anonymousTXID)
            {
               // We never got enlisted, so just commit the transaction
               try
               {
                  if (trace)
                     log.trace("XASession was not enlisted - Committing work using anonymous xid: " + anonymousTXID);
                  session.connection.spyXAResourceManager.endTx(anonymousTXID, true);
                  session.connection.spyXAResourceManager.commit(anonymousTXID, true);
               }
               catch (Throwable t)
               {
                  log.error("Could not commit", t);
               }
               finally
               {
                  session.unsetCurrentTransactionId(anonymousTXID);
               }
            }
         }
      }
      else
      {
         // Should we Auto-ack the message since the message has now been
         // processesed
         if (session.acknowledgeMode == Session.AUTO_ACKNOWLEDGE
               || session.acknowledgeMode == Session.DUPS_OK_ACKNOWLEDGE)
         {
            message.doAcknowledge();
         }
      }
   }
   
   /**
    * Check whether we are closed
    * 
    * @return true when closed
    */
   private boolean isClosed()
   {
      return closed.get();
   }
   
   /**
    * Check whether we are closed
    * 
    * @throws IllegalStateException when the session is closed
    */
   private void checkClosed() throws IllegalStateException
   {
      if (closed.get())
         throw new IllegalStateException("The consumer is closed");
   }
}
