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

import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import org.jboss.mq.AcknowledgementRequest;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57198 $
 */
public class AcknowledgementRequestMsg extends BaseMsg
{
   private AcknowledgementRequest item;

   public AcknowledgementRequestMsg()
   {
      this(new AcknowledgementRequest());
   }
   public AcknowledgementRequestMsg(AcknowledgementRequest item)
   {
      super(MsgTypes.m_acknowledge);
      this.item = item;
   }

   public AcknowledgementRequest getAck()
   {
      return item;
   }

   public void trimReply()
   {
      item = null;
   }

   public void write(ObjectOutputStream out) throws IOException
   {
      super.write(out);
      int hasItem = item != null ? 1 : 0;
      out.writeByte(hasItem);
      if (hasItem == 1)
         item.writeExternal(out);
   }
   public void read(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      super.read(in);
      int hasItem = in.readByte();
      if (hasItem == 1)
         item.readExternal(in);
   }
}
