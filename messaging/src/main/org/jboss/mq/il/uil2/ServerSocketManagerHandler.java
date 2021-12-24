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

import java.rmi.RemoteException;
import javax.jms.Destination;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;
import org.jboss.mq.ConnectionToken;
import org.jboss.mq.AcknowledgementRequest;
import org.jboss.mq.Recoverable;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.SpyDestination;
import org.jboss.mq.TransactionRequest;
import org.jboss.mq.DurableSubscriptionID;
import org.jboss.mq.il.uil2.msgs.BaseMsg;
import org.jboss.mq.il.uil2.msgs.ConnectionTokenMsg;
import org.jboss.mq.il.uil2.msgs.AcknowledgementRequestMsg;
import org.jboss.mq.il.uil2.msgs.MsgTypes;
import org.jboss.mq.il.uil2.msgs.AddMsg;
import org.jboss.mq.il.uil2.msgs.BrowseMsg;
import org.jboss.mq.il.uil2.msgs.CheckIDMsg;
import org.jboss.mq.il.uil2.msgs.CreateDestMsg;
import org.jboss.mq.il.uil2.msgs.DeleteTemporaryDestMsg;
import org.jboss.mq.il.uil2.msgs.GetIDMsg;
import org.jboss.mq.il.uil2.msgs.RecoverMsg;
import org.jboss.mq.il.uil2.msgs.TemporaryDestMsg;
import org.jboss.mq.il.uil2.msgs.ReceiveMsg;
import org.jboss.mq.il.uil2.msgs.EnableConnectionMsg;
import org.jboss.mq.il.uil2.msgs.SubscribeMsg;
import org.jboss.mq.il.uil2.msgs.TransactMsg;
import org.jboss.mq.il.uil2.msgs.UnsubscribeMsg;
import org.jboss.mq.il.uil2.msgs.DeleteSubscriptionMsg;
import org.jboss.mq.il.uil2.msgs.CheckUserMsg;
import org.jboss.mq.il.uil2.msgs.PingMsg;
import org.jboss.mq.il.Invoker;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;

/** This is the SocketManager callback handler for the UIL2 server side
 * socket. This handles messages that are requests from clients.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 61545 $
 */
public class ServerSocketManagerHandler implements MsgTypes, SocketManagerHandler
{
   private static Logger log = Logger.getLogger(ServerSocketManagerHandler.class);

   private ConnectionToken connectionToken;
   private Invoker server;
   private SocketManager socketMgr;
   private UILServerILService uilServerILService;
   private SynchronizedBoolean closed = new SynchronizedBoolean(false);

   public ServerSocketManagerHandler(Invoker server, SocketManager socketMgr, UILServerILService uilServerILService)
   {
      this.server = server;
      this.socketMgr = socketMgr;
      this.uilServerILService = uilServerILService;
   }

   /** The callback from the SocketManager
    * @param msg
    */
   public void handleMsg(BaseMsg msg)
      throws Exception
   {
      boolean trace = log.isTraceEnabled();
      int msgType = msg.getMsgType();
      if (trace)
         log.trace("Begin handleMsg, msgType: " + msgType);

      switch (msgType)
      {
         case m_setSpyDistributedConnection:
            log.debug("Setting up the UILClientIL Connection");
            ConnectionTokenMsg cmsg = (ConnectionTokenMsg) msg;
            connectionToken = cmsg.getToken();
            UILClientIL clientIL = (UILClientIL) connectionToken.clientIL;
            clientIL.setSocketMgr(socketMgr);
            socketMgr.sendReply(msg);
            log.debug("The UILClientIL Connection is set up");
            break;
         case m_acknowledge:
            AcknowledgementRequestMsg ackmsg = (AcknowledgementRequestMsg) msg;
            AcknowledgementRequest ack = ackmsg.getAck();
            server.acknowledge(connectionToken, ack);
            // We send the reply although on newer clients it is ignored.
            socketMgr.sendReply(msg);
            break;
         case m_addMessage:
            AddMsg amsg = (AddMsg) msg;
            server.addMessage(connectionToken, amsg.getMsg());
            socketMgr.sendReply(msg);
            break;
         case m_browse:
            BrowseMsg bmsg = (BrowseMsg) msg;
            SpyMessage[] msgs = server.browse(connectionToken, bmsg.getDest(), bmsg.getSelector());
            bmsg.setMessages(msgs);
            socketMgr.sendReply(msg);
            break;
         case m_checkID:
            CheckIDMsg idmsg = (CheckIDMsg) msg;
            String ID = idmsg.getID();
            server.checkID(ID);
            if (connectionToken != null)
               connectionToken.setClientID(ID);
            socketMgr.sendReply(msg);
            break;
         case m_connectionClosing:
            server.connectionClosing(connectionToken);
            closed.set(true);
            socketMgr.sendReply(msg);
            socketMgr.stop();
            break;
         case m_createQueue:
            CreateDestMsg cqmsg = (CreateDestMsg) msg;
            Destination queue = server.createQueue(connectionToken, cqmsg.getName());
            cqmsg.setDest(queue);
            socketMgr.sendReply(msg);
            break;
         case m_createTopic:
            CreateDestMsg ctmsg = (CreateDestMsg) msg;
            Destination topic = server.createTopic(connectionToken, ctmsg.getName());
            ctmsg.setDest(topic);
            socketMgr.sendReply(msg);
            break;
         case m_deleteTemporaryDestination:
            DeleteTemporaryDestMsg dtdmsg = (DeleteTemporaryDestMsg) msg;
            SpyDestination tmpdest = dtdmsg.getDest();
            server.deleteTemporaryDestination(connectionToken, tmpdest);
            socketMgr.sendReply(msg);
            break;
         case m_getID:
            GetIDMsg gidmsg = (GetIDMsg) msg;
            String gid = server.getID();
            if (connectionToken != null)
               connectionToken.setClientID(gid);
            gidmsg.setID(gid);
            socketMgr.sendReply(msg);
            break;
         case m_getTemporaryQueue:
            TemporaryDestMsg tqmsg = (TemporaryDestMsg) msg;
            Destination tmpQueue = server.getTemporaryQueue(connectionToken);
            tqmsg.setDest(tmpQueue);
            socketMgr.sendReply(msg);
            break;
         case m_getTemporaryTopic:
            TemporaryDestMsg ttmsg = (TemporaryDestMsg) msg;
            Destination tmpTopic = server.getTemporaryTopic(connectionToken);
            ttmsg.setDest(tmpTopic);
            socketMgr.sendReply(msg);
            break;
         case m_receive:
            ReceiveMsg rmsg = (ReceiveMsg) msg;
            SpyMessage reply = server.receive(connectionToken, rmsg.getSubscriberID(), rmsg.getWait());
            rmsg.setMessage(reply);
            socketMgr.sendReply(msg);
            break;
         case m_setEnabled:
            EnableConnectionMsg ecmsg = (EnableConnectionMsg) msg;
            server.setEnabled(connectionToken, ecmsg.isEnabled());
            socketMgr.sendReply(msg);
            break;
         case m_subscribe:
            SubscribeMsg smsg = (SubscribeMsg) msg;
            server.subscribe(connectionToken, smsg.getSubscription());
            socketMgr.sendReply(msg);
            break;
         case m_transact:
            TransactMsg tmsg = (TransactMsg) msg;
            TransactionRequest trans = tmsg.getRequest();
            server.transact(connectionToken, trans);
            socketMgr.sendReply(msg);
            break;
         case m_recover:
            RecoverMsg recmsg = (RecoverMsg) msg;
            int flags = recmsg.getFlags();
            if (server instanceof Recoverable)
            {
               Recoverable recoverable = (Recoverable) server;
               Xid[] xids = recoverable.recover(connectionToken, flags);
               recmsg.setXids(xids);
               socketMgr.sendReply(msg);
               break;
            }
            throw new IllegalStateException("Invoker does not implement recoverable " + server);
         case m_unsubscribe:
            UnsubscribeMsg umsg = (UnsubscribeMsg) msg;
            server.unsubscribe(connectionToken, umsg.getSubscriptionID());
            socketMgr.sendReply(msg);
            break;
         case m_destroySubscription:
            DeleteSubscriptionMsg dsmsg = (DeleteSubscriptionMsg) msg;
            DurableSubscriptionID dsub = dsmsg.getSubscriptionID();
            server.destroySubscription(connectionToken, dsub);
            socketMgr.sendReply(msg);
            break;
         case m_checkUser:
            CheckUserMsg cumsg = (CheckUserMsg) msg;
            String uid = server.checkUser(cumsg.getUsername(), cumsg.getPassword());
            cumsg.setID(uid);
            cumsg.clearPassword();
            socketMgr.sendReply(msg);
            break;
         case m_ping:
            PingMsg ping = (PingMsg) msg;
            server.ping(connectionToken, ping.getTime());
            break;
         case m_pong:
            break;
         // Ignore, this is an old client, that still wants to send us replies
         case m_receiveRequest:
            break;
         case m_authenticate:
            CheckUserMsg cumsg2 = (CheckUserMsg) msg;
            String sessionID = server.authenticate(cumsg2.getUsername(), cumsg2.getPassword());
            cumsg2.setID(sessionID);
            cumsg2.clearPassword();
            socketMgr.sendReply(msg);
            break;
         default:
            throw new RemoteException("Unknown msgType: "+msgType);
      }
      if (trace)
         log.trace("End handleMsg, msgType: " + msgType);
   }

   public void onStreamNotification(Object stream, int size)
   {
   }

   public void asynchFailure(String error, Throwable e)
   {
      log.debug(error, e);
   }

   public void close()
   {
      try
      {
         uilServerILService.removeHandler(this);
         if (closed.get() == false)
            server.connectionClosing(connectionToken);
      }
      catch (Exception e)
      {
         log.debug("Error closing connection: ", e);
      }
   }
}
