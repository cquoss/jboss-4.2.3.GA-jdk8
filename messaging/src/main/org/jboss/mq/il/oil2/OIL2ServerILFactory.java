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

import java.util.Properties;

import org.jboss.logging.Logger;
import org.jboss.mq.il.ServerIL;
import org.jboss.mq.il.ServerILFactory;

/**
 * Factory class to produce OIL2ServerIL objects.
 *
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 * @version   $Revision: $
 */
public final class OIL2ServerILFactory
   implements ServerILFactory
{
   private final static Logger log = Logger.getLogger(OIL2ServerILFactory.class);

   final public static String SERVER_IL_FACTORY   = OIL2ServerILFactory.class.getName();
   final public static String CLIENT_IL_SERVICE   = OIL2ClientILService.class.getName();  
   final public static String OIL2_ADDRESS_KEY    = "OIL2_ADDRESS_KEY";
   final public static String OIL2_PORT_KEY       = "OIL2_PORT_KEY";
   final public static String OIL2_TCPNODELAY_KEY = "OIL2_TCPNODELAY_KEY";

   private ServerIL serverIL;

   /**
    * @see ServerILFactory#init(Properties)
    */
   public void init(Properties props)
      throws Exception
   {
   	
      String address = props.getProperty(OIL2_ADDRESS_KEY);
      if (address == null)
         throw new javax.jms.JMSException("A required connection property was not set: " + OIL2_ADDRESS_KEY);

      String t = props.getProperty(OIL2_PORT_KEY);
      if (t == null)
         throw new javax.jms.JMSException("A required connection property was not set: " + OIL2_PORT_KEY);
      int port = Integer.parseInt(t);
      
      boolean enableTcpNoDelay=false;
      t = props.getProperty(OIL2_TCPNODELAY_KEY);
      if (t != null) 
         enableTcpNoDelay = t.equals("yes");

      String clientSocketFactoryName = null;
      serverIL = new OIL2ServerIL(address, port, clientSocketFactoryName, enableTcpNoDelay);

   }

   /**
    * @see ServerILFactory#getServerIL()
    */
   public ServerIL getServerIL()
      throws Exception
   {
      if( log.isTraceEnabled() )
         log.trace("Providing ServerIL: "+serverIL);
      return serverIL;
   }

}
