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
package org.jboss.mx.util;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * A runnable scheduler.<p>
 * 
 * The scheduler needs to be started to do real work. To add work to the
 * scheduler, create a SchedulableRunnable and set the scheduler. When
 * the next run has passed the work is performed.
 * 
 * @see SchedulableRunnable
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 */
public class RunnableScheduler
   implements Runnable
{
   // Attributes ----------------------------------------------------

   /**
    * The runnables to schedule
    */
   private TreeSet runnables = new TreeSet();

   /**
    * The thread pool used to process the runnables.
    */
   private ThreadPool threadPool;

   /**
    * The controller thread.
    */
   private Thread controller = null;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Constructs a new runnable scheduler.
    */
   public RunnableScheduler()
   {
   }

   /**
    * Start the scheduler
    */
   public synchronized void start()
   {
      //log.debug("start");
      if (controller != null)
         return;
      controller = new Thread(this);
      controller.setDaemon(true);
      controller.start();
   }

   /**
    * Stop the scheduler
    */
   public synchronized void stop()
   {
      //log.debug("stop");
      if (controller == null)
         return;
      controller.interrupt();
      controller = null;
   }

   /**
    * Run the scheduler
    */
   public void run()
   {
      // Start the threadpool
      threadPool = new ThreadPool();
      threadPool.setActive(true);
      try
      {
         // Do outstanding work until stopped
         while (true)
         {
            try
            {
               runOutstanding();
               waitOutstanding();
            }
            catch (InterruptedException weAreDone)
            {
               //log.debug("interupted");
               break;
            }
         }
      }
      finally
      {
         // Stop the thread pool
         threadPool.setActive(false);
         threadPool = null;
      }
   }

   // Public --------------------------------------------------------

   // X Implementation ----------------------------------------------

   // Y Overrides ---------------------------------------------------

   // Protected -----------------------------------------------------

   // Package -------------------------------------------------------

   /**
    * Add a schedulable runnable
    *
    * @param runnable the runnable to add
    */
   synchronized void add(SchedulableRunnable runnable)
   {
      runnables.add(runnable);
      notifyAll();
   }

   /**
    * Remove a schedulable runnable
    *
    * @param runnable the runnable to add
    */
   synchronized void remove(SchedulableRunnable runnable)
   {
      runnables.remove(runnable);
   }

   /**
    * Check whether the scheduler contains a runnable
    *
    * @param runnable the runnable to check
    * @return true when the runnable is present, false otherwise
    */
   synchronized boolean contains(SchedulableRunnable runnable)
   {
      return runnables.contains(runnable);
   }

   // Private -------------------------------------------------------

   /**
    * Run all outstanding runnables, they are in date order
    */
   private synchronized void runOutstanding()
   {
      long current = System.currentTimeMillis();
      Iterator iterator = runnables.iterator();
      while (iterator.hasNext())
      {
         SchedulableRunnable next = (SchedulableRunnable) iterator.next();
         if (next.getNextRun() <= current)
         {
            //log.debug("runOutstanding: " + next);
            iterator.remove();
            threadPool.run(next);
         }
         else
         {
            //log.debug("runOutstanding: break");
            break;
         }
      }
   }

   /**
    * Wait for the next outstanding runnable
    */
   private synchronized void waitOutstanding()
      throws InterruptedException
   {
      // There is nothing to run
      if (runnables.size() == 0)
      {
         //log.debug("waitOutstanding_1");
         wait();
         //log.debug("waitOutstanding_1 - wakeup");
      }
      else
      {
         // Wait until the next runnable
         SchedulableRunnable next = (SchedulableRunnable) runnables.first();
         long current = System.currentTimeMillis();
         long wait = next.getNextRun() - current;
         //log.debug("waitOutstanding_2 until: " + new Date(current + wait));
         if (wait > 0)
            wait(wait);
         //log.debug("waitOutstanding_2 - wakeup");
      }
   }

   // Inner Classes -------------------------------------------------
}

