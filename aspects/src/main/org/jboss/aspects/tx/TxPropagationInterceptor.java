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
package org.jboss.aspects.tx;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.logging.Logger;
import org.jboss.tm.TransactionManagerLocator;
import org.jboss.tm.TransactionPropagationContextFactory;
import org.jboss.tm.TransactionPropagationContextUtil;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 64759 $
 */
public class TxPropagationInterceptor implements Interceptor
{
   private TransactionManager tm;
   
   private static final Logger log = Logger.getLogger(TxPropagationInterceptor.class);

   public TxPropagationInterceptor(TransactionManager tm)
   {
      this.tm = tm;
   }

   public TxPropagationInterceptor()
   {
      tm = TransactionManagerLocator.getInstance().locate();
   }

   public String getName()
   {
      return "TxPropagationInterceptor";
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      Object importedTpc = invocation.getMetaData(ClientTxPropagationInterceptor.TRANSACTION_PROPAGATION_CONTEXT, ClientTxPropagationInterceptor.TRANSACTION_PROPAGATION_CONTEXT);
      if (importedTpc != null)
      {
         
         Transaction tx = tm.getTransaction();
         
         if (tx != null)
         {
            TransactionPropagationContextFactory tpcFactory = TransactionPropagationContextUtil.getTPCFactory();
            Object tpc = null;
            if (tpcFactory != null)
               tpc = tpcFactory.getTransactionPropagationContext();
            
            if (importedTpc.equals(tpc))
               return invocation.invokeNext();
            else
               throw new RuntimeException("cannot import a transaction context when a transaction is already associated with the thread");
         }
         
         Transaction importedTx = TransactionPropagationContextUtil.getTPCImporter().importTransactionPropagationContext(importedTpc);
         
         tm.resume(importedTx);
         try
         {
            return invocation.invokeNext();
         }
         finally
         {
            tm.suspend();
         }
      }
      else
      {
         return invocation.invokeNext();
      }
   }
}
