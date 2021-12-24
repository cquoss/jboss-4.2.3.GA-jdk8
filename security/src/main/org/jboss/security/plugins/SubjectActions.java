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
 
import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.security.Principal;
import java.util.Set;
import java.util.Iterator;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.callback.CallbackHandler;

import org.jboss.security.SecurityAssociation; 

/** Common PrivilegedAction used by classes in this package.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
class SubjectActions
{
   private static class ToStringSubjectAction implements PrivilegedAction
   {
      Subject subject;
      ToStringSubjectAction(Subject subject)
      {
         this.subject = subject;
      }
      public Object run()
      {
         StringBuffer tmp = new StringBuffer();
         tmp.append("Subject(");
         tmp.append(System.identityHashCode(subject));
         tmp.append(").principals=");
         Iterator principals = subject.getPrincipals().iterator();
         while( principals.hasNext() )
         {
            Object p = principals.next();
            Class c = p.getClass();
            tmp.append(c.getName());
            tmp.append('@');
            tmp.append(System.identityHashCode(c));
            tmp.append('(');
            tmp.append(p);
            tmp.append(')');
         }
         return tmp.toString();
      }
   }

   private static class GetSubjectAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetSubjectAction();
      public Object run()
      {
         Subject subject = SecurityAssociation.getSubject();
         return subject;
      }
   }

   private static class CopySubjectAction implements PrivilegedAction
   {
      Subject fromSubject;
      Subject toSubject;
      boolean setReadOnly;
      boolean deepCopy;
      
      CopySubjectAction(Subject fromSubject, Subject toSubject, boolean setReadOnly)
      {
         this.fromSubject = fromSubject;
         this.toSubject = toSubject;
         this.setReadOnly = setReadOnly;
      }
      public void setDeepCopy(boolean flag)
      {
         this.deepCopy = flag;
      }
      
      public Object run()
      {
         Set principals = fromSubject.getPrincipals();
         Set principals2 = toSubject.getPrincipals();
         Iterator iter = principals.iterator();
         while( iter.hasNext() ) 
            principals2.add(getCloneIfNeeded(iter.next()));  
         Set privateCreds = fromSubject.getPrivateCredentials();
         Set privateCreds2 = toSubject.getPrivateCredentials();
         iter = privateCreds.iterator();
         while( iter.hasNext() )
            privateCreds2.add(getCloneIfNeeded(iter.next()));
         Set publicCreds = fromSubject.getPublicCredentials();
         Set publicCreds2 = toSubject.getPublicCredentials();
         iter = publicCreds.iterator();
         while( iter.hasNext() )
            publicCreds2.add(getCloneIfNeeded(iter.next()));
         if( setReadOnly == true )
            toSubject.setReadOnly();
         return null;
      }
      
      /** Check if the deepCopy flag is ON &&
       *  Object implements Cloneable and return cloned object */
      private Object getCloneIfNeeded(Object obj)
      {
         Object clonedObject = null;
         if(this.deepCopy && obj instanceof Cloneable)
         {
            Class clazz = obj.getClass();
            try
            {
               Method cloneMethod = clazz.getMethod("clone", null);
               clonedObject = cloneMethod.invoke(obj, null);
            }
            catch (Exception e)
            {//Ignore non-cloneable issues 
            } 
         }
         if(clonedObject == null)
            clonedObject = obj;
         return clonedObject;
      }
   }

   private static class LoginContextAction implements PrivilegedExceptionAction
   {
      String securityDomain;
      Subject subject;
      CallbackHandler handler;
      LoginContextAction(String securityDomain, Subject subject,
         CallbackHandler handler)
      {
         this.securityDomain = securityDomain;
         this.subject = subject;
         this.handler = handler;
      }
      public Object run() throws Exception
      {
         LoginContext lc = new LoginContext(securityDomain, subject, handler);
         return lc;
      }
   }

   private static class GetTCLAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetTCLAction();
      public Object run()
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         return loader;
      }
   }

   private static class SetContextInfoAction implements PrivilegedAction
   {
      Object key;
      Object value;
      SetContextInfoAction(Object key, Object value)
      {
         this.key = key;
         this.value = value;
      }
      public Object run()
      {
         return SecurityAssociation.setContextInfo(key, value);
      }
   }

   interface PrincipalInfoAction
   {
      PrincipalInfoAction PRIVILEGED = new PrincipalInfoAction()
      {
         public void push(final Principal principal, final Object credential,
            final Subject subject)
         {
            AccessController.doPrivileged(
               new PrivilegedAction()
               {
                  public Object run()
                  {
                     SecurityAssociation.pushSubjectContext(subject, principal, credential);
                     return null;
                  }
               }
            );
         }
         public void pop()
         {
            AccessController.doPrivileged(
               new PrivilegedAction()
               {
                  public Object run()
                  {
                     SecurityAssociation.popSubjectContext();
                     return null;
                  }
               }
            );
         }
      };

      PrincipalInfoAction NON_PRIVILEGED = new PrincipalInfoAction()
      {
         public void push(Principal principal, Object credential, Subject subject)
         {
            SecurityAssociation.pushSubjectContext(subject, principal, credential);
         }
         public void pop()
         {
            SecurityAssociation.popSubjectContext();
         }
      };

      void push(Principal principal, Object credential, Subject subject);
      void pop();
   }

   static Subject getActiveSubject()
   {
      Subject subject = (Subject) AccessController.doPrivileged(GetSubjectAction.ACTION);
      return subject;
   }
   static void copySubject(Subject fromSubject, Subject toSubject)
   {
      copySubject(fromSubject, toSubject, false);
   }
   static void copySubject(Subject fromSubject, Subject toSubject, boolean setReadOnly)
   {
      CopySubjectAction action = new CopySubjectAction(fromSubject, toSubject, setReadOnly);
      if( System.getSecurityManager() != null )
         AccessController.doPrivileged(action);
      else
         action.run();
   }
   
   static void copySubject(Subject fromSubject, Subject toSubject, boolean setReadOnly,
         boolean deepCopy)
   {
      CopySubjectAction action = new CopySubjectAction(fromSubject, toSubject, setReadOnly);
      action.setDeepCopy(deepCopy);
      if( System.getSecurityManager() != null )
         AccessController.doPrivileged(action);
      else
         action.run();
   }

   static LoginContext createLoginContext(String securityDomain, Subject subject,
      CallbackHandler handler)
      throws LoginException
   {
      LoginContextAction action = new LoginContextAction(securityDomain, subject, handler);
      try
      {
         LoginContext lc = (LoginContext) AccessController.doPrivileged(action);
         return lc;
      }
      catch(PrivilegedActionException e)
      {
         Exception ex = e.getException();
         if( ex instanceof LoginException )
            throw (LoginException) ex;
         else
            throw new LoginException(ex.getMessage());
      }
   }

   static ClassLoader getContextClassLoader()
   {
      ClassLoader loader = (ClassLoader) AccessController.doPrivileged(GetTCLAction.ACTION);
      return loader;
   }

   static Object setContextInfo(Object key, Object value)
   {
      SetContextInfoAction action = new SetContextInfoAction(key, value);
      Object prevInfo = AccessController.doPrivileged(action);
      return prevInfo;
   }

   static void pushSubjectContext(Principal principal, Object credential,
      Subject subject)
   {
      if(System.getSecurityManager() == null)
      {
         PrincipalInfoAction.NON_PRIVILEGED.push(principal, credential, subject);
      }
      else
      {
         PrincipalInfoAction.PRIVILEGED.push(principal, credential, subject);
      }
   }
   static void popSubjectContext()
   {
      if(System.getSecurityManager() == null)
      {
         PrincipalInfoAction.NON_PRIVILEGED.pop();
      }
      else
      {
         PrincipalInfoAction.PRIVILEGED.pop();
      }
   }

   
   static String toString(Subject subject)
   {
      ToStringSubjectAction action = new ToStringSubjectAction(subject);
      String info = (String) AccessController.doPrivileged(action);
      return info;
   }
}

