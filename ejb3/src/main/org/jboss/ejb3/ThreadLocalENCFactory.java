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
package org.jboss.ejb3;

import java.util.Hashtable;
import java.util.LinkedList;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.spi.ObjectFactory;
import org.jnp.interfaces.NamingContext;
import org.jnp.server.NamingServer;

/**
 * Implementation of "java:comp" namespace factory. The context is associated
 * with the thread class loader.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 60420 $
 */
public class ThreadLocalENCFactory
        implements ObjectFactory
{
   private static ThreadLocal<Context> enc = new ThreadLocal<Context>();
   private static ThreadLocal<LinkedList<Context>> stack = new ThreadLocal<LinkedList<Context>>();

   public static Context create(Context parent) throws Exception
   {
      NamingServer srv = new NamingServer();
      return new NamingContext(parent.getEnvironment(), null, srv);
   }

   public static void push(Context ctx)
   {
      if (enc.get() == null)
      {
         enc.set(ctx);
         return;
      }
      LinkedList<Context> currentStack = stack.get();
      if (currentStack == null)
      {
         currentStack = new LinkedList<Context>();
         stack.set(currentStack);
      }
      currentStack.addLast(enc.get());
      enc.set(ctx);
   }

   public static void pop()
   {
      LinkedList<Context> currentStack = stack.get();
      if (currentStack == null)
      {
         enc.set(null);
         return;
      }
      if (currentStack.size() == 0)
      {
         enc.set(null);
         return;
      }
      Context previous = currentStack.removeLast();
      enc.set(previous);
   }
   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // ObjectFactory implementation ----------------------------------
   public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                   Hashtable environment)
           throws Exception
   {
      Context ctx = enc.get();
      return ctx;
   }

}
