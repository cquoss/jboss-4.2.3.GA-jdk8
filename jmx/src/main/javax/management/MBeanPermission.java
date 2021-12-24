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

// $Id: MBeanPermission.java 57200 2006-09-26 12:10:47Z dimitris@jboss.org $

import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.Permission;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Iterator;

/** Permission controlling access to MBeanServer operations. If a security
 manager has been set using System.setSecurityManager(java.lang.SecurityManager),
 most operations on the MBean Server require that the caller's permissions imply
 an MBeanPermission appropriate for the operation. This is described in detail
 in the documentation for the MBeanServer interface.

As with other Permission objects, an MBeanPermission can represent either a
 permission that you have or a permission that you need. When a sensitive
 operation is being checked for permission, an MBeanPermission is constructed
 representing the permission you need. The operation is only allowed if the
 permissions you have imply the permission you need.

An MBeanPermission contains four items of information:

- The action. For a permission you need, this is one of the actions in the
 list below. For a permission you have, this is a comma-separated list of those
 actions, or *, representing all actions.

      The action is returned by getActions().
 
- The class name.

      For a permission you need, this is the class name of an MBean you are
 accessing, as returned by MBeanServer.getMBeanInfo(name).getClassName().
 Certain operations do not reference a class name, in which case the class
 name is null.

      For a permission you have, this is either empty or a class name pattern.
 A class name pattern is a string following the Java conventions for
 dot-separated class names. It may end with ".*" meaning that the permission
 grants access to any class that begins with the string preceding ".*". For
 instance, "javax.management.*" grants access to
 javax.management.MBeanServerDelegate and javax.management.timer.Timer,
 among other classes.

      A class name pattern can also be empty or the single character "*", both
 of which grant access to any class.
 
- The member.

      For a permission you need, this is the name of the attribute or operation
 you are accessing. For operations that do not reference an attribute or
 operation, the member is null.

      For a permission you have, this is either the name of an attribute or
 operation you can access, or it is empty or the single character "*", both of
 which grant access to any member.
 
- The object name.

      For a permission you need, this is the ObjectName of the MBean you are
 accessing. For operations that do not reference a single MBean, it is null.
 It is never an object name pattern.

      For a permission you have, this is the ObjectName of the MBean or MBeans
 you can access. It may be an object name pattern to grant access to all MBeans
 whose names match the pattern. It may also be empty, which grants access to all
 MBeans whatever their name.

If you have an MBeanPermission, it allows operations only if all four of the
 items match.

The class name, member, and object name can be written together as a single
 string, which is the name of this permission. The name of the permission is
 the string returned by getName(). The format of the string is:

    className#member[objectName] 

The object name is written using the usual syntax for ObjectName. It may
 contain any legal characters, including ]. It is terminated by a ] character
 that is the last character in the string.

One or more of the className, member, or objectName may be omitted. If the
 member is omitted, the # may be too (but does not have to be). If the
 objectName is omitted, the [] may be too (but does not have to be). It is not
 legal to omit all three items, that is to have a name that is the empty string.

One or more of the className, member, or objectName may be the character "-",
 which is equivalent to a null value. A null value is implied by any value
 (including another null value) but does not imply any other value.

The possible actions are these:

    * addNotificationListener
    * getAttribute
    * getClassLoader
    * getClassLoaderFor
    * getClassLoaderRepository
    * getDomains
    * getMBeanInfo
    * getObjectInstance
    * instantiate
    * invoke
    * isInstanceOf
    * queryMBeans
    * queryNames
    * registerMBean
    * removeNotificationListener
    * setAttribute
    * unregisterMBean

In a comma-separated list of actions, spaces are allowed before and after each
 action.
 
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57200 $
 */
public class MBeanPermission extends Permission
{
   private static final long serialVersionUID = -2416928705275160661L;
   private static ObjectName ANY_NAME;
   private static TreeSet VALID_ACTIONS = new TreeSet();
   static
   {
      VALID_ACTIONS.add("addNotificationListener");
      VALID_ACTIONS.add("getAttribute");
      VALID_ACTIONS.add("getClassLoader");
      VALID_ACTIONS.add("getClassLoaderFor");
      VALID_ACTIONS.add("getClassLoaderRepository");
      VALID_ACTIONS.add("getDomains");
      VALID_ACTIONS.add("getMBeanInfo");
      VALID_ACTIONS.add("getObjectInstance");
      VALID_ACTIONS.add("instantiate");
      VALID_ACTIONS.add("invoke");
      VALID_ACTIONS.add("isInstanceOf");
      VALID_ACTIONS.add("queryMBeans");
      VALID_ACTIONS.add("queryNames");
      VALID_ACTIONS.add("registerMBean");
      VALID_ACTIONS.add("removeNotificationListener");
      VALID_ACTIONS.add("setAttribute");
      VALID_ACTIONS.add("unregisterMBean");
   }

   /** The class name this applies to */
   private transient String className;
   private transient boolean prefixMatch;
   private transient String member;
   private transient ObjectName objectName;
   private transient TreeSet actionSet;
   private String actions;

   /**
    * Create a new MBeanPermission object with the specified target name and actions.
    *
    * The target name is of the form "className#member[objectName]" where each
    * part is optional. It must not be empty or null.
    *
    * The actions parameter contains a comma-separated list of the desired
    * actions granted on the target name. It must not be empty or null.
    *
    * @param name the triplet "className#member[objectName]".
    * @param actions the action string.
    * @throws IllegalArgumentException if the name or actions is invalid.
    */
   public MBeanPermission(String name, String actions)
   {
      super(name);
      parseName(name);
      parseActions(actions);
   }
   
   /** Create a new MBeanPermission object with the specified target name
    * (class name, member, object name) and actions.
    *
    * The class name, member and object name parameters define a target name of
    * the form "className#member[objectName]" where each part is optional. This
    * will be the result of Permission.getName() on the resultant
    * MBeanPermission.
    *
    * The actions parameter contains a comma-separated list of the desired
    * actions granted on the target name. It must not be empty or null.
    *
    * @param className the class name to which this permission applies. May be
    * null or "-", which represents a class name that is implied by any class
    * name but does not imply any other class name.
    * @param member the member to which this permission applies. May be null or
    * "-", which represents a member that is implied by any member but does not
    * imply any other member.
    * @param objectName the object name to which this permission applies. May
    * be null, which represents an object name that is implied by any object
    * name but does not imply any other object name.
    * @param actions the action string.
    */
   public MBeanPermission(String className, String member,
      ObjectName objectName, String actions)
   {
      super((className == null ? "-" : className)
         + "#" + (member == null ? "-" : member)
         +"["+(objectName == null ? "-" : objectName.toString())+"]");
      this.className = className;
      this.member = member;
      this.objectName = objectName;
      parseActions(actions);
   }

   /**
    * Returns the "canonical string representation" of the actions.
    * That is, this method always returns present actions in alphabetical order.
    * @return the canonical string representation of the actions.
    */
   public String getActions()
   {
      return actions;
   }

   /**
    * Returns the hash code value for this object.
    * @return a hash code value for this object.
    */
   public int hashCode()
   {
      int hashCode = getName().hashCode();
      if( actionSet != null )
         hashCode += actionSet.hashCode();
      return hashCode;
   }

   /**
    * Checks if this MBeanPermission object "implies" the specified permission.
    *
    * More specifically, this method returns true if:
    * <ul>
    * <li>p is an instance of MBeanPermission; and
    * <li>p has a null className or p's className matches this object's
    * className; and
    * <li>p has a null member or p's member matches this object's member; and
    * <li>p has a null object name name or or p's object name matches this
    * object's object name; and
    * <li>p's actions are a subset of this object's actions
    * </ul>
    *
    * If this object's className is "*", p's className always matches it. If it
    * is "a.*", p's className matches it if it begins with "a.".
    *
    * If this object's member is "*", p's member always matches it.
    *
    * If this object's objectName n1 is an object name pattern, p's objectName
    * n2 matches it if n1.equals(n2) or if n1.apply(n2).
    *
    * A permission that includes the queryMBeans action is considered to include
    * queryNames as well.
    *
    * @param p the permission to check against.
    * @return true if the specified permission is implied by this object, false
    * if not.
    */
   public boolean implies(Permission p)
   {
      if( p == null || (p instanceof MBeanPermission) == false )
         return false;

      MBeanPermission perm = (MBeanPermission) p;
      boolean implies = false;
      // Check the className
      if( perm.className == null )
         implies = true;
      else if( className == null )
         implies = false;
      else if( className.length() == 0 )
         implies = true;
      else if( prefixMatch == true && perm.className != null )
         implies = perm.className.startsWith(className);
      else
         implies = className.equals(perm.className);

      // Check the member
      if( implies == true )
      {
         if( perm.member == null )
            implies = true;
         else if( member == null )
            implies = false;
         else if( member.length() == 0 )
            implies = true;
         else
            implies = member.equals(perm.member);
      }

      // Check the object name
      if( implies == true )
      {
         if( perm.objectName == null )
            implies = true;
         else if( objectName == null )
            implies = false;
         else
         {
            implies = objectName == perm.objectName ||
               objectName.apply(perm.objectName);
            if( implies == false && perm.objectName.isPattern() )
               implies = objectName.equals(perm.objectName);
         }
      }

      // Check the actions
      if( actionSet != null && implies == true )
      {
         implies = perm.actionSet != null && actionSet.containsAll(perm.actionSet);
      }

      return implies;
   }

   /**
    * Checks two MBeanPermission objects for equality. Checks that obj is an
    * MBeanPermission, and has the same name and actions as this object.
    * @param p the object we are testing for equality with this object.
    * @return true if obj is an MBeanPermission, and has the same name and
    * actions as this MBeanPermission object.
    */
   public boolean equals(Object p)
   {
      if( p == null || (p instanceof MBeanPermission) == false )
         return false;

      MBeanPermission perm = (MBeanPermission) p;
      boolean equals = getName().equals(perm.getName());
      if( equals )
      {
         equals = actionSet == perm.actionSet;
         if( equals == false && actionSet != null )
            equals = actionSet.equals(perm.actionSet);
      }

      return equals;
   }

   /** Parse the className#member[objectName] name.
    * @param name - className#member[objectName]
    * @throws IllegalArgumentException
    */ 
   private void parseName(String name)
      throws IllegalArgumentException
   {
      if( name == null || name.length() == 0 )
         throw new IllegalArgumentException("name must not be empty or null");

      StringTokenizer tokenizer = new StringTokenizer(name, "#[]", true);
      boolean inMember = false;
      boolean inObjectName = false;
      // Parse the className
      className = tokenizer.nextToken();
      if( className.equals("#") )
      {
         className = "";
         inMember = true;
      }
      else if( className.equals("[") )
      {
         className = "";
         inObjectName = true;
      }
      else if( className.equals("*") )
         className = "";
      else if( className.equals("-") )
         className = null;
      else if( className.endsWith(".*") )
      {
         className = className.substring(0, className.length()-2);
         prefixMatch = true;
      }

      // Parse the member
      member = "";
      if( inObjectName == false )
      {
         if( inMember == true )
         {
            // Parse after the #
            if( tokenizer.hasMoreTokens() )
            {
               member = tokenizer.nextToken();
               if( member.equals("[") )
               {
                  inObjectName = true;
                  member = "";
               }
               else if( member.equals("*"))
               {
                  member = "";
               }
               else if( member.equals("-"))
               {
                  member = null;
               }
            }
         }
         // See if there is a #
         else if( tokenizer.hasMoreTokens() )
         {
            // Can only be a # or [
            member = tokenizer.nextToken();
            if( member.equals("#") )
            {
               if( tokenizer.hasMoreTokens() )
               {
                  member = tokenizer.nextToken();
                  if( member.equals("[") )
                  {
                     inObjectName = true;
                     member = "";
                  }
                  else if( member.equals("*"))
                  {
                     member = "";
                  }
                  else if( member.equals("-"))
                  {
                     member = null;
                  }
               }
               else
               {
                  member = "";
               }
            }
            else
            {
               inObjectName = true;
            }
         }
      }

      if( ANY_NAME == null )
      {
         try
         {
            ANY_NAME = new ObjectName("*:*");
         }
         catch(Exception e)
         {
            throw new IllegalStateException("Could not create ObjectName(*:*)");
         }
      }

      // Parse the objectName
      objectName = ANY_NAME;
      if( inObjectName == false && tokenizer.hasMoreTokens() )
      {
         inObjectName = true;
         // Throw away the [
         tokenizer.nextToken();
      }

      if( inObjectName )
      {
         // Get the token upto the trailing ]
         String token = tokenizer.nextToken("]");
         try
         {
            if( token.equals("-") )
               objectName = null;
            else if( token.equals("]") )
               objectName = ANY_NAME;
            else
               objectName = new ObjectName(token);
         }
         catch(Exception e)
         {
            IllegalArgumentException ex = new IllegalArgumentException("Invalid objectName");
            ex.initCause(e);
            throw ex;
         }
      }
   }

   /** Parse the actions list
    * @param actions - a comma-separated list of the desired actions granted on
    * the target name. It must not be empty or null
    * @throws IllegalArgumentException
    */ 
   private void parseActions(String actions)
      throws IllegalArgumentException
   {
      if( actions == null || actions.length() == 0 )
         throw new IllegalArgumentException("actions must not be empty or null");

      if( actions.equals("*") )
      {
         this.actionSet = null;
         this.actions = "*";
      }
      else
      {
         this.actionSet = new TreeSet();
         StringTokenizer tokenizer = new StringTokenizer(actions, ", ");
         while( tokenizer.hasMoreTokens() )
         {
            String action = tokenizer.nextToken();
            if( VALID_ACTIONS.contains(action) == false )
               throw new IllegalArgumentException(action+" is not one of: "+VALID_ACTIONS);
            this.actionSet.add(action);
         }
         // queryMBeans -> queryNames;
         if( this.actionSet.contains("queryMBeans") )
            this.actionSet.add("queryNames");

         StringBuffer tmp = new StringBuffer();
         Iterator iter = this.actionSet.iterator();
         while( iter.hasNext() )
         {
            tmp.append(iter.next());
            tmp.append(',');
         }
         tmp.setLength(tmp.length()-1);
         this.actions = tmp.toString();
      }
   }
   
   private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
   {
      ois.defaultReadObject();
      parseName(getName());
      parseActions(getActions());
   }
}
