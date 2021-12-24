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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import javax.jms.Destination;

/**
 * This class implements javax.jms.Destination
 * 
 * @author Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author David Maplesden (David.Maplesden@orion.co.nz)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class SpyDestination implements Destination, Serializable
{
   // Constants -----------------------------------------------------

   /** The serialVersionUID */
   static final long serialVersionUID = -451227938651163471L;

   /** Null object */
   protected final static int NULL = 0;
   /** An object */
   protected final static int OBJECT = 1;
   /** A queue */
   protected final static int SPY_QUEUE = 2;
   /** A topic */
   protected final static int SPY_TOPIC = 3;
   /** A temporary queue */
   protected final static int SPY_TEMP_QUEUE = 4;
   /** A temporary topic */
   protected final static int SPY_TEMP_TOPIC = 5;

   // Attributes ----------------------------------------------------

   /** The name */
   protected String name;

   /** The hash code */
   protected int hash;

   // Static --------------------------------------------------------

   /**
    * Write the destination
    * 
    * @param out the output
    * @param dest the destination
    * @throws IOException for any error
    */
   public static void writeDest(ObjectOutput out, Destination dest) throws IOException
   {
      if (dest == null)
         out.writeByte(NULL);
      else if (dest instanceof SpyTemporaryQueue)
      {
         out.writeByte(SPY_TEMP_QUEUE);
         out.writeUTF(((SpyTemporaryQueue) dest).getName());
      }
      else if (dest instanceof SpyTemporaryTopic)
      {
         out.writeByte(SPY_TEMP_TOPIC);
         out.writeUTF(((SpyTemporaryTopic) dest).getName());
      }
      else if (dest instanceof SpyQueue)
      {
         out.writeByte(SPY_QUEUE);
         out.writeUTF(((SpyQueue) dest).getName());
      }
      else if (dest instanceof SpyTopic)
      {
         out.writeByte(SPY_TOPIC);
         out.writeUTF(((SpyTopic) dest).getName());
         DurableSubscriptionID id = ((SpyTopic) dest).durableSubscriptionID;
         if (id == null)
         {
            out.writeByte(NULL);
         }
         else
         {
            out.writeByte(OBJECT);
            writeString(out, id.getClientID());
            writeString(out, id.getSubscriptionName());
            writeString(out, id.getSelector());
         }
      }
      else
      {
         out.writeByte(OBJECT);
         out.writeObject(dest);
      }
   }

   /**
    * Read a destination
    *
    * @param in the input
    * @return the destination
    * @throws IOException for any error
    */
   public static Destination readDest(ObjectInput in) throws IOException
   {
      byte destType = in.readByte();
      if (destType == NULL)
         return null;
      else if (destType == SPY_TEMP_QUEUE)
         return new SpyTemporaryQueue(in.readUTF(), null);
      else if (destType == SPY_TEMP_TOPIC)
         return new SpyTemporaryTopic(in.readUTF(), null);
      else if (destType == SPY_QUEUE)
         return new SpyQueue(in.readUTF());
      else if (destType == SPY_TOPIC)
      {
         String name = in.readUTF();
         destType = in.readByte();
         if (destType == NULL)
            return new SpyTopic(name);
         else
         {
            String clientId = readString(in);
            String subName = readString(in);
            String selector = readString(in);
            return new SpyTopic(new SpyTopic(name), clientId, subName, selector);
         }
      }
      else
      {
         try
         {
            return (Destination) in.readObject();
         }
         catch (ClassNotFoundException e)
         {
            throw new IOException("Class not found for unknown destination.");
         }
      }
   }

   // Constructors --------------------------------------------------

   /**
	 * Create a new SpyDestination
	 * 
	 * @param name the name
	 */
   SpyDestination(String name)
   {
      this.name = name;
      hash = name.hashCode();
   }

   // Public --------------------------------------------------------

   /**
	 * Gets the name of the destination.
	 * 
	 * @return the name
	 */
   public String getName()
   {
      return name;
   }

   // Object overrides ----------------------------------------------

   public int hashCode()
   {
      return hash;
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   /**
    * Write a string
    *
    * @param out the output
    * @param s the string
    * @throws IOException for any error
    */
   private static void writeString(ObjectOutput out, String s) throws IOException
   {
      if (s == null)
         out.writeByte(NULL);
      else
      {
         out.writeByte(OBJECT);
         //non-null
         out.writeUTF(s);
      }
   }

   /**
    * Read a string
    *
    * @param in the input 
    * @return the string
    * @throws IOException for any error
    */
   private static String readString(ObjectInput in) throws IOException
   {
      byte b = in.readByte();
      if (b == NULL)
         return null;
      else
         return in.readUTF();
   }

   // Inner classes -------------------------------------------------
}