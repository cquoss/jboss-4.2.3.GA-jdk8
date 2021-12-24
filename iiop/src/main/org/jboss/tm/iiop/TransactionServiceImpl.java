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
package org.jboss.tm.iiop;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;
import org.jboss.tm.LocalId;
import org.jboss.tm.TransactionImpl;
import org.jboss.tm.TransactionPropagationContextImporter;
import org.jboss.tm.XidImpl;
import org.jboss.util.UnreachableStatementException;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.ControlHelper;
import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions.CoordinatorHelper;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.Inactive;
import org.omg.CosTransactions.NotSubtransaction;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.RecoveryCoordinator;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.Status;
import org.omg.CosTransactions.SubtransactionAwareResource;
import org.omg.CosTransactions.SubtransactionsUnavailable;
import org.omg.CosTransactions.Synchronization;
import org.omg.CosTransactions.SynchronizationUnavailable;
import org.omg.CosTransactions.Terminator;
import org.omg.CosTransactions.TerminatorHelper;
import org.omg.CosTransactions.TransIdentity;
import org.omg.CosTransactions.TransactionFactoryHelper;
import org.omg.CosTransactions.Unavailable;
import org.omg.CosTransactions.otid_t;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.CurrentPackage.NoContext;

/**
 * CORBA servant for the JBoss TransactionService.
 *
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 57194 $
 */
public class TransactionServiceImpl 
      extends TransactionServicePOA
{
   // Constants  ----------------------------------------------------

   private static final Logger log = 
      Logger.getLogger(TransactionServiceImpl.class);

   // The first byte of our CORBA oids tells the type of the object:

   /** Value in position 0 of the oid within a Control reference. */
   private static final byte CONTROL = 0x43; 

   /** Value in position 0 of the oid within a Coordinator reference. */
   private static final byte COORDINATOR = 0x63;

   /** Value in position 0 of the oid within a TransactionFactory reference. */
   private static final byte TX_FACTORY = 0x46;

   /** Value in position 0 of the oid within a Terminator reference. */
   private static final byte TERMINATOR = 0x74;

   // String arrays returned by _all_interfaces:

   /** Interface ids of Control and its superinterfaces. */
   private static final String[] controlInterfaceIds = {
      ControlHelper.id() 
   };

   /** Interface ids of CoordinatorExt and its superinterfaces. */
   private static final String[] coordinatorInterfaceIds = { 
      CoordinatorExtHelper.id(),
      CoordinatorHelper.id()
   };

   /** Interface ids of TransactionFactoryExt and its superinterfaces. */
   private static final String[] txFactoryInterfaceIds = { 
      TransactionFactoryExtHelper.id(),
      TransactionFactoryHelper.id() 
   };

   /** Interface ids of Terminator and its superinterfaces. */
   private static final String[] terminatorInterfaceIds = { 
      TerminatorHelper.id() 
   };

   // Static  -------------------------------------------------------

   private static TransactionManager tm;
   private static TransactionPropagationContextImporter tpcImporter;

   // Attributes ----------------------------------------------------

   private ORB orb;
   private POA poa;
   private org.omg.PortableServer.Current poaCurrent;

   // Package -------------------------------------------------------

   static byte[] theFactoryId() { return new byte[] { TX_FACTORY }; }

   // Constructor ---------------------------------------------------

   TransactionServiceImpl(ORB orb, POA poa)
   {
      this.orb = orb;
      this.poa = poa;

      try
      {
         org.omg.CORBA.Object obj =
            orb.resolve_initial_references("POACurrent");
         poaCurrent = org.omg.PortableServer.CurrentHelper.narrow(obj);
      }
      catch (InvalidName e) // thrown by resolve_initial_references
      {
         log.warn("Call to resolve_initial_references failed: ", e);
         throw new RuntimeException(
                           "Call to resolve_initial_references failed: " + e);
      }
      catch (BAD_PARAM e) // thrown by narrow
      {
         log.warn("Call to narrow failed: ", e);
         throw new RuntimeException("Call to narrow failed: " + e);
      }
   }

   // org.omg.PortableServer.Servant override -----------------------
   public String[] _all_interfaces(POA poa, byte[] oid)
   {
      if (oid[0] == TX_FACTORY)
         return txFactoryInterfaceIds;
      else if (oid[0] == CONTROL)
         return controlInterfaceIds;
      else if (oid[0] == COORDINATOR)
         return coordinatorInterfaceIds;
      else if (oid[0] == TERMINATOR)
         return terminatorInterfaceIds;
      else
         throw new BAD_PARAM("Unknown CORBA object id");
   }

   // TransactionFactoryExt operation -------------------------------

   public TransactionDesc create_transaction(int timeout)
   {
      checkInvocationTarget(TX_FACTORY);
      try
      {
         TransactionManager tm = getTransactionManager();

         // Set timeout value
         if (timeout != 0)
            tm.setTransactionTimeout(timeout);

         // Start tx 
         tm.begin();

         // Suspend thread association 
         // and get the xid and the local id the transaction
         TransactionImpl tx = (TransactionImpl)tm.suspend();
         XidImpl xid = tx.getXid();
         long localId = xid.getLocalIdValue();

         // Set up oid byte array to create CORBA reference to the Coordinator
         byte[] oid = new byte[9];
         oid[0] = COORDINATOR;
         LocalId.toByteArray(localId, oid, 1);

         // Create CORBA reference to the Coordinator
         Coordinator coord = CoordinatorHelper.narrow(
               poa.create_reference_with_id(oid, CoordinatorHelper.id()));

         // Reuse the oid array to create CORBA reference to the Terminnator
         oid[0] = TERMINATOR;
         Terminator term = TerminatorHelper.narrow(
               poa.create_reference_with_id(oid, TerminatorHelper.id()));

         // Create and initialize PropagationContext object
         PropagationContext pc = new PropagationContext();
         pc.current = createTransIdentity(xid, coord, term);
         pc.timeout = timeout;
         pc.parents = new TransIdentity[0];
         pc.implementation_specific_data = orb.create_any();

         // Create and initialize TransactionDesc object
         TransactionDesc td = new TransactionDesc();
         td.control = ControlHelper.narrow(
                     poa.create_reference_with_id(oid, ControlHelper.id()));
         td.propagationContext = pc;

         // Return TransactionDesc
         return td;
      }
      catch (SystemException e)
      {
         log.trace("Unexpected exception: ", e);
         throw new UNKNOWN(e.toString());
      } 
      catch (NotSupportedException e)
      {
         log.trace("Unexpected exception: ", e);
         throw new BAD_INV_ORDER(e.toString());
      }
   }

   // CosTransactions::TransactionFactory operations ----------------

   public Control create(int timeout)
   {
      checkInvocationTarget(TX_FACTORY);
      try
      {
         TransactionManager tm = getTransactionManager();

         // Set timeout value
         if (timeout != 0)
            tm.setTransactionTimeout(timeout);

         // Start tx 
         tm.begin();

         // Suspend thread association 
         // and get the xid and the local id the transaction
         TransactionImpl tx = (TransactionImpl)tm.suspend();
         XidImpl xid = tx.getXid();
         long localId = xid.getLocalIdValue();

         // Set up oid byte array to create CORBA reference to the Control
         byte[] oid = new byte[9];
         oid[0] = CONTROL;
         LocalId.toByteArray(localId, oid, 1); 

         // Return CORBA reference to the control
         return ControlHelper.narrow(
                     poa.create_reference_with_id(oid, ControlHelper.id()));
      }
      catch (SystemException e)
      {
         log.trace("Unexpected exception: ", e);
         throw new UNKNOWN(e.toString());
      } 
      catch (NotSupportedException e)
      {
         log.trace("Unexpected exception: ", e);
         throw new BAD_INV_ORDER(e.toString());
      }
   }

   public Control recreate(PropagationContext pc)
   {
      checkInvocationTarget(TX_FACTORY);
      log.trace("Operation recreate is not implemented");
      throw new NO_IMPLEMENT("recreate not implemented");
   }

   // CosTransactions::Control operations ---------------------------

   public Terminator get_terminator()
      throws Unavailable
   {
      byte[] oid = getTargetId();
      if (oid[0] != CONTROL)
         throw new BAD_OPERATION();
      oid[0] = TERMINATOR;
      return TerminatorHelper.narrow(
                  poa.create_reference_with_id(oid, TerminatorHelper.id()));
   }

   public Coordinator get_coordinator()
      throws Unavailable
   {
      byte[] oid = getTargetId();
      if (oid[0] != CONTROL)
         throw new BAD_OPERATION();
      oid[0] = COORDINATOR;
      return CoordinatorHelper.narrow(
                  poa.create_reference_with_id(oid, CoordinatorHelper.id()));
   }

   // CosTransactions::Terminator operations ------------------------

   public void commit(boolean reportHeuristics)
      throws HeuristicHazard, HeuristicMixed
   {
      byte[] oid = getTargetId();
      if (oid[0] != TERMINATOR)
         throw new BAD_OPERATION();

      long localIdValue = LocalId.fromByteArray(oid, 1);
      LocalId localId = new LocalId(localIdValue);
      Transaction tx = getTransaction(localId);

      if (tx == null)
      {
         log.trace("RuntimeException in commit: transaction not found");
         throw new OBJECT_NOT_EXIST("No transaction.");
      }

      TransactionManager tm = getTransactionManager();
      try 
      {
         tm.resume(tx);
         tm.commit();
      }
      catch (RollbackException e)
      {
         log.trace("Exception: ", e);
         throw new TRANSACTION_ROLLEDBACK(e.toString());
      }
      catch (HeuristicMixedException e)
      {
         log.trace("Exception: ", e);
         if (reportHeuristics) 
            throw new HeuristicMixed(e.toString());
      }
      catch (HeuristicRollbackException e)
      {
         log.trace("Exception: ", e);
         if (reportHeuristics)
            throw new HeuristicHazard(e.toString());
      }
      catch (InvalidTransactionException e)
      {
         log.trace("Unexpected exception: ", e);
         throw new INVALID_TRANSACTION(e.toString());
      }
      catch (IllegalStateException e)
      {
         log.trace("Unexpected exception: ", e);
         throw new BAD_INV_ORDER(e.toString());
      }
      catch (SystemException e)
      {
         log.trace("Unexpected exception: ", e);
         throw new UNKNOWN(e.toString());
      }
      catch (SecurityException e)
      {
         log.trace("Unexpected exception: ", e);
         throw new NO_PERMISSION(e.toString());
      }
   }

   public void rollback()
   {
      byte[] oid = getTargetId();
      if (oid[0] != TERMINATOR)
         throw new BAD_OPERATION();

      long localIdValue = LocalId.fromByteArray(oid, 1);
      LocalId localId = new LocalId(localIdValue);
      Transaction tx = getTransaction(localId);

      if (tx == null)
      {
         log.trace("RuntimeException in rollback: transaction not found");
         throw new OBJECT_NOT_EXIST("No transaction.");
      }

      TransactionManager tm = getTransactionManager();
      try 
      {
         tm.resume(tx);
         tm.rollback();
      }
      catch (InvalidTransactionException e)
      {
         log.trace("Unexpected exception: ", e);
         throw new INVALID_TRANSACTION(e.toString());
      }
      catch (IllegalStateException e)
      {
         log.trace("Unexpected exception: ", e);
         throw new BAD_INV_ORDER(e.toString());
      }
      catch (SystemException e)
      {
         log.trace("Unexpected exception: ", e);
         throw new UNKNOWN(e.toString());
      }
      catch (SecurityException e)
      {
         log.trace("Unexpected exception: ", e);
         throw new NO_PERMISSION(e.toString());
      }
   }

   // CoordinatorExt operation --------------------------------------

   public TransactionId get_transaction_id()
   {
      byte[] oid = getTargetId();
      if (oid[0] != COORDINATOR)
         throw new BAD_OPERATION();

      long localIdValue = LocalId.fromByteArray(oid, 1);
      LocalId localId = new LocalId(localIdValue);
      TransactionImpl tx = (TransactionImpl)getTransaction(localId);

      if (tx == null)
      {
         log.trace("RuntimeException in get_transaction_id: transaction not found");
         throw new OBJECT_NOT_EXIST("No transaction.");
      }
      
      Xid xid = tx.getXid();
      return new TransactionId(xid.getFormatId(), 
                               xid.getGlobalTransactionId());
   }

   // CosTransactions::Coordinator operations -----------------------

   public Status get_status()
   {
      byte[] oid = getTargetId();
      if (oid[0] != COORDINATOR)
         throw new BAD_OPERATION();

      long localIdValue = LocalId.fromByteArray(oid, 1);
      LocalId localId = new LocalId(localIdValue);
      Transaction tx = getTransaction(localId);

      if (tx == null)
      {
         log.trace("RuntimeException in get_status: transaction not found");
         // Not sure if here I should return StatusNoTransaction 
         // instead of throwing OBJECT_NOT_EXIST... (Francisco)
         throw new OBJECT_NOT_EXIST("No transaction.");
         // return org.omg.CosTransactions.Status.StatusNoTransaction; // ?
      }
      
      int status;

      try
      {
         status = tx.getStatus();
      }
      catch (SystemException e)
      {
         log.trace("Unexpected exception: ", e);
         throw new UNKNOWN(e.toString());
      }
      return javaxToCosTransactions(status);
   }

   public Status get_parent_status()
   {
      // The target coordinator is associated with a top-level 
      // transaction, because we do not support nested transactions.
      // Therefore get_parent_status() is equivalent to get_status().
      return get_status(); 
   }

   public Status get_top_level_status()
   {
      // The target coordinator is associated with a top-level 
      // transaction, because we do not support nested transactions.
      // Therefore get_top_level_status() is equivalent to get_status().
      return get_status();
   }

   public boolean is_same_transaction(Coordinator other)
   {
      byte[] oid = getTargetId();
      if (oid[0] != COORDINATOR)
         throw new BAD_OPERATION();

      long localIdValue = LocalId.fromByteArray(oid, 1);
      LocalId localId = new LocalId(localIdValue);
      TransactionImpl tx = (TransactionImpl)getTransaction(localId);

      if (tx == null)
      {
         log.trace("RuntimeException in is_same_transaction: transaction not found");
         throw new OBJECT_NOT_EXIST("No transaction.");
      }
      
      Xid xid = tx.getXid();

      try
      {
         CoordinatorExt otherExt = CoordinatorExtHelper.narrow(other);
         TransactionId otherId = otherExt.get_transaction_id();
         return compare(xid,
                        otherId.formatId, 
                        otherId.globalId, 
                        otherId.globalId.length);
      }
      catch (BAD_PARAM e) 
      {
          // narrow failed: foreign transaction case
         try
         {
            otid_t otherOtid = other.get_txcontext().current.otid;
            return compare(xid,
                           otherOtid.formatID,
                           otherOtid.tid,
                           otherOtid.tid.length - otherOtid.bqual_length);
         }
         catch (Unavailable u)
         {
            log.trace("Foreign Coordinator do not support get_txcontext(): ", 
                      e);
            throw new BAD_PARAM(e.toString());
         }
      }
   }

   public boolean is_ancestor_transaction(Coordinator other)
   {
      // We do not support nested transactions. In this case 
      // is_ancestor_transaction() is equivalent to is_same_transaction().
      return is_same_transaction(other);
   }

   public boolean is_descendant_transaction(Coordinator other)
   {
      // We do not support nested transactions. In this case 
      // is_descendant_transaction() is equivalent to is_same_transaction().
      return is_same_transaction(other);
   }

   public boolean is_related_transaction(Coordinator other)
   {
      // We do not support nested transactions. In this case 
      // is_descendant_transaction() is equivalent to is_same_transaction().
      return is_same_transaction(other);
   }

   public boolean is_top_level_transaction()
   {
      checkInvocationTarget(COORDINATOR);
      return true; // because we do not support nested transactions. 
   }

   public int hash_transaction()
   {
      byte[] oid = getTargetId();
      if (oid[0] != COORDINATOR)
         throw new BAD_OPERATION();

      long localIdValue = LocalId.fromByteArray(oid, 1);
      LocalId localId = new LocalId(localIdValue);
      TransactionImpl tx = (TransactionImpl)getTransaction(localId);
      if (tx == null)
      {
         log.trace("RuntimeException in is_same_transaction: transaction not found");
         throw new OBJECT_NOT_EXIST("No transaction.");
      }
      return tx.getXid().hashCode();
   }

   public int hash_top_level_tran()
   {
      // We do not support nested transactions. In this case 
      // hash_top_level_transaction() is equivalent to hash_transaction().
      return hash_transaction(); 
   }

   public RecoveryCoordinator register_resource(Resource r)
      throws Inactive 
   {
      checkInvocationTarget(COORDINATOR);
      throw new NO_IMPLEMENT("Two-phase commit is not supported."); // TODO!
   }

   public void register_synchronization(final Synchronization sync)
      throws SynchronizationUnavailable, Inactive
   {
      byte[] oid = getTargetId();
      if (oid[0] != COORDINATOR)
         throw new BAD_OPERATION();

      long localIdValue = LocalId.fromByteArray(oid, 1);
      LocalId localId = new LocalId(localIdValue);
      Transaction tx = getTransaction(localId);

      if (tx == null)
      {
         log.trace("RuntimeException in register_synchronization: transaction not found");
         throw new OBJECT_NOT_EXIST("No transaction.");
      }
      
      try 
      {
         tx.registerSynchronization(
            new javax.transaction.Synchronization()
            {
               public void beforeCompletion() 
               {
                  sync.before_completion();
               }
               public void afterCompletion(int status)
               {
                  sync.after_completion(javaxToCosTransactions(status));
               }
            });
      }
      catch (RollbackException e)
      {
         log.trace("Exception: ", e);
         throw new TRANSACTION_ROLLEDBACK(e.toString());
      }
      catch (IllegalStateException e)
      {
         log.trace("Unexpected exception: ", e);
         throw new BAD_INV_ORDER(e.toString());
      }
      catch (SystemException e)
      {
         log.trace("Unexpected exception: ", e);
         throw new UNKNOWN(e.toString());
      }
   }

   public void register_subtran_aware(SubtransactionAwareResource r)
      throws NotSubtransaction, Inactive
   {
      checkInvocationTarget(COORDINATOR);
      throw new NotSubtransaction("Nested transactions are not supported");
   }

   public void rollback_only()
      throws Inactive
   {
      byte[] oid = getTargetId();
      if (oid[0] != COORDINATOR)
         throw new BAD_OPERATION();

      long localIdValue = LocalId.fromByteArray(oid, 1);
      LocalId localId = new LocalId(localIdValue);
      Transaction tx = getTransaction(localId);

      if (tx == null)
      {
         log.trace("RuntimeException in rollback_only: transaction not found");
         throw new OBJECT_NOT_EXIST("No transaction.");
      }

      try 
      {
         tx.setRollbackOnly();
      }
      catch (IllegalStateException e)
      {
         log.trace("Unexpected exception: ", e);
         throw new BAD_INV_ORDER(e.toString());
      }
      catch (SystemException e)
      {
         log.trace("Unexpected exception: ", e);
         throw new UNKNOWN(e.toString());
      }
   }

   public String get_transaction_name()
   {
      byte[] oid = getTargetId();
      if (oid[0] != COORDINATOR)
         throw new BAD_OPERATION();

      long localIdValue = LocalId.fromByteArray(oid, 1);
      LocalId localId = new LocalId(localIdValue);
      Transaction tx = getTransaction(localId);

      if (tx == null)
      {
         log.trace("RuntimeException in get_transaction_name: transaction not found");
         throw new OBJECT_NOT_EXIST("No transaction.");
      }
      return tx.toString();
   }

   public Control create_subtransaction()
      throws SubtransactionsUnavailable, Inactive
   {
      checkInvocationTarget(COORDINATOR);
      throw new SubtransactionsUnavailable(
                              "Nested transactions are not supported");
   }

   public PropagationContext get_txcontext()
      throws Unavailable
   {
      byte[] oid = getTargetId();
      if (oid[0] != COORDINATOR)
         throw new BAD_OPERATION();
      
      long localIdValue = LocalId.fromByteArray(oid, 1);
      LocalId localId = new LocalId(localIdValue);
      TransactionImpl tx = (TransactionImpl)getTransaction(localId);
      if (tx == null)
      {
         log.trace("RuntimeException in get_txcontext: transaction not found");
         throw new OBJECT_NOT_EXIST("No transaction.");
      }
      Xid xid = tx.getXid();

      // Create CORBA reference to the Coordinator (the target of this call)
      Coordinator coord = CoordinatorHelper.narrow(
            poa.create_reference_with_id(oid, CoordinatorHelper.id()));

      // Create and initialize PropagationContext object
      PropagationContext pc = new PropagationContext();
      pc.current = createTransIdentity(xid, coord, null);
      try
      {
         pc.timeout = divideAndRoundUp(tx.getTimeLeftBeforeTimeout(false), 1000);
      }
      catch (RollbackException e)
      {
         throw new UnreachableStatementException();
      }
      pc.parents = new TransIdentity[0];
      pc.implementation_specific_data = orb.create_any();
      
      // Return PropagationContext
      return pc; 
  }

   // Protected -----------------------------------------------------

   /**
    *  Get a reference to the transaction manager.
    */
   protected static TransactionManager getTransactionManager()
   {
      if (tm == null)
      {
         try
         {
            Context ctx = new InitialContext();
            tm = (TransactionManager)ctx.lookup("java:/TransactionManager");
         }
         catch (NamingException ex)
         {
            log.error("java:/TransactionManager lookup failed", ex);
         }
      }
      return tm;
   }

   /**
    *  Get a reference to the transaction importer.
    */
   protected static TransactionPropagationContextImporter getTPCImporter()
   {
      if (tpcImporter == null)
      {
         try
         {
            Context ctx = new InitialContext();
            tpcImporter = (TransactionPropagationContextImporter)ctx.lookup(
                                 "java:/TransactionPropagationContextImporter");
         }
         catch (NamingException ex)
         {
            log.error(
                  "java:/TransactionPropagationContextImporter lookup failed", 
                  ex);
         }
      }
      return tpcImporter;
   }

   // Private -------------------------------------------------------

   private static Transaction getTransaction(LocalId localId)
   {
      return getTPCImporter().importTransactionPropagationContext(localId);
   }

   private byte[] getTargetId()
   {
      byte[] id = null;
      try 
      {
         id = poaCurrent.get_object_id();
      }
      catch (NoContext e)
      {
         log.trace("Unexpected exception: " + e);
         throw new RuntimeException("Unexpected exception: " + e);
      }
      return id;
   }

   private void checkInvocationTarget(int targetType)
   {
      if (getTargetId()[0] != targetType)
         throw new BAD_OPERATION();
   }

   /**
    * Create and initialize TransIdentity object.
    */
   private static TransIdentity createTransIdentity(Xid xid, 
                                                    Coordinator coord, 
                                                    Terminator term)
   {
      // Concatenate global transaction id and branch qualifier
      byte gtrid[] = xid.getGlobalTransactionId();
      byte bqual[] = xid.getBranchQualifier();
      byte[] trid = new byte[gtrid.length + bqual.length];
      System.arraycopy(gtrid, 0, trid, 0, gtrid.length);
      System.arraycopy(bqual, 0, trid, gtrid.length, bqual.length);

      // Create TransIdentity instance and set its fields
      TransIdentity ti = new TransIdentity();
      ti.coord = coord;
      ti.term = term;
      ti.otid = new otid_t(xid.getFormatId(), bqual.length, trid);
      return ti;
   }

   private static int divideAndRoundUp(long m, long n)
   {
      long retval = m / n;

      if ((m % n) != 0)
         retval = retval + 1;
      return (int)retval ;
   }

   private static Status javaxToCosTransactions(int status)
   {
      switch (status)
      {
      case javax.transaction.Status.STATUS_ACTIVE:
         return org.omg.CosTransactions.Status.StatusActive;
      case javax.transaction.Status.STATUS_COMMITTED:
         return org.omg.CosTransactions.Status.StatusCommitted;
      case javax.transaction.Status.STATUS_COMMITTING:
         return org.omg.CosTransactions.Status.StatusCommitting;
      case javax.transaction.Status.STATUS_MARKED_ROLLBACK:
         return org.omg.CosTransactions.Status.StatusMarkedRollback;
      case javax.transaction.Status.STATUS_NO_TRANSACTION:
         return org.omg.CosTransactions.Status.StatusNoTransaction;
      case javax.transaction.Status.STATUS_PREPARED:
         return org.omg.CosTransactions.Status.StatusPrepared;
      case javax.transaction.Status.STATUS_PREPARING:
         return org.omg.CosTransactions.Status.StatusPreparing;
      case javax.transaction.Status.STATUS_ROLLEDBACK:
         return org.omg.CosTransactions.Status.StatusRolledBack;
      case javax.transaction.Status.STATUS_ROLLING_BACK:
         return org.omg.CosTransactions.Status.StatusRollingBack;
      case javax.transaction.Status.STATUS_UNKNOWN:
         return org.omg.CosTransactions.Status.StatusUnknown;
      default:
         log.trace("Invalid transaction status.");
         return org.omg.CosTransactions.Status.StatusUnknown;
      }
   }

    private static boolean compare(Xid xid,
                                   int otherFormatId, 
                                   byte[] otherGlobalId,
                                   int otherLength)
    {
        if (xid.getFormatId() != otherFormatId)
            return false;

        byte[] globalId = xid.getGlobalTransactionId();
        int len = globalId.length;

        if (len != otherLength)
            return false;

        for (int i = 0; i < len; ++i)
            if (globalId[i] != otherGlobalId[i])
                return false;

        return true;
    }

}
