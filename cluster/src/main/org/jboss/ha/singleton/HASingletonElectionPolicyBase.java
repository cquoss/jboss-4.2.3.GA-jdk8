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

import org.jboss.ha.framework.interfaces.ClusterNode;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.system.ServiceMBeanSupport;

/**
 * A base class for policy service that decides which node in the cluster should be 
 * the master node to run certain HASingleton service.
 * 
 * @author <a href="mailto:afu@novell.com">Alex Fu</a>
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @version $Revision: 46010 $
 */
public abstract class HASingletonElectionPolicyBase 
   extends ServiceMBeanSupport
   implements HASingletonElectionPolicyMBean
{
   private Object mManagedSingleton;
   private HAPartition mPartition;
   
   /**
    * @see HASingletonElectionPolicyMBean#setManagedSingleton(Object)
    */
   public void setManagedSingleton(Object singleton)
   {
      this.mManagedSingleton = singleton;
   }

   /**
    * @see HASingletonElectionPolicyMBean#getManagedSingleton()
    */
   public Object getManagedSingleton()
   {
      return this.mManagedSingleton;
   }

   /**
    * @see HASingletonElectionPolicyMBean#setHAPartition(HAPartition)
    */
   public void setHAPartition(HAPartition partition)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Partition set to " + partition);
      }
      
      this.mPartition = partition;
   }

   /**
    * @see HASingletonElectionPolicyMBean#getHAPartition()
    */
   public HAPartition getHAPartition()
   {
      return this.mPartition;
   }
   
   /**
    * @see HASingletonElectionPolicyMBean#isElectedMaster()
    */
   public boolean isElectedMaster()
   {
      if (null == this.mPartition)
         throw new IllegalStateException("HAPartition is not set");
      
      return pickSingleton().equals(this.mPartition.getClusterNode());
   }
    
   /**
    * @see HASingletonElectionPolicyMBean#isElectedMaster(HAPartition)
    */
   @Deprecated
   public boolean isElectedMaster(HAPartition partition) 
   {
      return isElectedMaster();
   }

   /**
    * @see HASingletonElectionPolicyMBean#pickSingleton(HAPartition)
    */
   @Deprecated
   public ClusterNode pickSingleton(HAPartition partition) 
   {
      return pickSingleton();
   }
}
