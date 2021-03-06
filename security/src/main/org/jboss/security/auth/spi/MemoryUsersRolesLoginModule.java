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

import java.io.IOException;
import java.util.Properties;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

/**
 * A subclass of UsersRolesLoginModule that uses a singleton instance and
 * memory based users/roles Properties maps to manage user/password and
 * user/role mappings. These maps need to be specified via the login module
 * options.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57203 $
 */
public class MemoryUsersRolesLoginModule extends UsersRolesLoginModule
{
   private Properties users;
   private Properties roles;

   /**
    * Override the UsersRolesLoginModule initialize to look for a users
    * and roles options specifying the
    * 
    * @param subject
    * @param callbackHandler
    * @param sharedState
    * @param options
    */ 
   public void initialize(Subject subject, CallbackHandler callbackHandler,
      Map sharedState, Map options)
   {
      // First extract the users/roles Properties from the options
      this.users = (Properties) options.get("users");
      this.roles = (Properties) options.get("roles");
      // Now initialize the superclass which will invoke createUsers/createRoles
      super.initialize(subject, callbackHandler, sharedState, options);
   }

   /**
    * Provide the users map obtained during initialize
    * @return the users login module option value
    */ 
   protected Properties createUsers(Map options)
   {
      return users;
   }

   /**
    * Provide the users map obtained during initialize
    * @return the users login module option value
    */ 
   protected Properties createRoles(Map options) throws IOException
   {
      return roles;
   }
}
