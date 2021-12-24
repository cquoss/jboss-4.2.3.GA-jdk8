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

import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Tests for recovery
 *
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version <tt>$Revision: 57211 $</tt>
 */
public class RecoveryDurableTopicSendUnitTestCase extends RecoveryTest
{
   static String RECOVERY_TOPIC = "topic/recoveryDurableTopic";

   Topic recoveryTopic;
   
   public RecoveryDurableTopicSendUnitTestCase(String name) throws Exception
   {
      super(name);
   }

   /**
    * Send a message and prepare the transaction.
    * 
    * Assert the xid appears in the recovery list
    * Assert the message cannot be received
    */
   public void testSendPrepareShowsInRecovery() throws Exception
   {
      deployRecoveryService();
      try
      {
         makeSubscription(recoveryTopic);
         try
         {
            reset(recoveryTopic);
            JMSClient client = new JMSClient(recoveryTopic);
            try
            {
               Xid xid = new MyXid();
               client.enlist(xid);
               client.sendMessage("Hello");
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
            assertEquals(0, clearDestination(recoveryTopic));
         }
         finally
         {
            removeSubscription(recoveryTopic);
         }
      }
      finally
      {
         undeployRecoveryService();
      }
   }
   
   /**
    * Send a message and prepare the transaction, but
    * then roll it back
    * 
    * Assert the xid doesn't appears in the recovery list
    * Assert the message cannot be received
    */
   public void testSendPrepareRollbackDoesntShowInRecovery() throws Exception
   {
      deployRecoveryService();
      try
      {
         makeSubscription(recoveryTopic);
         try
         {
            reset(recoveryTopic);
            JMSClient client = new JMSClient(recoveryTopic);
            try
            {
               Xid xid = new MyXid();
               client.enlist(xid);
               client.sendMessage("Hello");
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
            assertEquals(0, clearDestination(recoveryTopic));
         }
         finally
         {
            removeSubscription(recoveryTopic);
         }
      }
      finally
      {
         undeployRecoveryService();
      }
   }
   
   /**
    * Send a message and prepare the transaction, but
    * then forget it
    * 
    * Assert the xid doesn't appears in the recovery list
    * Assert the message cannot be received
    */
   public void testSendPrepareForgetDoesntShowInRecovery() throws Exception
   {
      deployRecoveryService();
      try
      {
         makeSubscription(recoveryTopic);
         try
         {
            reset(recoveryTopic);
            JMSClient client = new JMSClient(recoveryTopic);
            try
            {
               Xid xid = new MyXid();
               client.enlist(xid);
               client.sendMessage("Hello");
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
            assertEquals(0, clearDestination(recoveryTopic));
         }
         finally
         {
            removeSubscription(recoveryTopic);
         }
      }
      finally
      {
         undeployRecoveryService();
      }
   }
   
   /**
    * Send a message and prepare the transaction, then commit it.
    * 
    * Assert the xid doesn't appears in the recovery list
    * Assert the message can be received
    */
   public void testSendPrepareCommitDoesntShowInRecovery() throws Exception
   {
      deployRecoveryService();
      try
      {
         makeSubscription(recoveryTopic);
         try
         {
            reset(recoveryTopic);
            JMSClient client = new JMSClient(recoveryTopic);
            try
            {
               Xid xid = new MyXid();
               client.enlist(xid);
               client.sendMessage("Hello");
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
            assertEquals(1, clearDestination(recoveryTopic));
         }
         finally
         {
            removeSubscription(recoveryTopic);
         }
      }
      finally
      {
         undeployRecoveryService();
      }
   }
   
   /**
    * Send a message and prepare the transaction on one connection
    * then commit it on another connection
    * 
    * Assert the xid appears in the recovery list before the commit
    * Assert the message cannot be received before the commit
    * Assert the xid doesn't appear in the recovery list after the commit
    * Assert the message can be received after the commit
    */
   public void testSendPrepareCommitDifferentConnection() throws Exception
   {
      deployRecoveryService();
      try
      {
         makeSubscription(recoveryTopic);
         try
         {
            reset(recoveryTopic);
            Xid xid = new MyXid();
            JMSClient client = new JMSClient(recoveryTopic);
            try
            {
               client.enlist(xid);
               client.sendMessage("Hello");
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
            assertEquals(0, clearDestination(recoveryTopic));

            client = new JMSClient(recoveryTopic);
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
            assertEquals(1, clearDestination(recoveryTopic));
         }
         finally
         {
            removeSubscription(recoveryTopic);
         }
      }
      finally
      {
         undeployRecoveryService();
      }
   }
   
   /**
    * Send a message and prepare the transaction on one connection
    * then roll it back on another connection
    * 
    * Assert the xid appears in the recovery list before the rollback
    * Assert the message cannot be received before the rollback
    * Assert the xid doesn't appear in the recovery list after the rollback
    * Assert the message cannot be received after the rollback
    */
   public void testSendPrepareRollbackDifferentConnection() throws Exception
   {
      deployRecoveryService();
      try
      {
         makeSubscription(recoveryTopic);
         try
         {
            reset(recoveryTopic);
            Xid xid = new MyXid();
            JMSClient client = new JMSClient(recoveryTopic);
            try
            {
               client.enlist(xid);
               client.sendMessage("Hello");
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
            assertEquals(0, clearDestination(recoveryTopic));

            client = new JMSClient(recoveryTopic);
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
            assertEquals(0, clearDestination(recoveryTopic));
         }
         finally
         {
            removeSubscription(recoveryTopic);
         }
      }
      finally
      {
         undeployRecoveryService();
      }
   }
   
   /**
    * Send a message and prepare the transaction on one connection
    * then forget it on another connection
    * 
    * Assert the xid appears in the recovery list before the forget
    * Assert the message cannot be received before the forget
    * Assert the xid doesn't appear in the recovery list after the forget
    * Assert the message cannot be received after the forget
    */
   public void testSendPrepareForgetDifferentConnection() throws Exception
   {
      deployRecoveryService();
      try
      {
         makeSubscription(recoveryTopic);
         try
         {
            reset(recoveryTopic);
            Xid xid = new MyXid();
            JMSClient client = new JMSClient(recoveryTopic);
            try
            {
               client.enlist(xid);
               client.sendMessage("Hello");
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
            assertEquals(0, clearDestination(recoveryTopic));

            client = new JMSClient(recoveryTopic);
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
            assertEquals(0, clearDestination(recoveryTopic));
         }
         finally
         {
            removeSubscription(recoveryTopic);
         }
      }
      finally
      {
         undeployRecoveryService();
      }
   }
   
   /**
    * Send a message and prepare the transaction on one connection
    * then commit it on another connection with a restart.
    * 
    * Assert the xid appears in the recovery list before the commit
    * Assert the message cannot be received before the commit
    * Assert the xid doesn't appear in the recovery list after the commit
    * Assert the message can be received after the commit
    */
   public void testSendPrepareCommitDifferentConnectionRestart() throws Exception
   {
      deployRecoveryService();
      try
      {
         makeSubscription(recoveryTopic);
         try
         {
            reset(recoveryTopic);
            Xid xid = new MyXid();
            JMSClient client = new JMSClient(recoveryTopic);
            try
            {
               client.enlist(xid);
               client.sendMessage("Hello");
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
            assertEquals(0, clearDestination(recoveryTopic));

            restartRecoveryService();

            assertEquals(0, clearDestination(recoveryTopic));
            
            client = new JMSClient(recoveryTopic);
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
            assertEquals(1, clearDestination(recoveryTopic));
         }
         finally
         {
            removeSubscription(recoveryTopic);
         }
      }
      finally
      {
         undeployRecoveryService();
      }
   }
   
   /**
    * Send a message and prepare the transaction on one connection
    * then roll it back on another connection with a restart
    * 
    * Assert the xid appears in the recovery list before the rollback
    * Assert the message cannot be received before the rollback
    * Assert the xid doesn't appear in the recovery list after the rollback
    * Assert the message cannot be received after the rollback
    */
   public void testSendPrepareRollbackDifferentConnectionRestart() throws Exception
   {
      deployRecoveryService();
      try
      {
         makeSubscription(recoveryTopic);
         try
         {
            reset(recoveryTopic);
            Xid xid = new MyXid();
            JMSClient client = new JMSClient(recoveryTopic);
            try
            {
               client.enlist(xid);
               client.sendMessage("Hello");
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
            assertEquals(0, clearDestination(recoveryTopic));

            restartRecoveryService();
            assertEquals(0, clearDestination(recoveryTopic));
            
            client = new JMSClient(recoveryTopic);
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
            assertEquals(0, clearDestination(recoveryTopic));
         }
         finally
         {
            removeSubscription(recoveryTopic);
         }
      }
      finally
      {
         undeployRecoveryService();
      }
   }
   
   /**
    * Send a message and prepare the transaction on one connection
    * then forget it on another connection with a restart
    * 
    * Assert the xid appears in the recovery list before the forget
    * Assert the message cannot be received before the forget
    * Assert the xid doesn't appear in the recovery list after the forget
    * Assert the message cannot be received after the forget
    */
   public void testSendPrepareForgetDifferentConnectionRestart() throws Exception
   {
      deployRecoveryService();
      try
      {
         makeSubscription(recoveryTopic);
         try
         {
            reset(recoveryTopic);
            Xid xid = new MyXid();
            JMSClient client = new JMSClient(recoveryTopic);
            try
            {
               client.enlist(xid);
               client.sendMessage("Hello");
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
            assertEquals(0, clearDestination(recoveryTopic));

            restartRecoveryService();
            assertEquals(0, clearDestination(recoveryTopic));
            
            client = new JMSClient(recoveryTopic);
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
            assertEquals(0, clearDestination(recoveryTopic));
         }
         finally
         {
            removeSubscription(recoveryTopic);
         }
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
      recoveryTopic = (Topic) ctx.lookup(RECOVERY_TOPIC);
   }
}
