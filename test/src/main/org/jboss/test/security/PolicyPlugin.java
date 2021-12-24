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
package org.jboss.test.security;

import java.io.FilePermission;
import java.security.AllPermission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;

/**
 * A Security Policy Plugin.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57204 $
 */
public abstract class PolicyPlugin extends Policy
{
   /** No permissions */
   private static PermissionCollection none;

   /** All file permissions */
   private static PermissionCollection fileRead;

   /** All permissions */
   private static PermissionCollection all;
   
   /**
    * Get the logging plugin
    * 
    * @return the logging plugin
    * @throws Exception for any error
    */
   public static PolicyPlugin getInstance() throws Exception
   {
      String policyClassName = System.getProperty("org.jboss.test.security.PolicyPlugin", "org.jboss.test.security.TestsPolicyPlugin");
      Class policyClass  = Thread.currentThread().getContextClassLoader().loadClass(policyClassName);
      return (PolicyPlugin) policyClass.newInstance();
   }

   public void refresh()
   {
   }
   
   protected PermissionCollection noPermissions()
   {
      if (none == null)
         none = new Permissions();
      return none;
   }
   
   protected PermissionCollection fileReadPermissions()
   {
      if (fileRead == null)
      {
         fileRead = new Permissions();
         fileRead.add(new FilePermission("<<ALL FILES>>", "read"));
      }
      return fileRead;
   }
   
   protected PermissionCollection allPermissions()
   {
      if (all == null)
      {
         all = new Permissions();
         all.add(new AllPermission());
      }
      return all;
   }
}
