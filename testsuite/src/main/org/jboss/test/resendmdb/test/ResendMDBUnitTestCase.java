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
package org.jboss.test.resendmdb.test;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;

/**
 * A fairly complicated test that bounces a message around mdbs
 * both via the DLQ and deliberate resends. 
 *
 * @author <a href="mailto:adrian@jboss.com>Adrian Brock</a>
 * @version <tt>$Revision: 1.4</tt>
 */
public class ResendMDBUnitTestCase extends JBossTestCase
{
   public ResendMDBUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ResendMDBUnitTestCase.class, "testresendmdb.jar");
   }

   public void testResends() throws Exception
   {
      InitialContext ctx = getInitialContext(); 
      Queue a = (Queue) ctx.lookup("queue/A");
      Queue b = (Queue) ctx.lookup("queue/B");
      Queue c = (Queue) ctx.lookup("queue/C");
      Queue d = (Queue) ctx.lookup("queue/D");
      ConnectionFactory cf = (ConnectionFactory) ctx.lookup("ConnectionFactory");
      Connection connection = cf.createConnection();
      try
      {
         Session s = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         connection.start();
         
         // Clear all the queues
         MessageConsumer r = s.createConsumer(a);
         while (r.receiveNoWait() != null);
         r.close();
         r = s.createConsumer(b);
         while (r.receiveNoWait() != null);
         r.close();
         r = s.createConsumer(c);
         while (r.receiveNoWait() != null);
         r.close();
         r = s.createConsumer(d);
         while (r.receiveNoWait() != null);
         r.close();
         
         MessageProducer p = s.createProducer(a);
         Message m = s.createTextMessage("Test");
         p.send(m);
         
         r = s.createConsumer(d);
         m = r.receive(20000l);
         if (m == null)
            fail("No message in 20 seconds");
      }
      finally
      {
         connection.close();
      }
   }
}
