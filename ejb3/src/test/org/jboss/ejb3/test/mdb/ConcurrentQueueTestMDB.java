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
package org.jboss.ejb3.test.mdb;

import java.util.Random;

import javax.ejb.ActivationConfigProperty;

import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.jboss.annotation.ejb.PoolClass;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@MessageDriven(activationConfig =
        {
        @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
        @ActivationConfigProperty(propertyName="destination", propertyValue="queue/concurrentmdbtest"),
        @ActivationConfigProperty(propertyName="maxSession", propertyValue="5")
        })
@PoolClass (value=org.jboss.ejb3.StrictMaxPool.class, maxSize=30, timeout=500)
public class ConcurrentQueueTestMDB implements MessageListener
{
   private static final Logger log = Logger.getLogger(ConcurrentQueueTestMDB.class);
   
   private static Random random = new Random(1234);
   
   public void onMessage(Message message)
   {
      TextMessage txt = (TextMessage)message;
      int test = random.nextInt(10);
      try
      {                 
         System.out.println("--- ConcurrentQueueTestMDB onMessage " + TestStatusBean.concurrentQueueRan + " " + txt.getText() + " " + test);
      
         Thread.sleep(2000);         
      }
      catch (Exception e)
      {
      }
      
      if (test == 1)
         throw new RuntimeException("Testing");
      else
         ++TestStatusBean.concurrentQueueRan;
   }
}
