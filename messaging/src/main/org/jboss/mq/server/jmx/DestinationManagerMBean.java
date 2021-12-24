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
package org.jboss.mq.server.jmx;

import java.util.Map;

import javax.management.ObjectName;

/**
 * MBean interface.
 */
public interface DestinationManagerMBean extends org.jboss.mq.server.jmx.InterceptorMBean
{

   int getClientCount();

   java.util.Map getClients();

   /**
    * Get the value of PersistenceManager.
    * @return value of PersistenceManager.    */
   javax.management.ObjectName getPersistenceManager();

   /**
    * Set the value of PersistenceManager.
    * @param v Value to assign to PersistenceManager.    */
   void setPersistenceManager(javax.management.ObjectName objectName);

   /**
    * Get the value of StateManager.
    * @return value of StateManager.    */
   javax.management.ObjectName getStateManager();

   /**
    * Set the value of StateManager.
    * @param v Value to assign to StateManager.    */
   void setStateManager(javax.management.ObjectName objectName);

   /**
    * Get the value of MessageCache.
    * @return value of MessageCache.    */
   javax.management.ObjectName getMessageCache();

   /**
    * Set the value of MessageCache.
    * @param v Value to assign to MessageCache.    */
   void setMessageCache(javax.management.ObjectName objectName);

   /**
    * Retrieve the temporary topic/queue max depth
    * @return the maximum depth
    */
   int getTemporaryMaxDepth();

   /**
    * Set the temporary topic/queue max depth
    * @param depth the maximum depth
    */
   void setTemporaryMaxDepth(int depth);

   /**
    * Retrieve the temporary topic/queue in memory mode
    * @return true for in memory
    */
   boolean getTemporaryInMemory();

   /**
    * Set the temporary topic/queue in memory mode
    * @param mode true for in memory
    */
   void setTemporaryInMemory(boolean mode);

   /**
    * Get the receivers implemenetation
    * @return the receivers implementation class
    */
   java.lang.Class getReceiversImpl();

   /**
    * Set the receivers implementation class
    * @param clazz the receivers implementation class
    */
   void setReceiversImpl(java.lang.Class clazz);

   /**
    * Returns the recovery retries
    */
   public int getRecoveryRetries();

   /**
    * Sets the class implementating the receivers
    */
   public void setRecoveryRetries(int retries);

   /**
    * Returns ThreadPool.
    */
   public ObjectName getThreadPool();

   /**
    * Sets ThreadPool.
    */
   public void setThreadPool(ObjectName threadPool);

   /**
    * Returns the expiry destination.
    */
   public ObjectName getExpiryDestination();

   /**
    * Sets the expiry destination.
    */
   public void setExpiryDestination(ObjectName destination);

   /**
    * Get the lazyRedeliveryUpdate.
    * 
    * @return the lazyRedeliveryUpdate.
    */
   boolean isLazyRedeliveryUpdate();

   void setLazyRedeliveryUpdate(boolean lazyRedeliveryUpdate);

   void createQueue(java.lang.String name) throws java.lang.Exception;

   void createTopic(java.lang.String name) throws java.lang.Exception;

   void createQueue(java.lang.String name, java.lang.String jndiLocation) throws java.lang.Exception;

   void createTopic(java.lang.String name, java.lang.String jndiLocation) throws java.lang.Exception;

   void destroyQueue(java.lang.String name) throws java.lang.Exception;

   void destroyTopic(java.lang.String name) throws java.lang.Exception;

   /**
    * Sets the destination message counter history day limit <0: unlimited, =0: disabled, > 0 maximum day count
    * @param days maximum day count
    */
   void setMessageCounterHistoryDayLimit(int days);

   /**
    * Gets the destination message counter history day limit
    * @return Maximum day count
    */
   int getMessageCounterHistoryDayLimit();

   /**
    * get message counter of all configured destinations
    */
   org.jboss.mq.server.MessageCounter[] getMessageCounter() throws java.lang.Exception;

   /**
    * get message stats
    */
   org.jboss.mq.MessageStatistics[] getMessageStatistics() throws java.lang.Exception;

   /**
    * List message counter of all configured destinations as HTML table
    */
   java.lang.String listMessageCounter() throws java.lang.Exception;

   /**
    * Reset message counter of all configured destinations
    */
   void resetMessageCounter();
   
   /**
    * Retrieve the prepared transactions
    * 
    * @return Map<Xid, indoubt boolean>
    */
   Map retrievePreparedTransactions(); 
   
   /**
    * Show the prepared transactions
    * 
    * @return Some html
    */
   String showPreparedTransactions(); 
}
