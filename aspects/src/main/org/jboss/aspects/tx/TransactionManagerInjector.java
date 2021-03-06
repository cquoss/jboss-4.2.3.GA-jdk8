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
package org.jboss.aspects.tx;

import org.jboss.aop.annotation.AnnotationElement;
import org.jboss.aop.joinpoint.ConstructorInvocation;
import org.jboss.aop.joinpoint.FieldReadInvocation;
import org.jboss.aop.joinpoint.FieldWriteInvocation;
import org.jboss.aspects.Injected;
import org.jboss.tm.TransactionManagerLocator;

import javax.transaction.TransactionManager;
import java.lang.reflect.Method;

/**
 * This aspect should be scoped PER_JOINPOINT
 * It allows a field to be like a ThreadLocal
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 57186 $
 */
public class TransactionManagerInjector
{
   public Object access(FieldReadInvocation invocation) throws Throwable
   {
      return TransactionManagerLocator.getInstance().locate();
   }

   public Object access(FieldWriteInvocation invocation) throws Throwable
   {
      throw new RuntimeException("It is illegal to set an injected TransactionManager field.");
   }

   public Object allocation(ConstructorInvocation invocation) throws Throwable
   {
      Object obj = invocation.invokeNext();

      try
      {
         Object[] arg = {TransactionManagerLocator.getInstance().locate()};
         Method[] methods = obj.getClass().getMethods();
         for (int i = 0; i < methods.length; i++)
         {
            if (methods[i].getParameterTypes().length == 1)
            {
               if (methods[i].getParameterTypes()[0].equals(TransactionManager.class))
               {
                  if (AnnotationElement.isAnyAnnotationPresent(methods[i], Injected.class))
                  {
                     methods[i].invoke(obj, arg);
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      return obj;
   }
}
