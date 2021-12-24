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
package org.jboss.test.classloader.circularity.test;

import org.jboss.mx.loading.ClassLoadingTask;
import org.jboss.mx.loading.UnifiedClassLoader3;

/** Extension of the ClassLoadingTask allows access to protected members
 * for testing
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57211 $
 */
public class MyClassLoadingTask extends ClassLoadingTask
{
   public MyClassLoadingTask(String classname,
      UnifiedClassLoader3 requestingClassLoader,
      Thread requestingThread)
   {
      super(classname, requestingClassLoader, requestingThread);
   }

   public int threadTaskCount()
   {
      return threadTaskCount;
   }

   public int state()
   {
      return state;
   }

   public Class loadedClass()
   {
      return loadedClass;
   }

   public Throwable loadException()
   {
      return loadException;
   }
}