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
package org.jboss.jms.client;

import java.util.Enumeration;

import javax.jms.ConnectionMetaData;
import javax.jms.JMSException;

/**
 * Connection metadata
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class JBossConnectionMetaData 
   implements ConnectionMetaData
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** The connection for this meta data */
   private ConnectionDelegate connection;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Create a new metadata object
    * 
    * @param connection the connection for this meta data
    */
   public JBossConnectionMetaData(ConnectionDelegate connection)
      throws JMSException
   {
      this.connection = connection;
   }

	// Public --------------------------------------------------------

   /**
    * Get the connection for this meta data 
    *      
    * @return the connection
    * @throws JMSException for any error
    */
   public ConnectionDelegate getConnection() throws JMSException
   {
      return connection;
   }

   // ConnectionMetaData implementation -----------------------------

	public int getJMSMajorVersion() throws JMSException
	{
		return 1;
	}

	public int getJMSMinorVersion() throws JMSException
	{
		return 1;
	}

	public String getJMSProviderName() throws JMSException
	{
      return "JBoss.org";
	}

	public String getJMSVersion() throws JMSException
	{
      return getJMSMajorVersion() + "." + getJMSMinorVersion();
	}

	public Enumeration getJMSXPropertyNames() throws JMSException
	{
       return connection.getJMSXPropertyNames();
	}

	public int getProviderMajorVersion() throws JMSException
	{
      return 4;
	}

	public int getProviderMinorVersion() throws JMSException
	{
		return 0;
	}

	public String getProviderVersion() throws JMSException
	{
      return getProviderMajorVersion() + "." + getProviderMinorVersion();
	}

   // Protected ------------------------------------------------------

   // Package Private ------------------------------------------------

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------
}
