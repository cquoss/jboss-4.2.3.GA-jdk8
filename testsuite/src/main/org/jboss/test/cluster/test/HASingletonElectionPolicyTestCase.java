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
package org.jboss.test.cluster.test;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.test.JBossClusteredTestCase;

/**
 * Unit tests for HASingletonElectionPolicy.
 * The testing deployment is under resources/ha/electionpolicy. 
 *
 * @author <a href="mailto:Alex.Fu@novell.com">Alex Fu</a>
 * @author Brian Stansberry
 * @version $Revision: 46010 $
 *
 */
public class HASingletonElectionPolicyTestCase extends JBossClusteredTestCase 
{
   public HASingletonElectionPolicyTestCase(String name)
   {
      super(name);
   }
   
   public static Test suite() throws Exception
   {
      // Refer to jboss-service.xml under resources/ha/electionpolicy
      return getDeploySetup(HASingletonElectionPolicyTestCase.class, "ha-electionpolicy-service.xml");
   }
   
   public void testElectionPolicy() throws Exception
   {
      // Get MBeanServerConnections
      MBeanServerConnection[] adaptors = this.getAdaptors();
      int size = adaptors.length;
      assertTrue(size == 2);   // cluster size must be 2 for 3rd policy test
      
      // First policy is to elect the oldest node (position = 0)
      {
         ObjectName mbean = new ObjectName("jboss.examples:service=HASingletonMBeanExample_1");
         
         Boolean n1 = (Boolean)adaptors[0].getAttribute(mbean, "MasterNode");
         Boolean n2 = (Boolean)adaptors[size - 1].getAttribute(mbean, "MasterNode");
         
         assertEquals(Boolean.TRUE, n1);
         assertEquals(Boolean.FALSE, n2);
      }
      
      // Second policy is the youngest (position = -1)
      {
         ObjectName mbean = new ObjectName("jboss.examples:service=HASingletonMBeanExample_2");
         
         Boolean n1 = (Boolean)adaptors[0].getAttribute(mbean, "MasterNode");
         Boolean n2 = (Boolean)adaptors[size - 1].getAttribute(mbean, "MasterNode");
         
         assertEquals(Boolean.FALSE, n1);
         assertEquals(Boolean.TRUE, n2);
      }
      
      // 3rd policy is the 2nd oldest (position = 1)
      {
         ObjectName mbean = new ObjectName("jboss.examples:service=HASingletonMBeanExample_3");
         
         Boolean n1 = (Boolean)adaptors[0].getAttribute(mbean, "MasterNode");
         Boolean n2 = (Boolean)adaptors[1].getAttribute(mbean, "MasterNode");
         
         assertEquals(Boolean.FALSE, n1);
         assertEquals(Boolean.TRUE, n2);
      }
      
      // 4th policy is not set, default is oldest
      {
         ObjectName mbean = new ObjectName("jboss.examples:service=HASingletonMBeanExample_4");
         
         Boolean n1 = (Boolean)adaptors[0].getAttribute(mbean, "MasterNode");
         Boolean n2 = (Boolean)adaptors[size - 1].getAttribute(mbean, "MasterNode");
         
         assertEquals(Boolean.TRUE, n1);
         assertEquals(Boolean.FALSE, n2);
      }
      
      return;
   }
}
