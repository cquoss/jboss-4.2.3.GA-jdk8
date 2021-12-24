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

import java.net.URL;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.loading.MLet;

import org.jboss.logging.Logger;

/**
 * A RepositoryClassLoader that wraps an MLet.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 */
class MLetRepositoryClassLoader extends RepositoryClassLoader
{
   // Constants -----------------------------------------------------

   /** The log */
   private static final Logger log = Logger.getLogger(MLetRepositoryClassLoader.class);

   // Attributes -----------------------------------------------------

   /** The MLet */
   private MLet mlet;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
    * Create a new LoaderRepositoryClassLoader
    * 
    * @param urls the urls
    * @param parent the parent classloader
    */
   protected MLetRepositoryClassLoader(MLet mlet)
   {
      super(mlet.getURLs(), mlet);
      this.mlet = mlet;
   }
   
   // Public --------------------------------------------------------

   /**
    * Get the ObjectName
    * 
    * @return the object name
    */
   public ObjectName getObjectName() throws MalformedObjectNameException
   {
      throw new UnsupportedOperationException("Not relevent");
   }

   /**
    * This method simply invokes the super.getURLs() method to access the
    * list of URLs that make up the RepositoryClassLoader classpath.
    * 
    * @return the urls that make up the classpath
    */
   public URL[] getClasspath()
   {
      return mlet.getURLs();
   }

   /**
    * Return all library URLs associated with this RepositoryClassLoader
    *
    * <p>Do not remove this method without running the WebIntegrationTestSuite
    */
   public URL[] getAllURLs()
   {
      return repository.getURLs();
   }
   
   // URLClassLoader overrides --------------------------------------

   public Class loadClassLocally(String name, boolean resolve)
      throws ClassNotFoundException
   {
      boolean trace = log.isTraceEnabled();
      if( trace )
         log.trace("loadClassLocally, " + this + " name=" + name);
      Class result = null;
      try
      {
         result = mlet.loadClass(name, null);
         return result;
      }
      finally
      {
         if (trace)
         {
            if (result != null)
               log.trace("loadClassLocally, " + this + " name=" + name + " class=" + result + " cl=" + result.getClassLoader());
            else
               log.trace("loadClassLocally, " + this + " name=" + name + " not found");
         }
      }
   }
   
   // Object overrides ----------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Package Private -----------------------------------------------
   
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
