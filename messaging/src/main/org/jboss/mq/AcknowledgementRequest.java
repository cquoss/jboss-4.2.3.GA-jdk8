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

import javax.jms.Destination;

/**
 * Used to Acknowledge sent messages.
 * <p>
 * This class holds the minimum amount of information needed to identify a
 * message to the JMSServer.
 * 
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author David Maplesden (David.Maplesden@orion.co.nz)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class AcknowledgementRequest implements Externalizable
{
   // Constants -----------------------------------------------------

   /** The serialVersionUID */
   private static final long serialVersionUID = -2227528634302168874L;
   
   // Attributes ----------------------------------------------------

   /** Is it an acknowledgement */
   public boolean isAck;
   /** The destination */
   public Destination destination = null;
   /** The messageID */
   public String messageID = null;
   /** The subscriberId */
   public int subscriberId;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public AcknowledgementRequest()
   {
      this(false);
   }

   public AcknowledgementRequest(boolean ack)
   {
      this.isAck = ack;
   }
   
   // Public --------------------------------------------------------
   
   public boolean isAck()
   {
      return isAck;
   }
   
   // Object overrides ----------------------------------------------
   
   public boolean equals(Object o)
   {

      if (!(o instanceof AcknowledgementRequest))
      {
         return false;
      }

      return messageID.equals(((AcknowledgementRequest) o).messageID)
            && destination.equals(((AcknowledgementRequest) o).destination)
            && subscriberId == ((AcknowledgementRequest) o).subscriberId;
   }

   public int hashCode()
   {
      return messageID.hashCode();
   }

   public String toString()
   {
      return "AcknowledgementRequest:" +
         ((isAck) ? "ACK" : "NACK") + "," + destination + "," + messageID;
   }

   public void readExternal(java.io.ObjectInput in) throws java.io.IOException
   {
      isAck = in.readBoolean();
      destination = SpyDestination.readDest(in);
      messageID = in.readUTF();
      subscriberId = in.readInt();
   }
   
   // Externalizable implementation ---------------------------------
   
   public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException
   {
      out.writeBoolean(isAck);
      SpyDestination.writeDest(out, destination);
      out.writeUTF(messageID);
      out.writeInt(subscriberId);
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
