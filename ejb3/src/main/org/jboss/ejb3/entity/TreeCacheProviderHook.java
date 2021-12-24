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
package org.jboss.ejb3.entity;

import java.util.Properties;

import javax.management.ObjectName;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.jboss.cache.TreeCache;
import org.jboss.cache.TreeCacheMBean;
import org.jboss.ejb3.tx.TxUtil;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * Support for integration as a 2nd level cache with an already existing 
 * JBoss Cache (TreeCache) instance.  The ObjectName of the cache is 
 * provided via the <code>hibernate.treecache.mbean.object_name</code>
 * configuration property.
 * <p/>
 * This class supports both optimistic and pessimistic locking, providing
 * instances of <code>org.hibernate.cache.OptimisticCache</code> if the 
 * underlying JBoss Cache is configured for optimistic locking.
 * 
 * @author Gavin King
 * @author Brian Stansberry
 */
public class TreeCacheProviderHook 
   extends org.hibernate.cache.TreeCacheProvider
   implements CacheProvider
{
   /**
    * Name of the Hibernate configuration property used to provide
    * the ObjectName of the JBoss Cache instance.
    */
   public static final String HIBERNATE_CACHE_OBJECT_NAME_PROPERTY = 
      "hibernate.treecache.mbean.object_name";
   
   /**
    * Default ObjectName for the JBoss Cache instance that will be used
    * if {@link HIBERNATE_CACHE_OBJECT_NAME_PROPERTY} is not provided.
    */
   public static final String DEFAULT_MBEAN_OBJECT_NAME = "jboss.cache:service=EJB3EntityTreeCache";
   
   protected Logger log = Logger.getLogger(getClass());
   
   private org.jboss.cache.TreeCache cache;
   private boolean optimistic;
   
   /**
    * Construct and configure the Cache representation of a named cache region.
    *
    * @param regionName the name of the cache region
    * @param properties configuration settings
    * @return The Cache representation of the named cache region.  If the
    *         underlying JBoss Cache is configured for optimistic locking,
    *         the returned object will also implement org.hibernate.cache.OptimisticCache.
    * @throws org.hibernate.cache.CacheException
    *          Indicates an error building the cache region.
    */
   public Cache buildCache(String regionName, Properties properties) throws CacheException
   {
      String regionPrefix = properties.getProperty("hibernate.cache.region_prefix");
      
      if (optimistic)
      {
         return new OptimisticJBCCache(cache, regionName, regionPrefix);
      }
      else
      {
         return new JBCCache(cache, regionName, regionPrefix, TxUtil.getTransactionManager());
      }
   }

   public boolean isMinimalPutsEnabledByDefault()
   {
      return true;
   }

   public long nextTimestamp()
   {
      return System.currentTimeMillis() / 100;
   }

   /**
    * Find the underlying JBoss Cache TreeCache instance.
    *
    * @param properties  All current config settings. 
    *                    If {@link #HIBERNATE_CACHE_OBJECT_NAME_PROPERTY} is provided,
    *                    the value will be the expected name of the cache; otherwise
    *                    {@link #DEFAULT_MBEAN_OBJECT_NAME} will be used.
    * @throws org.hibernate.cache.CacheException
    *          Indicates a problem preparing cache for use.
    */
   public void start(Properties properties)
   {
      try
      {
         String cacheName = (String) properties.get(HIBERNATE_CACHE_OBJECT_NAME_PROPERTY);
         if (cacheName == null)
         {
            cacheName = DEFAULT_MBEAN_OBJECT_NAME;
         }
         ObjectName mbeanObjectName = new ObjectName(cacheName);
         TreeCacheMBean mbean = (TreeCacheMBean) MBeanProxyExt.create(TreeCacheMBean.class, mbeanObjectName, MBeanServerLocator.locateJBoss());
         cache = mbean.getInstance();
         if ("OPTIMISTIC".equals(cache.getNodeLockingScheme()))
         {
            optimistic = true;
            log.debug("JBoss Cache is configured for optimistic locking; " +
                    "provided Cache implementations will also implement OptimisticCache");
         }
      }
      catch (Exception e)
      {
         throw new CacheException(e);
      }
   }

   public void stop()
   {
   }
   
   public boolean isOptimistic()
   {
      return optimistic;
   }

   public TreeCache getUnderlyingCache()
   {
      return cache;
   }

}
