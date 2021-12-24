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
package org.jboss.mx.loading;

import java.net.URLStreamHandlerFactory;

import org.jboss.util.loading.DelegatingClassLoader;

/**
 * A delegating classloader that first peeks in the loader
 * repository's cache.
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57200 $
 */
public class LoaderRepositoryClassLoader
   extends DelegatingClassLoader
{
   /** The loader repository */
   protected LoaderRepository repository;

   /**
    * Constructor
    *
    * @param parent the parent classloader, cannot be null.
    * @param repository the loader repository, cannot be null.
    */
   public LoaderRepositoryClassLoader(ClassLoader parent, LoaderRepository repository)
   {
      super(parent);
      if (repository == null)
         throw new IllegalArgumentException("No repository");
      this.repository = repository;
   }

   /**
    * Constructor
    *
    * @param parent, the parent classloader, cannot be null.
    * @param factory the url stream factory.
    */
   public LoaderRepositoryClassLoader(ClassLoader parent, LoaderRepository repository, URLStreamHandlerFactory factory)
   {
      super(parent);
      if (repository == null)
         throw new IllegalArgumentException("No repository");
      this.repository = repository;
   }

   /**
    * Load a class, first peek in the loader repository cache then
    * ask the parent.
    *
    * @param className the class name to load
    * @param resolve whether to link the class
    * @return the loaded class
    * @throws ClassNotFoundException when the class could not be found
    */
   protected Class loadClass(String className, boolean resolve)
      throws ClassNotFoundException
   {
      Class clazz = repository.getCachedClass(className);
      if (clazz != null)
      {
         if (resolve)
            resolveClass(clazz);
         return clazz;
      }

      // Delegate
      return super.loadClass(className, resolve);
   }
}
