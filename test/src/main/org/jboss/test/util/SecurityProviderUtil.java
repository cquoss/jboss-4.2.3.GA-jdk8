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
package org.jboss.test.util;

import java.security.Provider;

import org.jboss.logging.Logger;

import java.util.Properties;

//$Id$

/**
 *  Util class that deals with Security Providers as part
 *  of the JVM
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Aug 8, 2006 
 *  @version $Revision$
 */
public class SecurityProviderUtil
{
   private static Logger log = Logger.getLogger(SecurityProviderUtil.class);
   
   //Pass the system properties as part of the test suite setup - based on your VM
   
   private static String sslProtocolClass = System.getProperty("www.protocol.class",
                                              "com.sun.net.ssl.internal.www.protocol");
   
   private static final String PROP_FILE_NAME = "security-provider.properties";
   private static final String JSSE_PROV_NAME_PREFIX = "jsse.provider.class.";

   /**
    * Get a JSSE Security Provider
    * @return
    */
   public static Provider getJSSEProvider() 
   {
      String jsseProviderName = null;

      Properties prop = new Properties();
      try
      {
         prop.load(ClassLoader.getSystemResourceAsStream(PROP_FILE_NAME));
      } catch (java.io.IOException ioe)
      {
         log.error("Error loading the property file: ", ioe);
         return null;
      }

      boolean searched = false;
      boolean found = false;
      int i=1;
      while (!searched)
      {
         if (prop.containsKey(JSSE_PROV_NAME_PREFIX + i))
         {
            String jsseProvider = prop.getProperty(JSSE_PROV_NAME_PREFIX + i);
            try
            {
               Class.forName(jsseProvider);
               jsseProviderName = jsseProvider;
               log.debug ("Using JSEE Provider :" + jsseProvider);
               searched = true;
            } catch (ClassNotFoundException cnfe)
            {
               log.debug ("Could not find JSEE Provider :" + jsseProvider );
            }
         } else
         {
            log.debug("Unable to read " + JSSE_PROV_NAME_PREFIX + i + " - no other properties");
            searched = true;
         }
         i++;
      }
      if (jsseProviderName == null)
      {
         log.error ("No JSSE Providers found. Please add the appropriate provider in providers.properties");
      }

      Provider obj = null;
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      Class clazz;
      try
      {
         clazz = tcl.loadClass(jsseProviderName);
         obj = (Provider)clazz.newInstance();
      }
      catch (Throwable t)
      {
         log.error("getJSSEProvider error:", t);
      }
      return obj;
   }
   
   /**
    * Get the https protocl handler
    * @return
    */
   public static String getProtocolHandlerName()
   {
      return sslProtocolClass;
   }
}
