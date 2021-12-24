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

import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

/** Various debugging utility methods available for use in unit tests
 * @author Scott.Stark@jboss.org
 */
public class Debug
{
   /** Format a string buffer containing the Class, Interfaces, CodeSource,
    and ClassLoader information for the given object clazz.

    @param clazz the Class
    @param results, the buffer to write the info to
    */
   public static void displayClassInfo(Class clazz, StringBuffer results)
   {
      // Print out some codebase info for the ProbeHome
      ClassLoader cl = clazz.getClassLoader();
      results.append("\n"+clazz.getName()+".ClassLoader="+cl);
      ClassLoader parent = cl;
      while( parent != null )
      {
         results.append("\n.."+parent);
         URL[] urls = getClassLoaderURLs(parent);
         int length = urls != null ? urls.length : 0;
         for(int u = 0; u < length; u ++)
         {
            results.append("\n...."+urls[u]);
         }
         if( parent != null )
            parent = parent.getParent();
      }
      CodeSource clazzCS = clazz.getProtectionDomain().getCodeSource();
      if( clazzCS != null )
         results.append("\n++++CodeSource: "+clazzCS);
      else
         results.append("\n++++Null CodeSource");

      results.append("\nImplemented Interfaces:");
      Class[] ifaces = clazz.getInterfaces();
      for(int i = 0; i < ifaces.length; i ++)
      {
         results.append("\n++"+ifaces[i]);
         ClassLoader loader = ifaces[i].getClassLoader();
         results.append("\n++++ClassLoader: "+loader);
         ProtectionDomain pd = ifaces[i].getProtectionDomain();
         CodeSource cs = pd.getCodeSource();
         if( cs != null )
            results.append("\n++++CodeSource: "+cs);
         else
            results.append("\n++++Null CodeSource");
      }
   }

   /** Use reflection to access a URL[] getURLs or ULR[] getAllURLs method so
    that non-URLClassLoader class loaders, or class loaders that override
    getURLs to return null or empty, can provide the true classpath info.
    */
   public static URL[] getClassLoaderURLs(ClassLoader cl)
   {
      URL[] urls = {};
      try
      {
         Class returnType = urls.getClass();
         Class[] parameterTypes = {};
         Method getURLs = cl.getClass().getMethod("getURLs", parameterTypes);
         if( returnType.isAssignableFrom(getURLs.getReturnType()) )
         {
            Object[] args = {};
            urls = (URL[]) getURLs.invoke(cl, args);
         }
      }
      catch(Exception ignore)
      {
      }
      return urls;
   }
   
}
