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

import java.util.Properties;

import org.jboss.mq.il.ServerIL;
import org.jboss.mq.il.ServerILFactory;
import java.net.InetAddress;

/**
 * Factory class to produce UILServerIL objects.
 *
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 * @version   $Revision: 57198 $
 */
public class UILServerILFactory implements ServerILFactory {

   final public static String SERVER_IL_FACTORY = UILServerILFactory.class.getName();
   final public static String CLIENT_IL_SERVICE = UILClientILService.class.getName();  
   final public static String UIL_ADDRESS_KEY = "UIL_ADDRESS_KEY";
   final public static String UIL_PORT_KEY = "UIL_PORT_KEY";
   final public static String UIL_TCPNODELAY_KEY = "UIL_TCPNODELAY_KEY";
   final public static String UIL_BUFFERSIZE_KEY = "UIL_BUFFERSIZE_KEY";
   final public static String UIL_CHUNKSIZE_KEY = "UIL_CHUNKSIZE_KEY";
   final public static String UIL_RECEIVE_REPLIES_KEY = "UIL_RECEIVE_REPLIES_KEY";
   final public static String UIL_SOTIMEOUT_KEY = "UIL_SOTIMEOUT_KEY";
   final public static String UIL_CONNECTADDRESS_KEY = "UIL_CONNECTADDRESS_KEY";
   final public static String UIL_CONNECTPORT_KEY = "UIL_CONNECTPORT_KEY";
   
   private ServerIL serverIL;

   /**
    * @see ServerILFactory#init(Properties)
    */
   public void init(Properties props) throws Exception {
   	
      String t = props.getProperty(UIL_ADDRESS_KEY);
      if (t == null)
         throw new javax.jms.JMSException("A required connection property was not set: " + UIL_ADDRESS_KEY);
      InetAddress address = InetAddress.getByName(t);

      t = props.getProperty(UIL_PORT_KEY);
      if (t == null)
         throw new javax.jms.JMSException("A required connection property was not set: " + UIL_PORT_KEY);
      int port = Integer.parseInt(t);
      
      boolean enableTcpNoDelay=false;
      t = props.getProperty(UIL_TCPNODELAY_KEY);
      if (t != null) 
         enableTcpNoDelay = t.equals("yes");

      int bufferSize = 1; // 1 byte == no buffering
      t = props.getProperty(UIL_BUFFERSIZE_KEY);
      if (t != null)
         bufferSize = Integer.parseInt(t);

      int chunkSize = 0x40000000; // 2^30 bytes
      t = props.getProperty(UIL_CHUNKSIZE_KEY);
      if (t != null)
         chunkSize = Integer.parseInt(t);

      int soTimeout = 0;
      t = props.getProperty(UIL_SOTIMEOUT_KEY);
      if (t != null)
         soTimeout = Integer.parseInt(t);

      String connectAddress = props.getProperty(UIL_CONNECTADDRESS_KEY);

      int connectPort = 0;
      t = props.getProperty(UIL_CONNECTPORT_KEY);
      if (t != null)
         connectPort = Integer.parseInt(t);
      
      String clientSocketFactoryName = null;

      serverIL = new UILServerIL(address, port, clientSocketFactoryName, enableTcpNoDelay, bufferSize, chunkSize, soTimeout, connectAddress, connectPort);

   }

   /**
    * @see ServerILFactory#getServerIL()
    */
   public ServerIL getServerIL() throws Exception {
      return serverIL;
   }

}