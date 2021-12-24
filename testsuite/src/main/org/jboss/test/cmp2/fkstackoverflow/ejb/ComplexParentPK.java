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
package org.jboss.test.cmp2.fkstackoverflow.ejb;

/**
 * Primary key for ComplexParent.
 */
public class ComplexParentPK
   extends Object
   implements java.io.Serializable
{
   private int _hashCode = 0;
   private StringBuffer _toStringValue = null;

   public Long id1;
   public Long id2;

   public ComplexParentPK()
   {
   }

   public ComplexParentPK( Long id1,Long id2 )
   {
      this.id1 = id1;
      this.id2 = id2;
   }

   public Long getId1()
   {
      return id1;
   }
   public Long getId2()
   {
      return id2;
   }

   public void setId1(Long id1)
   {
      this.id1 = id1;
      _hashCode = 0;
   }
   public void setId2(Long id2)
   {
      this.id2 = id2;
      _hashCode = 0;
   }

   public int hashCode()
   {
      if( _hashCode == 0 )
      {
         if (this.id1 != null) _hashCode += this.id1.hashCode();
         if (this.id2 != null) _hashCode += this.id2.hashCode();
      }

      return _hashCode;
   }

   public boolean equals(Object obj)
   {
      if( !(obj instanceof  ComplexParentPK) )
         return false;

       ComplexParentPK pk = ( ComplexParentPK)obj;
      boolean eq = true;

      if( obj == null )
      {
         eq = false;
      }
      else
      {
         if( this.id1 == null && (( ComplexParentPK)obj).getId1() == null )
         {
            eq = true;
         }
         else
         {
            if( this.id1 == null || (( ComplexParentPK)obj).getId1() == null )
            {
               eq = false;
            }
            else
            {
               eq = eq && this.id1.equals( pk.id1 );
            }
         }
         if( this.id2 == null && (( ComplexParentPK)obj).getId2() == null )
         {
            eq = true;
         }
         else
         {
            if( this.id2 == null || (( ComplexParentPK)obj).getId2() == null )
            {
               eq = false;
            }
            else
            {
               eq = eq && this.id2.equals( pk.id2 );
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
         _toStringValue.append(this.id1).append('.');
         _toStringValue.append(this.id2).append('.');
         _toStringValue.append(']');
      }

      return _toStringValue.toString();
   }

}
