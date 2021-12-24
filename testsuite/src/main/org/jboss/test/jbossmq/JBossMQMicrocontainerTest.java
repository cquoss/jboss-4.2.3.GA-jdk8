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
package org.jboss.test.jbossmq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;

import org.jboss.mq.SpyDestination;
import org.jboss.mq.SpyQueue;
import org.jboss.mq.server.JMSDestinationManager;
import org.jboss.mq.server.JMSQueue;
import org.jboss.test.AbstractTestDelegate;
import org.jboss.test.jbossmq.support.MockServerFailureInterceptor;
import org.jboss.test.kernel.junit.MicrocontainerTest;

/**
 * JBossMQMicrocontainerTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class JBossMQMicrocontainerTest extends MicrocontainerTest
{
   /**
    * Get the test delegate
    * 
    * @param clazz the test class
    * @return the delegate
    * @throws Exception for any error
    */
   public static AbstractTestDelegate getDelegate(Class clazz) throws Exception
   {
      return new JBossMQMicrocontainerTestDelegate(clazz);
   }
   
   /**
    * Create a new JBossMQMicrocontainer test
    * 
    * @param name the test name
    */
   public JBossMQMicrocontainerTest(String name)
   {
      super(name);
   }
   
   protected ConnectionFactory getConnectionFactory() throws Exception
   {
      return (ConnectionFactory) getBean("ConnectionFactory");
   }
   
   protected Connection createConnection() throws Exception
   {
      return getConnectionFactory().createConnection();
   }
   
   protected JMSDestinationManager getJMSServer() throws Exception
   {
      return (JMSDestinationManager) getBean("JMSServer");
   }
   
   protected SpyQueue createQueue(String name) throws Exception
   {
      SpyQueue queue = new SpyQueue(name);
      JMSDestinationManager server = getJMSServer();
      JMSQueue realQueue = new JMSQueue(queue, null, server, server.getParameters());
      server.addDestination(realQueue);
      return queue;
   }
   
   protected void removeDestination(SpyDestination destination) throws Exception
   {
      JMSDestinationManager server = getJMSServer();
      server.closeDestination(destination);
   }
   
   protected MockServerFailureInterceptor getMockServerFailure() throws Exception
   {
      return (MockServerFailureInterceptor) getBean("MockServerFailure");
   }
   
   protected void raiseReceiveError(boolean value) throws Exception
   {
      getMockServerFailure().raiseReceiveError = value; 
   }
}
