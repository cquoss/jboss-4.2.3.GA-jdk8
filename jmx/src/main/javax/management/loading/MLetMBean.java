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
package javax.management.loading;

import java.util.Set;
import java.util.Enumeration;

import java.net.URL;

import java.io.InputStream;
import java.io.IOException;

import javax.management.ServiceNotFoundException;

/**
 * Management interface of an MLet.
 *
 * @see javax.management.loading.MLet
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 57200 $  
 */
public interface MLetMBean
{

   /**
    * Loads an MLET text file from a given url. The MLET text file is parsed
    * and the declared MBeans will be registered to the MBean server.
    *
    * @param   url   url to load the MLET text file from
    *
    * @return  A set of <tt>ObjectInstance</tt> instances for each registered
    *          MBean. If there was an error registering the MBean, a
    *          <tt>Throwable</tt> instance is added to the returned collection.
    *
    * @exception ServiceNotFoundException if the given URL is malformed, or the
    *            given MLET text file cannot be found, or the given text file
    *            does not contain an &lt;MLET&gt; tag or one of the specified
    *            mandatory attributes (see the JMX specification for a list of
    *            mandatory attributes in an MLET text file).
    */
   public Set getMBeansFromURL(String url) throws ServiceNotFoundException;

   /**
    * Loads an MLET text file from a given url. The MLET text file is parsed
    * and the declared MBeans will be registered to the MBean server.
    *
    * @param   url   url to load the MLET text file from
    *
    * @return  A set of <tt>ObjectInstance</tt> instances for each registered
    *          MBean. If there was an error registering the MBean, a
    *          <tt>Throwable</tt> instance is added to the returned collection.
    *
    * @exception ServiceNotFoundException if the
    *            given MLET text file cannot be found, or the given text file
    *            does not contain an &lt;MLET&gt; tag or one of the specified
    *            mandatory attributes (see the JMX specification for a list of
    *            mandatory attributes in an MLET text file).
    */
   public Set getMBeansFromURL(URL url) throws ServiceNotFoundException;

   /**
    * Adds the given URL to the MLet's classpath.
    *
    * @param   url   url
    */
   public void addURL(URL url);

   /**
    * Adds the given URL to the MLet's classpath.
    *
    * @param   url   url
    *
    * @throws ServiceNotFoundException if the given URL is malformed
    */
   public void addURL(String url) throws ServiceNotFoundException;

   /**
    * Returns the list of URLs associated with this MLet.
    *
    * @return  array of URLs
    */
   public URL[] getURLs();

   /**
    * Returns a resource with a given name from this MLet's classpath.
    *
    * @param  name   the resource name with a '/' separated path
    *
    * @return  URL to the requested resource, or a <tt>null</tt> if it could
    *          not be found
    */
   public URL getResource(String name);

   /**
    * Returns a resource with a given name from this MLet's classpath.
    *
    * @param  name   the resource name with a '/' separated path
    *
    * @return  An InputStream to the requested resource, or a <tt>null</tt> if
    *          it could not be found
    */
   public InputStream getResourceAsStream(String name);

   /**
    * Returns all resources with a given name.
    *
    * @param   name  the resource name with a '/' separated path
    *
    * @return  an enumeration of URLs to the resource, or an empty 
    *          <tt>Enumeration</tt> instance if no resources were found
    * 
    * @throws  IOException
    */
   public Enumeration getResources(String name) throws IOException;


   public String getLibraryDirectory();


   public void setLibraryDirectory(String libdir);

}

