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
package org.jboss.ha.jmx;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.management.AttributeChangeNotification;
import javax.management.InstanceNotFoundException;
import javax.management.Notification;
import javax.management.ObjectInstance;
import javax.management.Query;
import javax.management.QueryExp;

import org.jboss.ha.framework.interfaces.DistributedReplicantManager;
import org.jboss.ha.framework.interfaces.DistributedState;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.server.ClusterPartition;
import org.jboss.ha.framework.server.ClusterPartitionMBean;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigUtil;

/**
 *  
 * Management Bean for an HA-Service.
 * Provides a convenient common base for cluster symmetric MBeans.
 * 
 * This class is also a user transparent extension
 * of the standard NotificationBroadcasterSupport
 * to a clustered environment.
 * Listeners register with their local broadcaster.
 * Invoking sendNotification() on any broadcaster,
 * will notify all listeners in the same cluster partition.
 * TODO: The performance can be further optimized by avoiding broadcast messages
 * when remote listener nodes are not interested (e.g. have no local subscribers)
 * or by iterating locally over filters or remote listeners. 
 *  
 * @author  Ivelin Ivanov <ivelin@apache.org> *
 * @version $Revision: 57188 $
 *
 */
public class HAServiceMBeanSupport
   extends ServiceMBeanSupport
   implements HAServiceMBean
{
   // Constants -----------------------------------------------------
 
   // Attributes ----------------------------------------------------
   private HAPartition partition_;
   private ClusterPartitionMBean clusterPartition;
   private String partitionName = ServerConfigUtil.getDefaultPartitionName();

   private DistributedReplicantManager.ReplicantListener drmListener = null;

   /** 
    * DRM participation TOKEN 
    */
   private String REPLICANT_TOKEN = "";

   private boolean sendLocalLifecycleNotifications = true;
   private boolean sendRemoteLifecycleNotifications = true;
   
   // Public --------------------------------------------------------

   public HAServiceMBeanSupport()
   {
      // for JMX
   }

   public ClusterPartitionMBean getClusterPartition()
   {
      return clusterPartition;
   }

   public void setClusterPartition(ClusterPartitionMBean clusterPartition)
   {
      if ((getState() != STARTED) && (getState() != STARTING))
      {         
         this.clusterPartition = clusterPartition;
      }
   }

   public String getPartitionName()
   {
      return partitionName;
   }

   public void setPartitionName(String newPartitionName)
   {
      if ((getState() != STARTED) && (getState() != STARTING))
      {
         partitionName = newPartitionName;
      }
   }


   // Protected ------------------------------

   /**
    * 
    * 
    * Convenience method for sharing state across a cluster partition.
    * Delegates to the DistributedStateService
    * 
    * @param key key for the distributed object
    * @param value the distributed object
    * 
    */
   public void setDistributedState(String key, Serializable value)
      throws Exception
   {
      DistributedState ds = getPartition().getDistributedStateService();
      ds.set(getServiceHAName(), key, value);
   }

   /**
    * 
    * Convenience method for sharing state across a cluster partition.
    * Delegates to the DistributedStateService
    * 
    * @param key key for the distributed object
    * @return Serializable the distributed object 
    * 
    */
   public Serializable getDistributedState(String key)
   {
      DistributedState ds = getPartition().getDistributedStateService();
      return ds.get(getServiceHAName(), key);
   }

   /**
    * <p>
    * Implementors of this method should not
    * code the singleton logic here. 
    * The MBean lifecycle create/start/stop are separate from
    * the singleton logic.
    * Singleton logic should originate in becomeMaster().
    * </p>
    * 
    * <p>
    * <b>Attention</b>: Always call this method when you overwrite it in a subclass
    *                   because it elects the master singleton node.
    * </p>
    * 
    */
   protected void startService() throws Exception
   {
      boolean debug = log.isDebugEnabled();
      if (debug)
         log.debug("start HAServiceMBeanSupport");

      setupPartition();

      registerRPCHandler();

      registerDRMListener();
   }

   /**
    * <p>
    * <b>Attention</b>: Always call this method when you overwrite it in a subclass
    * </p>
    * 
    */
   protected void stopService() throws Exception
   {
      boolean debug = log.isDebugEnabled();
      if (debug)
         log.debug("stop HAServiceMBeanSupport");

      unregisterDRMListener();

      unregisterRPCHandler();
   }

   protected void setupPartition() throws Exception
   {
      // get handle to the cluster partition
      if (clusterPartition == null)
      {
         String pName = getPartitionName();
         partition_ = findHAPartitionWithName(pName);
      }
      else
      {
         partition_ = clusterPartition.getHAPartition();
         partitionName = partition_.getPartitionName();
      }
   }

   protected void registerRPCHandler()
   {
      partition_.registerRPCHandler(getServiceHAName(), this);
   }

   protected void unregisterRPCHandler()
   {
      partition_.unregisterRPCHandler(getServiceHAName(), this);
   }

   protected void registerDRMListener() throws Exception
   {
      DistributedReplicantManager drm =
         this.partition_.getDistributedReplicantManager();

      // register to listen to topology changes, which might cause the election of a new master
      drmListener = new DistributedReplicantManager.ReplicantListener()
      {
         Object mutex = new Object();
         
         public void replicantsChanged(
            String key,
            List newReplicants,
            int newReplicantsViewId)
         {
            if (key.equals(getServiceHAName()))
            {
               // This synchronized block was added when the internal behavior of
               // DistributedReplicantManagerImpl was changed so that concurrent
               // replicantsChanged notifications are possible. Synchronization
               // ensures that this change won't break non-thread-safe 
               // subclasses of HAServiceMBeanSupport.
               synchronized(mutex)
               {
                  // change in the topology callback
                  HAServiceMBeanSupport.this.partitionTopologyChanged(newReplicants, newReplicantsViewId);
               }
            }
         }
      };
      drm.registerListener(getServiceHAName(), drmListener);

      // this ensures that the DRM knows that this node has the MBean deployed 
      drm.add(getServiceHAName(), REPLICANT_TOKEN);
   }

   protected void unregisterDRMListener() throws Exception
   {
      DistributedReplicantManager drm =
         this.partition_.getDistributedReplicantManager();

      // remove replicant node  
      drm.remove(getServiceHAName());

      // unregister 
      drm.unregisterListener(getServiceHAName(), drmListener);
   }

   public void partitionTopologyChanged(List newReplicants, int newReplicantsViewId)
   {
      boolean debug = log.isDebugEnabled();

      if (debug)
      {
         log.debug("partitionTopologyChanged(). cluster view id: " + newReplicantsViewId);
      }
   }

   protected boolean isDRMMasterReplica()
   {
      DistributedReplicantManager drm =
         getPartition().getDistributedReplicantManager();

      return drm.isMasterReplica(getServiceHAName());
   }


   public HAPartition getPartition()
   {
      return partition_;
   }

    /**
     *
     * @param methodName
     * @param args
     * @throws Exception
     * @deprecated Use {@link #callMethodOnPartition(String, Object[], Class[])} instead
     */
   public void callMethodOnPartition(String methodName, Object[] args)
      throws Exception
   {
      getPartition().callMethodOnCluster(
         getServiceHAName(),
         methodName,
         args,
         true);
   }

    public void callMethodOnPartition(String methodName, Object[] args, Class[] types)
      throws Exception
   {
      getPartition().callMethodOnCluster(
         getServiceHAName(),
         methodName,
         args, types,
         true);
   }

   /**
    * Gets whether JMX Notifications should be sent to local (same JVM) listeners
    * if the notification is for an attribute change to attribute "State".
    * <p>
    * Default is <code>true</code>.
    * </p>
    * @see #sendNotification(Notification)
    */
   public boolean getSendLocalLifecycleNotifications()
   {
      return sendLocalLifecycleNotifications;
   }

   /**
    * Sets whether JMX Notifications should be sent to local (same JVM) listeners
    * if the notification is for an attribute change to attribute "State".
    * <p>
    * Default is <code>true</code>.
    * </p>
    * @see #sendNotification(Notification)
    */
   public void setSendLocalLifecycleNotifications(boolean sendLocalLifecycleNotifications)
   {
      this.sendLocalLifecycleNotifications = sendLocalLifecycleNotifications;
   }

   /**
    * Gets whether JMX Notifications should be sent to remote listeners
    * if the notification is for an attribute change to attribute "State".
    * <p>
    * Default is <code>true</code>.
    * </p>
    * <p>
    * See http://jira.jboss.com/jira/browse/JBAS-3194 for an example of a
    * use case where this property should be set to false.
    * </p>
    * 
    * @see #sendNotification(Notification)
    */
   public boolean getSendRemoteLifecycleNotifications()
   {
      return sendRemoteLifecycleNotifications;
   }

   /**
    * Sets whether JMX Notifications should be sent to remote listeners
    * if the notification is for an attribute change to attribute "State".
    * <p>
    * Default is <code>true</code>.
    * </p>
    * <p>
    * See http://jira.jboss.com/jira/browse/JBAS-3194 for an example of a
    * use case where this property should be set to false.
    * </p>
    * 
    * @see #sendNotification(Notification)
    */
   public void setSendRemoteLifecycleNotifications(boolean sendRemoteLifecycleNotifications)
   {
      this.sendRemoteLifecycleNotifications = sendRemoteLifecycleNotifications;
   }

   /** 
    * Broadcast the notification to the remote listener nodes (if any) and then 
    * invoke super.sendNotification() to notify local listeners.
    * 
    * @param notification sent out to local listeners and other nodes. It should be serializable.
    * It is recommended that the source of the notification is an ObjectName of an MBean that 
    * is is available on all nodes where the broadcaster MBean is registered. 
    *   
    * @see #getSendLocalLifecycleNotifications()
    * @see #getSendRemoteLifecycleNotifications()
    * @see javax.management.NotificationBroadcasterSupport#sendNotification(Notification)
    */
   public void sendNotification(Notification notification)
   {
      boolean stateChange = isStateChangeNotification(notification);
      
      if (!stateChange || sendRemoteLifecycleNotifications)
      {
         try
         {
            // Overriding the source MBean with its ObjectName
            // to ensure that it can be safely transferred over the wire
            notification.setSource(this.getServiceName());
            sendNotificationRemote(notification);
         }
   
         catch (Throwable th)
         {
            boolean debug = log.isDebugEnabled();
            if (debug)
               log.debug("sendNotificationRemote( " + notification + " ) failed ", th);
            // even if broadcast failed, local notification should still be sent
   
         }
      }
      
      if (!stateChange || sendLocalLifecycleNotifications)
      {
         sendNotificationToLocalListeners(notification);
      }
   }
   
   private boolean isStateChangeNotification(Notification notification)
   {
      boolean stateChange = false;
      if (notification instanceof AttributeChangeNotification)
      {
         stateChange = "State".equals(((AttributeChangeNotification) notification).getAttributeName());
      }
      return stateChange;
   }

   protected void sendNotificationToLocalListeners(Notification notification)
   {
      super.sendNotification(notification);
   }

   protected void callAsyncMethodOnPartition(String methodName, Object[] args, Class[] types)
      throws Exception
   {
      HAPartition partition = getPartition();
      if (partition != null)
      {
         getPartition().callAsynchMethodOnCluster(
            getServiceHAName(),
            methodName,
            args, types,
            true);
      }
   }


   /**
    * 
    * Broadcast a notifcation remotely to the partition participants
    * 
    * @param notification
    */
   protected void sendNotificationRemote(Notification notification)
      throws Exception
   {
      callAsyncMethodOnPartition("_receiveRemoteNotification",
                                 new Object[]{notification}, new Class[]{Notification.class});
   }

   /**
    * 
    * Invoked by remote broadcasters. 
    * Delegates to the super class
    * 
    */
   public void _receiveRemoteNotification(Notification notification)
   {
      super.sendNotification(notification);
   }


   /**
    * 
    * Override this method only if you need to provide a custom partition wide unique service name. 
    * The default implementation will usually work, provided that 
    * the getServiceName() method returns a unique canonical MBean name.
    * 
    * @return partition wide unique service name
    */
   public String getServiceHAName()
   {
      return getServiceName().getCanonicalName();
   }


   protected HAPartition findHAPartitionWithName(String name) throws Exception
   {
      log.debug("findHAPartitionWithName, name="+name);
      HAPartition result = null;
      QueryExp classEQ = Query.eq(Query.classattr(),
               Query.value(ClusterPartition.class.getName()));
      QueryExp matchPartitionName = Query.match(Query.attr("PartitionName"),
         Query.value(name));
      QueryExp exp = Query.and(classEQ, matchPartitionName);
      Set mbeans = this.getServer().queryMBeans(null, exp);
      if (mbeans != null && mbeans.size() > 0)
      {
         ObjectInstance inst = (ObjectInstance) (mbeans.iterator().next());
         ClusterPartitionMBean cp =
            (ClusterPartitionMBean) MBeanProxyExt.create(
               ClusterPartitionMBean.class,
               inst.getObjectName(),
               this.getServer());
         result = cp.getHAPartition();
      }

      if( result == null )
      {
         String msg = "Failed to find HAPartition with PartitionName="+name;
         throw new InstanceNotFoundException(msg);
      }
      return result;
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}

