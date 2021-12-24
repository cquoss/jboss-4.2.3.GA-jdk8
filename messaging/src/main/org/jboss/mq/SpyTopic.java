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

import javax.jms.Topic;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;

/**
 * This class implements javax.jms.Topic
 * 
 * @author Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author David Maplesden (David.Maplesden@orion.co.nz)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class SpyTopic extends SpyDestination implements Serializable, Topic, Referenceable
{
   // Constants -----------------------------------------------------
   
   /** The serialVersionUID */
   static final long serialVersionUID = -4784950783387129468L;
   
   // Attributes ----------------------------------------------------
   
   /** The durableSubscriptionID */
   protected DurableSubscriptionID durableSubscriptionID;

   /** added cached toString string for efficiency */
   private String toStringStr;
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   /**
    * Create a new SpyTopic
    *
    * @param topicName the topic name
    */
   public SpyTopic(String topicName)
   {
      super(topicName);
      toStringStr = "TOPIC." + name;
   }

   /**
    * Create a new SpyTopic
    *
    * @param topic the topic 
    * @param clientID the client id
    * @param subscriptionName the subscription name
    * @param selector the selector
    */
   public SpyTopic(SpyTopic topic, String clientID, String subscriptionName, String selector)
   {
      this(topic, new DurableSubscriptionID(clientID, subscriptionName, selector));
   }

   /**
    * Create a new SpyTopic
    *
    * @param topic the topic
    * @param subid the durable subscription
    */
   public SpyTopic(SpyTopic topic, DurableSubscriptionID subid)
   {
      super(topic.getTopicName());
      if (subid == null)
         toStringStr = "TOPIC." + name;
      else
         toStringStr = "TOPIC." + name + "." + subid;
      this.durableSubscriptionID = subid;
   }
   
   // Public --------------------------------------------------------

   /**
    * Get the durable subscription id
    * 
    * return the durable subscription id
    */
   public DurableSubscriptionID getDurableSubscriptionID()
   {
      return durableSubscriptionID;
   }
   
   // Topic implementation ------------------------------------------

   public String getTopicName()
   {
      return name;
   }
   
   // Referenceable implementation ----------------------------------
   public Reference getReference() throws javax.naming.NamingException
   {
      return new Reference("org.jboss.mq.SpyTopic", new StringRefAddr("name", name),
            "org.jboss.mq.referenceable.SpyDestinationObjectFactory", null);
   }
   
   // Object overrides ----------------------------------------------

   public boolean equals(Object obj)
   {
      if (!(obj instanceof SpyTopic))
         return false;
      if (obj.hashCode() != hash)
         return false;
      return ((SpyDestination) obj).name.equals(name);
   }
   
   public String toString()
   {
      return toStringStr;
   }
   
   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}