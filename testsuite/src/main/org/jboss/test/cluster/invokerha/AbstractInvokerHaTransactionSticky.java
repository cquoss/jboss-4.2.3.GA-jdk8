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
import java.util.HashSet;
import java.util.Set;

import javax.transaction.Transaction;

import org.jboss.ha.framework.interfaces.LoadBalancePolicy;
import org.jboss.ha.framework.interfaces.TransactionStickyFirstAvailable;
import org.jboss.ha.framework.interfaces.TransactionStickyFirstAvailableIdenticalAllProxies;
import org.jboss.ha.framework.interfaces.TransactionStickyRandomRobin;
import org.jboss.ha.framework.interfaces.TransactionStickyRoundRobin;
import org.jboss.invocation.Invocation;
import org.jboss.logging.Logger;
import org.jboss.test.cluster.invokerha.InvokerHaTransactionalMockUtils.MockTransaction;

/**
 * AbstractInvokerHaTransactionSticky.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class AbstractInvokerHaTransactionSticky extends AbstractInvokerHa
{
   private static final Logger log = Logger.getLogger(AbstractInvokerHaTransactionSticky.class);
   
   private Set<Object> chosenTargets;
   
   @Override
   public void testTransactionalSuccessfulCallsRoundRobin() throws Exception
   {
      /* same proxies used in simulated transactions */
      transactionalSuccessfulCalls(TransactionStickyRoundRobin.class, false);
      /* different proxies in simulated transactions */
      transactionalSuccessfulCalls(TransactionStickyRoundRobin.class, true);
   }
   
   @Override
   public void testTransactionalSuccessfulCallsFirstAvailable() throws Exception
   {
      /* same proxies used in simulated transactions */
      transactionalSuccessfulCalls(TransactionStickyFirstAvailable.class, false);
      /* different proxies in simulated transactions */
      transactionalSuccessfulCalls(TransactionStickyFirstAvailable.class, true);
   }
   
   @Override
   public void testTransactionalSuccessfulCallsFirstAvailableIndenticalAllProxies() throws Exception
   {
      /* same proxies used in simulated transactions */
      transactionalSuccessfulCalls(TransactionStickyFirstAvailableIdenticalAllProxies.class, false);
      /* different proxies in simulated transactions */
      transactionalSuccessfulCalls(TransactionStickyFirstAvailableIdenticalAllProxies.class, true);
   }

   @Override
   public void testTransactionalSuccessfulCallsRandomRobin() throws Exception
   {
      /* same proxies used in simulated transactions */
      transactionalSuccessfulCalls(TransactionStickyRandomRobin.class, false);
      /* different proxies in simulated transactions */
      transactionalSuccessfulCalls(TransactionStickyRandomRobin.class, true);
   }
   
   public void testSuccessfulCallsRoundRobin() throws Exception
   {
      /* test transaction sticky round robin lbp without transactions, should 
       * behave like standard round robin */
      successfulCalls(TransactionStickyRoundRobin.class);
   }
   
   public void testSuccessfulCallsFirstAvailable() throws Exception
   {
      /* test transaction sticky first available lbp without transactions, should 
       * behave like standard first available */
      successfulCalls(TransactionStickyFirstAvailable.class);
   }
   
   public void testSuccessfulCallsFirstAvailableIndenticalAllProxies() throws Exception
   {
      /* test transaction sticky first available identical all proxies lbp 
       * without transactions, should behave like standard identical all 
       * proxies */
      successfulCalls(TransactionStickyFirstAvailableIdenticalAllProxies.class);
   }
   
   public void testSuccessfulCallsRandomRobin() throws Exception
   {
      /* test transaction sticky random robin lbp without transactions, should 
       * behave like standard random robin */
      successfulCalls(TransactionStickyRandomRobin.class);
   }
   
   public void testFailBeforeServer() throws Exception
   {
      /* if failure happens before reaching the server, regardless of whether
       * the failure happened in the 1st or the Nth invocation, it's not 
       * recoverable */
      transactionalFailureCalls(InvokerHaFailureType.BEFORE_SERVER, TransactionStickyRandomRobin.class, false);
   }
   
   public void testFailAfterServerNotCompletedButSuccessAfter() throws Exception
   {
      /* we use new proxies in between tests for 1st and Nth 
       * call so that results from Nth call don't get polluted with what 
       * happened in the 1st call test. */
      transactionalFailureCalls(InvokerHaFailureType.AFTER_SERVER_NOT_COMPLETED_BUT_SUCCESS_AFTER, TransactionStickyRandomRobin.class, true);
   }
   
   public void testFailAfterServerNotCompletedBothServers() throws Exception
   {
      /* we use new proxies in between tests for 1st and Nth call 
       * so that results from Nth call don't get polluted with what happened in 
       * the 1st call test. */
      transactionalFailureCalls(InvokerHaFailureType.AFTER_SERVER_NOT_COMPLETED_BOTH_SERVERS, TransactionStickyRandomRobin.class, true);
   }
   
   public void testFailAfterServerCompleted() throws Exception
   {
      /* We use new proxies in between tests for 1st and Nth call 
       * so that results from Nth call don't get polluted with what happened in 
       * the 1st call test. */
      transactionalFailureCalls(InvokerHaFailureType.AFTER_SERVER_COMPLETED, TransactionStickyRandomRobin.class, true);
   }

   @Override
   protected void transactionalSuccessfulCalls(Class<? extends LoadBalancePolicy> policyClass,
         boolean newProxiesInBetweenTransactions)
   {
      log.debug("transactional successfull calls [policy=" + policyClass + ",newProxiesInBetweenTransactions=" + newProxiesInBetweenTransactions + "]");
      
      try
      {
         UID uid;
         
         createNewProxies(0, policyClass, true);

         /* Simulate client user transaction */
         uid = new UID();         
         transactionalMockUtils.getTpcf().setUid(uid);
         performTransactionalStickyCalls(3, null, policyClass, newProxiesInBetweenTransactions);
         /* either set would do because they should be the same */
         Set<Object> chosenTargetsTx1 = chosenTargets;
         
         if (newProxiesInBetweenTransactions)
         {
            createNewProxies(0, policyClass, false);
         }
         
         /* Simulate transaction interceptor */
         uid = new UID();
         Transaction tx = new MockTransaction();
         transactionalMockUtils.getTpcf().setUid(uid);
         transactionalMockUtils.getTpci().setTransaction(tx);
         performTransactionalStickyCalls(3, tx, policyClass, newProxiesInBetweenTransactions);
         /* either set would do because they should be the same */
         Set<Object> chosenTargetsTx2 = chosenTargets;
         
         assertChosenTargetsInBetweenTx(policyClass, chosenTargetsTx1, chosenTargetsTx2, newProxiesInBetweenTransactions);
      }
      catch(Exception e)
      {
         /* catching to log the error properly (JUnit in eclipse does not show 
          * correctly exceptions from invokers) and fail */
         log.error("error", e);
         fail();
      }
      
   }
   
   protected void successfulCalls(Class<? extends LoadBalancePolicy> policyClass)
   {
      log.debug("successfull calls [policy=" + policyClass);
      
      try
      {
         createNewProxies(0, policyClass, true);
         performCalls(3, null, policyClass);
      }
      catch(Exception e)
      {
         /* catching to log the error properly (JUnit in eclipse does not show 
          * correctly exceptions from invokers) and fail */
         log.error("error", e);
         fail();
      }
   }

   protected void performTransactionalStickyCalls(int numberPairCalls, Transaction tx,
         Class<? extends LoadBalancePolicy> policyClass, boolean newProxiesInBetweenTransactions) throws Exception
   {
      Invocation inv;
      
      Set<Object> chosenTargetsDateTimeTeller = new HashSet<Object>(1);
      Set<Object> chosenTargetsSystemTimeTeller = new HashSet<Object>(1);
      
      for (int i = 0; i < numberPairCalls; i++)
      {
         /* create invocation to date time teller */
         inv = infrastructure.createDateTimeTellerInvocation(tx, null);
         /* invoke on proxy passing the invocation */
         log.debug(timeTellerProxy.invoke(inv));
         /* assert post conditions after invocation */
         chosenTargetsDateTimeTeller = assertSuccessfulPostConditions(inv, chosenTargetsDateTimeTeller);
         
         /* create invocation to system time teller */
         inv = infrastructure.createSystemTimeTellerInvocation(tx, null);
         /* invoke on proxy passing the invocation */
         log.debug(systemTimeProxy.invoke(inv));
         /* assert post conditions after invocation */
         chosenTargetsSystemTimeTeller = assertSuccessfulPostConditions(inv, chosenTargetsSystemTimeTeller);
      }
      
      /* for the duration of a transaction, all chosen targets should be the same */
      assertEquals(chosenTargetsDateTimeTeller, chosenTargetsSystemTimeTeller);
      
      chosenTargets = chosenTargetsDateTimeTeller;
   }
      
   protected Set<Object> assertSuccessfulPostConditions(Invocation inv, Set<Object> chosenTargets)
   {
      assertEquals(0, inv.getAsIsValue("FAILOVER_COUNTER"));
      Object chosenTarget = inv.getTransientValue(invokerHaFactory.getChosenTargetKey());
      assertNotNull(chosenTarget);
      /* check tx failover authorisations */
      assertTrue("transaction should have reached the server", invokerHaFactory.getTxFailoverAuthorizationsMap().containsKey(transactionalMockUtils.getTpcf().getUid()));
      /* add target to singleton set, there must only be one different target within a transaction */
      chosenTargets.add(chosenTarget);
      assertEquals(1, chosenTargets.size());
      
      return chosenTargets;
   }
   
   protected void assertChosenTargetsInBetweenTx(Class<? extends LoadBalancePolicy> policyClass, Set<?> chosenTargetsTx1, Set<?> chosenTargetsTx2, boolean newProxiesInBetweenTransactions)
   {
      if (policyClass.equals(TransactionStickyRoundRobin.class))
      {
         if (!newProxiesInBetweenTransactions)
         {
            /* If we're using sticky round robin and we didn't change proxies 
             * in between transactions, we can guarantee that the chosen targets
             * were different between transactions.*/
            assertNotSame(chosenTargetsTx1, chosenTargetsTx2);
         }
      }
      else if (policyClass.equals(TransactionStickyFirstAvailable.class))
      {
         if (!newProxiesInBetweenTransactions)
         {
            /* If we're using sticky first available and we didn't change proxies 
             * in between transactions, we can guarantee that the chosen targets
             * are equals between transactions.*/
            assertEquals(chosenTargetsTx1, chosenTargetsTx2);
         }
      }
      else if (policyClass.equals(TransactionStickyFirstAvailableIdenticalAllProxies.class))
      {
         /* If we're using sticky first available identical all proxies, 
          * regardless of whether we're using same proxies or not, we can 
          * guarantee that the chosen targets are the same between 
          * transactions.*/         
         assertEquals(chosenTargetsTx1, chosenTargetsTx2);
      }
   }
   
   protected void transactionalFailureCalls(InvokerHaFailureType failureType, Class<? extends LoadBalancePolicy> policyClass, boolean newProxiesInBetweenFailures) throws Exception
   {
      createNewProxies(0, policyClass, true);
      
      try 
      {
         /* fail in 1st call */
         failureCall(failureType, true, policyClass, newProxiesInBetweenFailures);
         
         if (newProxiesInBetweenFailures)
         {
            createNewProxies(0, policyClass, false);            
         }
         
         /* fail in Nth call */
         failureCall(failureType, false, policyClass, newProxiesInBetweenFailures);
      }
      catch(Exception e)
      {
         /* catching to log the error properly (JUnit in eclipse does not show 
          * correctly exceptions from invokers) and fail */
         log.error("error", e);
         fail();
      }      
   }
   
   protected void failureCall(InvokerHaFailureType failureType, boolean injectFailureIn1stCall, Class<? extends LoadBalancePolicy> policyClass, boolean newProxiesInBetweenFailures) throws Exception
   {
      UID uid;
      Invocation inv;
      
      if (injectFailureIn1stCall)
      {
         uid = new UID();         
         transactionalMockUtils.getTpcf().setUid(uid);
         /* When failure must happen in the very 1st call, we create a 1st 
          * invocation with the failure injected */
         inv = infrastructure.createDateTimeTellerInvocation(null, failureType);         
      }
      else
      {
         uid = new UID();
         Transaction tx = new MockTransaction();
         transactionalMockUtils.getTpcf().setUid(uid);
         transactionalMockUtils.getTpci().setTransaction(tx);
         /* When failure is not in first call, we do a call to each bean before 
          * injecting the faliure */
         performTransactionalStickyCalls(1, tx, policyClass, newProxiesInBetweenFailures);
         /* Now we create the invocation with the failure injected */
         inv = infrastructure.createDateTimeTellerInvocation(null, failureType);
      }
      
      try 
      {
         log.debug(timeTellerProxy.invoke(inv));
         if (failureType.isRecoverable(injectFailureIn1stCall))
         {
            failureType.assertFailoverCounter(injectFailureIn1stCall, inv.getAsIsValue("FAILOVER_COUNTER"));
            inv = infrastructure.createDateTimeTellerInvocation(null, null);
            log.debug(timeTellerProxy.invoke(inv));
         }
         else
         {
            fail("should have failed, failover is not allowed for " + failureType);            
         }
      }
      catch (Exception e)
      {
         if (failureType.isRecoverable(injectFailureIn1stCall))
         {
            fail("should have failed, failover is not allowed for " + failureType);
         }
         else
         {
            assertNull("transaction on the client side should be null", inv.getTransaction());
            failureType.assertFailoverCounter(injectFailureIn1stCall, inv.getAsIsValue("FAILOVER_COUNTER"));
            failureType.assertException(e);                        
         }
      }
   }

}
