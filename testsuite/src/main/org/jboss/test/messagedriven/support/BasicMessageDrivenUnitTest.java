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
package org.jboss.test.messagedriven.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.naming.Util;
import org.jboss.test.JBossTestCase;
import org.jboss.test.messagedriven.mbeans.TestMessageDrivenManagementMBean;

/**
 * Basic tests of message driven beans 
 *
 * @author <a href="mailto:adrian@jboss.com>Adrian Brock</a>
 * @version <tt>$Revision: 1.4</tt>
 */
public abstract class BasicMessageDrivenUnitTest extends JBossTestCase implements ExceptionListener
{
   protected static final long WAIT_TIME = 5000L;
   protected static final long REPEATED_WAIT = 4;

   protected static final ObjectName testQueue = ObjectNameFactory.create("jboss.mq.destination:service=Queue,name=testQueue");
   protected static final Properties testQueueProps = new Properties();
   
   protected static final ObjectName testTopic = ObjectNameFactory.create("jboss.mq.destination:service=Topic,name=testTopic");
   protected static final Properties testTopicProps = new Properties();
   
   protected static final ObjectName testDurableTopic = ObjectNameFactory.create("jboss.mq.destination:service=Topic,name=testDurableTopic");
   protected static final Properties testDurableTopicProps = new Properties();
   
   static
   {
      testQueueProps.put("destination", "queue/testQueue");
      testQueueProps.put("destinationType", "javax.jms.Queue");

      testTopicProps.put("destination", "topic/testTopic");
      testTopicProps.put("destinationType", "javax.jms.Topic");

      testDurableTopicProps.put("destination", "topic/testDurableTopic");
      testDurableTopicProps.put("destinationType", "javax.jms.Topic");
      //testDurableTopicProps.put("clientID", "DurableSubscriberExample");
      testDurableTopicProps.put("durability", "Durable");
      testDurableTopicProps.put("subscriptionName", "messagedriven");
      testDurableTopicProps.put("user", "john");
      testDurableTopicProps.put("password", "needle");
   }
   
   protected Thread thread;
   protected boolean running = false;

   protected String mdbjar = "testmessagedriven.jar"; 
   protected String mbeansar = "testmessagedriven.sar"; 

   protected ObjectName jmxDestination = ObjectNameFactory.create("does:not=exist"); 
   protected ObjectName dlqJMXDestination = ObjectNameFactory.create("jboss.mq.destination:service=Queue,name=DLQ");
   protected String connectionFactoryJNDI = "ConnectionFactory";
   protected Destination destination;
   protected Destination dlqDestination;
   protected Properties defaultProps;
   protected Properties props;

   protected Connection connection;
   protected Session session;
   protected HashMap producers = new HashMap();
   protected ArrayList messages = new ArrayList(); 

   public BasicMessageDrivenUnitTest(String name, ObjectName jmxDestination, Properties defaultProps)
   {
      super(name);
      this.jmxDestination = jmxDestination;
      this.defaultProps = defaultProps;
   }
   
   public void runTest(Operation[] ops, Properties props) throws Exception
   {
      startTest(props);
      try
      {
         for (int i = 0; i < ops.length; ++i)
            ops[i].run();
      }
      finally
      {
         stopTest();
      }
   }

   public String getMDBDeployment()
   {
      return mdbjar;
   }
   
   public ObjectName getJMXDestination()
   {
      return jmxDestination;
   }
   
   public ObjectName getDLQJMXDestination()
   {
      return dlqJMXDestination;
   }
   
   public Destination getDestination() throws Exception
   {
      if (destination != null)
         return destination;
      String jndiName = (String) getAttribute(getJMXDestination(), "JNDIName");
      destination = (Destination) lookup(jndiName, Destination.class);
      return destination;
   }
   
   public Destination getDLQDestination() throws Exception
   {
      if (dlqDestination != null)
         return dlqDestination;
      String jndiName = (String) getAttribute(getDLQJMXDestination(), "JNDIName");
      dlqDestination = (Destination) lookup(jndiName, Destination.class);
      return dlqDestination;
   }
   
   public MessageProducer getMessageProducer() throws Exception
   {
      return getMessageProducer(getDestination());
   }
   
   public MessageProducer getMessageProducer(Destination destination) throws Exception
   {
      MessageProducer producer = (MessageProducer) producers.get(destination);
      if (producer == null)
         producer = getSession().createProducer(destination);
      return producer;
   }

   public Session getSession() throws Exception
   {
      if (session != null)
         return session;
      
      return getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
   }
   
   public Connection getConnection() throws Exception
   {
      if (connection != null)
         return connection;
      
      ConnectionFactory factory = (ConnectionFactory) lookup(connectionFactoryJNDI, ConnectionFactory.class);
      connection = factory.createConnection();
      connection.setExceptionListener(this);
      return connection;
   }
   
   public Connection getConnection(String user, String password) throws Exception
   {
      if (connection != null)
         return connection;

      ConnectionFactory factory = (ConnectionFactory) lookup(connectionFactoryJNDI, ConnectionFactory.class);
      connection = factory.createConnection(user, password);
      connection.setExceptionListener(this);
      return connection;
   }
   
   public void onException(JMSException e)
   {
      log.debug("Notified of error", e);
      Connection temp = connection;
      connection = null;
      try
      {
         if (temp != null)
            temp.close();
      }
      catch (JMSException ignored)
      {
         log.debug("Ignored ", ignored);
      }
   }
   
   public Message getTestMessage() throws Exception
   {
      return getSession().createMessage();
   }

   protected void setUp() throws Exception
   {
      if ("testServerFound".equals(getName()))
         return;
      deploy(mbeansar);
   }

   protected void tearDown() throws Exception
   {
      if ("testServerFound".equals(getName()))
         return;
      try
      {
         undeploy(mbeansar);
      }
      catch (Throwable t)
      {
         getLog().error("Error undeploying: " + mbeansar, t);
      }
   }
   
   protected void startTest(Properties props) throws Exception
   {
      this.props = props;
      clearMessages(getJMXDestination());
      clearMessages(getDLQJMXDestination());
      tidyup(props);
      initProperties(props);
      deploy(getMDBDeployment());
      try
      {
         // FIXME Need to wait for asynchrounous bootstrap of container
         Thread.sleep(5000);
         startReceiverThread();
      }
      catch (Exception e)
      {
         undeploy(getMDBDeployment());
         throw e;
      }
   }

   protected void stopTest()
   {
      if (connection != null)
      {
         try
         {
            connection.close();
         }
         catch (Exception ignored)
         {
         }
         connection = null;
      }
      stopReceiverThread();
      try
      {
         undeploy(getMDBDeployment());
      }
      catch (Throwable t)
      {
         getLog().error("Error undeploying: " + getMDBDeployment(), t);
      }
      try
      {
         clearMessages(getJMXDestination());
         tidyup(props);
      }
      catch (Throwable t)
      {
         getLog().error("Error clearing messages: " + getJMXDestination(), t);
      }
      try
      {
         clearMessages(getDLQJMXDestination());
      }
      catch (Throwable t)
      {
         getLog().error("Error clearing messages: " + getDLQJMXDestination(), t);
      }
   }
   
   protected void clearMessages(ObjectName name) throws Exception
   {
      if (name != null)
      {
         getLog().info("Clearing messages " + name);
         getServer().invoke(name, "removeAllMessages", new Object[0], new String[0]);
      }
   }
   
   protected void tidyup(Properties props) throws Exception
   {
      String name = props.getProperty("subscriptionName");
      if (name != null)
      {
         String user = props.getProperty("user");
         if (user != null)
         {
            String password = props.getProperty("password");
            getConnection(user, password);
         }
         else
            getConnection();
         try
         {
            Session session = getSession();
            try
            {
               session.unsubscribe(name);
            }
            catch (Throwable t)
            {
               log.debug("Unsubscribe failed: ", t);
            }
         }
         finally
         {
            try
            {
               connection.close();
            }
            catch (Exception ignored)
            {
            }
            connection = null;
         }
      }
   }
   
   protected void activate(ObjectName name) throws Exception
   {
      getServer().invoke(name, "startDelivery", new Object[0], new String[0]);
   }
   
   protected void deactivate(ObjectName name) throws Exception
   {
      getServer().invoke(name, "stopDelivery", new Object[0], new String[0]);
   }
   
   protected void start(ObjectName name) throws Exception
   {
      getServer().invoke(name, "create", new Object[0], new String[0]);
      getServer().invoke(name, "start", new Object[0], new String[0]);
   }
   
   protected void stop(ObjectName name) throws Exception
   {
      getServer().invoke(name, "stop", new Object[0], new String[0]);
      getServer().invoke(name, "destroy", new Object[0], new String[0]);
   }
   
   protected void initProperties(Properties props) throws Exception
   {
      getLog().info("Init properties " + props);
      getServer().invoke(TestMessageDrivenManagementMBean.OBJECT_NAME, "initProperties", new Object[] { props }, new String[] { Properties.class.getName() });
   }
   
   protected void waitMessages(int expected, long wait) throws Exception
   {
      synchronized (this)
      {
         if (wait != 0)
            wait(wait);
         
         for (int i = 0; i < REPEATED_WAIT && messages.size() < expected; ++i)
            wait(WAIT_TIME);
      }
   }
   
   protected ArrayList getMessages() throws Exception
   {
      synchronized (this)
      {
         return new ArrayList(messages);
      }
   }
   
   protected void startReceiverThread()
   {
      synchronized (this)
      {
         thread = new Thread(new ReceiverRunnable(), getClass().getName());
         thread.start();
         running = true;
      }
   }
   
   protected void stopReceiverThread()
   {
      synchronized (this)
      {
         running = false;
         while (thread != null)
         {
            try
            {
               this.notifyAll();
               this.wait();
            }
            catch (Throwable t)
            {
               getLog().error("Error waiting for receiver thread to stop " + thread, t);
            }
         }
      }
   }

   protected Object getAttribute(ObjectName name, String attribute) throws Exception
   {
      return getServer().getAttribute(name, attribute);
   }
   
   protected Object lookup(String jndiName, Class clazz) throws Exception
   {
      return Util.lookup(getInitialContext(), jndiName, clazz);
   }
   
   public class ReceiverRunnable implements Runnable
   {
      public void run()
      {
         try
         {
            while (true)
            {
               ArrayList result = (ArrayList) getAttribute(TestMessageDrivenManagementMBean.OBJECT_NAME, "Messages");
               synchronized (BasicMessageDrivenUnitTest.this)
               {
                  if (running == false)
                     break;
                  if (result.size() > 0)
                  {
                     messages.addAll(result);
                     BasicMessageDrivenUnitTest.this.notifyAll();
                  }
                  BasicMessageDrivenUnitTest.this.wait(WAIT_TIME);
               }
            }
         }
         catch (Throwable t)
         {
            getLog().error("Error in receiver thread " + thread, t);
         }
         
         synchronized (BasicMessageDrivenUnitTest.this)
         {
            thread = null;
            BasicMessageDrivenUnitTest.this.notifyAll();
         }
      }
   }
}
