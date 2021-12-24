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

import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.XAQueueConnection;
import javax.jms.XAQueueConnectionFactory;
import javax.naming.InitialContext;

import junit.framework.Test;
import junit.textui.TestRunner;

import org.jboss.mq.SpyConnectionFactory;
import org.jboss.mq.SpyXAConnectionFactory;
import org.jboss.mq.il.uil2.UILServerILFactory;
import org.jboss.test.JBossTestCase;

/** 
 * Test all the verious ways that a connection can be 
 * established with JBossMQ
 *
 * @author hiram.chirino@jboss.org
 * @version $Revision: 57211 $
 */
public class UIL2ConnectionUnitTestCase extends JBossTestCase
{

   public UIL2ConnectionUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
   }

   public void testMultipleUIL2ConnectViaJNDI() throws Exception
   {

      getLog().debug("Starting testMultipleUIL2ConnectViaJNDI");

      InitialContext ctx = new InitialContext();

      QueueConnectionFactory cf = (QueueConnectionFactory) ctx.lookup("UIL2ConnectionFactory");

      QueueConnection connections[] = new QueueConnection[10];

      getLog().debug("Creating " + connections.length + " connections.");
      for (int i = 0; i < connections.length; i++)
      {
         connections[i] = cf.createQueueConnection();
      }

      getLog().debug("Closing the connections.");
      for (int i = 0; i < connections.length; i++)
      {
         connections[i].close();
      }

      getLog().debug("Finished testMultipleUIL2ConnectViaJNDI");
   }

   public void testUIL2ConnectViaJNDI() throws Exception
   {
      InitialContext ctx = new InitialContext();

      QueueConnectionFactory cf = (QueueConnectionFactory) ctx.lookup("UIL2ConnectionFactory");
      QueueConnection c = cf.createQueueConnection();
      c.close();

      XAQueueConnectionFactory xacf = (XAQueueConnectionFactory) ctx.lookup("UIL2XAConnectionFactory");
      XAQueueConnection xac = xacf.createXAQueueConnection();
      xac.close();
   }

   public void testUIL2ConnectNoJNDI() throws Exception
   {

      Properties props = new Properties();
      props.setProperty(org.jboss.mq.il.uil2.UILServerILFactory.SERVER_IL_FACTORY_KEY,
            org.jboss.mq.il.uil2.UILServerILFactory.SERVER_IL_FACTORY);
      props.setProperty(org.jboss.mq.il.uil2.UILServerILFactory.CLIENT_IL_SERVICE_KEY,
            org.jboss.mq.il.uil2.UILServerILFactory.CLIENT_IL_SERVICE);
      props.setProperty(org.jboss.mq.il.uil2.UILServerILFactory.PING_PERIOD_KEY, "1000");
      props.setProperty(org.jboss.mq.il.uil2.UILServerILFactory.UIL_ADDRESS_KEY, getServerHost());
      props.setProperty(org.jboss.mq.il.uil2.UILServerILFactory.UIL_PORT_KEY, "8093");
      props.setProperty(org.jboss.mq.il.uil2.UILServerILFactory.UIL_RECEIVE_REPLIES_KEY, "no");

      QueueConnectionFactory cf = new SpyConnectionFactory(props);
      QueueConnection c = cf.createQueueConnection();
      c.close();

      XAQueueConnectionFactory xacf = new SpyXAConnectionFactory(props);
      XAQueueConnection xac = xacf.createXAQueueConnection();
      xac.close();
   }
   public void testOverriddenUIL2ConnectViaJNDI() throws Exception
   {
      InitialContext ctx = new InitialContext();

      SpyConnectionFactory cf = (SpyConnectionFactory) ctx.lookup("OverriddenUIL2ConnectionFactory");
      Properties props = cf.getProperties();
      String value = props.getProperty(UILServerILFactory.UIL_CONNECTADDRESS_KEY);
      assertEquals("Overridden Address", value);
      value = props.getProperty(UILServerILFactory.UIL_CONNECTPORT_KEY);
      assertEquals("-1000", value);
      
      cf = (SpyConnectionFactory) ctx.lookup("OverriddenUIL2XAConnectionFactory");
      props = cf.getProperties();
      value = props.getProperty(UILServerILFactory.UIL_CONNECTADDRESS_KEY);
      assertEquals("Overridden Address", value);
      value = props.getProperty(UILServerILFactory.UIL_CONNECTPORT_KEY);
      assertEquals("-1000", value);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(UIL2ConnectionUnitTestCase.class, "overridden-uil2-service.xml");
   }

   public static void main(java.lang.String[] args)
   {
      TestRunner.run(UIL2ConnectionUnitTestCase.class);
   }
}
