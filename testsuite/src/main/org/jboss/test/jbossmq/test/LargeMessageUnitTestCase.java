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

import java.util.Properties;

import javax.jms.BytesMessage;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

import org.jboss.mq.SpyConnectionFactory;
import org.jboss.test.JBossTestCase;

/** 
 * A that a large message doesn't get in the way of ping/pong
 *
 * @author Adrian@jboss.org
 * @version $Revision: 57211 $
 */
public class LargeMessageUnitTestCase extends JBossTestCase implements ExceptionListener
{

   private Exception failed = null;

   public LargeMessageUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
   }

   public void testUIL2LargeMessage() throws Exception
   {
      Properties props = new Properties();
      props.setProperty(
         org.jboss.mq.il.uil2.UILServerILFactory.SERVER_IL_FACTORY_KEY,
         org.jboss.mq.il.uil2.UILServerILFactory.SERVER_IL_FACTORY);
      props.setProperty(
         org.jboss.mq.il.uil2.UILServerILFactory.CLIENT_IL_SERVICE_KEY,
         org.jboss.mq.il.uil2.UILServerILFactory.CLIENT_IL_SERVICE);
      props.setProperty(org.jboss.mq.il.uil2.UILServerILFactory.PING_PERIOD_KEY, "60000");
      props.setProperty(org.jboss.mq.il.uil2.UILServerILFactory.UIL_ADDRESS_KEY, getServerHost());
      props.setProperty(org.jboss.mq.il.uil2.UILServerILFactory.UIL_PORT_KEY, "8093");
      props.setProperty(org.jboss.mq.il.uil2.UILServerILFactory.UIL_BUFFERSIZE_KEY, "1");
      props.setProperty(org.jboss.mq.il.uil2.UILServerILFactory.UIL_CHUNKSIZE_KEY, "10000");

      runTest(props);
   }

   public void runTest(Properties props) throws Exception
   {
      QueueConnectionFactory cf = new SpyConnectionFactory(props);
      QueueConnection c = cf.createQueueConnection();
      TemporaryQueue queue = null;
      try
      {
         failed = null;
         c.setExceptionListener(this);
         c.start();
         QueueSession session = c.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
         queue = session.createTemporaryQueue();
         QueueSender sender = session.createSender(queue);
         char[] chars = new char[1000000];
         for (int i = 0; i < chars.length - 1; ++i)
            chars[i] = 'a';
         chars[chars.length - 1] = 0;
         String string = new String(chars);
         TextMessage message = session.createTextMessage(string);
         sender.send(message);

         QueueReceiver receiver = session.createReceiver(queue);
         assertTrue("No message?", receiver.receiveNoWait() != null);

         BytesMessage bytesMessage = session.createBytesMessage();
         bytesMessage.writeUTF(string);
         sender.send(bytesMessage);

         assertTrue("No message?", receiver.receiveNoWait() != null);

         assertTrue("We should not get a ping exception because it should pong every chunk: " + failed, failed == null);
      }
      finally
      {
         c.close();
      }
   }

   public void onException(JMSException e)
   {
      failed = e;
   }

   public static void main(java.lang.String[] args)
   {
      junit.textui.TestRunner.run(LargeMessageUnitTestCase.class);
   }
}
