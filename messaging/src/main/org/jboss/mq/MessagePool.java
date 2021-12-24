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

/**
 *	This class provides a pool of SpyMessages.
 *
 * This is an very simple implementation first up.
 *
 *	@author David Maplesden (David.Maplesden@orion.co.nz)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class MessagePool
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   /**
    * Gets a message
    * 
    * @return a message
    */
   public static SpyMessage getMessage()
   {
      return new SpyMessage();
   }

   /**
    * Gets a bytes message.
    * 
    * @return a bytes message
    */
   public static SpyBytesMessage getBytesMessage()
   {
      return new SpyBytesMessage();
   }

   /**
    * Gets a map message
    * 
    * @return a map message
    */
   public static SpyMapMessage getMapMessage()
   {
      return new SpyMapMessage();
   }

   /**
    * Gets a stream message.
    * 
    * @return a stream message 
    */
   public static SpyStreamMessage getStreamMessage()
   {
      return new SpyStreamMessage();
   }

   /**
    * Gets an object message.
    * 
    * @return an object message
    */
   public static SpyObjectMessage getObjectMessage()
   {
      return new SpyObjectMessage();
   }

   /**
    * Gets a text message.
    */
   public static SpyTextMessage getTextMessage()
   {
      return new SpyTextMessage();
   }

   /**
    * Gets a encapsulated message.
    */
   public static SpyEncapsulatedMessage getEncapsulatedMessage()
   {
      return new SpyEncapsulatedMessage();
   }

   /**
    * Releases a SpyMessage.
    */
   public static void releaseMessage(SpyMessage message)
   {
      // Pooling is no longer used
   }
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}