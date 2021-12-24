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
package org.jboss.test.cluster.partition.test;

import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.test.JBossClusteredTestCase;

/**
 * A PartitionRestartUnitTestCase.
 * 
 * @author <a href="mailto://brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision$
 */
public class JBAS4406UnitTestCase extends JBossClusteredTestCase
{
   public static final String DEPLOYMENT = "jbas4406-service.xml";
   /**
    * Create a new PartitionRestartUnitTestCase.
    * 
    * @param name
    */
   public JBAS4406UnitTestCase(String name)
   {
      super(name);
   }         

   public static Test suite() throws Exception
   {
      return getDeploySetup(JBAS4406UnitTestCase.class, DEPLOYMENT);
   }   
   
   public void testMBeanRegistration() 
   throws Exception
   {       
      getLog().debug("testMBeanRegistration");
      
      MBeanServerConnection[] adaptors = this.getAdaptors(); 
      
      Set channelMBean = getChannelMBeans(adaptors[0]);
      assertNotNull("Channel mbean registered", channelMBean);
      assertEquals("Only one channel mbean", 1, channelMBean.size()); 
      
      Set protocolMBeans = getProtocolMBeans(adaptors[0]);
      assertNotNull("Protocol mbeans registered", channelMBean);
      assertEquals("Multiple protocol mbeans", 10, protocolMBeans.size());      

      // Stop and confirm the mbeans are still registered
      stopPartition(adaptors[0]);
      
      // Let the cluster stabilize
      sleep(2000); 
      
      Set channelMBean2 = getChannelMBeans(adaptors[0]);
      assertTrue("Channel mbean unchanged", channelMBean.equals(channelMBean2)); 
      
      Set protocolMBeans2 = getProtocolMBeans(adaptors[0]);
      assertTrue("Protocol mbeans unchanged", protocolMBeans.equals(protocolMBeans2));
      
      // Undeploy and confirm that mbeans are gone
      undeploy(adaptors[0], DEPLOYMENT);
      
      Set channelMBean3 = getChannelMBeans(adaptors[0]);
      if (channelMBean3 != null)
         assertTrue("Channel mbean undeployed", channelMBean3.size() == 0); 
      
      Set protocolMBeans3 = getProtocolMBeans(adaptors[0]);
      if (protocolMBeans3 != null)
         assertTrue("Protocol mbeans undeployed", protocolMBeans3.size() == 0); 
      
      getLog().debug("ok");
   }
   
   protected void stopPartition(MBeanServerConnection adaptor) throws Exception
   {
      ObjectName partition = new ObjectName("jboss:service=JBAS4406Partition");
      
      Object[] params = new Object[0];
      String[] types = new String[0];
      adaptor.invoke(partition, "stop", params, types);
      
      sleep(2000);
   }
   
   protected Set getChannelMBeans(MBeanServerConnection adaptor) throws Exception
   {
      ObjectName on = new ObjectName("jboss.jgroups:type=channel,cluster=JBAS4406Partition");
      return adaptor.queryNames(on, null);
   }
   
   protected Set getProtocolMBeans(MBeanServerConnection adaptor) throws Exception
   {
      ObjectName on = new ObjectName("jboss.jgroups:*,type=protocol,cluster=JBAS4406Partition");
      return adaptor.queryNames(on, null);
   }
}
