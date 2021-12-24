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
package org.jboss.jms.server.container;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.metadata.SimpleMetaData;
import org.jboss.jms.destination.JBossDestination;
import org.jboss.jms.server.BrowserEndpointFactory;
import org.jboss.jms.server.DeliveryEndpointFactory;
import org.jboss.jms.server.MessageBroker;
import org.jboss.util.id.GUID;

/**
 * The serverside representation of the client
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class Client
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** The Message broker */
   private MessageBroker broker;

   // Static --------------------------------------------------------

   public static Client getClient(Invocation invocation)
   {
      return (Client) invocation.getMetaData("JMS", "Client");
   }

   // Constructors --------------------------------------------------

   public Client(MessageBroker broker)
   {
      this.broker = broker;
   }

   // Public --------------------------------------------------------

   public SimpleMetaData createSession(MethodInvocation invocation)
   {
      return getMetaData();
   }

   public SimpleMetaData createBrowser(MethodInvocation invocation)
   {
      SimpleMetaData result = getMetaData();
      
      JBossDestination destination = (JBossDestination) invocation.getArguments()[0];
      String selector = (String) invocation.getArguments()[1];
      BrowserEndpointFactory endpointFactory = broker.getBrowserEndpointFactory(destination, selector);
      result.addMetaData("JMS", "BrowserEndpointFactory", endpointFactory);
      return result;
   }

   public SimpleMetaData createConsumer(MethodInvocation invocation)
   {
      return getMetaData();
   }

   public SimpleMetaData createProducer(MethodInvocation invocation)
   {
      SimpleMetaData result = getMetaData();
      
      JBossDestination destination = (JBossDestination) invocation.getArguments()[0];
      DeliveryEndpointFactory endpointFactory = broker.getDeliveryEndpointFactory(destination);
      result.addMetaData("JMS", "DeliveryEndpointFactory", endpointFactory);
      return result;
   }

   public SimpleMetaData getMetaData()
   {
      SimpleMetaData result = new SimpleMetaData();
      result.addMetaData("JMS", "Client", this);
      result.addMetaData("JMS", "OID", GUID.asString());
      return result;
   }

   // Protected ------------------------------------------------------
   
   // Package Private ------------------------------------------------

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------

}
