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

package org.jboss.ha.framework.interfaces;

/**
 * ThreadLocal-based context information about whether the
 * current thread is executing a cluster merge.
 * <p/>
 * This is really a temporary 4.x workaround for JBAS-4229. In
 * AS 5 this will likely be replaced by adding a boolean parameter
 * to DistributedReplicantManager.ReplicantListener.replicantsChanged().
 * 
 * @author Brian Stansberry
 */
public class ClusterMergeStatus
{
   private static final ThreadLocal status = new ThreadLocal();
   
   public static void startMergeProcess()
   {
      status.set(Boolean.TRUE);
   }
   
   public static void endMergeProcess()
   {
      status.set(null);
   }
   
   public static boolean isMergeInProcess()
   {
      return status.get() != null;
   }
}
