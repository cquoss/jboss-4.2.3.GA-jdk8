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

import java.util.Arrays;
import java.util.List;

import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.naming.InitialContext;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.test.JBossTestCase;

/**
 * Tests for recovery
 *
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version <tt>$Revision: 57211 $</tt>
 */
public abstract class RecoveryTest extends JBossTestCase
{
   static String RECOVERY_CONFIG = "jbossmq-recovery-service.xml";

   static String SUBSCRIPTION = "testSubscription";
   
   static String FACTORY = "RecoveryXAConnectionFactory";
   
   XAConnectionFactory connectionFactory;
   
   public RecoveryTest(String name) throws Exception
   {
      super(name);
   }
   
   class JMSClient implements ExceptionListener
   {
      Destination destination;
      
      XAConnection connection;
      
      XASession xaSession;

      MessageProducer producer;
      
      MessageConsumer consumer;
      
      public JMSClient(Destination destination) throws Exception
      {
         this.destination = destination;
         connection = connectionFactory.createXAConnection();
         connection.setClientID("JMSClient");
         connection.setExceptionListener(this);
      }
      
      public void onException(JMSException e)
      {
         log.warn("onException", e);
         close();
      }
      
      public void sendMessage(String text) throws Exception
      {
         Message message = getSession().createTextMessage(text);
         getProducer().send(message);
      }
      
      public void sendMessage(int i) throws Exception
      {
         Message message = getSession().createObjectMessage(new Integer(i));
         getProducer().send(message);
      }
      
      public MessageProducer getProducer() throws Exception
      {
         if (producer == null)
            producer = getSession().createProducer(destination);
         return producer;
      }

      public Message receiveNoWait() throws Exception
      {
         return getConsumer().receiveNoWait();
      }
      
      public MessageConsumer getConsumer() throws Exception
      {
         if (consumer == null)
         {
            connection.start();
            if (destination instanceof Topic)
               consumer = getSession().createDurableSubscriber((Topic) destination, SUBSCRIPTION);
            else
               consumer = getSession().createConsumer(destination);
         }
         return consumer;
      }
      
      public void removeSubscription() throws Exception
      {
         getSession().unsubscribe(SUBSCRIPTION);
      }
      
      public void enlist(Xid xid) throws Exception
      {
         getXAResource().start(xid, XAResource.TMNOFLAGS);
      }
      
      public void delist(Xid xid) throws Exception
      {
         getXAResource().end(xid, XAResource.TMSUCCESS);
      }
      
      public int prepare(Xid xid) throws Exception
      {
         return getXAResource().prepare(xid);
      }
      
      public void commit(Xid xid) throws Exception
      {
         getXAResource().commit(xid, false);
      }
      
      public void rollback(Xid xid) throws Exception
      {
         getXAResource().rollback(xid);
      }
      
      public void forget(Xid xid) throws Exception
      {
         getXAResource().forget(xid);
      }
      
      public List recover() throws Exception
      {
         return Arrays.asList(getXAResource().recover(XAResource.TMSTARTRSCAN));
      }
      
      public XAResource getXAResource() throws Exception
      {
         return getXASession().getXAResource();
      }
      
      public Session getSession() throws Exception
      {
         return getXASession().getSession();
      }
      
      public XASession getXASession() throws Exception
      {
         if (xaSession == null)
            xaSession = connection.createXASession();
         return xaSession;
      }
      
      public void close()
      {
         try
         {
            if (connection != null)
               connection.close();
         }
         catch (Throwable ignored)
         {
         }
         connection = null;
      }
   }

   public static class MyXid implements Xid
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

   protected void assertXidEquals(Xid xid1, Xid xid2)
   {
      if (xid1 == null)
         fail("Null xid1");
      if (xid2 == null)
         fail("Null xid2");
      
      assertEquals("Different format id " + xid1.getFormatId() + " " + xid2.getFormatId(), xid1.getFormatId(), xid2.getFormatId());
      assertTrue("Different global id " + xid1.getGlobalTransactionId() + " " + xid2.getGlobalTransactionId(), Arrays.equals(xid1.getGlobalTransactionId(), xid2.getGlobalTransactionId()));
      if (xid1.getBranchQualifier() == null)
      {
         if (xid1.getBranchQualifier() == null)
            return;
         else
            fail("Different branch null " + xid2.getBranchQualifier());
      }
      assertTrue("Different branch " + xid1.getBranchQualifier() + " " + xid2.getBranchQualifier(), Arrays.equals(xid1.getBranchQualifier(), xid2.getBranchQualifier()));
   }
   
   protected void reset(Destination destination)
   {
      try
      {
         JMSClient client = new JMSClient(destination);
         try
         {
            List xids = client.recover();
            log.debug("Recovering: " + xids);
            for (int i = 0; i < xids.size(); ++i)
               client.rollback((Xid) xids.get(i));
         }
         finally
         {
            client.close();
         }
      }
      catch (Exception e)
      {
         log.warn("Error resetting", e);
      }
      clearDestination(destination);
   }
   
   protected int clearDestination(Destination destination)
   {
      int count = 0;
      try
      {
         JMSClient client = new JMSClient(destination);
         try
         {
            Message message = client.receiveNoWait();
            while (message != null)
            {
               ++count;
               message = client.receiveNoWait();
            }
         }
         finally
         {
            client.close();
         }
      }
      catch (Exception e)
      {
         log.warn("Error clearing " + destination, e);
      }
      return count;
   }
   
   protected void setup(Destination destination, int count) throws Exception
   {
      JMSClient client = new JMSClient(destination);
      try
      {
         for (int i = 0; i < count; ++i)
            client.sendMessage(i);
      }
      finally
      {
         client.close();
      }
   }

   protected void makeSubscription(Topic topic) throws Exception
   {
      JMSClient client = new JMSClient(topic);
      try
      {
         client.getConsumer();
      }
      finally
      {
         client.close();
      }
   }
   
   protected void removeSubscription(Topic topic) throws Exception
   {
      JMSClient client = new JMSClient(topic);
      try
      {
         client.removeSubscription();
      }
      finally
      {
         client.close();
      }
   }

   protected void deployRecoveryService() throws Exception
   {
      deploy(RECOVERY_CONFIG);
      try
      {
         InitialContext ctx = getInitialContext();
         connectionFactory = (XAConnectionFactory) ctx.lookup(FACTORY);
      }
      catch (Throwable t)
      {
         try
         {
            undeploy(RECOVERY_CONFIG);
         }
         catch (Throwable ignored)
         {
         }
      }
   }
   
   protected void undeployRecoveryService() throws Exception
   {
      undeploy(RECOVERY_CONFIG);
   }
   
   protected void restartRecoveryService() throws Exception
   {
      undeployRecoveryService();
      deployRecoveryService();
   }
}
