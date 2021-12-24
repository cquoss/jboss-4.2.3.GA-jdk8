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
package javax.management.relation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A list of unresolved roles.
 *
 * <p><b>Revisions:</b>
 * <p><b>20020313 Adrian Brock:</b>
 * <ul>
 * <li>Fix the cloning
 * </ul>
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 */
public class RoleUnresolvedList
  extends ArrayList
{
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Construct an empty RoleUnresolvedList.
    */
   public RoleUnresolvedList()
   {
     super();
   }

   /**
    * Construct a RoleUnresolvedList with an initial capacity.
    *
    * @param initialCapacity the initial capacity.
    */
   public RoleUnresolvedList(int initialCapacity)
   {
     super(initialCapacity);
   }

   /**
    * Construct a RoleUnresolvedList from a list. It must be an ArrayList.
    * The order of the list is maintained.
    *
    * @param list the list to copy from.
    * @exception IllegalArgumentException for a null list or
    *            an list element that is not a role unresolved.
    */
   public RoleUnresolvedList(List list)
     throws IllegalArgumentException
   {
     super();
     if (list == null)
       throw new IllegalArgumentException("Null list");
     Iterator iterator = new ArrayList(list).iterator();
     while (iterator.hasNext())
     {
       try
       {
         add((RoleUnresolved) iterator.next());
       }
       catch (ClassCastException cce)
       {
         throw new IllegalArgumentException("List element is not an unresolved role.");
       }
     }
   }

   // Public ---------------------------------------------------------

   /**
    * Appends a unresolved role to the end of the list.
    * 
    * @param roleUnresolved the new unresolved role.
    * @exception IllegalArgumentException if the unresolved role is null
    */
   public void add(RoleUnresolved roleUnresolved)
     throws IllegalArgumentException
   {
     if (roleUnresolved == null)
       throw new IllegalArgumentException("Null unresolved role");
     super.add(roleUnresolved);
   }

   /**
    * Adds an unresolved role at the specified location in the list.
    * 
    * @param index the location at which to insert the unresolved role.
    * @param roleUnresolved the new unresolved role.
    * @exception IllegalArgumentException if the unresolved role is null
    * @exception IndexOutOfBoundsException if there is no such index
    *            in the list
    */
   public void add(int index, RoleUnresolved roleUnresolved)
     throws IllegalArgumentException, IndexOutOfBoundsException
   {
     if (roleUnresolved == null)
       throw new IllegalArgumentException("Null unresolved role");
     super.add(index, roleUnresolved);
   }

   /**
    * Appends an unresolved role list to the end of the list.
    * 
    * @param roleUnresolvedList the unresolved role list to append (can be null).
    * @return true if the list changes, false otherwise
    * @exception IndexOutOfBoundsException if there is no such index
    *            in the list
    */
   public boolean addAll(RoleUnresolvedList roleUnresolvedList)
     throws IndexOutOfBoundsException
   {
     if (roleUnresolvedList == null)
       return false;
     return super.addAll(roleUnresolvedList);
   }

   /**
    * Inserts an unresolved role list at the specified location in the list.
    * 
    * @param index the location at which to insert the unresolved role list.
    * @param roleUnresolvedList the unresolved role list to insert.
    * @return true if the list changes, false otherwise
    * @exception IllegalArgumentException if the unresolved role list is null
    * @exception IndexOutOfBoundsException if there is no such index
    *            in the list
    */
   public boolean addAll(int index, RoleUnresolvedList roleUnresolvedList)
     throws IllegalArgumentException, IndexOutOfBoundsException
   {
     if (roleUnresolvedList == null)
       throw new IllegalArgumentException("null roleUnresolvedList");
     return super.addAll(index, roleUnresolvedList);
   }

   /**
    * Sets an unresolved role at the specified location in the list.
    * 
    * @param index the location of the unresolved role to replace.
    * @param roleUnresolved the new unresolved role.
    * @exception IllegalArgumentException if the unresolved role is null
    * @exception IndexOutOfBoundsException if there is no such index
    *            in the list
    */
   public void set(int index, RoleUnresolved roleUnresolved)
     throws IllegalArgumentException, IndexOutOfBoundsException
   {
     if (roleUnresolved == null)
       throw new IllegalArgumentException("Null unresolved role");
     super.set(index, roleUnresolved);
   }

   // Array List Overrides -------------------------------------------

   // NONE! I think there was supposed to be?

   // Object Overrides -----------------------------------------------

   /**
    * Cloning.
    *
    * REVIEW: The spec says to return a RoleList, that's not very much
    * of a clone is it? It must be a typo in the RI.
    * 
    * @return the new unresolved role list with the same unresolved roles.
    */
   public Object clone()
   {
      return super.clone();
   }
}

