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
package org.jboss.test;

import java.security.Permission;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/** The PermissionCollection object for NamespacePermissions.

@author Scott.Stark@jboss.org
@version $Revision: 37390 $
*/
public class NamespacePermissionCollection extends PermissionCollection
{
    private TreeMap namespacePerms = new TreeMap();
    private TreeMap namespaceKeys = new TreeMap(new PermissionName.NameLengthComparator());

    /** Creates new NamespacePermission */
    public NamespacePermissionCollection()
    {
    }

    public void add(Permission permission)
    {
        if( this.isReadOnly() )
            throw new SecurityException("Cannot add permission to read-only collection");
        if( (permission instanceof NamespacePermission) == false )
            throw new IllegalArgumentException("Only NamespacePermission can be added, invalid="+permission);
        NamespacePermission np = (NamespacePermission) permission;
        PermissionName key = np.getFullName();
        ArrayList tmp = (ArrayList) namespacePerms.get(key);
        if( tmp == null )
        {
            tmp = new ArrayList();
            namespacePerms.put(key, tmp);
            namespaceKeys.put(key, key);
        }
        tmp.add(np);
    }

    /** Locate the closest permissions assigned to the namespace. This is based
     *on the viewing the permission name as a heirarchical PermissionName and
     */
    public boolean implies(Permission permission)
    {
        boolean implies = false;
        if( namespacePerms.isEmpty() == true )
            return false;

        NamespacePermission np = (NamespacePermission) permission;
        // See if there is an exact permission for the name
        PermissionName key = np.getFullName();
        ArrayList tmp = (ArrayList) namespacePerms.get(key);
        if( tmp == null )
        {   // Find the closest parent position.
            SortedMap headMap = namespacePerms.headMap(key);
            try
            {
                PermissionName lastKey = (PermissionName) headMap.lastKey();
                if( lastKey.isParent(key) == true )
                    tmp = (ArrayList) namespacePerms.get(lastKey);
                else
                {
                    PermissionName[] keys = {};
                    keys = (PermissionName[]) headMap.keySet().toArray(keys);
                    for(int k = keys.length-1; k >= 0; k --)
                    {
                        lastKey = keys[k];
                        if( lastKey.isParent(key) == true )
                        {
                            tmp = (ArrayList) namespacePerms.get(lastKey);
                            break;
                        }
                    }
                }
            }
            catch(NoSuchElementException e)
            {   /* Assign the first permission
                Object firstKey = namespacePerms.firstKey();
                tmp = (ArrayList) namespacePerms.get(firstKey);
		*/
            }
        }

        // See if the permission is implied by any we found
        if( tmp != null )
            implies = isImplied(tmp, np);
//System.out.println("NPC["+this+"].implies("+np+") -> "+implies);
        return implies;
    }

    public Enumeration elements()
    {
        Set s = namespaceKeys.keySet();
        final Iterator iter = s.iterator();
        Enumeration elements = new Enumeration()
        {
            ArrayList activeEntry;
            int index;
            public boolean hasMoreElements()
            {
                boolean hasMoreElements = true;
                if( activeEntry == null || index >= activeEntry.size() )
                {
                    hasMoreElements = iter.hasNext();
                    activeEntry = null;
                }
                return hasMoreElements;
            }
            public Object nextElement()
            {
                Object next = null;
                if( activeEntry == null )
                {
                    Object key = iter.next();
                    activeEntry = (ArrayList) namespacePerms.get(key);
                    index = 0;
                    next = activeEntry.get(index ++);
                }
                else
                {
                    next = activeEntry.get(index ++);
                }
                return next;
            }
        };
        return elements;
    }


    private boolean isImplied(ArrayList permissions, NamespacePermission np)
    {
        boolean isImplied = false;
        for(int p = 0; p < permissions.size(); p ++)
        {
            Permission perm = (Permission) permissions.get(p);
            isImplied |= perm.implies(np);
            if( isImplied == true )
                break;
        }
        return isImplied;
    }
}
