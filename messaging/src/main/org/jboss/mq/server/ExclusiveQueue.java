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
package org.jboss.mq.server;

import javax.jms.JMSException;

import org.jboss.mq.SpyDestination;
import org.jboss.mq.Subscription;

/**
 *  This class implements a basic queue with an exclusive subscription.
 *
 * @author     Adrian Brock (Adrian.Brock@HappeningTimes.com)
 * @created    28th October 2002
 */
public class ExclusiveQueue
   extends BasicQueue
{
   Subscription exclusive;
   boolean removed = false;

   public ExclusiveQueue
   (
      JMSDestinationManager server, 
      SpyDestination destination,
      Subscription exclusive,
      BasicQueueParameters parameters
   )
      throws JMSException
   {
      super
      (
         server, 
         destination.toString() + "." + exclusive.connectionToken.getClientID() + '.' + exclusive.subscriptionId,
         parameters
      );
      this.exclusive = exclusive;
   }

   public Subscription getExclusiveSubscription()
   {
      return exclusive;
   }

   public void addMessage(MessageReference mesRef, org.jboss.mq.pm.Tx txId)
      throws JMSException
   {
      // Ignore the message if we are not interested
      if (removed || exclusive.accepts(mesRef.getHeaders()) == false)
         dropMessage(mesRef);
      else
         super.addMessage( mesRef, txId );
   }

   public void restoreMessage(MessageReference mesRef)
   {
      if (removed)
         dropMessage(mesRef);
      else
         super.restoreMessage(mesRef);
   }

   protected void nackMessage(MessageReference mesRef)
   {
      if (removed)
         dropMessage(mesRef);
      else
         super.nackMessage(mesRef);
   }

   public void removeSubscriber(Subscription sub)
   {
      removed = true;
      super.removeSubscriber(sub);
   }
}
