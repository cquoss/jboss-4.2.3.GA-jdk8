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
package org.jboss.mq.pm;

import javax.jms.JMSException;

import org.jboss.mq.SpyDestination;
import org.jboss.mq.server.JMSDestination;
import org.jboss.mq.server.MessageCache;
import org.jboss.mq.server.MessageReference;

/**
 * This class allows provides the base for user supplied persistence packages.
 * 
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author Paul Kendall (paul.kendall@orion.co.nz)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public interface PersistenceManager
{
   // Constants -----------------------------------------------------

   // Public --------------------------------------------------------
   
   /**
    * Get the message cache
    *
    * @return the instance of the message cache
    */
   MessageCache getMessageCacheInstance();

   /**
	 * Create and return a unique transaction id.
	 * 
	 * @return the transaction
    * @throws JMSException for any error
	 */
   Tx createPersistentTx() throws javax.jms.JMSException;

   /**
	 * Commit the transaction to the persistent store.
	 * 
	 * @param txId Description of Parameter
    * @throws JMSException for any error
	 */
   void commitPersistentTx(Tx txId) throws javax.jms.JMSException;

   /**
	 * Rollback the transaction.
	 * 
	 * @param txId Description of Parameter
    * @throws JMSException for any error
	 */
   void rollbackPersistentTx(Tx txId) throws javax.jms.JMSException;


   /**
    * Get a transaction manager.
    * 
    * @return the transaction manager
    * @throws JMSException for any error
    */
   TxManager getTxManager();

   /**
	 * Add a message to the persistent store. If the message is part of a
	 * transaction, txId is not null.
	 * 
	 * @param message the message
	 * @param txId the transaction
    * @throws JMSException for any error
	 */
   void add(MessageReference message, Tx txId) throws JMSException;

   /**
	 * Restore a queue.
	 * 
	 * @param jmsDest the jms destination
    * @param dest the client destination 
    * @throws JMSException for any error
	 */
   void restoreQueue(JMSDestination jmsDest, SpyDestination dest) throws JMSException;

   /**
	 * Update message in the persistent store. If the message is part of a
	 * transaction, txId is not null (not currently supported).
	 * 
	 * @param message 
	 * @param txId Description of Parameter
    * @throws JMSException for any error
	 */
   void update(MessageReference message, Tx txId) throws JMSException;

   /**
	 * Remove message from the persistent store. If the message is part of a
	 * transaction, txId is not null.
	 * 
	 * @param message the message
	 * @param txId the transaction
    * @throws JMSException for any error
	 */
   void remove(MessageReference message, Tx txId) throws JMSException;

   /**
    * Close a queue
    * 
    * @param jmsDest the jms destination
    * @param dest the client destination 
    * @throws JMSException for any error
    */
   void closeQueue(JMSDestination jmsDest, SpyDestination dest) throws JMSException;
}