/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

/**
 * Extended HASingleton election policy that allows setting HASingleton's name. 
 * This enables election policies to work correctly in heterogenous topologies
 * where HASingletons are not deployed in all nodes in the cluster.
 * 
 * By setting the HASingleton name in the election policy, the policy is able
 * to query the DistributedReplicantManager in order to find out the List nodes 
 * where the HASingleton is deployed and then, choose from this list the node 
 * that should be running it.  
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public interface ExtendedElectionPolicy extends HASingletonElectionPolicy
{
   /**
    * Called by the HASingleton, during the start service phase, to set the 
    * name with which the singleton is registered with the HAPartition.
    * 
    * @param serviceName the singleton service name.
    */
   void setSingletonName(String serviceName);

   /**
    * Get the singleton name for this election policy. 
    * 
    * @return the singleton service name.
    */
   String getSingletonName();    
}
