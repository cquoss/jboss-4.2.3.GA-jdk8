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
package org.jboss.test.classloader.test;

import java.net.URL;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import org.jboss.logging.Logger;
import org.jboss.mx.loading.UnifiedLoaderRepository3;
import org.jboss.mx.loading.UnifiedClassLoader3;
import org.jboss.mx.loading.LoaderRepository;
import org.jboss.mx.loading.LoadMgr3;
import org.jboss.test.classloader.circularity.test.MyClassLoadingTask;
import org.jboss.test.util.ClassMover;


/** Additional deadlock scenario tests of the UnifiedClassLoader3

     .....................................
     :   +-------+           +-------+   :
     :   | TestA |           | TestA2|   :
     :   +---+---+           +---+---+   :
     '''''''''|'''''''''''''''''''|'''''''
              |      extends      |
          ''''|''''''             |
    ......|.........|.............|.........
    : +--+----+  +--+----+     +--+----+   :
    : | TestB |  | TestB3|     | TestB2|   :
    : +-------+  +-------+     +-------+   :
    :......................................:
 
 @author Frank.Gamerdinger@Sun.COM
 @author Scott.Stark@jboss.org
 @version $Revision: 57211 $
 */
public class DeadlockTests32
{
   private static Logger log = Logger.getLogger(DeadlockTests32.class);
   private SyncEvent syncEvent = new SyncEvent();
   private TestClassLoader loaders[];
   private UnifiedLoaderRepository3 mainRep;
   private TestClassLoader blockingLoader;
   private String blockname;

   /**
      [starksm@banshee testsuite]$ jar -tf output/lib/dl-a.jar
      org/jboss/test/classloader/test/abc/TestA.class
      org/jboss/test/classloader/test/abc/TestA2.class
      [starksm@banshee testsuite]$ jar -tf output/lib/dl-b.jar
      org/jboss/test/classloader/test/abc/TestB.class
      org/jboss/test/classloader/test/abc/TestB2.class
      org/jboss/test/classloader/test/abc/TestB3.class
    @param libDir
    @throws Exception
    */ 
   public DeadlockTests32(File libDir) throws Exception
   {
      ClassLoader appCl = this.getClass().getClassLoader();
      URL u1 = new File(libDir, "dl-a.jar").toURL();
      URL u2 = new File(libDir, "dl-b.jar").toURL();
      loaders = new TestClassLoader[2];
      mainRep = new UnifiedLoaderRepository3();
      loaders[0] = new TestClassLoader(u1, appCl, mainRep);
      loaders[1] = new TestClassLoader(u2, appCl, mainRep);
   }

   /** Scenario:

    Thread T1                               Thread T2
    classLoader 1 [a.jar]                   classLoader 2   [b.jar]
    
             .                                     .
    t0 ------------------------------------------------------------
             |                                     | loadClass(abc.TestB)
             |                                     |----+
             |                                     +----+ registered,
             |                                     |
             |                                     |----+
             |                                     +----+ wait in beforeTaskLoop
    t1 ------------------------------------------------------------
             | load abc.TestB2                     |
             |       schedule abc.TestB2 --------->|
             |                                     |
             |-----+                               |
             |     | nextTask(WAIT_ON_EVENT)       | 
             +-----+                               |
    t2 ------------------------------------------------------------
             |                                     | <--- release beforeTaskLoop lock
    t3 ------------------------------------------------------------
             |                                     | loadClassLocally(abc.TestB2)
             |                                     |
             |                                     |
             |<+---------- schedule abc.TestA -----|
             |                                     |-----+ nextTask
             |                                     |     |
             |                                     +-----+
             |                                     |
             |                                     |   loadClassLocally
             |<+---------- schedule abc.TestA2-----|   abc.TestB
             |                                     |
             |                                     |-----+
             |                                     |     | nextTask
             |                                     +-----+ wait
             |                                     |
    t4 -------------------- Dead-Lock ----------------------------
    @throws Exception
    */ 
   public void testDeadLock() throws Exception
   {
      log.info("RUNNING: testDeadLock");
      File TestAclass = ClassMover.move("org.jboss.test.classloader.test.abc.TestA");
      File TestBclass = ClassMover.move("org.jboss.test.classloader.test.abc.TestB");
      File TestB2class = ClassMover.move("org.jboss.test.classloader.test.abc.TestB2");

      /* Indicate that CL2 will wait after acquring the UCL registration monitor
      and that the class its loading is TestB
      */
      blockingLoader = loaders[1];
      blockname = "org.jboss.test.classloader.test.abc.TestB";
      ClassLoader cl1 = loaders[0];
      ClassLoader cl2 = loaders[1];
      Runner t2 = new Runner("T2", blockname, cl2);
      ThreadList threads = new ThreadList();
      threads.add(t2);

      log.info("t0, Waiting for T2 to start");
      t2.start();      
      Thread.sleep(2000);
      log.info("t1, T2 should be waiting in beforeTaskLoop");

      /* Load B2 through T1/CL1 which requires CL2 held by T2
      */
      Runner t1 = new Runner("T1", "org.jboss.test.classloader.test.abc.TestB2", cl1);
      threads.add(t1);
      log.info("t2, Waiting for T1 to start");
      t1.start();
      Thread.sleep(4000);
      log.info("t3, Now releasing T2/CL2");
      syncEvent.set();
      log.info("t4, Waiting for T1, T2 to complete");
      threads.waitAll();

      ClassMover.restore(TestAclass);
      ClassMover.restore(TestBclass);
      ClassMover.restore(TestB2class);
   }

   /** Scenario:

    Thread T1                               Thread T2
    classLoader 1 [a.jar]                   classLoader 2   [b.jar]
    
             .                                     .
    t0 ------------------------------------------------------------
             |                                     | loadClass(abc.TestB)
             |                                     |----+
             |                                     +----+ registered,
             |                                     |
             |                                     |----+
             |                                     +----+ wait in beforeTaskLoop
    t1 ------------------------------------------------------------
             | load abc.TestB3                     |
             |       schedule abc.TestB3 --------->|
             |                                     |
             |-----+                               |
             |     | nextTask(WAIT_ON_EVENT)       | 
             +-----+                               |
    t2 ------------------------------------------------------------
             |                                     | <--- release beforeTaskLoop lock
    t3 ------------------------------------------------------------
             |                                     | loadClassLocally(abc.TestB3)
             |                                     |
             |                                     |
             |<+---------- schedule abc.TestA -----|
             | loadClassLocally(abc.TestA)         |
             |-----+                               |
             |     | nextTask(WAIT_ON_EVENT)       | 
             +-----+                               |
             |                                     | loadClassLocally(abc.TestB)
             |                                     | CCE(abc.TestA)
             |                                     |
             |                                     |-----+
             |                                     |     | nextTask
             |                                     +-----+ wait(WAIT_ON_EVENT)
             |                                     |
    t4 -------------------- Dead-Lock ----------------------------
    @throws Exception
    */ 
   public void testDeadLockAndCircularity() throws Exception
   {
      log.info("RUNNING: testDeadLockAndCircularity");
      File TestAclass = ClassMover.move("org.jboss.test.classloader.test.abc.TestA");
      File TestBclass = ClassMover.move("org.jboss.test.classloader.test.abc.TestB");
      File TestB3class = ClassMover.move("org.jboss.test.classloader.test.abc.TestB3");

      /* Indicate that CL2 will wait after acquring the UCL registration monitor
      and that the class its loading is TestB
      */
      blockingLoader = loaders[1];
      blockname = "org.jboss.test.classloader.test.abc.TestB";
      ClassLoader cl1 = loaders[0];
      ClassLoader cl2 = loaders[1];
      Runner t2 = new Runner("T2", blockname, cl2);
      ThreadList threads = new ThreadList();
      threads.add(t2);
      // Start t2 so that it blocks
      log.info("t0, Waiting for T2 to start");
      t2.start();
      Thread.sleep(2000);
      log.info("t1, T2 should be waiting in beforeTaskLoop");

      Runner t1 = new Runner("T1", "org.jboss.test.classloader.test.abc.TestB3", cl1);
      threads.add(t1);
      log.info("t2, Waiting for T1 to start");
      t1.start();
      Thread.sleep(4000);
      log.info("t3, Now releasing T2/CL2");
      syncEvent.set();
      threads.waitAll();

      ClassMover.restore(TestAclass);
      ClassMover.restore(TestBclass);
      ClassMover.restore(TestB3class);
   }


   private class Runner extends Thread
   {
      private String clsname;
      private ClassLoader loader;

      public Runner(String name, String clsname, ClassLoader cl)
      {
         super(name);
         this.clsname = clsname;
         this.loader = cl;
      }

      private Throwable savedEx;

      public Throwable getThrowable()
      {
         return savedEx;
      }

      public void showException()
      {
         if (savedEx != null)
         {
            log.error("Exception for: " + getName(), savedEx);
         }
      }

      public void run()
      {
         try
         {
            Class cls = Class.forName(clsname, true, loader);
            cls.newInstance();
         }
         catch (Throwable ex)
         {
            savedEx = ex;
         }
      }

   }


   public void beforeTaskLoop(UnifiedClassLoader3 cl, String clsname)
   {
      log.info("BeforeTaskLoop: " + clsname);
      if (cl == blockingLoader && clsname.equals(blockname))
      {
         log.info("Waiting in beforeTaskLoop: " + cl + " on " + blockname);
         try
         {
            syncEvent.aquire();
         }
         catch (InterruptedException e)
         {
            log.error("Interrupted", e);
         }
         log.info("End beforeTaskLoop: " + cl);
      }
   }

   public class TestClassLoader extends UnifiedClassLoader3
   {
      public TestClassLoader(URL url, ClassLoader parent,
         LoaderRepository repository)
      {
         super(url, null, parent, null);
         repository.addClassLoader(this);
      }

      public synchronized Class loadClassImpl(String name, boolean resolve)
         throws ClassNotFoundException
      {
         boolean trace = log.isTraceEnabled();

         /* Since loadClass can be called from loadClassInternal with the monitor
            already held, we need to determine if there is a ClassLoadingTask
            which requires this UCL. If there is, we release the UCL monitor
            so that the ClassLoadingTask can use the UCL.
          */
         boolean acquired = attempt(1);
         while( acquired == false )
         {
            /* Another thread needs this UCL to load a class so release the
             monitor acquired by the synchronized method. We loop until
             we can acquire the class loading lock.
            */
           try
            {
               if( trace )
                  log.trace("Waiting for loadClass lock");
               this.wait();
            }
            catch(InterruptedException ignore)
            {
            }
            acquired = attempt(1);
         }

         MyClassLoadingTask task = null;
         try
         {
            Thread t = Thread.currentThread();
            // Register this thread as owning this UCL
            if( loadLock.holds() == 1 )
               LoadMgr3.registerLoaderThread(this, t);
            // Callout to cause blocking with UCL monitor held
            beforeTaskLoop(name);

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
               if( log.isTraceEnabled() )
                  log.trace("Unexpected error during load of:"+name, task.loadException());
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

      protected void beforeTaskLoop(String clsname)
      {
         log.info("Calling taskLoop");
         DeadlockTests32.this.beforeTaskLoop(this, clsname);
      }

   }

   private class ThreadList
   {
      List list = new ArrayList();

      public ThreadList()
      {
      }


      public void add(Thread t)
      {
         list.add(t);
      }

      public void waitAll() throws Exception
      {
         Iterator it = list.iterator();
         Throwable ex = null;
         StringBuffer buf = new StringBuffer();
         while (it.hasNext())
         {
            Runner t = (Runner) it.next();
            t.join();
            //t.showException();
            Throwable e = t.getThrowable();
            if (e != null)
            {
               buf.append("Failure for: ").append(t.getName());
               buf.append('\n').append(e);
               if (ex == null)
               {
                  ex = e;
               }
            }
         }
         if (ex != null)
         {
            log.info(buf.toString());
            if (ex instanceof Exception)
            {
               throw (Exception) ex;
            }
            else if (ex instanceof Error)
            {
               throw (Error) ex;
            }
         }
      }

   }

   private class SyncEvent
   {
      private boolean cond;

      public SyncEvent()
      {
         this(false);
      }

      public SyncEvent(boolean initialValue)
      {
         cond = initialValue;
      }


      public void aquire() throws InterruptedException
      {
         synchronized (this)
         {
            while (!cond)
            {
               wait();
            }
         }
      }

      public synchronized void set()
      {
         cond = true;
         notifyAll();
      }

      public synchronized void unset()
      {
         cond = false;
      }

   }
}
