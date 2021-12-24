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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.management.MBeanServer;

import org.jgroups.JChannel;
import org.jgroups.MergeView;
import org.jgroups.View;
import org.jgroups.Message;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.MethodCall;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;

import org.jboss.invocation.MarshalledValueInputStream;
import org.jboss.invocation.MarshalledValueOutputStream;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager;
import org.jboss.ha.framework.interfaces.DistributedState;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.interfaces.ClusterNode;

import org.jboss.naming.NonSerializableFactory;
import org.jboss.logging.Logger;

/**
 * This class is an abstraction class for a JGroups RPCDispatch and JChannel.
 * It is a default implementation of HAPartition for the
 * <a href="http://www.jgroups.com/">JGroups</A> framework
 *
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 62255 $
 */
public class HAPartitionImpl
   extends org.jgroups.blocks.RpcDispatcher
   implements org.jgroups.MessageListener, org.jgroups.MembershipListener,
      HAPartition, AsynchEventHandler.AsynchEventProcessor
{
   private static class NoHandlerForRPC implements Serializable
   {
      static final long serialVersionUID = -1263095408483622838L;
   }

   // Constants -----------------------------------------------------

   // final MethodLookup method_lookup_clos = new MethodLookupClos();

   // Attributes ----------------------------------------------------

   protected HashMap rpcHandlers = new HashMap();
   protected HashMap stateHandlers = new HashMap();
   /** Do we send any membership change notifications synchronously? */
   protected boolean allowSyncListeners = false;
   /** The synch HAMembershipListener and HAMembershipExtendedListeners */
   protected ArrayList synchListeners = new ArrayList();
   /** The asynch HAMembershipListener and HAMembershipExtendedListeners */
   protected ArrayList asynchListeners = new ArrayList();
   /** The handler used to send membership change notifications asynchronously */
   protected AsynchEventHandler asynchHandler;
   /** The current cluster partition members */
   protected Vector members = null;
   protected Vector jgmembers = null;

   public Vector history = null;

   /** The partition members other than this node */
   protected Vector otherMembers = null;
   protected Vector jgotherMembers = null;
   /** The JChannel name */
   protected String partitionName;
   /** the local JG IP Address */
   protected org.jgroups.stack.IpAddress localJGAddress = null;
   /** The cluster transport protocol address string */
   protected String nodeName;
   /** me as a ClusterNode */
   protected ClusterNode me = null;
   /** The timeout for cluster RPC calls */
   protected long timeout = 60000;
   /** The JGroups partition channel */
   protected JChannel channel;
   /** The cluster replicant manager */
   protected DistributedReplicantManagerImpl replicantManager;
   /** The cluster state manager */
   protected DistributedStateImpl dsManager;
   /** The cluster instance log category */
   protected Logger log;
   protected Logger clusterLifeCycleLog;   
   /** The current cluster view id */
   protected long currentViewId = -1;
   /** The JMX MBeanServer to use for registrations */
   protected MBeanServer server;
   /** Number of ms to wait for state */
   protected long state_transfer_timeout=60000;
   /** Whether to bind the partition into JNDI */
   protected boolean bindIntoJndi = true;

   /**
    * True if state was initialized during start-up.
    */
   protected boolean isStateSet = false;

   /**
    * An exception occuring upon fetch state.
    */
   protected Exception setStateException;
   private final Object stateLock = new Object();

   // Static --------------------------------------------------------
   
   /**
    * Creates an object from a byte buffer
    */
   public static Object objectFromByteBuffer (byte[] buffer) throws Exception
   {
      if(buffer == null) 
         return null;

      ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
      MarshalledValueInputStream mvis = new MarshalledValueInputStream(bais);
      return mvis.readObject();
   }
   
   /**
    * Serializes an object into a byte buffer.
    * The object has to implement interface Serializable or Externalizable
    */
   public static byte[] objectToByteBuffer (Object obj) throws Exception
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      MarshalledValueOutputStream mvos = new MarshalledValueOutputStream(baos);
      mvos.writeObject(obj);
      mvos.flush();
      return baos.toByteArray();
   }

   public long getStateTransferTimeout() {
      return state_transfer_timeout;
   }

   public void setStateTransferTimeout(long state_transfer_timeout) {
      this.state_transfer_timeout=state_transfer_timeout;
   }


   public long getMethodCallTimeout() {
      return timeout;
   }

   public void setMethodCallTimeout(long timeout) {
      this.timeout=timeout;
   }

    // Constructors --------------------------------------------------
       
   public HAPartitionImpl(String partitionName, org.jgroups.JChannel channel, boolean deadlock_detection, MBeanServer server) throws Exception
   {
      this(partitionName, channel, deadlock_detection);
      this.server = server;
   }
   
   public HAPartitionImpl(String partitionName, org.jgroups.JChannel channel, boolean deadlock_detection) throws Exception
   {
      super(channel, null, null, new Object(), deadlock_detection); // init RpcDispatcher with a fake target object
      this.log = Logger.getLogger(HAPartition.class.getName() + "." + partitionName);
      this.clusterLifeCycleLog = Logger.getLogger(HAPartition.class.getName() + ".lifecycle." + partitionName);
      this.channel = channel;
      this.partitionName = partitionName;
      this.history = new Vector();
      this.setMarshaller(new MarshallerImpl());
      logHistory ("Partition object created");
   }
   
    // Public --------------------------------------------------------
   
   public void init() throws Exception
   {
      log.info("Initializing");
      logHistory ("Initializing partition");

      // Subscribe to dHA events comming generated by the org.jgroups. protocol stack
      //
      log.debug("setMembershipListener");
      setMembershipListener(this);
      log.debug("setMessageListener");
      setMessageListener(this);
      
      // Create the DRM and link it to this HAPartition
      //
      log.debug("create replicant manager");
      this.replicantManager = new DistributedReplicantManagerImpl(this, this.server);
      log.debug("init replicant manager");
      this.replicantManager.init();
      log.debug("bind replicant manager");
      
      // Create the DS and link it to this HAPartition
      //
      log.debug("create distributed state");
      this.dsManager = new DistributedStateImpl(this, this.server);
      log.debug("init distributed state service");
      this.dsManager.init();
      log.debug("bind distributed state service");

      // Create the asynchronous handler for view changes
      asynchHandler = new AsynchEventHandler(this, "AsynchViewChangeHandler");
      
      log.debug("done initing.");
   }
   
   public void startPartition() throws Exception
   {
      // get current JG group properties
      //
      logHistory ("Starting partition");
      log.debug("get nodeName");
      this.localJGAddress = (IpAddress)channel.getLocalAddress();
      this.me = new ClusterNode(this.localJGAddress);
      this.nodeName = this.me.getName();

      log.debug("Get current members");
      View view = channel.getView();
      this.jgmembers = (Vector)view.getMembers().clone();
      this.members = translateAddresses(this.jgmembers); // TRANSLATE
      log.info("Number of cluster members: " + members.size());
      for(int m = 0; m > members.size(); m ++)
      {
         Object node = members.get(m);
         log.debug(node);
      }
      // Keep a list of other members only for "exclude-self" RPC calls
      //
      this.jgotherMembers = (Vector)view.getMembers().clone();
      this.jgotherMembers.remove (channel.getLocalAddress());
      this.otherMembers = translateAddresses(this.jgotherMembers); // TRANSLATE
      log.info ("Other members: " + this.otherMembers.size ());

      verifyNodeIsUnique (view.getMembers());

      // Update the initial view id
      //
      this.currentViewId = view.getVid().getId();

      // We must now synchronize new state transfer subscriber
      //
      fetchState();
      
      // We start now able to start our DRM and DS
      //
      this.replicantManager.start();
      this.dsManager.start();

      // Start the asynch listener handler thread
      asynchHandler.start();
      
      // Bind ourself in the public JNDI space if configured to do so
      if (!bindIntoJndi)
    	  return;
      
      Context ctx = new InitialContext();
      this.bind("/HAPartition/" + partitionName, this, HAPartitionImpl.class, ctx);
   }


   protected void fetchState() throws Exception
   {
      log.info("Fetching state (will wait for " + this.state_transfer_timeout + " milliseconds):");
      long start, stop;
      isStateSet = false;
      start = System.currentTimeMillis();
      boolean rc = channel.getState(null, this.state_transfer_timeout);
      if (rc)
      {
         synchronized (stateLock)
         {
            while (!isStateSet)
            {
               if (setStateException != null)
                  throw setStateException;

               try
               {
                  stateLock.wait();
               }
               catch (InterruptedException iex)
               {
               }
            }
         }
         stop = System.currentTimeMillis();
         log.info("state was retrieved successfully (in " + (stop - start) + " milliseconds)");
      }
      else
      {
         // No one provided us with state.
         // We need to find out if we are the coordinator, so we must
         // block until viewAccepted() is called at least once

         synchronized (members)
         {
            while (members.size() == 0)
            {
               log.debug("waiting on viewAccepted()");
               try
               {
                  members.wait();
               }
               catch (InterruptedException iex)
               {
               }
            }
         }

         if (isCurrentNodeCoordinator())
         {
            log.info("State could not be retrieved (we are the first member in group)");
         }
         else
         {
            throw new IllegalStateException("Initial state transfer failed: " +
               "Channel.getState() returned false");
         }
      }
   }

   public void closePartition() throws Exception
   {
      logHistory ("Closing partition");
      log.info("Closing partition " + partitionName);

      try
      {
         asynchHandler.stop();
      }
      catch( Exception e)
      {
         log.warn("Failed to stop asynchHandler", e);
      }

      // Stop the DRM and DS services
      //
      try
      {
         this.replicantManager.stop();
      }
      catch (Exception e)
      {
         log.error("operation failed", e);
      }

      try
      {
         this.dsManager.stop();
      }
      catch (Exception e)
      {
         log.error("operation failed", e);
      }

//    NR 200505 : [JBCLUSTER-38] replace channel.close() by a disconnect and
//    add the destroyPartition() step
      try
      {
//          channel.close();
          channel.disconnect();
      }
      catch (Exception e)
      {
         log.error("operation failed", e);
      }

      if (bindIntoJndi)
      {
         String boundName = "/HAPartition/" + partitionName;
         InitialContext ctx = new InitialContext();
         try
         {
            ctx.unbind(boundName);
         }
         finally
         {
            ctx.close();
         }
         NonSerializableFactory.unbind (boundName);    	  
      }
      
      log.info("Partition " + partitionName + " closed.");
   }

// NR 200505 : [JBCLUSTER-38] destroy partition close the channel
   public void destroyPartition()  throws Exception
   {

      try
      {
         this.replicantManager.destroy();
      }
      catch (Exception e)
      {
         log.error("operation failed", e);
      }

      try
      {
         this.dsManager.destroy();
      }
      catch (Exception e)
      {
         log.error("operation failed", e);
      }
      try
      {
         channel.close();
      }
      catch (Exception e)
      {
         log.error("operation failed", e);
      }

      log.info("Partition " + partitionName + " destroyed.");
  }
   // org.jgroups.MessageListener implementation ----------------------------------------------

   // MessageListener methods
   //
   public byte[] getState()
   {
      logHistory ("getState called on partition");
      boolean debug = log.isDebugEnabled();
      
      log.debug("getState called.");
      try
      {
         // we now get the sub-state of each HAPartitionStateTransfer subscribers and
         // build a "macro" state
         //
         HashMap state = new HashMap();
         Iterator keys = stateHandlers.keySet().iterator();
         while (keys.hasNext())
         {
            String key = (String)keys.next();
            HAPartition.HAPartitionStateTransfer subscriber = (HAPartition.HAPartitionStateTransfer)stateHandlers.get(key);
            if (debug)
               log.debug("getState for " + key);
            state.put(key, subscriber.getCurrentState());
         }
         return objectToByteBuffer(state);
      }
      catch (Exception ex)
      {
         log.error("getState failed", ex);
      }
      return null;
   }
   
   public void setState(byte[] obj)
   {
      logHistory ("setState called on partition");
      try
      {
         log.debug("setState called");
         if (obj == null)
         {
            log.debug("state is null");
            return;
         }
         
         long used_mem_before, used_mem_after;
         int state_size=obj != null? obj.length : 0;
         Runtime rt=Runtime.getRuntime();
         used_mem_before=rt.totalMemory() - rt.freeMemory();

         HashMap state = (HashMap)objectFromByteBuffer(obj);
         java.util.Iterator keys = state.keySet().iterator();
         while (keys.hasNext())
         {
            String key = (String)keys.next();
            log.debug("setState for " + key);
            Object someState = state.get(key);
            HAPartition.HAPartitionStateTransfer subscriber = (HAPartition.HAPartitionStateTransfer)stateHandlers.get(key);
            if (subscriber != null)
            {
               try
               {
                  subscriber.setCurrentState((java.io.Serializable)someState);
               }
               catch (Exception e)
               {
                  // Don't let issues with one subscriber affect others
                  // unless it is DRM or DS, which are really internal 
                  // functions of the HAPartition
                  if (DistributedReplicantManagerImpl.SERVICE_NAME.equals(key) 
                        || DistributedStateImpl.SERVICE_NAME.equals(key))
                  {
                     if (e instanceof RuntimeException)
                        throw (RuntimeException) e;
                     else
                        throw new RuntimeException(e);
                  }
                  else
                  {
                     log.error("Caught exception setting state to " + subscriber, e);
                  }
               }
            }
            else
            {
               log.debug("There is no stateHandler for: " + key);
            }
         }

         used_mem_after=rt.totalMemory() - rt.freeMemory();
         log.debug("received a state of " + state_size + " bytes; expanded memory by " +
               (used_mem_after - used_mem_before) + " bytes (used memory before: " + used_mem_before +
               ", used memory after: " + used_mem_after + ")");
         
         isStateSet = true;
      }
      catch (Throwable t)
      {
         log.error("failed setting state", t);
         if (t instanceof Exception)
            setStateException = (Exception) t;
         else
            setStateException = new Exception(t);
      }
      finally
      {
         synchronized (stateLock)
         {
            // Notify wait that state has been set.
            stateLock.notifyAll();
         }
      }
   }
   
   public void receive(org.jgroups.Message msg)
   { /* complete */}
   
   // org.jgroups.MembershipListener implementation ----------------------------------------------
   
   public void suspect(org.jgroups.Address suspected_mbr)
   {      
      logHistory ("Node suspected: " + (suspected_mbr==null?"null":suspected_mbr.toString()));
      if (isCurrentNodeCoordinator ())
         clusterLifeCycleLog.info ("Suspected member: " + suspected_mbr);
      else
         log.info("Suspected member: " + suspected_mbr);
   }

   public void block() {}
   
   /** Notification of a cluster view change. This is done from the JG protocol
    * handlder thread and we must be careful to not unduly block this thread.
    * Because of this there are two types of listeners, synchronous and
    * asynchronous. The synchronous listeners are messaged with the view change
    * event using the calling thread while the asynchronous listeners are
    * messaged using a seperate thread.
    *
    * @param newView
    */
   public void viewAccepted(View newView)
   {
      try
      {
         // we update the view id
         //
         this.currentViewId = newView.getVid().getId();

         // Keep a list of other members only for "exclude-self" RPC calls
         //
         this.jgotherMembers = (Vector)newView.getMembers().clone();
         this.jgotherMembers.remove (channel.getLocalAddress());
         this.otherMembers = translateAddresses (this.jgotherMembers); // TRANSLATE!
         Vector translatedNewView = translateAddresses ((Vector)newView.getMembers().clone());
         logHistory ("New view: " + translatedNewView + " with viewId: " + this.currentViewId +
                     " (old view: " + this.members + " )");


         // Save the previous view and make a copy of the new view
         Vector oldMembers = this.members;

         Vector newjgMembers = (Vector)newView.getMembers().clone();
         Vector newMembers = translateAddresses(newjgMembers); // TRANSLATE
         if (this.members == null)
         {
            // Initial viewAccepted
            this.members = newMembers;
            this.jgmembers = newjgMembers;
            log.debug("ViewAccepted: initial members set");
            return;
         }
         this.members = newMembers;
         this.jgmembers = newjgMembers;

         int difference = 0;
         if (oldMembers == null)
            difference = newMembers.size () - 1;
         else
            difference = newMembers.size () - oldMembers.size ();
         
         if (isCurrentNodeCoordinator ())
            clusterLifeCycleLog.info ("New cluster view for partition " + this.partitionName + " (id: " +
                                      this.currentViewId + ", delta: " + difference + ") : " + this.members);
         else
            log.info("New cluster view for partition " + this.partitionName + ": " +
                     this.currentViewId + " (" + this.members + " delta: " + difference + ")");

         // Build a ViewChangeEvent for the asynch listeners
         ViewChangeEvent event = new ViewChangeEvent();
         event.viewId = currentViewId;
         event.allMembers = translatedNewView;
         event.deadMembers = getDeadMembers(oldMembers, event.allMembers);
         event.newMembers = getNewMembers(oldMembers, event.allMembers);
         event.originatingGroups = null;
         // if the new view occurs because of a merge, we first inform listeners of the merge
         if(newView instanceof MergeView)
         {
            MergeView mergeView = (MergeView) newView;
            event.originatingGroups = mergeView.getSubgroups();
         }

         log.debug("membership changed from " + 
                   (oldMembers == null ? 0 : oldMembers.size()) + " to " + 
                   event.allMembers.size());
         // Put the view change to the asynch queue
         this.asynchHandler.queueEvent(event);

         // Broadcast the new view to the synchronous view change listeners
         if (this.allowSyncListeners)
         {
            this.notifyListeners(synchListeners, event.viewId, event.allMembers,
                  event.deadMembers, event.newMembers, event.originatingGroups);
         }
      }
      catch (Exception ex)
      {
         log.error("ViewAccepted failed", ex);
      }
   }

   // HAPartition implementation ----------------------------------------------
   
   public String getNodeName()
   {
      return nodeName;
   }
   
   public String getPartitionName()
   {
      return partitionName;
   }
   
   public DistributedReplicantManager getDistributedReplicantManager()
   {
      return replicantManager;
   }
   
   public DistributedState getDistributedStateService()
   {
      return this.dsManager;
   }

   public long getCurrentViewId()
   {
      return this.currentViewId;
   }
   
   public Vector getCurrentView()
   {
      Vector result = new Vector (this.members.size());
      for (int i = 0; i < members.size(); i++)
      {
         result.add( ((ClusterNode) members.elementAt(i)).getName() );
      }
      return result;
   }

   public ClusterNode[] getClusterNodes ()
   {
      ClusterNode[] nodes = new ClusterNode[this.members.size()];
      this.members.toArray(nodes);
      return nodes;
   }

   public ClusterNode getClusterNode ()
   {
      return me;
   }

   public boolean isCurrentNodeCoordinator ()
   {
      if(this.members == null || this.members.size() == 0 || this.me == null)
         return false;
     return this.members.elementAt (0).equals (this.me);
   }

   // ***************************
   // ***************************
   // RPC multicast communication
   // ***************************
   // ***************************
   //
   public void registerRPCHandler(String objName, Object subscriber)
   {
      rpcHandlers.put(objName, subscriber);
   }
   
   public void unregisterRPCHandler(String objName, Object subscriber)
   {
      rpcHandlers.remove(objName);
   }
      

   /**
    *
    * @param objName
    * @param methodName
    * @param args
    * @param excludeSelf
    * @return
    * @throws Exception
    * @deprecated Use {@link #callMethodOnCluster(String,String,Object[],Class[], boolean)} instead
    */
   public ArrayList callMethodOnCluster(String objName, String methodName,
      Object[] args, boolean excludeSelf) throws Exception
   {
      return callMethodOnCluster(objName, methodName, args, null, excludeSelf);
   }

   /**
    * This function is an abstraction of RpcDispatcher.
    */
   public ArrayList callMethodOnCluster(String objName, String methodName,
      Object[] args, Class[] types, boolean excludeSelf) throws Exception
   {
      return callMethodOnCluster(objName, methodName, args, types, excludeSelf, this.timeout);
   }


   public ArrayList callMethodOnCluster(String objName, String methodName,
       Object[] args, Class[] types, boolean excludeSelf, long methodTimeout) throws Exception
   {
      ArrayList rtn = new ArrayList();
      MethodCall m=null;
      RspList rsp = null;
      boolean trace = log.isTraceEnabled();

      if(types != null)
         m=new MethodCall(objName + "." + methodName, args, types);
      else
         m=new MethodCall(objName + "." + methodName, args);

      if (excludeSelf)
      {
         if( trace )
         {
            log.trace("callMethodOnCluster(true), objName="+objName
               +", methodName="+methodName+", members="+jgotherMembers);
         }
         rsp = this.callRemoteMethods(this.jgotherMembers, m, GroupRequest.GET_ALL, methodTimeout);
      }
      else
      {
         if( trace )
         {
            log.trace("callMethodOnCluster(false), objName="+objName
               +", methodName="+methodName+", members="+members);
         }
         rsp = this.callRemoteMethods(null, m, GroupRequest.GET_ALL, methodTimeout);
      }

      if (rsp != null)
      {
         for (int i = 0; i < rsp.size(); i++)
         {
            Object item = rsp.elementAt(i);
            if (item instanceof Rsp)
            {
               Rsp response = (Rsp) item;
               // Only include received responses
               boolean wasReceived = response.wasReceived();
               if( wasReceived == true )
               {
                  item = response.getValue();
                  if (!(item instanceof NoHandlerForRPC))
                     rtn.add(item);
               }
               else if( trace )
                  log.trace("Ignoring non-received response: "+response);
            }
            else
            {
               if (!(item instanceof NoHandlerForRPC))
                  rtn.add(item);
               else if( trace )
                  log.trace("Ignoring NoHandlerForRPC");
            }
         }
      }

      return rtn;
    }

   /**
    * Calls method on Cluster coordinator node only.  The cluster coordinator node is the first node to join the
    * cluster.
    * and is replaced
    * @param objName
    * @param methodName
    * @param args
    * @param types
    * @param excludeSelf
    * @return
    * @throws Exception
    */
   public ArrayList callMethodOnCoordinatorNode(String objName, String methodName,
          Object[] args, Class[] types,boolean excludeSelf) throws Exception
      {
      return callMethodOnCoordinatorNode(objName,methodName,args,types,excludeSelf,this.timeout);
      }

   /**
    * Calls method on Cluster coordinator node only.  The cluster coordinator node is the first node to join the
    * cluster.
    * and is replaced
    * @param objName
    * @param methodName
    * @param args
    * @param types
    * @param excludeSelf
    * @param methodTimeout
    * @return
    * @throws Exception
    */
   public ArrayList callMethodOnCoordinatorNode(String objName, String methodName,
          Object[] args, Class[] types,boolean excludeSelf, long methodTimeout) throws Exception
      {
         ArrayList rtn = new ArrayList();
         MethodCall m=null;
         RspList rsp = null;
         boolean trace = log.isTraceEnabled();

         if(types != null)
            m=new MethodCall(objName + "." + methodName, args, types);
         else
            m=new MethodCall(objName + "." + methodName, args);

         if( trace )
         {
            log.trace("callMethodOnCoordinatorNode(false), objName="+objName
               +", methodName="+methodName);
         }

         // the first cluster view member is the coordinator
         Vector coordinatorOnly = new Vector();
         // If we are the coordinator, only call ourself if 'excludeSelf' is false
         if (false == isCurrentNodeCoordinator () ||
             false == excludeSelf)
            coordinatorOnly.addElement(this.jgmembers.elementAt (0));

         rsp = this.callRemoteMethods(coordinatorOnly, m, GroupRequest.GET_ALL, methodTimeout);

         if (rsp != null)
         {
            for (int i = 0; i < rsp.size(); i++)
            {
               Object item = rsp.elementAt(i);
               if (item instanceof Rsp)
               {
                  Rsp response = (Rsp) item;
                  // Only include received responses
                  boolean wasReceived = response.wasReceived();
                  if( wasReceived == true )
                  {
                     item = response.getValue();
                     if (!(item instanceof NoHandlerForRPC))
                        rtn.add(item);
                  }
                  else if( trace )
                     log.trace("Ignoring non-received response: "+response);
               }
               else
               {
                  if (!(item instanceof NoHandlerForRPC))
                     rtn.add(item);
                  else if( trace )
                     log.trace("Ignoring NoHandlerForRPC");
               }
            }
         }

         return rtn;
       }


   /**
    *
    * @param objName
    * @param methodName
    * @param args
    * @param excludeSelf
    * @throws Exception
    * @deprecated Use {@link #callAsynchMethodOnCluster(String, String, Object[], Class[], boolean)} instead
    */
   public void callAsynchMethodOnCluster(String objName, String methodName,
      Object[] args, boolean excludeSelf) throws Exception {
      callAsynchMethodOnCluster(objName, methodName, args, null, excludeSelf);
   }





   /**
    * This function is an abstraction of RpcDispatcher for asynchronous messages
    */
   public void callAsynchMethodOnCluster(String objName, String methodName,
      Object[] args, Class[] types, boolean excludeSelf) throws Exception
   {
      MethodCall m = null;
      boolean trace = log.isTraceEnabled();

      if(types != null)
         m=new MethodCall(objName + "." + methodName, args, types);
      else
         m=new MethodCall(objName + "." + methodName, args);

      if (excludeSelf)
      {
         if( trace )
         {
            log.trace("callAsynchMethodOnCluster(true), objName="+objName
               +", methodName="+methodName+", members="+jgotherMembers);
         }
         this.callRemoteMethods(this.jgotherMembers, m, GroupRequest.GET_NONE, timeout);
      }
      else
      {
         if( trace )
         {
            log.trace("callAsynchMethodOnCluster(false), objName="+objName
               +", methodName="+methodName+", members="+members);
         }
         this.callRemoteMethods(null, m, GroupRequest.GET_NONE, timeout);
      }
   }
   
   // *************************
   // *************************
   // State transfer management
   // *************************
   // *************************
   //      
   public void subscribeToStateTransferEvents(String objectName, HAPartitionStateTransfer subscriber)
   {
      stateHandlers.put(objectName, subscriber);
   }
   
   public void unsubscribeFromStateTransferEvents(String objectName, HAPartitionStateTransfer subscriber)
   {
      stateHandlers.remove(objectName);
   }
   
   // *************************
   // *************************
   // Group Membership listeners
   // *************************
   // *************************
   //   
   public void registerMembershipListener(HAMembershipListener listener)
   {
      boolean isAsynch = (this.allowSyncListeners == false) 
            || (listener instanceof AsynchHAMembershipListener)
            || (listener instanceof AsynchHAMembershipExtendedListener);
      if( isAsynch ) {
         synchronized(this.asynchListeners) {
            this.asynchListeners.add(listener);
         }
      }
      else  { 
         synchronized(this.synchListeners) {
            this.synchListeners.add(listener);
         }
      }
   }
   
   public void unregisterMembershipListener(HAMembershipListener listener)
   {
      boolean isAsynch = (this.allowSyncListeners == false) 
            || (listener instanceof AsynchHAMembershipListener)
            || (listener instanceof AsynchHAMembershipExtendedListener);
      if( isAsynch ) {
         synchronized(this.asynchListeners) {
            this.asynchListeners.remove(listener);
         }
      }
      else  { 
         synchronized(this.synchListeners) {
            this.synchListeners.remove(listener);
         }
      }
   }
   
   public boolean getAllowSynchronousMembershipNotifications()
   {
      return allowSyncListeners;
   }

   public void setAllowSynchronousMembershipNotifications(boolean allowSync)
   {      
      this.allowSyncListeners = allowSync;
   }
   
   // org.jgroups.RpcDispatcher overrides ---------------------------------------------------
   
   /**
    * Message contains MethodCall. Execute it against *this* object and return result.
    * Use MethodCall.Invoke() to do this. Return result.
    *
    * This overrides RpcDispatcher.Handle so that we can dispatch to many different objects.
    * @param req The org.jgroups. representation of the method invocation
    * @return The serializable return value from the invocation
    */
   public Object handle(Message req)
   {
      Object body = null;
      Object retval = null;
      MethodCall  method_call = null;
      boolean trace = log.isTraceEnabled();
      
      if( trace )
         log.trace("Partition " + partitionName + " received msg");
      if(req == null || req.getBuffer() == null)
      {
         log.warn("message or message buffer is null !");
         return null;
      }
      
      try
      {
         body = objectFromByteBuffer(req.getBuffer());
      }
      catch(Exception e)
      {
         log.warn("failed unserializing message buffer (msg=" + req + ")", e);
         return null;
      }
      
      if(body == null || !(body instanceof MethodCall))
      {
         log.warn("message does not contain a MethodCall object !");
         return null;
      }
      
      // get method call informations
      //
      method_call = (MethodCall)body;
      String methodName = method_call.getName();      
      
      if( trace )
         log.trace("pre methodName: " + methodName);
      
      int idx = methodName.lastIndexOf('.');
      String handlerName = methodName.substring(0, idx);
      String newMethodName = methodName.substring(idx + 1);
      
      if( trace ) 
      {
         log.trace("handlerName: " + handlerName + " methodName: " + newMethodName);
         log.trace("Handle: " + methodName);
      }
      
      // prepare method call
      method_call.setName(newMethodName);
      Object handler = rpcHandlers.get(handlerName);
      if (handler == null)
      {
         if( trace )
            log.debug("No rpc handler registered under: "+handlerName);
         return new NoHandlerForRPC();
      }

      /* Invoke it and just return any exception with trace level logging of
      the exception. The exception semantics of a group rpc call are weak as
      the return value may be a normal return value or the exception thrown.
      */
      try
      {
         retval = method_call.invoke(handler);
         if( trace )
            log.trace("rpc call return value: "+retval);
      }
      catch (Throwable t)
      {
         if( trace )
            log.trace("rpc call threw exception", t);
         retval = t;
      }
      
      return retval;
   }
   
   // AsynchEventHandler.AsynchEventProcessor -----------------------

   public void processEvent(Object event)
   {
      ViewChangeEvent vce = (ViewChangeEvent) event;
      notifyListeners(asynchListeners, vce.viewId, vce.allMembers,
            vce.deadMembers, vce.newMembers, vce.originatingGroups);
      
   }
   
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------

   protected void verifyNodeIsUnique (Vector javaGroupIpAddresses) throws Exception
   {
      byte[] localUniqueName = this.localJGAddress.getAdditionalData();
      if (localUniqueName == null)
      {
         log.warn("No additional information has been found in the JavaGroup address: " +
                  "make sure you are running with a correct version of JGroups and that the protocol " +
                  " you are using supports the 'additionalData' behaviour");
         return;
      }

      for (int i = 0; i < javaGroupIpAddresses.size(); i++)
      {
         IpAddress address = (IpAddress) javaGroupIpAddresses.elementAt(i);
         if (!address.equals(this.localJGAddress))
         {
            if (localUniqueName.equals(address.getAdditionalData()))
               throw new Exception ("Local node removed from cluster (" + this.localJGAddress + "): another node (" + address + ") publicizing the same name was already there");
         }
      }
   }

   /**
    * Helper method that binds the partition in the JNDI tree.
    * @param jndiName Name under which the object must be bound
    * @param who Object to bind in JNDI
    * @param classType Class type under which should appear the bound object
    * @param ctx Naming context under which we bind the object
    * @throws Exception Thrown if a naming exception occurs during binding
    */   
   protected void bind(String jndiName, Object who, Class classType, Context ctx) throws Exception
   {
      // Ah ! This service isn't serializable, so we use a helper class
      //
      NonSerializableFactory.bind(jndiName, who);
      Name n = ctx.getNameParser("").parse(jndiName);
      while (n.size () > 1)
      {
         String ctxName = n.get (0);
         try
         {
            ctx = (Context)ctx.lookup (ctxName);
         }
         catch (NameNotFoundException e)
         {
            log.debug ("creating Subcontext" + ctxName);
            ctx = ctx.createSubcontext (ctxName);
         }
         n = n.getSuffix (1);
      }

      // The helper class NonSerializableFactory uses address type nns, we go on to
      // use the helper class to bind the service object in JNDI
      //
      StringRefAddr addr = new StringRefAddr("nns", jndiName);
      Reference ref = new Reference(classType.getName (), addr, NonSerializableFactory.class.getName (), null);
      ctx.rebind (n.get (0), ref);
   }
   
   /**
    * Helper method that returns a vector of dead members from two input vectors: new and old vectors of two views.
    * Dead members are old - new members.
    * @param oldMembers Vector of old members
    * @param newMembers Vector of new members
    * @return Vector of members that have died between the two views, can be empty.
    */   
   protected Vector getDeadMembers(Vector oldMembers, Vector newMembers)
   {
      boolean debug = log.isDebugEnabled();
      if(oldMembers == null) oldMembers=new Vector();
      if(newMembers == null) newMembers=new Vector();
      Vector dead=(Vector)oldMembers.clone();
      dead.removeAll(newMembers);
      if(dead.size() > 0 && debug)
         log.debug("dead members: " + dead);
      return dead;
   }
   
   /**
    * Helper method that returns a vector of new members from two input vectors: new and old vectors of two views.
    * @param oldMembers Vector of old members
    * @param allMembers Vector of new members
    * @return Vector of members that have joined the partition between the two views
    */   
   protected Vector getNewMembers(Vector oldMembers, Vector allMembers)
   {
      if(oldMembers == null) oldMembers=new Vector();
      if(allMembers == null) allMembers=new Vector();
      Vector newMembers=(Vector)allMembers.clone();
      newMembers.removeAll(oldMembers);
      return newMembers;
   }

   protected void notifyListeners(ArrayList theListeners, long viewID,
      Vector allMembers, Vector deadMembers, Vector newMembers,
      Vector originatingGroups)
   {
      log.debug("Begin notifyListeners, viewID: "+viewID);
      
      synchronized(theListeners)
      {
         // JBAS-3619 -- don't hold synch lock while notifying
         theListeners = (ArrayList) theListeners.clone();
      }
      
      for (int i = 0; i < theListeners.size(); i++)
      {
         HAMembershipListener aListener = null;
         try
         {
            aListener = (HAMembershipListener) theListeners.get(i);
            if(originatingGroups != null && (aListener instanceof HAMembershipExtendedListener))
            {
               HAMembershipExtendedListener exListener = (HAMembershipExtendedListener) aListener;
               exListener.membershipChangedDuringMerge (deadMembers, newMembers,
                  allMembers, originatingGroups);
            }
            else
            {
               aListener.membershipChanged(deadMembers, newMembers, allMembers);
            }
         }
         catch (Throwable e)
         {
            // a problem in a listener should not prevent other members to receive the new view
            log.warn("HAMembershipListener callback failure: "+aListener, e);
         }
      }
      log.debug("End notifyListeners, viewID: "+viewID);
   }
   
   /*
    * Allows caller to specify whether the partition instance should be bound into JNDI.  Default value is true.
    * This method must be called before the partition is started as the binding occurs during startup.
    * 
    * @param bind  Whether to bind the partition into JNDI.
    */
   public void setBindIntoJndi(boolean bind)
   {
	   bindIntoJndi = bind;
   }
   
   /*
    * Allows caller to determine whether the partition instance has been bound into JNDI.
    * 
    * @return true if the partition has been bound into JNDI.
    */
   public boolean getBindIntoJndi()
   {
	   return bindIntoJndi;
   }
   
   protected Vector translateAddresses (Vector jgAddresses)
   {
      if (jgAddresses == null)
         return null;

      Vector result = new Vector (jgAddresses.size());
      for (int i = 0; i < jgAddresses.size(); i++)
      {
         IpAddress addr = (IpAddress) jgAddresses.elementAt(i);
         result.add(new ClusterNode (addr));
      }

      return result;
   }

   public void logHistory (String message)
   {
      try
      {
         history.add(new SimpleDateFormat().format (new Date()) + " : " + message);
      }
      catch (Exception ignored){}
   }

   /** A simple data class containing the view change event needed to
    * message the HAMembershipListeners
    */
   private static class ViewChangeEvent
   {
      long viewId;
      Vector deadMembers;
      Vector newMembers;
      Vector allMembers;
      Vector originatingGroups;
   }
   
   private class MarshallerImpl implements org.jgroups.blocks.RpcDispatcher.Marshaller
   {

      public Object objectFromByteBuffer(byte[] buf) throws Exception
      {
         return HAPartitionImpl.objectFromByteBuffer(buf);
      }

      public byte[] objectToByteBuffer(Object obj) throws Exception
      {
         return HAPartitionImpl.objectToByteBuffer(obj);
      }
      
   }

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------

}
