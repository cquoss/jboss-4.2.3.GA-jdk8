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
import java.util.Arrays;

import javax.transaction.xa.Xid;

/**
 * This class is a wrapper for non-serializable implementations of 
 * java.transaction.xa.Xid.
 *
 * @author Daniel Bloomfield Ramagem (daniel.ramagem@gmail.com) 
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class JBossMQXid implements Serializable, Xid
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -2227021688745286343L;

   /** The format id */
   private int formatId;

   /** The gid */
   private byte[] globalTransactionId;

   /** The branch */
   private byte[] branchQualifier;

   /** Cached toString() */
   private transient String cachedToString;

   /** Cached hashCode() */
   private transient int cachedHashCode;

   /**
    * Create a new wrapper Xid
    * 
    * @param xid the wrapped xid
    */
   public JBossMQXid(Xid xid)
   {
      formatId = xid.getFormatId();
      globalTransactionId = xid.getGlobalTransactionId();
      branchQualifier = xid.getBranchQualifier();
   }

   public int getFormatId()
   {
      return formatId;
   }

   public byte[] getGlobalTransactionId()
   {
      return globalTransactionId;
   }

   public byte[] getBranchQualifier()
   {
      return branchQualifier;
   }

   public boolean equals(Object object)
   {
      if (object == null || (object instanceof Xid) == false)
         return false;

      Xid other = (Xid) object;
      return
      (
         formatId == other.getFormatId() && 
         Arrays.equals(globalTransactionId, other.getGlobalTransactionId()) &&
         Arrays.equals(branchQualifier, other.getBranchQualifier())
      );
   }

   public int hashCode()
   {
      if (cachedHashCode == 0)
      {
         cachedHashCode = formatId;
         for (int i = 0; i < globalTransactionId.length; ++i)
            cachedHashCode += globalTransactionId[i];
      }
      return cachedHashCode;
   }

   public String toString()
   {
      if (cachedToString == null)
      {
         StringBuffer buffer = new StringBuffer();
         buffer.append("JBossMQXid[FormatId=").append(getFormatId());
         buffer.append(" GlobalId=").append(new String(getGlobalTransactionId()).trim());
         byte[] branchQualifer = getBranchQualifier();
         buffer.append(" BranchQual=");
         if (branchQualifer == null)
            buffer.append("null");
         else
            buffer.append(new String(getBranchQualifier()).trim());
         buffer.append(']');
         cachedToString = buffer.toString();
      }
      return cachedToString;
   }
}