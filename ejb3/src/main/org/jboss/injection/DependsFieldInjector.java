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

import java.lang.reflect.Field;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import org.jboss.ejb3.BeanContext;
import org.jboss.mx.util.MBeanProxyExt;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision: 60233 $
 */
public class DependsFieldInjector implements Injector
{
   Field field;
   ObjectName on;

   public DependsFieldInjector(Field field, ObjectName on)
   {
      this.field = field;
      this.on = on;
      field.setAccessible(true);
   }

   public void inject(BeanContext ctx)
   {
      Object instance = ctx.getInstance();
      inject(instance);
   }

   public void inject(Object instance)
   {
      Class clazz = field.getType();
      Object value = null;

      if (clazz == ObjectName.class)
      {
         value = on;
      }
      else
      {
         MBeanServer server = (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);
         value = MBeanProxyExt.create(clazz, on, server);
      }

      try
      {
         field.set(instance, value);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
   }

   public Class getInjectionClass()
   {
      return field.getType();
   }
}
