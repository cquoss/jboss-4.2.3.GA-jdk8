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
package org.jboss.injection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.jboss.ejb3.BeanContext;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 60233 $
 * @deprecated use EJBContextPropertyInjector
 */
public class EJBContextMethodInjector implements Injector, PojoInjector
{
   private Method setMethod;

   public EJBContextMethodInjector(Method setMethod)
   {
      this.setMethod = setMethod;
      setMethod.setAccessible(true);
   }

   public void inject(BeanContext ctx)
   {
      inject(ctx, ctx.getInstance());
   }
   
   public void inject(BeanContext ctx, Object instance)
   {

      Object[] args = {ctx.getEJBContext()};
      try
      {
         setMethod.invoke(instance, args);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException("Failed in setting EntityManager on setter method: " + setMethod.toString());
      }
      catch (InvocationTargetException e)
      {
         throw new RuntimeException(e.getCause());  //To change body of catch statement use Options | File Templates.
      }
   }

   public void inject(Object instance)
   {
      throw new RuntimeException("Illegal operation");
   }

  
   public Class getInjectionClass()
   {
      return setMethod.getParameterTypes()[0];
   }
}
