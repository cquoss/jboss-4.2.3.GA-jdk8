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
package org.jboss.test.jbossmq.perf;

import java.io.Serializable;
import java.util.HashMap;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.jboss.test.JBossTestCase;
import org.jboss.util.NestedRuntimeException;

/**
 * A stress test for an impatient receiver
 *
 * @author    <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version   $Revision: 57211 $
 */
public class ReceiveNackClientStressTestCase extends JBossTestCase implements ExceptionListener
{
   protected static final ObjectName destinationManager;
   
   protected QueueConnection queueConnection;
   
   static
   {
      try
      {
         destinationManager = new ObjectName("jboss.mq:service=DestinationManager");
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }

   public ReceiveNackClientStressTestCase(String name) throws Exception
   {
      super(name);
   }

   public void onException(JMSException e)
   {
      log.error("Error: ", e);
      try
      {
         queueConnection.close();
      }
      catch (Exception ignored)
      {
      }
   }
   
   public void createQueue(String name) throws Exception
   {
      MBeanServerConnection server = getServer();
      try
      {
         server.invoke(destinationManager, "createQueue", new Object[] { name, name },  new String[] { String.class.getName(), String.class.getName() } );
      }
      catch (Exception ignored)
      {
         log.debug("Ignored", ignored);
      }
      ObjectName queueName = new ObjectName("jboss.mq.destination:service=Queue,name=" + name);
      server.invoke(queueName, "removeAllMessages", null, null);
   }

   public void deleteQueue(String name) throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName queueName = new ObjectName("jboss.mq.destination:service=Queue,name=" + name);
      server.invoke(queueName, "removeAllMessages", null, null);
      server.invoke(destinationManager, "destroyQueue", new Object[] { name },  new String[] { String.class.getName() } );
   }

   public void testImpatient() throws Exception
   {
      int target = getIterationCount();
      createQueue("Impatient");
      try
      {
         InitialContext context = getInitialContext();
         QueueConnectionFactory queueFactory = (QueueConnectionFactory) context.lookup("ConnectionFactory");
         Queue queue = (Queue) context.lookup("Impatient");
         queueConnection = queueFactory.createQueueConnection();
         try
         {
            QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueSender sender = session.createSender(queue);
            QueueReceiver receiver = session.createReceiver(queue);
            Serializable payload = new HashMap();
            Message message = session.createObjectMessage(payload);
            queueConnection.start();
            int count = 0;
            int sendCount = 0;
            while (count < target)
            {
               if (sendCount <= target)
               {
                  for (int i = 0; i < 10 && ++sendCount <= target; ++i)
                     sender.send(message);
               }
               if (receiver.receive(1) != null)
                  ++count;
            }
         }
         finally
         {
            queueConnection.close();
         }
      }
      finally
      {
         deleteQueue("Impatient");
      }
   }
}
