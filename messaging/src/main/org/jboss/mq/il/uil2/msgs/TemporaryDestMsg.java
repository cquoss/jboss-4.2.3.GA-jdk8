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

import javax.jms.Destination;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57198 $
 */
public class TemporaryDestMsg extends BaseMsg
{
   private Destination dest;

   public TemporaryDestMsg(boolean isQueue)
   {
      super(isQueue ? MsgTypes.m_getTemporaryQueue : MsgTypes.m_getTemporaryTopic);
   }

   public TemporaryQueue getQueue()
   {
      return (TemporaryQueue) dest;
   }
   public TemporaryTopic getTopic()
   {
      return (TemporaryTopic) dest;
   }
   public void setDest(Destination dest)
   {
      this.dest = dest;
   }

   public void write(ObjectOutputStream out) throws IOException
   {
      super.write(out);
      out.writeObject(dest);
   }
   public void read(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      super.read(in);
      dest = (Destination) in.readObject();
   }
}
