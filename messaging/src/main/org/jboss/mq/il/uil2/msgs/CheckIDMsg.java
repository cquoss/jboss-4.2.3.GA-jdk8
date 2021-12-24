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

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57198 $
 */
public class CheckIDMsg extends BaseMsg
{
   private String id;

   public CheckIDMsg()
   {
      this(null);
   }
   public CheckIDMsg(String id)
   {
      super(MsgTypes.m_checkID);
      this.id = id;
   }

   public String getID()
   {
      return id;
   }

   public void trimReply()
   {
      id = null;
   }

   public void write(ObjectOutputStream out) throws IOException
   {
      super.write(out);
      int hasId = id != null ? 1 : 0;
      out.writeByte(hasId);
      if (hasId == 1)
         out.writeObject(id);
   }

   public void read(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      super.read(in);
      int hasId = in.readByte();
      if (hasId == 1)
         id = (String) in.readObject();
   }
}
