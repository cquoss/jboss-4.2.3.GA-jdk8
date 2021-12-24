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
package org.jboss.mq.sm.none;

import java.util.Collection;
import java.util.Collections;

import javax.jms.JMSException;

import org.jboss.mq.DurableSubscriptionID;
import org.jboss.mq.SpyTopic;
import org.jboss.mq.sm.AbstractStateManager;
import org.jboss.mq.sm.StateManager;
import org.jboss.util.NotImplementedException;

/**
 * NullStateManager.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 40901 $
 */
public class NullStateManager extends AbstractStateManager
{
   protected void checkLoggedOnClientId(String clientID) throws JMSException
   {
      return;
   }

   protected DurableSubscription getDurableSubscription(DurableSubscriptionID sub) throws JMSException
   {
      throw new NotImplementedException("Durable subscriptions are not supported");
   }

   public Collection getDurableSubscriptionIdsForTopic(SpyTopic topic) throws JMSException
   {
      return Collections.EMPTY_SET;
   }

   protected String getPreconfClientId(String login, String passwd) throws JMSException
   {
      return null;
   }

   protected void removeDurableSubscription(DurableSubscription ds) throws JMSException
   {
      throw new NotImplementedException("Durable subscriptions are not supported");
   }

   protected void saveDurableSubscription(DurableSubscription ds) throws JMSException
   {
      throw new NotImplementedException("Durable subscriptions are not supported");
   }

   public StateManager getInstance()
   {
      return this;
   }
}
