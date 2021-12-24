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
package org.jboss.mq.server.jmx;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.jms.IllegalStateException;

import org.jboss.mq.DurableSubscriptionID;
import org.jboss.mq.MessageStatistics;
import org.jboss.mq.SpyTopic;
import org.jboss.mq.Subscription;
import org.jboss.mq.server.BasicQueue;
import org.jboss.mq.server.JMSDestinationManager;
import org.jboss.mq.server.JMSTopic;
import org.jboss.mq.server.MessageCounter;

/**
 * This class is a message queue which is stored (hashed by Destination) on the
 * JMS provider
 *
 * @jmx:mbean extends="org.jboss.mq.server.jmx.DestinationMBean"
 * @author     Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author     <a href="hiram.chirino@jboss.org">Hiram Chirino</a>
 * @author     <a href="pra@tim.se">Peter Antman</a>
 * @version    $Revision: 57198 $
 */
public class Topic
   extends DestinationMBeanSupport
   implements TopicMBean
{
   protected JMSTopic destination;
   
   /**
    * @jmx:managed-attribute
    */
   public String getTopicName()
   {
      return destinationName;
   }

   public void startService() throws Exception
   {
      if (destinationName == null || destinationName.length() == 0)
      {
         throw new IllegalStateException("TopicName was not set");
      }
      
      JMSDestinationManager jmsServer = (JMSDestinationManager)
         server.getAttribute(jbossMQService, "Interceptor");

      spyDest = new SpyTopic(destinationName);
      destination = new JMSTopic(spyDest, null, jmsServer, parameters);
      
      jmsServer.addDestination(destination);
                          
      if (jndiName == null) {
            setJNDIName("topic/" + destinationName);
      }
      else {
         // in config phase, we only stored the name, and didn't actually bind it
         setJNDIName(jndiName);
      }
      super.startService();
   }

   public void stopService() throws Exception
   {
      super.stopService();
      destination = null;
   }

   /**
    * @see DestinationMBean#removeAllMessages()
    */
   public void removeAllMessages() throws Exception
   {
      if (destination == null)
         return;
      destination.removeAllMessages();
   }

   /**
    * @jmx:managed-attribute
    */
   public int getAllMessageCount()
   {
      if (destination == null)
         return 0;
      return destination.getAllMessageCount();
   }

   /**
    * @jmx:managed-attribute
    */
   public int getDurableMessageCount()
   {
      if (destination == null)
         return 0;
      return destination.getDurableMessageCount();
   }

   /**
    * @jmx:managed-attribute
    */
   public int getNonDurableMessageCount()
   {
      if (destination == null)
         return 0;
      return destination.getNonDurableMessageCount();
   }

   /**
    * @jmx:managed-attribute
    */
   public int getAllSubscriptionsCount()
   {
      if (destination == null)
         return 0;
      return destination.getAllSubscriptionsCount();
   }

   /**
    * @jmx:managed-attribute
    */
   public int getDurableSubscriptionsCount()
   {
      if (destination == null)
         return 0;
      return destination.getDurableSubscriptionsCount();
   }

   /**
    * @jmx:managed-attribute
    */
   public int getNonDurableSubscriptionsCount()
   {
      if (destination == null)
         return 0;
      return destination.getNonDurableSubscriptionsCount();
   }

   /**
    * @jmx:managed-operation
    */
   public List listAllSubscriptions()
   {
      if (destination == null)
         return null;
      return destination.getAllSubscriptions();
   }

   /**
    * @jmx:managed-operation
    */
   public List listDurableSubscriptions()
   {
      if (destination == null)
         return null;
      return destination.getDurableSubscriptions();
   }

   /**
    * @jmx:managed-operation
    */
   public List listNonDurableSubscriptions()
   {
      if (destination == null)
         return null;
      return destination.getNonDurableSubscriptions();
   }

   public List listMessages(String id) throws Exception
   {
      if (destination == null)
         return null;
      BasicQueue queue = findBasicQueue(id);
      return Arrays.asList(queue.browse(null));
   }

   public List listMessages(String id, String selector) throws Exception
   {
      if (destination == null)
         return null;
      BasicQueue queue = findBasicQueue(id);
      return Arrays.asList(queue.browse(selector));
   }

   public List listNonDurableMessages(String id, String sub) throws Exception
   {
      if (destination == null)
         return null;
      BasicQueue queue = findNonDurableBasicQueue(id, sub);
      return Arrays.asList(queue.browse(null));
   }

   public List listNonDurableMessages(String id, String sub, String selector) throws Exception
   {
      if (destination == null)
         return null;
      BasicQueue queue = findNonDurableBasicQueue(id, sub);
      return Arrays.asList(queue.browse(selector));
   }

   public List listDurableMessages(String id, String name) throws Exception
   {
      if (destination == null)
         return null;
      BasicQueue queue = findDurableBasicQueue(id, name);
      return Arrays.asList(queue.browse(null));
   }

   public List listDurableMessages(String id, String name, String selector) throws Exception
   {
      if (destination == null)
         return null;
      BasicQueue queue = findDurableBasicQueue(id, name);
      return Arrays.asList(queue.browse(selector));
   }

   public long getNonDurableMessageCount(String id, String sub) throws Exception
   {
      if (destination == null)
         return 0;
      BasicQueue queue = findNonDurableBasicQueue(id, sub);
      return queue.getQueueDepth();
   }

   public long getDurableMessageCount(String id, String name) throws Exception
   {
      if (destination == null)
         return 0;
      BasicQueue queue = findDurableBasicQueue(id, name);
      return queue.getQueueDepth();
   }

   public List listNonDurableScheduledMessages(String id, String sub) throws Exception
   {
      if (destination == null)
         return null;
      BasicQueue queue = findNonDurableBasicQueue(id, sub);
      return queue.browseScheduled(null);
   }

   public List listNonDurableScheduledMessages(String id, String sub, String selector) throws Exception
   {
      if (destination == null)
         return null;
      BasicQueue queue = findNonDurableBasicQueue(id, sub);
      return queue.browseScheduled(selector);
   }

   public List listDurableScheduledMessages(String id, String name) throws Exception
   {
      if (destination == null)
         return null;
      BasicQueue queue = findDurableBasicQueue(id, name);
      return queue.browseScheduled(null);
   }

   public List listDurableScheduledMessages(String id, String name, String selector) throws Exception
   {
      if (destination == null)
         return null;
      BasicQueue queue = findDurableBasicQueue(id, name);
      return queue.browseScheduled(selector);
   }

   public long getNonDurableScheduledMessageCount(String id, String sub) throws Exception
   {
      if (destination == null)
         return 0;
      BasicQueue queue = findNonDurableBasicQueue(id, sub);
      return queue.getScheduledMessageCount();
   }

   public long getDurableScheduledMessageCount(String id, String name) throws Exception
   {
      if (destination == null)
         return 0;
      BasicQueue queue = findDurableBasicQueue(id, name);
      return queue.getScheduledMessageCount();
   }

   public List listNonDurableInProcessMessages(String id, String sub) throws Exception
   {
      if (destination == null)
         return null;
      BasicQueue queue = findNonDurableBasicQueue(id, sub);
      return queue.browseInProcess(null);
   }

   public List listNonDurableInProcessMessages(String id, String sub, String selector) throws Exception
   {
      if (destination == null)
         return null;
      BasicQueue queue = findNonDurableBasicQueue(id, sub);
      return queue.browseInProcess(selector);
   }

   public List listDurableInProcessMessages(String id, String name) throws Exception
   {
      if (destination == null)
         return null;
      BasicQueue queue = findDurableBasicQueue(id, name);
      return queue.browseInProcess(null);
   }

   public List listDurableInProcessMessages(String id, String name, String selector) throws Exception
   {
      if (destination == null)
         return null;
      BasicQueue queue = findDurableBasicQueue(id, name);
      return queue.browseInProcess(selector);
   }

   public long getNonDurableInProcessMessageCount(String id, String sub) throws Exception
   {
      if (destination == null)
         return 0;
      BasicQueue queue = findNonDurableBasicQueue(id, sub);
      return queue.getInProcessMessageCount();
   }

   public long getDurableInProcessMessageCount(String id, String name) throws Exception
   {
      if (destination == null)
         return 0;
      BasicQueue queue = findDurableBasicQueue(id, name);
      return queue.getInProcessMessageCount();
   }
   
   public MessageCounter[] getMessageCounter()
   {
      if (destination == null)
         return null;
      return destination.getMessageCounter();
   }
   
   public MessageStatistics[] getMessageStatistics() throws Exception
   {
      if (destination == null)
         return null;
      return MessageCounter.getMessageStatistics(destination.getMessageCounter());
   }

   protected BasicQueue findBasicQueue(String id) throws Exception
   {
      if (destination == null)
         return null;
      List queues = destination.getAllQueues();
      if (id == null)
         throw new IllegalArgumentException("Null subscription id: " + help(queues));
      for (Iterator i = queues.iterator(); i.hasNext();)
      {
         BasicQueue q = (BasicQueue) i.next();
         if (q.getDescription().equals(id))
            return q;
      }
      throw new IllegalArgumentException("Invalid subscription id: " + help(queues));
   }

   protected BasicQueue findNonDurableBasicQueue(String id, String sub) throws Exception
   {
      if (destination == null)
         return null;
      List subscriptions = destination.getNonDurableSubscriptions();
      if (id == null)
         throw new IllegalArgumentException("Null subscription id, enter client id plus the subscription id: " + help(subscriptions));
      for (Iterator i = subscriptions.iterator(); i.hasNext();)
      {
         Subscription s = (Subscription) i.next();
         String clientId = s.connectionToken.getClientID();
         
         if (sub == null || sub.trim().length() == 0 || Integer.toString(s.subscriptionId).equals(sub))
         {
            if (clientId != null && clientId.equals(id))
               return destination.getQueue(s);
         }
      }
      throw new IllegalArgumentException("Invalid subscription id, enter client id plus the subscription id: " + help(subscriptions));
   }

   protected BasicQueue findDurableBasicQueue(String id, String name) throws Exception
   {
      if (destination == null)
         return null;
      List subscriptions = destination.getDurableSubscriptions();
      if (id == null || name == null)
         throw new IllegalArgumentException("Null durable subscription enter client id and subscription name: " + help(subscriptions));
      DurableSubscriptionID durID = new DurableSubscriptionID(id, name, null);
      BasicQueue result = destination.getDurableSubscription(durID);
      if (result != null)
         return result;
      throw new IllegalArgumentException("Invalid durable subscription enter client id and subscription name: " + help(subscriptions));
   }
   
   protected String help(List queues)
   {
      return "id must be one from the following list " + queues;
   }
}
