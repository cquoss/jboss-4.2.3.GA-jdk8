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
package org.jboss.mq.pm.jdbc2;

import javax.management.ObjectName;

import org.jboss.mq.pm.CacheStoreMBean;
import org.jboss.system.ServiceMBean;

/**
 * MBean interface.
 * 
 * @author Jayesh Parayali (jayeshpk1@yahoo.com)
 * @author Hiram Chirino (cojonudo14@hotmail.com)
 * @author Adrian Brock (adrian@jboss.com)
 * @version $Revision: 57198 $
 */
public interface PersistenceManagerMBean extends ServiceMBean, org.jboss.mq.pm.PersistenceManagerMBean, CacheStoreMBean
{
   /**
    * Get the instance of the persistence manager
    * 
    * @return the instance
    */
   Object getInstance();

   /**
    * Get the object name of the DataSource
    * 
    * @return the object name of the DataSource
    */
   ObjectName getConnectionManager();

   /**
    * Set the object name of the DataSource
    * 
    * @param connectionManagerName the object name of the DataSource
    */
   void setConnectionManager(ObjectName connectionManagerName);

   /**
    * Gets the sqlProperties.
    * 
    * @return Returns a Properties    
    */
   String getSqlProperties();

   /**
    * Sets the sqlProperties.
    * 
    * @param sqlProperties The sqlProperties to set    
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

   /**
    * Any override recovery transaction timeout
    * 
    * @return the override transaction timeout
    */
   int getRecoveryTimeout();

   /**
    * Set the override recovery transaction timeout
    * 
    * @param timeout the timeout
    */
   void setRecoveryTimeout(int timeout);

   /**
    * Returns the recovery retries
    * 
    * @return the retries
    */
   int getRecoveryRetries();

   /**
    * Set the recovery retries
    * 
    * @param retries the number of retries
    */
   void setRecoveryRetries(int retries);

   /**
    * Returns the recover messages chunk
    * 
    * @return the chunk size
    */
   int getRecoverMessagesChunk();

   /**
    * Set the recover messages chunk
    * 
    * @param recoverMessagesChunk the chunk size
    */
   void setRecoverMessagesChunk(int recoverMessagesChunk);

   /**
    * Get the xaRecovery.
    * 
    * @return the xaRecovery.
    */
   boolean isXARecovery();

   /**
    * Set the xaRecovery.
    * 
    * @param xaRecovery the xaRecovery.
    */
   void setXARecovery(boolean xaRecovery);

   /**
    * Get the statement retries
    * 
    * @return the retries
    */
   int getStatementRetries();

   /**
    * Set the statement retries
    * 
    * @param statementRetries the retries
    */
   void setStatementRetries(int statementRetries);
}
