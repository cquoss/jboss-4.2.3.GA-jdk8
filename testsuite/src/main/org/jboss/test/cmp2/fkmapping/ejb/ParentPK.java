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
 * Primary key for Parent.
 */
public class ParentPK
   extends Object
   implements java.io.Serializable
{
   private int _hashCode = 0;
   private StringBuffer _toStringValue = null;

   public Long id;
   public String firstName;

   public ParentPK()
   {
   }

   public ParentPK( Long id,String firstName )
   {
      this.id = id;
      this.firstName = firstName;
   }

   public Long getId()
   {
      return id;
   }
   public String getFirstName()
   {
      return firstName;
   }

   public void setId(Long id)
   {
      this.id = id;
      _hashCode = 0;
   }
   public void setFirstName(String firstName)
   {
      this.firstName = firstName;
      _hashCode = 0;
   }

   public int hashCode()
   {
      if( _hashCode == 0 )
      {
         if (this.id != null) _hashCode += this.id.hashCode();
         if (this.firstName != null) _hashCode += this.firstName.hashCode();
      }

      return _hashCode;
   }

   public boolean equals(Object obj)
   {
      if( !(obj instanceof  ParentPK) )
         return false;

       ParentPK pk = ( ParentPK)obj;
      boolean eq = true;

      if( obj == null )
      {
         eq = false;
      }
      else
      {
         if( this.id == null && (( ParentPK)obj).getId() == null )
         {
            eq = true;
         }
         else
         {
            if( this.id == null || (( ParentPK)obj).getId() == null )
            {
               eq = false;
            }
            else
            {
               eq = eq && this.id.equals( pk.id );
            }
         }
         if( this.firstName == null && (( ParentPK)obj).getFirstName() == null )
         {
            eq = true;
         }
         else
         {
            if( this.firstName == null || (( ParentPK)obj).getFirstName() == null )
            {
               eq = false;
            }
            else
            {
               eq = eq && this.firstName.equals( pk.firstName );
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
         _toStringValue.append(this.id).append('.');
         _toStringValue.append(this.firstName).append('.');
         _toStringValue.append(']');
      }

      return _toStringValue.toString();
   }

}
