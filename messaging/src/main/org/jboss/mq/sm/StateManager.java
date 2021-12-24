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
import javax.jms.JMSException;
import org.jboss.mq.SpyTopic;
import org.jboss.mq.DurableSubscriptionID;
import org.jboss.mq.server.JMSDestinationManager;

/**
 * Interface for StateManager.
 *
 * A StateManager is a manager that manages states that has to do with clients/
 * users and durable subscription.
 *
 * A state manager most know how to persist information regarding durable
 * subscriptions.
 *
 * It should also hold all current clientID's that are connected to the server.
 *
 * It may also support basic autentication, which is what the old
 * default StateManager did.
 *
 * @author     <a href="pra@tim.se">Peter Antman</a>
 * @author     <a href="Norbert.Lataille@m4x.org">Norbert Lataille</a>
 * @author     <a href="hiram.chirino@jboss.org">Hiram Chirino</a>
 * @version $Revision: 57198 $
 */

public interface StateManager  {
   
  /**
    * Ad, change or delete a durable subsciption.  
    *
    * The contract is that the StateManager, must physically create the durable
    * subscription. And when the method returns the information must be persisted.
    *
    *
    * @param  server            The JMSServer
    * @param  sub               The id of the durable subscription
    * @param  topic             The topic to subscribe durable on, if null
    *                           the subscription will be removed.
    * @exception  JMSException  Description of Exception
    */
   public void setDurableSubscription(JMSDestinationManager server, DurableSubscriptionID sub, SpyTopic topic) throws JMSException;

   /**
    * Get the destination a subscription is for.
    */
   public SpyTopic getDurableTopic(DurableSubscriptionID sub) throws JMSException;
   
   /**
    * Check if a user has a preconfigured clientID and return it.
    *
    * If the user has a preconfigured clienID it will be added to the current
    * logged in clientID's and returned.
    *
    * The state manager may also use this method to authenticate a user.
    * If a SecurityManager is installed this is not necesarry.
    *
    * @param  login             user name
    * @param  passwd            password
    * @return                   a preconfigured clientID.
    * @exception  JMSException  if the user
    * @exception  JMSSecurityException if the clientID is already loged in.
    */
   public String checkUser(String login, String passwd) throws JMSException;

   /**
    *  Ad a logged in clientID to the statemanager.
    *
    * The clientID must not be active. 
    *
    * The StateManager should somehow assure that a clientID that is 
    * preconfigured for a user is not allowed to be added this way.
    *
    * @param  ID            a clientID
    * @exception  JMSException  Description of Exception
    * @exception InvalidClientIDException if the clientID wass already logged in.
    */
   public void addLoggedOnClientId(String ID) throws JMSException;

   /**
    *  Remove the logged in clientID.
    *
    * @param  ID  clientID.
    */
   public void removeLoggedOnClientId(String ID);

   /**
    * Get all configured durable subscriptions for a particular topic.
    *
    * @param  topic  the topic.
    * @return a collection of DurableSubscriptionID
    * @exception  JMSException  Description of Exception
    */
   public Collection getDurableSubscriptionIdsForTopic(SpyTopic topic)
      throws JMSException;
} // StateManager



