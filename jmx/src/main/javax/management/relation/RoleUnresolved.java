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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.mx.util.Serialization;

/**
 * An unresolved role. Used when a role could not be retrieved from a
 * relation due to a problem. It has the role name, the value if that
 * was passed and the problem type.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 */
public class RoleUnresolved
  implements Serializable
{
   // Attributes ----------------------------------------------------

   /**
    * The role name
    */
   private String roleName;

   /**
    * An ordered list of MBean object names.
    */
   private List roleValue;

   /**
    * The problem type.
    */
   private int problemType;

   // Static --------------------------------------------------------

   private static final long serialVersionUID;
   private static final ObjectStreamField[] serialPersistentFields;

   static
   {
      switch (Serialization.version)
      {
      case Serialization.V1R0:
         serialVersionUID = -9026457686611660144L;
         serialPersistentFields = new ObjectStreamField[]
         {
            new ObjectStreamField("myRoleName",  String.class),
            new ObjectStreamField("myRoleValue", ArrayList.class),
            new ObjectStreamField("myPbType", Integer.TYPE)
         };
         break;
      default:
         serialVersionUID = -48350262537070138L;
         serialPersistentFields = new ObjectStreamField[]
         {
            new ObjectStreamField("roleName",  String.class),
            new ObjectStreamField("roleValue", List.class),
            new ObjectStreamField("problemType", Integer.TYPE)
         };
      }
   }

   // Constructors --------------------------------------------------

   /**
    * Construct a new unresolved role.<p>
    *
    * See {@link RoleStatus} for the problem types.<p>
    * 
    * The passed list must be an ArrayList.
    *
    * @param roleName the role name
    * @param roleValue the MBean object names in the role can be null
    * @param problemType the problem type. 
    * @exception IllegalArgumentException for null values or
    *            incorrect problem type.
    */
   public RoleUnresolved(String roleName, List roleValue, int problemType)
     throws IllegalArgumentException
   {
     if (roleName == null)
       throw new IllegalArgumentException("Null roleName");
     if (roleValue == null)
       throw new IllegalArgumentException("Null roleValue");
     if (RoleStatus.isRoleStatus(problemType) == false)
       throw new IllegalArgumentException("Invalid problem type.");
     this.roleName = roleName;
     this.roleValue = new ArrayList(roleValue); 
     this.problemType = problemType; 
   }

   // Public ---------------------------------------------------------

   /**
    * Retrieve the problem type.
    * 
    * @return the problem type.
    */
   public int getProblemType()
   {
     return problemType;
   }

   /**
    * Retrieve the role name.
    * 
    * @return the role name.
    */
   public String getRoleName()
   {
     return roleName;
   }

   /**
    * Retrieve the role value.
    * 
    * @return a list of MBean object names.
    */
   public List getRoleValue()
   {
     return roleValue;
   }

   /**
    * Set the problem type.
    * 
    * @param problemType the problem type.
    * @exception IllegalArgumentException for an invalid problem type
    */
   public void setProblemType(int problemType)
     throws IllegalArgumentException
   {
     if (RoleStatus.isRoleStatus(problemType) == false)
       throw new IllegalArgumentException("Invalid problem type.");
     this.problemType = problemType;
   }
   /**
    * Set the role name.
    * 
    * @param roleName the role name.
    * @exception IllegalArgumentException for a null name
    */
   public void setRoleName(String roleName)
     throws IllegalArgumentException
   {
     if (roleName == null)
       throw new IllegalArgumentException("Null roleName");
     this.roleName = roleName;
   }

   /**
    * Set the role value it must be an ArrayList.
    * A list of mbean object names.
    * 
    * @param roleValue the role value.
    */
   public void setRoleValue(List roleValue)
   {
     this.roleValue = new ArrayList(roleValue);
   }

   // Object Overrides -------------------------------------------------

   /**
    * Clones the object.
    */
   public synchronized Object clone()
   {
      return new RoleUnresolved(roleName, roleValue, problemType);
/*      try
      {
         RoleUnresolved clone = (RoleUnresolved) super.clone();
         clone.roleName = this.roleName;
         clone.problemType = this.problemType;
         clone.roleValue = new ArrayList(this.roleValue);
         return clone;
      }
      catch (CloneNotSupportedException e)
      {
         throw new RuntimeException(e.toString());
      }
*/   }

   /**
    * Formats the unresolved role for output.
    */
   public synchronized String toString()
   {
     StringBuffer buffer = new StringBuffer("Problem (");
     buffer.append(problemType); // REVIEW?????
     buffer.append(") Role Name (");
     buffer.append(roleName);
     buffer.append(") ObjectNames (");
     Iterator iterator = roleValue.iterator(); 
     while (iterator.hasNext())
     {
       buffer.append(iterator.next());
       if (iterator.hasNext())
         buffer.append(" ");
     }
     buffer.append(")");
     return buffer.toString();
   }

   private void readObject(ObjectInputStream ois)
      throws IOException, ClassNotFoundException
   {
      switch (Serialization.version)
      {
      case Serialization.V1R0:
         ObjectInputStream.GetField getField = ois.readFields();
         roleName = (String) getField.get("myRoleName", null);
         roleValue = (List) getField.get("myRoleValue", null);
         problemType = getField.get("myPbType", -1);
         if (problemType == -1)
            throw new StreamCorruptedException("No problem type?");
         break;
      default:
         ois.defaultReadObject();
      }
   }

   private void writeObject(ObjectOutputStream oos)
      throws IOException
   {
      switch (Serialization.version)
      {
      case Serialization.V1R0:
         ObjectOutputStream.PutField putField = oos.putFields();
         putField.put("myRoleName", roleName);
         putField.put("myRoleValue", roleValue);
         putField.put("myPbType", problemType);
         oos.writeFields();
         break;
      default:
         oos.defaultWriteObject();
      }
   }
}

