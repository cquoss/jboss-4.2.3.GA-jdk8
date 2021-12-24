/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.ServerSessionPool;

import org.jboss.mq.SpyConnection;
import org.jboss.mq.SpyQueue;
import org.jboss.test.jbossmq.support.DummyJBossMQTest;
import org.jboss.test.jbossmq.support.DummyServerSessionPool;

/**
 * Test an error in the connection consumer fires
 * the asynch failure, i.e. exception listener
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class ConnectionConsumerErrorFiredUnitTestCase extends DummyJBossMQTest
{
   public ConnectionConsumerErrorFiredUnitTestCase(String name)
   {
      super(name);
   }

   public void testExceptionListenerFiredOnError() throws Exception
   {
      SpyConnection connection = createConnection();
      try
      {
         ServerSessionPool pool = new DummyServerSessionPool(connection);
         setThrowError(connection, "receive");
         connection.createConnectionConsumer(new SpyQueue("QUEUE.testQueue"), null, pool, 1);
         connection.start();
         
         JMSException e = getException();
         assertNotNull("ExceptionListener should have been invoked", e);
         checkThrowable(IOException.class, e.getLinkedException());
      }
      finally
      {
         connection.close();
      }
   }
}
