package org.jboss.ha.singleton;

import org.jboss.ha.framework.interfaces.ClusterNode;
import org.jboss.ha.framework.interfaces.HAPartition;

/**
 * HASingletonElectionPolicy.
 * 
 * @author <a href="mailto:Alex.Fu@novell.com">Alex Fu</a>
 * @author Brian Stansberry
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public interface HASingletonElectionPolicy
{

   /**
    * Called by the HASingleton to provide the election policy a reference to 
    * itself.  A policy that was designed to elect a particular kind of singleton
    * could downcast this object to a particular type and then access the
    * singleton for state information needed for the election decision.
    */
   void setManagedSingleton(Object singleton);

   Object getManagedSingleton();

   /**
    * Sets the HAPartition; from this the election policy can gain
    * access to the DistributedReplicantManager for tracking the 
    * deployment topology for the singleton service and to the HAPartition 
    * for making group RPC calls.
    */
   void setHAPartition(HAPartition partition);

   HAPartition getHAPartition();

   /**
    * Return the elected master node or null if there are no cluster nodes 
    * where the singleton can be deployed. 
    * @return the master node or null.
    */
   ClusterNode pickSingleton();

   /**
    * Given the HAPartition, return the elected master node or null if there are 
    * no cluster nodes where the singleton can be deployed. 
    * @param partition
    * @return the master node or null.
    * @deprecated HAPartition is now set during the HASingleton start phase, as 
    * part of the HAPartition set up. Implementations of this method should 
    * delegate to {@link #pickSingleton()}.
    */
   @Deprecated ClusterNode pickSingleton(HAPartition partition);

   /**
    * Conducts an election and returns whether the managed service
    * is the master, based on the current view of partition.
    * @return true only if the managed service is the master
    */
   boolean isElectedMaster();

   /**
    * Given the HAPartition, return whether the managed service is the master.
    * @param partition
    * @return true only if the managed service is the master
    * @deprecated HAPartition is set during the HASingleton start phase, as 
    * part of the HAPartition set up. Implementations of this method should 
    * delegate to {@link #isElectedMaster()}. 
    */
   @Deprecated boolean isElectedMaster(HAPartition partition);

}