/*
  * JBoss, Home of Professional Open Source
  * Copyright 2006, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
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
package org.jboss.web.tomcat.security;
 
import java.security.Permission;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator; 
import java.util.Set;

import javax.security.auth.Subject;

import org.jboss.metadata.WebMetaData;
import org.jboss.security.RealmMapping;
import org.jboss.security.SimplePrincipal;

//$Id$

/**
 *  JBAS-4149: Extension of JACCAuthorizationRealm that considers deployment level
 *  role mapping
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Feb 23, 2007 
 *  @version $Revision$
 */
public class ExtendedJaccAuthorizationRealm extends JaccAuthorizationRealm
{ 
   protected Principal getCachingPrincpal(RealmMapping realmMapping, 
         Principal authPrincipal,
         Principal callerPrincipal, Object credential, Subject subject)
   { 
      if(SecurityAssociationActions.getCallerRunAsIdentity() == null)
      {
         //Check if there are deployment level roles
         WebMetaData wmd = (WebMetaData) JaccContextValve.activeWebMetaData.get();
         if(wmd != null)
         {
            Set secroles = wmd.getSecurityRoleNamesByPrincipal(authPrincipal.getName());
            Set<Principal> principalroles = new HashSet<Principal>();
            
            if(secroles != null && secroles.isEmpty() == false)
            {
               Iterator iter = secroles.iterator();
               while(iter.hasNext())
               {
                  principalroles.add(new SimplePrincipal((String) iter.next()));
               }
               
               return new JBossGenericPrincipal(this, subject,
                     authPrincipal, callerPrincipal, credential, 
                                new ArrayList(secroles), principalroles);  
            }
         }
      }
      return super.getCachingPrincpal(realmMapping, authPrincipal, 
            callerPrincipal, credential, subject);
   }  
   
   /** See if the given JACC permission is implied using the caller as
    * obtained from either the
    * PolicyContext.getContext(javax.security.auth.Subject.container) or
    * the info associated with the requestPrincipal.
    * 
    * @param perm - the JACC permission to check
    * @param requestPrincpal - the http request getPrincipal
    * @return true if the permission is allowed, false otherwise
    */ 
   protected boolean checkSecurityAssociation(Permission perm, Principal requestPrincpal)
   {
      // Get the caller
      establishSubjectContext(requestPrincpal);

      // Get the caller principals, its null if there is no caller
      Principal[] principals = null;
      
      //Use the roles cached in the principal
      if(requestPrincpal instanceof JBossGenericPrincipal)
      {
         JBossGenericPrincipal jgp = (JBossGenericPrincipal)requestPrincpal;
         String[] rolenames = jgp.getRoles();
         int len = rolenames.length;
         principals = new Principal[len];
         for(int i = 0; i < len; i++)
         {
            principals[i] = new SimplePrincipal(rolenames[i]);
         }
      } 
      return checkSecurityAssociation(perm, principals);
   }
}
