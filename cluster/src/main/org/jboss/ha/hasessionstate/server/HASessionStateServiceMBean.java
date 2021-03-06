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
package org.jboss.ha.hasessionstate.server;

import javax.management.ObjectName;

import org.jboss.ha.framework.server.ClusterPartitionMBean;
import org.jboss.mx.util.ObjectNameFactory;

/**
 * MBEAN interface for HASessionState service.
 *
 * @see org.jboss.ha.hasessionstate.interfaces.HASessionState
 *
 * @author sacha.labourey@cogito-info.ch
 * @version $Revision: 57188 $
 *
 * <p><b>Revisions:</b><br>
 */
public interface HASessionStateServiceMBean
   extends org.jboss.system.ServiceMBean
{
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss:service=HASessionState");

   String getJndiName();
   void setJndiName(String newName);

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
    * 
    * @param clusterPartition the partition
    */
   void setClusterPartition(ClusterPartitionMBean clusterPartition);
   
   long getBeanCleaningDelay();
   void setBeanCleaningDelay(long newDelay);
}
