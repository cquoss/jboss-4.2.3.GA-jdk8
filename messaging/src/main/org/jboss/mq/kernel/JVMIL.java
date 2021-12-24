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
package org.jboss.mq.kernel;

import java.util.Hashtable;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jboss.mq.il.jvm.JVMServerILService;
import org.jboss.mq.il.Invoker;

/**
 * lite wrapper so that this can work in a dependency injection framework.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 57198 $
 */
public class JVMIL extends JVMServerILService
{
   protected Hashtable initialContextProperties;

   public void setJmsInvoker(Invoker invoker)
   {
      jmsServer = invoker;
   }

   public void setInitialContextProperties(Hashtable initialContextProperties)
   {
      this.initialContextProperties = initialContextProperties;
   }

   protected InitialContext getInitialContext() throws NamingException
   {
      InitialContext ctx = null;
      if (initialContextProperties != null)
      {
         ctx = new InitialContext(initialContextProperties);
      }
      else ctx = new InitialContext();
      return ctx;
   }

   public void create() throws Exception
   {

   }

   public void start() throws Exception
   {
      bindJNDIReferences();
   }

   public void stop()
   {
      try
      {
         unbindJNDIReferences();
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
   }

   public void destroy()
   {
   }

}
