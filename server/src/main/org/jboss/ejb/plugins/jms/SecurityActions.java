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
package org.jboss.ejb.plugins.jms;

import java.security.PrivilegedAction;
import java.security.AccessController;

import org.jboss.security.SecurityAssociation;

/** The priviledged actions in used in this package
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57209 $
 */
class SecurityActions
{
   interface SubjectActions
   {
      SubjectActions PRIVILEGED = new SubjectActions()
      {
         private final PrivilegedAction clearAction = new PrivilegedAction()
         {
            public Object run()
            {
               SecurityAssociation.clear();
               return null;
            }
         };

         public void clear()
         {
            AccessController.doPrivileged(clearAction);
         }
      };

      SubjectActions NON_PRIVILEGED = new SubjectActions()
      {
         public void clear()
         {
            SecurityAssociation.clear();
         }
      };

      void clear();
   }

   static void clear()
   {
      if(System.getSecurityManager() == null)
      {
         SubjectActions.NON_PRIVILEGED.clear();
      }
      else
      {
         SubjectActions.PRIVILEGED.clear();
      }
   }
}
