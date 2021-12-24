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

import javax.management.Notification;
import javax.management.NotificationBroadcaster;

import org.jboss.ha.framework.server.ClusterPartitionMBean;

/**
 * <p>
 * HA-Service interface.
 * Defines common functionality for partition symmetric (farming) services.
 * </p>
 *
 * @author <a href="mailto:ivelin@apache.org">Ivelin Ivanov</a>
 * @version $Revision: 57188 $
 *
 */

public interface HAServiceMBean 
  extends org.jboss.system.ServiceMBean, NotificationBroadcaster
{

	/**
	 * Name of the underlying partition that determine the cluster to use.
    * 
    * @deprecate use {@link #getClusterPartition()}
	 */
	public String getPartitionName();

	/**
	 * Set the name of the underlying partition that determine the cluster to use.
	 * Can be set only when the MBean is not in a STARTED or STARTING state.
    * 
    * @deprecate use {@link #setClusterPartition()}
	 */
	public void setPartitionName(String partitionName);
   
   /**
    * Get the underlying partition used by this service.
    * 
    * @return the partition
    */
   ClusterPartitionMBean getClusterPartition();
   
   /**
    * Sets the underlying partition used by this service.
    * Can be set only when the MBean is not in a STARTED or STARTING state.
    * 
    * @param clusterPartition the partition
    */
   void setClusterPartition(ClusterPartitionMBean clusterPartition);


  /**
   * 
   * Convenience method for broadcasting a call to all members 
   * of a partition.
   * 
   * @param methodName
   * @param args
   * @throws Exception
   */
  public void callMethodOnPartition(String methodName, Object[] args)
    throws Exception;

  /**
   * 
   * Convenience method for sharing state across a cluster partition.
   * Delegates to the DistributedStateService
   * 
   * @param key key for the distributed object
   * @return Serializable the distributed object 
   * 
   */
  public Serializable getDistributedState(String key);

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
    throws Exception;


  /**
   * 
   * Broadcast the notification to the remote listener nodes (if any) and then 
   * invoke super.sendNotification() to notify local listeners.
   * 
   * @param notification sent out to local listeners and other nodes. It should be serializable.
   * It is recommended that the source of the notification is an ObjectName of an MBean that 
   * is is available on all nodes where the broadcaster MBean is registered. 
   *   
   * 
   * @see javax.management.NotificationBroadcasterSupport#sendNotification(Notification)
   * 
   */
  public void sendNotification(Notification notification);
  
}
