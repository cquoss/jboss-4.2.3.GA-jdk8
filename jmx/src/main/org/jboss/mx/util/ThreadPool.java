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

import java.util.Stack;

/**
 * A simple thread pool. Idle threads are cached for future use.
 * The cache grows until it reaches a maximum size (default 10).
 * When there is nothing in the cache, a new thread is created.
 * By default the threads are daemon threads.
 *
 * <a href="mailto:rickard.oberg@telkel.com">Rickard \u00d6berg</a>
 * <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 */
public class ThreadPool
{
  // Constants -----------------------------------------------------

  // Attributes ----------------------------------------------------
  private static int counter = 0;

  /**
   * Stack of idle threads cached for future use.
   */
  private Stack pool = new Stack();

  /**
   * Maximum number of idle threads cached in this pool.
   */
  private int maxSize = 10;

  /**
   * Is the thread pool active
   */
  private boolean active = false;

  /**
   * Whether the threads are daemon threads.
   */
  private boolean daemon = true;

  // Static --------------------------------------------------------

  // Constructors --------------------------------------------------

  /**
   * Create a new pool.
   */
  public ThreadPool()
  {
  }

  /**
   * Create a new pool with an activity status
   *
   * @param active true for active, false otherwise
   */
  public ThreadPool(boolean active)
  {
     this.active = active;
  }

  // Public --------------------------------------------------------

  /**
   * Set the maximum number of idle threads cached in this pool.
   *
   * @param size the new maximum size.
   */
  public void setMaximumSize(int size)
  {
    maxSize = size;
  }

  /**
   * Get the maximum number of idle threads cached in this pool.
   *
   * @return the maximum size
   */
  public int getMaximumSize()
  {
    return maxSize;
  }

  /**
   * Set the activity status of the pool. Setting the pool to
   * inactive, clears the pool.
   *
   * @param status pass true for active, false otherwise.
   */
  public void setActive(boolean status)
  {
    active = status;
    if (active == false)
      while (pool.size() > 0)
        ((Worker)pool.pop()).die();
  }

  /**
   * Get the activity status of the pool.
   *
   * @return true for an active pool, false otherwise.
   */
  public boolean isActive()
  {
    return active;
  }

  /**
   * Set whether new threads are daemon threads.
   *
   * @param value pass true for daemon threads, false otherwise.
   */
  public void setDaemonThreads(boolean value)
  {
    daemon = value;
  }

  /**
   * Get whether new threads are daemon threads.
   *
   * @return true for daemon threads, false otherwise.
   */
  public boolean getDaemonThreads()
  {
    return daemon;
  }

  /**
   * Do some work.
   * This will either create a new thread to do the work, or
   * use an existing idle cached thread.
   *
   * @param work the work to perform.
   */
   public synchronized void run(Runnable work)
   {
     if (pool.size() == 0)
       new Worker(work);
     else 
     {
       Worker worker = (Worker) pool.pop();
       worker.run(work);
     }
   }

   // Private -------------------------------------------------------

   /**
    * Put an idle worker thread back in the pool of cached idle threads.
    * This is called from the worker thread itself. When the cache is
    * full, the thread is discarded.
    *
    * @param worker the worker to return.
    */
   private synchronized void returnWorker(Worker worker)
   {
     if (pool.size() < maxSize)
       pool.push(worker);
     else
       worker.die();   
   }

   // Inner classes -------------------------------------------------

  /**
   * A worker thread runs a worker.
   */
  class Worker extends Thread
  {
    /**
     * Flags that this worker may continue to work.
     */
    boolean running = true;

    /**
     * Work to do, of <code>null</code> if no work to do.
     */
    Runnable work;

    /**
    * Create a new Worker to do some work.
    *
    * @param work the work to perform
    */
    Worker(Runnable work)
    {
	   // give it a thread so we can figure out what this thread is in debugging
	   super("ThreadPoolWorker["+(++counter)+"]");
      this.work = work;
      setDaemon(daemon);
      start();
    }

    /**
     * Tell this worker to die.
     */
    public synchronized void die()
    {
      running = false;
      this.notify();
    }

    /**
     * Give this Worker some work to do.
     *
     * @param the work to perform.
     * @throws IllegalStateException If this worker already
     *         has work to do.
     */
    public synchronized void run(Runnable work)
    {
      if (this.work != null)
        throw new IllegalStateException("Worker already has work to do.");
      this.work = work;
      this.notify();
    }

    /**
     * The worker loop.
     */
    public void run()
    {
      while (active && running)
      {
        // If work is available then execute it
        if (work != null)
        {
          try
          {
            work.run();
          }
          catch (Exception ignored) {}

          // Clear work
          work = null;
        }
                
        // Return to pool of cached idle threads
        returnWorker(this);

        // Wait for more work to become available
        synchronized (this)
        {
          while (running && work == null)
          {
            try
            {
              this.wait();
            }
            catch (InterruptedException ignored) {}
          }
        }
      }
    }
  }
}
