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

import java.util.HashSet;
import java.util.Set;

/**
 * The meta data object for the security-role-mapping element.
 *
 * The security-role-mapping element maps the user principal
 * to a different principal on the server. It can for example
 * be used to map a run-as-principal to more than one role.
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 57203 $
 */
public class SecurityRoleMetaData 
{
   private String roleName;
   private Set principals;

   public SecurityRoleMetaData(String roleName)
   {
      this.roleName = roleName;
      this.principals = new HashSet();
   }

   public void addPrincipalName(String principalName)
   {
      principals.add(principalName);
   }

   public void addPrincipalNames(Set principalNames)
   {
      principals.addAll(principalNames);
   }

   public String getRoleName()
   {
      return roleName;
   }

   public Set getPrincipals()
   {
      return principals;
   }
}
