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
package org.jboss.test.cluster.web;

import org.jboss.cache.aop.TreeCacheAop;
import org.jboss.cache.aop.TreeCacheAopMBean;
import org.jboss.mx.util.MBeanProxyExt;

import javax.management.ObjectName;

/**
 * Helper class to locate the cache mbean used by Tomcat
 *
 * @author Ben Wang
 *         Date: Aug 16, 2005
 * @version $Id: CacheHelper.java 57211 2006-09-26 12:39:46Z dimitris@jboss.org $
 */
public class CacheHelper {
   public static final String DEFAULT_CACHE_NAME =
      "jboss.cache:service=TomcatClusteringCache";

   public static TreeCacheAop getCacheInstance()
   {
      try
      {
         ObjectName cacheServiceName_ = new ObjectName(DEFAULT_CACHE_NAME);
         // Create Proxy-Object for this service
         TreeCacheAop proxy_ = (TreeCacheAop)((TreeCacheAopMBean) MBeanProxyExt.create(TreeCacheAopMBean.class,
                 cacheServiceName_)).getInstance();
         if (proxy_ == null)
         {
            throw new RuntimeException("getCacheInstance: locate null TomcatCacheAop");
         }
         return proxy_;
      }
      catch (Throwable e)
      {
         e.printStackTrace();
         throw new RuntimeException("getCacheInstance: Exception: " +e);
      }
   }
}
