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
 * Primary key for Examenation.
 */
public class ExamenationPK
   extends Object
   implements java.io.Serializable
{
   private int _hashCode = 0;
   private StringBuffer _toStringValue = null;

   public String examId;

   public ExamenationPK()
   {
   }

   public ExamenationPK( String examId )
   {
      this.examId = examId;
   }

   public String getExamId()
   {
      return examId;
   }

   public void setExamId(String examId)
   {
      this.examId = examId;
      _hashCode = 0;
   }

   public int hashCode()
   {
      if( _hashCode == 0 )
      {
         if (this.examId != null) _hashCode += this.examId.hashCode();
      }

      return _hashCode;
   }

   public boolean equals(Object obj)
   {
      if( !(obj instanceof  ExamenationPK) )
         return false;

       ExamenationPK pk = ( ExamenationPK)obj;
      boolean eq = true;

      if( obj == null )
      {
         eq = false;
      }
      else
      {
         if( this.examId == null && (( ExamenationPK)obj).getExamId() == null )
         {
            eq = true;
         }
         else
         {
            if( this.examId == null || (( ExamenationPK)obj).getExamId() == null )
            {
               eq = false;
            }
            else
            {
               eq = eq && this.examId.equals( pk.examId );
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
         _toStringValue.append(this.examId).append('.');
         _toStringValue.append(']');
      }

      return _toStringValue.toString();
   }

}
