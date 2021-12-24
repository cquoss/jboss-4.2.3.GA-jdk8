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

import javax.transaction.xa.Xid;

/**
 * RecoverMsg.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class RecoverMsg extends BaseMsg
{
   private int flags;
   private Xid[] xids;

   public RecoverMsg()
   {
      super(MsgTypes.m_recover);
   }
   
   public RecoverMsg(int flags)
   {
      super(MsgTypes.m_recover);
      this.flags = flags;
   }

   public int getFlags()
   {
      return flags;
   }
   
   public Xid[] getXids()
   {
      return xids;
   }
   
   public void setXids(Xid[] xids)
   {
      this.xids = xids;
   }

   public void write(ObjectOutputStream out) throws IOException
   {
      super.write(out);
      int hasXids = xids != null ? 1 : 0;
      out.writeByte(hasXids);
      if (hasXids == 1)
         out.writeObject(xids);
   }
   
   public void read(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      super.read(in);
      int hasXids = in.readByte();
      if (hasXids == 1)
         xids = (Xid[]) in.readObject();
   }
}
