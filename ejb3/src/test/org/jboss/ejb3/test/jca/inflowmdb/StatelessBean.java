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
package org.jboss.ejb3.test.jca.inflowmdb;

import javax.ejb.Stateless;
import javax.ejb.Remote;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

import org.jboss.ejb3.InitialContextFactory;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision: 60233 $
 */
@Stateless
@Remote(StatelessRemote.class)
public class StatelessBean implements StatelessRemote
{
   private static final Logger log = Logger.getLogger(StatelessBean.class);
     
   public void sendMessage() throws Exception
   {
      QueueConnection cnn = null;
      QueueSender sender = null;
      QueueSession session = null;
      
      InitialContext jndiContext = InitialContextFactory.getInitialContext();

      Queue queue = (Queue) jndiContext.lookup("queue/inflowmdbtest");
      QueueConnectionFactory factory = (QueueConnectionFactory) jndiContext.lookup("java:/ConnectionFactory");
      cnn = factory.createQueueConnection();
      session = cnn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

      TextMessage msg = session.createTextMessage("Hello World");

      sender = session.createSender(queue);
      
      sender.send(msg);
   }
}
