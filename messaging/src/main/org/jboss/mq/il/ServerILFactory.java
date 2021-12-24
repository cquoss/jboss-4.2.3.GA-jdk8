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
package org.jboss.mq.il;

import java.util.Properties;

/**
 * This interface is used to define a factory to produce ServerIL objects. This
 * is used by the client in the GenericConnectionFactory class. Implementations
 * should provide a default constructor.
 * 
 * @author Hiram Chirino (Cojonudo14@hotmail.com) 
 * @author Adrian Brock (adrian@jboss.com)
 * @version $Revision: 57198 $
 */
public interface ServerILFactory
{

	/**
	 * Constant used to identify the property that holds the ServerILFactor
	 * class name
	 */
	public final static String SERVER_IL_FACTORY_KEY = "ServerILFactory";

	/**
	 * Constant used to identify the property that holds the ClientILService
	 * class name
	 */
	public final static String CLIENT_IL_SERVICE_KEY = "ClientILService";

	/**
	 * Constant used to identify the property that holds time period between
	 * server pings.
	 */
	public final static String PING_PERIOD_KEY = "PingPeriod";

	/**
	 * Constant used to identify the property that holds the client id
	 */
	public final static String CLIENTID = "ClientID";

	// init is called before any calls are made to getServerIL()
	public void init(Properties props) throws Exception;

	// must return a instance of ServerIL or else throw an Exception.
	public ServerIL getServerIL() throws Exception;
}
