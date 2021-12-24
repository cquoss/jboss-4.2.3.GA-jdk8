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
package org.jboss.test.cmp2.cmrtree.ejb;

/**
 * Primary key for A.
 */
public class APK extends Object
   implements java.io.Serializable
{
   private int _hashCode = 0;
   private StringBuffer _toStringValue = null;

   public int majorId;
   public String minorId;

   public APK()
   {
   }

   public APK( int majorId,String minorId )
   {
      this.majorId = majorId;
      this.minorId = minorId;
   }

   public int getMajorId()
   {
      return majorId;
   }
   public String getMinorId()
   {
      return minorId;
   }

   public void setMajorId(int majorId)
   {
      this.majorId = majorId;
      _hashCode = 0;
   }
   public void setMinorId(String minorId)
   {
      this.minorId = minorId;
      _hashCode = 0;
   }

   public int hashCode()
   {
      if( _hashCode == 0 )
      {
         _hashCode += (int)this.majorId;
         if (this.minorId != null) _hashCode += this.minorId.hashCode();
      }

      return _hashCode;
   }

   public boolean equals(Object obj)
   {
      if( !(obj instanceof APK) )
         return false;

      APK pk = (APK)obj;
      boolean eq = true;

      if( obj == null )
      {
         eq = false;
      }
      else
      {
         eq = eq && this.majorId == pk.majorId;
         if( this.minorId == null && ((APK)obj).getMinorId() == null )
         {
            eq = true;
         }
         else
         {
            if( this.minorId == null || ((APK)obj).getMinorId() == null )
            {
               eq = false;
            }
            else
            {
               eq = eq && this.minorId.equals( pk.minorId );
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
         _toStringValue.append(this.majorId).append('.');
         _toStringValue.append(this.minorId).append('.');
         _toStringValue.append(']');
      }

      return _toStringValue.toString();
   }

}
