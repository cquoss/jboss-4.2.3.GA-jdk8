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
package org.jboss.test.jbossmq.support;

import java.util.Properties;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import org.jboss.mq.GenericConnectionFactory;
import org.jboss.mq.SpyConnection;
import org.jboss.mq.il.ServerIL;
import org.jboss.mq.il.ServerILFactory;
import org.jboss.test.BaseTestCase;

/**
 * DummyJBossMQTest.<p>
 * 
 * Uses the DummyIL to test client behaviour under
 * more "controlled" conditions
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class DummyJBossMQTest extends BaseTestCase implements ExceptionListener
{
   private JMSException exception;
   
   public DummyJBossMQTest(String name)
   {
      super(name);
   }
   
   protected SpyConnection createConnection() throws Exception
   {
      ServerIL serverIL = new DummyServerIL();
      Properties properties = new Properties();
      properties.put(ServerILFactory.CLIENT_IL_SERVICE_KEY, DummyClientILService.class.getName());
      GenericConnectionFactory gcf = new GenericConnectionFactory(serverIL, properties);
      SpyConnection connection = new SpyConnection(SpyConnection.UNIFIED, gcf);
      connection.setExceptionListener(this);
      return connection;
   }
   
   protected void setThrowError(SpyConnection connection, String when)
   {
      DummyServerIL serverIL = (DummyServerIL) connection.getServerIL();
      serverIL.setThrowError(when);
   }
   
   protected synchronized JMSException getException() throws Exception
   {
      if (exception == null)
         wait(15000);
      return exception;
   }

   public synchronized void onException(JMSException exception)
   {
      this.exception = exception;
      notifyAll();
   }
}
