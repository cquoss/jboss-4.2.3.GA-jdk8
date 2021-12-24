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
import java.io.IOException;
import java.net.InetAddress;
import java.net.ConnectException;
import java.net.Socket;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;
import javax.net.SocketFactory;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;
import org.jboss.mq.AcknowledgementRequest;
import org.jboss.mq.Connection;
import org.jboss.mq.ConnectionToken;
import org.jboss.mq.DurableSubscriptionID;
import org.jboss.mq.Recoverable;
import org.jboss.mq.SpyDestination;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.TransactionRequest;
import org.jboss.mq.il.ServerIL;
import org.jboss.mq.il.uil2.msgs.MsgTypes;
import org.jboss.mq.il.uil2.msgs.ConnectionTokenMsg;
import org.jboss.mq.il.uil2.msgs.EnableConnectionMsg;
import org.jboss.mq.il.uil2.msgs.GetIDMsg;
import org.jboss.mq.il.uil2.msgs.RecoverMsg;
import org.jboss.mq.il.uil2.msgs.TemporaryDestMsg;
import org.jboss.mq.il.uil2.msgs.AcknowledgementRequestMsg;
import org.jboss.mq.il.uil2.msgs.AddMsg;
import org.jboss.mq.il.uil2.msgs.BrowseMsg;
import org.jboss.mq.il.uil2.msgs.CheckIDMsg;
import org.jboss.mq.il.uil2.msgs.CheckUserMsg;
import org.jboss.mq.il.uil2.msgs.CloseMsg;
import org.jboss.mq.il.uil2.msgs.CreateDestMsg;
import org.jboss.mq.il.uil2.msgs.DeleteTemporaryDestMsg;
import org.jboss.mq.il.uil2.msgs.DeleteSubscriptionMsg;
import org.jboss.mq.il.uil2.msgs.PingMsg;
import org.jboss.mq.il.uil2.msgs.ReceiveMsg;
import org.jboss.mq.il.uil2.msgs.SubscribeMsg;
import org.jboss.mq.il.uil2.msgs.TransactMsg;
import org.jboss.mq.il.uil2.msgs.UnsubscribeMsg;

/** The UILServerIL is created on the server and copied to the client during
 * connection factory lookups. It represents the transport interface to the
 * JMS server.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57198 $
 */
public class UILServerIL
   implements Cloneable, MsgTypes, Serializable, ServerIL, Recoverable
{
   /** @since 1.7, at least jboss-3.2.5, jboss-4.0.0 */
   private static final long serialVersionUID = 853594001646066224L;
   private static Logger log = Logger.getLogger(UILServerIL.class);

   /** The org.jboss.mq.il.uil2.useServerHost system property allows a client to
    * to connect to the host name rather than the ip address
    */
   private final static String USE_SERVER_HOST = "org.jboss.mq.il.uil2.useServerHost";

   /** The org.jboss.mq.il.uil2.localAddr system property allows a client to
    *define the local interface to which its sockets should be bound
    */
   private final static String LOCAL_ADDR = "org.jboss.mq.il.uil2.localAddr";
   /** The org.jboss.mq.il.uil2.localPort system property allows a client to
    *define the local port to which its sockets should be bound
    */
   private final static String LOCAL_PORT = "org.jboss.mq.il.uil2.localPort";
   /** The org.jboss.mq.il.uil2.serverAddr system property allows a client to
    * override the address to which it attempts to connect to. This is useful
    * for networks where NAT is ocurring between the client and jms server.
    */
   private final static String SERVER_ADDR = "org.jboss.mq.il.uil2.serverAddr";
   /** The org.jboss.mq.il.uil2.serverPort system property allows a client to
    * override the port to which it attempts to connect. This is useful for
    * for networks where port forwarding is ocurring between the client and jms
    * server.
    */
   private final static String SERVER_PORT = "org.jboss.mq.il.uil2.serverPort";
   /** The org.jboss.mq.il.uil2.retryCount controls the number of attempts to
    * retry connecting to the jms server. Retries are only made for 
    * java.net.ConnectException failures. A value <= 0 means no retry atempts
    * will be made.
    */
   private final static String RETRY_COUNT = "org.jboss.mq.il.uil2.retryCount";
   /** The org.jboss.mq.il.uil2.retryDelay controls the delay in milliseconds
    * between retries due to ConnectException failures.
    */
   private final static String RETRY_DELAY = "org.jboss.mq.il.uil2.retryDelay";

   /** The server host name/IP to connect to as defined by the jms server.
    */
   private InetAddress addr;
   /** The server port to connect to as defined by the jms server.
    */
   private int port;
   /** The name of the class implementing the javax.net.SocketFactory to
    * use for creating the client socket.
    */
   private String socketFactoryName;

   /**
    * If the TcpNoDelay option should be used on the socket.
    */
   private boolean enableTcpNoDelay = false;

   /**
    * The client side read timeout
    */
   private int soTimeout = 0;

   /**
    * The connect address
    */
   private String connectAddress;

   /**
    * The connect port
    */
   private int connectPort = 0;

   /**
    * The buffer size.
    */
   private int bufferSize;

   /**
    * The chunk size.
    */
   private int chunkSize;

   /** The local interface name/IP to use for the client
    */
   private transient InetAddress localAddr;
   /** The local port to use for the client
    */
   private transient int localPort;

   /**
    * Description of the Field
    */
   protected transient Socket socket;
   /**
    * Description of the Field
    */
   protected transient SocketManager socketMgr;

   public UILServerIL(InetAddress addr, int port, String socketFactoryName,
      boolean enableTcpNoDelay, int bufferSize, int chunkSize, int soTimeout, String connectAddress, int connectPort)
      throws Exception
   {
      this.addr = addr;
      this.port = port;
      this.socketFactoryName = socketFactoryName;
      this.enableTcpNoDelay = enableTcpNoDelay;
      this.bufferSize = bufferSize;
      this.chunkSize = chunkSize;
      this.soTimeout = soTimeout;
      this.connectAddress = connectAddress;
      this.connectPort = connectPort;
   }

   public void setConnectionToken(ConnectionToken dest)
          throws Exception
   {
      ConnectionTokenMsg msg = new ConnectionTokenMsg(dest);
      getSocketMgr().sendMessage(msg);
   }

   public void setEnabled(ConnectionToken dc, boolean enabled)
          throws JMSException, Exception
   {
      EnableConnectionMsg msg = new EnableConnectionMsg(enabled);
      getSocketMgr().sendMessage(msg);
   }

   public String getID()
          throws Exception
   {
      GetIDMsg msg = new GetIDMsg();
      getSocketMgr().sendMessage(msg);
      String id = msg.getID();
      return id;
   }

   public TemporaryQueue getTemporaryQueue(ConnectionToken dc)
          throws JMSException, Exception
   {
      TemporaryDestMsg msg = new TemporaryDestMsg(true);
      getSocketMgr().sendMessage(msg);
      TemporaryQueue dest = msg.getQueue();
      return dest;
   }

   public TemporaryTopic getTemporaryTopic(ConnectionToken dc)
          throws JMSException, Exception
   {
      TemporaryDestMsg msg = new TemporaryDestMsg(false);
      getSocketMgr().sendMessage(msg);
      TemporaryTopic dest = msg.getTopic();
      return dest;
   }

   public void acknowledge(ConnectionToken dc, AcknowledgementRequest item)
          throws JMSException, Exception
   {
      AcknowledgementRequestMsg msg = new AcknowledgementRequestMsg(item);
      if (item.isAck())
         getSocketMgr().sendMessage(msg);
      else
         getSocketMgr().sendOneWay(msg);
   }

   public void addMessage(ConnectionToken dc, SpyMessage val)
          throws Exception
   {
      AddMsg msg = new AddMsg(val);
      getSocketMgr().sendMessage(msg);
   }

   public SpyMessage[] browse(ConnectionToken dc, Destination dest, String selector)
          throws JMSException, Exception
   {
      BrowseMsg msg = new BrowseMsg(dest, selector);
      getSocketMgr().sendMessage(msg);
      SpyMessage[] msgs = msg.getMessages();
      return msgs;
   }

   public void checkID(String id)
          throws JMSException, Exception
   {
      CheckIDMsg msg = new CheckIDMsg(id);
      getSocketMgr().sendMessage(msg);
   }

   public String checkUser(String username, String password)
          throws JMSException, Exception
   {
      CheckUserMsg msg = new CheckUserMsg(username, password, false);
      getSocketMgr().sendMessage(msg);
      String clientID = msg.getID();
      return clientID;
   }

   public String authenticate(String username, String password)
          throws JMSException, Exception
   {
      CheckUserMsg msg = new CheckUserMsg(username, password, true);
      getSocketMgr().sendMessage(msg);
      String sessionID = msg.getID();
      return sessionID;
   }

   public Object clone()
          throws CloneNotSupportedException
   {
      return super.clone();
   }

   public ServerIL cloneServerIL()
          throws Exception
   {
      return (ServerIL)clone();
   }

   public void connectionClosing(ConnectionToken dc)
          throws JMSException, Exception
   {
      CloseMsg msg = new CloseMsg();
      try
      {
         getSocketMgr().sendMessage(msg);
      }
      catch (IOException ignored)
      {
      }
      destroyConnection();
   }

   public Queue createQueue(ConnectionToken dc, String destName)
          throws JMSException, Exception
   {
      CreateDestMsg msg = new CreateDestMsg(destName, true);
      getSocketMgr().sendMessage(msg);
      Queue dest = msg.getQueue();
      return dest;
   }

   public Topic createTopic(ConnectionToken dc, String destName)
          throws JMSException, Exception
   {
      CreateDestMsg msg = new CreateDestMsg(destName, false);
      getSocketMgr().sendMessage(msg);
      Topic dest = msg.getTopic();
      return dest;
   }

   public void deleteTemporaryDestination(ConnectionToken dc, SpyDestination dest)
          throws JMSException, Exception
   {
      DeleteTemporaryDestMsg msg = new DeleteTemporaryDestMsg(dest);
      getSocketMgr().sendMessage(msg);
   }

   public void destroySubscription(ConnectionToken dc,DurableSubscriptionID id)
          throws JMSException, Exception
   {
      DeleteSubscriptionMsg msg = new DeleteSubscriptionMsg(id);
      getSocketMgr().sendMessage(msg);
   }

   public void ping(ConnectionToken dc, long clientTime)
          throws Exception
   {
      PingMsg msg = new PingMsg(clientTime, true);
      msg.getMsgID();
      getSocketMgr().sendReply(msg);
   }

   public SpyMessage receive(ConnectionToken dc, int subscriberId, long wait)
          throws Exception, Exception
   {
      ReceiveMsg msg = new ReceiveMsg(subscriberId, wait);
      getSocketMgr().sendMessage(msg);
      SpyMessage reply = msg.getMessage();
      return reply;
   }

   public void subscribe(ConnectionToken dc, org.jboss.mq.Subscription s)
          throws JMSException, Exception
   {
      SubscribeMsg msg = new SubscribeMsg(s);
      getSocketMgr().sendMessage(msg);
   }

   public void transact(ConnectionToken dc, TransactionRequest t)
          throws JMSException, Exception
   {
      TransactMsg msg = new TransactMsg(t);
      getSocketMgr().sendMessage(msg);
   }

   public Xid[] recover(ConnectionToken dc, int flags) throws Exception
   {
      RecoverMsg msg = new RecoverMsg(flags);
      getSocketMgr().sendMessage(msg);
      Xid[] reply = msg.getXids();
      return reply;
   }

   public void unsubscribe(ConnectionToken dc, int subscriptionID)
          throws JMSException, Exception
   {
      UnsubscribeMsg msg = new UnsubscribeMsg(subscriptionID);
      getSocketMgr().sendMessage(msg);
   }

   final SocketManager getSocketMgr()
      throws Exception
   {
      if( socketMgr == null )
         createConnection();
      return socketMgr;
   }

   protected void checkConnection()
          throws Exception
   {
      if (socketMgr == null)
      {
         createConnection();
      }
   }

   /**
    * Used to establish a new connection to the server
    *
    * @exception Exception  Description of Exception
    */
   protected void createConnection()
          throws Exception
   {
      boolean tracing = log.isTraceEnabled();

      /** Attempt to load the socket factory and if this fails, use the
       * default socket factory impl.
       */
      SocketFactory socketFactory = null;
      if( socketFactoryName != null )
      {
         try
         {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class factoryClass = loader.loadClass(socketFactoryName);
            socketFactory = (SocketFactory) factoryClass.newInstance();
         }
         catch(Exception e)
         {
            log.debug("Failed to load socket factory: "+socketFactoryName, e);
         }
      }
      // Use the default socket factory
      if( socketFactory == null )
      {
         socketFactory = SocketFactory.getDefault();
      }

      // Look for a local address and port as properties
      String tmp = getProperty(LOCAL_ADDR);
      if( tmp != null )
         this.localAddr = InetAddress.getByName(tmp);
      tmp = getProperty(LOCAL_PORT);
      if( tmp != null )
         this.localPort = Integer.parseInt(tmp);

      // Look for client side overrides of the server address/port
      InetAddress serverAddr = addr;
      int serverPort = port;
      tmp = getProperty(SERVER_ADDR);
      if (tmp == null)
         tmp = connectAddress;
      if( tmp != null )
         serverAddr = InetAddress.getByName(tmp);
      tmp = getProperty(SERVER_PORT);
      if( tmp != null )
         serverPort = Integer.parseInt(tmp);
      else if (connectPort != 0)
         serverPort = connectPort;
      
      String useHostNameProp = getProperty(USE_SERVER_HOST);
      String serverHost = serverAddr.getHostAddress();
      if (Boolean.valueOf(useHostNameProp).booleanValue())
         serverHost = serverAddr.getHostName();
      
      int retries = 0;
      // Default to 10 retries, no delay in the absence of user override
      int maxRetries = 10;
      tmp = getProperty(RETRY_COUNT);
      if( tmp != null )
         maxRetries = Integer.parseInt(tmp);
      long retryDelay = 0;
      tmp = getProperty(RETRY_DELAY);
      if( tmp != null )
      {
         retryDelay = Long.parseLong(tmp);
         if( retryDelay < 0 )
            retryDelay = 0;
      }
      if( tracing )
         log.trace("Begin connect loop, maxRetries="+maxRetries+", delay="+retryDelay);

      while (true)
      {
         try
         {
            if( tracing )
            {
               log.trace("Connecting with addr="+serverHost+", port="+serverPort
                  + ", localAddr="+localAddr+", localPort="+localPort
                  + ", socketFactory="+socketFactory
                  + ", enableTcpNoDelay="+enableTcpNoDelay
                  + ", bufferSize="+bufferSize
                  + ", chunkSize="+chunkSize
                  );
            }
            if( localAddr != null )
               socket = socketFactory.createSocket(serverHost, serverPort, localAddr, localPort);
            else
               socket = socketFactory.createSocket(serverHost, serverPort);
            break;
         }
         catch (ConnectException e)
         {
            if (++retries > maxRetries)
               throw e;
            if( tracing )
               log.trace("Failed to connect, retries="+retries, e);
         }
         try
         {
            Thread.sleep(retryDelay);
         }
         catch(InterruptedException e)
         {
            break;
         }
      }

      socket.setTcpNoDelay(enableTcpNoDelay);
      if (soTimeout != 0)
         socket.setSoTimeout(soTimeout);
      socketMgr = new SocketManager(socket);
      socketMgr.setBufferSize(bufferSize);
      socketMgr.setChunkSize(chunkSize);
      socketMgr.start(Connection.getThreadGroup());
   }

   /**
    * Used to close the current connection with the server
    *
    */
   protected void destroyConnection()
   {
      try
      {
        if( socket != null )
        {
           try
           {
              socketMgr.stop();
           }
           finally
           {
              socket.close();
           }
        }
      }
      catch(IOException ignore)
      {
      }
   }

   private String getProperty(String name)
   {
      String value = null;
      try
      {
         value = System.getProperty(name);
      }
      catch (Throwable ignored)
      {
         log.trace("Cannot retrieve system property " + name);
      }
      return value;
   }
}
