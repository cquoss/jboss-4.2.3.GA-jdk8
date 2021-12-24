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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Utilities for accessing server configuration. Note that this class cannot
 * have dependencies on anything but bootstrap and jdk classes. Anything more
 * than this needs to be defined in the ServerConfig interface and provided
 * in the associated implementation.
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version <tt>$Revision: 57205 $</tt>
 */
public class ServerConfigUtil
{
   private static final String ANY = "0.0.0.0";
   
   /**
    * Retrieve the default bind address for the server
    * 
    * @return the default bind adress
    */
   public static String getDefaultBindAddress()
   {
      return System.getProperty(ServerConfig.SERVER_BIND_ADDRESS);
   }

   /**
    * Retrieve the default bind address, but only if it is specific
    * 
    * @return the specific bind address
    */
   public static String getSpecificBindAddress()
   {
      String address = System.getProperty(ServerConfig.SERVER_BIND_ADDRESS);
      if (address == null || address.equals(ANY))
         return null;
      return address;
   }

   /**
    * Fix the remote inet address.
    * 
    * If we pass the address to the client we don't want to
    * tell it to connect to 0.0.0.0, use our host name instead
    * @param address the passed address
    * @return the fixed address
    */
   public static InetAddress fixRemoteAddress(InetAddress address)
   {
      try
      {
         if (address == null || InetAddress.getByName(ANY).equals(address))
            return InetAddress.getLocalHost();
      }
      catch (UnknownHostException ignored)
      {
      }
      return address;
   }

   /**
    * Fix the remote address.
    * 
    * If we pass the address to the client we don't want to
    * tell it to connect to 0.0.0.0, use our host name instead
    * @param address the passed address
    * @return the fixed address
    */
   public static String fixRemoteAddress(String address)
   {
      try
      {
         if (address == null || ANY.equals(address))
            return InetAddress.getLocalHost().getHostName();
      }
      catch (UnknownHostException ignored)
      {
      }
      return address;
   }

   /**
    * Get the default partition name
    * 
    * @return the default partition name
    */
   public static String getDefaultPartitionName()
   {
      return System.getProperty(ServerConfig.PARTITION_NAME_PROPERTY, ServerConfig.DEFAULT_PARITION_NAME);
   }

   /**
    * Whether to load native directories
    * 
    * @return true when loading native directories
    */
   public static boolean isLoadNative()
   {
      return Boolean.getBoolean(ServerConfig.NATIVE_LOAD_PROPERTY);
   }

   /**
    * Utility to get a shortened url relative to the server home if possible
    * 
    * @param longUrl
    * @return the short url
    */
   public static String shortUrlFromServerHome(String longUrl)
   {
      String serverHomeUrl = System.getProperty(org.jboss.system.server.ServerConfig.SERVER_HOME_URL);

      if (longUrl == null || serverHomeUrl == null)
          return longUrl;

      if (longUrl.startsWith(serverHomeUrl))
        return ".../" + longUrl.substring(serverHomeUrl.length());
      else
        return longUrl;
   }
}
