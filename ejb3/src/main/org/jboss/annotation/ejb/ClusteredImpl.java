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
package org.jboss.annotation.ejb;

import org.jboss.ha.framework.interfaces.LoadBalancePolicy;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 61664 $
 */
public class ClusteredImpl implements Clustered
{
   private Class loadBalancePolicy = LoadBalancePolicy.class;
   private String partition = "${jboss.partition.name:DefaultPartition}";
   

   public Class loadBalancePolicy()
   {
      return loadBalancePolicy;
   }
   
   public void setLoadBalancePolicy(Class loadBalancePolicy)
   {
      this.loadBalancePolicy = loadBalancePolicy;
   }

   public String partition()
   {
      return partition;
   }
   
   public void setPartition(String partition)
   {
      this.partition = partition;
   }

   public Class annotationType()
   {
      return Clustered.class;
   }
   
   public String toString()
   {
      StringBuffer sb = new StringBuffer(100);
      sb.append("[");
      sb.append("loadBalancePolicy=").append(loadBalancePolicy);
      sb.append("partition=").append(partition);
      sb.append("]");
      return sb.toString();
   }
}
