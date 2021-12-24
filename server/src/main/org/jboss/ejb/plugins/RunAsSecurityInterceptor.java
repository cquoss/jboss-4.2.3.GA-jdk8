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
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.AssemblyDescriptorMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SecurityIdentityMetaData;
import org.jboss.security.RunAsIdentity;

import java.util.Set;

/**
 * An interceptor that enforces the run-as identity declared by a bean.
 *
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @author <a href="mailto:Thomas.Diesler@jboss.org">Thomas Diesler</a>.
 * @version $Revision: 57209 $
 */
public class RunAsSecurityInterceptor extends AbstractInterceptor
{
   protected RunAsIdentity runAsIdentity;

   public RunAsSecurityInterceptor()
   {
   }

   /**
    * Called by the super class to set the container to which this interceptor
    * belongs. We obtain the security manager and runAs identity to use here.
    */
   public void setContainer(Container container)
   {
      super.setContainer(container);
      if (container != null)
      {
         BeanMetaData beanMetaData = container.getBeanMetaData();
         ApplicationMetaData application = beanMetaData.getApplicationMetaData();
         AssemblyDescriptorMetaData assemblyDescriptor = application.getAssemblyDescriptor();

         SecurityIdentityMetaData secMetaData = beanMetaData.getSecurityIdentityMetaData();
         if (secMetaData != null && secMetaData.getUseCallerIdentity() == false)
         {
            String roleName = secMetaData.getRunAsRoleName();
            String principalName = secMetaData.getRunAsPrincipalName();
            if( principalName == null )
               principalName = application.getUnauthenticatedPrincipal();
            // the run-as principal might have extra roles mapped in the assembly-descriptor
            Set extraRoleNames = assemblyDescriptor.getSecurityRoleNamesByPrincipal(principalName);
            runAsIdentity = new RunAsIdentity(roleName, principalName, extraRoleNames);
         }
      }
   }

   // Container implementation --------------------------------------
   public void start() throws Exception
   {
      super.start();
   }

   public Object invokeHome(Invocation mi) throws Exception
   {
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
      }
   }

   public Object invoke(Invocation mi) throws Exception
   {
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
      }
   }

}
