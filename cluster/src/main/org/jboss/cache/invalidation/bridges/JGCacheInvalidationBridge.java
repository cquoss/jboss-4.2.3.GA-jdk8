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
package org.jboss.cache.invalidation.bridges;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Collection;

import org.jboss.cache.invalidation.BatchInvalidation;
import org.jboss.cache.invalidation.InvalidationManager;
import org.jboss.cache.invalidation.InvalidationGroup;
import org.jboss.cache.invalidation.InvalidationManagerMBean;
import org.jboss.cache.invalidation.BridgeInvalidationSubscription;
import org.jboss.cache.invalidation.InvalidationBridgeListener;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.interfaces.DistributedState;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager;
import org.jboss.ha.framework.server.ClusterPartitionMBean;
import org.jboss.system.server.ServerConfigUtil;

/**
 * JGroups implementation of a cache invalidation bridge
 *
 * @see JGCacheInvalidationBridgeMBean
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 58255 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>24 septembre 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public class JGCacheInvalidationBridge 
   extends org.jboss.system.ServiceMBeanSupport 
   implements JGCacheInvalidationBridgeMBean, 
      DistributedState.DSListenerEx,
      InvalidationBridgeListener, 
      DistributedReplicantManager.ReplicantListener
{
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------      
   
   protected String partitionName = ServerConfigUtil.getDefaultPartitionName();
   /**
    * The ClusterPartition with which we are associated.
    */
   protected ClusterPartitionMBean clusterPartition;
   protected String invalidationManagerName = InvalidationManager.DEFAULT_JMX_SERVICE_NAME;
   protected String bridgeName = "DefaultJGCacheIB";
   
   protected HAPartition partition = null;
   protected DistributedState ds = null;
   protected DistributedReplicantManager drm = null;
   protected String RPC_HANLE_NAME = null;
   protected String nodeName = null;
   
   protected InvalidationManagerMBean invalMgr = null;
   protected BridgeInvalidationSubscription invalidationSubscription = null;
   protected Collection localGroups = null;
   protected Vector bridgedGroups = new Vector ();
   

   protected final Class[] rpc_invalidate_types=new Class[]{String.class, Serializable.class};
   protected final Class[] rpc_invalidates_types=new Class[]{String.class, Serializable[].class};
   protected final Class[] rpc_invalidate_all_types=new Class[]{String.class};
   protected final Class[] rpc_batch_invalidate_types=new Class[]{BatchInvalidation[].class};


   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------

   public JGCacheInvalidationBridge ()
   {
   }
      
   // Public --------------------------------------------------------
   
   // JGCacheInvalidationBridgeMBean implementation ----------------------------------------------
   
   public String getInvalidationManager ()
   {
      return this.invalidationManagerName;
   }

   public ClusterPartitionMBean getClusterPartition()
   {
      return clusterPartition;
   }

   public void setClusterPartition(ClusterPartitionMBean clusterPartition)
   {
      this.clusterPartition = clusterPartition;
   }
   
   public String getPartitionName ()
   {
      return this.partitionName;
   }
   
   public void setInvalidationManager (String objectName)
   {
      this.invalidationManagerName = objectName;
   }
   
   public void setPartitionName (String partitionName)
   {
      this.partitionName = partitionName;
   }
   
   public String getBridgeName ()
   {
      return this.bridgeName;
   }
   
   public void setBridgeName (String name)
   {
      this.bridgeName = name;
   }
   
   // DistributedReplicantManager.ReplicantListener implementation ---------------------------
   
   /**
    * @todo examine thread safety. synchronized keyword was added to method 
    * signature when internal behavior of DistributedReplicantManagerImpl was 
    * changed so that multiple threads could concurrently send replicantsChanged
    * notifications. Need to examine in detail how this method interacts with
    * DistributedState to see if we can remove/narrow the synchronization. 
    */
   public synchronized void replicantsChanged (String key, java.util.List newReplicants, int newReplicantsViewId)
   {
      if (key.equals (this.RPC_HANLE_NAME) && this.drm.isMasterReplica (this.RPC_HANLE_NAME))
      {
         log.debug ("The list of replicant for the JG bridge has changed, computing and updating local info...");
         
         // we remove any entry from the DS whose node is dead
         //
         java.util.Collection coll = this.ds.getAllKeys (this.RPC_HANLE_NAME);                  
         if (coll == null) 
         {
            log.debug ("... No bridge info was associated to this node");
            return;
         }
         
         // to avoid ConcurrentModificationException, we copy the list of keys in a new structure
         //
         ArrayList collCopy = new java.util.ArrayList (coll);
         java.util.List newReplicantsNodeNames = this.drm.lookupReplicantsNodeNames (this.RPC_HANLE_NAME);
         

         for (int i = 0; i < collCopy.size(); i++)
         {
            String nodeEntry = (String)collCopy.get(i);
            if (!newReplicantsNodeNames.contains (nodeEntry))
            {
               // the list of bridged topic contains a dead member: we remove it
               //
               try
               {
                  log.debug ("removing bridge information associated to this node from the DS");
                  this.ds.remove (this.RPC_HANLE_NAME, nodeEntry, true);
               }
               catch (Exception e)
               {
                  log.info ("Unable to remove a node entry from the distributed cache", e);
               }
            }
         }
      }
   }
      
   // DistributedState.DSListener implementation ----------------------------------------------
   
    public void valueHasChanged (String category, Serializable key, Serializable value, boolean locallyModified)
    {
       this.updatedBridgedInvalidationGroupsInfo ();
    }
    
    public void keyHasBeenRemoved (String category, Serializable key, Serializable previousContent, boolean locallyModified)
    {
       this.updatedBridgedInvalidationGroupsInfo ();
    }

   // InvalidationBridgeListener implementation ----------------------------------------------
   
   public void batchInvalidate (BatchInvalidation[] invalidations, boolean asynchronous)
   {
      if (invalidations == null) return;
      
      // we need to sort which group other nodes accept or refuse and propagate through the net
      //      
      ArrayList acceptedGroups = new ArrayList();
      
      for (int i=0; i<invalidations.length; i++)
      {
         BatchInvalidation currBI = invalidations[i];
         if (groupExistsRemotely (currBI.getInvalidationGroupName ()))
            acceptedGroups.add (currBI);
      }
      
      if (acceptedGroups.size () > 0)
      {
         BatchInvalidation[] result = new BatchInvalidation[acceptedGroups.size ()];
         result = (BatchInvalidation[])acceptedGroups.toArray (result);
         
         if (log.isTraceEnabled ())
            log.trace ("Transmitting batch invalidation: " + result);
         this._do_rpc_batchInvalidate (result, asynchronous);      
      }
   }
   
   public void invalidate (String invalidationGroupName, Serializable[] keys, boolean asynchronous)
   {
      // if the group exists on another node, we simply propagate to other nodes
      //
      if (log.isTraceEnabled ())
         log.trace ("Transmitting invalidations for group: " + invalidationGroupName);
      
      if (groupExistsRemotely (invalidationGroupName))
         _do_rpc_invalidates (invalidationGroupName, keys, asynchronous);
   }
   
   public void invalidate (String invalidationGroupName, Serializable key, boolean asynchronous)
   {
      // if the group exists on another node, we simply propagate to other nodes
      //
      if (log.isTraceEnabled ())
         log.trace ("Transmitting invalidation for group: " + invalidationGroupName);

      if (groupExistsRemotely (invalidationGroupName))
         _do_rpc_invalidate (invalidationGroupName, key, asynchronous);
   }

   public void invalidateAll(String groupName, boolean async)
   {
      if (log.isTraceEnabled ())
         log.trace ("Transmitting for all entries for invalidation for group: " + groupName);
      if (groupExistsRemotely (groupName))
         _do_rpc_invalidate_all (groupName, async);
   }

   public void newGroupCreated (String groupInvalidationName)
   {
      try
      {
         this.publishLocalInvalidationGroups ();
         //this.updatedBridgedInvalidationGroupsInfo ();
      }
      catch (Exception e)
      {
         log.info ("Problem while registering a new invalidation group over the cluster", e);
      }
   }
   
   public void groupIsDropped (String groupInvalidationName)
   {
      try
      {
         this.publishLocalInvalidationGroups ();
         //this.updatedBridgedInvalidationGroupsInfo ();
      }
      catch (Exception e)
      {
         log.info ("Problem while un-registering a new invalidation group over the cluster", e);
      }
   }
   
   // ServiceMBeanSupport overrides ---------------------------------------------------
   
   public void startService () throws Exception
   {
      RPC_HANLE_NAME = "DCacheBridge-" + this.bridgeName;
      
      // Support old-style partition lookup for configs that don't
      // inject the partition.
      // TODO remove this after a while; deprecated in 4.0.4
      if (this.clusterPartition == null)
      {
         javax.naming.Context ctx = new javax.naming.InitialContext ();
         this.partition = (HAPartition)ctx.lookup("/HAPartition/" + this.partitionName); 
      }
      else
      {
         this.partition = this.clusterPartition.getHAPartition();
         this.partitionName = this.partition.getPartitionName();
      }
      
      this.ds = this.partition.getDistributedStateService ();
      this.drm = this.partition.getDistributedReplicantManager ();
      this.nodeName = this.partition.getNodeName ();
      
      this.drm.add (this.RPC_HANLE_NAME, "");
      this.drm.registerListener (this.RPC_HANLE_NAME, this);      
      this.ds.registerDSListenerEx (RPC_HANLE_NAME, this);
      this.partition.registerRPCHandler(RPC_HANLE_NAME, this);   
      
      // we now publish the list of caches we have access to
      //
      this.invalMgr = (org.jboss.cache.invalidation.InvalidationManagerMBean)
         org.jboss.system.Registry.lookup (this.invalidationManagerName);
      
      publishLocalInvalidationGroups ();      
      this.updatedBridgedInvalidationGroupsInfo ();
      
      this.invalidationSubscription = invalMgr.registerBridgeListener (this);
      
   }
   
   public void stopService ()
   {
      try
      {
         this.partition.unregisterRPCHandler (this.RPC_HANLE_NAME, this);
         this.ds.unregisterDSListenerEx (this.RPC_HANLE_NAME, this);
         this.drm.unregisterListener (this.RPC_HANLE_NAME, this);
         this.drm.remove (this.RPC_HANLE_NAME);
         
         this.invalidationSubscription.unregister ();
                  
         this.ds.remove (this.RPC_HANLE_NAME, this.nodeName, true);
         
         this.invalMgr = null;
         this.partition = null;
         this.drm = null;
         this.ds = null;
         this.invalidationSubscription = null;
         this.RPC_HANLE_NAME = null;
         this.nodeName = null;
         this.localGroups = null;
         this.bridgedGroups = new Vector ();                  
      }
      catch (Exception e)
      {
         log.info ("Problem while shuting down invalidation cache bridge", e);
      }
   }
   
   // RPC calls ---------------------------------------------
   
   public void _rpc_invalidate (String invalidationGroupName, Serializable key)
   {
      if (log.isTraceEnabled ())
         log.trace ("Received remote invalidation for group: " + invalidationGroupName);

      this.invalidationSubscription.invalidate (invalidationGroupName, key);
   }
   
   public void _rpc_invalidates (String invalidationGroupName, Serializable[] keys)
   {
      if (log.isTraceEnabled ())
         log.trace ("Received remote invalidations for group: " + invalidationGroupName);

      this.invalidationSubscription.invalidate (invalidationGroupName, keys);
   }

   public void _rpc_invalidate_all (String invalidationGroupName)
   {
      if (log.isTraceEnabled ())
         log.trace ("Received remote invalidate_all for group: " + invalidationGroupName);

      this.invalidationSubscription.invalidateAll (invalidationGroupName);
   }

   public void _rpc_batchInvalidate (BatchInvalidation[] invalidations)
   {
      if (log.isTraceEnabled () && invalidations != null)
         log.trace ("Received remote batch invalidation for this number of groups: " + invalidations.length);

      this.invalidationSubscription.batchInvalidate (invalidations);
   }

   protected void _do_rpc_invalidate (String invalidationGroupName, Serializable key, boolean asynch)
   {
      Object[] params = new Object[] {invalidationGroupName, key};
      try
      {         
         if (asynch)
            this.partition.callAsynchMethodOnCluster (this.RPC_HANLE_NAME,
                                                      "_rpc_invalidate",
                                                      params, rpc_invalidate_types, true);
         else
            this.partition.callMethodOnCluster (this.RPC_HANLE_NAME,
                                                "_rpc_invalidate",
                                                params, rpc_invalidate_types, true);
      }
      catch (Exception e)
      {
         log.debug ("Distributed invalidation (1) has failed for group " + 
                    invalidationGroupName + " (Bridge: " + this.bridgeName + ")");
      }
   }
   
   protected void _do_rpc_invalidates (String invalidationGroupName, Serializable[] keys, boolean asynch)
   {
      Object[] params = new Object[] {invalidationGroupName, keys};
      try
      {         
         if (asynch)
            this.partition.callAsynchMethodOnCluster (this.RPC_HANLE_NAME,
                                                      "_rpc_invalidates", params, rpc_invalidates_types, true);
         else
            this.partition.callMethodOnCluster (this.RPC_HANLE_NAME,
                                                "_rpc_invalidates", params, rpc_invalidates_types, true);
      }
      catch (Exception e)
      {
         log.debug ("Distributed invalidation (2) has failed for group " + 
                    invalidationGroupName + " (Bridge: " + this.bridgeName + ")");
      }
   }

   protected void _do_rpc_invalidate_all (String invalidationGroupName, boolean asynch)
   {
      Object[] params = new Object[] {invalidationGroupName};
      try
      {
         if (asynch)
            this.partition.callAsynchMethodOnCluster (this.RPC_HANLE_NAME,
                                                      "_rpc_invalidate_all", params, rpc_invalidate_all_types, true);
         else
            this.partition.callMethodOnCluster (this.RPC_HANLE_NAME,
                                                "_rpc_invalidate_all", params, rpc_invalidate_all_types, true);
      }
      catch (Exception e)
      {
         log.debug ("Distributed invalidation (2) has failed for group " +
                    invalidationGroupName + " (Bridge: " + this.bridgeName + ")");
      }
   }
   
   protected void _do_rpc_batchInvalidate (BatchInvalidation[] invalidations, boolean asynch)
   {
      Object[] params = new Object[] {invalidations};
      try
      {         
         if (asynch)
            this.partition.callAsynchMethodOnCluster (this.RPC_HANLE_NAME,
                                                      "_rpc_batchInvalidate", params, rpc_batch_invalidate_types, true);
         else
            this.partition.callMethodOnCluster (this.RPC_HANLE_NAME,
                                                "_rpc_batchInvalidate", params, rpc_batch_invalidate_types, true);
      }
      catch (Exception e)
      {
         log.debug ("Distributed invalidation (3) has failed (Bridge: " + this.bridgeName + ")");
      }
   }

   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   protected synchronized void publishLocalInvalidationGroups () throws Exception
   {
      this.localGroups = invalMgr.getInvalidationGroups ();      
      
      log.debug ("Publishing locally available invalidation groups: " + this.localGroups);

      ArrayList content = new ArrayList (this.localGroups);      
      ArrayList result = new ArrayList (content.size ());
      

      for (int i = 0; i < content.size(); i++)
      {
         String aGroup = ((InvalidationGroup)content.get(i)).getGroupName ();
         result.add (aGroup);
      }
      
      if (result.size () > 0)       
      {
         NodeInfo info = new NodeInfo (result, this.nodeName);
         this.ds.set (this.RPC_HANLE_NAME, this.nodeName, info, true);
      }
      else
         this.ds.remove (this.RPC_HANLE_NAME, this.nodeName, true);
   }
   
   protected void updatedBridgedInvalidationGroupsInfo ()
   {
      Collection bridgedByNode = this.ds.getAllValues (this.RPC_HANLE_NAME);
      
      log.debug ("Updating list of invalidation groups that are bridged...");
      
      if (bridgedByNode != null)
      {
         // Make a copy
         //      
         ArrayList copy = new ArrayList (bridgedByNode);

         Vector result  = new Vector ();
         

         for (int i = 0; i < copy.size(); i++)
         {
            NodeInfo infoForNode = (NodeInfo)copy.get(i);
            log.trace ("InfoForNode: " + infoForNode);
            
            if (infoForNode != null && !infoForNode.groupName.equals (this.nodeName))
            {
               ArrayList groupsForNode = infoForNode.groups;
               log.trace ("Groups for node: " + groupsForNode);
               

               for (int j = 0; j < groupsForNode.size(); j++)
               {
                  String aGroup = (String)groupsForNode.get(j);
                  if (!result.contains (aGroup))
                  {
                     log.trace ("Adding: " + aGroup);
                     result.add (aGroup);                  
                  }
               }
               
            }            
            
         }
         // atomic assignation of the result
         //
         this.bridgedGroups = result;
         
         log.debug ("... computed list of bridged groups: " + result);
      }
      else
      {
         log.debug ("... nothing needs to be bridged.");            
      }
         
   }
   
   protected boolean groupExistsRemotely (String groupName)
   {
      return this.bridgedGroups.contains (groupName);
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}

class NodeInfo implements java.io.Serializable
{
   static final long serialVersionUID = -3215712955134929006L;

   public ArrayList groups = null;
   public String groupName = null;
   
   public NodeInfo (){}
   
   public NodeInfo (ArrayList groups, String groupName)
   {
      this.groups = groups;
      this.groupName = groupName;
   }
   
}
