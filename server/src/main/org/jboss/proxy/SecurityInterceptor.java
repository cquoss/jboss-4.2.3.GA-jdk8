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
package org.jboss.proxy;

import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.AccessController;

import org.jboss.invocation.Invocation;
import org.jboss.security.SecurityAssociation;

/**
* The client-side proxy for an EJB Home object.
*      
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 57209 $
*/
public class SecurityInterceptor
   extends Interceptor
{
   /** Serial Version Identifier. @since 1.4.2.1 */
   private static final long serialVersionUID = -4206940878404525061L;

   /**
   * No-argument constructor for externalization.
   */
   public SecurityInterceptor()
   {
   }

   // Public --------------------------------------------------------
   
   public Object invoke(Invocation invocation)
      throws Throwable
   {
      // Get Principal and credentials
      SecurityActions sa = SecurityActions.UTIL.getSecurityActions();

      Principal principal = sa.getPrincipal();
      if (principal != null)
      {
         invocation.setPrincipal(principal);
      }

      Object credential = sa.getCredential();
      if (credential != null)
      {
         invocation.setCredential(credential);
      }

      return getNext().invoke(invocation);
   }

   interface SecurityActions
   {
      class UTIL
      {
         static SecurityActions getSecurityActions()
         {
            return System.getSecurityManager() == null ? NON_PRIVILEGED : PRIVILEGED;
         }
      }

      SecurityActions NON_PRIVILEGED = new SecurityActions()
      {
         public Principal getPrincipal()
         {
            return SecurityAssociation.getPrincipal();
         }

         public Object getCredential()
         {
            return SecurityAssociation.getCredential();
         }
      };

      SecurityActions PRIVILEGED = new SecurityActions()
      {
         private final PrivilegedAction getPrincipalAction = new PrivilegedAction()
         {
            public Object run()
            {
               return SecurityAssociation.getPrincipal();
            }
         };

         private final PrivilegedAction getCredentialAction = new PrivilegedAction()
         {
            public Object run()
            {
               return SecurityAssociation.getCredential();
            }
         };

         public Principal getPrincipal()
         {
            return (Principal)AccessController.doPrivileged(getPrincipalAction);
         }

         public Object getCredential()
         {
            return AccessController.doPrivileged(getCredentialAction);
         }
      };

      Principal getPrincipal();

      Object getCredential();
   }
}
