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
 * Exception listener tests-
 *
 *
 * @author     <a href="mailto:pra@tim.se">Peter Antman</a>
 * @version $Revision: 57211 $
 */

public class ExceptionListenerTest  extends MQBase{
   
   public ExceptionListenerTest(String name) {
      super(name);
   }
   /*
     To catch the old typ of error, we need a consumer that has not consumed
     anything before the server goes down.

     To govern this we need a publisher method to be used in the tests.

     Big question is: when are we satisfyed with the listeners result???
    */


   public void runListener() throws Exception {
      // Clean testarea up
      drainTopic();

      int ic = getIterationCount();
      long sleep = getRunSleep();
      IntRangeMessageFilter f1 = new IntRangeMessageFilter(javax.jms.Message.class,
                                                           "FAILSAFE_NR",
                                                           0,
                                                           ic);
      
      TopicWorker sub1 = new TopicWorker(FAILSAFE_SUBSCRIBER,
                                         TRANS_NONE,
                                         f1);
      Thread t1 = new Thread(sub1);
      t1.start();
      
      // Now we must wait until JBoss has been restarted before we check
      // messages
      sleep(5*sleep);
      log.info("Awakened from sleep");
      
      Assert.assertEquals("Subscriber did not get correct number of messages "+sub1.getMessageHandled(), ic,
                          sub1.getMessageHandled());
      
      sub1.setStoped();
      t1.interrupt();
      sub1.close();

   }

   public void runPublish() throws Exception {
      int ic = getIterationCount();
      // This does NOT work perfect, since both sends will have base 0
      IntRangeMessageCreator c1 = new IntRangeMessageCreator("FAILSAFE_NR",
                                                             0);
      TopicWorker pub1 = new TopicWorker(PUBLISHER,
                                         TRANS_NONE,
                                         c1,
                                         ic/2);
      pub1.connect();
      pub1.publish();
      
      Assert.assertEquals("Publisher did not publish correct number of messages "+pub1.getMessageHandled(),
                          ic/2,
                          pub1.getMessageHandled());
      
      
      pub1.close();
   }
   public static junit.framework.Test suite() throws Exception{
      
      TestSuite suite= new TestSuite();
      suite.addTest(new  ExceptionListenerTest("runListener"));
      
      //suite.addTest(new DurableSubscriberTest("testBadClient"));
      return suite;
   }
   
   public static void main(String[] args) {
      
   }
   
} // ExceptionListenerTest
