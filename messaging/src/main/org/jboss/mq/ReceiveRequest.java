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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * This class contians all the data needed to perform a JMS transaction
 * 
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author David Maplesden (David.Maplesden@orion.co.nz)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class ReceiveRequest implements Externalizable
{
   // Constants -----------------------------------------------------

   /** The serialVersionUID */
   private static final long serialVersionUID = 8632752498878645170L;

   /** Whether the subscription is null */
   protected final static byte NULL = 0;

   /** Whether the subscription is non null */
   protected final static byte NON_NULL = 1;
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   /** The message */
   public SpyMessage message;
   /** Is this an exlusive message? Then subscriptionId != null */
   public Integer subscriptionId;
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   public void readExternal(ObjectInput in) throws IOException
   {
      message = SpyMessage.readMessage(in);
      byte b = in.readByte();
      if (b == NON_NULL)
      {
         subscriptionId = new Integer(in.readInt());
      }
      else
      {
         subscriptionId = null;
      }
   }
   
   // Object overrides ----------------------------------------------
   
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("ReceiveRequest[");
      buffer.append("message=").append(message.header.jmsMessageID);
      buffer.append(" subscription=").append(subscriptionId);
      return buffer.toString();
   }
   
   // Externalizable implementation ---------------------------------
   
   public void writeExternal(ObjectOutput out) throws IOException
   {
      SpyMessage.writeMessage(message, out);
      if (subscriptionId == null)
      {
         out.writeByte(NULL);
      }
      else
      {
         out.writeByte(NON_NULL);
         out.writeInt(subscriptionId.intValue());
      }
   }

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}