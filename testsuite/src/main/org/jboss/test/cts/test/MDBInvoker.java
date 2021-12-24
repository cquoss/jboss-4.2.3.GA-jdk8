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
package org.jboss.test.cts.test;

import javax.jms.QueueSession;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.TextMessage;
import javax.jms.QueueReceiver;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import EDU.oswego.cs.dl.util.concurrent.CountDown;
import org.apache.log4j.Category;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57211 $
 */
public class MDBInvoker extends Thread
{
   Category log;
   QueueSession session;
   Queue queueA;
   Queue queueB;
   int id;
   CountDown done;
   Exception runEx;

   public MDBInvoker(QueueSession session, Queue queueA, Queue queueB, int id,
         CountDown done, Category log)
   {
      super("MDBInvoker#"+id);
      this.session = session;
      this.queueA = queueA;
      this.queueB = queueB;
      this.id = id;
      this.done = done;
      this.log = log;
   }
   public void run()
   {
      log.info("Begin run, this="+this);
      try
      {
         QueueSender sender = session.createSender(queueA);
         TextMessage message = session.createTextMessage();
         message.setText(this.toString());
         sender.send(message);
         QueueReceiver receiver = session.createReceiver(queueB);
         log.info("Begin receive");
         Message reply = receiver.receive(10000);
         log.info("reply="+reply);
         if( reply == null )
            runEx = new IllegalStateException("Message receive timeout");
         else if( reply instanceof ObjectMessage )
         {
            ObjectMessage om = (ObjectMessage) reply;
            runEx = (Exception) om.getObject();
         }
         sender.close();
         receiver.close();
      }
      catch(Exception e)
      {
         runEx = e;
      }
      done.release();
      System.out.println("End run, this="+this);
   }

}
