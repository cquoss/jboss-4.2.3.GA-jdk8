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
package org.jboss.mq.il.oil;

import java.util.Properties;

import org.jboss.mq.il.ServerIL;
import org.jboss.mq.il.ServerILFactory;
import java.net.InetAddress;

/**
 * Factory class to produce OILServerIL objects.
 *
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 * @version   $Revision: 57198 $
 */
public final class OILServerILFactory
   implements ServerILFactory
{

   final public static String SERVER_IL_FACTORY = OILServerILFactory.class.getName();
   final public static String CLIENT_IL_SERVICE = OILClientILService.class.getName();  
   final public static String OIL_ADDRESS_KEY = "OIL_ADDRESS_KEY";
   final public static String OIL_PORT_KEY = "OIL_PORT_KEY";
   final public static String OIL_TCPNODELAY_KEY = "OIL_TCPNODELAY_KEY";

   private ServerIL serverIL;

   /**
    * @see ServerILFactory#init(Properties)
    */
   public void init(Properties props)
      throws Exception
   {
   	
      String t = props.getProperty(OIL_ADDRESS_KEY);
      if (t == null)
         throw new javax.jms.JMSException("A required connection property was not set: " + OIL_ADDRESS_KEY);
      InetAddress address = InetAddress.getByName(t);

      t = props.getProperty(OIL_PORT_KEY);
      if (t == null)
         throw new javax.jms.JMSException("A required connection property was not set: " + OIL_PORT_KEY);
      int port = Integer.parseInt(t);
      
      boolean enableTcpNoDelay=false;
      t = props.getProperty(OIL_TCPNODELAY_KEY);
      if (t != null) 
         enableTcpNoDelay = t.equals("yes");

      String clientSocketFactoryName = null;
      serverIL = new OILServerIL(address, port, clientSocketFactoryName, enableTcpNoDelay);

   }

   /**
    * @see ServerILFactory#getServerIL()
    */
   public ServerIL getServerIL()
      throws Exception
   {
      return serverIL;
   }

}
