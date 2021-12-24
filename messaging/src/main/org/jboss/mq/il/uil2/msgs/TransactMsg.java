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
package org.jboss.mq.il.uil2.msgs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jboss.mq.TransactionRequest;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57198 $
 */
public class TransactMsg extends BaseMsg
{
   private TransactionRequest request;

   public TransactMsg()
   {
      this(new TransactionRequest());
   }
   public TransactMsg(TransactionRequest request)
   {
      super(MsgTypes.m_transact);
      this.request = request;
   }

   public TransactionRequest getRequest()
   {
      return request;
   }

   public void trimReply()
   {
      request = null;
   }

   public void write(ObjectOutputStream out) throws IOException
   {
      super.write(out);
      int hasRequest = request != null ? 1 : 0;
      out.writeByte(hasRequest);
      if (hasRequest == 1)
         request.writeExternal(out);
   }
   public void read(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      super.read(in);
      int hasRequest = in.readByte();
      if (hasRequest == 1)
         request.readExternal(in);
   }
}
