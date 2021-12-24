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
package org.jboss.deployment;

import java.net.URL;
import javax.management.ObjectName;
import org.jboss.mx.loading.LoaderRepositoryFactory;
import org.jboss.mx.loading.RepositoryClassLoader;
import org.jboss.system.ServiceMBeanSupport;


/** A service that allows one to add an arbitrary URL to a named LoaderRepository. 
 *
 * Created: Sun Jun 30 13:17:22 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 57205 $
 *
 * @jmx:mbean name="jboss:type=Service,service=ClasspathExtension"
 *            extends="org.jboss.system.ServiceMBean"
 */
public class ClasspathExtension
   extends ServiceMBeanSupport
   implements ClasspathExtensionMBean
{
   private String metadataURL;
   private ObjectName loaderRepository;
   private RepositoryClassLoader ucl;

   public ClasspathExtension() 
   {
      
   }

   /**
    * mbean get-set pair for field metadataURL
    * Get the value of metadataURL
    * @return value of metadataURL
    *
    * @jmx:managed-attribute
    */
   public String getMetadataURL()
   {
      return metadataURL;
   }

   /**
    * Set the value of metadataURL
    * @param metadataURL  Value to assign to metadataURL
    *
    * @jmx:managed-attribute
    */
   public void setMetadataURL(String metadataURL)
   {
      this.metadataURL = metadataURL;
   }

   /**
    * mbean get-set pair for field loaderRepository
    * Get the value of loaderRepository
    * @return value of loaderRepository
    *
    * @jmx:managed-attribute
    */
   public ObjectName getLoaderRepository()
   {
      return loaderRepository;
   }
   
   
   /**
    * Set the value of loaderRepository
    * @param loaderRepository  Value to assign to loaderRepository
    *
    * @jmx:managed-attribute
    */
   public void setLoaderRepository(ObjectName loaderRepository)
   {
      this.loaderRepository = loaderRepository;
   }

   protected void createService() throws Exception
   {
      if (metadataURL != null) 
      {
         URL url = new URL(metadataURL);
         if( loaderRepository == null )
            loaderRepository = LoaderRepositoryFactory.DEFAULT_LOADER_REPOSITORY;
         Object[] args = {url, url, Boolean.TRUE};
         String[] sig = {"java.net.URL", "java.net.URL", "boolean"};
         ucl = (RepositoryClassLoader) server.invoke(loaderRepository,
            "newClassLoader",args, sig);
      } // end of if ()
   }

   protected void destroyService() throws Exception
   {
      if (ucl != null) 
         ucl.unregister();
   }

}// ClasspathExtension
