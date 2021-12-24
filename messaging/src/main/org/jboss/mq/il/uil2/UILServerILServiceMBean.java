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

/**
 * MBean interface.
 */
public interface UILServerILServiceMBean extends org.jboss.mq.il.ServerILJMXServiceMBean
{

   /**
    * Get the UIL server listening port
    * @return Value of property serverBindPort.
    */
   int getServerBindPort();

   /**
    * Set the UIL server listening port
    * @param serverBindPort New value of property serverBindPort.
    */
   void setServerBindPort(int serverBindPort);

   /**
    * Get the interface address the UIL2 server bind its listening port on
    */
   java.lang.String getBindAddress();

   /**
    * Set the interface address the UIL2 server bind its listening port on
    */
   void setBindAddress(java.lang.String host) throws java.net.UnknownHostException;

   /**
    * Get the interface address the UIL2 exposed to the client as the server address
    */
   java.net.InetAddress getClientAddress();

   /**
    * Set the interface address the UIL2 exposed to the client as the server address
    */
   void setClientAddress(java.net.InetAddress addr);

   /**
    * Gets the enableTcpNoDelay.
    * @return Returns a boolean
    */
   boolean getEnableTcpNoDelay();

   /**
    * Sets the enableTcpNoDelay.
    * @param enableTcpNoDelay The enableTcpNoDelay to set
    */
   void setEnableTcpNoDelay(boolean enableTcpNoDelay);

   /**
    * Gets the buffer size.
    * @return Returns an int
    */
   int getBufferSize();

   /**
    * Sets the buffer size.
    * @param size the buffer size
    */
   void setBufferSize(int size);

   /**
    * Gets the chunk size.
    * @return Returns an int
    */
   int getChunkSize();

   /**
    * Sets the chunk size.
    * @param size the chunk size
    */
   void setChunkSize(int size);

   /**
    * Gets the socket read timeout.
    * @return Returns the read timeout in milli-seconds
    */
   int getReadTimeout();

   /**
    * Sets the read time out.
    * @param timeout The read time out in milli seconds
    */
   void setReadTimeout(int timeout);

   /**
    * Gets the client socket read timeout.
    * @return Returns the read timeout in milli-seconds
    */
   int getClientReadTimeout();

   /**
    * Sets the read time out.
    * @param timeout The read time out in milli seconds
    */
   void setClientReadTimeout(int timeout);

   /**
    * Get the javax.net.SocketFactory implementation class to use on the client.
    */
   java.lang.String getClientSocketFactory();

   /**
    * Set the javax.net.SocketFactory implementation class to use on the client.
    */
   void setClientSocketFactory(java.lang.String name);

   /**
    * Set the javax.net.ServerSocketFactory implementation class to use to create the service SocketFactory.
    */
   void setServerSocketFactory(java.lang.String name) throws java.lang.Exception;

   /**
    * Get the javax.net.ServerSocketFactory implementation class to use to create the service SocketFactory.
    */
   java.lang.String getServerSocketFactory();

   /**
    * Set the security domain name to use with SSL aware socket factories
    */
   void setSecurityDomain(java.lang.String domainName);

   /**
    * Get the security domain name to use with SSL aware socket factories
    */
   java.lang.String getSecurityDomain();

   String getConnectAddress();

   void setConnectAddress(String addr);
   
   int getConnectPort();

   void setConnectPort(int port);
}
