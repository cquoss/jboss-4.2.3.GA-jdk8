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
package org.jboss.hibernate.cache;

import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.transaction.TransactionManager;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.jboss.cache.TreeCache;
import org.jboss.cache.TreeCacheMBean;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.tm.TransactionManagerLocator;

/**
 * A Hibernate CacheProvider implementation which knows how to
 * obtain a deployed JBossCache via its JMX ObjectName.
 *
 * @version <tt>$Revision: 57193 $</tt>
 * @author <a href="mailto:steve@hibernate.org">Steve Ebersole</a>
 */
public class DeployedTreeCacheProvider implements CacheProvider
{
   private static final Logger log = Logger.getLogger( DeployedTreeCacheProvider.class );

   public static final String OBJECT_NAME_PROP = "hibernate.treecache.objectName";
   public static final String DEFAULT_OBJECT_NAME = "jboss.cache:service=HibernateTreeCache";

   private TreeCache deployedTreeCache;

   public void start(Properties properties) throws CacheException
   {
      // Determine the TreeCache MBean ObjectName.
      String configObjectName = properties.getProperty( OBJECT_NAME_PROP, DEFAULT_OBJECT_NAME );
      ObjectName objectName;
      try
      {
         objectName = new ObjectName( configObjectName );
      }
      catch( Throwable t )
      {
         throw new CacheException( "Malformed TreeCache ObjectName");
      }

      TreeCacheMBean mbean;
      try
      {
         MBeanServer server = MBeanServerLocator.locateJBoss();
         mbean = (TreeCacheMBean) MBeanProxyExt.create(TreeCacheMBean.class, objectName, server);
      }
      catch( Throwable t )
      {
         log.warn( "Unable to locate TreeCache MBean under object name [" + configObjectName + "]", t );
         throw new CacheException( "Unable to locate TreeCache MBean under object name [" + configObjectName + "]" );
      }

      deployedTreeCache = mbean.getInstance();
   }

   public void stop()
   {
      deployedTreeCache = null;
   }

   public boolean isMinimalPutsEnabledByDefault()
   {
      return true;
   }

   /**
    * Called by Hibernate in order to build the given named cache "region".
    *
    * @param name The cache "region" name.
    * @param properties The configuration properties.
    * @return The constructed Cache wrapper around the jndi-deployed TreeCache.
    * @throws CacheException Generally indicates a problem locating the TreeCache.
    */
   public Cache buildCache(String name, Properties properties) throws CacheException
   {
      TransactionManager tm = TransactionManagerLocator.getInstance().locate(); 
      return new org.hibernate.cache.TreeCache( deployedTreeCache, name, tm );
   }

   public long nextTimestamp()
   {
		return System.currentTimeMillis() / 100;
   }
}
