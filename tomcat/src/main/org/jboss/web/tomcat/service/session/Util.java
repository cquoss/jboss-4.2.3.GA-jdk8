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
package org.jboss.web.tomcat.service.session;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.aop.Advised;

/**
 * Utility methods related to JBoss distributed sessions.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public class Util
{
   // Types that are considered "primitive".
   private static final Set immediates =
      new HashSet(Arrays.asList(new Object[]{
         String.class,
         Boolean.class,
         Double.class,
         Float.class,
         Integer.class,
         Long.class,
         Short.class,
         Character.class,
         Boolean.TYPE,
         Double.TYPE,
         Float.TYPE,
         Integer.TYPE,
         Long.TYPE,
         Short.TYPE,
         Character.TYPE,
         Class.class}));

   /**
    * Returns a session id with any trailing jvmRoute removed.
    * 
    * @param sessionId the raw session id
    * 
    * @return <code>sessionId</code> with the final '.' and any
    *         characters thereafter removed.
    */
   public static String getRealId(String sessionId)
   {
      int index = sessionId.lastIndexOf(".");
      if (index > 0)
      {
         return sessionId.substring(0, index);
      }
      else
      {
         return sessionId;
      }
   }

   /**
    * Checks whether the given object is usable for FIELD granularity 
    * replication.
    * 
    * @param pojo  the pojo
    * @return <code>true</code> if the attribute type is acceptable,
    *         <code>false</code> otherwise
    */
   public static boolean checkPojoType(Object pojo)
   {
      return (   (pojo instanceof Serializable)
              || (pojo instanceof Collection) 
              || (pojo instanceof Map) 
              || (pojo instanceof Advised)
              || (immediates.contains(pojo.getClass())));
   }
   
   public static Set parseComplexFields(Class clazz)
   {
      Set result = new HashSet();
      
      while (clazz != null)
      {
         Field[] fields = clazz.getDeclaredFields();
         for (int i = 0; i < fields.length; i++)
         {
            if (!immediates.contains(fields[i].getType()) 
                  && isReplicatable(fields[i]))
            {
               result.add(fields[i].getName());
            }
         }
         
         clazz = clazz.getSuperclass();
      }
      return result;
   }

   /**
    * Returns false if the given field is static, transient or final.
    * 
    * @param f the field
    * @return
    */
   public static boolean isReplicatable(Field f) {
      int mods = f.getModifiers();
      /**
       * The following modifiers are ignored in the cache, i.e., they will not be stored in the cache.
       * Whenever, user trying to access these fields, it will be accessed from the in-memory version.
       */
      if (Modifier.isStatic(mods)
            || Modifier.isTransient(mods)
            || Modifier.isFinal(mods)) {
         return false;
      }
      return true;
   }

   /**
    * Prevent instantiation.
    */
   private Util() {}

}
