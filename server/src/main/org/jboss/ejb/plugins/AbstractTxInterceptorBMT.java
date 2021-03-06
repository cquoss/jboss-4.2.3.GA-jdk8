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
package org.jboss.ejb.plugins;

import java.util.Hashtable;

import java.rmi.RemoteException;

import javax.transaction.Transaction;
import javax.transaction.Status;
import javax.transaction.SystemException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.RefAddr;
import javax.naming.spi.ObjectFactory;

import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.AllowedOperationsAssociation;
import org.jboss.invocation.Invocation;
import org.jboss.tm.TxUtils;

/**
 *  A common superclass for the BMT transaction interceptors.
 *
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 73910 $
 */
abstract class AbstractTxInterceptorBMT extends AbstractTxInterceptor
{
   // Attributes ----------------------------------------------------

   /**
    *  This associates the thread to the UserTransaction.
    *
    *  It is used to redirect lookups on java:comp/UserTransaction to
    *  the <code>getUserTransaction()</code> method of the context.
    */
   private ThreadLocal userTransaction = new ThreadLocal();

   /**
    *  If <code>false</code>, transactions may live across bean instance
    *  invocations, otherwise the bean instance should terminate any
    *  transaction before returning from the invocation.
    *  This attribute defaults to <code>true</code>.
    */
   protected boolean stateless = true;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // Interceptor implementation --------------------------------------

   public void create() throws Exception
   {
      // Do initialization in superclass.
      super.create();

      // bind java:comp/UserTransaction
      RefAddr refAddr = new RefAddr("userTransaction")
      {
         /** This is never really serialized */
         private static final long serialVersionUID = -8228448967597474960L;

         public Object getContent()
         {
            return userTransaction;
         }
      };

      Reference ref = new Reference("javax.transaction.UserTransaction", refAddr, new UserTxFactory().getClass()
            .getName(), null);
      ((Context) new InitialContext().lookup("java:comp/")).bind("UserTransaction", ref);
   }

   public void stop()
   {
      // bind java:comp/UserTransaction
      try
      {
         ((Context) new InitialContext().lookup("java:comp/")).unbind("UserTransaction");
      }
      catch (Exception e)
      {
         //ignore
      }
   }

   // Protected  ----------------------------------------------------

   /*
    *  This method calls the next interceptor in the chain.
    *
    *  It handles the suspension of any client transaction, and the
    *  association of the calling thread with the instance transaction.
    *  And it takes care that any lookup of
    *  <code>java:comp/UserTransaction</code> will return the right
    *  UserTransaction for the bean instance.
    *
    *  @param remoteInvocation If <code>true</code> this is an invocation
    *                          of a method in the remote interface, otherwise
    *                          it is an invocation of a method in the home
    *                          interface.
    *  @param mi The <code>Invocation</code> of this call.
    */
   protected Object invokeNext(Invocation mi) throws Exception
   {
      // Save the transaction that comes with the MI
      Transaction oldTransaction = mi.getTransaction();

      // Get old threadlocal: It may be non-null if one BMT bean does a local
      // call to another.
      Object oldUserTx = userTransaction.get();

      // Suspend any transaction associated with the thread: It may be
      // non-null on optimized local calls.
      Transaction threadTx = tm.suspend();

      try
      {
         EnterpriseContext ctx = ((EnterpriseContext) mi.getEnterpriseContext());

         // Set the threadlocal to the userTransaction of the instance
         try
         {
            AllowedOperationsAssociation.pushInMethodFlag(IN_INTERCEPTOR_METHOD);
            userTransaction.set(ctx.getEJBContext().getUserTransaction());
         }
         finally
         {
            AllowedOperationsAssociation.popInMethodFlag();
         }

         // Get the bean instance transaction
         Transaction beanTx = ctx.getTransaction();

         // Resume the bean instance transaction
         // only if it not null, some TMs can't resume(null), e.g. Tyrex
         if (beanTx != null)
            tm.resume(beanTx);

         // Let the MI know about our new transaction
         mi.setTransaction(beanTx);

         try
         {
            // Let the superclass call next interceptor and do the exception
            // handling
            return super.invokeNext(mi, false);
         }
         finally
         {
            try
            {
               if (stateless)
                  checkStatelessDone();
               else
                  checkBadStateful();
            }
            finally
            {
               tm.suspend();
            }
         }
      }
      finally
      {
         // Reset threadlocal to its old value
         userTransaction.set(oldUserTx);

         // Restore old MI transaction
         // OSH: Why ???
         mi.setTransaction(oldTransaction);

         // If we had a Tx associated with the thread reassociate
         if (threadTx != null)
            tm.resume(threadTx);
      }
   }

   private void checkStatelessDone() throws RemoteException
   {
      int status = Status.STATUS_NO_TRANSACTION;

      try
      {
         status = tm.getStatus();
      }
      catch (SystemException ex)
      {
         log.error("Failed to get status", ex);
      }

      try
      {
         switch (status)
         {
            case Status.STATUS_ACTIVE :
            case Status.STATUS_COMMITTING :
            case Status.STATUS_MARKED_ROLLBACK :
            case Status.STATUS_PREPARING :
            case Status.STATUS_ROLLING_BACK :
               try
               {
                  tm.rollback();
               }
               catch (Exception ex)
               {
                  log.error("Failed to rollback", ex);
               }
            // fall through...
            case Status.STATUS_PREPARED :
               String msg = "Application error: BMT stateless bean " + container.getBeanMetaData().getEjbName()
                     + " should complete transactions before" + " returning (ejb1.1 spec, 11.6.1)";
               log.error(msg);

               // the instance interceptor will discard the instance
               throw new RemoteException(msg);
         }
      }
      finally
      {
         Transaction tx = null;
         try
         {
            tx = tm.suspend();
         }
         catch (SystemException ex)
         {
            log.error("Failed to suspend transaction", ex);
         }
         if (tx != null)
         {
            String msg = "Application error: BMT stateless bean " + container.getBeanMetaData().getEjbName()
                   + " should complete transactions before " + " returning (ejb1.1 spec, 11.6.1), suspended tx=" + tx ;
            log.error(msg);
            throw new RemoteException(msg);
         }
      }
   }

   private void checkBadStateful() throws RemoteException
   {
      int status = Status.STATUS_NO_TRANSACTION;

      try
      {
         status = tm.getStatus();
      }
      catch (SystemException ex)
      {
         log.error("Failed to get status", ex);
      }
      switch (status)
      {
         case Status.STATUS_COMMITTING :
         case Status.STATUS_MARKED_ROLLBACK :
         case Status.STATUS_PREPARING :
         case Status.STATUS_ROLLING_BACK :
            try
            {
               tm.rollback();
            }
            catch (Exception ex)
            {
               log.error("Failed to rollback", ex);
            }
            String msg = "BMT stateful bean '" + container.getBeanMetaData().getEjbName()
                  + "' did not complete user transaction properly status=" + TxUtils.getStatusAsString(status);
            log.error(msg);
      }
   }

   // Inner classes -------------------------------------------------

   public static class UserTxFactory implements ObjectFactory
   {
      public Object getObjectInstance(Object ref, Name name, Context nameCtx, Hashtable environment) throws Exception
      {
         // The ref is a list with only one RefAddr ...
         RefAddr refAddr = ((Reference) ref).get(0);
         // ... whose content is the threadlocal
         ThreadLocal threadLocal = (ThreadLocal) refAddr.getContent();

         // The threadlocal holds the right UserTransaction
         return threadLocal.get();
      }
   }

}
