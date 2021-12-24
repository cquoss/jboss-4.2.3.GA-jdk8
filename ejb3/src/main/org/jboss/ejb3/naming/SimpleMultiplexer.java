/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.naming;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;

import org.jboss.ejb3.InitialContextFactory;
import org.jboss.logging.Logger;

/**
 * An object factory which creates a multiplexing context to "comp.ejb3" and "comp.original"
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class SimpleMultiplexer implements ObjectFactory
{
   private static final Logger log = Logger.getLogger(SimpleMultiplexer.class);
   
   public SimpleMultiplexer() throws NamingException
   {
   }
   
   private Context createMultiplexer(Context ctx) throws NamingException
   {
      Context ctxTwo = (Context) ctx.lookup("comp.ejb3");
      Context ctxOne = (Context) ctx.lookup("comp.original");
      if(log.isTraceEnabled())
      {
         log.trace("contextClassLoader = " + getContextClassLoader() + " ctxOne = " + ctxOne);
      }
      if(ctxTwo == null)
         return ctxOne;
      return new MultiplexerContext(ctxOne, ctxTwo);
   }
   
   private static ClassLoader getContextClassLoader()
   {
      if(System.getSecurityManager() == null)
         return Thread.currentThread().getContextClassLoader();
      else
      {
         PrivilegedAction<ClassLoader> action = new PrivilegedAction<ClassLoader>()
         {
            public ClassLoader run()
            {
               return Thread.currentThread().getContextClassLoader();
            }
         };
         return AccessController.doPrivileged(action);
      }
   }
   
   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception
   {
      if(nameCtx == null)
      {
         nameCtx = (Context) InitialContextFactory.getInitialContext().lookup("java:");
         try
         {
            return createMultiplexer(nameCtx);
         }
         finally
         {
            nameCtx.close();
         }
      }
      else
      {
         return createMultiplexer(nameCtx);
      }
   }
}
