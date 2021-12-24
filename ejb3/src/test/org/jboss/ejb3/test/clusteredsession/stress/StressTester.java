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

package org.jboss.ejb3.test.clusteredsession.stress;

import java.io.File;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Random;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;

/**
 * @author Brian Stansberry
 *
 */
public class StressTester
{
   private static boolean useLogger = true;
   
   private static final Random random = new Random();
   
   private TestClient[] clients;
   private int duration;
   private long finishTime;
   private long sleepTime;
   private long startupDelay;
   private boolean stopped;
   private File logFile;
   private PrintWriter logWriter;
   private boolean wroteLog;
   private volatile int requestCount;
   private volatile int threadCount;
   private Logger log;
   
   /**
    * @param args numHeavyClients, heavyClientWeight, numLightClients, duration (secs), sleepTime, startupDelay, discoveryAddress (optional)
    *  
    */
   public static void main(String[] args)
   {
      useLogger = false;
      
      int numHeavyClients = Integer.parseInt(args[0]);
      int heavyClientWeight = Integer.parseInt(args[1]);
      int numLightClients = Integer.parseInt(args[2]);
      int duration = Integer.parseInt(args[3]);
      int sleepTime = Integer.parseInt(args[4]);
      int startupDelay = Integer.parseInt(args[5]);
      String discoveryAddress = null;
      if (args.length > 6)
         discoveryAddress = args[6];
      
      try
      {
         StressTester tester = new StressTester(numHeavyClients, heavyClientWeight, numLightClients, 
                                                duration, sleepTime, 0, startupDelay, discoveryAddress);
         tester.runTest();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   public StressTester(int numHeavyClients, int heavyClientWeight, int numLightClients,
                       int duration, int sleepTime, int newBeanFactor, int startupDelay, String discoveryAddress)
      throws Exception
   {
      if (useLogger)
      {
         log = Logger.getLogger(getClass());
      }
      
      this.duration = duration * 1000;
      this.sleepTime = sleepTime;
      this.startupDelay = startupDelay;
      
      Hashtable<String, String> namingEnv = new Hashtable<String, String>();
      namingEnv.put("java.naming.factory.initial", "org.jboss.naming.NamingContextFactory");
      if (discoveryAddress != null)
      {
         namingEnv.put("jnp.discoveryGroup", discoveryAddress);
      }
      
      clients = new TestClient[numHeavyClients + numLightClients];
      for (int i = 0; i < numHeavyClients; i++)
      {
         clients[i] = new TestClient(String.valueOf(i), heavyClientWeight, newBeanFactor, namingEnv);
      }
      for (int i = 0; i < numLightClients; i++)
      {
         clients[i + numHeavyClients] = new TestClient(String.valueOf(i + numHeavyClients),
                                                       0, newBeanFactor, namingEnv);
      }
      
      if (!useLogger)
      {
         File homeDir = new File(System.getProperty("user.home"));
         if (!homeDir.exists())
            throw new IllegalStateException("No home dir");
         File tmp = new File(homeDir, "ClusterStressTest");
         if (!tmp.exists())
            tmp.mkdir();
         else if (!tmp.isDirectory())
            throw new IllegalStateException(tmp + "is not a directory");
         logFile = File.createTempFile("StressTester", ".log", tmp);
         logWriter = new PrintWriter(logFile);
      }
   }
   
   public void runTest()
   {
      long started = System.currentTimeMillis();
      finishTime = started + duration;
      
      stopped = false;
      
      for (int i = 0; i < clients.length; i++)
      {
         clients[i].start();
         
         if (startupDelay > 0)
            sleep(startupDelay);
      }
      
      long lastCheck = started;
      int lastCount = 0;
      while (lastCheck < finishTime)
      {
         long sleepTime = Math.min(15000, finishTime - lastCheck);
         sleep(sleepTime);
         long now = System.currentTimeMillis();
         int count = requestCount;
         long totElapsed = (now - started) / 1000;
         long lastElapsed = (now - lastCheck) / 1000;
         lastCheck = now;
         int recentCount = count - lastCount;
         lastCheck = now;
         lastCount = count;
         
         String msg = recentCount / lastElapsed + " / sec in last " + lastElapsed + " seconds; " +
                      count / totElapsed + " / sec overall; " + threadCount + " active threads";
         
         if (useLogger)
         {
            log.info(msg);
         }
         else
         {
            System.out.println(msg);
         }
      }

      int survivors = threadCount;
      
      stop();
      
      String msg = "Executed " + requestCount + " requests. " + 
                   survivors + " survivors completed all requests. Saw " +
                   getExceptionCount() + " exceptions";
      
      if (useLogger)
      {
         log.info(msg);
      }
      else
      {
         System.out.println(msg);
      }
      
      if (!wroteLog)
      {
         logFile.delete();
         if (logFile.exists())
            logFile.deleteOnExit();
      }
   }
   
   public void stop()
   {      
      stopped = true;
      
      for (int i = 0; i < clients.length; i++)
      {
         clients[i].stop();
      }
      
   }
   
   public int getActiveClientCount()
   {
      return threadCount;
   }
   
   public int getExceptionCount()
   {
      int count = 0;
      for (int i = 0; i < clients.length; i++)
      {
         if (clients[i].getException() != null)
         {
            count++;
         }
      }
      return count;
   }
   
   private synchronized void log(String message, Throwable t)
   {
      if (!useLogger)
      {
         logWriter.println(message);
         wroteLog = true;
      }
      else
      {
         if (t == null)
            log.error(message);
         else
            log.error(message, t);
      }
   }
   
   private void sleep(long millis)
   {
      try
      {
         Thread.sleep(millis);         
      }
      catch (InterruptedException ignored)
      {   
         if (!stopped)
            log(Thread.currentThread().getName() + " -- Interrupted during test", null);
      }
   }
   
   class TestClient implements Runnable
   {      
      private String id;
      private int payloadSize;
      private int newBeanFactor;
      private Hashtable namingEnv;
      private Exception exception;
      private Thread thread;
      
      TestClient(String id, int payloadSize, int newBeanFactor, Hashtable namingEnv)
      {
         this.id = id;
         this.payloadSize = payloadSize;
         this.namingEnv = namingEnv;
      }
      
      public void run()
      {
         threadCount++;
         try
         {
            ReplicationStressCounter bean = createBean();
            
            long now = System.currentTimeMillis();
            long lastReset = now;
            int expectedCount = 0;
            while (!stopped && (now < finishTime))
            {
               int count = bean.incrementCounter();
               requestCount++;
               if (count != ++expectedCount)
               {
                  throw new IllegalStateException("Incorrect count -- expected " + 
                        expectedCount + " but got " + count);
               }
               
               if (sleepTime > 0)
               {
                  sleep(sleepTime);
               }
               
               now = System.currentTimeMillis();
               if (now - lastReset > (120 * 1000) || 
                     (newBeanFactor > 0 && random.nextInt(newBeanFactor) == 0))
               {
                  // Start a new session
                  bean = createBean();
                  lastReset = now;
                  expectedCount = 0;
               }
            }
         }
         catch (Exception e)
         {
            log("Client " + id + " caught exception -- " +e.toString(), e);
            exception = e;
         }
         finally
         {
            threadCount--;
         }
      }
      
      private ReplicationStressCounter createBean() throws NamingException
      {
         Context ctx = new InitialContext(namingEnv);
         ReplicationStressCounter bean = 
            (ReplicationStressCounter) ctx.lookup("ReplicationStressCounterBean/remote");
         bean.setPayloadSize(payloadSize);
         return bean;
      }
      
      public void start()
      {
         thread = new Thread(this);
         thread.setDaemon(true);
         thread.start();
      }
      
      public void stop()
      {
         stopped = true;
         
         if (thread != null)
         {
            if (thread.isAlive())
            {
               try
               {
                  thread.join(100);
               }
               catch (InterruptedException ignored)
               {
               }
            }
            
            if (thread.isAlive())
               thread.interrupt();
         }
      }
      
      public Exception getException()
      {
         return exception;
      }
   }

}
