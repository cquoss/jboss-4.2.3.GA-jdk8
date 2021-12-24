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

import java.util.ArrayList;
import java.util.List;

import org.jboss.ha.framework.interfaces.ClusterNode;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager;
import org.jboss.ha.framework.interfaces.HAPartition;

/**
 * Base extended election policy providing helper methods for concrete 
 * implementations.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public abstract class ExtendedElectionPolicyBase extends HASingletonElectionPolicyBase
      implements ExtendedElectionPolicyMBean
{
   private String singletonName;

   public String getSingletonName()
   {
      return singletonName;
   }

   public void setSingletonName(String serviceName)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Singleton name set to " + serviceName);
      }
      
      singletonName = serviceName;
   }

   public ClusterNode pickSingleton()
   {
      List<ClusterNode> candidates = getCandidates();
      if (candidates == null)
      {
         log.debug("List of cluster node candidates where to run the singleton is null");
         return null;
      }
             
      return elect(candidates);
   }
   
   @Override
   public boolean isElectedMaster()
   {
      ClusterNode selectedNode = pickSingleton();
      if (selectedNode != null)
      {
         return selectedNode.equals(getHAPartition().getClusterNode());
      }
      
      return false;
   }
   
   /**
    * Get the list of candidate {@link ClusterNode} instances where the 
    * singleton could be deployed.
    * 
    * @return List of {@link ClusterNode} instances.
    */
   protected List<ClusterNode> getCandidates() 
   {
      HAPartition partition = getHAPartition();
      
      if (partition == null)
      {
         throw new IllegalStateException("HAPartition has not been set");
      }
      
      DistributedReplicantManager drm = partition.getDistributedReplicantManager();
      /* retrieve node names where singleton is deployed */
      List<String> nodeNames = drm.lookupReplicantsNodeNames(getSingletonName());
      if (nodeNames != null)
      {
         ClusterNode[] allNodes = partition.getClusterNodes();
         List<ClusterNode> nodesSingletonIn = new ArrayList<ClusterNode>(nodeNames.size());
         
         /* For each node in the list of nodes where the singleton is deployed, 
          * find the corresponding ClusterNode object and add it to the list of 
          * ClusterNode candidates where the singleton can run. 
          * 
          * This is certainly not the most efficient way to do it's we can do 
          * given the limited DRM API in AS 4.x. */
         for (String nodeName : nodeNames)
         {
            for(int i = 0; i < allNodes.length; i++)
            {
               ClusterNode node = allNodes[i];
               if (node.getName().equals(nodeName))
               {
                  nodesSingletonIn.add(node);
                  break;
               }
            }
         }
         
         return nodesSingletonIn;            
      }
      
      return null;
   }
   
   /**
    * Given a List of candidate {@link ClusterNode} instances, return the 
    * elected node where the singleton should run.
    * 
    * @param candidates List of {@link ClusterNode}.
    * @return {@link ClusterNode} instance.
    */
   protected abstract ClusterNode elect(List<ClusterNode> candidates);
}