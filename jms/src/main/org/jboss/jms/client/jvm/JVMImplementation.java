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
package org.jboss.jms.client.jvm;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.jboss.aop.advice.Interceptor;
import org.jboss.jms.client.ConnectionDelegate;
import org.jboss.jms.client.ImplementationDelegate;
import org.jboss.jms.client.JBossConnectionFactory;
import org.jboss.jms.client.container.ClientContainerFactory;
import org.jboss.jms.client.container.FactoryInterceptor;
import org.jboss.jms.server.MessageBroker;
import org.jboss.jms.server.container.Client;
import org.jboss.jms.server.container.ServerConnectionInterceptor;
import org.jboss.jms.server.container.ServerContainerFactory;
import org.jboss.jms.server.container.ServerFactoryInterceptor;

/**
 * The in jvm implementation
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class JVMImplementation
   implements ImplementationDelegate
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** The message broker */
   private MessageBroker broker;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public JVMImplementation(MessageBroker broker)
   {
      this.broker = broker;
   }

   // Public --------------------------------------------------------

   // ImplementationDelegate implementation -------------------------

   public ConnectionDelegate createConnection(String userName, String password) throws JMSException
   {
      Client client = new Client(broker);
      Interceptor[] serverInterceptors = new Interceptor[]
      {
         ServerFactoryInterceptor.singleton,
         ServerConnectionInterceptor.singleton 
      };
      ConnectionDelegate delegate = ServerContainerFactory.getConnectionContainer(this, serverInterceptors, client.getMetaData()); 

      Interceptor[] clientInterceptors = new Interceptor[]
      {
         FactoryInterceptor.singleton
      };
      return ClientContainerFactory.getConnectionContainer(this, delegate, clientInterceptors, null);
   }

   public Reference getReference() throws NamingException
   {
      return new Reference
      (
         JBossConnectionFactory.class.getName(),
         new StringRefAddr("dummy", "dummy"),
         JVMImplementationFactory.class.getName(),
         null
      );
   }

   // Protected ------------------------------------------------------

   // Package Private ------------------------------------------------

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------

}
