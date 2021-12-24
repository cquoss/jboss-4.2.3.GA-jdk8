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
import javax.security.auth.login.LoginContext;

import org.jboss.mq.SpyConnectionFactory;
import org.jboss.mq.SpyXAConnectionFactory;
import org.jboss.mq.il.http.HTTPServerILFactory;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.AppCallbackHandler;

/**
 * Test all the verious ways that a connection can be
 * established with JBossMQ
 *
 * @author hiram.chirino@jboss.org
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57211 $
 */
public class HTTPConnectionUnitTestCase extends JBossTestCase
{

   public HTTPConnectionUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
   }

   public void testMultipleHTTPConnectViaJNDI() throws Exception
   {

      getLog().debug("Starting testMultipleHTTPConnectViaJNDI");

      InitialContext ctx = new InitialContext();

      QueueConnectionFactory cf = (QueueConnectionFactory) ctx.lookup("HTTPConnectionFactory");

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

      getLog().debug("Finished testMultipleHTTPConnectViaJNDI");
   }

   public void testHTTPConnectViaJNDI() throws Exception
   {
      InitialContext ctx = new InitialContext();

      QueueConnectionFactory cf = (QueueConnectionFactory) ctx.lookup("HTTPConnectionFactory");
      QueueConnection c = cf.createQueueConnection();
      c.close();

      XAQueueConnectionFactory xacf = (XAQueueConnectionFactory) ctx.lookup("HTTPXAConnectionFactory");
      XAQueueConnection xac = xacf.createXAQueueConnection();
      xac.close();
   }

   public void testHTTPConnectNoJNDI() throws Exception
   {

      Properties props = new Properties();
      props.setProperty(HTTPServerILFactory.SERVER_IL_FACTORY_KEY, HTTPServerILFactory.SERVER_IL_FACTORY);
      props.setProperty(HTTPServerILFactory.CLIENT_IL_SERVICE_KEY, HTTPServerILFactory.CLIENT_IL_SERVICE);
      props.setProperty(HTTPServerILFactory.SERVER_URL_KEY, "http://" + getServerHost() + ":8080/jbossmq-httpil/HTTPServerILServlet");
      props.setProperty(HTTPServerILFactory.PING_PERIOD_KEY, "0");
      props.setProperty(HTTPServerILFactory.TIMEOUT_KEY, "60");
      props.setProperty(HTTPServerILFactory.REST_INTERVAL_KEY, "1");

      QueueConnectionFactory cf = new SpyConnectionFactory(props);
      QueueConnection c = cf.createQueueConnection();
      c.close();

      XAQueueConnectionFactory xacf = new SpyXAConnectionFactory(props);
      XAQueueConnection xac = xacf.createXAQueueConnection();
      xac.close();
   }

   public void testHTTPConnectNoJNDIWithBasicAuthentication() throws Exception
   {

      ////////////////////THIS IS HOW YOU HANDLE SECURITY ////////////////////
      String authConf = super.getResourceURL("security/auth.conf");
      System.setProperty("java.security.auth.login.config", authConf);
      AppCallbackHandler handler = new AppCallbackHandler("guest", "guest".toCharArray());
      LoginContext lc = new LoginContext("other", handler);
      lc.login();
      ////////////////////////////////////////////////////////////////////////
        
      Properties props = new Properties();
      props.setProperty(HTTPServerILFactory.SERVER_IL_FACTORY_KEY, HTTPServerILFactory.SERVER_IL_FACTORY);
      props.setProperty(HTTPServerILFactory.CLIENT_IL_SERVICE_KEY, HTTPServerILFactory.CLIENT_IL_SERVICE);
      props.setProperty(HTTPServerILFactory.SERVER_URL_KEY, "http://" + getServerHost() + ":8080/jbossmq-httpil/restricted/HTTPServerILServlet");
      props.setProperty(HTTPServerILFactory.PING_PERIOD_KEY, "0");
      props.setProperty(HTTPServerILFactory.TIMEOUT_KEY, "60");
      props.setProperty(HTTPServerILFactory.REST_INTERVAL_KEY, "1");

      QueueConnectionFactory cf = new SpyConnectionFactory(props);
      QueueConnection c = cf.createQueueConnection();
      c.close();

      XAQueueConnectionFactory xacf = new SpyXAConnectionFactory(props);
      XAQueueConnection xac = xacf.createXAQueueConnection();
      xac.close();
      lc.logout();    // Log out.
   }

   public static void main(java.lang.String[] args)
   {
      junit.textui.TestRunner.run(HTTPConnectionUnitTestCase.class);
   }
}
