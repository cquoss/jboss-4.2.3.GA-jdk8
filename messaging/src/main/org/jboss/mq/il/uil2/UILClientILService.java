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

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Properties;

import org.jboss.logging.Logger;
import org.jboss.mq.Connection;
import org.jboss.mq.ReceiveRequest;
import org.jboss.mq.SpyDestination;
import org.jboss.mq.il.ClientILService;
import org.jboss.mq.il.ClientIL;
import org.jboss.mq.il.uil2.msgs.MsgTypes;
import org.jboss.mq.il.uil2.msgs.BaseMsg;
import org.jboss.mq.il.uil2.msgs.ReceiveRequestMsg;
import org.jboss.mq.il.uil2.msgs.DeleteTemporaryDestMsg;
import org.jboss.mq.il.uil2.msgs.PingMsg;

/** The UILClientILService runs on the client side of a JMS server connection
 * and acts as a factory for the UILClientIL passed to the server. It also
 * handles the callbacks from the client side SocketManager.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57198 $
 */
public class UILClientILService
      implements ClientILService, MsgTypes, SocketManagerHandler
{
   static Logger log = Logger.getLogger(UILClientILService.class);

   // Attributes ----------------------------------------------------

   //the client IL
   private UILClientIL clientIL;
   //The thread that is doing the Socket reading work
   private SocketManager socketMgr;
   //A link on my connection
   private Connection connection;
   // Whether to send receive replies
   private boolean sendReceiveReplies = true;

   /**
    * getClientIL method comment.
    *
    * @return  The ClientIL value
    * @exception Exception  Description of Exception
    */
   public ClientIL getClientIL()
         throws Exception
   {
      return clientIL;
   }

   /**
    * init method comment.
    *
    * @param connection               Description of Parameter
    * @param props                    Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void init(Connection connection, Properties props)
         throws Exception
   {
      this.connection = connection;
      clientIL = new UILClientIL();
      UILServerIL serverIL = (UILServerIL) connection.getServerIL();
      socketMgr = serverIL.getSocketMgr();
      String t = props.getProperty(UILServerILFactory.UIL_BUFFERSIZE_KEY);
      if (t != null)
         socketMgr.setBufferSize(Integer.parseInt(t));
      t = props.getProperty(UILServerILFactory.UIL_CHUNKSIZE_KEY);
      if (t != null)
         socketMgr.setChunkSize(Integer.parseInt(t));
      // If we have this property, it means it is a new server
      t = props.getProperty(UILServerILFactory.UIL_RECEIVE_REPLIES_KEY);
      if (t != null)
         sendReceiveReplies = false;
      socketMgr.setHandler(this);
   }

   /** Callback from the SocketManager
    */
   public void handleMsg(BaseMsg msg)
      throws Exception
   {
      boolean trace = log.isTraceEnabled();
      int msgType = msg.getMsgType();
      if (trace)
         log.trace("Begin handleMsg, msgType: " + msgType);
      switch( msgType )
      {
         case m_acknowledge:
            // We have to ignore NACK replies because of backwards compatibility
            break;
         case m_receiveRequest:
            ReceiveRequestMsg rmsg = (ReceiveRequestMsg) msg;
            ReceiveRequest[] messages = rmsg.getMessages();
            connection.asynchDeliver(messages);
            // It is an old server that needs a reply
            if (sendReceiveReplies)
            {
               rmsg.trimTheMessages();
               socketMgr.sendReply(msg);
            }
            break;
         case m_deleteTemporaryDestination:
            DeleteTemporaryDestMsg dmsg = (DeleteTemporaryDestMsg) msg;
            SpyDestination dest = dmsg.getDest();
            connection.asynchDeleteTemporaryDestination(dest);
            socketMgr.sendReply(msg);
            break;
         case m_close:
            connection.asynchClose();
            socketMgr.sendReply(msg);
            break;
         case m_pong:
            PingMsg pmsg = (PingMsg) msg;
            long time = pmsg.getTime();
            connection.asynchPong(time);
            break;
         default:
            connection.asynchFailure("UILClientILService received bad msg: "+msg, null);
      }
      if (trace)
         log.trace("End handleMsg");
   }

   /**
    *
    * @exception Exception  Description of Exception
    */
   public void start() throws Exception
   {
      log.debug("Starting");
   }

   /**
    * @exception Exception  Description of Exception
    */
   public void stop() throws Exception
   {
      log.debug("Stopping");
      socketMgr.stop();
   }

   public void onStreamNotification(Object stream, int size)
   {
      connection.asynchPong(System.currentTimeMillis());
   }

   public void asynchFailure(String error, Throwable e)
   {
      if (e instanceof Exception)
         connection.asynchFailure(error, e);
      else
         connection.asynchFailure(error, new UndeclaredThrowableException(e));
   }

   public void close()
   {
   }
}
