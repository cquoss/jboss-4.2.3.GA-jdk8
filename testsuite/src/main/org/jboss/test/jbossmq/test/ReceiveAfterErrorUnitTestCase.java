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

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import junit.framework.Test;

import org.jboss.mq.SpyDestination;
import org.jboss.test.jbossmq.JBossMQMicrocontainerTest;

/**
 * A test to make sure an error during receive doesn't lead to corrupt "receiving" state
 *
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version <tt>$Revision: 57211 $</tt>
 */
public class ReceiveAfterErrorUnitTestCase extends JBossMQMicrocontainerTest
{
   public ReceiveAfterErrorUnitTestCase(String name) throws Exception
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(ReceiveAfterErrorUnitTestCase.class);
   }
   
   public interface ReceiveOperation
   {
      Message receive(MessageConsumer consumer) throws JMSException;
   }
   
   public void testReceiveAfterError() throws Exception
   {
      receiveAfterError(new ReceiveOperation()
      {
         public Message receive(MessageConsumer consumer) throws JMSException
         {
            return consumer.receive();
         }
      });
   }
   
   public void testReceiveWithWaitAfterError() throws Exception
   {
      receiveAfterError(new ReceiveOperation()
      {
         public Message receive(MessageConsumer consumer) throws JMSException
         {
            return consumer.receive(1000);
         }
      });
   }
   
   public void testReceiveNoWaitAfterError() throws Exception
   {
      receiveAfterError(new ReceiveOperation()
      {
         public Message receive(MessageConsumer consumer) throws JMSException
         {
            return consumer.receiveNoWait();
         }
      });
   }
   
   protected void receiveAfterError(ReceiveOperation operation) throws Exception
   {
      SpyDestination destination = createQueue("testQueue");
      try
      {
         Connection connection = createConnection();
         try
         {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            connection.start();

            MessageProducer producer = session.createProducer(destination);
            Message message = session.createMessage();
            producer.send(message);
            
            MessageConsumer consumer = session.createConsumer(destination);
            
            // Receive should now throw an error
            raiseReceiveError(true);
            try
            {
               operation.receive(consumer);
               fail("Should not be here!");
            }
            catch (Throwable t)
            {
               checkThrowable(JMSException.class, t);
            }
            raiseReceiveError(false);
            
            message = operation.receive(consumer);
            assertNotNull(message);
         }
         finally
         {
            connection.close();
         }
      }
      finally
      {
         removeDestination(destination);
      }
   }
}

