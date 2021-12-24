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
import java.util.Collection;
import java.util.List;

import javax.jms.IllegalStateException;

import org.jboss.mq.MessageStatistics;
import org.jboss.mq.SpyQueue;
import org.jboss.mq.server.JMSDestinationManager;
import org.jboss.mq.server.JMSQueue;
import org.jboss.mq.server.MessageCounter;

/**
 * This class is a message queue which is stored (hashed by Destination)
 * on the JMS provider
 *
 * @author     Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author     <a href="hiram.chirino@jboss.org">Hiram Chirino</a>
 * @author     <a href="pra@tim.se">Peter Antman</a>
 * @version    $Revision: 57198 $
 */
public class Queue extends DestinationMBeanSupport implements QueueMBean
{
   /** The destination */
   protected JMSQueue destination;

   public String getQueueName()
   {
      return destinationName;
   }

   public int getQueueDepth() throws Exception
   {
      if (destination == null)
         return 0;
      return destination.queue.getQueueDepth();
   }

   public int getScheduledMessageCount() throws Exception
   {
      if (destination == null)
         return 0;
      return destination.queue.getScheduledMessageCount();
   }

   public int getInProcessMessageCount() throws Exception
   {
      if (destination == null)
         return 0;
      return destination.queue.getInProcessMessageCount();
   }

   public void startService() throws Exception
   {
      if (destinationName == null || destinationName.length() == 0)
         throw new IllegalStateException("QueueName was not set");

      JMSDestinationManager jmsServer = (JMSDestinationManager)
         server.getAttribute(jbossMQService, "Interceptor");

      spyDest = new SpyQueue(destinationName);
      destination = new JMSQueue(spyDest, null, jmsServer, parameters);

      jmsServer.addDestination(destination);

      if (jndiName == null)
      {
         setJNDIName("queue/" + destinationName);
      }
      else
      {
         // in config phase, all we did was store the name, and not actually bind
         setJNDIName(jndiName);
      }
      super.startService();
   }   

   public void stopService() throws Exception
   {
      super.stopService();
      destination = null;
   }
   
   public void removeAllMessages() throws Exception
   {
      if (destination == null)
         return;
      destination.removeAllMessages();
   }

   public int getReceiversCount()
   {
      if (destination == null)
         return 0;
      return destination.queue.getReceiversCount();
   }
   
   public List listReceivers()
   {
      if (destination == null)
         return null;
      return destination.queue.getReceivers();
   }

   public List listMessages() throws Exception
   {
      if (destination == null)
         return null;
      return Arrays.asList(destination.queue.browse(null));
   }

   public List listMessages(String selector) throws Exception
   {
      if (destination == null)
         return null;
      return Arrays.asList(destination.queue.browse(selector));
   }

   public List listScheduledMessages() throws Exception
   {
      if (destination == null)
         return null;
      return destination.queue.browseScheduled(null);
   }

   public List listScheduledMessages(String selector) throws Exception
   {
      if (destination == null)
         return null;
      return destination.queue.browseScheduled(selector);
   }

   public List listInProcessMessages() throws Exception
   {
      if (destination == null)
         return null;
      return destination.queue.browseInProcess(null);
   }

   public List listInProcessMessages(String selector) throws Exception
   {
      if (destination == null)
         return null;
      return destination.queue.browseInProcess(selector);
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

   public int getSubscribersCount()
   {
      if (destination == null)
         return 0;
      return destination.queue.getSubscribers().size();
   }
   
   public Collection listSubscribers()
   {
      if (destination == null)
         return null;
      return destination.queue.getSubscribers();
   }
}
