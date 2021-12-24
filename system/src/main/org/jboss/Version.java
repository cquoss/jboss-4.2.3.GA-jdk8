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
package org.jboss;

import java.io.IOException;
import java.io.InputStream;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Provides access to JBoss version (and build) properties.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 59329 $
 */
public final class Version
{
   public final static String VERSION_MAJOR = "version.major";
   public final static String VERSION_MINOR = "version.minor";
   public final static String VERSION_REVISION = "version.revision";
   public final static String VERSION_TAG = "version.tag";
   public final static String VERSION_NAME = "version.name";
   public final static String VERSION_CVSTAG = "version.cvstag";

   public final static String BUILD_NUMBER = "build.number";
   public final static String BUILD_ID = "build.id";
   public final static String BUILD_DATE = "build.day";
   public final static String BUILD_JVM_VERSION = "java.vm.version";
   public final static String BUILD_JVM_VENDOR = "java.vendor";
   public final static String BUILD_OS = "os.name";
   public final static String BUILD_OS_ARCH = "os.arch";
   public final static String BUILD_OS_VERSION = "os.version";

   /**
    * The single instance.
    */
   private static Version instance = null;

   /**
    * The version properties.
    */
   private Properties props;

   /**
    * Do not allow direct public construction.
    */
   private Version()
   {
      props = loadProperties();
   }

   /**
    * Get the single <tt>Version</tt> instance.
    *
    * @return The single <tt>Version</tt> instance.
    */
   public static Version getInstance()
   {
      if (instance == null)
      {
         instance = new Version();
      }
      return instance;
   }

   /**
    * Returns an unmodifiable map of version properties.
    *
    * @return An unmodifiable map of version properties.
    */
   public Map getProperties()
   {
      return Collections.unmodifiableMap(props);
   }

   /**
    * Returns the value for the given property name.
    *
    * @param name - The name of the property.
    * @return The property value or null if the property is not set.
    */
   public String getProperty(final String name)
   {
      return props.getProperty(name);
   }

   /**
    * Returns the major number of the version.
    *
    * @return The major number of the version.
    */
   public int getMajor()
   {
      return getIntProperty(VERSION_MAJOR);
   }

   /**
    * Returns the minor number of the version.
    *
    * @return The minor number of the version.
    */
   public int getMinor()
   {
      return getIntProperty(VERSION_MINOR);
   }

   /**
    * Returns the revision number of the version.
    *
    * @return The revision number of the version.
    */
   public int getRevision()
   {
      return getIntProperty(VERSION_REVISION);
   }

   /**
    * Returns the tag of the version.
    *
    * @return The tag of the version.
    */
   public String getTag()
   {
      return props.getProperty(VERSION_TAG);
   }
   /**
    * Returns the CVS tag of the version.
    *
    * @return The CVS tag of the version.
    */
   public String getCvsTag()
   {
      return props.getProperty(VERSION_CVSTAG);
   }

   /**
    * Returns the name number of the version.
    *
    * @return The name of the version.
    */
   public String getName()
   {
      return props.getProperty(VERSION_NAME);
   }

   /**
    * Returns the build identifier for this version.
    *
    * @return The build identifier for this version.
    */
   public String getBuildID()
   {
      return props.getProperty(BUILD_ID);
   }

   /**
    * Returns the build number for this version.
    *
    * @return The build number for this version.
    */
   public String getBuildNumber()
   {
      return props.getProperty(BUILD_NUMBER);
   }

   /**
    * Returns the build date for this version.
    *
    * @return The build date for this version.
    */
   public String getBuildDate()
   {
      return props.getProperty(BUILD_DATE);
   }

   /** Returns the BUILD_JVM_VERSION (BUILD_JVM_VENDOR) which should look like:
    * 1.4.2_05-b04 (Sun Microsystems Inc.)
    * @return
    */ 
   public String getBuildJVM()
   {
      String vm = props.getProperty(BUILD_JVM_VERSION);
      String vendor = props.getProperty(BUILD_JVM_VENDOR);
      return vm + '(' + vendor + ')';
   }

   /** Returns the BUILD_OS (BUILD_OS_ARCH,BUILD_OS_VERSION) which should look
    * like:
    * Windows XP (x86,5.1)
    * Linux (i386,2.4.21-4.ELsmp)
    * @return
    */ 
   public String getBuildOS()
   {
      String os = props.getProperty(BUILD_OS);
      String arch = props.getProperty(BUILD_OS_ARCH);
      String version = props.getProperty(BUILD_OS_VERSION);
      return os + '(' + arch +',' + version + ')';
   }

   /**
    * Returns the full version number, e.g. 5.0.0.GA
    * 
    * @return The full version number as string
    */
   public String getVersionNumber()
   {
      StringBuffer buff = new StringBuffer();
      
      buff.append(getMajor()).append(".");
      buff.append(getMinor()).append(".");
      buff.append(getRevision()).append(".");
      buff.append(getTag());
      
      return buff.toString();      
   }
   
   /**
    * Returns the version information as a string.
    *
    * @return Basic information as a string.
    */
   public String toString()
   {
      StringBuffer buff = new StringBuffer();
      
      buff.append(getVersionNumber());
      buff.append(" (build: SVNTag=");
      buff.append(getCvsTag());
      buff.append(" date=");
      buff.append(getBuildID());
      buff.append(")");
      
      return buff.toString();
   }

   /**
    * Returns a property value as an int.
    *
    * @param name - The name of the property.
    * @return The property value, or -1 if there was a problem converting
    *         it to an int.
    */
   private int getIntProperty(final String name)
   {
      try
      {
         return Integer.valueOf(props.getProperty(name)).intValue();
      }
      catch (Exception e)
      {
         return -1;
      }
   }

   /**
    * Load the version properties from a resource.
    */
   private Properties loadProperties()
   {
      props = new Properties();

      try
      {
         InputStream in =
            Version.class.getResourceAsStream("/org/jboss/version.properties");

         props.load(in);
         in.close();
      }
      catch (IOException e)
      {
         throw new Error("Missing version.properties");
      }

      return props;
   }
}
