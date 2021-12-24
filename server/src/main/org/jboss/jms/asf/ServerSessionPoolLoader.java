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
package org.jboss.jms.asf;

import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.util.naming.NonSerializableFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.tm.XidFactoryMBean;

/**
 * A loader for <tt>ServerSessionPools</tt>.
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57209 $
 */
public class ServerSessionPoolLoader extends ServiceMBeanSupport implements ServerSessionPoolLoaderMBean
{
   /** The factory used to create server session pools. */
   private ServerSessionPoolFactory poolFactory;

   /** The name of the pool. */
   private String name;

   /** The type of pool factory to use. */
   private String poolFactoryClass;

   private ObjectName xidFactory;

   public void setPoolName(final String name)
   {
      this.name = name;
   }

   public String getPoolName()
   {
      return name;
   }

   public void setPoolFactoryClass(final String classname)
   {
      this.poolFactoryClass = classname;
   }

   public String getPoolFactoryClass()
   {
      return poolFactoryClass;
   }

   public ObjectName getXidFactory()
   {
      return xidFactory;
   }

   public void setXidFactory(final ObjectName xidFactory)
   {
      this.xidFactory = xidFactory;
   }

   protected void startService() throws Exception
   {
      XidFactoryMBean xidFactoryObj = (XidFactoryMBean) getServer().getAttribute(xidFactory, "Instance");

      Class cls = Class.forName(poolFactoryClass);
      poolFactory = (ServerSessionPoolFactory) cls.newInstance();
      poolFactory.setName(name);
      poolFactory.setXidFactory(xidFactoryObj);
      log.debug("initialized with pool factory: " + poolFactory);

      InitialContext ctx = new InitialContext();
      String name = poolFactory.getName();
      String jndiname = "java:/" + name;
      try
      {
         NonSerializableFactory.rebind(ctx, jndiname, poolFactory);
         log.debug("pool factory " + name + " bound to " + jndiname);
      }
      finally
      {
         ctx.close();
      }
   }

   protected void stopService()
   {
      // Unbind from JNDI
      InitialContext ctx = null;
      try
      {
         ctx = new InitialContext();
         String name = poolFactory.getName();
         String jndiname = "java:/" + name;

         ctx.unbind(jndiname);
         NonSerializableFactory.unbind(jndiname);
         log.debug("pool factory " + name + " unbound from " + jndiname);
      }
      catch (NamingException ignore)
      {
      }
      finally
      {
         if (ctx != null)
         {
            try
            {
               ctx.close();
            }
            catch (NamingException ignore)
            {
            }
         }
      }
   }
}
