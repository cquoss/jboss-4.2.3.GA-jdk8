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

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.TCKind;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions.Current;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.Inactive;
import org.omg.CosTransactions.InvalidControl;
import org.omg.CosTransactions.NoTransaction;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.Status;
import org.omg.CosTransactions.Status;
import org.omg.CosTransactions.SubtransactionsUnavailable;
import org.omg.CosTransactions.Terminator;
import org.omg.CosTransactions.Unavailable;
import org.omg.PortableInterceptor.InvalidSlot;

import org.jboss.tm.iiop.TransactionDesc;
import org.jboss.tm.iiop.TransactionFactoryExt;
import org.jboss.tm.iiop.TransactionFactoryExtHelper;
import org.jboss.tm.iiop.TxClientInterceptor;

/**
 * This class implements <code>org.omg.CosTransactions.Current</code>.
 *
 * @author Francisco Reverbel
 */

public class TransactionCurrent 
      extends LocalObject 
      implements Current 
{
   // Static fields -------------------------------------------------

   private static TransactionCurrent instance = null; // singleton instance
   private static TransactionFactoryExt txFactory;
   private static Set suspendedTransactions = 
                           Collections.synchronizedSet (new HashSet());
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

   // Initialization of static fields -------------------------------

   public static void init(NamingContextExt nc)
   {
      try 
      {
         org.omg.CORBA.Object txFactoryObj = 
            nc.resolve_str("TransactionService");
         txFactory = TransactionFactoryExtHelper.narrow(txFactoryObj);
      } 
      catch (CannotProceed e)
      {
         throw new RuntimeException(
                           "Exception initializing TransactionCurrent: " + e);
      }
      catch (NotFound e)
      {
         throw new RuntimeException(
                           "Exception initializing TransactionCurrent: " + e);
      }
      catch (InvalidName e)
      {
         throw new RuntimeException(
                           "Exception initializing TransactionCurrent: " + e);
      }
   }

   // Singleton accessor --------------------------------------------

   public static synchronized TransactionCurrent getInstance()
   {
      if (instance == null)
         instance = new TransactionCurrent();
      return instance;
   }
   
   // org.omg.CosTransactions.Current operations --------------------

   /**
    * Begins a new transaction, which will become the current transaction 
    * associated with the calling thread.
    *
    * @see org.omg.CosTransactions.CurrentOperations#begin()
    */
   public void begin() 
      throws SubtransactionsUnavailable
   {
      if (getThreadLocalControl() != null)
         throw new SubtransactionsUnavailable();
      TransactionDesc td = txFactory.create_transaction(
                                                      getThreadLocalTimeout());
      setCurrentTransaction(td.control, td.propagationContext);
   }
   
   /** 
    * Commits the current transaction.
    *
    * @see org.omg.CosTransactions.CurrentOperations#commit(boolean)
    */
   public void commit(boolean reportHeuristics) 
      throws NoTransaction, 
             HeuristicHazard, 
             HeuristicMixed
   {
      getThreadLocalTerminator().commit(reportHeuristics);
      unsetCurrentTransaction();
   }
   
   /**
    * Rolls the current transaction back.
    *
    * @see org.omg.CosTransactions.CurrentOperations#rollback()
    */
   public void rollback()
      throws NoTransaction
   {
      getThreadLocalTerminator().rollback();
      unsetCurrentTransaction();
   }
   
   /**
    * Marks the current transaction as rollback only.
    *
    * @see org.omg.CosTransactions.CurrentOperations#rollback_only()
    */
   public void rollback_only()
      throws NoTransaction
   {
      try
      {
         Coordinator coord = getThreadLocalCoordinator();

         if (coord == null)
            throw new NoTransaction();

         coord.rollback_only();
      } 
      catch (Inactive e)
      {
         throw new RuntimeException("Current transaction already prepared: " 
                                    + e);
      }
   }
   
   /**
    * Gets the status of current transaction.
    *
    * @see org.omg.CosTransactions.CurrentOperations#get_status()
    */
   public Status get_status() 
   {
      Coordinator coord = getThreadLocalCoordinator();
      return (coord == null) ? Status.StatusNoTransaction : coord.get_status();
   }
   
   /**
    * Returns the name of the current transaction.
    *
    * @see org.omg.CosTransactions.CurrentOperations#get_transaction_name()
    */
   public String get_transaction_name()
   {
      Coordinator coord = getThreadLocalCoordinator();
      return (coord == null) ? "" : coord.get_transaction_name();
   }
   
   /**
    * Sets the transaction time out that will be in effect for 
    * transactions created after this call.
    * 
    * @see org.omg.CosTransactions.CurrentOperations#set_timeout(int)
    */
   public void set_timeout(int timeOut)
   {
      setThreadLocalTimeout(timeOut);
   }
   
   /**
    * Returns the Control associated with the current transaction.
    *
    * @see org.omg.CosTransactions.CurrentOperations#get_control()
    */
   public Control get_control()
   {
      return getThreadLocalControl();
   }
   
   /**
    * Suspends the current transaction.
    *
    * @see org.omg.CosTransactions.CurrentOperations#suspend()
    */
   public Control suspend()
   {
      Control control = getThreadLocalControl();
      if (control != null) 
      {
         unsetCurrentTransaction();
         suspendedTransactions.add(control);
      }
      return control;
   }
   
   /**
    * Resumes the specified transaction.
    *
    * @see org.omg.CosTransactions.CurrentOperations#resume(org.omg.CosTransactions.Control)
    */
   public void resume(Control whichTransaction) 
      throws InvalidControl
   {
      try
      {
         if (whichTransaction == null)
            throw new InvalidControl();
         if (!suspendedTransactions.remove(whichTransaction))
            throw new InvalidControl();
         Coordinator coord = whichTransaction.get_coordinator();
         if (coord == null) 
            throw new InvalidControl();
         PropagationContext pc = coord.get_txcontext(); // throws Unavailable
         setCurrentTransaction(whichTransaction, pc);
      }
      catch (Unavailable e)
      {
         throw new InvalidControl();
      }
   }
   
}
