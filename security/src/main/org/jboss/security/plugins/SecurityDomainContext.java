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
package org.jboss.security.plugins;

import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import javax.security.auth.Subject;

import org.jboss.security.RealmMapping;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.SubjectSecurityManager;
import org.jboss.util.CachePolicy;

/** An encapsulation of the JNDI security context infomation
 *
 * @author  Scott.Stark@jboss.org
 * @version 
 */
public class SecurityDomainContext
{
   static final String ACTIVE_SUBJECT = "subject";
   static final String AUTHENTICATION_MGR = "securityMgr";
   static final String AUTORIZATION_MGR = "realmMapping";
   static final String AUTH_CACHE = "authenticationCache";

   AuthenticationManager securityMgr;
   CachePolicy authenticationCache;

   /** Creates new SecurityDomainContextHandler */
   public SecurityDomainContext(AuthenticationManager securityMgr, CachePolicy authenticationCache)
   {
      this.securityMgr = securityMgr;
      this.authenticationCache = authenticationCache;
   }

   public Object lookup(String name) throws NamingException
   {
      Object binding = null;
      if( name == null || name.length() == 0 )
         throw new InvalidNameException("name cannot be null or empty");

      if( name.equals(ACTIVE_SUBJECT) )
         binding = getSubject();
      else if( name.equals(AUTHENTICATION_MGR) )
         binding = securityMgr;
      else if( name.equals(AUTORIZATION_MGR) )
         binding = getRealmMapping();
      else if( name.equals(AUTH_CACHE) )
         binding = authenticationCache;
      return binding;
   }
   public Subject getSubject()
   {
      Subject subject = null;
      if( securityMgr instanceof SubjectSecurityManager )
      {
         subject = ((SubjectSecurityManager)securityMgr).getActiveSubject();
      }
      return subject;
   }
   public AuthenticationManager getSecurityManager()
   {
      return securityMgr;
   }
   public RealmMapping getRealmMapping()
   {
      RealmMapping realmMapping = null;
      if( securityMgr instanceof RealmMapping )
      {
         realmMapping = (RealmMapping)securityMgr;
      }
      return realmMapping;
   }
   public CachePolicy getAuthenticationCache()
   {
      return authenticationCache;
   }

}
