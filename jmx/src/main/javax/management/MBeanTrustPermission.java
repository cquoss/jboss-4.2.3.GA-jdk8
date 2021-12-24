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

import java.security.BasicPermission;

/** This permission represents "trust" in a signer or codebase.
 * 
 * MBeanTrustPermission contains a target name but no actions list. A single
 * target name, "register", is defined for this permission. The target "*" is
 * also allowed, permitting "register" and any future targets that may be
 * defined. Only the null value or the empty string are allowed for the action
 * to allow the policy object to create the permissions specified in the policy
 * file.
 * 
 * If a signer, or codesource is granted this permission, then it is considered
 * a trusted source for MBeans. Only MBeans from trusted sources may be
 * registered in the MBeanServer.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57200 $
 */
public class MBeanTrustPermission
   extends BasicPermission
{
   private static final long serialVersionUID = -2952178077029018140L;

   /** Create a new MBeanTrustPermission with the given name.
    * 
    * @param name
    * @throws IllegalArgumentException
    * @throws NullPointerException
    */ 
   public MBeanTrustPermission(String name)
      throws IllegalArgumentException, NullPointerException
   {
      this(name, null);
   }
   /** Create a new MBeanTrustPermission with the given name and actions.
    * 
    * @param name
    * @throws IllegalArgumentException - if the name is neither "register" nor
    * "*"; or if actions is a non-null non-empty string.
    * @throws NullPointerException - if the name is null. 
    */ 
   public MBeanTrustPermission(String name, String actions)
      throws IllegalArgumentException, NullPointerException
   {
      super(name, actions);
      if( name == null )
         throw new NullPointerException("name cannot be null");
      if( name.equals("register") == false && name.equals("*") == false )
         throw new IllegalArgumentException("name must be 'register' or '*'");
      if( actions != null && actions.length() > 0 )
         throw new IllegalArgumentException("actions must be null or ''");
   }

}
