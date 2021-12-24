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

import java.util.List;

import org.jboss.ha.framework.interfaces.ClusterNode;

/**
 * Simple extended election policy implementation:
 * 
 * A simple concrete policy service that decides which node in the cluster 
 * should be the master node to run certain HASingleton service based on 
 * attribute "Position". The value will be divided by partition size and only 
 * remainder will be used. 
 * 
 * Let's say partition size is n:
 * 0 means the first oldest node.
 * 1 means the 2nd oldest node. 
 * ...
 * n-1 means the nth oldest node.
 * 
 * -1 means the youngest node.
 * -2 means the 2nd youngest node.
 * ...
 * -n means the nth youngest node.
 * 
 * E.g. the following attribute says the singleton will be running on the 3rd 
 * oldest node of the current partition:
 * <attribute name="Position">2</attribute>
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class ExtendedElectionPolicySimple extends ExtendedElectionPolicyBase
      implements ExtendedElectionPolicySimpleMBean
{
   private int position = 0; // Default
   
   public void setPosition(int pos)
   {
      position = pos;
   }

   public int getPosition()
   {
      return position;
   }

   @Override
   protected ClusterNode elect(List<ClusterNode> candidates)
   {
      int size = candidates.size();
      int remainder = ((position % size) + size) % size;
      
      return candidates.get(remainder);
   }  
}
