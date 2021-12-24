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

import java.io.Serializable;

import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;

import org.jboss.mq.selectors.Selector;

/**
 * This class contains all the data needed to for a the provider to to
 * determine if a message can be routed to a consumer.
 * 
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author David Maplesden (David.Maplesden@orion.co.nz)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class Subscription implements Serializable
{
   // Constants -----------------------------------------------------

   /** The serialVersionID */
   private static final long serialVersionUID = -4045603824932803577L;
   
   // Attributes ----------------------------------------------------

   /** This gets set to a unique value at the SpyConnection. */
   public int subscriptionId;

   /** The queue we want to subscribe to. */
   public SpyDestination destination;

   /** The selector which will filter out messages. */
   public String messageSelector;

   /** Should this message destroy the subscription? */
   public boolean destroyDurableSubscription;

   /** Topics might not want locally produced messages. */
   public boolean noLocal;

   /** The message selector */
   public transient Selector selector;
   
   /** The connection token */
   public transient ConnectionToken connectionToken;
   
   /** The client consumer */
   public transient Object clientConsumer;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   /**
    * Determines the consumer would accept the message.
    * 
    * @return the selector
    * @throws InvalidSelectorException for an invalid selector
    */
   public Selector getSelector() throws InvalidSelectorException
   {
      if (messageSelector == null || messageSelector.trim().length() == 0)
         return null;

      if (selector == null)
         selector = new Selector(messageSelector);

      return selector;
   }

   /**
    * Determines the consumer would accept the message.
    * 
    * @param header the message header
    * @return true when accepted, false otherwise
    * @throws JMSException for any error
    */
   public boolean accepts(SpyMessage.Header header) throws JMSException
   {
      if (header.jmsDestination instanceof SpyTopic && noLocal && header.producerClientId.equals(connectionToken.getClientID()))
         return false;

      Selector ms = getSelector();
      if (ms != null && !ms.test(header))
         return false;
      return true;
   }

   /**
    * Clone the subscription
    *
    * @return the cloned subscription
    */
   public Subscription myClone()
   {
      Subscription result = new Subscription();
      //only need to clone non-transient fields for our purposes.

      result.subscriptionId = subscriptionId;
      result.destination = destination;
      result.messageSelector = messageSelector;
      result.destroyDurableSubscription = destroyDurableSubscription;
      result.noLocal = noLocal;

      return result;
   }
   
   // Object overrides ----------------------------------------------

   public String toString()
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append("Subscription[subId=").append(subscriptionId);
      if (connectionToken != null)
         buffer.append("connection=").append(connectionToken);
      buffer.append(" destination=").append(destination);
      buffer.append(" messageSelector=").append(messageSelector);
      if (noLocal)
         buffer.append(" NoLocal");
      else
         buffer.append(" Local");
      if (destroyDurableSubscription)
         buffer.append(" Destroy");
      else
         buffer.append(" Create");

      buffer.append(']');
      return buffer.toString();
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------

}