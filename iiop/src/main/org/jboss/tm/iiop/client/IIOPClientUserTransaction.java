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
package org.jboss.tm.iiop.client;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.UserTransaction;

import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.Inactive;
import org.omg.CosTransactions.NoTransaction;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.SubtransactionsUnavailable;
import org.omg.CosTransactions.Terminator;

import org.jboss.iiop.CorbaORB;
import org.jboss.tm.iiop.TransactionDesc;
import org.jboss.tm.iiop.TransactionFactoryExt;
import org.jboss.tm.iiop.TransactionFactoryExtHelper;
import org.jboss.tm.iiop.TxClientInterceptor;

/**
 * The client-side UserTransaction implementation for RMI/IIOP clients.
 * This will delegate all UserTransaction calls to the server.
 *
 * <em>Warning:</em> This is only for stand-alone RMI/IIOP clients that 
 * do not have their own transaction service. No local work is done in
 * the context of transactions started here, only work done in beans
 * at the server. 
 *
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 57194 $
 */
public class IIOPClientUserTransaction
      implements UserTransaction,
                 Referenceable,
                 Serializable
{
   
   // Static --------------------------------------------------------
   static final long serialVersionUID = 6653800687253055416L;

   /** Our singleton instance. */
   private static IIOPClientUserTransaction singleton = null;

   /** CORBA reference to the server's transaction factory. */
   private static TransactionFactoryExt txFactory;

   /** Transaction information associated with the current thread. */
   private static ThreadLocal threadLocalData = new ThreadLocal() {
         protected synchronized Object initialValue() 
         {
            return new TransactionInfo(); // see nested class below
         }
      };
 
   // Nested class  -------------------------------------------------

   /**
    * The <code>TransactionInfo</code> class holds transaction information 
    * associated with the current thread. The <code>threadLocalData</code> 
    * field contains an instance of this class. The field timeout applies
    * to new transactions started by the current thread; its value is not 
    * necessarily equal to the time out of the currrent transaction. The 
    * three remaining fields refer to the currrent transaction.
    */
   private static class TransactionInfo
   {
      int timeout = 0;   // for new transactions started by the current thread
      Control control;   // null if no current transaction
      Coordinator coord; // null if no current transaction
      Terminator term;   // null if no current transaction
   }

   // Static accessors to thread-local data -------------------------

   private static void setThreadLocalTimeout(int timeout)
   {
      ((TransactionInfo)threadLocalData.get()).timeout = timeout;
   }

   private static int getThreadLocalTimeout()
   {
      return ((TransactionInfo)threadLocalData.get()).timeout;
   }
   
   private static void setThreadLocalControl(Control control)
   {
      ((TransactionInfo)threadLocalData.get()).control = control;
   }

   private static Control getThreadLocalControl()
   {
      return ((TransactionInfo)threadLocalData.get()).control;
   }
   
   private static void setThreadLocalCoordinator(Coordinator coord)
   {
      ((TransactionInfo)threadLocalData.get()).coord = coord;
   }

   private static Coordinator getThreadLocalCoordinator()
   {
      return ((TransactionInfo)threadLocalData.get()).coord;
   }
   
   private static void setThreadLocalTerminator(Terminator term)
   {
      ((TransactionInfo)threadLocalData.get()).term = term;
   }

   private static Terminator getThreadLocalTerminator()
      throws NoTransaction
   {
      Terminator term = ((TransactionInfo)threadLocalData.get()).term;

      if (term == null)
         throw new NoTransaction();

      return term;
   }
   
   // Management of the transaction-thread association --------------

   /**
    * Auxiliary method that sets the current transaction.
    */
   private static void setCurrentTransaction(Control control, 
                                             PropagationContext pc) 
   {
      setThreadLocalControl(control);
      setThreadLocalCoordinator(pc.current.coord);
      setThreadLocalTerminator(pc.current.term);
      TxClientInterceptor.setOutgoingPropagationContext(pc);
   }

   /**
    * Auxiliary method that unsets the current transaction.
    */
   private static void unsetCurrentTransaction() 
   {
      setThreadLocalControl(null);
      setThreadLocalCoordinator(null);
      setThreadLocalTerminator(null);
      TxClientInterceptor.unsetOutgoingPropagationContext();
   }

   // Other auxiliary (and static) methods  -------------------------

   /**
    *  Returns a CORBA reference to the TransactionFactory implemented by 
    *  the JBoss server.
    */
   private static TransactionFactoryExt getTxFactory()
   {
      if (txFactory == null)
      {
         try 
         {
            ORB orb = CorbaORB.getInstance();
            org.omg.CORBA.Object obj = 
               orb.resolve_initial_references("NameService");
            NamingContextExt rootContext = NamingContextExtHelper.narrow(obj);
            org.omg.CORBA.Object txFactoryObj = 
               rootContext.resolve_str("TransactionService");
            txFactory = TransactionFactoryExtHelper.narrow(txFactoryObj);
         }
         catch (Exception e)
         {
            throw new RuntimeException("Could not get transaction factory: " 
                                       + e);
         }
      }
      return txFactory;
   }

   /**
    *  Converts transaction status from org.omg.CosTransactions format 
    *  to javax.transaction format.
    */
   private static int cosTransactionsToJavax(
                                       org.omg.CosTransactions.Status status)
   {
      switch (status.value())
      {
      case org.omg.CosTransactions.Status._StatusActive:
         return javax.transaction.Status.STATUS_ACTIVE;
      case org.omg.CosTransactions.Status._StatusCommitted:
         return javax.transaction.Status.STATUS_COMMITTED;
      case org.omg.CosTransactions.Status._StatusCommitting:
         return javax.transaction.Status.STATUS_COMMITTING;
      case org.omg.CosTransactions.Status._StatusMarkedRollback:
         return javax.transaction.Status.STATUS_MARKED_ROLLBACK;
      case org.omg.CosTransactions.Status._StatusNoTransaction:
         return javax.transaction.Status.STATUS_NO_TRANSACTION;
      case org.omg.CosTransactions.Status._StatusPrepared:
         return javax.transaction.Status.STATUS_PREPARED;
      case org.omg.CosTransactions.Status._StatusPreparing:
         return javax.transaction.Status.STATUS_PREPARING;
      case org.omg.CosTransactions.Status._StatusRolledBack:
         return javax.transaction.Status.STATUS_ROLLEDBACK;
      case org.omg.CosTransactions.Status._StatusRollingBack:
         return javax.transaction.Status.STATUS_ROLLING_BACK;
      case org.omg.CosTransactions.Status._StatusUnknown:
         return javax.transaction.Status.STATUS_UNKNOWN;
      default:
         return javax.transaction.Status.STATUS_UNKNOWN;
      }
   }

   // Constructors --------------------------------------------------

   /**
    *  Create a new instance.
    */
   private IIOPClientUserTransaction()
   {
   }

   // Public --------------------------------------------------------

   /**
    *  Returns a reference to the singleton instance.
    */
   public static IIOPClientUserTransaction getSingleton()
   {
      if (singleton == null)
         singleton = new IIOPClientUserTransaction();
      return singleton;
   }

   //
   // Implementation of interface UserTransaction
   //

   public void begin()
      throws NotSupportedException, SystemException
   {
      if (getThreadLocalControl() != null)
         throw new NotSupportedException();
      try
      {
         TransactionDesc td = 
            getTxFactory().create_transaction(getThreadLocalTimeout());
         setCurrentTransaction(td.control, td.propagationContext);
      }
      catch (RuntimeException e)
      {
         SystemException se = new SystemException("Failed to create tx");
         se.initCause(e);
      }
   }

   public void commit()
      throws RollbackException,
             HeuristicMixedException,
             HeuristicRollbackException,
             SecurityException,
             IllegalStateException,
             SystemException
   {
      try
      {
         getThreadLocalTerminator().commit(true /* reportHeuristics */);
         unsetCurrentTransaction();
      }
      catch (NoTransaction e)
      {
         IllegalStateException ex = new IllegalStateException();
         ex.initCause(e);
         throw ex;
      }
      catch (HeuristicMixed e)
      {
         HeuristicMixedException ex = new HeuristicMixedException();
         ex.initCause(e);
         throw ex;
      }
      catch (HeuristicHazard e)
      {
         HeuristicRollbackException ex = new HeuristicRollbackException();
         ex.initCause(e);
         throw ex;
      }
      catch (TRANSACTION_ROLLEDBACK e)
      {
         RollbackException ex = new RollbackException();
         ex.initCause(e);
         throw ex;
      }
      catch (BAD_INV_ORDER e)
      {
         IllegalStateException ex = new IllegalStateException();
         ex.initCause(e);
         throw ex;
      }
      catch (NO_PERMISSION e)
      {
         SecurityException ex = new SecurityException();
         ex.initCause(e);
         throw ex;
      }
      catch (RuntimeException e)
      {
         SystemException ex = new SystemException();
         ex.initCause(e);
         throw ex;
      }
   }

   public void rollback()
      throws SecurityException,
             IllegalStateException,
             SystemException
   {
      try
      {
         getThreadLocalTerminator().rollback();
         unsetCurrentTransaction();
      }
      catch (NoTransaction e)
      {
         IllegalStateException ex = new IllegalStateException();
         ex.initCause(e);
         throw ex;
      }
      catch (BAD_INV_ORDER e)
      {
         IllegalStateException ex = new IllegalStateException();
         ex.initCause(e);
         throw ex;
      }
      catch (NO_PERMISSION e)
      {
         SecurityException ex = new SecurityException();
         ex.initCause(e);
         throw ex;
      }
      catch (RuntimeException e)
      {
         SystemException ex = new SystemException();
         ex.initCause(e);
         throw ex;
      }
   }

   public void setRollbackOnly()
      throws IllegalStateException,
             SystemException
   {
      Coordinator coord = getThreadLocalCoordinator();
      
      if (coord == null)
         throw new IllegalStateException();
      
      try
      {
         coord.rollback_only();
      }
      catch (Inactive e)
      {
         SystemException ex = new SystemException();
         ex.initCause(e);
         throw ex;
      }
      catch (BAD_INV_ORDER e)
      {
         IllegalStateException ex = new IllegalStateException();
         ex.initCause(e);
         throw ex;
      }
      catch (RuntimeException e)
      {
         SystemException ex = new SystemException();
         ex.initCause(e);
         throw ex;
      }
   }

   public int getStatus()
      throws SystemException
   {
      try
      {
         Coordinator coord = getThreadLocalCoordinator();
         if (coord == null)
            return Status.STATUS_NO_TRANSACTION;
         else 
            return cosTransactionsToJavax(coord.get_status());
      }
      catch (RuntimeException e)
      {
         SystemException ex = new SystemException();
         ex.initCause(e);
         throw ex;
      }
   }

   public void setTransactionTimeout(int seconds)
      throws SystemException
   {
      setThreadLocalTimeout(seconds);
   }
   
   //
   // Implementation of interface Referenceable
   //

   public Reference getReference()
      throws NamingException
   {
      Reference ref = new Reference(
            "org.jboss.tm.iiop.client.IIOPClientUserTransaction",
            "org.jboss.tm.iiop.client.IIOPClientUserTransactionObjectFactory",
            null);
      return ref;
   }

}
