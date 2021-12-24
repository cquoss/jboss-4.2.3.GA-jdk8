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
package org.jboss.messaging.memory;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.messaging.interfaces.Consumer;
import org.jboss.messaging.interfaces.MessageReference;
import org.jboss.messaging.interfaces.MessageSet;

import EDU.oswego.cs.dl.util.concurrent.ReentrantLock;

/**
 * An in memory message set
 * 
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class MemoryMessageSet implements MessageSet
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   
   /** The messages */
   private Set messages;
   
   /** The lock */
   private ReentrantLock mutex = new ReentrantLock();

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   /**
    * Create a new MemoryMessageSet.
    *
    * @param comparator the comparator for the messages
    */
   public MemoryMessageSet(Comparator comparator)
   {
      messages = new TreeSet(comparator);
   }
   
   // Public --------------------------------------------------------

   // MessageSet implementation -------------------------------------

   public void add(MessageReference reference)
   {
      messages.add(reference);
   }

   public MessageReference remove(Consumer consumer)
   {
      // Do we have a message?
      if (messages.size() > 0)
      {
         for (Iterator iterator = messages.iterator(); iterator.hasNext();)
         {
            MessageReference message = (MessageReference) iterator.next();
            if (consumer.accepts(message, true))
            {
               iterator.remove();
               return message;
            }
         }
      }
      return null;
   }
   
   public void lock()
   {
      boolean interrupted = false;
      try
      {
         mutex.acquire();
      }
      catch (InterruptedException e)
      {
         interrupted = true;
      }
      if (interrupted)
         Thread.currentThread().interrupt();
   }

   public void unlock()
   {
      mutex.release();
   }
   
   public void setConsumer(Consumer consumer)
   {
      // There are no out of band notifications
   }
   
   // Protected -----------------------------------------------------
   
   // Package Private -----------------------------------------------

   // Private -------------------------------------------------------

   // Inner Classes -------------------------------------------------
}
