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
package org.jboss.test.cmp2.cmrstress.interfaces;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

/**
 * Utility class for Parent.
 */
public class ParentUtil
{
   /** Cached remote home (EJBHome). Uses lazy loading to obtain its value (loaded by getHome() methods). */
   private static ParentHome cachedRemoteHome = null;

   /** Cached local home (EJBLocalHome). Uses lazy loading to obtain its value (loaded by getLocalHome() methods). */
   private static ParentLocalHome cachedLocalHome = null;

   // Home interface lookup methods

   /**
    * Obtain remote home interface from default initial context
    * @return Home interface for Parent. Lookup using JNDI_NAME
    */
   public static ParentHome getHome() throws NamingException
   {
      if (cachedRemoteHome == null) {
         // Obtain initial context
         InitialContext initialContext = new InitialContext();
         try {
            java.lang.Object objRef = initialContext.lookup(ParentHome.JNDI_NAME);
            cachedRemoteHome = (ParentHome)  PortableRemoteObject.narrow(objRef, ParentHome.class);
         } finally {
            initialContext.close();
         }
      }
      return cachedRemoteHome;
   }

   /**
    * Obtain remote home interface from parameterised initial context
    * @param environment Parameters to use for creating initial context
    * @return Home interface for Parent. Lookup using JNDI_NAME
    */
   public static ParentHome getHome( java.util.Hashtable environment ) throws NamingException
   {
      // Obtain initial context
      InitialContext initialContext = new InitialContext(environment);
      try {
         java.lang.Object objRef = initialContext.lookup( ParentHome.JNDI_NAME);
         return ( ParentHome)  PortableRemoteObject.narrow(objRef, ParentHome.class);
      } finally {
         initialContext.close();
      }
   }

   /**
    * Obtain local home interface from default initial context
    * @return Local home interface for Parent. Lookup using JNDI_NAME
    */
   public static ParentLocalHome getLocalHome() throws NamingException
   {
      // Local homes shouldn't be narrowed, as there is no RMI involved.
      if (cachedLocalHome == null) {
         // Obtain initial context
         InitialContext initialContext = new InitialContext();
         try {
            cachedLocalHome = (ParentLocalHome) initialContext.lookup( ParentLocalHome.JNDI_NAME);
         } finally {
            initialContext.close();
         }
      }
      return cachedLocalHome;
   }

}