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
import java.util.Hashtable;

/**
 * JVM singleton that associates a list of targets (+ other info) 
 * contained in a FamilyClusterInfo to a proxy family. For example
 * All remote proxies for a given EJB in a given cluster are part of the
 * same proxy family. Note that home and remote for a same EJB form *2*
 * proxy families.
 *
 * @see org.jboss.ha.framework.interfaces.FamilyClusterInfo
 * @see org.jboss.ha.framework.interfaces.FamilyClusterInfoImpl
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

public class ClusteringTargetsRepository
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   protected static Hashtable families = new Hashtable ();
   
   // Static --------------------------------------------------------
   
   public synchronized static FamilyClusterInfo initTarget (String familyName, ArrayList targets)
   {
      return initTarget (familyName, targets, 0L);
   }
   
   public synchronized static FamilyClusterInfo initTarget (String familyName, ArrayList targets, long viewId)
   {
      // this method must be somehow synchronized to avoid multiple FamilyClusterInfoImpl 
      // for the same familyName in multi-threaded app accessing the same bean
      //
      FamilyClusterInfoImpl family = (FamilyClusterInfoImpl)families.get (familyName);
      if (family == null)
      {
         family = new FamilyClusterInfoImpl (familyName, targets, viewId);
         families.put (familyName, family);         
      }
      else
      {
         // no need to initialize: it is already done: we keep the same object
         //
         family.updateClusterInfo (targets, viewId); // should not happen: possible if redeploy after undeploy fails
      }
         
      return family;
         
   }
   
   public static FamilyClusterInfo getFamilyClusterInfo (String familyName)
   {
      return (FamilyClusterInfo)families.get (familyName);
   }
   
   // Constructors --------------------------------------------------
   
   private ClusteringTargetsRepository () {}
   
   // Public --------------------------------------------------------
   
   // Z implementation ----------------------------------------------
   
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}
