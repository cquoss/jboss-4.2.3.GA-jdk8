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
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import javax.security.jacc.EJBMethodPermission;

import org.jboss.ejb.Container;
import org.jboss.invocation.Invocation;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.AssemblyDescriptorMetaData;
import org.jboss.metadata.BeanMetaData; 
import org.jboss.security.SimplePrincipal;

//$Id$

/**
 *  JBAS-4149: : Jacc Authorization Interceptor that checks for deployment level 
 *  role mappings before using the roles provided in the jaas based
 *  subject
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Feb 23, 2007 
 *  @version $Revision$
 */
public class ExtendedJaccAuthorizationInterceptor extends JaccAuthorizationInterceptor
{  
   //Deployment level principal to roles mapping
   protected Map<String,Set<String>> deploymentRoleMap = null;
    
   public void setContainer(Container container)
   { 
      super.setContainer(container);
      if(container != null)
      {
         BeanMetaData beanMetaData = container.getBeanMetaData();
         ApplicationMetaData applicationMetaData = beanMetaData.getApplicationMetaData();
         AssemblyDescriptorMetaData assemblyDescriptor = applicationMetaData.getAssemblyDescriptor();
         
         //Check for any deployment level mapping
         deploymentRoleMap = assemblyDescriptor.getPrincipalVersusRolesMap();
      }
   }
 
   protected void checkSecurityAssociation(Invocation mi) throws Exception
   { 
      Method m = mi.getMethod();
      // Ignore internal container calls
      if( m == null  )
         return;
      String iface = mi.getType().toInterfaceString();
      EJBMethodPermission methodPerm = new EJBMethodPermission(ejbName, iface, m);

      //Check if there is caller RAI
      if(SecurityActions.peekRunAsIdentity(1) == null)
      {
         if(deploymentRoleMap != null && deploymentRoleMap.size() > 0)
         {
            Principal[] principals = null;
            Principal principal = mi.getPrincipal();
            if(principal != null)
            {
               Set<String> roles = deploymentRoleMap.get(principal.getName());
               if(roles != null)
               {
                  ArrayList<Principal> al = new ArrayList<Principal>();
                  for(String rolename: roles)
                  {
                     al.add(new SimplePrincipal(rolename));
                  }
                  principals = new Principal[al.size()];
                  al.toArray(principals);
                  if(log.isTraceEnabled())
                     log.trace("Principal=" + principal.getName() + "::roles=" + principals);
               }

               checkPolicy(principals, methodPerm, SecurityActions.getContextSubject());
               return;
            }
         }  
      }
      //For RAI as well as the non-availability of deployment level role mapping
      super.checkSecurityAssociation(mi);
   } 
}
