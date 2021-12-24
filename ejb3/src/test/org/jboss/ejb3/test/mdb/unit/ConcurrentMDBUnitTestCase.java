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
package org.jboss.ejb3.test.mdb.unit;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.Test;

import org.jboss.ejb3.InitialContextFactory;
import org.jboss.ejb3.test.mdb.TestStatus;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class ConcurrentMDBUnitTestCase extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(ConcurrentMDBUnitTestCase.class);

   public ConcurrentMDBUnitTestCase(String name)
   {
      super(name);
   }
   

   public void testQueue() throws Exception
   {
      TestStatus status = (TestStatus) getInitialContext().lookup(
            "TestStatusBean/remote");
      clear(status);
      QueueConnection cnn = null;
      QueueSender sender = null;
      QueueSession session = null;

      Queue queue = (Queue) getInitialContext().lookup("queue/concurrentmdbtest");
      QueueConnectionFactory factory = getQueueConnectionFactory();
      cnn = factory.createQueueConnection();
      session = cnn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

      TextMessage msg ;
      int numMessages = 100;
      sender = session.createSender(queue);
      for (int i = 1 ; i <= numMessages ; ++i)
      {
         msg = session.createTextMessage("Hello World " + i);
         sender.send(msg);
      }
      
      session.close();
      cnn.close();

      Thread.sleep(60 * 1000);
      assertEquals(numMessages, status.concurrentQueueFired());
   }

   protected QueueConnectionFactory getQueueConnectionFactory()
         throws Exception
   {
      try
      {
         return (QueueConnectionFactory) getInitialContext().lookup(
               "ConnectionFactory");
      } catch (NamingException e)
      {
         return (QueueConnectionFactory) getInitialContext().lookup(
               "java:/ConnectionFactory");
      }
   }

   protected void clear(TestStatus status)
   {
      status.clear();
      assertEquals(0, status.bmtQueueRan());
      assertEquals(0, status.defaultedQueueFired());
      assertEquals(0, status.messageCount());
      assertEquals(0, status.nondurableQueueFired());
      assertEquals(0, status.overrideDefaultedQueueFired());
      assertEquals(0, status.overrideQueueFired());
      assertEquals(0, status.queueFired());
      assertEquals(0, status.topicFired());
      assertEquals(0, status.expirationQueueRan());
      assertFalse(status.interceptedQueue());
      assertFalse(status.interceptedTopic());
      assertFalse(status.postConstruct());
      assertFalse(status.preDestroy());
   }
   
   protected InitialContext getInitialContext() throws Exception
   {
      return InitialContextFactory.getInitialContext();
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ConcurrentMDBUnitTestCase.class,
            "mdbtest-service.xml, mdb-test.jar");
   }

}