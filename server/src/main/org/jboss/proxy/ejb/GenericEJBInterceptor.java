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
package org.jboss.proxy.ejb;

import java.io.Externalizable;
import java.lang.reflect.Method;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationContext;
import org.jboss.proxy.Interceptor;

/**
 * The base EJB behavior interceptor. 
 *      
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57209 $
 */
public abstract class GenericEJBInterceptor
   extends Interceptor
   implements Externalizable
{
   /** Serial Version Identifier. @since 1.3 */
   private static final long serialVersionUID = 3844706474734439975L;

   // Static method references
   protected static final Method TO_STRING;
   protected static final Method HASH_CODE;
   protected static final Method EQUALS;
   protected static final Method GET_PRIMARY_KEY;
   protected static final Method GET_HANDLE;
   protected static final Method GET_EJB_HOME;
   protected static final Method IS_IDENTICAL;
   
   /** Initialize the static variables. */
   static
   {
      try
      { 
         // Get the methods from Object
         Class[] empty = {};
         Class type = Object.class;
         
         TO_STRING = type.getMethod("toString", empty);
         HASH_CODE = type.getMethod("hashCode", empty);
         EQUALS = type.getMethod("equals", new Class[] { type });
         
         // Get the methods from EJBObject
         type = EJBObject.class;
         
         GET_PRIMARY_KEY = type.getMethod("getPrimaryKey", empty);
         GET_HANDLE = type.getMethod("getHandle", empty);
         GET_EJB_HOME = type.getMethod("getEJBHome", empty);
         IS_IDENTICAL = type.getMethod("isIdentical", new Class[] { type });
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);
      }
   }
   
   /**
    *  A public, no-args constructor for externalization to work.
    */
   public GenericEJBInterceptor()
   {
      // For externalization to work
   }
   
   protected EJBHome getEJBHome(Invocation invocation) throws NamingException
   {
      // Look to the context for the home
      InvocationContext ctx = invocation.getInvocationContext();
      EJBHome home = (EJBHome) ctx.getValue(InvocationKey.EJB_HOME);
      // If there is no home use the legacy lookup method
      if( home == null )
      {
         String jndiName = (String) ctx.getValue(InvocationKey.JNDI_NAME);
         InitialContext iniCtx =  new InitialContext();
         home = (EJBHome) iniCtx.lookup(jndiName);
      }
      return home;
   }
}
  
