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
package org.jboss.security;



/**
 * The CallerIdentity is a principal that may have a credential.
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 57203 $
 */
public class CallerIdentity extends SimplePrincipal
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -6321698553594875103L;

   /** The run-as role */
   private Object credential;

   // hash code cache
   private int hashCode;

   /**
    * Construct an unmutable instance of a CallerIdentity
    */
   public CallerIdentity(String principal, Object credential)
   {
      super(principal);
      this.credential = credential;
   }

   public Object getCredential()
   {
      return credential;
   }

   /**
    * Returns a string representation of the object.
    * @return a string representation of the object.
    */
   public String toString()
   {
      return "[principal=" + getName() + "]";
   }

   /**
    * Indicates whether some other object is "equal to" this one.
    */
   public boolean equals(Object obj)
   {
      if (obj == null) return false;
      if (obj instanceof CallerIdentity)
      {
         CallerIdentity other = (CallerIdentity)obj;
         return getName().equals(other.getName());
      }
      return false;
   }

   /**
    * Returns a hash code value for the object.
    */
   public int hashCode()
   {
      if (hashCode == 0)
      {
         hashCode = toString().hashCode();
      }
      return hashCode;
   }
}
