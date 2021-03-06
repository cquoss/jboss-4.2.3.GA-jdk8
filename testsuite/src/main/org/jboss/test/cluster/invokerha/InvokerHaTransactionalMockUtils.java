/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.cluster.invokerha;

import java.rmi.server.UID;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.jboss.tm.TransactionPropagationContextFactory;
import org.jboss.tm.TransactionPropagationContextImporter;
import org.jboss.tm.TransactionPropagationContextUtil;

/**
 * Transactional mock utils for non managed invoker ha unit tests.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class InvokerHaTransactionalMockUtils
{
   private MockTransactionPropagationContextFactory tpcf;
   
   private MockTransactionPropagationContextImporter tpci;   
   
   public InvokerHaTransactionalMockUtils()
   {
      tpcf = createTransactionPropagationContextFactory();
      tpci = createTransactionPropagationContextImporter();      
   }
   
   public MockTransactionPropagationContextFactory getTpcf()
   {
      return tpcf;
   }

   public MockTransactionPropagationContextImporter getTpci()
   {
      return tpci;
   }

   protected MockTransactionPropagationContextFactory createTransactionPropagationContextFactory()
   {
      /* Set the TPC factory so that the invoker proxy can generate a tpc */
      MockTransactionPropagationContextFactory tpcf = new MockTransactionPropagationContextFactory();
      TransactionPropagationContextUtil.setTPCFactory(tpcf);
      return tpcf;
   }
   
   protected MockTransactionPropagationContextImporter createTransactionPropagationContextImporter()
   {
      MockTransactionPropagationContextImporter tpci = new MockTransactionPropagationContextImporter();
      TransactionPropagationContextUtil.setTPCImporter(tpci);
      return tpci;
   }   
   
   /** Classes **/
   
   public static class MockTransactionPropagationContextFactory implements TransactionPropagationContextFactory
   {      
      private UID uid;
      
      public Object getTransactionPropagationContext()
      {
         return uid;
      }

      public Object getTransactionPropagationContext(Transaction tx)
      {
         return null;
      }

      public void setUid(UID uid)
      {
         this.uid = uid;
      }

      public UID getUid()
      {
         return uid;
      }
   }
   
   public static class MockTransactionPropagationContextImporter implements TransactionPropagationContextImporter
   {
      private Transaction transaction;
      
      public Transaction importTransactionPropagationContext(Object tpc)
      {
         return transaction;
      }

      public void setTransaction(Transaction transaction)
      {
         this.transaction = transaction;
      }      
   }
   
   public static class MockTransaction implements Transaction
   {       
      public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
            SecurityException, SystemException
      {
      }

      public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException, SystemException
      {
         return false;
      }

      public boolean enlistResource(XAResource xaRes) throws RollbackException, IllegalStateException, SystemException
      {
         return false;
      }

      public int getStatus() throws SystemException
      {
         return 0;
      }

      public void registerSynchronization(Synchronization sync) throws RollbackException, IllegalStateException,
            SystemException
      {
      }

      public void rollback() throws IllegalStateException, SystemException
      {
      }

      public void setRollbackOnly() throws IllegalStateException, SystemException
      {
      }
   }

}
