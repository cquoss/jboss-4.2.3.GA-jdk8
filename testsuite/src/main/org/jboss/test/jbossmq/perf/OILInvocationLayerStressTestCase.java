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
package org.jboss.test.jbossmq.perf;

import junit.textui.TestRunner;
import EDU.oswego.cs.dl.util.concurrent.Semaphore;

/**
 * JBossMQPerfStressTestCase.java Some simple tests of JBossMQ
 *
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 * @version   $Revision: 57211 $
 */

public class OILInvocationLayerStressTestCase extends InvocationLayerStressTest
{
   /**
    * Constructor for the JBossMQPerfStressTestCase object
    *
    * @param name           Description of Parameter
    * @exception Exception  Description of Exception
    */
   public OILInvocationLayerStressTestCase(String name) throws Exception
   {
      super(name);
   }


   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testOILMutliSessionOneConnection() throws Exception
   {
      getLog().debug("Starting OIL MutliSessionOneConnection test");

      connect("ConnectionFactory", "ConnectionFactory");
      queueConnection.start();
      exitSemaphore = new Semaphore(-WORKER_COUNT);
      exitSemaphore.release();

      getLog().debug("Creating workers.");
      QueueWorker workers[] = new QueueWorker[WORKER_COUNT];
      for (int i = 0; i < WORKER_COUNT; i++)
      {
         workers[i] = new QueueWorker("ConnectionTestQueue-" + i, "OIL");
      }

      getLog().debug("Starting workers.");
      for (int i = 0; i < WORKER_COUNT; i++)
      {
         workers[i].start();
      }

      getLog().debug("Waiting for workers to finish.");
      exitSemaphore.acquire();

      disconnect();
      getLog().debug("OIL MutliSessionOneConnection passed");
   }

   /**
    * The main entry-point for the JBossMQPerfStressTestCase class
    *
    * @param args  The command line arguments
    */
   public static void main(String[] args)
   {

      String newArgs[] = {"org.jboss.test.jbossmq.perf.OILInvocationLayerStressTestCase"};
      TestRunner.main(newArgs);

   }

}
