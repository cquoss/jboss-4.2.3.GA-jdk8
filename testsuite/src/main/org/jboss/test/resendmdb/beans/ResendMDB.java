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
package org.jboss.test.resendmdb.beans;

import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;

/**
 * An MDB that resends a message 
 *
 * @author <a href="mailto:adrian@jboss.com>Adrian Brock</a>
 * @version <tt>$Revision: 1.4</tt>
 */
public class ResendMDB implements MessageDrivenBean, MessageListener
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   protected static final Logger log = Logger.getLogger(ResendMDB.class);
   
   protected MessageDrivenContext ctx;
   
   public void onMessage(Message message)
   {
      log.info("Resending message: " + message);
      try
      {
         InitialContext context = new InitialContext();
         String name = (String) context.lookup("java:comp/env/queueName");
         Queue queue = (Queue) context.lookup(name);
         ConnectionFactory cf = (ConnectionFactory) context.lookup("java:/JmsXA");
         Connection c = cf.createConnection();
         try
         {
            Session s = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer p = s.createProducer(queue);
            p.send(message);
         }
         finally
         {
            c.close();
         }
      }
      catch (Exception e)
      {
         log.error("Error", e);
      }
      
   }
   
   public void ejbCreate()
   {
   }
   
   public void ejbRemove()
   {
   }

   public void setMessageDrivenContext(MessageDrivenContext ctx)
   {
      this.ctx = ctx;
   }
}
