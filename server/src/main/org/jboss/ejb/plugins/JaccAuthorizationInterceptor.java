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

import java.lang.reflect.Method;
import java.security.CodeSource;
import java.security.Policy;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.jacc.EJBMethodPermission; 

import org.jboss.ejb.Container;
import org.jboss.invocation.Invocation;
import org.jboss.metadata.BeanMetaData; 

/** This interceptor is where the JACC ejb container authorization is performed.
 *
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 * @version $Revision: 60859 $
 */
public class JaccAuthorizationInterceptor extends AbstractInterceptor
{
   protected Policy policy;
   protected String ejbName;
   protected CodeSource ejbCS; 

   /** Called by the super class to set the container to which this interceptor
    belongs. We obtain the security manager and runAs identity to use here.
    */
   public void setContainer(Container container)
   {
      super.setContainer(container);
      if (container != null)
      {
         BeanMetaData beanMetaData = container.getBeanMetaData();
         ejbName = beanMetaData.getEjbName();
         ejbCS = container.getBeanClass().getProtectionDomain().getCodeSource();
         
         //Set the flag on the container that JACC is enabled
         container.setJaccEnabled(true); 
      }
      policy = Policy.getPolicy();  
   }

   // Container implementation --------------------------------------
   public void start() throws Exception
   {
      super.start();
   }

   public Object invokeHome(Invocation mi) throws Exception
   {
      // Authorize the call
      checkSecurityAssociation(mi);
      Object returnValue = getNext().invokeHome(mi);
      return returnValue;
   }

   public Object invoke(Invocation mi) throws Exception
   {
      // Authorize the call
      checkSecurityAssociation(mi);
      Object returnValue = getNext().invoke(mi);
      return returnValue;
   }

   /** Authorize the caller's access to the method invocation
    */
   protected void checkSecurityAssociation(Invocation mi)
      throws Exception
   {
      Method m = mi.getMethod();
      // Ignore internal container calls
      if( m == null  )
         return;

      String iface = mi.getType().toInterfaceString();
      EJBMethodPermission methodPerm = new EJBMethodPermission(ejbName, iface, m);
      // Get the caller
      Subject caller = SecurityActions.getContextSubject();
      Principal[] principals = null;
      if( caller != null )
      {
         // Get the caller principals
         Set principalsSet = caller.getPrincipals();
         principals = new Principal[principalsSet.size()];
         principalsSet.toArray(principals);      
      }
      checkPolicy(principals, methodPerm, caller);
   }  
   
   protected void checkPolicy(Principal[] principals, 
         EJBMethodPermission methodPerm, Subject caller)
   {
      ProtectionDomain pd = new ProtectionDomain (ejbCS, null, null, principals);
      if( policy.implies(pd, methodPerm) == false )
      {
         String msg = "Denied: "+methodPerm+", caller=" + caller;
         SecurityException e = new SecurityException(msg);
         throw e;
      }
   }
}
