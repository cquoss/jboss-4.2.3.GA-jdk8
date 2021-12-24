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
package org.jboss.jms.client.p2p;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.jboss.jms.MessageImpl;
import org.jboss.jms.client.ConsumerDelegate;

/**
 * The p2p consumer
 * 
 * @author <a href="mailto:nathan@jboss.org">Nathan Phelps</a>
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class P2PConsumerDelegate
   implements ConsumerDelegate
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private P2PSessionDelegate session = null;
   private MessageListener messageListener = null;
   private Destination destination = null;
   boolean noLocal;
   private boolean waiting = false;
   private Message lastReceivedMessage = null;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public P2PConsumerDelegate(P2PSessionDelegate session, Destination destination, String selector, boolean noLocal)
      throws JMSException
   {
      this.session = session;
      this.destination = destination;
      this.noLocal = noLocal;
   }

   // Public --------------------------------------------------------

   // ConsumerDelegate implementation --------------------------------

	public void close() throws JMSException
	{
	}

	public void closing() throws JMSException
	{
	}

   public Message receive(long timeout) throws JMSException
   {
      Message message = this.lastReceivedMessage;
      if (message == null && timeout != -1)
      {
          this.waiting = true;
          synchronized (this)
          {
              try
              {
                  this.wait(timeout);
              }
              catch (InterruptedException exception){}
          }
          message = this.lastReceivedMessage;
          this.lastReceivedMessage = null;
          this.waiting = false;
      }
      return message;
   }

   public void setMessageListener(MessageListener listener) throws JMSException
   {
      this.messageListener = listener;
   }

   // Protected ------------------------------------------------------

   // Package Private ------------------------------------------------

   boolean deliver(MessageImpl message)
   {
       try
       {
           if (this.noLocal && message.isLocal())
           {
               return false;
           }
           if (message.getJMSDestination() != null)
           {
               if (message.getJMSDestination().equals(this.destination))
               {
                   if (this.messageListener != null)
                   {
                       this.messageListener.onMessage((Message)message.clone());
                       return true;
                   }
                   else
                   {
                       if (this.waiting)
                       {
                           this.lastReceivedMessage = (MessageImpl)message.clone();
                           synchronized(this)
                           {
                               this.notify();
                           }
                           return true;
                       }
                   }
               }
           }
           return false;
       }
       catch (Exception e){}
       return false;
   }

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------

}
