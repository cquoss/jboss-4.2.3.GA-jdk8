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
package org.jboss.ejb3.test.enventry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:carlo@nerdnet.nl">Carlo de Wolf</a>
 * @version <tt>$Revision: 60233 $</tt>
 */
@MessageDriven(name="TestEnvEntryMD",
   activationConfig = {
      @ActivationConfigProperty(
         propertyName = "destinationType", 
         propertyValue = "javax.jms.Queue"),
      @ActivationConfigProperty(
         propertyName = "destination", 
         propertyValue = "queue/testEnvEntry") })
public class TestEnvEntryMDBean implements MessageListener
{
   private static final Logger log = Logger.getLogger(TestEnvEntryMDBean.class);
   
   @Resource(mappedName="java:/JmsXA")
   private ConnectionFactory connectionFactory;

   @Resource(name="maxExceptions") private int maxExceptions = 4;
   
   @Resource private int numExceptions = 3;
   
   private int minExceptions = 1;
   
   public void onMessage(Message msg) {
      try {
         Destination destination = msg.getJMSReplyTo();
         Connection conn = connectionFactory.createConnection();
         try {
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer replyProducer = session.createProducer(destination);
            MapMessage replyMsg = session.createMapMessage();
            replyMsg.setJMSCorrelationID(msg.getJMSMessageID());

            replyMsg.setInt("maxExceptions", maxExceptions);
            replyMsg.setInt("numExceptions", numExceptions);
            replyMsg.setInt("minExceptions", minExceptions);

//          System.err.println("reply to: " + destination);
//          System.err.println("maxExceptions: " + maxExceptions);
//          System.err.println("numExceptions: " + numExceptions);
//          System.err.println("minExceptions: " + minExceptions);

            replyProducer.send(replyMsg);
         }
         finally {
            conn.close();
         }
      }
      catch(JMSException e) {
         throw new RuntimeException(e);
      }
   }
}
