/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
import java.rmi.dgc.VMID;
import java.rmi.server.UID;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ReflectionException;

import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.naming.NamingServiceMBean;
import org.jboss.system.ServiceMBean;
import org.jboss.system.server.ServerConfigUtil;
import org.jgroups.Channel;
import org.jgroups.Event;
import org.jgroups.mux.MuxChannel;
import org.jgroups.stack.IpAddress;

/**
 * Extension to the JGroups JChannelFactory that supports the addition
 * of "additional_data" to the channel config.  Needed until logical
 * addresses are supported in JGroups.
 * 
 * @author <a href="mailto://brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision$
 */
public class JChannelFactory extends org.jgroups.jmx.JChannelFactory
{
   protected static Logger log = Logger.getLogger(JChannelFactory.class);
   
   private InetAddress nodeAddress;
   private String nodeName;

   /**
    * Overrides the superclass version by generating a unique node id
    * and passing it down the Channel as additional_data.
    */
   public Channel createMultiplexerChannel(String stack_name, String id, boolean register_for_state_transfer, String substate_id) throws Exception
   {
      Channel channel = super.createMultiplexerChannel(stack_name, id, register_for_state_transfer, substate_id);
      
      setChannelUniqueId(channel);
      
      return channel;
   }
   
   /**
    * Overrides the superclass version by generating a unique node id
    * and passing it down the Channel as additional_data.
    */
   public Channel createMultiplexerChannel(String stack_name, String id) throws Exception
   {
      Channel channel = super.createMultiplexerChannel(stack_name, id);
      
      setChannelUniqueId(channel);
      
      return channel;
   }

   public InetAddress getNodeAddress()
   {
      return nodeAddress;
   }


   public void setNodeAddress(InetAddress nodeAddress)
   {
      this.nodeAddress = nodeAddress;
   }


   public String getNodeName()
   {
      return nodeName;
   }


   public void setNodeName(String nodeName)
   {
      this.nodeName = nodeName;
   }

   private void setChannelUniqueId(Channel channel) throws Exception
   {
      IpAddress address = (IpAddress) channel.getLocalAddress();
      if (address == null)
      {
         // We push the independent name in the protocol stack before connecting to the cluster
         if (this.nodeName == null || "".equals(this.nodeName)) {
            this.nodeName = generateUniqueNodeName();
         }
         
         log.debug("Passing unique node id " + nodeName + " to the channel as additional data");
         
         java.util.HashMap staticNodeName = new java.util.HashMap();
         staticNodeName.put("additional_data", this.nodeName.getBytes());
         channel.down(new Event(Event.CONFIG, staticNodeName));
         
      }
      else if (address.getAdditionalData() == null)
      {
         Channel testee = channel;
         if (channel instanceof MuxChannel)
         {
            testee = ((MuxChannel) channel).getChannel();
         }
         
         if (testee.isConnected())
         {
            throw new IllegalStateException("Underlying JChannel was " +
                    "connected before additional_data was set");
         }
      }
      else if (this.nodeName == null || "".equals(this.nodeName))
      {         
         this.nodeName = new String(address.getAdditionalData());
         log.warn("Field nodeName was not set but mux channel already had " +
                "additional data -- setting nodeName to " + nodeName);
      }
   }

   private String generateUniqueNodeName () throws Exception
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
         MBeanServer server = MBeanServerLocator.locateJBoss();
         AttributeList al = server.getAttributes(NamingServiceMBean.OBJECT_NAME,
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
   
}
