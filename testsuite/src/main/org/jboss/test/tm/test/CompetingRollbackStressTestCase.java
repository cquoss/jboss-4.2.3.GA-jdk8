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
package org.jboss.test.tm.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import junit.framework.Test;

import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.ejb.EJBTestCase;
import org.jboss.tm.TransactionManagerLocator;

/**
 * Abstract concurrent stress test.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 65495 $
 */
public class CompetingRollbackStressTestCase extends EJBTestCase
{
   protected final Logger log = Logger.getLogger(getClass());
   
   private CountDownLatch latch;

   private TransactionManager tm;
   
   private AtomicReference<Transaction> transaction = new AtomicReference<Transaction>();

   public void testExecuteSQLDuringRollback() throws Throwable
   {
      for (int i = 0; i < getIterationCount(); ++i)
      {
         latch = new CountDownLatch(2);

         Main main = new Main();
         Thread thread1 = new Thread(main);
         thread1.start();

         Rollback rollback = new Rollback();
         Thread thread2 = new Thread(rollback);
         thread2.start();
         
         thread1.join();
         thread2.join();
         if (main.error != null)
            throw main.error;
         if (rollback.error != null)
            throw rollback.error;
      }
   }

   public class TestSynchronization implements Synchronization
   {
      private AtomicInteger invocations = new AtomicInteger(0);

      public int getInvocations()
      {
         return invocations.get();
      }
      
      public void afterCompletion(int status)
      {
         invocations.incrementAndGet();
      }

      public void beforeCompletion()
      {
      }
   }
   
   public class Main extends TestRunnable
   {
      private TestSynchronization synch;
      
      public void setup() throws Throwable
      {
         tm.begin();
         try
         {
            Transaction tx = tm.getTransaction();
            transaction.set(tm.getTransaction());
            synch = new TestSynchronization();
            tx.registerSynchronization(synch);
         }
         catch (Throwable t)
         {
            try
            {
               tm.rollback();
            }
            catch (Exception ignored)
            {
               log.warn("Ignored", ignored);
            }
            throw t;
         }
      }

      public void test() throws Throwable
      {
         try
         {
            tm.rollback();
         }
         catch (Exception ignored)
         {
         }
         int invocations = synch.getInvocations();
         if (invocations != 1)
            throw new RuntimeException("Synchronization invoked " + invocations + " times"); 
      }
   }
   
   public class Rollback extends TestRunnable
   {
      public void test() throws Throwable
      {
         Transaction tx = transaction.get();
         if (tx != null)
         {
            try
            {
               tx.rollback();
            }
            catch (Exception ignored)
            {
            }
         }
      }
   }
   
   public class TestRunnable implements Runnable
   {
      public Throwable error;
      
      public void setup() throws Throwable
      {
      }
      
      public void test() throws Throwable
      {
      }
      
      public void run()
      {
         try
         {
            setup();
         }
         catch (Throwable t)
         {
            error = t;
            latch.countDown();
            return;
         }
         latch.countDown();
         try
         {
            latch.await();
         }
         catch (InterruptedException e)
         {
            log.warn("Ignored", e);
         }
         try
         {
            test();
         }
         catch (Throwable t)
         {
            error = t;
         }
      }
   }
   
   protected void setUp() throws Exception
   {
      tm = TransactionManagerLocator.getInstance().locate();
   }
   
   public CompetingRollbackStressTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(CompetingRollbackStressTestCase.class, "transaction-test.jar");
   }
}
