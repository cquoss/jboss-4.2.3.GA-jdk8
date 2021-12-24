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

import org.jboss.ha.framework.server.ClusterPartitionMBean;
import org.jboss.system.ServiceMBean;

/**
 * Cache Invalidation bridge working over JavaGroup.
 * The partition to be used and the invalidation manager can be defined as part
 * of the MBean interface.
 * The bridge automatically discovers which are the InvalidationGroup that are
 * managed by other node of the cluster and only send invalidation information
 * for these groups over the network. This makes this bridge very easy to setup
 * while still being efficient with network resource and CPU serialization cost.
 *
 * @see JGCacheInvalidationBridge
 * @see org.jboss.cache.invalidation.InvalidationManager
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 57188 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>24 septembre 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public interface JGCacheInvalidationBridgeMBean extends ServiceMBean
{
   /** 
    * Gets the name of the Clustering partition to be used to exchange
    * invalidation messages and discover which caches (i.e. InvalidationGroup)
    * are available
    * 
    * @return the name of the partition
    * 
    * @deprecate use {@link #getClusterPartition()}
    */
   String getPartitionName();
   /**
    * Sets the name of the Clustering partition to be used to exchange
    * invalidation messages and discover which caches (i.e. InvalidationGroup)
    * are available
    * 
    * @param name the name of the partition
    * 
    * @deprecate use {@link #setClusterPartition()}
    */
   void setPartitionName(String name);
   
   /**
    * Get the underlying partition used by this service to exchange
    * invalidation messages and discover which caches (i.e. InvalidationGroup)
    * are available
    * 
    * @return the partition
    */
   ClusterPartitionMBean getClusterPartition();
   
   /**
    * Sets the underlying partition used by this service to exchange
    * invalidation messages and discover which caches (i.e. InvalidationGroup)
    * are available
    * 
    * @param clusterPartition the partition
    */
   void setClusterPartition(ClusterPartitionMBean clusterPartition);
   
   /**
    * ObjectName of the InvalidationManager to be used. Optional: in this
    * case, the default InvalidationManager is used.
    */
   public String getInvalidationManager ();
   public void setInvalidationManager (String objectName);
   
   public String getBridgeName ();
   public void setBridgeName (String name);
   
}
