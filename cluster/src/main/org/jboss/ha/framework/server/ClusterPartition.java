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


import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.rmi.dgc.VMID;
import java.rmi.server.UID;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.naming.NamingServiceMBean;
import org.jboss.system.ServiceMBean;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigUtil;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.Version;
import org.jgroups.jmx.JChannelFactoryMBean;
import org.jgroups.jmx.JmxConfigurator;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** 
 *   Management Bean for Cluster HAPartitions.  It will start a JGroups
 *   channel and initialize the ReplicantManager and DistributedStateService.
 *
 *   @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>.
 *   @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 *   @version $Revision: 67538 $
 */
public class ClusterPartition
   extends ServiceMBeanSupport 
   implements ClusterPartitionMBean
{
   // Constants -----------------------------------------------------

   public static final String JGROUPS_JMX_DOMAIN = "jboss.jgroups";
   public static final String CHANNEL_JMX_ATTRIBUTES = "type=channel,cluster=";
   public static final String PROTOCOL_JMX_ATTRIBUTES = "type=protocol,cluster=";
   
   // Attributes ----------------------------------------------------
   
   protected String partitionName = ServerConfigUtil.getDefaultPartitionName();
   protected String jgProps =
      "UDP(mcast_addr=228.1.2.3;mcast_port=45566;ip_ttl=64;" +
      "mcast_send_buf_size=150000;mcast_recv_buf_size=80000):" +
      "PING(timeout=2000;num_initial_members=3):" +
      "MERGE2(min_interval=5000;max_interval=10000):" +
      "FD:" +
      "VERIFY_SUSPECT(timeout=1500):" +
      "pbcast.NAKACK(gc_lag=50;retransmit_timeout=300,600,1200,2400,4800;max_xmit_size=8192):" +
      "UNICAST(timeout=600,1200,2400):" +
      "pbcast.STABLE(desired_avg_gossip=20000):" +
      "FRAG(down_thread=false;up_thread=false;frag_size=8192):" +
      "pbcast.GMS(join_timeout=5000;join_retry_timeout=2000;" +
      "shun=false;print_local_addr=true):" +
      "pbcast.STATE_TRANSFER";
   
   protected HAPartitionImpl partition;
   protected boolean deadlock_detection = false;
   protected boolean allow_sync_events = false;
   protected JChannelFactoryMBean multiplexer = null;
   protected String stackName = null;
   protected org.jgroups.JChannel channel;
   protected Object debugger=null;
   protected boolean use_debugger=false;

   protected String nodeName = null;
   protected InetAddress nodeAddress = null;

   /** Number of milliseconds to wait until state has been transferred. Increase this value for large states
    * 0 = wait forever
    */
   protected long state_transfer_timeout=60000;


   protected long method_call_timeout=60000;
   
   protected boolean channelRegistered;   
   protected boolean protocolsRegistered;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   // ClusterPartitionMBean implementation ----------------------------------------------
   
   public String getPartitionName()
   {
      return partitionName;
   }

   public void setPartitionName(String newName)
   {
      partitionName = newName;
   }

   public String getPartitionProperties()
   {
      // The channel knows best
      if (channel != null)
         return channel.getProperties();
      
      if (multiplexer == null && stackName == null)
         return jgProps;
      
      // We are configured for the multiplexer but don't know
      // the details of the stack yet
      return null;
   }

   public void setPartitionProperties(String newProps)
   {
      jgProps = newProps;
   }

   /** Convert a list of elements to the JG property string
    */
   public void setPartitionConfig(Element config)
   {
      StringBuffer buffer = new StringBuffer();
      NodeList stack = config.getChildNodes();
      int length = stack.getLength();
      for(int s = 0; s < length; s ++)
      {
         Node node = stack.item(s);
         if( node.getNodeType() != Node.ELEMENT_NODE )
            continue;

         Element tag = (Element) node;
         String protocol = tag.getTagName();
         buffer.append(protocol);
         NamedNodeMap attrs = tag.getAttributes();
         int attrLength = attrs.getLength();
         if( attrLength > 0 )
            buffer.append('(');
         for(int a = 0; a < attrLength; a ++)
         {
            Attr attr = (Attr) attrs.item(a);
            String name = attr.getName();
            String value = attr.getValue();
            buffer.append(name);
            buffer.append('=');
            buffer.append(value);
            if( a < attrLength-1 )
               buffer.append(';');
         }
         if( attrLength > 0 )
            buffer.append(')');
         buffer.append(':');
      }
      // Remove the trailing ':'
      buffer.setLength(buffer.length()-1);
      this.jgProps = buffer.toString();
      log.debug("Setting JGProps from xml to: "+jgProps);
   }

   /**
    * Uniquely identifies this node. MUST be unique accros the whole cluster!
    * Cannot be changed once the partition has been started
    */
   public String getNodeName()
   {
      return this.nodeName;
   }

   public void setNodeName(String node) throws Exception
   {
      if (this.getState() == ServiceMBean.CREATED ||
          this.getState() == ServiceMBean.STARTED ||
          this.getState() == ServiceMBean.STARTING)
      {
         throw new Exception ("Node name cannot be changed once the partition has been started");
      }
      else
      {
         this.nodeName = node;
      }
   }

   public InetAddress getNodeAddress()
   {
      return nodeAddress;
   }
   
   public void setNodeAddress(InetAddress address)
   {
      this.nodeAddress = address;
   }
   
   public String getJGroupsVersion()
   {
      return Version.version + "( " + Version.cvs + ")";
   }

   public long getStateTransferTimeout()
   {
      return state_transfer_timeout;
   }

   public void setStateTransferTimeout(long timeout)
   {
      this.state_transfer_timeout=timeout;
   }

   public long getMethodCallTimeout() {
      return method_call_timeout;
   }

   public void setMethodCallTimeout(long timeout) {
      this.method_call_timeout=timeout;
   }

   public JChannelFactoryMBean getMultiplexer()
   {
      return multiplexer;
   }
   
   public void setMultiplexer(JChannelFactoryMBean muxFactory)
   {
      this.multiplexer = muxFactory;
   }

   public String getMultiplexerStack()
   {
      return stackName;
   }
   
   public void setMultiplexerStack(String stackName)
   {
      this.stackName = stackName;
   }

//   public boolean getChannelDebugger()
//   {
//      return this.use_debugger;
//   }
//
//   public void setChannelDebugger(boolean flag)
//   {
//      this.use_debugger=flag;
//   }

   public boolean getDeadlockDetection()
   {
      return deadlock_detection;
   }

   public void setDeadlockDetection(boolean doit)
   {
      deadlock_detection = doit;
   }

   public boolean getAllowSynchronousMembershipNotifications()
   {
      return allow_sync_events;
   }

   public void setAllowSynchronousMembershipNotifications(boolean allowSync)
   {
      this.allow_sync_events = allowSync;
   }

   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws MalformedObjectNameException
   {
      return name == null ? OBJECT_NAME : name;
   }
   
   public HAPartition getHAPartition ()
   {
      return this.partition;      
   }

   /** Return the list of member nodes that built from the current view
    * @return A Vector Strings representing the host:port values of the nodes
    */
   public Vector getCurrentView()
   {
      return partition.getCurrentView();
   }

   // ServiceMBeanSupport overrides ---------------------------------------------------
   
   public String getName()
   {
      return partitionName;
   }


   protected void createService()
      throws Exception
   {
      log.debug("Creating JGroups JChannel");

      if (stackName != null && multiplexer != null)
      {
         this.channel = (JChannel) multiplexer.createMultiplexerChannel(stackName, getPartitionName());
      }
      else
      {
         this.channel = new org.jgroups.JChannel(jgProps);
         // JBAS-4406 Hack to register the channel
         registerChannelInJmx();
      }
      
      if(use_debugger && debugger == null)
      {
         startChannelDebugger();
      }
      channel.setOpt(Channel.AUTO_RECONNECT, Boolean.TRUE);
      channel.setOpt(Channel.AUTO_GETSTATE, Boolean.TRUE);
      
      log.debug("Creating HAPartition");
      partition = createPartition();
      
      // JBAS-2769 Init partition in create
      log.debug("Initing HAPartition: " + partition);
      partition.init();
      log.debug("HAPartition initialized");
      
   }
   
   /**
    * Extension point meant for test cases; instantiates the HAPartitionImpl.
    * Test cases can instantiate their own subclass of HAPartitionImpl.
    */
   protected HAPartitionImpl createPartition() throws Exception
   {
      HAPartitionImpl result = new HAPartitionImpl(partitionName, channel, deadlock_detection, getServer());
      result.setStateTransferTimeout(this.state_transfer_timeout);
      result.setMethodCallTimeout(this.method_call_timeout);
      return result;
   }

   protected void startService() 
      throws Exception
   {
      // We push the independant name in the protocol stack
      // before it is connected to the cluster
      //
      if (this.nodeName == null || "".equals(this.nodeName))
         this.nodeName = generateUniqueNodeName ();

      java.util.HashMap staticNodeName = new java.util.HashMap();
      staticNodeName.put("additional_data", this.nodeName.getBytes());
      
      // JBAS-4258 -- invoke via reflection to allow upgrade to JGroups 2.5
      Class[] paramTypes = new Class[]{org.jgroups.Event.class};
      Method downMethod = JChannel.class.getDeclaredMethod("down", paramTypes);
      Object[] params = { new org.jgroups.Event(org.jgroups.Event.CONFIG, staticNodeName) };
      downMethod.invoke(channel, params);

      this.channel.getProtocolStack().flushEvents(); // temporary fix for JG bug (808170) TODO: REMOVE ONCE JGROUPS IS FIXED

      log.debug("Starting ClusterPartition: " + partitionName);
      channel.connect(partitionName);
      
      try
      {
         log.debug("Starting channel");
         partition.startPartition();

         log.debug("Started ClusterPartition: " + partitionName);         
      }
      catch (Exception e)
      {
         log.debug("Caught exception after channel connected; closing channel -- " + e.getLocalizedMessage());
         channel.disconnect();
         throw e;
      }
   }
   
   protected void stopService() throws Exception
   {
      stopChannelDebugger();
      log.debug("Stopping ClusterPartition: " + partitionName);
      partition.closePartition();
      log.debug("Stopped ClusterPartition: " + partitionName);
   }

   // NR 200505 : [JBCLUSTER-38] close partition just disconnect from channel
   // destroy close it.
   protected void destroyService() throws Exception
   {
      log.debug("Destroying ClusterPartition: " + partitionName);
      partition.destroyPartition();
      // JBAS-4406 Hack
      unregisterChannelFromJmx();
      log.debug("Destroyed ClusterPartition: " + partitionName);
   }
    
   
   protected String generateUniqueNodeName () throws Exception
   {
      // we first try to find a simple meaningful name:
      // 1st) "local-IP:JNDI_PORT" if JNDI is running on this machine
      // 2nd) "local-IP:JMV_GUID" otherwise
      // 3rd) return a fully GUID-based representation
      //

      // Before anything we determine the local host IP (and NOT name as this could be
      // resolved differently by other nodes...)

      // But use the specified node address for multi-homing

      String hostIP = null;
      InetAddress address = ServerConfigUtil.fixRemoteAddress(nodeAddress);
      if (address == null)
      {
         log.debug ("unable to create a GUID for this cluster, check network configuration is correctly setup (getLocalHost has returned an exception)");
         log.debug ("using a full GUID strategy");
         return new VMID().toString();
      }
      else
      {
         hostIP = address.getHostAddress();
      }

      // 1st: is JNDI up and running?
      //
      try
      {
         AttributeList al = this.server.getAttributes(NamingServiceMBean.OBJECT_NAME,
                                      new String[] {"State", "Port"});

         int status = ((Integer)((Attribute)al.get(0)).getValue()).intValue();
         if (status == ServiceMBean.STARTED)
         {
            // we can proceed with the JNDI trick!
            int port = ((Integer)((Attribute)al.get(1)).getValue()).intValue();
            return hostIP + ":" + port;
         }
         else
         {
            log.debug("JNDI has been found but the service wasn't started so we cannot " +
                      "be entirely sure we are the only one that wants to use this PORT " +
                      "as a GUID on this host.");
         }

      }
      catch (InstanceNotFoundException e)
      {
         log.debug ("JNDI not running here, cannot use this strategy to find a node GUID for the cluster");
      }
      catch (ReflectionException e)
      {
         log.debug ("JNDI querying has returned an exception, cannot use this strategy to find a node GUID for the cluster");
      }

      // 2nd: host-GUID strategy
      //
      String uid = new UID().toString();
      return hostIP + ":" + uid;
   }

   public String showHistory ()
   {
      StringBuffer buff = new StringBuffer();
      Vector data = new Vector (this.partition.history);
      for (java.util.Iterator row = data.iterator(); row.hasNext();)
      {
         String info = (String) row.next();
         buff.append(info).append("\n");
      }
      return buff.toString();
   }

   public String showHistoryAsXML ()
   {
      StringBuffer buff = new StringBuffer();
      buff.append("<events>\n");
      Vector data = new Vector (this.partition.history);
      for (java.util.Iterator row = data.iterator(); row.hasNext();)
      {
         buff.append("   <event>\n      ");
         String info = (String) row.next();
         buff.append(info);
         buff.append("\n   </event>\n");
      }
      buff.append("</events>\n");
      return buff.toString();
   }

   public void startChannelDebugger()
   {
      startChannelDebugger(false);
   }

   public void startChannelDebugger(boolean accumulative)
   {
      if(debugger == null)
      {
         try
         {
            Class clazz = getClass().getClassLoader().loadClass("org.jgroups.debug.Debugger");
            Constructor ctor = clazz.getConstructor(new Class[]
            {JChannel.class, boolean.class});
            debugger = ctor.newInstance(new Object[]
            {channel, Boolean.valueOf(accumulative)});
            Method start = clazz.getDeclaredMethod("start", new Class[] {});
            start.invoke(debugger, new Object[] {});
         }
         catch (Exception e)
         {
            log.error("Unable to start Debugger", e);
         }
      }
   }

   public void stopChannelDebugger()
   {
      if(debugger != null)
      {
         Class clazz = debugger.getClass();
         try
         {
            Method stop = clazz.getDeclaredMethod("stop", new Class[] {});
            stop.invoke(debugger, new Object[]{});
         }
         catch (Exception e)
         {
            log.error("Unable to cleanly stop Debugger", e);
         }
         debugger=null;
      }
   }
   
   protected void registerChannelInJmx()
   {
      if (server != null)
      {
         try
         {
            String protocolPrefix = JGROUPS_JMX_DOMAIN + ":" + PROTOCOL_JMX_ATTRIBUTES + getPartitionName();
            JmxConfigurator.registerProtocols(server, channel, protocolPrefix);
            protocolsRegistered = true;
            
            String name = JGROUPS_JMX_DOMAIN + ":" + CHANNEL_JMX_ATTRIBUTES + getPartitionName();
            JmxConfigurator.registerChannel(channel, server, name);
            channelRegistered = true;
         }
         catch (Exception e)
         {
            log.error("Caught exception registering channel in JXM", e);
         }
      }
   }
   
   protected void unregisterChannelFromJmx()
   {
      ObjectName on = null;
      if (channelRegistered)
      {          
         // Unregister the channel itself
         try
         {
            on = new ObjectName(JGROUPS_JMX_DOMAIN + ":" + CHANNEL_JMX_ATTRIBUTES + getPartitionName());
            server.unregisterMBean(on);
         }
         catch (Exception e)
         {
            if (on != null)
               log.error("Caught exception unregistering channel at " + on, e);
            else
               log.error("Caught exception unregistering channel", e);
         }
      }
      
      if (protocolsRegistered)
      {
         // Unregister the protocols
         try
         {
            on = new ObjectName(JGROUPS_JMX_DOMAIN + ":*," + PROTOCOL_JMX_ATTRIBUTES + getPartitionName());
            Set mbeans=server.queryNames(on, null);
            if(mbeans != null) {
                for(Iterator it=mbeans.iterator(); it.hasNext();) {
                    server.unregisterMBean((ObjectName)it.next());
                }
            }
         }
         catch (Exception e)
         {
            if (on != null)
               log.error("Caught exception unregistering protocols at " + on, e);
            else
               log.error("Caught exception unregistering protocols", e);
         }
      }
   }
}
