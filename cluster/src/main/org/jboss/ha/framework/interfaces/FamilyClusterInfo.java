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

import java.util.ArrayList;

/**
 * Maintain information for a given proxy family. Proxies can statically reference
 * objects implementing this interface: only the content will change as the 
 * cluster topology changes, not the FamilyClusterInfo object itself.
 * Proxies or LoadBalancing policy implementations can use the cursor and object
 * attribute to store arbitrary data that is then shared accross all proxies belonging
 * to the same family. 
 * Initial access to this object is done through the ClusteringTargetsRepository singleton.
 *
 * @see org.jboss.ha.framework.interfaces.FamilyClusterInfoImpl
 * @see org.jboss.ha.framework.interfaces.ClusteringTargetsRepository
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 57188 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>2002/08/23, Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public interface FamilyClusterInfo
{
   public String getFamilyName ();
   
   /**
    * Gets the list of targets for this family.
    * 
    * <strong>NOTE:</strong> Implementations should synchronize on themselves 
    * when executing this method (see JBAS-2071).
    */
   public ArrayList getTargets ();
   public long getCurrentViewId ();
   
   /**
    * Remove the given target from the list of targets.
    * 
    * <strong>NOTE:</strong> Implementations should synchronize on themselves 
    * when executing this method (see JBAS-2071).
    * 
    * @param target the target
    * @return the updated list of targets
    */
   public ArrayList removeDeadTarget(Object target);
   
   /**
    * Updates the targets and the view id.
    * 
    * <strong>NOTE:</strong> Implementations should synchronize on themselves 
    * when executing this method (see JBAS-2071).
    */
   public ArrayList updateClusterInfo (ArrayList targets, long viewId);
   
   public boolean currentMembershipInSyncWithViewId();
   
   /**
    * Force a reload of the view at the next invocation.
    * 
    * <strong>NOTE:</strong> Implementations should synchronize on themselves 
    * when executing this method (see JBAS-2071).
    */
   public void resetView ();
   
   // arbitrary usage by the LoadBalancePolicy implementation
   // We could have used an HashMap but the lookup would have taken
   // much more time and we probably don't need as much flexibility
   // (+ it is slow for a simple int)
   //
   public int getCursor();
   public int setCursor (int cursor);
   public Object getObject ();
   public Object setObject (Object whatever);
   
   public final static int UNINITIALIZED_CURSOR = 999999999;
}
