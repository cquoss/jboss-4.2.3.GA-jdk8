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
package org.jboss.ejb.plugins;

import org.jboss.ejb.Container;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.AssemblyDescriptorMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SecurityIdentityMetaData;
import org.jboss.security.AnybodyPrincipal;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.RealmMapping;
import org.jboss.security.RunAsIdentity;
import org.jboss.system.Registry;

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.lang.reflect.Method;
import javax.security.auth.Subject;
import javax.ejb.TimedObject;
import javax.ejb.Timer;

/**
 * The SecurityInterceptor is where the EJB 2.0 declarative security model
 * is enforced. This is where the caller identity propagation is controlled as well.
 *
 * @author <a href="on@ibis.odessa.ua">Oleg Nitz</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @author <a href="mailto:Thomas.Diesler@jboss.org">Thomas Diesler</a>.
 * @version $Revision: 60859 $
 */
public class SecurityInterceptor extends AbstractInterceptor
{
   /** The interface of an observer that should be notified when principal 
    authentication fails.
    */
   public interface AuthenticationObserver
   {
      final String KEY = "SecurityInterceptor.AuthenticationObserver";
      void authenticationFailed();
   }

   /** The authentication manager plugin
    */
   protected AuthenticationManager securityManager;

   /** The authorization manager plugin
    */
   protected RealmMapping realmMapping;

   // The bean uses this run-as identity to call out
   protected RunAsIdentity runAsIdentity;

   // A map of SecurityRolesMetaData from jboss.xml
   protected Map securityRoles;

   // The observer to be notified when principal authentication fails.
   // This is a hook for the CSIv2 code. The authenticationObserver may
   // send out a ContextError message, as required by the CSIv2 protocol.
   protected AuthenticationObserver authenticationObserver;
   /** */
   protected Method ejbTimeout;

   /** Called by the super class to set the container to which this interceptor
    belongs. We obtain the security manager and runAs identity to use here.
    */
   public void setContainer(Container container)
   {
      super.setContainer(container);
      if (container != null)
      {
         BeanMetaData beanMetaData = container.getBeanMetaData();
         ApplicationMetaData applicationMetaData = beanMetaData.getApplicationMetaData();
         AssemblyDescriptorMetaData assemblyDescriptor = applicationMetaData.getAssemblyDescriptor();
         securityRoles = assemblyDescriptor.getSecurityRoles();

         SecurityIdentityMetaData secMetaData = beanMetaData.getSecurityIdentityMetaData();
         if (secMetaData != null && secMetaData.getUseCallerIdentity() == false)
         {
            String roleName = secMetaData.getRunAsRoleName();
            String principalName = secMetaData.getRunAsPrincipalName();

            // the run-as principal might have extra roles mapped in the assembly-descriptor
            Set extraRoleNames = assemblyDescriptor.getSecurityRoleNamesByPrincipal(principalName);
            runAsIdentity = new RunAsIdentity(roleName, principalName, extraRoleNames);
         }

         securityManager = container.getSecurityManager();
         realmMapping = container.getRealmMapping();

         try
         {
            // Get the timeout method
            ejbTimeout = TimedObject.class.getMethod("ejbTimeout", new Class[]{Timer.class});
         }
         catch (NoSuchMethodException ignore)
         {
         }
      }
   }

   // Container implementation --------------------------------------
   public void start() throws Exception
   {
      super.start();
      authenticationObserver =
         (AuthenticationObserver) Registry.lookup(AuthenticationObserver.KEY);
   }

   public Object invokeHome(Invocation mi) throws Exception
   {
      // Authenticate the subject and apply any declarative security checks
      checkSecurityAssociation(mi);

      /* If a run-as role was specified, push it so that any calls made
       by this bean will have the runAsRole available for declarative
       security checks.
      */
      SecurityActions.pushRunAsIdentity(runAsIdentity);

      try
      {
         Object returnValue = getNext().invokeHome(mi);
         return returnValue;
      }
      finally
      {
         SecurityActions.popRunAsIdentity();
         SecurityActions.popSubjectContext();
      }
   }

   public Object invoke(Invocation mi) throws Exception
   {
      // Authenticate the subject and apply any declarative security checks
      checkSecurityAssociation(mi);

      /* If a run-as role was specified, push it so that any calls made
       by this bean will have the runAsRole available for declarative
       security checks.
      */
      SecurityActions.pushRunAsIdentity(runAsIdentity);

      try
      {
         Object returnValue = getNext().invoke(mi);
         return returnValue;
      }
      finally
      {
         SecurityActions.popRunAsIdentity();
         SecurityActions.popSubjectContext();
      }
   }

   /** The EJB 2.0 declarative security algorithm:
    1. Authenticate the caller using the principal and credentials in the MethodInfocation
    2. Validate access to the method by checking the principal's roles against
    those required to access the method.
    */
   protected void checkSecurityAssociation(Invocation mi)
      throws Exception
   {
      Principal principal = mi.getPrincipal();
      Object credential = mi.getCredential();
      boolean trace = log.isTraceEnabled();

      // If there is not a security manager then there is no authentication required
      Method m = mi.getMethod();
      boolean containerMethod = m == null || m.equals(ejbTimeout);
      if ( containerMethod == true || securityManager == null || container == null )
      {
         // Allow for the progatation of caller info to other beans
         SecurityActions.pushSubjectContext(principal, credential, null);
         return;
      }

      if (realmMapping == null)
      {
         throw new SecurityException("Role mapping manager has not been set");
      }

      // authenticate the current principal
      RunAsIdentity callerRunAsIdentity = SecurityActions.peekRunAsIdentity();
      if (callerRunAsIdentity == null)
      {
         // Check the security info from the method invocation
         Subject subject = new Subject();
         if (securityManager.isValid(principal, credential, subject) == false)
         {
            // Notify authentication observer
            if (authenticationObserver != null)
               authenticationObserver.authenticationFailed();
            // Check for the security association exception
            Exception ex = SecurityActions.getContextException();
            if( ex != null )
               throw ex;
            // Else throw a generic SecurityException
            String msg = "Authentication exception, principal=" + principal;
            SecurityException e = new SecurityException(msg);
            throw e;
         }
         else
         {
            SecurityActions.pushSubjectContext(principal, credential, subject);
            if (trace)
            {
               log.trace("Authenticated  principal=" + principal);
            }
         }
      }
      else
      {
         // Duplicate the current subject context on the stack since
         SecurityActions.dupSubjectContext();
      }

      // Get the method permissions
      InvocationType iface = mi.getType();
      Set methodRoles = container.getMethodPermissions(mi.getMethod(), iface);
      if (methodRoles == null)
      {
         String method = mi.getMethod().getName();
         String msg = "No method permissions assigned to method=" + method
            + ", interface=" + iface;
         SecurityException e = new SecurityException(msg);
         throw e;
      }
      else if (trace)
      {
         log.trace("method=" + mi.getMethod() + ", interface=" + iface
            + ", requiredRoles=" + methodRoles);
      }

      // Check if the caller is allowed to access the method
      if (methodRoles.contains(AnybodyPrincipal.ANYBODY_PRINCIPAL) == false)
      {
         // The caller is using a the caller identity
         if (callerRunAsIdentity == null)
         {
            // Now actually check if the current caller has one of the required method roles
            if (realmMapping.doesUserHaveRole(principal, methodRoles) == false)
            {
               Set userRoles = realmMapping.getUserRoles(principal);
               String method = mi.getMethod().getName();
               BeanMetaData beanMetaData = container.getBeanMetaData();
               String msg = "Insufficient method permissions, principal=" + principal
                  + ", ejbName=" + beanMetaData.getEjbName()
                  + ", method=" + method + ", interface=" + iface
                  + ", requiredRoles=" + methodRoles + ", principalRoles=" + userRoles;
               SecurityException e = new SecurityException(msg);
               throw e;
            }
         }

         // The caller is using a run-as identity
         else
         {
            // Check that the run-as role is in the set of method roles
            if (callerRunAsIdentity.doesUserHaveRole(methodRoles) == false)
            {
               String method = mi.getMethod().getName();
               BeanMetaData beanMetaData = container.getBeanMetaData();
               String msg = "Insufficient method permissions, principal=" + principal
                  + ", ejbName=" + beanMetaData.getEjbName()
                  + ", method=" + method + ", interface=" + iface
                  + ", requiredRoles=" + methodRoles + ", runAsRoles=" + callerRunAsIdentity.getRunAsRoles();
               SecurityException e = new SecurityException(msg);
               throw e;
            }
         }
      }
   }
}
