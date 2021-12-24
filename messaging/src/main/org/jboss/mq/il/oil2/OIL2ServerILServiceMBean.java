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
package org.jboss.mq.il.oil2;

/**
 * MBean interface.
 */
public interface OIL2ServerILServiceMBean extends org.jboss.mq.il.ServerILJMXServiceMBean
{

   /**
    * Getter for property serverBindPort.
    * @return Value of property serverBindPort.
    */
   int getServerBindPort();

   /**
    * Setter for property serverBindPort.
    * @param serverBindPort New value of property serverBindPort.
    */
   void setServerBindPort(int serverBindPort);

   /**
    * Get the interface address the OIL server bind its listening port on.
    * @return The hostname or dotted decimal address that the service is bound to.
    */
   java.lang.String getBindAddress();

   /**
    * Set the interface address the OIL server bind its listening port on.
    * @param host The host address to bind to, if any.
    * @throws java.net.UnknownHostException Thrown if the hostname cannot be resolved to an InetAddress object.
    */
   void setBindAddress(java.lang.String host) throws java.net.UnknownHostException;

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

}
