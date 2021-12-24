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
package org.jboss.test.jbossmq.test;

import java.util.List;

import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Tests for recovery
 *
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version <tt>$Revision: 57211 $</tt>
 */
public class RecoveryQueueReceiveUnitTestCase extends RecoveryTest
{
   static String RECOVERY_QUEUE = "queue/recoveryQueue";

   Queue recoveryQueue;
   
   public RecoveryQueueReceiveUnitTestCase(String name) throws Exception
   {
      super(name);
   }

   /**
    * Receive a message and prepare the transaction.
    * 
    * Assert the xid appears in the recovery list
    * Assert the message is received after the rollback
    */
   public void testReceivePrepareShowsInRecovery() throws Exception
   {
      deployRecoveryService();
      try
      {
         reset(recoveryQueue);
         setup(recoveryQueue, 1);
         JMSClient client = new JMSClient(recoveryQueue);
         try
         {
            Xid xid = new MyXid();
            client.enlist(xid);
            assertNotNull(client.receiveNoWait());
            client.delist(xid);
            int result = client.prepare(xid);
            try
            {
               assertEquals(XAResource.XA_OK, result);
               List xids = client.recover();
               assertEquals(1, xids.size());
               assertXidEquals(xid, (Xid) xids.get(0));
            }
            finally
            {
               client.rollback(xid);
            }
         }
         finally
         {
            client.close();
         }
         assertEquals(1, clearDestination(recoveryQueue));
      }
      finally
      {
         undeployRecoveryService();
      }
   }
   
   /**
    * Receive a message and prepare the transaction, but
    * then roll it back
    * 
    * Assert the xid doesn't appears in the recovery list
    * Assert the message is received after the rollback
    */
   public void testReceivePrepareRollbackDoesntShowInRecovery() throws Exception
   {
      deployRecoveryService();
      try
      {
         reset(recoveryQueue);
         setup(recoveryQueue, 1);
         JMSClient client = new JMSClient(recoveryQueue);
         try
         {
            Xid xid = new MyXid();
            client.enlist(xid);
            assertNotNull(client.receiveNoWait());
            client.delist(xid);
            int result = client.prepare(xid);
            try
            {
               assertEquals(XAResource.XA_OK, result);
               List xids = client.recover();
               assertEquals(1, xids.size());
               assertXidEquals(xid, (Xid) xids.get(0));
               client.rollback(xid);
               xids = client.recover();
               assertEquals(0, xids.size());
            }
            finally
            {
               try
               {
                  client.rollback(xid);
               }
               catch (Exception ignored)
               {
               }
            }
         }
         finally
         {
            client.close();
         }
         assertEquals(1, clearDestination(recoveryQueue));
      }
      finally
      {
         undeployRecoveryService();
      }
   }
   
   /**
    * Receive a message and prepare the transaction, but
    * then forget it
    * 
    * Assert the xid doesn't appears in the recovery list
    * Assert the message is received after the forget
    */
   public void testReceivePrepareForgetDoesntShowInRecovery() throws Exception
   {
      deployRecoveryService();
      try
      {
         reset(recoveryQueue);
         setup(recoveryQueue, 1);
         JMSClient client = new JMSClient(recoveryQueue);
         try
         {
            Xid xid = new MyXid();
            client.enlist(xid);
            assertNotNull(client.receiveNoWait());
            client.delist(xid);
            int result = client.prepare(xid);
            try
            {
               assertEquals(XAResource.XA_OK, result);
               List xids = client.recover();
               assertEquals(1, xids.size());
               assertXidEquals(xid, (Xid) xids.get(0));
               client.forget(xid);
               xids = client.recover();
               assertEquals(0, xids.size());
            }
            finally
            {
               try
               {
                  client.rollback(xid);
               }
               catch (Exception ignored)
               {
               }
            }
         }
         finally
         {
            client.close();
         }
         assertEquals(1, clearDestination(recoveryQueue));
      }
      finally
      {
         undeployRecoveryService();
      }
   }
   
   /**
    * Receive a message and prepare the transaction, then commit it.
    * 
    * Assert the xid doesn't appears in the recovery list
    * Assert the message cannot be received after the commit
    */
   public void testReceivePrepareCommitDoesntShowInRecovery() throws Exception
   {
      deployRecoveryService();
      try
      {
         reset(recoveryQueue);
         setup(recoveryQueue, 1);
         JMSClient client = new JMSClient(recoveryQueue);
         try
         {
            Xid xid = new MyXid();
            client.enlist(xid);
            assertNotNull(client.receiveNoWait());
            client.delist(xid);
            int result = client.prepare(xid);
            try
            {
               assertEquals(XAResource.XA_OK, result);
               List xids = client.recover();
               assertEquals(1, xids.size());
               assertXidEquals(xid, (Xid) xids.get(0));
               client.commit(xid);
               xids = client.recover();
               assertEquals(0, xids.size());
            }
            finally
            {
               try
               {
                  client.rollback(xid);
               }
               catch (Exception ignored)
               {
               }
            }
         }
         finally
         {
            client.close();
         }
         assertEquals(0, clearDestination(recoveryQueue));
      }
      finally
      {
         undeployRecoveryService();
      }
   }
   
   /**
    * Receive a message and prepare the transaction on one connection
    * then commit it on another connection
    * 
    * Assert the xid appears in the recovery list before the commit
    * Assert the message cannot be received before the commit
    * Assert the xid doesn't appear in the recovery list after the commit
    * Assert the message cannot be received after the commit
    */
   public void testReceivePrepareCommitDifferentConnection() throws Exception
   {
      deployRecoveryService();
      try
      {
         reset(recoveryQueue);
         setup(recoveryQueue, 1);
         Xid xid = new MyXid();
         JMSClient client = new JMSClient(recoveryQueue);
         try
         {
            client.enlist(xid);
            assertNotNull(client.receiveNoWait());
            client.delist(xid);
            int result = client.prepare(xid);
            assertEquals(XAResource.XA_OK, result);
            List xids = client.recover();
            assertEquals(1, xids.size());
            assertXidEquals(xid, (Xid) xids.get(0));
         }
         finally
         {
            client.close();
         }

         assertEquals(0, clearDestination(recoveryQueue));
         
         client = new JMSClient(recoveryQueue);
         try
         {
            List xids = client.recover();
            assertEquals(1, xids.size());
            assertXidEquals(xid, (Xid) xids.get(0));
            client.commit(xid);
            xids = client.recover();
            assertEquals(0, xids.size());
         }
         finally
         {
            client.close();
         }
         assertEquals(0, clearDestination(recoveryQueue));
      }
      finally
      {
         undeployRecoveryService();
      }
   }
   
   /**
    * Receive a message and prepare the transaction on one connection
    * then roll it back on another connection
    * 
    * Assert the xid appears in the recovery list before the rollback
    * Assert the message cannot be received before the rollback
    * Assert the xid doesn't appear in the recovery list after the rollback
    * Assert the message can be received after the rollback
    */
   public void testReceivePrepareRollbackDifferentConnection() throws Exception
   {
      deployRecoveryService();
      try
      {
         reset(recoveryQueue);
         setup(recoveryQueue, 1);
         Xid xid = new MyXid();
         JMSClient client = new JMSClient(recoveryQueue);
         try
         {
            client.enlist(xid);
            assertNotNull(client.receiveNoWait());
            client.delist(xid);
            int result = client.prepare(xid);
            assertEquals(XAResource.XA_OK, result);
            List xids = client.recover();
            assertEquals(1, xids.size());
            assertXidEquals(xid, (Xid) xids.get(0));
         }
         finally
         {
            client.close();
         }
         assertEquals(0, clearDestination(recoveryQueue));

         client = new JMSClient(recoveryQueue);
         try
         {
            List xids = client.recover();
            assertEquals(1, xids.size());
            assertXidEquals(xid, (Xid) xids.get(0));
            client.rollback(xid);
            xids = client.recover();
            assertEquals(0, xids.size());
         }
         finally
         {
            client.close();
         }
         assertEquals(1, clearDestination(recoveryQueue));
      }
      finally
      {
         undeployRecoveryService();
      }
   }
   
   /**
    * Receive a message and prepare the transaction on one connection
    * then forget it on another connection
    * 
    * Assert the xid appears in the recovery list before the forget
    * Assert the message cannot be received before the forget
    * Assert the xid doesn't appear in the recovery list after the forget
    * Assert the message can be received after the forget
    */
   public void testReceivePrepareForgetDifferentConnection() throws Exception
   {
      deployRecoveryService();
      try
      {
         reset(recoveryQueue);
         setup(recoveryQueue, 1);
         Xid xid = new MyXid();
         JMSClient client = new JMSClient(recoveryQueue);
         try
         {
            client.enlist(xid);
            assertNotNull(client.receiveNoWait());
            client.delist(xid);
            int result = client.prepare(xid);
            assertEquals(XAResource.XA_OK, result);
            List xids = client.recover();
            assertEquals(1, xids.size());
            assertXidEquals(xid, (Xid) xids.get(0));
         }
         finally
         {
            client.close();
         }
         assertEquals(0, clearDestination(recoveryQueue));

         client = new JMSClient(recoveryQueue);
         try
         {
            List xids = client.recover();
            assertEquals(1, xids.size());
            assertXidEquals(xid, (Xid) xids.get(0));
            client.forget(xid);
            xids = client.recover();
            assertEquals(0, xids.size());
         }
         finally
         {
            client.close();
         }
         assertEquals(1, clearDestination(recoveryQueue));
      }
      finally
      {
         undeployRecoveryService();
      }
   }
   
   /**
    * Receive a message and prepare the transaction on one connection
    * then commit it on another connection with a restart
    * 
    * Assert the xid appears in the recovery list before the commit
    * Assert the message cannot be received before the commit
    * Assert the xid doesn't appear in the recovery list after the commit
    * Assert the message cannot be received after the commit
    */
   public void testReceivePrepareCommitDifferentConnectionRestart() throws Exception
   {
      deployRecoveryService();
      try
      {
         reset(recoveryQueue);
         setup(recoveryQueue, 1);
         Xid xid = new MyXid();
         JMSClient client = new JMSClient(recoveryQueue);
         try
         {
            client.enlist(xid);
            assertNotNull(client.receiveNoWait());
            client.delist(xid);
            int result = client.prepare(xid);
            assertEquals(XAResource.XA_OK, result);
            List xids = client.recover();
            assertEquals(1, xids.size());
            assertXidEquals(xid, (Xid) xids.get(0));
         }
         finally
         {
            client.close();
         }

         assertEquals(0, clearDestination(recoveryQueue));
         
         restartRecoveryService();
         assertEquals(0, clearDestination(recoveryQueue));
         
         client = new JMSClient(recoveryQueue);
         try
         {
            List xids = client.recover();
            assertEquals(1, xids.size());
            assertXidEquals(xid, (Xid) xids.get(0));
            client.commit(xid);
            xids = client.recover();
            assertEquals(0, xids.size());
         }
         finally
         {
            client.close();
         }
         assertEquals(0, clearDestination(recoveryQueue));
      }
      finally
      {
         undeployRecoveryService();
      }
   }
   
   /**
    * Receive a message and prepare the transaction on one connection
    * then roll it back on another connection with a restart
    * 
    * Assert the xid appears in the recovery list before the rollback
    * Assert the message cannot be received before the rollback
    * Assert the xid doesn't appear in the recovery list after the rollback
    * Assert the message can be received after the rollback
    */
   public void testReceivePrepareRollbackDifferentConnectionRestart() throws Exception
   {
      deployRecoveryService();
      try
      {
         reset(recoveryQueue);
         setup(recoveryQueue, 1);
         Xid xid = new MyXid();
         JMSClient client = new JMSClient(recoveryQueue);
         try
         {
            client.enlist(xid);
            assertNotNull(client.receiveNoWait());
            client.delist(xid);
            int result = client.prepare(xid);
            assertEquals(XAResource.XA_OK, result);
            List xids = client.recover();
            assertEquals(1, xids.size());
            assertXidEquals(xid, (Xid) xids.get(0));
         }
         finally
         {
            client.close();
         }
         assertEquals(0, clearDestination(recoveryQueue));
         
         restartRecoveryService();
         assertEquals(0, clearDestination(recoveryQueue));

         client = new JMSClient(recoveryQueue);
         try
         {
            List xids = client.recover();
            assertEquals(1, xids.size());
            assertXidEquals(xid, (Xid) xids.get(0));
            client.rollback(xid);
            xids = client.recover();
            assertEquals(0, xids.size());
         }
         finally
         {
            client.close();
         }
         assertEquals(1, clearDestination(recoveryQueue));
      }
      finally
      {
         undeployRecoveryService();
      }
   }
   
   /**
    * Receive a message and prepare the transaction on one connection
    * then forget it on another connection with a restart
    * 
    * Assert the xid appears in the recovery list before the forget
    * Assert the message cannot be received before the forget
    * Assert the xid doesn't appear in the recovery list after the forget
    * Assert the message can be received after the forget
    */
   public void testReceivePrepareForgetDifferentConnectionRestart() throws Exception
   {
      deployRecoveryService();
      try
      {
         reset(recoveryQueue);
         setup(recoveryQueue, 1);
         Xid xid = new MyXid();
         JMSClient client = new JMSClient(recoveryQueue);
         try
         {
            client.enlist(xid);
            assertNotNull(client.receiveNoWait());
            client.delist(xid);
            int result = client.prepare(xid);
            assertEquals(XAResource.XA_OK, result);
            List xids = client.recover();
            assertEquals(1, xids.size());
            assertXidEquals(xid, (Xid) xids.get(0));
         }
         finally
         {
            client.close();
         }
         assertEquals(0, clearDestination(recoveryQueue));
         
         restartRecoveryService();
         assertEquals(0, clearDestination(recoveryQueue));

         client = new JMSClient(recoveryQueue);
         try
         {
            List xids = client.recover();
            assertEquals(1, xids.size());
            assertXidEquals(xid, (Xid) xids.get(0));
            client.forget(xid);
            xids = client.recover();
            assertEquals(0, xids.size());
         }
         finally
         {
            client.close();
         }
         assertEquals(1, clearDestination(recoveryQueue));
      }
      finally
      {
         undeployRecoveryService();
      }
   }
   
   protected void deployRecoveryService() throws Exception
   {
      super.deployRecoveryService();
      InitialContext ctx = getInitialContext();
      recoveryQueue = (Queue) ctx.lookup(RECOVERY_QUEUE);
   }
}
