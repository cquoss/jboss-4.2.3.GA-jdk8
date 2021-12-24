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
package org.jboss.ha.singleton;

import java.util.List;

import javax.management.Notification;
import javax.management.ReflectionException;

import org.jboss.ha.framework.interfaces.ClusterMergeStatus;
import org.jboss.ha.jmx.HAServiceMBeanSupport;
import org.jboss.invocation.jrmp.server.JRMPProxyFactoryMBean;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.MBeanProxyInstance;

/** 
 * Base class for HA-Singleton services.
 *
 * @author <a href="mailto:ivelin@apache.org">Ivelin Ivanov</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @version $Revision: 72388 $
 */
public class HASingletonSupport extends HAServiceMBeanSupport
   implements HASingletonSupportMBean, HASingleton
{
   // Private Data --------------------------------------------------
   
   private boolean isMasterNode = false;
   private HASingletonElectionPolicy mElectionPolicyMB = null;
   private boolean restartOnMerge = true;

   // Constructors --------------------------------------------------
   
   /**
    * Default CTOR
    */
   public HASingletonSupport()
   {
      // empty
   }

   // Attributes ----------------------------------------------------
   
   /**
    * @jmx:managed-attribute
    * 
    * @return true if this cluster node has the active mbean singleton, false otherwise
    */
   public boolean isMasterNode()
   {
      return isMasterNode;
   }

   /**
    * @see HASingletonSupportMBean#setElectionPolicy(HASingletonElectionPolicy)
    */
   public void setElectionPolicy(HASingletonElectionPolicy mb)
   {
      this.mElectionPolicyMB = mb;
   }
   
   /**
    * @see HASingletonSupportMBean#getElectionPolicy()
    */
   public HASingletonElectionPolicy getElectionPolicy()
   {
      return this.mElectionPolicyMB;
   }

   /**
    * Gets whether this singleton will stop and restart itself if it is the
    * master and a cluster merge occurs.
    * <p/>
    * A restart allows the service to reset any state that may
    * have gotten out-of-sync with the rest of the cluster while
    * the just-merged split was in effect.
    * 
    * @return <code>true</code> if a restart will occur, <code>false</code>
    *         otherwise
    */
   public boolean getRestartOnMerge()
   {
      return restartOnMerge;
   }

   /**
    * Sets whether this singleton will stop and restart itself if it is the
    * master and a cluster merge occurs?
    * <p/>
    * A restart allows the service to reset any state that may
    * have gotten out-of-sync with the rest of the cluster while
    * the just-merged split was in effect.
    * 
    * @param restartOnMerge <code>true</code> if a restart should occur, 
    *                       <code>false</code> otherwise
    */
   public void setRestartOnMerge(boolean restartOnMerge)
   {
      this.restartOnMerge = restartOnMerge;
   }

   // Public --------------------------------------------------------

   /**
    * Extending classes should override this method and implement the custom
    * singleton logic. Only one node in the cluster is the active master.
    * If the current node is elected for master, this method is invoked.
    * When another node is elected for master for some reason, the
    * stopSingleton() method is invokded.
    * <p>
    * When the extending class is a stateful singleton, it will
    * usually use putDistributedState() and getDistributedState() to save in
    * the cluster environment information that will be needed by the next node
    * elected for master should the current master node fail.  
    *
    * @see HASingleton
    */
   public void startSingleton()
   {
      if (log.isDebugEnabled())
         log.debug("startSingleton() : elected for master singleton node");

      // Extending classes will implement the singleton logic here
   }

   /**
    * Extending classes should override this method and implement the custom
    * singleton logic. Only one node in the cluster is the active master.
    * If the current node is master and another node is elected for master, this
    * method is invoked.
    * 
    * @see HASingleton
    */
   public void stopSingleton()
   {
      if (log.isDebugEnabled())
         log.debug("stopSingleton() : another node in the partition (if any) is elected for master");
      
      // Extending classes will implement the singleton logic here
   }

   /**
    * When topology changes, a new master is elected based on the result
    * of the isDRMMasterReplica() call.
    * 
    * @see HAServiceMBeanSupport#partitionTopologyChanged(List, int)
    * @see  DistributedReplicantManager#isMasterReplica(String);
    */
   public void partitionTopologyChanged(List newReplicants, int newViewID)
   {   
      boolean isElectedNewMaster;
      if (this.mElectionPolicyMB != null)
         isElectedNewMaster = this.mElectionPolicyMB.isElectedMaster();
      else
         isElectedNewMaster = isDRMMasterReplica();
      
      if (log.isDebugEnabled())
      {
         log.debug("partitionTopologyChanged, isElectedNewMaster=" + isElectedNewMaster
            + ", isMasterNode=" + isMasterNode + ", viewID=" + newViewID);
      }

      // if this node is already the master, don't bother electing it again
      if (isElectedNewMaster && isMasterNode)
      {
         // JBAS-4229         
         if (restartOnMerge && ClusterMergeStatus.isMergeInProcess())
         {
            restartMaster();
         }
      }
      // just becoming master
      else if (isElectedNewMaster && !isMasterNode)
      {
         makeThisNodeMaster();
      }
      // transition from master to slave
      else if (isMasterNode == true)
      {
         _stopOldMaster();
      }
   }

   /**
    * This method will be invoked twice by the local node 
    * when it stops as well as by the remote
    */
   public void _stopOldMaster()
   {
      log.debug("_stopOldMaster, isMasterNode=" + isMasterNode);
      
      try 
      {
         // since this is a cluster call, all nodes will hear it
         // so if the node is not the master, then ignore 
         if (isMasterNode == true)
         {
            isMasterNode = false;
            
            // notify stopping
            sendLocalNotification(HASINGLETON_STOPPING_NOTIFICATION);
            
            // stop the singleton
            stopSingleton();
            
            // notify stopped
            sendLocalNotification(HASINGLETON_STOPPED_NOTIFICATION);
         }
      }
      catch (Exception ex)
      {
         log.error(
            "_stopOldMaster failed. Will still try to start new master. " +
            "You need to examine the reason why the old master wouldn't stop and resolve it. " +
            "It is bad that the old singleton may still be running while we are starting a new one, " +
            "so you need to resolve this ASAP.", ex);
      }
   }

   // Protected -----------------------------------------------------
   
   protected void makeThisNodeMaster()
   {
      try
      {
         // stop the old master (if there is one) before starting the new one

         // ovidiu 09/02/04 - temporary solution for Case 1843, use an asynchronous
         // distributed call.
         //callMethodOnPartition("_stopOldMaster", new Object[0], new Class[0]);
         callAsyncMethodOnPartition("_stopOldMaster", new Object[0], new Class[0]);

         startNewMaster();  
      }
      catch (Exception ex)
      {
         log.error("_stopOldMaster failed. New master singleton will not start.", ex);
      }
   }
   
   protected void startNewMaster()
   {
      log.debug("startNewMaster, isMasterNode=" + isMasterNode);
      
      isMasterNode = true;
      
      // notify starting
      sendLocalNotification(HASINGLETON_STARTING_NOTIFICATION);

      // start new master
      startSingleton();
      
      // notify started
      sendLocalNotification(HASINGLETON_STARTED_NOTIFICATION);
   }
   
   protected void restartMaster()
   {
      _stopOldMaster();
      startNewMaster();
   }
   
   @Override
   protected void setupPartition() throws Exception
   {
      super.setupPartition();
      
      if (mElectionPolicyMB != null)
      {
         if (log.isTraceEnabled())
         {
            log.trace("Optional singleton election policy defined: " + mElectionPolicyMB);
         }
         
         mElectionPolicyMB.setHAPartition(getPartition());
         
         try
         {
            server.invoke(((MBeanProxyInstance)mElectionPolicyMB).getMBeanProxyObjectName(), 
                  "setSingletonName", new Object[] {getServiceHAName()}, 
                  new String[] {"java.lang.String"});
         }
         catch(ReflectionException re)
         {
            log.debug("Selected election policy does not support setting singleton name, skipping assignment.");
         }         
      }
   }
   
   // Private -------------------------------------------------------
   
   private void sendLocalNotification(String type)
   {
      Notification n = new Notification(type, this, getNextNotificationSequenceNumber());
      super.sendNotificationToLocalListeners(n);
   }
}
