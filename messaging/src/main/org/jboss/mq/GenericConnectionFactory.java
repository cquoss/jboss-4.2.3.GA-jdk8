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
package org.jboss.mq;

import java.io.Serializable;
import java.util.Properties;

import javax.jms.JMSException;

import org.jboss.logging.Logger;
import org.jboss.mq.il.ClientILService;
import org.jboss.mq.il.ServerIL;
import org.jboss.mq.il.ServerILFactory;

/**
 * The RMI implementation of the DistributedConnectionFactory object
 * 
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class GenericConnectionFactory implements Serializable
{
   // Constants -----------------------------------------------------

   /** The serialVersionUID */
   private static final long serialVersionUID = 2288420610006129296L;

   /** The log */
   static Logger log = Logger.getLogger(GenericConnectionFactory.class);

   // Attributes ----------------------------------------------------

   /**
	 * An instance of the ServerIL, once it is setup, we make clones every
	 */
   private ServerIL server;

   /**
	 * Holds all the information need to connect to the server.
	 */
   private Properties connectionProperties;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
	 * The constructor takes a ServerIL and the Connection Properties
	 * parameters, The connection properties are allways required since they are
	 * used to setup the ClientIL, but the ServerIL can be null if the
	 * connection properties defines a ServerILFactory so that the SeverIL can
	 * be created on the client side. The ServerIL paramter is usefull for IL
	 * such as RMI or the JVM IL since trying to explicity create a connection
	 * to them is not strait forward.
	 * 
	 * @param server the serverIL
	 * @param props the connection properties
	 */
   public GenericConnectionFactory(ServerIL server, Properties props)
   {
      this.server = server;
      this.connectionProperties = props;
   }
   
   // Public --------------------------------------------------------

   /**
    * For testing
    */
   public Properties getProperties()
   {
      return connectionProperties;
   }
   
   /**
    * Initialise the connection
    * 
    * @param connection the connection to initialise
    */
   public void initialise(Connection connection) throws JMSException
   {
      String clientID = connectionProperties.getProperty(ServerILFactory.CLIENTID);
      if (clientID != null)
         connection.clientID = clientID;
   }
   
   /**
	 * Creates a new instance of the ClientILService
	 * 
	 * @param connection the connection
	 * @return the client il
	 * @exception Exception for any error
	 */
   public ClientILService createClientILService(Connection connection) throws Exception
   {
      // This is a good time to setup the PingPeriod
      String pingPeriod = connectionProperties.getProperty(ServerILFactory.PING_PERIOD_KEY, "" + connection.pingPeriod);
      connection.pingPeriod = Long.parseLong(pingPeriod);

      // Setup the client connection.
      String clientILServiceCN = connectionProperties.getProperty(ServerILFactory.CLIENT_IL_SERVICE_KEY);
      ClientILService service = (ClientILService) Class.forName(clientILServiceCN).newInstance();
      service.init(connection, connectionProperties);

      if (log.isTraceEnabled())
         log.trace("Handing out ClientIL: " + clientILServiceCN);

      return service;
   }

   /**
	 * Creates a new instance of the ServerIL
	 * 
	 * @return the server il
	 * @exception JMSException for any error
	 */
   public ServerIL createServerIL() throws JMSException
   {
      try
      {
         // The server was not set, so lets try to set it up with
         // A ServerILFactory
         if (server == null)
         {
            String className = connectionProperties.getProperty(ServerILFactory.SERVER_IL_FACTORY_KEY);
            ServerILFactory factory = (ServerILFactory) Class.forName(className).newInstance();
            factory.init(connectionProperties);

            server = factory.getServerIL();
         }

         // We clone because one ConnectionFactory instance can be
         // used to produce multiple connections.
         return server.cloneServerIL();
      }
      catch (Exception e)
      {
         throw new SpyJMSException("Could not connect to the server", e);
      }
   }
   
   // Object overrides ----------------------------------------------
   
   public String toString()
   {
      return "GenericConnectionFactory[server=" + server + " connectionProperties=" + connectionProperties + "]";
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}