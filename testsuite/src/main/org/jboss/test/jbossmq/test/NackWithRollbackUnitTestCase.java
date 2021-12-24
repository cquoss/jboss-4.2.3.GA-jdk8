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

import javax.jms.DeliveryMode;

import org.jboss.mq.AcknowledgementRequest;
import org.jboss.mq.ConnectionToken;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.SpyQueue;
import org.jboss.mq.Subscription;
import org.jboss.mq.TransactionRequest;
import org.jboss.mq.server.JMSDestinationManager;
import org.jboss.test.jbossmq.JBossMQMicrocontainerTest;
import org.jboss.test.jbossmq.support.MockClientIL;

/**
 * NackWithRollbackUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class NackWithRollbackUnitTestCase extends JBossMQMicrocontainerTest 
{
   public NackWithRollbackUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testNackWithRollback() throws Exception
   {
      SpyQueue queue = createQueue("testQueue");
      
      JMSDestinationManager server = getJMSServer();
      MockClientIL client = new MockClientIL();
      ConnectionToken dc = new ConnectionToken("test", client, "session");
      server.setEnabled(dc, true);
      
      // Add a message
      SpyMessage send = new SpyMessage();
      send.setJMSDestination(queue);
      send.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
      long ts = System.currentTimeMillis();
      send.setJMSTimestamp(ts);
      send.setJMSPriority(5);
      send.setJMSMessageID("Message1");
      server.addMessage(dc, send);

      // Subscribe to the queue
      Subscription sub = new Subscription();
      sub.connectionToken = dc;
      sub.subscriptionId = 1;
      sub.destination = queue;
      sub.noLocal = false;
      server.subscribe(dc, sub);
      
      // Get the message
      SpyMessage received = server.receive(dc, sub.subscriptionId, -1);
      assertNotNull(received);
      
      // Prepare a nack
      TransactionRequest t = new TransactionRequest();
      t.xid = new Long(1);
      t.requestType = TransactionRequest.TWO_PHASE_COMMIT_PREPARE_REQUEST;
      received.createAcknowledgementRequest(sub.subscriptionId);
      AcknowledgementRequest nack = received.getAcknowledgementRequest(false);
      t.acks = new AcknowledgementRequest[] { nack };
      server.transact(dc, t);

      // Rollback
      t = new TransactionRequest();
      t.xid = new Long(1);
      t.requestType = TransactionRequest.TWO_PHASE_COMMIT_ROLLBACK_REQUEST;
      server.transact(dc, t);

      // Should re-receive
      received = server.receive(dc, sub.subscriptionId, -1);
      assertNotNull(received);
   }
}
