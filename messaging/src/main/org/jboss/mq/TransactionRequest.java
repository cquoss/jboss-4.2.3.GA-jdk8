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
import java.io.Serializable;

import javax.transaction.xa.Xid;

/**
 * This class contians all the data needed to perform a JMS transaction
 * 
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author David Maplesden (David.Maplesden@orion.co.nz)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @author Daniel Bloomfield Ramagem (daniel.ramagem@gmail.com) 
 * @version $Revision: 57198 $
 */
public class TransactionRequest implements Externalizable
{
   /** The serialVersionUID */
   static final long serialVersionUID = 5368191944552650149L;
   
   /** One phase Commit request */
   public final static byte ONE_PHASE_COMMIT_REQUEST = 0;
   /** Two phase Prepare phase */
   public final static byte TWO_PHASE_COMMIT_PREPARE_REQUEST = 1;
   /** Two phase Commit phase */
   public final static byte TWO_PHASE_COMMIT_COMMIT_REQUEST = 2;
   /** Rollback request */
   public final static byte TWO_PHASE_COMMIT_ROLLBACK_REQUEST = 3;
   
   /** Request type */
   public byte requestType = ONE_PHASE_COMMIT_REQUEST;

   /** For 2 phase commit, this identifies the transaction. */
   public Object xid;

   /** messages sent in the transaction */
   public SpyMessage[] messages;

   /** messages acknowleged in the transaction */
   public AcknowledgementRequest[] acks;

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      requestType = in.readByte();
      xid = in.readObject();
      int size = in.readInt();
      messages = new SpyMessage[size];
      for (int i = 0; i < size; ++i)
         messages[i] = SpyMessage.readMessage(in);
      size = in.readInt();
      acks = new AcknowledgementRequest[size];
      for (int i = 0; i < size; ++i)
      {
         acks[i] = new AcknowledgementRequest();
         acks[i].readExternal(in);
      }
   }

   public void writeExternal(ObjectOutput out) throws IOException
   {
      out.writeByte(requestType);
      // Non Serializable Xid, use our wrapper
      if (xid != null && xid instanceof Xid && xid instanceof Serializable == false)
         out.writeObject(new JBossMQXid((Xid) xid));
      else
         out.writeObject(xid);
      if (messages == null)
         out.writeInt(0);
      else
      {
         out.writeInt(messages.length);
         for (int i = 0; i < messages.length; ++i)
            SpyMessage.writeMessage(messages[i], out);
      }
      if (acks == null)
         out.writeInt(0);
      else
      {
         out.writeInt(acks.length);
         for (int i = 0; i < acks.length; ++i)
            acks[i].writeExternal(out);
      }
   }
}