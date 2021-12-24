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
package org.jboss.test.cluster.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.jboss.ha.framework.interfaces.ClusteringTargetsRepository;
import org.jboss.ha.framework.interfaces.FamilyClusterInfo;

import junit.framework.TestCase;

/**
 * Test of FamilyClusterInfoImpl.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 1.1 $
 */
public class FamilyClusterInfoUnitTestCase extends TestCase
{
   private abstract class FCIChanger extends Thread
   {
      private boolean started;
      private boolean stopped;
      private Throwable caught;
      
      protected FamilyClusterInfo fci;
      
      FCIChanger(FamilyClusterInfo fci)
      {
         this.fci = fci;
      }
      
      abstract void update();
      
      
      public void run()
      {
         try
         {
            started = true;
            
            while (!stopped)
            {
               if (interrupted())
                  throw new InterruptedException();
               
               update();
            }
            
         }
         catch (InterruptedException ie)
         {
            if (!stopped)
               caught = ie;
         }
         catch (Throwable t)
         {
            caught = t;
         }
         finally
         {
            started = false;
         }
      }
      
      void beginUpdates()
      {
         this.start();
         while (!started)
         {
            try
            {
               sleep(10);
            }
            catch (InterruptedException e)
            {
               caught = e;
            }
         }
      }
      
      void endUpdates()
      {
         if (started)
         {
            stopped = true;
            try
            {
               this.join(250);
            }
            catch (InterruptedException ignored) {}
         }
         
         if (started)
            interrupt();
      }
      
      Throwable getThrowable()
      {
         return caught;
      }
   }
   
   private class Updater extends FCIChanger
   {
      private ArrayList targets = new ArrayList();
      private int viewId;
      
      Updater(FamilyClusterInfo fci)
      {
         super(fci);
      }
      
      void update()
      {
         targets.add(new Object());
         viewId++;
         fci.updateClusterInfo((ArrayList) targets.clone(), viewId);         
      }
   }
   
   private class Remover extends FCIChanger
   {
      Remover(FamilyClusterInfo fci)
      {
         super(fci);
      }
      
      void update()
      {
         List targets = fci.getTargets();
         if (targets.size() > 0)
            fci.removeDeadTarget(targets.get(0));
      }
      
   }
   
   private class Resetter extends FCIChanger
   {
      Resetter(FamilyClusterInfo fci)
      {
         super(fci);
      }
      
      void update()
      {
         fci.resetView();
      }      
   }
   
   public void testSynchronization() throws Exception
   {
      ClusteringTargetsRepository.initTarget("testSynchronization", new ArrayList(), 0);
      FamilyClusterInfo fci = ClusteringTargetsRepository.getFamilyClusterInfo("testSynchronization");
      
      Updater updater = new Updater(fci);
      Remover remover = new Remover(fci);
      Resetter resetter = new Resetter(fci);
      
      updater.beginUpdates();
      
      checkFCIConsistency(fci, 150, false);
      
      remover.beginUpdates();
      
      checkFCIConsistency(fci, 150, true);
      
      resetter.beginUpdates();
      
      checkFCIConsistency(fci, 150, true);
      
      updater.endUpdates();
      remover.endUpdates();
      resetter.endUpdates();
      
      assertNull("Updater had no exceptions", updater.getThrowable());
      assertNull("Remover had no exceptions", remover.getThrowable());
      assertNull("Resetter had no exceptions", resetter.getThrowable());
      
   }
   
   public void testTargetListImmutability() throws Exception
   {
      ArrayList targets = new ArrayList();
      targets.add("ONE");
      targets.add("TWO");
      ClusteringTargetsRepository.initTarget("testImmutability", targets, 0);
      FamilyClusterInfo fci = ClusteringTargetsRepository.getFamilyClusterInfo("testImmutability");
      
      ArrayList fciTargets = fci.getTargets();
      
      // Confirm the targets are what we expect
      assertEquals("Expected number of targets", 2, fciTargets.size());
      assertEquals("First target as expected", "ONE", fciTargets.get(0));
      assertEquals("Second target as expected", "TWO", fciTargets.get(1));
      
      try 
      { 
         fciTargets.add("FAIL");
         fail("add call did not fail");
      } 
      catch (UnsupportedOperationException good) {}
      
      try 
      { 
         fciTargets.addAll(targets);
         fail("addAll call did not fail");
      } 
      catch (UnsupportedOperationException good) {}
      
      try 
      { 
         fciTargets.clear();
         fail("clear call did not fail");
      } 
      catch (UnsupportedOperationException good) {}
      
      try 
      { 
         fciTargets.ensureCapacity(5);
         fail("ensureCapacity call did not fail");
      } 
      catch (UnsupportedOperationException good) {}
      
      try 
      { 
         fciTargets.remove(0);
         fail("remove call did not fail");
      } 
      catch (UnsupportedOperationException good) {}
      
      try 
      { 
         fciTargets.set(0, "FAIL");
         fail("set call did not fail");
      } 
      catch (UnsupportedOperationException good) {}
      
      try 
      { 
         fciTargets.trimToSize();
         fail("trimToSize call did not fail");
      } 
      catch (UnsupportedOperationException good) {}
      
      try 
      { 
         fciTargets.removeAll(targets);
         fail("removeAll call did not fail");
      } 
      catch (UnsupportedOperationException good) {}
      
      try 
      { 
         fciTargets.retainAll(targets);
         fail("retainAll call did not fail");
      } 
      catch (UnsupportedOperationException good) {}
      
      Iterator iter = fciTargets.iterator();
      int count = 0;
      while (iter.hasNext())
      {
         iter.next();
         count++;         
      }
      assertEquals("Correct count", 2, count);
      try 
      { 
         iter.remove();
         fail("Iterator.remove call did not fail");
      } 
      catch (UnsupportedOperationException good) {}
      
      ListIterator listIter = fciTargets.listIterator();
      count = 0;
      while (listIter.hasNext())
      {
         listIter.next();
         count++;         
      }
      assertEquals("Correct count", 2, count);
      try 
      { 
         listIter.remove();
         fail("Iterator.remove call did not fail");
      } 
      catch (UnsupportedOperationException good) {}
      
      while (listIter.hasPrevious())
      {
         listIter.previous();
         count--;
      }
      assertEquals("Correct count", 0, count);
      
      listIter = fciTargets.listIterator(1);
      while (listIter.hasNext())
      {
         listIter.next();
         count++;         
      }
      assertEquals("Correct count", 1, count);
      try 
      { 
         listIter.remove();
         fail("Iterator.remove call did not fail");
      } 
      catch (UnsupportedOperationException good) {}
      
      while (listIter.hasPrevious())
      {
         listIter.previous();
         count--;
      }
      assertEquals("Correct count", -1, count);
      
      // Check the lists returned by other methods
      fciTargets = fci.updateClusterInfo(targets, 0);

      try 
      { 
         fciTargets.add("FAIL");
         fail("add call did not fail");
      } 
      catch (UnsupportedOperationException good) {}
      
      fciTargets = fci.removeDeadTarget(targets.get(0));

      try 
      { 
         fciTargets.add("FAIL");
         fail("add call did not fail");
      } 
      catch (UnsupportedOperationException good) {}
      
   }
      
   private void checkFCIConsistency(FamilyClusterInfo fci, int checks, boolean allowOutOfSync)
   {
      
      for (int i = 0; i < checks; i++)
      {
         synchronized (fci)
         {
            if (fci.currentMembershipInSyncWithViewId())
            {
               List targets = fci.getTargets();
               long vid = fci.getCurrentViewId();
               assertEquals("targets and vid match", vid, targets.size());
            }
            else
            {
               assertTrue("OK for FCI view to be out of sync", allowOutOfSync);
            }
         }
      }
      
   }
}
