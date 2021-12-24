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
package org.jboss.security.auth.spi;

// $Id: DeploymentRolesLoginModule.java 57203 2006-09-26 12:19:06Z dimitris@jboss.org $

import org.jboss.security.SecurityRoleMetaData;
import org.jboss.security.SecurityRolesAssociation;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * The DeploymentRolesLoginModule adds the roles to the subject that were declared in the
 * assembly-descriptor element in jboss.xml.
 *
 * <assembly-descriptor>
 *   <security-role>
 *     <role-name>
 *     <principal-name>
 *   </security-role>
 * </assembly-descriptor>
 * 
 * This allows dynamic role assignment to a given principal per EJB jar deployment.
 * Used by EJB jar deployments in the CTS. 
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 57203 $
 */
public class DeploymentRolesLoginModule extends AbstractServerLoginModule
{
   /**
    * Initialize the login module.
    *
    * @param subject         the Subject to update after a successful login.
    * @param callbackHandler the CallbackHandler that will be used to obtain the
    *                        the user identity and credentials.
    * @param sharedState     a Map shared between all configured login module instances
    * @param options         the parameters passed to the login module.
    */
   public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options)
   {
      super.initialize(subject, callbackHandler, sharedState, options);

      // Relies on another LoginModule having done the authentication
      useFirstPass = true;
   }

   /**
    * Overriden by subclasses to return the Principal that corresponds to
    * the user primary identity.
    */
   protected Principal getIdentity()
   {
      // Setup our view of the user
      Object username = sharedState.get("javax.security.auth.login.name");
      if(username == null)
         throw new IllegalStateException("Expected to find the username in the shared state");

      if (username instanceof Principal)
         return (Principal)username;

      return new SimplePrincipal((String)username);
   }

   /**
    * Create the 'Roles' group and populate it with the
    * principals security roles from the SecurityRolesAssociation
    * @return Group[] containing the sets of roles
    */
   protected Group[] getRoleSets() throws LoginException
   {
      Group group = new SimpleGroup("Roles");
      Iterator itRoleNames = getSecurityRoleNames().iterator();
      while (itRoleNames.hasNext())
      {
         String roleName = (String) itRoleNames.next();
         group.addMember(new SimplePrincipal(roleName));
      }

      return new Group[]{group};
   }

   /**
    * Get the securtiy role names for the current principal from the
    * SecurityRolesAssociation.
    */
   private Set getSecurityRoleNames()
   {
      HashSet roleNames = new HashSet();
      String userName = getIdentity().getName();

      Map securityRoles = SecurityRolesAssociation.getSecurityRoles();
      if (securityRoles != null)
      {
         Iterator it = securityRoles.values().iterator();
         while (it.hasNext())
         {
            SecurityRoleMetaData srMetaData = (SecurityRoleMetaData) it.next();
            if (srMetaData.getPrincipals().contains(userName))
               roleNames.add(srMetaData.getRoleName());
         }
      }
      return roleNames;
   }
}
