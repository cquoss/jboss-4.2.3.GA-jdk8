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
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.test.JBossTestCase;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;

/**
 * A test to make sure exception listeners are fired when a service is restarted
 *
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version <tt>$Revision: 57211 $</tt>
 */
public abstract class AbstractRestartDisconnectClientsTest extends JBossTestCase implements ExceptionListener
{
   static String FACTORY = "ConnectionFactory";
   
   private SynchronizedBoolean exceptionListenerFired = new SynchronizedBoolean(false); 
   
   public AbstractRestartDisconnectClientsTest(String name) throws Exception
   {
      super(name);
   }

   protected abstract ObjectName getRestartObjectName();
   
   public synchronized void onException(JMSException exception)
   {
      exceptionListenerFired.set(true);
      getLog().debug("Received notification of error: ", exception);
      notifyAll();
   }

   public void testDisconnect() throws Exception
   {
      ConnectionFactory factory = (ConnectionFactory) getInitialContext().lookup(FACTORY);
      Connection connection = factory.createConnection();
      try
      {
         connection.setExceptionListener(this);
         connection.start();
         MBeanServerConnection server = getServer();
         ObjectName objectName = getRestartObjectName();
         server.invoke(objectName, "stop", null, null);
         try
         {
            // Shouldn't really have to wait for the ping timeout, but just in case
            synchronized (this)
            {
               if (exceptionListenerFired.get() == false)
                  wait(20 * 1000);
            }
            assertTrue("Exception Listener should have been invoked", exceptionListenerFired.get());
         }
         finally
         {
            server.invoke(objectName, "start", null, null);
         }
      }
      finally
      {
         try
         {
            connection.close();
         }
         catch (JMSException ignored)
         {
         }
      }
   }
}
