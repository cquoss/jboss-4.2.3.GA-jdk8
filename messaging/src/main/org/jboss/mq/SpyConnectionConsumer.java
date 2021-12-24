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

import java.util.ArrayList;
import java.util.LinkedList;

import javax.jms.ConnectionConsumer;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ServerSession;
import javax.jms.ServerSessionPool;

import org.jboss.logging.Logger;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;

/**
 * This class implements javax.jms.ConnectionConsumer
 * 
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 66858 $
 */
public class SpyConnectionConsumer implements ConnectionConsumer, SpyConsumer, Runnable
{
   // Constants -----------------------------------------------------

   /** The log */
   static Logger log = Logger.getLogger(SpyConnectionConsumer.class);

   /** Whether trace is enabled */
   static boolean trace = log.isTraceEnabled();

   /** Delivered once */
   static final Integer ONCE = new Integer(1);
   
   // Attributes ----------------------------------------------------

   /** The connection is the consumer was created with */
   Connection connection;
   /** The destination this consumer will receive messages from */
   Destination destination;
   /** The ServerSessionPool that is implemented by the AS */
   javax.jms.ServerSessionPool serverSessionPool;
   /** The maximum number of messages that a single session will be loaded with. */
   int maxMessages;
   /** This queue will hold messages until they are dispatched to the
   MessageListener */
   LinkedList queue = new LinkedList();
   /** Is the ConnectionConsumer closed? */
   boolean closed = false;
   /** Whether we are waiting for a message */
   boolean waitingForMessage = false;
   /** The subscription info the consumer */
   Subscription subscription = new Subscription();
   /** The "listening" thread that gets messages from destination and queues
   them for delivery to sessions */
   Thread internalThread;
   /** The thread id */
   int id;
   /** The thread id generator */
   static SynchronizedInt threadId = new SynchronizedInt(0);
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------

   /**
    * SpyConnectionConsumer constructor
    * 
    * @param connection the connection
    * @param destination destination
    * @param messageSelector the message selector
    * @param serverSessionPool the server session pool
    * @param maxMessages the maxmimum messages
    * @exception JMSException for any error
    */
   public SpyConnectionConsumer(Connection connection, Destination destination, String messageSelector,
                                ServerSessionPool serverSessionPool, int maxMessages) throws JMSException
   {
      trace = log.isTraceEnabled();
      
      this.connection = connection;
      this.destination = destination;
      this.serverSessionPool = serverSessionPool;
      this.maxMessages = maxMessages;
      if (this.maxMessages < 1)
         this.maxMessages = 1;

      subscription.destination = (SpyDestination) destination;
      subscription.messageSelector = messageSelector;
      subscription.noLocal = false;

      connection.addConsumer(this);
      id = threadId.increment();
      internalThread = new Thread(this, "Connection Consumer for dest " + subscription + " id=" + id);
      internalThread.start();

      if (trace)
         log.trace("New " + this);
   }
   
   // Public --------------------------------------------------------

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
    * @mes the message
    * @throws JMSException for any error
    */
   public void addMessage(SpyMessage mes) throws JMSException
   {
      synchronized (queue)
      {
         if (closed)
         {
            if (trace)
               log.trace("Consumer close nacking message=" + mes.header.jmsMessageID + " " + this);
            log.warn("NACK issued. The connection consumer was closed.");
            connection.send(mes.getAcknowledgementRequest(false));
            return;
         }

         if (trace)
            log.trace("Add message=" + mes.header.jmsMessageID + " " + this);
         
         if (waitingForMessage)
         {
            prepareDelivery(mes);
            queue.addLast(mes);
            queue.notifyAll();
         }
         else
         {
            if (trace)
               log.trace("Consumer not waiting nacking message=" + mes.header.jmsMessageID + " " + this);
            connection.send(mes.getAcknowledgementRequest(false));
         }
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

   // ConnectionConsumer implementation -----------------------------

   public ServerSessionPool getServerSessionPool() throws JMSException
   {
      return serverSessionPool;
   }

   public void close() throws javax.jms.JMSException
   {
      synchronized (queue)
      {
         if (closed)
            return;

         closed = true;
         queue.notifyAll();
      }

      if (trace)
         log.trace("Close " + this);

      if (internalThread != null && !internalThread.equals(Thread.currentThread()))
      {
         try
         {

            if (trace)
               log.trace("Joining thread " + this);
            internalThread.join();
         }
         catch (InterruptedException e)
         {
            if (trace)
               log.trace("Ignoring interrupting while joining thread " + this);
         }
      }
      synchronized (queue)
      {
         if (trace)
            log.trace("Nacking messages on queue " + this);
         try
         {
            while (queue.isEmpty() == false)
            {
               SpyMessage message = (SpyMessage) queue.removeFirst();
               connection.send(message.getAcknowledgementRequest(false));
            }
         }
         catch (Throwable ignore)
         {
            if (trace)
               log.trace("Ignoring error nacking messages in queue " + this, ignore);
         }
         try
         {
            connection.removeConsumer(this);
         }
         catch (Throwable ignore)
         {
            if (trace)
               log.trace("Ignoring error removing consumer from connection " + this, ignore);
         }
      }
   }
   
   // Runnable implementation ---------------------------------------

   public void run()
   {
      ArrayList mesList = new ArrayList();
      try
      {
         outer : while (true)
         {
            synchronized (queue)
            {
               if (closed)
               {
                  if (trace)
                     log.trace("run() closed " + this);
                  break outer;
               }
            }

            for (int i = 0; i < maxMessages; i++)
            {
               SpyMessage mes = connection.receive(subscription, -1); 
               if (mes == null)
               {
                  if (trace)
                     log.trace("run() receivedNoWait got no message" + this);
                  break;
               }
               else
               {
                  if (trace)
                     log.trace("run() receivedNoWait message=" + mes.header.jmsMessageID + " " + this);
                  mesList.add(mes);
               }
            }

            if (mesList.isEmpty())
            {
               SpyMessage mes = null;
               synchronized (queue)
               {
                  mes = connection.receive(subscription, 0);
                  if (mes == null)
                  {
                     waitingForMessage = true;
                     while (queue.isEmpty() && !closed)
                     {
                        if (trace)
                           log.trace("run() waiting for message " + this);
                        try
                        {
                           queue.wait();
                        }
                        catch (InterruptedException e)
                        {
                           if (trace)
                              log.trace("Ignoring interruption waiting for message " + this, e);
                        }
                     }
                     if (closed)
                     {
                        if (trace)
                           log.trace("run() closed while waiting " + this);
                        waitingForMessage = false;
                        break outer;
                     }
                     mes = (SpyMessage) queue.removeFirst();
                     waitingForMessage = false;
                     if (trace)
                        log.trace("run() got message message=" + mes.header.jmsMessageID + " " + this);
                  }
               }
               mesList.add(mes);
            }

            if (trace)
               log.trace("Waiting for serverSesionPool " + this);
            ServerSession serverSession = serverSessionPool.getServerSession();
            SpySession spySession = (SpySession) serverSession.getSession();
            if (trace)
               log.trace("Waited serverSesion=" + serverSession + " session=" + spySession + " " + this);
            
            if (spySession.sessionConsumer == null)
            {
               if (trace)
                  log.trace("Session did not have a set MessageListner " + spySession + " " + this);
            }
            else
            {
               spySession.sessionConsumer.subscription = subscription;
            }

            for (int i = 0; i < mesList.size(); i++)
               spySession.addMessage((SpyMessage) mesList.get(i));

            if (trace)
               log.trace(" Starting the ServerSession=" + serverSession + " " + this);
            serverSession.start();
            mesList.clear();
         }
      }
      catch (Throwable t)
      {
         try
         {
            for (int i = 0; i < mesList.size(); i++)
            {
               SpyMessage msg = (SpyMessage) mesList.get(i);
               connection.send(msg.getAcknowledgementRequest(false));
            }
         }
         catch (Throwable ignore)
         {
            if (trace)
               log.trace("Ignoring error nacking message " + this, ignore);
         }
         try
         {
            close();
         }
         catch (Throwable ignore)
         {
            if (trace)
               log.trace("Ignoring error during close " + this, ignore);
         }
         
         connection.asynchFailure("Connection consumer closing due to error in listening thread" + this, t);
      }
   }   

   // Object overrides ----------------------------------------------

   public String toString()
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append("SpyConnectionConsumer[sub=").append(subscription);
      if (closed)
         buffer.append(" CLOSED");
      buffer.append(" messages=").append(queue.size());
      buffer.append(" waitingForMessage=").append(waitingForMessage);
      if (internalThread != null)
         buffer.append(" internalThread=").append(internalThread);
      buffer.append(" sessionPool=").append(serverSessionPool);
      buffer.append(" connection=").append(connection);
      buffer.append(']');
      return buffer.toString();
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
