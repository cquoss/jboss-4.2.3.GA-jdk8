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

import java.net.InetAddress;
import java.util.Vector;

import javax.management.ObjectName;

import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.mx.util.ObjectNameFactory;
import org.jgroups.jmx.JChannelFactoryMBean;
import org.w3c.dom.Element;

/** 
 *   Management Bean for Cluster HAPartitions.  It will start a JGroups
 *   channel and initialize the ReplicantManager and DistributedStateService.
 *
 *   @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>.
 *   @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 *   @version $Revision: 60110 $
 *
 * <p><b>Revisions:</b><br>
 */

public interface ClusterPartitionMBean
   extends org.jboss.system.ServiceMBean
{
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss:service=ClusterPartition");

   /**
    * Name of the partition being built. All nodes/services belonging to 
    * a partition with the same name are clustered together.
    */
   String getPartitionName();
   void setPartitionName(String newName);

   /**
    * Get JGroups property string a la JDBC
    * see <a href="http://www.jgroups.com/">JGroups web site for more information</a>
    * 
    * @return the protocol stack used by this ClusterPartition, or <code>null</code>
    *         if the {@link #setMultiplexer(JChannelFactoryMBean) multiplexer} is
    *         configured but the channel has not been created yet.
    */
   String getPartitionProperties(); // i.e. JGroups properties
   void setPartitionProperties(String newProps);

   /** A write-only attribute that allows for an xml specification of the 
    *PartitionProperties string. For example, a string like:
    UDP(mcast_addr=228.1.2.3):PING(timeout=2000):MERGE2(min_interval=5000;max_interval=10000):FD"
    * would be specified in xml as:
    <JGProps>
    <UDP mcast_addr="228.1.2.3" />
    <PING timeout="2000" />
    <MERGE2 min_interval="5000" max_interval="10000" />
    <FD />
    </JGProps>    
    * <p/>
    * Ignored if the {@link #setMultiplexer(JChannelFactoryMBean) multiplexer} is
    * used.
    */
   void setPartitionConfig(Element config);

   /**
    * Uniquely identifies this node. MUST be unique accros the whole cluster!
    * Cannot be changed once the partition has been started (otherwise an exception is thrown)
    */
   String getNodeName();
   void setNodeName(String node) throws Exception;

   /**
    * The node address used to generate the node name
    */
   InetAddress getNodeAddress();
   void setNodeAddress(InetAddress address);

   /** The version of JGroups this is running on */
   String getJGroupsVersion();

   /* Number of milliseconds to wait until state has been transferred. Increase this value for large states
      0 = wait forever */
   long getStateTransferTimeout();

   void setStateTransferTimeout(long timeout);


   /** Max time (in ms) to wait for <em>synchronous</em> group method calls
    * ({@link HAPartition#callMethodOnCluster(String, String, Object[], Class[], boolean)}) */ 
   long getMethodCallTimeout();

   void setMethodCallTimeout(long timeout);
   
   /**
    * Gets the multiplexer channel factory that should be used to
    * create the JGroups channel.  
    * 
    * @return the multiplexed channel factory, or <code>null</code> if
    *         one isn't configured
    */
   JChannelFactoryMBean getMultiplexer();   
   
   /**
    * Sets the multiplexer channel factory that should be used to
    * create the JGroups channel.  If this and a 
    * {@link #setMultiplexerStack() multiplexer stack name} are set,
    * the channel will come from the multiplexer and properties
    * {@link #setPartitionConfig(Element) partitionConfig} and
    * {@link #setPartitionProperties(String) partitionProperties} will be
    * ingored.
    */
   void setMultiplexer(JChannelFactoryMBean muxFactory);

   /**
    * Gets the name of the protocol stack the multiplexer channel factory 
    * should use to create the JGroups channel.
    * 
    * @return the stack name, or <code>null</code>
    */
   String getMultiplexerStack();   
   
   /**
    * Sets the name of the protocol stack the multiplexer channel factory 
    * should use to create the JGroups channel.  If this and a 
    * {@link #setMultiplexer() multiplexed channel factory} are set,
    * the channel will come from the multiplexer and properties
    * {@link #setPartitionConfig(Element) partitionConfig} and
    * {@link #setPartitionProperties(String) partitionProperties} will be
    * ingored.
    * 
    * @param stackName the name of one of the protocols stacks included
    *                  in the multiplexed channel factor's configuration
    */
   void setMultiplexerStack(String stackName);


//   boolean getChannelDebugger();
//   void setChannelDebugger(boolean flag);


   /**
    * Determine if deadlock detection is enabled
    */
   boolean getDeadlockDetection();
   void setDeadlockDetection(boolean doit);
   
   /**
    * Returns whether this partition will synchronously notify any 
    * HAPartition.HAMembershipListener of membership changes using the 
    * calling thread from the underlying group communications layer
    * (e.g. JGroups).
    * 
    * @return <code>true</code> if registered listeners that don't implement
    *         <code>AsynchHAMembershipExtendedListener</code> or
    *         <code>AsynchHAMembershipListener</code> will be notified
    *         synchronously of membership changes; <code>false</code> if
    *         those listeners will be notified asynchronously.  Default
    *         is <code>false</code>.
    */
   public boolean getAllowSynchronousMembershipNotifications();
   /**
    * Sets whether this partition will synchronously notify any 
    * HAPartition.HAMembershipListener of membership changes using the  
    * calling thread from the underlying group communications layer
    * (e.g. JGroups).
    * 
    * @param allowSync  <code>true</code> if registered listeners that don't 
    *         implement <code>AsynchHAMembershipExtendedListener</code> or
    *         <code>AsynchHAMembershipListener</code> should be notified
    *         synchronously of membership changes; <code>false</code> if
    *         those listeners can be notified asynchronously.  Default
    *         is <code>false</code>.
    */
   public void setAllowSynchronousMembershipNotifications(boolean allowSync);

   /** Access to the underlying HAPartition without going through JNDI
    *
    * @return the HAPartition for the cluster service
    */
   HAPartition getHAPartition ();

   /** Return the list of member nodes that built from the current view
    * @return A Vector Strings representing the host:port values of the nodes
    */
   Vector getCurrentView();

   String showHistory ();

   String showHistoryAsXML ();

   void startChannelDebugger();
   void startChannelDebugger(boolean accumulative);
   void stopChannelDebugger();
}
