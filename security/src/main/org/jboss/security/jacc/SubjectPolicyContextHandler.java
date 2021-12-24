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
package org.jboss.security.jacc;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;
import java.util.HashSet;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyContextHandler;

import org.jboss.security.RunAsIdentity;
import org.jboss.security.SecurityAssociation;

/** A PolicyContextHandler for the current authenticated Subject.
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
public class SubjectPolicyContextHandler implements PolicyContextHandler
{
   public static final String SUBJECT_CONTEXT_KEY = "javax.security.auth.Subject.container";
   public static final HashSet EMPTY_SET = new HashSet();

   private static class GetSubjectAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetSubjectAction();
      public Object run()
      {
         Subject theSubject = null;
         Subject activeSubject = SecurityAssociation.getSubject();
         if( activeSubject != null )
         {
            Set principalsSet = null;
            RunAsIdentity callerRunAsIdentity = (RunAsIdentity)
               SecurityAssociation.peekRunAsIdentity(1);
            if( callerRunAsIdentity == null )
            {
               principalsSet = activeSubject.getPrincipals();
            }
            else
            {
               principalsSet = callerRunAsIdentity.getPrincipalsSet();
            }

            theSubject = new Subject(true, principalsSet,
               activeSubject.getPublicCredentials(),
               activeSubject.getPrivateCredentials());
         }
         else
         {
            RunAsIdentity callerRunAsIdentity = (RunAsIdentity)
               SecurityAssociation.peekRunAsIdentity(1);
            if( callerRunAsIdentity != null )
            {
               Set principalsSet = callerRunAsIdentity.getPrincipalsSet();
               theSubject = new Subject(true, principalsSet, EMPTY_SET, EMPTY_SET);
            }
         }
         return theSubject;
      }
   }

   public Object getContext(String key, Object data)
      throws PolicyContextException
   {
      if( key.equalsIgnoreCase(SUBJECT_CONTEXT_KEY) == false )
         return null;

      Subject subject = (Subject) AccessController.doPrivileged(GetSubjectAction.ACTION);
      return subject;
   }

   public String[] getKeys()
      throws PolicyContextException
   {
      String[] keys = {SUBJECT_CONTEXT_KEY};
      return keys;
   }

   public boolean supports(String key)
      throws PolicyContextException
   {
      return key.equalsIgnoreCase(SUBJECT_CONTEXT_KEY);
   }
}
