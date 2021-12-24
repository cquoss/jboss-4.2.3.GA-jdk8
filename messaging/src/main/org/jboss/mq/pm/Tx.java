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

import java.io.Externalizable;
import java.util.ArrayList;

import javax.jms.JMSException;
import javax.transaction.xa.Xid;

/**
 * A transaction
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class Tx implements Comparable, Externalizable
{
   /** The serialVersionUID */
   static final long serialVersionUID = -5428592493990463362L;  

   /** Transaction open */
   private static final int OPEN = 0;
   /** Transaction ended */
   private static final int ENDED = 2;

   /** Restore unknown */
   public static final int UNKNOWN = 0;

   /** Restore addition */
   public static final int ADD = 1;

   /** Restore remove */
   public static final int REMOVE = -1;
   
   /** The transaction value */
   long value = 0;
   
   /** Whether the transaction has been persisted */
   boolean persisted = false;

   transient Xid xid;
   
   /** List of Runnable tasks */
   transient ArrayList postCommitTasks;
   /** List of Runnable tasks */
   transient ArrayList postRollbackTasks;

   /** The status */
   transient int status = OPEN;

   /** The lock */
   transient Object lock = new Object();
   
   /**
    * Create a new Tx for externailzation
    */
   public Tx()
   {
   }
   
   /**
    * Create a new Tx
    * 
    * @param value the value
    */
   public Tx(long value)
   {
      this.value = value;
   }
   
   /**
    * Set the value
    *
    * @param tx the new value
    */
   public void setValue(long tx)
   {
      value = tx;
   }

   /**
    * Get the long value
    *
    * @return the long value
    */
   public long longValue()
   {
      return value;
   }

   /**
    * Get the xid.
    * 
    * @return the xid.
    */
   public Xid getXid()
   {
      return xid;
   }

   /**
    * Set the xid.
    * 
    * @param xid the xid.
    */
   public void setXid(Xid xid)
   {
      this.xid = xid;
   }

   /**
    * Get whether the transaction has been persisted
    *
    * @return true when persisted
    */
   public synchronized boolean checkPersisted()
   {
      boolean result = persisted;
      persisted = true;
      return result;
   }

   /**
    * Get whether the transaction has been persisted
    *
    * @return true when persisted
    */
   public synchronized boolean wasPersisted()
   {
      return persisted;
   }

   /**
    * Compare
    *
    * @param anotherLong the other value
    * @return -1, 0, 1 if less than, equal or greater than respectively
    */
   public int compareTo(Tx anotherLong)
   {
      long thisVal = this.value;
      long anotherVal = anotherLong.value;
      return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
   }
   
   public int compareTo(Object o)
   {
      return compareTo((Tx) o);
   }
   
   public void readExternal(java.io.ObjectInput in) throws java.io.IOException
   {
      value = in.readLong();
   }

   public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException
   {
      out.writeLong(value);
   }
   
   public String toString()
   {
      return String.valueOf(value);
   }
   
   public int hashCode()
   {
      return (int) (value ^ (value >> 32));
   }

   /**
    * Commit the transaction
    * 
    * @param pm the persistence manager
    * @throws JMSExecption for any error
    */
   void commit(PersistenceManager pm) throws JMSException
   {
      synchronized (lock)
      {
         if (status == ENDED)
            throw new JMSException("Transaction is not active for commit");
         status = ENDED;
         postRollbackTasks = null;
      }
      
      pm.commitPersistentTx(this);
      
      synchronized (lock)
      {
         if (postCommitTasks == null)
            return;
         
         for (int i = 0; i < postCommitTasks.size(); ++i)
         {
            Runnable task = (Runnable) postCommitTasks.get(i);
            task.run();
         }
         
         postCommitTasks = null;
      }
   }

   /**
    * Add post commit task
    * 
    * @param task the task
    * @throws JMSExecption for any error
    */
   void addPostCommitTask(Runnable task) throws JMSException
   {
      synchronized (lock)
      {
         if (status == ENDED)
            throw new JMSException("Transacation is not active.");
         if (postCommitTasks == null)
            postCommitTasks = new ArrayList();
         postCommitTasks.add(task);
      }
   }

   /**
    * Commit the transaction
    * 
    * @param pm the persistence manager
    * @throws JMSExecption for any error
    */
   public void rollback(PersistenceManager pm) throws JMSException
   {
      synchronized (lock)
      {
         if (status == ENDED)
            throw new JMSException("Transaction is not active for rollback");
         status = ENDED;
         postCommitTasks = null;
      }
      
      pm.rollbackPersistentTx(this);
      
      synchronized (lock)
      {
         if (postRollbackTasks == null)
            return;

         for (int i = 0; i < postRollbackTasks.size(); ++i)
         {
            Runnable task = (Runnable) postRollbackTasks.get(i);
            task.run();
         }
         
         postRollbackTasks = null;
      }
   }

   /**
    * Add post rollback task
    * 
    * @param task the task
    * @throws JMSExecption for any error
    */
   public void addPostRollbackTask(Runnable task) throws JMSException
   {
      synchronized (lock)
      {
         if (status == ENDED)
            throw new JMSException("Transacation is not active.");
         if (postRollbackTasks == null)
               postRollbackTasks = new ArrayList();
         postRollbackTasks.add(task);
      }
   }
}