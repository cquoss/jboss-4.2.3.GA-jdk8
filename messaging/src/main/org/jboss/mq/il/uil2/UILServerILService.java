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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.net.ServerSocketFactory;

import org.jboss.mq.il.Invoker;
import org.jboss.mq.il.ServerIL;
import org.jboss.mq.il.ServerILJMXService;
import org.jboss.mq.il.uil2.msgs.BaseMsg;
import org.jboss.mq.il.uil2.msgs.MsgTypes;
import org.jboss.security.SecurityDomain;
import org.jboss.system.server.ServerConfigUtil;

import EDU.oswego.cs.dl.util.concurrent.CopyOnWriteArrayList;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;

/** This is the server side MBean for the UIL2 transport layer.
 *
 * @author Scott.Stark@jboss.org
 * @version   $Revision: 61545 $
 *
 * @jmx:mbean extends="org.jboss.mq.il.ServerILJMXServiceMBean"
 */
public class UILServerILService extends ServerILJMXService
      implements MsgTypes, Runnable, UILServerILServiceMBean
{
   final static int SO_TIMEOUT = 5000;

   /** The security domain name to use with SSL aware socket factories.
    */
   private String securityDomain;
   /* The javax.net.SocketFactory implementation class to use on the client.
    */
   private String clientSocketFactoryName;
   /** The socket factory used to obtain the server socket.
    */
   private ServerSocketFactory serverSocketFactory;
   /** The UIL2 server socket clients connect to
    */
   private ServerSocket serverSocket;
   private UILServerIL serverIL;
   private SynchronizedBoolean running = new SynchronizedBoolean(false);
   /** The server jms listening port */
   private int serverBindPort = 0;
   /** The server jms address the listening socket binds to */
   private InetAddress bindAddress = null;
   /** The thread that manages the client connection attempts */
   private Thread acceptThread;
   /** The address passed to the client il layer as the address that should
    * be used to connect to the server.
    */
   private InetAddress clientAddress;
   /** The address passed to the client il layer as the address that should
    * be used to connect to the server.
    */
   private String connectAddress;
   /** The port passed to the client il layer as the address that should
    * be used to connect to the server.
    */
   private int connectPort;
   /**
    * If the TcpNoDelay option should be used on the socket.
    */
   private boolean enableTcpNoDelay = false;

   /**
    * The socket read timeout.
    */
   private int readTimeout = 0;

   /**
    * The client socket read timeout.
    */
   private int clientReadTimeout = 0;

   /**
    * The buffer size.
    */
   private int bufferSize = 1;

   /**
    * The chunk size.
    */
   private int chunkSize = 0x40000000;

   /**
    * The connection properties passed to the client to connect to this IL
    */
   private Properties connectionProperties;

   /** The server handlers */
   private CopyOnWriteArrayList handlers = new CopyOnWriteArrayList();
   
   /**
    * Used to construct the GenericConnectionFactory (bindJNDIReferences()
    * builds it) Sets up the connection properties need by a client to use this
    * IL
    *
    * @return   The ClientConnectionProperties value
    */
   public Properties getClientConnectionProperties()
   {
      return connectionProperties;
   }

   /**
    * Used to construct the GenericConnectionFactory (bindJNDIReferences()
    * builds it)
    *
    * @return    The ServerIL value
    * @return   ServerIL the instance of this IL
    */
   public ServerIL getServerIL()
   {
      return serverIL;
   }

   /** Client socket accept thread.
    */
   public void run()
   {
      boolean trace = log.isTraceEnabled();
      while (running.get())
      {
         Socket socket = null;
         SocketManager socketMgr = null;
         try
         {
            socket = serverSocket.accept();
            if( trace )
               log.trace("Accepted connection: "+socket);
            socket.setSoTimeout(readTimeout);
            socket.setTcpNoDelay(enableTcpNoDelay);
            socketMgr = new SocketManager(socket);
            ServerSocketManagerHandler handler = new ServerSocketManagerHandler(getJMSServer(), socketMgr, this);
            handlers.add(handler);
            socketMgr.setHandler(handler);
            socketMgr.setBufferSize(bufferSize);
            socketMgr.setChunkSize(chunkSize);
            Invoker s = getJMSServer();
            socketMgr.start(s.getThreadGroup());
         }
         catch (IOException e)
         {
            if (running.get())
               log.warn("Failed to setup client connection", e);
         }
         catch(Throwable e)
         {
            if (running.get() || trace)
               log.warn("Unexpected error in setup of client connection", e);            
         }
      }
   }

   /**
    * Starts this IL, and binds it to JNDI
    *
    * @exception Exception  Description of Exception
    */
   public void startService() throws Exception
   {
      super.startService();

      // Use the default javax.net.ServerSocketFactory if none was set
      if (serverSocketFactory == null)
         serverSocketFactory = ServerSocketFactory.getDefault();

      /* See if the server socket supports setSecurityDomain(SecurityDomain)
      if an securityDomain was specified
      */
      if (securityDomain != null)
      {
         try
         {
            InitialContext ctx = new InitialContext();
            Class ssfClass = serverSocketFactory.getClass();
            SecurityDomain domain = (SecurityDomain) ctx.lookup(securityDomain);
            Class[] parameterTypes = {SecurityDomain.class};
            Method m = ssfClass.getMethod("setSecurityDomain", parameterTypes);
            Object[] args = {domain};
            m.invoke(serverSocketFactory, args);
         }
         catch (NoSuchMethodException e)
         {
            log.error("Socket factory does not support setSecurityDomain(SecurityDomain)");
         }
         catch (Exception e)
         {
            log.error("Failed to setSecurityDomain=" + securityDomain + " on socket factory");
         }
      }

      // Create the server socket using the socket factory
      serverSocket = serverSocketFactory.createServerSocket(serverBindPort, 50, bindAddress);

      InetAddress socketAddress = serverSocket.getInetAddress();
      log.info("JBossMQ UIL service available at : " + socketAddress + ":" + serverSocket.getLocalPort());
      acceptThread = new Thread(getJMSServer().getThreadGroup(), this, "UILServerILService Accept Thread");
      running.set(true);
      acceptThread.start();

      /* We need to check the socketAddress against "0.0.0.0/0.0.0.0"
         because this is not a valid address on Win32 while it is for
         *NIX. See BugParade bug #4343286.
      */
      socketAddress = ServerConfigUtil.fixRemoteAddress(socketAddress);
      // If an explicit client bind address has been specified use it
      if( clientAddress != null )
         socketAddress = clientAddress;
      serverIL = new UILServerIL(socketAddress, serverSocket.getLocalPort(),
            clientSocketFactoryName, enableTcpNoDelay, bufferSize, chunkSize, clientReadTimeout, connectAddress, connectPort);

      // Initialize the connection poperties using the base class.
      connectionProperties = super.getClientConnectionProperties();
      connectionProperties.setProperty(UILServerILFactory.CLIENT_IL_SERVICE_KEY, UILClientILService.class.getName());
      connectionProperties.setProperty(UILServerILFactory.UIL_PORT_KEY, "" + serverSocket.getLocalPort());
      connectionProperties.setProperty(UILServerILFactory.UIL_ADDRESS_KEY, "" + socketAddress.getHostAddress());
      connectionProperties.setProperty(UILServerILFactory.UIL_TCPNODELAY_KEY, enableTcpNoDelay ? "yes" : "no");
      connectionProperties.setProperty(UILServerILFactory.UIL_BUFFERSIZE_KEY, "" + bufferSize);
      connectionProperties.setProperty(UILServerILFactory.UIL_CHUNKSIZE_KEY, "" + chunkSize);
      connectionProperties.setProperty(UILServerILFactory.UIL_RECEIVE_REPLIES_KEY, "No");
      connectionProperties.setProperty(UILServerILFactory.UIL_SOTIMEOUT_KEY, "" + clientReadTimeout);
      connectionProperties.setProperty(UILServerILFactory.UIL_CONNECTADDRESS_KEY, "" + connectAddress);
      connectionProperties.setProperty(UILServerILFactory.UIL_CONNECTPORT_KEY, "" + connectPort);

      bindJNDIReferences();
      BaseMsg.setUseJMSServerMsgIDs(true);
   }

   /**
    * Stops this IL, and unbinds it from JNDI
    */
   public void stopService()
   {
      try
      {
         running.set(false);
         unbindJNDIReferences();

         // unbind Server Socket if needed
         if (serverSocket != null)
         {
            serverSocket.close();
         }
      }
      catch (Exception e)
      {
         log.error("Exception occured when trying to stop UIL Service: ", e);
      }
      
      // Try to close any open sockets that we know about
      for (Iterator i = handlers.iterator(); i.hasNext();)
      {
         ServerSocketManagerHandler handler = (ServerSocketManagerHandler) i.next();
         if (handler != null)
         {
            try
            {
               handler.close();
            }
            catch (Throwable ignored)
            {
            }
         }
      }
   }

   protected void removeHandler(ServerSocketManagerHandler handler)
   {
      handlers.remove(handler);
   }
   
   /**
    * Get the UIL server listening port
    *
    * @return Value of property serverBindPort.
    *
    * @jmx:managed-attribute
    */
   public int getServerBindPort()
   {
      return serverBindPort;
   }

   /**
    * Set the UIL server listening port
    *
    * @param serverBindPort New value of property serverBindPort.
    *
    * @jmx:managed-attribute
    */
   public void setServerBindPort(int serverBindPort)
   {
      this.serverBindPort = serverBindPort;
   }

   /**
    * Get the interface address the UIL2 server bind its listening port on
    *
    * @jmx:managed-attribute
    */
   public String getBindAddress()
   {
      String addr = "0.0.0.0";
      if (bindAddress != null)
         addr = bindAddress.getHostName();
      return addr;
   }

   /**
    * Set the interface address the UIL2 server bind its listening port on
    *
    * @jmx:managed-attribute
    */
   public void setBindAddress(String host) throws UnknownHostException
   {
      // If host is null or empty use any address
      if (host == null || host.length() == 0)
         bindAddress = null;
      else
         bindAddress = InetAddress.getByName(host);
   }
   
   public InetAddress getClientAddress()
   {
      return clientAddress;
   }

   public void setClientAddress(InetAddress addr)
   {
      log.warn("ClientAddress has been deprecated, use ConnectAddress");
      clientAddress = addr;
   }
   
   public String getConnectAddress()
   {
      return connectAddress;
   }

   public void setConnectAddress(String addr)
   {
      connectAddress = addr;
   }
   
   public int getConnectPort()
   {
      return connectPort;
   }

   public void setConnectPort(int port)
   {
      connectPort = port;
   }

   /**
    * Gets the enableTcpNoDelay.
    * @return Returns a boolean
    *
    * @jmx:managed-attribute
    */
   public boolean getEnableTcpNoDelay()
   {
      return enableTcpNoDelay;
   }

   /**
    * Sets the enableTcpNoDelay.
    * @param enableTcpNoDelay The enableTcpNoDelay to set
    *
    * @jmx:managed-attribute
    */
   public void setEnableTcpNoDelay(boolean enableTcpNoDelay)
   {
      this.enableTcpNoDelay = enableTcpNoDelay;
   }

   /**
    * Gets the buffer size.
    * @return Returns an int
    *
    * @jmx:managed-attribute
    */
   public int getBufferSize()
   {
      return bufferSize;
   }

   /**
    * Sets the buffer size.
    * @param size the buffer size
    *
    * @jmx:managed-attribute
    */
   public void setBufferSize(int size)
   {
      this.bufferSize = size;
   }

   /**
    * Gets the chunk size.
    * @return Returns an int
    *
    * @jmx:managed-attribute
    */
   public int getChunkSize()
   {
      return chunkSize;
   }

   /**
    * Sets the chunk size.
    * @param size the chunk size
    *
    * @jmx:managed-attribute
    */
   public void setChunkSize(int size)
   {
      this.chunkSize = size;
   }

   public int getReadTimeout()
   {
      return readTimeout;
   }

   public void setReadTimeout(int timeout)
   {
      this.readTimeout = timeout;
   }

   public int getClientReadTimeout()
   {
      return clientReadTimeout;
   }

   public void setClientReadTimeout(int timeout)
   {
      this.clientReadTimeout = timeout;
   }

   /** Get the javax.net.SocketFactory implementation class to use on the
    *client.
    * @jmx:managed-attribute
    */
   public String getClientSocketFactory()
   {
      return clientSocketFactoryName;
   }

   /** Set the javax.net.SocketFactory implementation class to use on the
    *client.
    * @jmx:managed-attribute
    */
   public void setClientSocketFactory(String name)
   {
      this.clientSocketFactoryName = name;
   }

   /** Set the javax.net.ServerSocketFactory implementation class to use to
    *create the service SocketFactory.
    *@jmx:managed-attribute
    */
   public void setServerSocketFactory(String name) throws Exception
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class ssfClass = loader.loadClass(name);
      serverSocketFactory = (ServerSocketFactory) ssfClass.newInstance();
   }

   /** Get the javax.net.ServerSocketFactory implementation class to use to
    *create the service SocketFactory.
    *@jmx:managed-attribute
    */
   public String getServerSocketFactory()
   {
      String name = null;
      if (serverSocketFactory != null)
         name = serverSocketFactory.getClass().getName();
      return name;
   }

   /** Set the security domain name to use with SSL aware socket factories
    *@jmx:managed-attribute
    */
   public void setSecurityDomain(String domainName)
   {
      this.securityDomain = domainName;
   }

   /** Get the security domain name to use with SSL aware socket factories
    *@jmx:managed-attribute
    */
   public String getSecurityDomain()
   {
      return this.securityDomain;
   }
}
