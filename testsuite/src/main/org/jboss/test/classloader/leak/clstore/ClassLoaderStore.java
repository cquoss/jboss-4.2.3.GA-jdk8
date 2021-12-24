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
package org.jboss.test.classloader.leak.clstore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;

public class ClassLoaderStore
{
   private static final Logger log = Logger.getLogger(ClassLoaderStore.class);
   
   private static ClassLoaderStore instance = new ClassLoaderStore();
   
   private final Map classloaders = new HashMap();
   private ClassLoaderStore()
   {
      
   }
   
   public static ClassLoaderStore getInstance()
   {
      return instance;
   }
   
   public void storeClassLoader(String key, ClassLoader loader)
   {
      log.debug("Storing " + loader + " under " + key);
      ClassLoader parent = loader.getParent();
      while (parent != null)
      {
         log.debug("Parent is " + parent);
         parent = parent.getParent();
      }
      WeakReference ref = new WeakReference(loader);
      classloaders.put(key, ref);
   }
   
   public ClassLoader getClassLoader(String key, boolean forceGC, String reportFile)
   {
      ClassLoader result = null;
      WeakReference ref = (WeakReference) classloaders.get(key);
      if (ref != null)
      {
         result = (ClassLoader) ref.get();
         if (result != null && forceGC)
         {
            try
            {
               result = null; // Don't hold a ref to it here while analyzing heap
               result = getClassLoader(ref, reportFile);
            }
            catch (Exception e)
            {
               log.error("Caught exception checking for classloader release", e);
            }
         }
      }
      
      return result;
   }
   

   /**
    * If you started your class with -agentlib:jbossAgent in case of leakage (if className still loaded) a file (reportFile) will be created, and a heapSnapshot(./snapshot,mem)
    * 
    * @param weakReferenceOnLoader A weakReference to the created ClassLoader. If there is no references to this classLoader this reference will be cleared
    * @param className The class name supposed to be unloade.
    * @param reportHTMLFile the report file 
    * @throws Exception
    */
   private ClassLoader getClassLoader(WeakReference weakReferenceOnLoader, String reportHTMLFile) throws Exception
   {
      LeakAnalyzer leakAnalyzer = null;
      try
      {
         leakAnalyzer = new LeakAnalyzer();
      }
      catch (Throwable t)
      {
         log.debug("Could not instantiate JVMTIInterface:" + t.getLocalizedMessage());
      }
      
      if (leakAnalyzer != null && leakAnalyzer.isActive())
      {
         leakAnalyzer.forceGC();
         
         if (weakReferenceOnLoader.get() == null)
         {
            return null;
         }
         
         fillMemory(weakReferenceOnLoader);
         
         if (weakReferenceOnLoader.get() == null)
         {
            return null;
         }
         
         leakAnalyzer.heapSnapshot("snapshot", "mem");
         
         if (weakReferenceOnLoader.get() == null)
         {
            return null;
         }
            
         HashMap datapoints = leakAnalyzer.createIndexMatrix();
         
         if (weakReferenceOnLoader.get() == null)
         {
            return null;
         }
         
         String report = leakAnalyzer.exploreObjectReferences(datapoints, weakReferenceOnLoader.get(), 18, true, false);
         log.info(report);
         if (reportHTMLFile != null)
         {
            File outputfile = new File(reportHTMLFile);
            FileOutputStream outfile = new FileOutputStream(outputfile);
            PrintStream realoutput = new PrintStream(outfile);
            realoutput.println(report);
            realoutput.close();
         }

         leakAnalyzer.forceGC();
      }
      else
      {
         log.debug("JVMTI not active; using System.gc()");
         System.gc();
         Thread.sleep(1000);
         
         if (weakReferenceOnLoader.get() != null)
            fillMemory(weakReferenceOnLoader);
         
         if (weakReferenceOnLoader.get() != null)
            fillMemory(weakReferenceOnLoader);
      }
      
      return (ClassLoader) weakReferenceOnLoader.get();
   }
   
   private void fillMemory(WeakReference ref)
   {
      Runtime rt = Runtime.getRuntime();
      int[] adds = { 0, 10, 20, 30, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49 };
      for (int i = 0; i < adds.length; i++) 
      {
          int toAdd = adds[i];
          System.gc();
          
          if (ref.get() == null)
             break;
          
          // create garbage, filling a larger and larger % of
          // free memory on each loop
          long avail = rt.freeMemory();
          int create = (int) (avail / 1000 * (950 + toAdd));
          String pct = (95 + (toAdd/10)) + "." + (toAdd - ((toAdd/10) * 10));
          int bucket = create / 1000;
          log.info("Filling " + pct + "% of free memory. Free memory=" + avail + 
                   " Total Memory=" + rt.totalMemory() + " Max Memory=" + rt.maxMemory());
          
          try
          {
             byte[][] bytez =  new byte[1000][];
             for (int j = 0; j < bytez.length; j++)
                bytez[j] = new byte[bucket];
          }
          catch (Throwable t)
          {
             System.gc();
             break;
          }
      }
      
      try
      {
         ByteArrayOutputStream byteout = new ByteArrayOutputStream();
         ObjectOutputStream out = new ObjectOutputStream(byteout);
        
         out.writeObject(new Dummy());
        
         ByteArrayInputStream byteInput = new ByteArrayInputStream(byteout.toByteArray());
         ObjectInputStream input = new ObjectInputStream(byteInput);
         input.readObject();
        
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      
      if (ref.get() != null)
         System.gc();
   }
   
   public void removeClassLoader(String key)
   {
      classloaders.remove(key);
   }
   
   /** Used just to serialize anything and release SoftCache on java Serialization */
   private static class Dummy implements Serializable
   {
        private static final long serialVersionUID = 1L;
   }
}
