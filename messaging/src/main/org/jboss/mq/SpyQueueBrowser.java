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

import java.util.Enumeration;
import java.util.NoSuchElementException;

import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;
import javax.jms.Queue;
import org.jboss.mq.selectors.Selector;

import javax.jms.QueueBrowser;

/**
 * This class implements javax.jms.QueueBrowser
 * 
 * @author Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class SpyQueueBrowser implements QueueBrowser
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------

   /** Whether we are closed */
   boolean closed;
   /** The destination this browser will browse messages from */
   Queue destination;
   /** String Selector */
   String messageSelector;
   /** The Session this was created with */
   SpySession session;
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------

   /**
    * Create a new SpyQueueBrowser
    *
    * @param session the session
    * @param destination the destination
    * @param messageSelector the message selector
    * @throws InvalidSelectorException for an invalid selector
    */
   SpyQueueBrowser(SpySession session, Queue destination, String messageSelector) throws InvalidSelectorException
   {
      this.destination = destination;
      this.session = session;
      this.messageSelector = messageSelector;

      // If the selector is set, try to build it, throws an
      // InvalidSelectorException
      // if it is not valid.
      if (messageSelector != null)
         new Selector(messageSelector);
   }
   
   // Public --------------------------------------------------------
   
   // QueueBrowser implementation -----------------------------------

   public Queue getQueue() throws JMSException
   {
      return destination;
   }

   public String getMessageSelector() throws JMSException
   {
      return messageSelector;
   }

   public Enumeration getEnumeration() throws JMSException
   {
      if (closed)
         throw new JMSException("The QueueBrowser was closed");

      final SpyMessage data[] = session.connection.browse(destination, messageSelector);
      return new Enumeration()
      {
         int i = 0;
         public boolean hasMoreElements()
         {
            return i < data.length;
         }
         public Object nextElement()
         {
            if (!hasMoreElements())
               throw new NoSuchElementException();
            return data[i++];
         }
      };
   }

   public void close() throws JMSException
   {
      closed = true;
      return;
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------

}