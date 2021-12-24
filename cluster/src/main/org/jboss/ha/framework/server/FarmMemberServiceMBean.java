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

import java.net.MalformedURLException;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.deployment.scanner.URLDeploymentScannerMBean;

/** 
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 57188 $
 *
 * <p><b>20020809 bill burke:</b>
 * <ul>
 *   <li>Initial import
 * </ul>
 */
public interface FarmMemberServiceMBean 
   extends URLDeploymentScannerMBean
{
   /** The default object name. */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss:service=FarmMember");

   /** 
    * Gets the name of the HAPartition used by this service.
    * 
    * @return the name of the partition
    * 
    * @deprecate use {@link #getClusterPartition()}
    */
   String getPartitionName();
   /**
    * Sets the name of the HAPartition used by this service.
    * Can be set only when the MBean is not in a STARTED or STARTING state.
    * 
    * @param name the name of the partition
    * 
    * @deprecate use {@link #setClusterPartition()}
    */
   void setPartitionName(String name);
   
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

   /** Backward compatibility, mapped to the URLs attribute of URLDeploymentScannerMBean
    * @deprecated
    */
   public void setFarmDeployDirectory(String urls)
      throws MalformedURLException;
   /** Backward compatibility, but ignored as it does nothing.
    * @deprecated
    */
   public void setScannerName(String name);
}
