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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;
import org.jboss.mq.ConnectionToken;
import org.jboss.mq.Recoverable;
import org.jboss.mq.SpyJMSException;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;
import EDU.oswego.cs.dl.util.concurrent.CopyOnWriteArraySet;

/**
 * This class allows provides the base for user supplied persistence packages.
 * 
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author Paul Kendall (paul.kendall@orion.co.nz)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 73847 $
 */
public class TxManager implements Recoverable
{
   /** The log */
   private static final Logger log = Logger.getLogger(TxManager.class);
   
   /** The persistence manager */
   PersistenceManager persistenceManager;
   
   /** Maps Global transactions to local transactions */
   ConcurrentHashMap globalToLocal = new ConcurrentHashMap();
   
   /** Prepared Transactions Map<Xid, PreparedInfo<Tx>> */
   ConcurrentHashMap prepared = new ConcurrentHashMap();
   
   /**
    * Create a new TxManager
    *
    * @param pm the persistence manager
    */
   public TxManager(PersistenceManager pm)
   {
      persistenceManager = pm;
   }

   /**
	 * Return the local transaction id for a distributed transaction id.
	 * 
     * @deprecated
	 * @param dc the connection
	 * @param xid the transaction id
	 * @return The Prepared transaction
	 * @exception javax.jms.JMSException Description of Exception
	 */
   public final Tx getPrepared(ConnectionToken dc, Object xid) throws JMSException
   {
      GlobalXID gxid = new GlobalXID(dc, xid);
      Tx txid = (Tx) globalToLocal.get(gxid);
      if (txid == null)
         throw new SpyJMSException("Transaction does not exist from: " + dc.getClientID() + " xid=" + xid);

      return txid;
   }

   /**
	 * Create and return a unique transaction id.
	 * 
	 * @return the transaction id
	 * @exception JMSException for any error
	 */
   public final Tx createTx() throws JMSException
   {
      Tx txId = persistenceManager.createPersistentTx();
      return txId;
   }

   /**
     * Force the txid to the log
     * 
     * @param txid the transaction id
     * @exception JMSException for any error
     */
   public final void forcePersistentTx(Tx txid) throws JMSException
   {
      if (persistenceManager instanceof PersistenceManagerExt)
        ((PersistenceManagerExt)persistenceManager).forcePersistentTx(txid);
   }

   /**
	 * Commit the transaction to the persistent store.
	 * 
	 * @param txId the transaction
	 * @exception JMSException for any error
	 */
   public final void commitTx(Tx txId) throws JMSException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("Commit branch=" + txId.longValue());
      txId.commit(persistenceManager);
   }

   /**
    * Commit the transaction to the persistent store.
    *
    * @param dc the connection token
    * @param xid the transaction
    * @exception JMSException for any error
    */
   public final void commitTx(ConnectionToken dc, Object xid) throws JMSException
   {
      boolean trace = log.isTraceEnabled();
      GlobalXID gxid = new GlobalXID(dc, xid);
      Tx txid = (Tx) globalToLocal.get(gxid);
      if (txid == null)
      {
         PreparedInfo preparedInfo = (PreparedInfo) prepared.get(xid);
         if (preparedInfo == null)
            throw new SpyJMSException("Transaction does not exist from: " + dc.getClientID() + " xid=" + xid);
         Set txids = preparedInfo.getTxids();
         for (Iterator i = txids.iterator(); i.hasNext();)
         {
            txid = (Tx) i.next();
            if (trace)
               log.trace("Commit xid=" + xid + " branch=" + txid.longValue());
            txid.commit(persistenceManager);
         }
         prepared.remove(xid);
      }
      else
      {
         if (trace)
            log.trace("Commit xid=" + xid + " branch=" + txid.longValue());
         txid.commit(persistenceManager);
      }
   }
   
   /**
    * Add an operation for after a commit
    *
    * @param txId the transaction
    * @param task the task
    * @throws JMSException for any error
    */
   public void addPostCommitTask(Tx txId, Runnable task) throws JMSException
   {
      if (txId == null)
      {
         task.run();
         return;
      }

      txId.addPostCommitTask(task);
   }

   /**
	 * Rollback the transaction.
	 * 
	 * @param txId the transaction
	 * @exception JMSException for any error
	 */
   public void rollbackTx(Tx txId) throws JMSException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("Rollback branch=" + txId.longValue());
      txId.rollback(persistenceManager);
   }

   /**
    * Rollback the transaction
    *
    * @param dc the connection token
    * @param xid the transaction
    * @exception JMSException for any error
    */
   public final void rollbackTx(ConnectionToken dc, Object xid) throws JMSException
   {
      boolean trace = log.isTraceEnabled();
      GlobalXID gxid = new GlobalXID(dc, xid);
      Tx txid = (Tx) globalToLocal.get(gxid);
      if (txid == null)
      {
         PreparedInfo preparedInfo = (PreparedInfo) prepared.get(xid);
         if (preparedInfo == null)
            throw new SpyJMSException("Transaction does not exist from: " + dc.getClientID() + " xid=" + xid);
         Set txids = preparedInfo.getTxids();
         for (Iterator i = txids.iterator(); i.hasNext();)
         {
            txid = (Tx) i.next();
            if (trace)
               log.trace("Rolling back xid=" + xid + " branch=" + txid.longValue());
            txid.rollback(persistenceManager);
         }
         prepared.remove(xid);
      }
      else
      {
         if (trace)
            log.trace("Rolling back xid=" + xid + " branch=" + txid.longValue());
         txid.rollback(persistenceManager);
      }
   }

   /**
    * Add an operation for after a rollback
    *
    * @param txId the transaction
    * @param task the task
    * @throws JMSException for any error
    */
   public void addPostRollbackTask(Tx txId, Runnable task) throws JMSException
   {
      if (txId == null)
         return;

      txId.addPostRollbackTask(task);
   }

   /**
	 * Create and return a unique transaction id. Given a distributed connection
	 * and a transaction id object, allocate a unique local transaction id if
	 * the remote id is not already known.
	 * 
	 * @param dc the connection token
	 * @param xid the xid
	 * @return the transaction
	 * @exception JMSException for any error
	 */
   public Tx createTx(ConnectionToken dc, Object xid) throws JMSException
   {
      GlobalXID gxid = new GlobalXID(dc, xid);
      if (globalToLocal.containsKey(gxid))
         throw new SpyJMSException("Duplicate transaction from: " + dc.getClientID() + " xid=" + xid);

      Tx txId = createTx();
      if (xid != null && xid instanceof Xid)
         txId.setXid((Xid) xid);
      globalToLocal.put(gxid, txId);

      //Tasks to remove the global to local mappings on commit/rollback
      txId.addPostCommitTask(gxid);
      txId.addPostRollbackTask(gxid);

      return txId;
   }

   /**
    * Restore a prepared transaction
    * 
    * @param txId the transaction id
    * @throws JMSException for any error
    */
   public void restoreTx(Tx txId) throws JMSException
   {
      addPreparedTx(txId, txId.getXid(), true);
   }

   /**
    * Add a prepared transactions
    * 
    * @param txId the transaction id
    * @param xid the xid
    * @param inDoubt whether it is in doubt
    * @throws JMSException for any error
    */
   void addPreparedTx(Tx txId, Xid xid, boolean inDoubt) throws JMSException
   {
      PreparedInfo preparedInfo = (PreparedInfo) prepared.get(xid);
      if (preparedInfo == null)
      {
         preparedInfo = new PreparedInfo(xid, false);
         prepared.put(xid, preparedInfo);
      }
      preparedInfo.add(txId);
      if (inDoubt)
         preparedInfo.setInDoubt(true);
   }
   
   /**
    * Mark the transaction branch as prepared
    * 
    * @param dc the connection token
    * @param xid the xid
    * @param txId the transaction
    * @throws JMSException for any error
    */
   public void markPrepared(ConnectionToken dc, Object xid, Tx txId) throws JMSException
   {
      try
      {
         if (xid instanceof Xid)
         {
            // If we are marking this prepared, we need to log the txid even if it has no messages
            // in the transaction
            forcePersistentTx(txId);
            addPreparedTx(txId, (Xid) xid, false);
         }
      }
      catch (Throwable t)
      {
         SpyJMSException.rethrowAsJMSException("Error marking transaction as prepared xid=" + xid + " tx=" + txId, t);
      }
   }

   public Xid[] recover(ConnectionToken dc, int flags) throws Exception
   {
      Set preparedXids = prepared.keySet();
      Xid[] xids = (Xid[]) preparedXids.toArray(new Xid[preparedXids.size()]);
      return xids;
   }
   
   /**
    * Get the prepared transactions
    * 
    * @return
    */
   public Map getPreparedTransactions()
   {
      return prepared;
   }
   
   /**
	 * A global transaction
	 */
   class GlobalXID implements Runnable
   {
      ConnectionToken dc;
      Object xid;

      GlobalXID(ConnectionToken dc, Object xid)
      {
         this.dc = dc;
         this.xid = xid;
      }

      public boolean equals(Object obj)
      {
         if (obj == null)
         {
            return false;
         }
         if (obj.getClass() != GlobalXID.class)
         {
            return false;
         }
         return ((GlobalXID) obj).xid.equals(xid) && ((GlobalXID) obj).dc.equals(dc);
      }

      public int hashCode()
      {
         return xid.hashCode();
      }

      public void run()
      {
         Tx txId = (Tx) globalToLocal.remove(this);
         // Tidyup the prepared transactions
         if (txId != null)
         {
            PreparedInfo preparedInfo = (PreparedInfo) prepared.get(xid);
            if (preparedInfo != null)
            {
               preparedInfo.remove(txId);
               if (preparedInfo.isEmpty())
                  prepared.remove(xid);
            }
         }
      }
   }

   /**
    * Information about a prepared global transaction
    * 
    * @author <a href="adrian@jboss.com">Adrian Brock</a>
    * @version $Revision: 73847 $
    */
   public static class PreparedInfo
   {
      /** The XID */
      private Xid xid;
      
      /** Whether the transaction is in doubt */
      private boolean inDoubt;
      
      /** The local transaction branches */
      private Set txids = new CopyOnWriteArraySet();

      /**
       * Create a new PreparedInfo.
       * 
       * @param xid the xid
       * @param indoubt whether the transaction is in doubt
       */
      public PreparedInfo(Xid xid, boolean inDoubt)
      {
         this.xid = xid;
         this.inDoubt = inDoubt;
      }

      /**
       * Whether the transaction is in doubt
       * 
       * @return true when in doubt
       */
      public boolean isInDoubt()
      {
         return inDoubt;
      }
      
      /**
       * Set the in doubt
       * 
       * @param inDoubt the in doubt value
       */
      public void setInDoubt(boolean inDoubt)
      {
         this.inDoubt = inDoubt;
      }
      
      /**
       * Get the XID
       * 
       * @return
       */
      public Xid getXid()
      {
         return xid;
      }

      /**
       * Get the local branches
       * 
       * @return the local branches
       */
      public Set getTxids()
      {
         return txids;
      }
      
      /**
       * Add a local branch
       * 
       * @param txid the local branch
       */
      public void add(Tx txid)
      {
         txids.add(txid);
      }
      
      /**
       * Remove a local branch
       * 
       * @param txid the local branch
       */
      public void remove(Tx txid)
      {
         txids.remove(txid);
      }
      
      /**
       * Whether there are no local branches
       * 
       * @return true when there no local branches
       */
      public boolean isEmpty()
      {
         return txids.isEmpty();
      }
   }
}