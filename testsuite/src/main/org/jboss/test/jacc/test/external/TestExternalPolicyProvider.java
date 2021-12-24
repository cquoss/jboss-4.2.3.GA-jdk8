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
package org.jboss.test.jacc.test.external;

import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

//$Id: TestExternalPolicyProvider.java 57211 2006-09-26 12:39:46Z dimitris@jboss.org $

/**
 *  Test Policy Provider for JACC Authorization checks
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Apr 27, 2006
 *  @version $Revision: 57211 $
 */
public class TestExternalPolicyProvider extends Policy
{ 
   private TestPermissionCollection tpc;
   
   public TestExternalPolicyProvider()
   {
      tpc = new TestPermissionCollection();
   }
   
   public PermissionCollection getPermissions(CodeSource codeSource)
   { 
      return tpc;
   }

   public void refresh()
   { 
   } 
   
   public boolean implies(ProtectionDomain domain, Permission perm)
   { 
      return super.implies(domain, perm);
   }

   //Custom methods used for xmbean injection
   public String listContextPolicies()
   {
      return "Dummy Context Policy";
   }
   
   public Policy getPolicyProxy()
   {
      return this;
   }
   
   public Class[] getExternalPermissionTypes()
   {
      return new Class[]{};
   }
   
   public void setExternalPermissionTypes(Class[] externalPermissionTypes)
   { 
   } 
   
   //Test Permission Collection
   public class TestPermissionCollection extends PermissionCollection
   { 
      /** The serialVersionUID */
      private static final long serialVersionUID = -8586347666117056129L;
      private Vector perms = new Vector();

      public void add(Permission perm)
      { 
         perms.add(perm);
      }

      public boolean implies(Permission perm)
      { 
         boolean result = false;
         //Uncomment when there are advanced jacc external provider testcases
         /*Iterator iter = perms.iterator();
         while(iter.hasNext())
         {
            Permission p = (Permission)iter.next();
            result = p.implies(perm);
            if(result)
               break;
         }*/
         return !result;
      }

      public Enumeration elements()
      { 
         return perms.elements();
      } 
   }
}
