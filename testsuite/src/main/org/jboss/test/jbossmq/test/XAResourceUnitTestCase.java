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

import javax.jms.Message;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.jms.XATopicConnection;
import javax.jms.XATopicConnectionFactory;
import javax.jms.XATopicSession;
import javax.naming.InitialContext;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.test.JBossTestCase;

/**
 * XAResourceUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57211 $
 */
public class XAResourceUnitTestCase extends JBossTestCase
{
   static String XA_TOPIC_FACTORY = "XAConnectionFactory";

   static String TEST_TOPIC = "topic/testTopic";

   public XAResourceUnitTestCase(String name) throws Exception
   {
      super(name);
   }

   public void testXAResourceSuspendWorkCommit() throws Exception
   {
      InitialContext context = getInitialContext();
      XATopicConnectionFactory factory = (XATopicConnectionFactory) context.lookup(XA_TOPIC_FACTORY);
      Topic topic = (Topic) context.lookup(TEST_TOPIC);

      XATopicConnection connection = factory.createXATopicConnection();
      try
      {
         // Set up
         XATopicSession xaSession = connection.createXATopicSession();
         TopicSession session = xaSession.getTopicSession();
         TopicPublisher publisher = session.createPublisher(topic);
         Message message = session.createTextMessage();

         // Add the xa resource to xid1
         MyXid xid1 = new MyXid();
         XAResource resource = xaSession.getXAResource();
         resource.start(xid1, XAResource.TMNOFLAGS);

         // Do some work
         publisher.publish(message);

         // Suspend the transaction
         resource.end(xid1, XAResource.TMSUSPEND);

         // Add the xa resource to xid2
         MyXid xid2 = new MyXid();
         resource.start(xid2, XAResource.TMNOFLAGS);

         // Do some work in the new transaction
         publisher.publish(message);

         // Commit the first transaction and end the branch
         resource.end(xid1, XAResource.TMSUCCESS);
         resource.commit(xid1, true);

         // Do some more work in the new transaction
         publisher.publish(message);

         // Commit the second transaction and end the branch
         resource.end(xid2, XAResource.TMSUCCESS);
         resource.commit(xid2, true);
      }
      finally
      {
         connection.close();
      }
   }

   public void testXAResourceRollbackAfterPrepare() throws Exception
   {
      InitialContext context = getInitialContext();
      XATopicConnectionFactory factory = (XATopicConnectionFactory) context.lookup(XA_TOPIC_FACTORY);
      Topic topic = (Topic) context.lookup(TEST_TOPIC);

      XATopicConnection connection = factory.createXATopicConnection();
      try
      {
         // Set up
         XATopicSession xaSession = connection.createXATopicSession();
         TopicSession session = xaSession.getTopicSession();
         TopicSubscriber subscriber = session.createSubscriber(topic);
         connection.start();
         TopicPublisher publisher = session.createPublisher(topic);
         Message message = session.createTextMessage();

         // Publish a message using "AutoAcknowledge"
         publisher.publish(message);

         // Add the xa resource to xid1
         MyXid xid1 = new MyXid();
         XAResource resource = xaSession.getXAResource();
         resource.start(xid1, XAResource.TMNOFLAGS);

         // Receive the message
         message = subscriber.receiveNoWait();
         if (message == null)
            fail("No message?");

         // Prepare the transaction
         resource.end(xid1, XAResource.TMSUCCESS);
         resource.prepare(xid1);
         
         // Rollback
         resource.rollback(xid1);
         
         // Receive the message using "AutoAcknowledge"
         message = subscriber.receiveNoWait();
         if (message == null)
            fail("No message after rollback?");
      }
      finally
      {
         connection.close();
      }
   }

   public static class MyXid
      implements Xid
   {
      static byte next = 0;

      byte[] xid;

      public MyXid()
      {
         xid = new byte[] { ++next };
      }
 
      public int getFormatId()
      {
         return 314;
      }

      public byte[] getGlobalTransactionId()
      {
         return xid;
      }

      public byte[] getBranchQualifier()
      {
         return null;
      }
   }
}
