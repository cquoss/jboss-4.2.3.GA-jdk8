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
package org.jboss.test.jbossmq.stress;

import junit.framework.TestSuite;
import junit.framework.Assert;

import org.jboss.test.jbossmq.MQBase;

/**
 * Test queue recover.
 *
 * @author <a href="mailto:pra@tim.se">Peter Antman</a>
 * @version $Revision: 57211 $
 */

public class QueueTest extends MQBase
   {

   public QueueTest(String name)
   {
      super(name);
   }

   /**
    * This test is done it two parts to be able to take down the server in
    * between
    */
   public void runQueueSubscriberPartOne() throws Exception
   {
      try
      {
         // Clean testarea up
         drainQueue();

         int ic = getIterationCount();
      
         // Set up a durable subscriber
         IntRangeMessageFilter f1 = new IntRangeMessageFilter(javax.jms.Message.class,
            "QUEUE_NR",
            0,
            ic / 2);

         QueueWorker sub1 = new QueueWorker(SUBSCRIBER,
            TRANS_NONE,
            f1);
         Thread t1 = new Thread(sub1);
         t1.start();

         // Publish 
         IntRangeMessageCreator c1 = new IntRangeMessageCreator("QUEUE_NR",
            0);
         QueueWorker pub1 = new QueueWorker(PUBLISHER,
            TRANS_NONE,
            c1,
            ic / 2);
         pub1.connect();
         pub1.publish();

         Assert.assertEquals("Publisher did not publish correct number of messages " + pub1.getMessageHandled(),
            ic / 2,
            pub1.getMessageHandled());
      
         // let sub1 have some time to handle the messages.
         log.debug("Sleeping for " + ((ic * 10) / 60000) + " minutes");
         // let sub1 have some time to handle the messages.
         sleep(ic * 10);

         Assert.assertEquals("Subscriber did not get correct number of messages " + sub1.getMessageHandled(),
            ic / 2,
            sub1.getMessageHandled());
      
         // Take down first sub
         sub1.close();
         t1.interrupt();
      
         //Publish some more
         pub1.publish(ic / 2);
         Assert.assertEquals("Publisher did not publish correct number of messages " + pub1.getMessageHandled(), ic,
            pub1.getMessageHandled());

         pub1.close();
      }
      catch (Throwable t)
      {
         log.error("Error in test: " + t, t);
         throw new Exception(t.getMessage());
      }
   }

   /**
    * Part two of durable subscriber test, part one should be run before
    * this is run.
    */
   public void runQueueSubscriberPartTwo() throws Exception
   {
      try
      {
         int ic = getIterationCount();
         // Set up a durable subscriber
         IntRangeMessageFilter f1 = new IntRangeMessageFilter(javax.jms.Message.class,
            "QUEUE_NR",
            0,
            ic / 2);

         QueueWorker sub1 = new QueueWorker(SUBSCRIBER,
            TRANS_NONE,
            f1);

         // Start up subscription again
         Thread t2 = new Thread(sub1);
         t2.start();

         log.debug("Sleeping for " + ((ic * 10) / 60000) + " minutes");
         sleep(ic * 10);
         Assert.assertEquals("Subscriber did not get correct number of messages " + sub1.getMessageHandled(), ic / 2,
            sub1.getMessageHandled());

         //OK, take everything down
         sub1.close();
         t2.interrupt();

      }
      catch (Throwable t)
      {
         log.error("Error in test: " + t, t);
         throw new Exception(t.getMessage());
      }
   }

   /**
    * Test queue without taking the server down.
    */
   public void testQueueSubscriber() throws Exception
   {
      runQueueSubscriberPartOne();
      runQueueSubscriberPartTwo();
   }

   public static junit.framework.Test suite() throws Exception
   {

      TestSuite suite = new TestSuite();
      suite.addTest(new QueueSubOne("testQueueSubscriber"));

      return suite;
   }

   public static void main(String[] args)
   {

   }

} // QueueTest
