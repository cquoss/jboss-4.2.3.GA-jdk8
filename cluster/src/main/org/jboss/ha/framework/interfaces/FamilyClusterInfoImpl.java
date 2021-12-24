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

import org.jboss.ha.framework.interfaces.FamilyClusterInfo;

/**
 * Default implementation of FamilyClusterInfo
 *
 * @see org.jboss.ha.framework.interfaces.FamilyClusterInfo
 * @see org.jboss.ha.framework.interfaces.ClusteringTargetsRepository
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 57188 $
 */
public class FamilyClusterInfoImpl implements FamilyClusterInfo
{
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   public String familyName = null;
   ArrayList targets = null;
   long currentViewId = 0;
   boolean isViewMembersInSyncWithViewId = false;
   
   int cursor = FamilyClusterInfo.UNINITIALIZED_CURSOR;
   Object arbitraryObject = null;
      
   // Static --------------------------------------------------------
    
   // Constructors --------------------------------------------------
   
   private FamilyClusterInfoImpl (){ }
   
   protected FamilyClusterInfoImpl (String familyName, ArrayList targets, long viewId)
   {
      this.familyName = familyName;
      this.targets = (ArrayList) targets.clone();
      this.currentViewId = viewId;
      
      this.isViewMembersInSyncWithViewId = false;
   }

   // Public --------------------------------------------------------
   
   // FamilyClusterInfo implementation ----------------------------------------------
   
   public String getFamilyName () { return this.familyName; }
   
   /**
    * Returns an immutable subclass of ArrayList.
    */
   public synchronized ArrayList getTargets () 
   { 
      return new ImmutableArrayList(this.targets); 
   }
   public long getCurrentViewId () { return this.currentViewId; }
   public int getCursor () { return this.cursor; }
   public int setCursor (int cursor) { return (this.cursor = cursor);}
   public Object getObject () { return this.arbitraryObject; }
   public Object setObject (Object whatever) { this.arbitraryObject = whatever; return this.arbitraryObject; }
   
   public ArrayList removeDeadTarget(Object target)
   {
      synchronized (this)
      {
         ArrayList tmp = (ArrayList) targets.clone();
         tmp.remove (target);
         this.targets = tmp;
         this.isViewMembersInSyncWithViewId = false;
         return new ImmutableArrayList(this.targets);
      }      
   }
   
   public ArrayList updateClusterInfo (ArrayList targets, long viewId)
   {
      synchronized (this)
      {
         this.targets = (ArrayList) targets.clone();
         this.currentViewId = viewId;
         this.isViewMembersInSyncWithViewId = true;
         return new ImmutableArrayList(this.targets);
      }
   }
      
   public boolean currentMembershipInSyncWithViewId ()
   {
      return this.isViewMembersInSyncWithViewId;
   }
   
   public void resetView ()
   {
      synchronized (this)
      {
         this.currentViewId = -1;
         this.isViewMembersInSyncWithViewId = false;
      }
   }
      
   // Object overrides ---------------------------------------------------
   
   public int hashCode()
   {
      return this.familyName.hashCode ();
   }
   
   public boolean equals (Object o)
   {
      if (o instanceof FamilyClusterInfoImpl)
      {
         FamilyClusterInfoImpl fr = (FamilyClusterInfoImpl)o;
         return fr.familyName == this.familyName;
      }
      else
         return false;         
   }

   public String toString()
   {
      StringBuffer tmp = new StringBuffer(super.toString());
      tmp.append("{familyName=");
      tmp.append(familyName);
      tmp.append(",targets=");
      tmp.append(targets);
      tmp.append(",currentViewId=");
      tmp.append(currentViewId);
      tmp.append(",isViewMembersInSyncWithViewId=");
      tmp.append(isViewMembersInSyncWithViewId);
      tmp.append(",cursor=");
      tmp.append(cursor);
      tmp.append(",arbitraryObject=");
      tmp.append(arbitraryObject);
      tmp.append("}");
      return tmp.toString();
   }
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
