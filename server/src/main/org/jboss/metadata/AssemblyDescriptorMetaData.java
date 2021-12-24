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
package org.jboss.metadata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.security.SecurityRoleMetaData;

/**
 * The meta data object for the assembly-descriptor element.
 * This implementation only contains the security-role meta data
 *
 * @author Thomas.Diesler@jboss.org
 * @author Anil.Saldhana@jboss.org
 * @version $Revision: 60857 $
 */
public class AssemblyDescriptorMetaData extends MetaData
{
   /** The assembly-descriptor/security-roles */
   private HashMap securityRoles = new HashMap();

   /** The message destinations */
   private HashMap messageDestinations = new HashMap();
   
   public void addSecurityRoleMetaData(SecurityRoleMetaData srMetaData)
   {
      securityRoles.put(srMetaData.getRoleName(), srMetaData);
   }

   public Map getSecurityRoles()
   {
      return new HashMap(securityRoles);
   }

   /**
    * Merge the security role/principal mapping defined in jboss.xml
    * with the one defined at jboss-app.xml.
    */
   public void mergeSecurityRoles(Map applRoles)
   {
      Iterator it = applRoles.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry) it.next();
         String roleName = (String)entry.getKey();
         SecurityRoleMetaData appRole = (SecurityRoleMetaData)entry.getValue();
         SecurityRoleMetaData srMetaData = (SecurityRoleMetaData)securityRoles.get(roleName);
         if (srMetaData != null)
         {
            Set principalNames = appRole.getPrincipals();
            srMetaData.addPrincipalNames(principalNames);
         }
         else
         {
            securityRoles.put(roleName, entry.getValue());
         }
      }
   }

   public SecurityRoleMetaData getSecurityRoleByName(String roleName)
   {
      return (SecurityRoleMetaData)securityRoles.get(roleName);
   }

   public Set getSecurityRoleNamesByPrincipal(String userName)
   {
      HashSet roleNames = new HashSet();
      Iterator it = securityRoles.values().iterator();
      while (it.hasNext())
      {
         SecurityRoleMetaData srMetaData = (SecurityRoleMetaData) it.next();
         if (srMetaData.getPrincipals().contains(userName))
            roleNames.add(srMetaData.getRoleName());
      }
      return roleNames;
   }
   
   /**
    * Generate a Map of Principal keyed against a set of role names
    * @return Map<Princpal,Set>
    */
   public Map<String,Set<String>> getPrincipalVersusRolesMap()
   {
      Map<String,Set<String>> principalRolesMap = null;

      Iterator iter = securityRoles.keySet().iterator();
      while(iter.hasNext())
      {
         if(principalRolesMap == null)
            principalRolesMap = new HashMap<String,Set<String>>();
         String rolename = (String)iter.next();
         SecurityRoleMetaData srm = (SecurityRoleMetaData) securityRoles.get(rolename);
         Iterator principalIter = srm.getPrincipals().iterator();
         while(principalIter.hasNext())
         {
            String pr = (String)principalIter.next();
            Set<String> roleset = (Set<String>)principalRolesMap.get(pr);
            if(roleset == null)
               roleset = new HashSet<String>();
            if(!roleset.contains(rolename))
               roleset.add(rolename);
            principalRolesMap.put(pr, roleset);
         }
      }
      return principalRolesMap;
   }  
   
   
   public void addMessageDestinationMetaData(MessageDestinationMetaData metaData)
   {
      messageDestinations.put(metaData.getName(), metaData);
   }
   
   public MessageDestinationMetaData getMessageDestinationMetaData(String name)
   {
      return (MessageDestinationMetaData) messageDestinations.get(name);
   }
}
