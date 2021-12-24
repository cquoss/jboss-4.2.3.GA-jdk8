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
package org.jboss.web.tomcat.service.session;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.logging.Logger;

/**
 * A snapshot manager that collects all modified sessions over a given
 * period of time and distributes them en bloc.
 *
 * @author Thomas Peuss <jboss@peuss.de>
 * @author Brian Stansberry
 * @version $Revision: 57206 $
 */
public class IntervalSnapshotManager extends SnapshotManager implements Runnable
{
   static Logger log = Logger.getLogger(IntervalSnapshotManager.class);

   // the interval in ms
   protected int interval = 1000;

   // the modified sessions
   protected Set sessions = new LinkedHashSet();

   // the distribute thread
   protected Thread thread = null;

   // Is session processing allowed?
   protected boolean processingAllowed = false;
   
   // has the thread finished?
   protected boolean threadDone = false;

   public IntervalSnapshotManager(AbstractJBossManager manager, String path)
   {
      super(manager, path);
   }

   public IntervalSnapshotManager(AbstractJBossManager manager, String path, int interval)
   {
      super(manager, path);
      this.interval = interval;
   }

   /**
    * Store the modified session in a hashmap for the distributor thread
    */
   public void snapshot(ClusteredSession session)
   {
      try
      {      
         // Don't hold a ref to the session for a long time
         synchronized (sessions)
         {
            sessions.add(session);
         }
      }
      catch (Exception e)
      {
         log.error("Failed to queue session " + session + " for replication", e);
      }
   }

   /**
    * Distribute all modified sessions
    */
   protected void processSessions()
   {
      ClusteredSession[] toProcess = null;
      synchronized (sessions)
      {
         toProcess = new ClusteredSession[sessions.size()];
         toProcess = (ClusteredSession[]) sessions.toArray(toProcess);
         sessions.clear();
      }

      AbstractJBossManager mgr = getManager();
      for (int i = 0; i < toProcess.length; i++)
      {
         // Confirm we haven't been stopped
         if (!processingAllowed)
            break;
         
         try
         {
            mgr.storeSession(toProcess[i]);
         }
         catch (Exception e)
         {
            getLog().error("Caught exception processing session " + toProcess[i].getRealId(), e);
         }
      }
   }

   /**
    * Start the snapshot manager
    */
   public void start()
   {
      processingAllowed = true;
      startThread();
   }

   /**
    * Stop the snapshot manager
    */
   public void stop()
   {
      processingAllowed = false;
      stopThread();
      synchronized (sessions)
      {
         sessions.clear();
      }
   }

   /**
    * Start the distributor thread
    */
   protected void startThread()
   {
      if (thread != null)
      {
         return;
      }

      thread = new Thread(this, "ClusteredSessionDistributor[" + getContextPath() + "]");
      thread.setDaemon(true);
      thread.setContextClassLoader(getManager().getContainer().getLoader().getClassLoader());
      threadDone = false;
      thread.start();
   }

   /**
    * Stop the distributor thread
    */
   protected void stopThread()
   {
      if (thread == null)
      {
         return;
      }
      threadDone = true;
      thread.interrupt();
      try
      {
         thread.join();
      }
      catch (InterruptedException e)
      {
      }
      thread = null;
   }

   /**
    * Thread-loop
    */
   public void run()
   {
      while (!threadDone)
      {
         try
         {
            Thread.sleep(interval);
            processSessions();
         }
         catch (InterruptedException ie)
         {
            if (!threadDone)
               getLog().error("Caught exception processing sessions", ie);
         }
         catch (Exception e)
         {
            getLog().error("Caught exception processing sessions", e);
         }
      }
   }
}
