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

/**
 * @author Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class DurableSubscriptionID implements Serializable
{
   // Constants -----------------------------------------------------
   
   /** The serialVersionUID */
   private static final long serialVersionUID = 2293499797647000970L;
   
   // Attributes ----------------------------------------------------

   /** The clientID */
   String clientID;
   /** The subscriptionName */
   String subscriptionName;
   /** The selector */
   String selector;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
    * Create a new DurableSubscriptionID
    *
    * @param id the client id
    * @param subName the subscription name
    * @param selector the selector
    */
   
   public DurableSubscriptionID(String id, String subName, String selector)
   {
      this.clientID = id;
      this.subscriptionName = subName;
      this.selector = selector;
   }
   
   // Public --------------------------------------------------------
   
   /**
    * Set the client id
    *
    * @param newClientID the client id
    */
   public void setClientID(String newClientID)
   {
      clientID = newClientID;
   }

   /**
    * Set the subscription name
    *
    * @param newSubscriptionName the subscription name
    */
   public void setSubscriptionName(java.lang.String newSubscriptionName)
   {
      subscriptionName = newSubscriptionName;
   }

   /**
    * Get the client id
    *
    * @return the client id
    */
   public String getClientID()
   {
      return clientID;
   }

   /**
    * Get the subscription name
    *
    * @return the subscription name
    */
   public String getSubscriptionName()
   {
      return subscriptionName;
   }
   /**
    * Gets the selector.
    * 
    * @return the selector
    */
   public String getSelector()
   {
      if (selector == null || selector.trim().length() == 0)
         return null;
      return selector;
   }

   /**
    * Sets the selector.
    * 
    * @param selector The selector to set
    */
   public void setSelector(String selector)
   {
      this.selector = selector;
   }
   
   // Object overrides ----------------------------------------------
   
   public boolean equals(Object obj)
   {
      try
      {
         DurableSubscriptionID o = (DurableSubscriptionID) obj;
         return o.clientID.equals(clientID) && o.subscriptionName.equals(subscriptionName);
      }
      catch (Throwable e)
      {
         return false;
      }
   }
   
   public int hashCode()
   {
      return Integer.MIN_VALUE + clientID.hashCode() + subscriptionName.hashCode();
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append("DurableSubscription[clientId=").append(clientID);
      buffer.append(" name=").append(subscriptionName);
      buffer.append(" selector=").append(selector);
      buffer.append(']');
      return buffer.toString();
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}