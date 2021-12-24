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
package org.jboss.tm;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBean;
import org.jboss.tm.integrity.TransactionIntegrityFactory;

/**
 * TransactionManagerService MBean interface.
 *
 * @see TxManager
 * @version $Revision: 57208 $
 */
public interface TransactionManagerServiceMBean extends ServiceMBean, TransactionManagerFactory
{
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss:service=TransactionManager");
   
   /**
    * Describe <code>getGlobalIdsEnabled</code> method here.
    * @return an <code>boolean</code> value
    */
   boolean getGlobalIdsEnabled();

   /**
    * Describe <code>setGlobalIdsEnabled</code> method here.
    * @param newValue an <code>boolean</code> value
    */
   void setGlobalIdsEnabled(boolean newValue);

   /**
    * Is thread interruption enabled at transaction timeout
    * @return true for interrupt threads, false otherwise
    */
   boolean isInterruptThreads();

   /**
    * Enable/disable thread interruption at transaction timeout.
    * @param interruptThreads pass true to interrupt threads, false otherwise*
    */
   void setInterruptThreads(boolean interruptThreads);

   /**
    * Describe <code>getTransactionTimeout</code> method here.
    * @return an <code>int</code> value
    */
   int getTransactionTimeout();

   /**
    * Describe <code>setTransactionTimeout</code> method here.
    * @param timeout an <code>int</code> value
    */
   void setTransactionTimeout(int timeout);

   /**
    * mbean get-set pair for field xidFactory Get the value of xidFactory
    * @return value of xidFactory
    */
   ObjectName getXidFactory();

   /**
    * Set the value of xidFactory
    * @param xidFactory Value to assign to xidFactory
    */
   void setXidFactory(ObjectName xidFactory);

   /**
    * Get the xa terminator
    * @return the xa terminator
    */
   JBossXATerminator getXATerminator();

   /**
    * Counts the number of transactions
    * @return the number of active transactions
    */
   long getTransactionCount();

   /**
    * The number of commits.
    * @return the number of transactions that have been committed
    */
   long getCommitCount();

   /**
    * The number of rollbacks.
    * @return the number of transactions that have been rolled back
    */
   long getRollbackCount();

   /**
    * The <code>registerXAExceptionFormatter</code> method
    * @param clazz a <code>Class</code> value
    * @param formatter a <code>XAExceptionFormatter</code> value
    */
   void registerXAExceptionFormatter(Class clazz, XAExceptionFormatter formatter);

   /**
    * The <code>unregisterXAExceptionFormatter</code> method
    * @param clazz a <code>Class</code> value
    */
   void unregisterXAExceptionFormatter(Class clazz);
   
   /**
    * Set the Integrity checker factory 
    * 
    * @param factory the integrity checker factory
    */
   void setTransactionIntegrityFactory(TransactionIntegrityFactory factory);

}
