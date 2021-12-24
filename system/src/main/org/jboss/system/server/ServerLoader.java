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
package org.jboss.system.server;

// $Id: ServerLoader.java 60584 2007-02-16 11:02:57Z dimitris@jboss.org $

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * A helper class to load a JBoss server instance.
 *
 * <p>Basic usage is something like this:
 * <pre>
 *    // setup the basic server config properties
 *    Properties props = new Properties(System.getProperties());
 *    props.put(ServerConfig.SERVER_LIBRARY_URL, "http://myserver.com/myjboss/lib/");
 *    // set some more properties
 *
 *    // create a new loader to do the dirty work
 *    ServerLoader loader = new ServerLoader(props);
 *
 *    // add the jaxp & jmx library to use
 *    loader.addLibrary("crimson.jar");
 *    loader.addLibrary("jboss-jmx-core.jar");
 *
 *    // load and initialize the server instance
 *    ClassLoader parent = Thread.currentThread().getContextClassLoader();
 *    Server server = loader.load(parent);
 *    server.init(props);
 *
 *    // start up the server
 *    server.start();
 *
 *    // go make some coffee, drink a beer or play GTA3
 *    // ...
 *
 *    // shutdown and go to sleep
 *    server.shutdown();
 * </pre>
 * @version <tt>$Revision: 60584 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @author Scott.Stark@jboss.org
 * @author Thomas.Diesler@jboss.org
 */
public class ServerLoader
{
   /**
    * The default list of boot libraries.  Does not include
    * the JAXP or JMX impl, users of this class should add the
    * proper libraries.
    */
   public static final String DEFAULT_BOOT_LIBRARY_LIST =
      "log4j-boot.jar,jboss-common.jar,jboss-system.jar,jboss-xml-binding.jar";

   /** The default server type. */
   public static final String DEFAULT_SERVER_TYPE = "org.jboss.system.server.ServerImpl";

   /**
    * Configuration properties.
    */
   protected Properties props;

   /**
    * The URL where libraries are read from.
    */
   protected URL libraryURL;

   /**
    * A list of extra URLs to add to the classpath when loading
    * the server.
    */
   protected List extraClasspath = new LinkedList();

   /**
    * Construct a <tt>ServerLoader</tt>.
    *
    * @param props    Configuration properties.
    *
    * @throws Exception    Invalid configuration
    */
   public ServerLoader(final Properties props) throws Exception
   {
      if (props == null)
         throw new IllegalArgumentException("props is null");

      this.props = props;

      // must have HOME_URL, or we can't continue
      URL homeURL = getURL(ServerConfig.HOME_URL);
      if (homeURL == null)
      {
         throw new Exception("Missing configuration value for: "
            + ServerConfig.HOME_URL);
      }

      libraryURL = getURL(ServerConfig.LIBRARY_URL);
      if (libraryURL == null)
      {
         // need library url to make boot urls list
         libraryURL = new URL(homeURL, ServerConfig.LIBRARY_URL_SUFFIX);
      }

      // If the home URL begins with http add the webav and httpclient jars
      if( homeURL.getProtocol().startsWith("http") == true )
      {
         this.addLibrary("webdavlib.jar");
         this.addLibrary("commons-httpclient.jar");
         this.addLibrary("commons-codec.jar");
         this.addLibrary("commons-logging.jar");
      }
   }

   /**
    * Add an extra library to the end of list of libraries
    * which will be loaded from the library URL when loading
    * the Server class.
    *
    * @param filename   A filename (no directory parts)
    *
    * @throws MalformedURLException   Could not generate URL from library URL + filename
    */
   public void addLibrary(final String filename) throws MalformedURLException
   {
      if (filename == null)
         throw new IllegalArgumentException("filename is null");

      URL jarURL = new URL(libraryURL, filename);
      extraClasspath.add(jarURL);
   }

   /**
    * Add a list of comma seperated library file names.
    *
    * @param filenames   A list of comma seperated filenames (with no directory parts)
    *
    * @throws MalformedURLException   Could not generate URL from library URL + filename
    */
   public void addLibraries(final String filenames) throws MalformedURLException
   {
      if (filenames == null)
         throw new IllegalArgumentException("filenames is null");

      StringTokenizer stok = new StringTokenizer(filenames, ",");
      while (stok.hasMoreElements())
      {
         addLibrary(stok.nextToken().trim());
      }
   }

   /**
    * Add an extra URL to the classpath used to load the server.
    *
    * @param url    A URL to add to the classpath.
    */
   public void addURL(final URL url)
   {
      if (url == null)
         throw new IllegalArgumentException("url is null");

      extraClasspath.add(url);
   }

   /**
    * Add the jars from the lib/endorsed dir if it exists.
    * Note, the path must exist locally for this to work.
    * @throws MalformedURLException  Could not generate URL from library URL + filename
    */
   public void addEndorsedJars() throws MalformedURLException
   {
      File endorsedDir = new File(libraryURL.getPath() + "/endorsed");
      if (endorsedDir.exists())
      {
         String [] list = endorsedDir.list();
         for (int i = 0; list != null && i < list.length; i++)
         {
            String jarname = list[i];
            addLibrary("endorsed/" + jarname);
         }
      }
   }

   /**
    * Get a URL from configuration or system properties.
    */
   protected URL getURL(final String name) throws MalformedURLException
   {
      String value = props.getProperty(name, null);
      if (value != null)
      {
         if (!value.endsWith("/")) value += "/";
         return new URL(value);
      }
      return null;
   }

   /**
    * Returns an array of URLs which will be used to load the
    * core system and construct a new Server object instance.
    */
   protected URL[] getBootClasspath() throws MalformedURLException
   {
      List list = new LinkedList();

      // prepend users classpath to allow for overrides
      list.addAll(extraClasspath);

      String value = props.getProperty(ServerConfig.BOOT_LIBRARY_LIST, DEFAULT_BOOT_LIBRARY_LIST);

      StringTokenizer stok = new StringTokenizer(value, ",");
      while (stok.hasMoreElements())
      {
         URL url = new URL(libraryURL, stok.nextToken().trim());
         list.add(url);
      }

      return (URL[]) list.toArray(new URL[list.size()]);
   }

   /**
    * Load a {@link Server} instance.
    *
    * @parent    The parent of any class loader created during boot.
    * @return    An uninitialized (and unstarted) Server instance.
    *
    * @throws Exception   Failed to load or create Server instance.
    */
   public Server load(final ClassLoader parent) throws Exception
   {
      Server server;
      ClassLoader oldCL = Thread.currentThread().getContextClassLoader();

      try
      {
         // get the boot lib list
         URL[] urls = getBootClasspath();
         URLClassLoader classLoader = new NoAnnotationURLClassLoader(urls, parent);
         Thread.currentThread().setContextClassLoader(classLoader);

         // construct a new Server instance
         String typename = props.getProperty(ServerConfig.SERVER_TYPE, DEFAULT_SERVER_TYPE);
         server = createServer(typename, classLoader);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCL);
      }

      // thats all folks, have fun
      return server;
   }

   /**
    * Construct a new instance of Server, loading all required classes from
    * the given ClossLoader.
    */
   protected Server createServer(final String typename, final ClassLoader classLoader) throws Exception
   {
      // load the class first
      Class type = classLoader.loadClass(typename);

      // and then create a new instance
      Server server = (Server) type.newInstance();

      // here ya go
      return server;
   }
}
