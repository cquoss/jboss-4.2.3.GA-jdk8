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
package javax.management;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.BasicPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Controls access to actions performed on MBeanServers. The name specifies
 * the permission applies to an operation. The special value * applies to
 * all operations.
 *
 * <ul>
 * <li><b>createMBeanServer<b> controls access to 
 *     {@link MBeanServerFactory#createMBeanServer()} or 
 *     {@link MBeanServerFactory#createMBeanServer(java.lang.String)} </li>
 * <li><b>findMBeanServer<b> controls access to 
 *     {@link MBeanServerFactory#findMBeanServer(java.lang.String)} </li>
 * <li><b>newMBeanServer<b> controls access to 
 *     {@link MBeanServerFactory#newMBeanServer()} or 
 *     {@link MBeanServerFactory#newMBeanServer(java.lang.String)} </li>
 * <li><b>releaseMBeanServer<b> controls access to 
 *     {@link MBeanServerFactory#releaseMBeanServer(javax.management.MBeanServer)} </li>
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57200 $
 */
public class MBeanServerPermission
   extends BasicPermission
{
   private static final long serialVersionUID = -5661980843569388590L;

   private transient boolean allNames;

   /**
    * Construct a new MBeanServer permission for a given name
    *
    * @param name the name of the permission to grant
    * @exception NullPointerException if the name is null
    * @exception IllegalArgumentException if the name is not * or one of
    *            listed names
    */
   public MBeanServerPermission(String name)
   {
      this(name, null);
   }

   /**
    * Construct a new MBeanServer permission for a given name
    *
    * @param name the name of the permission to grant
    * @param actions unused
    * @exception NullPointerException if the name is null
    * @exception IllegalArgumentException if the name is not * or one of the
    * allowed names or a comma-separated list of the allowed names, or if
    * actions is a non-null non-empty string.
    */
   public MBeanServerPermission(String name, String actions)
   {
      super(name, actions);
      init(name, actions);
   }

   // Public ------------------------------------------------------

   /**
    * @return human readable string.
    */
   public String toString()
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append(getClass().getName()).append(":");
      buffer.append(" name=").append(getName());
      buffer.append(" actions=").append(getActions());
      return buffer.toString();
   }

   /** Checks if this MBeanServerPermission object "implies" the specified
    * permission. More specifically, this method returns true if:
    * p is an instance of MBeanServerPermission,
    * p's target names are a subset of this object's target names
    * 
    * The createMBeanServer permission implies the newMBeanServer permission.
    * @param p
    * @return
    */ 
   public boolean implies(Permission p)
   {
      if( (p instanceof MBeanServerPermission) == false )
         return false;

      boolean implies = allNames == true;
      if( implies == false )
      {
         String n0 = getName();
         String n1 = p.getName();
         implies = n0.equals(n1);
         if( implies == false )
         {
            // Check for a createMBeanServer != newMBeanServer
            implies =
               (n0.equals("createMBeanServer") && n1.equals("newMBeanServer"));
         }
      }
      return implies;
   }

   /**
    * Construct a new MBeanServer permission for a given name
    *
    * @param name the name of the permission to grant
    * @param actions unused
    * @exception NullPointerException if the name is null
    * @exception IllegalArgumentException if the name is not * or one of the
    * allowed names or a comma-separated list of the allowed names, or if
    * actions is a non-null non-empty string.
    */
   private void init(String name, String actions)
   {
      if( name == null )
         throw new NullPointerException("name cannot be null");

      if( actions != null && actions.length() > 0 )
         throw new IllegalArgumentException("actions must be null or empty");

      if (name.equals("*") == false &&
          name.equals("createMBeanServer") == false &&
          name.equals("findMBeanServer") == false &&
          name.equals("newMBeanServer") == false &&
          name.equals("releaseMBeanServer") == false)
         throw new IllegalArgumentException("Unknown name: " + name);
      allNames = name.equals("*");
   }

   private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
   {
      ois.defaultReadObject();
      init(getName(), getActions());
   }

   /** Must override to handle the createMBeanServer <-> newMBeanServer
    * relationship.
    * 
    * @return
    */ 
   public PermissionCollection newPermissionCollection()
   {
      return new MBeanServerPermissionCollections();
   }

   class MBeanServerPermissionCollections extends PermissionCollection
   {
      private static final long serialVersionUID = -4111836792595161197L;
      private HashSet permissions = new HashSet();
      private boolean hasAll;

      public void add(Permission p)
      {
         if( this.isReadOnly() )
            throw new SecurityException("Collection is read-only");
         if( p instanceof MBeanServerPermission )
         permissions.add(p);
         if( p.getName().equals("createMBeanServer") )
            permissions.add(new MBeanServerPermission("newMBeanServer"));
         else if( p.getName().equals("*") )
            hasAll = true;
      }

      public boolean implies(Permission p)
      {
         boolean implies = false;
         if( p instanceof MBeanServerPermission )
         {
            implies = hasAll;
            if( implies == false )
            {
               implies = permissions.contains(p);
            }
         }
         return implies;
      }

      public Enumeration elements()
      {
         final Iterator iter = permissions.iterator();
         Enumeration enumerator = new Enumeration()
         {
            public boolean hasMoreElements()
            {
               return iter.hasNext();
            }

            public Object nextElement()
            {
               return iter.next();
            }
         };
         return enumerator;
      }
   }
}
