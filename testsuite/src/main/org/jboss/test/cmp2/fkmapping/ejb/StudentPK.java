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
package org.jboss.test.cmp2.fkmapping.ejb;

/**
 * Primary key for Student.
 */
public class StudentPK
   extends Object
   implements java.io.Serializable
{
   private int _hashCode = 0;
   private StringBuffer _toStringValue = null;

   public String departmentCode;
   public String departmentCode2;
   public String lastName;

   public StudentPK()
   {
   }

   public StudentPK( String departmentCode,String departmentCode2,String lastName )
   {
      this.departmentCode = departmentCode;
      this.departmentCode2 = departmentCode2;
      this.lastName = lastName;
   }

   public String getDepartmentCode()
   {
      return departmentCode;
   }
   public String getDepartmentCode2()
   {
      return departmentCode2;
   }
   public String getLastName()
   {
      return lastName;
   }

   public void setDepartmentCode(String departmentCode)
   {
      this.departmentCode = departmentCode;
      _hashCode = 0;
   }
   public void setDepartmentCode2(String departmentCode2)
   {
      this.departmentCode2 = departmentCode2;
      _hashCode = 0;
   }
   public void setLastName(String lastName)
   {
      this.lastName = lastName;
      _hashCode = 0;
   }

   public int hashCode()
   {
      if( _hashCode == 0 )
      {
         if (this.departmentCode != null) _hashCode += this.departmentCode.hashCode();
         if (this.departmentCode2 != null) _hashCode += this.departmentCode2.hashCode();
         if (this.lastName != null) _hashCode += this.lastName.hashCode();
      }

      return _hashCode;
   }

   public boolean equals(Object obj)
   {
      if( !(obj instanceof  StudentPK) )
         return false;

       StudentPK pk = ( StudentPK)obj;
      boolean eq = true;

      if( obj == null )
      {
         eq = false;
      }
      else
      {
         if( this.departmentCode == null && (( StudentPK)obj).getDepartmentCode() == null )
         {
            eq = true;
         }
         else
         {
            if( this.departmentCode == null || (( StudentPK)obj).getDepartmentCode() == null )
            {
               eq = false;
            }
            else
            {
               eq = eq && this.departmentCode.equals( pk.departmentCode );
            }
         }
         if( this.departmentCode2 == null && (( StudentPK)obj).getDepartmentCode2() == null )
         {
            eq = true;
         }
         else
         {
            if( this.departmentCode2 == null || (( StudentPK)obj).getDepartmentCode2() == null )
            {
               eq = false;
            }
            else
            {
               eq = eq && this.departmentCode2.equals( pk.departmentCode2 );
            }
         }
         if( this.lastName == null && (( StudentPK)obj).getLastName() == null )
         {
            eq = true;
         }
         else
         {
            if( this.lastName == null || (( StudentPK)obj).getLastName() == null )
            {
               eq = false;
            }
            else
            {
               eq = eq && this.lastName.equals( pk.lastName );
            }
         }
      }

      return eq;
   }

   /** @return String representation of this pk in the form of [.field1.field2.field3]. */
   public String toString()
   {
      if( _toStringValue == null )
      {
         _toStringValue = new StringBuffer("[.");
         _toStringValue.append(this.departmentCode).append('.');
         _toStringValue.append(this.departmentCode2).append('.');
         _toStringValue.append(this.lastName).append('.');
         _toStringValue.append(']');
      }

      return _toStringValue.toString();
   }

}
