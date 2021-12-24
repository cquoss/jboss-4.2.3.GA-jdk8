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

/**
 * The problems that occur when resolving roles.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 */
public class RoleStatus
{
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   /**
    * Tried to set a role with less objects that minimum cardinality
    */
   public static final int LESS_THAN_MIN_ROLE_DEGREE = 4;

   /**
    * Tried to set a role with more objects that maximum cardinality
    */
   public static final int MORE_THAN_MAX_ROLE_DEGREE = 5;

   /**
    * Tried to use an unknown role
    */
   public static final int NO_ROLE_WITH_NAME = 1;

   /**
    * Tried to use an an object name that is not registered
    */
   public static final int REF_MBEAN_NOT_REGISTERED = 7;

   /**
    * Tried to use an an object name for an MBean with an incorrect class
    */
   public static final int REF_MBEAN_OF_INCORRECT_CLASS = 6;

   /**
    * Tried to access a role that is not readable
    */
   public static final int ROLE_NOT_READABLE = 2;

   /**
    * Tried to set a role that is not writable
    */
   public static final int ROLE_NOT_WRITABLE = 3;

   /**
    * See if the passed integer is a valid problem type.
    * 
    * @return true when it is, false otherwise.
    */
   public static boolean isRoleStatus(int problemType)
   {
     return (problemType >= NO_ROLE_WITH_NAME && problemType <= REF_MBEAN_NOT_REGISTERED);
   }

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------
}

