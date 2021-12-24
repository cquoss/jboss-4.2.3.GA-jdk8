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
package org.jboss.test.jms.client;

import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import org.jboss.jms.client.ImplementationDelegate;
import org.jboss.jms.client.JBossConnectionFactory;
import org.jboss.jms.client.jvm.JVMImplementation;
import org.jboss.jms.destination.JBossQueue;
import org.jboss.jms.server.MessageBroker;
import org.jboss.jms.server.standard.StandardMessageBroker;
import org.jboss.test.jms.BaseJMSTest;

/**
 * A basic test
 * 
 * @author <a href="adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class BasicTestCase extends BaseJMSTest
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public BasicTestCase(String name)
   {
      super(name);
   }

   // Public --------------------------------------------------------

   public void testSomething()
      throws Exception
   {
      Queue queue = new JBossQueue("queue");
      MessageBroker broker = new StandardMessageBroker();
      ImplementationDelegate impl = new JVMImplementation(broker);
      ConnectionFactory cf = new JBossConnectionFactory(impl);
      Connection c = cf.createConnection();
      try
      {
         Session s = c.createSession(true, 0);
         MessageProducer p = s.createProducer(queue);
         Message m = s.createMessage();
         p.send(m);
         p.send(m);
         QueueBrowser b = s.createBrowser(queue);
         Enumeration e = b.getEnumeration();
         while (e.hasMoreElements())
            System.out.println(e.nextElement());
      }
      finally
      {
         c.close();
      }
   }

   // Protected ------------------------------------------------------

   // Package Private ------------------------------------------------

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------
}
