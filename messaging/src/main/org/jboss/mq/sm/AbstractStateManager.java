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
package org.jboss.mq.sm;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jms.InvalidClientIDException;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import org.jboss.mq.DurableSubscriptionID;
import org.jboss.mq.SpyTopic;
import org.jboss.mq.server.JMSDestinationManager;
import org.jboss.mq.server.JMSTopic;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.JBossStringBuilder;

/**
 * An abstract baseclass to make it a little bit easier to implement new
 * StateManagers.
 * 
 * <p>Apart from one methods in StateManager subclasses implement the
 * protected abstract callback methods to do its work.
 * 
 * @author <a href="pra@tim.se">Peter Antman </a>
 * @author <a href="Norbert.Lataille@m4x.org">Norbert Lataille </a>
 * @author <a href="hiram.chirino@jboss.org">Hiram Chirino </a>
 * @author <a href="adrian@jboss.com">Adrian Brock </a>
 * @version $Revision: 62298 $
 */

public abstract class AbstractStateManager extends ServiceMBeanSupport
   implements StateManager, AbstractStateManagerMBean
{
   /** The logged on client ids */
   private final Set loggedOnClientIds = new HashSet();

   /**
    * Create a new abstract state manager
    */
   public AbstractStateManager()
   {
   }

   public void setDurableSubscription(JMSDestinationManager server, DurableSubscriptionID sub, SpyTopic topic)
         throws JMSException
   {
      boolean debug = log.isDebugEnabled();
      if (debug)
         log.debug("Checking durable subscription: " + sub + ", on topic: " + topic);

      DurableSubscription subscription = getDurableSubscription(sub);

      // A new subscription
      if (subscription == null)
      {
         if (debug)
            log.debug("The subscription was not previously registered " + sub);
         // Either this was a remove attemt, not to successfull,
         // or it was a bogus request (should then an exception be raised?)
         if (topic == null)
            return;
         // Create the real durable subscription
         JMSTopic dest = (JMSTopic) server.getJMSDestination(topic);
         if (dest == null)
            throw new InvalidDestinationException("Topic does not exist: " + topic);
         dest.createDurableSubscription(sub);

         // Save it
         subscription = new DurableSubscription(sub.getClientID(), sub.getSubscriptionName(), topic.getName(), sub
               .getSelector());
         // Call subclass
         saveDurableSubscription(subscription);
      }
      // An existing subscription...it was previously registered...
      // Check if it is an unsubscribe, or a change of subscription.
      else
      {
         if (debug)
            log.debug("The subscription was previously registered: " + subscription);

         String newSelector = sub.getSelector();
         String oldSelector = subscription.getSelector();
         boolean selectorChanged = false;
         if ((newSelector == null && oldSelector != null)
               || (newSelector != null && newSelector.equals(oldSelector) == false))
            selectorChanged = true;

         // The client wants an unsubscribe
         // TODO: we are not spec compliant since we neither check if
         // the topic has an active subscriber or if there are messages
         // destined for the client not yet acked by the session!!!
         if (topic == null)
         {
            if (debug)
               log.debug("Removing subscription: " + subscription);
            // we have to change the subscription...do physical work
            SpyTopic prevTopic = new SpyTopic(subscription.getTopic());
            JMSTopic dest = (JMSTopic) server.getJMSDestination(prevTopic);
            if (dest == null)
               throw new InvalidDestinationException("Topic does not exist: " + prevTopic);
            // TODO here we should check if the client still has
            // an active consumer

            dest.destroyDurableSubscription(sub);

            //straight deletion, remove subscription - call subclass
            removeDurableSubscription(subscription);
         }
         // The topic previously subscribed to is not the same as the
         // one in the subscription request.
         else if (!subscription.getTopic().equals(topic.getName()) || selectorChanged)
         {
            //new topic so we have to change it
            if (debug)
               log.debug("But the topic or selector was different, changing the subscription.");
            // remove the old sub
            SpyTopic prevTopic = new SpyTopic(subscription.getTopic());
            JMSTopic dest = (JMSTopic) server.getJMSDestination(prevTopic);
            if (dest == null)
               throw new InvalidDestinationException("Previous topic does not exist: " + prevTopic);
            dest.destroyDurableSubscription(sub);

            // Create the new
            dest = (JMSTopic) server.getJMSDestination(topic);
            if (dest == null)
               throw new InvalidDestinationException("Topic does not exist: " + topic);
            dest.createDurableSubscription(sub);

            //durable subscription has new topic, save.
            subscription.setTopic(topic.getName());
            subscription.setSelector(sub.getSelector());
            saveDurableSubscription(subscription);
         }
      }
   }

   public SpyTopic getDurableTopic(DurableSubscriptionID sub) throws JMSException
   {
      DurableSubscription subscription = getDurableSubscription(sub);
      if (subscription == null)
         throw new InvalidDestinationException("No durable subscription found for subscription: "
               + sub.getSubscriptionName());

      return new SpyTopic(subscription.getTopic());
   }

   public String checkUser(String login, String passwd) throws JMSException
   {
      String clientId = getPreconfClientId(login, passwd);
      
      if (clientId != null)
      {
         synchronized (loggedOnClientIds)
         {
            if (loggedOnClientIds.contains(clientId))
            {
               throw new JMSSecurityException
                  ("The login id has an assigned client id '" + clientId +
                   "', that is already connected to the server!");
            }
            loggedOnClientIds.add(clientId);
         }
      }

      return clientId;
   }

   public void addLoggedOnClientId(String ID) throws JMSException
   {
      checkLoggedOnClientId(ID);

      synchronized (loggedOnClientIds)
      {
         if (loggedOnClientIds.contains(ID))
            throw new InvalidClientIDException("This client id '" + ID + "' is already registered!");
         loggedOnClientIds.add(ID);
      }
      if (log.isTraceEnabled())
         log.trace("Client id '" + ID + "' is logged in.");
   }

   public void removeLoggedOnClientId(String ID)
   {
      synchronized (loggedOnClientIds)
      {
         loggedOnClientIds.remove(ID);
      }
      if (log.isTraceEnabled())
         log.trace("Client id '" + ID + "' is logged out.");
   }

   abstract public Collection getDurableSubscriptionIdsForTopic(SpyTopic topic) throws JMSException;

   /**
    * Get preconfigured clientID for login/user, and if state manager wants
    * do authentication. This is NOT recomended when using a SecurityManager.
    * 
    * @param login the user name
    * @param passwd the password
    * @return any preconfigured client id
    * @throws JMSException for any error
    */
   abstract protected String getPreconfClientId(String login, String passwd) throws JMSException;

   /**
    * Check if the clientID is allowed to logg in from the particular state
    * managers perspective.
    * 
    * @param clientID the client id to check
    * @throws JMSException for any error
    */
   abstract protected void checkLoggedOnClientId(String clientID) throws JMSException;

   /**
    * Get a DurableSubscription. 
    *
    * @param sub the durable subscription id
    * @return the durable subscription or null if not found
    * @throws JMSException for any error
    */
   abstract protected DurableSubscription getDurableSubscription(DurableSubscriptionID sub) throws JMSException;

   /**
    * Add to durable subs and save the subsrcription to persistent storage.<p>
    *
    * Called by this class so the sublclass can save. This may be both
    * a new subscription or a changed one. It is up to the sublcass
    * to know how to find a changed on. (Only the topic will have changed,
    * and it is the same DurableSubscription that is saved again that this
    * class got through getDurableSubscription.
    *
    * @param ds the durable subscription to save
    * @throws JMSException for any error
    */
   abstract protected void saveDurableSubscription(DurableSubscription ds) throws JMSException;

   /**
    * Remove the subscription and save  to persistent storage.<p>
    *
    * Called by this class so the sublclass can remove.
    * 
    * @param ds the durable subscription to save
    * @throws JMSException for any error
    */
   abstract protected void removeDurableSubscription(DurableSubscription ds) throws JMSException;

   /**
    * Abstracts the data between a subclass and this class.
    * 
    * A sublcass can extends this class to ad custom behaviour.
    */
   protected class DurableSubscription
   {
      /** The client id of the durable subscription */
      String clientID;

      /** The subscription name of the durable subscription */
      String name;

      /** The topic name of the durable subscription */
      String topic;

      /** Any message selector of the durable subscription */
      String selector;

      /**
       * Create a new DurableSubscription.
       */
      public DurableSubscription()
      {
      }

      /**
       * Create a new DurableSubscription.
       * 
       * @param clientID the client id
       * @param name the subscription name
       * @param topic the topic name
       * @param selector any message selector
       */
      public DurableSubscription(String clientID, String name, String topic, String selector)
      {
         this.clientID = clientID;
         this.name = name;
         this.topic = topic;
         this.selector = selector;
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
       * Get the subcription name
       * 
       * @return the subscription name
       */
      public String getName()
      {
         return name;
      }

      /**
       * Get the topic name
       * 
       * @return the topic name
       */
      public String getTopic()
      {
         return topic;
      }

      /**
       * Set the topic name
       * 
       * @param topic the internal name of the topic
       */
      public void setTopic(String topic)
      {
         this.topic = topic;
      }

      /**
       * Gets the selector.
       * 
       * @return the selector
       */
      public String getSelector()
      {
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

      public String toString()
      {
         JBossStringBuilder buffer = new JBossStringBuilder();
         buffer.append("DurableSub[clientID=").append(clientID);
         buffer.append(" name=").append(name);
         buffer.append(" topic=").append(topic);
         buffer.append(" selector=").append(selector);
         buffer.append(']');
         return buffer.toString();
      }
   }
}
