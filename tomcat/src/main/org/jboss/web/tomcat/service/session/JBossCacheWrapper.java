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

import org.jboss.cache.Fqn;
import org.jboss.cache.aop.PojoCacheMBean;
import org.jboss.cache.config.Option;
import org.jboss.cache.lock.TimeoutException;

import java.util.Map;

public class JBossCacheWrapper 
{
   private static final int RETRY = 3;
   private PojoCacheMBean proxy_;

   JBossCacheWrapper(PojoCacheMBean cache)
   {
      proxy_ = cache;
   }

   /**
    * Wrapper to embed retyr logic.
    *
    * @param fqn
    * @param id
    * @return
    */
   Object get(Fqn fqn, String id)
   {
      return get(fqn, id, false);
   }

   /**
    * Wrapper to embed retyr logic.
    *
    * @param fqn
    * @param id
    * @return
    */
   Object get(Fqn fqn, String id, boolean gravitate)
   {
      Exception ex = null;
      for (int i = 0; i < RETRY; i++)
      {
         try
         {
            Object value = null;
            if (gravitate)
            {
               Option option = new Option();
               option.setForceDataGravitation(true);
               value = proxy_.get(fqn, id, option);
            }
            else
            {
               value = proxy_.get(fqn, id);
            }
            return value;
         }
         catch (TimeoutException e)
         {
            ex = e;
         }
         catch (Exception e)
         {
            if (e instanceof RuntimeException)
               throw (RuntimeException) e;
            throw new RuntimeException("JBossCacheService: exception occurred in cache get ... ", e);
         }
      }
      throw new RuntimeException("JBossCacheService: exception occurred in cache get after retry ... ", ex);
   }

   /**
    * Wrapper to embed retry logic.
    *
    * @param fqn
    * @param id
    * @param value
    * @return
    */
   void put(Fqn fqn, String id, Object value)
   {
      Exception ex = null;
      for (int i = 0; i < RETRY; i++)
      {
         try
         {
            proxy_.put(fqn, id, value);
            return;
         }
         catch (TimeoutException e)
         {
            ex = e;
         }
         catch (Exception e)
         {
            throw new RuntimeException("JBossCacheService: exception occurred in cache put ... ", e);
         }
      }
      throw new RuntimeException("JBossCacheService: exception occurred in cache put after retry ... ", ex);
   }


   /**
    * Wrapper to embed retry logic.
    *
    * @param fqn
    * @param map
    */
   void put(Fqn fqn, Map map)
   {
      Exception ex = null;
      for (int i = 0; i < RETRY; i++)
      {
         try
         {
            proxy_.put(fqn, map);
            return;
         }
         catch (TimeoutException e)
         {
            ex = e;
         }
         catch (Exception e)
         {
            throw new RuntimeException("JBossCacheService: exception occurred in cache put ... ", e);
         }
      }
      throw new RuntimeException("JBossCacheService: exception occurred in cache put after retry ... ", ex);
   }

   /**
    * Wrapper to embed retyr logic.
    *
    * @param fqn
    * @param id
    * @return
    */
   Object remove(Fqn fqn, String id)
   {
      Exception ex = null;
      for (int i = 0; i < RETRY; i++)
      {
         try
         {
            return proxy_.remove(fqn, id);
         }
         catch (TimeoutException e)
         {
            ex = e;
         }
         catch (Exception e)
         {
            throw new RuntimeException("JBossCacheService: exception occurred in cache remove ... ", e);
         }
      }
      throw new RuntimeException("JBossCacheService: exception occurred in cache remove after retry ... ", ex);
   }

   /**
    * Wrapper to embed retry logic.
    *
    * @param fqn
    */
   void remove(Fqn fqn)
   {
      Exception ex = null;
      for (int i = 0; i < RETRY; i++)
      {
         try
         {
            proxy_.remove(fqn);
            return;
         }
         catch (TimeoutException e)
         {
            ex = e;
         }
         catch (Exception e)
         {
            throw new RuntimeException("JBossCacheService: exception occurred in cache remove ... ", e);
         }
      }
      throw new RuntimeException("JBossCacheService: exception occurred in cache remove after retry ... ", ex);
   }

   /**
    * Wrapper to embed retry logic.
    *
    * @param fqn
    */
   void evict(Fqn fqn)
   {
      Exception ex = null;
      for (int i = 0; i < RETRY; i++)
      {
         try
         {
            proxy_.evict(fqn);
            return;
         }
         catch (TimeoutException e)
         {
            ex = e;
         }
         catch (Exception e)
         {
            throw new RuntimeException("JBossCacheService: exception occurred in cache evict ... ", e);
         }
      }
      throw new RuntimeException("JBossCacheService: exception occurred in cache evict after retry ... ", ex);
   }
   
   void evictSubtree(Fqn fqn)
   {
      
      Exception ex = null;
      for (int i = 0; i < RETRY; i++)
      {
         try
         {
            // Just do a local-only remove
            Option option = new Option();
            option.setCacheModeLocal(true);
            proxy_.remove(fqn, option);
            return;            
         }
         catch (TimeoutException e)
         {
            ex = e;
         }
         catch (Exception e)
         {
            throw new RuntimeException("JBossCacheService: exception occurred in cache evictSubtree ... ", e);
         }
      }
      throw new RuntimeException("JBossCacheService: exception occurred in cache evictSubtree after retry ... ", ex);
   }

}
