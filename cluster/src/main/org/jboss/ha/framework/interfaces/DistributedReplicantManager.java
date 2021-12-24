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

import org.jboss.ha.framework.interfaces.DistributedReplicantManager.ReplicantListener;
import java.io.Serializable;
import java.util.List;
import java.util.Collection;

/** 
 *
 *   @author  <a href="mailto:bill@burkecentral.com">Bill Burke</a>.
 *   @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 *   @version $Revision: 57188 $
 *
 * <p><b>Revisions:</b><br>
 * <p><b>2001/10/31: marcf</b>
 * <ol>
 *   <li>DRM is no longer remote
 * </ol>
 * <p><b>2002/08/23: Sacha Labourey</b>
 * <ol>
 *   <li>added isMasterReplica
 * </ol>
 */
public interface DistributedReplicantManager
{
   /**
    * When a particular key in the DistributedReplicantManager table gets modified, all listeners
    * will be notified of replicant changes for that key.
    */
   public interface ReplicantListener
   {
      /**
       * Callback called when the content/list of replicant for a given replicant key has changed
       * @param key The name of the key of the replicant that has changed
       * @param newReplicants The list of new replicants for the give replicant key.
       *                      This list will be in a consistent order on all
       *                      cluster nodes on which the current viewId is
       *                      in effect
       * @param newReplicantsViewId The new replicant view id corresponding to this change
       */      
      public void replicantsChanged(String key, List newReplicants, int newReplicantsViewId);
   }

   /**
    * Subscribe a new listener {@link ReplicantListener} for replicants change
    * @param key Name of the replicant, must be identical cluster-wide for all identical replicants
    * @param subscriber The subsribing {@link ReplicantListener}
    */   
   public void registerListener(String key, ReplicantListener subscriber);
   /**
    * Unsubscribe a listener {@link ReplicantListener} that had subscribed for replicants changes
    * @param key Name of the replicant, must be identical cluster-wide for all identical replicants
    * @param subscriber The unsubscribing {@link ReplicantListener}
    */   
   public void unregisterListener(String key, ReplicantListener subscriber);

   // State binding methods
   //

   /**
    * Add a replicant, it will be attached to this cluster node
    * @param key Replicant name. All replicas around the cluster must use the same key name.
    * @param replicant Local data of the replicant, that is, any serializable data
    * @throws Exception Thrown if a cluster communication problem occurs
    */
   public void add(String key, Serializable replicant) throws Exception;

   /**
    * Remove the entire key from the ReplicationService
    * @param key Name of the replicant
    * @throws Exception Thrown if a cluster communication problem occurs
    */
   public void remove(String key) throws Exception;

   /**
    * Lookup the replicant attached to this cluster node
    * @param key The name of the replicant
    * @return The local replicant for the give key name
    */   
   public Serializable lookupLocalReplicant(String key);

   /**
    * Return a list of all replicants.
    * @param key The replicant name
    * @return An list of serialized replicants available around the cluster 
    *         for the given key. This list will be in the same order in all 
    *         nodes in the cluster.
    */
   public List lookupReplicants(String key);

   /**
    * Return a list of all replicants node names.
    * @param key The replicant name
    * @return A list the node names of cluster nodes that have made available
    *         a replicant for the given key. This list will be in the same 
    *         order in all nodes in the cluster.
    */
   public List lookupReplicantsNodeNames(String key);

   /**
    * Return a list of all services that have a least one replicant.
    * @return A collection of services names (String)
    */
   public Collection getAllServices ();

   /**
    * Returns an id corresponding to the current view of this set of replicants.
    * @param key The replicant name
    * @return A view id (doesn't grow sequentially)
    */
   public int getReplicantsViewId(String key);
   
   /**
    * Indicates if the current node is the master replica for this given key.
    * @param key The replicant name
    * @return True if this node is the master
    */
   public boolean isMasterReplica (String key);

}
