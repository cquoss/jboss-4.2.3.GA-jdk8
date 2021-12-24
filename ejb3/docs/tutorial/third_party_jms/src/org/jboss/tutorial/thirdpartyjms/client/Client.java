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
package org.jboss.tutorial.thirdpartyjms.client;

import javax.jms.Destination;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;

import java.util.Hashtable;

public class Client
{
   public static void main(String[] args) throws Exception
   {
      processMessage();
   }
   
   private static void processMessage() throws Exception
   {
      Hashtable properties = new Hashtable();
      properties.put(Context.INITIAL_CONTEXT_FACTORY, 
                     "org.exolab.jms.jndi.InitialContextFactory");
      properties.put(Context.PROVIDER_URL, "tcp://localhost:3035/");
      InitialContext jndiContext = new InitialContext(properties);
      
      ConnectionFactory factory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
      Connection connection = factory.createConnection();
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      
      Destination queue = (Destination) jndiContext.lookup("queue1");
      
      MessageProducer sender = session.createProducer(queue);

      TextMessage msg = session.createTextMessage("Hello World");
      
      sender.send(msg);
      System.out.println("Message sent successfully to remote third party queue ");

   }
}
