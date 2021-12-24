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
package org.jboss.ha.framework.server;

import java.util.ArrayList;
import java.util.List;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager.ReplicantListener;
import org.jboss.ha.framework.interfaces.HAPartition;
import java.io.Serializable;
import EDU.oswego.cs.dl.util.concurrent.Latch;

/** 
 * This class is a holder and manager of replicants.
 * It manages lists of replicated objects and changes the list as the HAPartition
 * notifies it.
 *
 * @author bill@burkecentral.com
 * @version $Revision: 57188 $
 *
 * <p><b>Revisions:</b><br>
 * <p><b>2002/01/13: Bill Burke</b>
 * <ol>
 *   <li>Initial revision
 * </ol>
 */
public class HATarget
   implements ReplicantListener
{
   public static final int DISABLE_INVOCATIONS = 0;
   public static final int MAKE_INVOCATIONS_WAIT = 1;
   public static final int ENABLE_INVOCATIONS = 2;
   
   protected String replicantName;
   protected ArrayList replicants = new ArrayList();
   protected HAPartition partition = null;
   protected org.jboss.logging.Logger log;
   protected int clusterViewId = 0;
   protected Serializable target;
   protected int allowInvocationsStatus = 0;
   protected Latch latch = null;
   
   public HATarget(HAPartition partition, 
		   String replicantName,
		   Serializable target,
         int allowInvocations) 
      throws Exception
   {
      this.replicantName = replicantName;      
      this.target = target;      
      init ();
      setInvocationsAuthorization (allowInvocations);
      updateHAPartition(partition);
   }
   
   public void init() throws Exception
   {
      this.log = org.jboss.logging.Logger.getLogger(this.getClass());
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer(super.toString());
      buffer.append('{');
      buffer.append("replicantName="+replicantName);
      buffer.append("partition="+partition.getPartitionName());
      buffer.append("clusterViewId="+clusterViewId);
      buffer.append("allowInvocationsStatus="+allowInvocationsStatus);
      buffer.append("replicants="+replicants);
      buffer.append('}');
      return buffer.toString();
   }

   public long getCurrentViewId()
   {
      return (long)clusterViewId;
   }
   
   public void destroy()
   {
      try
      {
         this.cleanExistenceInCurrentHAPartition();
         
         // maybe some threads are blocked: we let them go here:
         //
         setInvocationsAuthorization (HATarget.DISABLE_INVOCATIONS);
      } 
      catch (Exception e)
      {
         log.error("failed to destroy", e);
      }
   }
 
   // After this call, the HATarget can still be queried for view change, etc. but
   // the local node is no more part of the cluster: temporary state
   //
   public void disable()
   {
      try
      {
         if (this.partition != null)
         {
            log.debug ("Disabled called on HATarget");
            this.partition.getDistributedReplicantManager().remove (this.replicantName);         
         }      
      } 
      catch (Exception e)
      {
         log.error("failed to disable", e);
      }
   }
   
   public ArrayList getReplicants()
   {
      return replicants;
   }
   
   public void updateHAPartition(HAPartition partition) throws Exception
   {
      cleanExistenceInCurrentHAPartition();
      
      this.partition = partition;
      DistributedReplicantManager drm = partition.getDistributedReplicantManager();
      drm.registerListener(this.replicantName, this);
      drm.add(this.replicantName, this.target);
   }

   public synchronized void setInvocationsAuthorization (int status)
   {
      if (this.allowInvocationsStatus == status)
      {
         // we don't release and reget a latch if two identical calls are performed
         //
         log.debug ("Invocation authorization called with no-op");
      }
      else
      {
         // CRITICAL CODE! DONT CHANGE ORDER WITHOUT THINKING ABOUT RACE CONDITIONS
         //
         if (status == MAKE_INVOCATIONS_WAIT)
         {
            log.debug ("Invocation authorization called: MAKE_INVOCATIONS_WAIT");
            latch = new Latch();
            this.allowInvocationsStatus = status;
         } 
         else
         {
            log.debug ("Invocation authorization called: " +
               ((status==ENABLE_INVOCATIONS)?"ENABLE_INVOCATIONS":"DISABLE_INVOCATIONS") );
            this.allowInvocationsStatus = status;
            if (latch != null)
               latch.release();
         }         
      }
   }
   
   public boolean invocationsAllowed () throws InterruptedException
   {
      if (this.allowInvocationsStatus == ENABLE_INVOCATIONS)
         return true;
      else if (this.allowInvocationsStatus == DISABLE_INVOCATIONS)
         return false;
      else if (this.allowInvocationsStatus == MAKE_INVOCATIONS_WAIT)
      {
         latch.acquire ();
         
         // if we arrive here, it means that the status has been changed, so
         // we check for the decision:
         //
         if (this.allowInvocationsStatus == ENABLE_INVOCATIONS)
            return true;
         else
            return false;         
      }    
      else
         return false;
   }
   
   protected void releaseCurrentLatch ()
   {
      latch.release ();
      latch = null;
   }
   
   public HAPartition getAssociatedPartition ()
   {
       return this.partition;
   }
   
   // DistributedReplicantManager.ReplicantListener implementation ------------
      
   public void replicantsChanged(String key, List newReplicants, int newReplicantsViewId)
   {
      if (log.isDebugEnabled())
         log.debug("replicantsChanged '" + replicantName + 
                   "' to " + (newReplicants==null? "0 (null)" : Integer.toString (newReplicants.size() ) ) + 
                   " (intra-view id: " + newReplicantsViewId + ")");
      
      synchronized(replicants)
      {
         // client has reference to replicants so it will automatically get
         // updated
         replicants.clear();
         if (newReplicants != null)
            replicants.addAll(newReplicants);
         
         this.clusterViewId = newReplicantsViewId;
      }
      
   }
   
   protected void cleanExistenceInCurrentHAPartition()
   {
      if (this.partition != null)
      {
         try
         {
            DistributedReplicantManager drm = partition.getDistributedReplicantManager();
            drm.unregisterListener(this.replicantName, this);
            drm.remove(this.replicantName);         
         } 
         catch (Exception e)
         {
            log.error("failed to clean existence in current ha partition", e);
         }
      }    
   }
}
