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
package org.jboss.test.classloader.circularity.test;

import java.net.URL;

import org.apache.log4j.Logger;

import org.jboss.mx.loading.UnifiedClassLoader;
import org.jboss.mx.loading.UnifiedClassLoader3;
import org.jboss.mx.loading.UnifiedLoaderRepository3;
import org.jboss.mx.loading.LoadMgr3;
import EDU.oswego.cs.dl.util.concurrent.Semaphore;

/** Deadlock tests of the UnifiedClassLoader3
 * @author Scott.Stark@jboss.org
 * @version $Revision: 60355 $
 */
public class RecursiveCCETests
{
   private static Logger log = Logger.getLogger(RecursiveCCETests.class);
   //private CyclicBarrier ifaceBarrier = new CyclicBarrier(2);

   public RecursiveCCETests()
   {
   }

   /** The scenario is:
    - Thread0 starts to load HARMIServerImpl via UCL0 which has ha.jar and
    contains HARMIServerImpl, HARMIServer and HARMIServerImpl_Stub.
    
    - Thread1 loads HARMIServerImpl_Stub via UCL1 which has none of the
    HARMIServerImpl, HARMIServer and HARMIServerImpl_Stub classes.

    @throws Exception
    */
   public void testRecursiveLoadMT() throws Exception
   {
      log.info("Begin testRecursiveLoadMT");
      UnifiedLoaderRepository3 repository = new UnifiedLoaderRepository3();
      Class thisClass = getClass();
      UnifiedClassLoader thisUCL = (UnifiedClassLoader) thisClass.getClassLoader();
      URL origURL = thisUCL.getOrigURL();
      log.info("Service origURL="+origURL);
      URL j0 = new URL(origURL, "ha.jar");
      log.info("j0 = "+j0);

      Semaphore s0 = new Semaphore(0);
      MyUCL ucl0 = new MyUCL(j0, s0);
      repository.addClassLoader(ucl0);
      Semaphore s1 = new Semaphore(0);
      MyUCL ucl1 = new MyUCL(origURL, s1);
      repository.addClassLoader(ucl1);
      String class0 = "org.jboss.test.classloader.circularity.support.HARMIServerImpl";
      MyThread t0 = new MyThread(ucl0, "testRecursiveLoadMT.T0", class0);
      {
         log.info("Starting T0");
         t0.start();
         log.info("Started T0, waiting on ucl="+System.identityHashCode(ucl0));
         s0.acquire();
         log.info("UCL0 notify received");
      }

      String class1 = "org.jboss.test.classloader.circularity.support.HARMIServerImpl_Stub";
      MyThread t1 = new MyThread(ucl1, "testRecursiveLoadMT.T1", class1);
      {
         log.info("Starting T1");
         t1.start();
         log.info("Started T1, waiting on ucl="+System.identityHashCode(ucl1));
         s1.acquire();
         log.info("UCL1 notify received");
      }

      t1.join(10000);
      if( t1.loadedClass == null || t1.loadedClass.getName().equals(class1) == false )
      {
         String msg = "Thread1 failed to load HARMIServerImpl_Stub, class="+t1.loadedClass;
         log.error(msg, t1.loadEx);
         throw new Exception(msg);
      }
      t0.join(5000);
      if( t0.loadedClass == null || t0.loadedClass.getName().equals(class0) == false )
      {
         String msg = "Thread0 failed to load HARMIServerImpl, class="+t0.loadedClass;
         log.error(msg, t0.loadEx);
         throw new Exception(msg);
      }
      log.info("End testRecursiveLoadMT");
   }

   /** Load org.jboss.test.classloader.circularity.support.pkg0.Derived via
    * UCL0
    */
   static class MyThread extends Thread
   {
      String className;
      Class loadedClass;
      Throwable loadEx;
      UnifiedClassLoader3 ucl;

      MyThread(UnifiedClassLoader3 ucl, String id, String className)
      {
         super(id);
         this.className = className;
         this.ucl = ucl;
      }
      public void run()
      {
         try
         {
            loadedClass = ucl.loadClass(className, false);
         }
         catch(Throwable t)
         {
            loadEx = t;
            log.error("Failed to load: "+className, t);
         }
      }
   }

   public static class MyUCL extends UnifiedClassLoader3
   {
      private static final Logger log = Logger.getLogger(MyUCL.class);
      Semaphore s;
      boolean passedBarriers;

      public MyUCL(URL url, Semaphore s)
      {
         super(url);
         this.s = s;
      }

      /** Override to
       */
      public synchronized Class loadClass(String name, boolean resolve)
         throws ClassNotFoundException
      {
         log.info("loadClass, name="+name);
         boolean acquired = attempt(1);
         if( acquired == false )
            throw new IllegalStateException("Failed to acquire loadClass lock");
         log.info("Acquired loadClass lock");

         MyClassLoadingTask task = null;
         try
         {
            Thread t = Thread.currentThread();
            // Register this thread as owning this UCL
            if( loadLock.holds() == 1 )
               LoadMgr3.registerLoaderThread(this, t);

            s.release();
            log.info("notifyAll, ucl="+System.identityHashCode(this));
            try
            {
               if( name.endsWith("HARMIServer") )
               {
                  t.sleep(5000);
                  log.info("Passed HARMIServer barrier");
               }
            }
            catch(InterruptedException e)
            {
               throw new IllegalStateException("MyUCL failed to enter HARMIServer barrier");
            }

            // Create a class loading task and submit it to the repository
            task = new MyClassLoadingTask(name, this, t);
            /* Process class loading tasks needing this UCL until our task has
               been completed by the thread owning the required UCL(s).
             */
            UnifiedLoaderRepository3 ulr3 = (UnifiedLoaderRepository3) repository;
            if( LoadMgr3.beginLoadTask(task, ulr3) == false )
            {
               while( task.threadTaskCount() != 0 )
               {
                  try
                  {
                     LoadMgr3.nextTask(t, task, ulr3);
                  }
                  catch(InterruptedException e)
                  {
                     // Abort the load or retry?
                     break;
                  }
               }
            }
         }
         finally
         {
            // Unregister as the UCL owner to reschedule any remaining load tasks
            if( loadLock.holds() == 1 )
               LoadMgr3.endLoadTask(task);
            // Notify any threads waiting to use this UCL
            this.release();
            this.notifyAll();
         }

         if( task.loadedClass() == null )
         {
            if( task.loadException() instanceof ClassNotFoundException )
               throw (ClassNotFoundException) task.loadException();
            else if( task.loadException() != null )
            {
               log.info("Unexpected error during load of:"+name, task.loadException());
               String msg = "Unexpected error during load of: "+name
                  + ", msg="+task.loadException().getMessage();
               throw new ClassNotFoundException(msg);
            }
            // Assert that loadedClass is not null
            else
               throw new IllegalStateException("ClassLoadingTask.loadedTask is null, name: "+name);
         }

         return task.loadedClass();
      }
   }
}
