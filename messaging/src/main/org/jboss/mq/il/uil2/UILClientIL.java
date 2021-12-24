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
package org.jboss.mq.il.uil2;

import java.io.Serializable;

import org.jboss.logging.Logger;
import org.jboss.mq.ReceiveRequest;
import org.jboss.mq.SpyDestination;
import org.jboss.mq.il.ClientIL;
import org.jboss.mq.il.uil2.msgs.MsgTypes;
import org.jboss.mq.il.uil2.msgs.DeleteTemporaryDestMsg;
import org.jboss.mq.il.uil2.msgs.PingMsg;
import org.jboss.mq.il.uil2.msgs.ReceiveRequestMsg;

/** UILClient is the server side interface for callbacks into the client. It
 * is created on the client and sent to the server via the ConnectionToken.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57198 $
 */
public class UILClientIL
   implements ClientIL, MsgTypes, Serializable
{
   static final long serialVersionUID = -2667733986731260459L;
   
   static Logger log = Logger.getLogger(UILClientIL.class);

   private transient SocketManager socketMgr;

   public void close()
          throws Exception
   {
      if (socketMgr != null)
         socketMgr.stop();
   }

   public void deleteTemporaryDestination(SpyDestination dest)
          throws Exception
   {
      DeleteTemporaryDestMsg msg = new DeleteTemporaryDestMsg(dest);
      socketMgr.sendReply(msg);
   }

   public void pong(long serverTime)
          throws Exception
   {
      PingMsg msg = new PingMsg(serverTime, false);
      msg.getMsgID();      
      socketMgr.sendReply(msg);
   }

   public void receive(ReceiveRequest messages[])
          throws Exception
   {
      ReceiveRequestMsg msg = new ReceiveRequestMsg(messages);
      // We send this one way and don't wait for a response
      socketMgr.sendReply(msg);
   }

   protected void setSocketMgr(SocketManager socketMgr)
   {
      this.socketMgr = socketMgr;
   }
}
