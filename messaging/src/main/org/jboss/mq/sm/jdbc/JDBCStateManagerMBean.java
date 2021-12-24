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
package org.jboss.mq.sm.jdbc;

import javax.management.ObjectName;

import org.jboss.mq.sm.AbstractStateManagerMBean;

/**
 * MBean interface.
 * 
 * @todo add support for jmx operations to maintain the database
 * @todo create indices
 * @author Adrian Brock (Adrian@jboss.org)
 * @version $Revision: 57198 $
 */
public interface JDBCStateManagerMBean extends AbstractStateManagerMBean
{
   /**
    * Get the connection manager name
    * 
    * @return the connection manager
    */
   ObjectName getConnectionManager();

   /**
    * Set the connection manager name
    * 
    * @param connectionManagerName the name
    */
   void setConnectionManager(ObjectName connectionManagerName);

   /**
    * Whether we have a security manager
    * 
    * @return true when using a security manager
    */
   boolean hasSecurityManager();

   /**
    * Set whether we have a security manager
    * 
    * @param hasSecurityManager true when there is a security manager
    */
   void setHasSecurityManager(boolean hasSecurityManager);

   /**
    * Gets the sqlProperties.
    * 
    * @return the Properties    
    */
   String getSqlProperties();

   /**
    * Sets the sqlProperties.
    * 
    * @param sqlProperties sqlProperties to set
    */
   void setSqlProperties(String value);

   /**
    * Sets the ConnectionRetryAttempts.
    * 
    * @param connectionRetryAttempts value    
    */
   void setConnectionRetryAttempts(int value);

   /**
    * Gets the ConnectionRetryAttempts.
    * 
    * @return Returns a ConnectionRetryAttempt value
    */
   int getConnectionRetryAttempts();
}
