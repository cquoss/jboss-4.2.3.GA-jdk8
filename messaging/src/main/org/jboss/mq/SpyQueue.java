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

import javax.jms.Queue;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;

/**
 * This class implements javax.jms.Queue
 * 
 * @author Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author David Maplesden (David.Maplesden@orion.co.nz)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class SpyQueue extends SpyDestination implements Serializable, Queue, Referenceable
{
   // Constants -----------------------------------------------------

   /** The serialVersionUID */
   static final long serialVersionUID = 3040902899515975733L;
   
   // Attributes ----------------------------------------------------

   /** added cached toString string for efficiency */
   private String toStringStr;
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------

   
   /**
    * Create a new SpyQueue
    *
    * @param queueName the queue name
    */
   public SpyQueue(String queueName)
   {
      super(queueName);
      toStringStr = "QUEUE." + name;
      hash++;
   }
   
   // Public --------------------------------------------------------
   
   // Queue implementation ------------------------------------------

   public String getQueueName()
   {
      return name;
   }
   
   // Referenceable implementation ----------------------------------

   public Reference getReference() throws javax.naming.NamingException
   {

      return new Reference("org.jboss.mq.SpyQueue", new StringRefAddr("name", name),
            "org.jboss.mq.referenceable.SpyDestinationObjectFactory", null);
   }

   // Object overrides ----------------------------------------------

   public String toString()
   {
      return toStringStr;
   }
   
   public boolean equals(Object obj)
   {
      if (!(obj instanceof SpyQueue))
         return false;
      if (obj.hashCode() != hash)
         return false;
      return ((SpyQueue) obj).name.equals(name);
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}