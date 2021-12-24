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
package org.jboss.ejb3.cache.tree;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;
import java.util.Map;

import java.util.Set;

import javax.ejb.EJBException;
import javax.ejb.NoSuchEJBException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.cache.xml.XmlHelper;
import org.jboss.cache.eviction.RegionManager;
import org.jboss.cache.eviction.Region;
import org.jboss.cache.marshall.RegionNotFoundException;
import org.jboss.cache.CacheException;
import org.jboss.cache.AbstractTreeCacheListener;
import org.jboss.cache.DataNode;
import org.jboss.cache.RegionNotEmptyException;
import org.jboss.cache.TreeCache;
import org.jboss.cache.TreeCacheMBean;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Pool;
import org.jboss.ejb3.cache.ClusteredStatefulCache;
import org.jboss.ejb3.stateful.NestedStatefulBeanContext;
import org.jboss.ejb3.stateful.ProxiedStatefulBeanContext;
import org.jboss.ejb3.stateful.StatefulBeanContext;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.util.id.GUID;
import org.jboss.logging.Logger;
import org.jboss.annotation.ejb.cache.tree.CacheConfig;
import org.w3c.dom.Element;

import org.jboss.cache.Fqn;
import org.jboss.cache.config.Option;

/**
 * Clustered SFSB cache that uses JBoss Cache to cache and replicate
 * bean contexts.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author Brian Stansberry
 * 
 * @version $Revision: 73125 $
 */
public class StatefulTreeCache implements ClusteredStatefulCache
{
   private static final int FQN_SIZE = 3; // depth of fqn that we store the session in.
   private static final int DEFAULT_BUCKET_COUNT = 100;

   private static final String[] DEFAULT_HASH_BUCKETS = new String[DEFAULT_BUCKET_COUNT];
   
   private static Option LOCAL_ONLY_OPTION = new Option();
   private static Option GRAVITATE_OPTION = new Option();
   static
   {
      LOCAL_ONLY_OPTION.setCacheModeLocal(true);
      GRAVITATE_OPTION.setForceDataGravitation(true);
      
      for (int i = 0; i < DEFAULT_HASH_BUCKETS.length; i++)
      {
         DEFAULT_HASH_BUCKETS[i] = String.valueOf(i);
      }
   }
   
   private ThreadLocal<Boolean> localActivity = new ThreadLocal<Boolean>();
   private Logger log = Logger.getLogger(StatefulTreeCache.class);
   private Pool pool;
   private WeakReference<ClassLoader> classloader;
   private TreeCache cache;
   private Fqn cacheNode;
   private ClusteredStatefulCacheListener listener;
   private RegionManager evictRegionManager;
   public static long MarkInUseWaitTime = 15000;
   protected String[] hashBuckets = DEFAULT_HASH_BUCKETS;
   protected int createCount = 0;
   protected int passivatedCount = 0;
   protected int removeCount = 0;
   protected long removalTimeout = 0; 
   protected RemovalTimeoutTask removalTask = null;
   protected boolean running = true;
   protected Map<Object, Long> beans = new ConcurrentHashMap<Object, Long>();
   protected EJBContainer ejbContainer;

   public StatefulBeanContext create()
   {
      StatefulBeanContext ctx = null;
      try
      {
         ctx = (StatefulBeanContext) pool.get();
         if (log.isTraceEnabled())
         {
            log.trace("Caching context " + ctx.getId() + " of type " + ctx.getClass());
         }
         putInCache(ctx);
         ctx.setInUse(true);
         ctx.lastUsed = System.currentTimeMillis();
         ++createCount;
         beans.put(ctx.getId(), ctx.lastUsed);
      }
      catch (EJBException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
      return ctx;
   }

   public StatefulBeanContext create(Class[] initTypes, Object[] initValues)
   {
      StatefulBeanContext ctx = null;
      try
      {
         ctx = (StatefulBeanContext) pool.get(initTypes, initValues);
         if (log.isTraceEnabled())
         {
            log.trace("Caching context " + ctx.getId() + " of type " + ctx.getClass());
         }
         putInCache(ctx);
         ctx.setInUse(true);
         ctx.lastUsed = System.currentTimeMillis();
         ++createCount;
         beans.put(ctx.getId(), ctx.lastUsed);
      }
      catch (EJBException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
      return ctx;
   }

   public StatefulBeanContext get(Object key) throws EJBException
   {
      return get(key, true);
   }
   
   public StatefulBeanContext get(Object key, boolean markInUse) throws EJBException
   {
      StatefulBeanContext entry = null;
      Fqn id = getFqn(key);
      Boolean active = localActivity.get();
      try
      {
         localActivity.set(Boolean.TRUE);
         Option opt = new Option();
         opt.setForceDataGravitation(true);
         entry = (StatefulBeanContext) cache.get(id, "bean", opt);
      }
      catch (CacheException e)
      {
         RuntimeException re = convertToRuntimeException(e);
         throw re;
      }
      finally
      {
         localActivity.set(active);
      }
      
      if (entry == null)
      {
         throw new NoSuchEJBException("Could not find stateful bean: " + key);
      }
      else if (markInUse && entry.isRemoved())
      {
         throw new NoSuchEJBException("Could not find stateful bean: " + key + 
                                      " (bean was marked as removed)");
      }
      
      entry.postReplicate();
      
      if (markInUse)
      {
         entry.setInUse(true);
         
         // Mark the Fqn telling the eviction thread not to passivate it yet.
         // Note the Fqn we use is relative to the region!
         evictRegionManager.markNodeCurrentlyInUse(id, MarkInUseWaitTime);
         entry.lastUsed = System.currentTimeMillis();
         beans.put(key, entry.lastUsed);
      }
      
      if(log.isTraceEnabled())
      {
         log.trace("get: retrieved bean with cache id " +id.toString());
      }
      
      return entry;
   }

   public void remove(Object key)
   {
      Fqn id = getFqn(key);
      try
      {
         if(log.isTraceEnabled())
         {
            log.trace("remove: cache id " +id.toString());
         }
         
         Option opt = new Option();
         opt.setForceDataGravitation(true);
         StatefulBeanContext ctx = (StatefulBeanContext) cache.get(id, "bean", opt); 
         
         if (ctx != null)
         {
            if (!ctx.isRemoved())
               pool.remove(ctx);
            
            if (ctx.getCanRemoveFromCache())
            {
               // Do a cluster-wide removal of the ctx
               cache.remove(id);
            }
            else 
            {
               // We can't remove the ctx as it contains live nested beans
               // But, we must replicate it so other nodes know the parent is removed!
               putInCache(ctx);
            }
            
            ++removeCount;
            beans.remove(key);
         }
      }
      catch (CacheException e)
      {
         RuntimeException re = convertToRuntimeException(e);
         throw re;
      }
   }

   public void finished(StatefulBeanContext ctx)
   {
      synchronized (ctx)
      {
         ctx.setInUse(false);
         ctx.lastUsed = System.currentTimeMillis();
         beans.put(ctx.getId(), ctx.lastUsed);
         // OK, it is free to passivate now.
         evictRegionManager.unmarkNodeCurrentlyInUse(getFqn(ctx.getId()));
      }
   }

   public void replicate(StatefulBeanContext ctx)
   {
      // StatefulReplicationInterceptor should only pass us the ultimate
      // parent context for a tree of nested beans, which should always be
      // a standard StatefulBeanContext
      if (ctx instanceof NestedStatefulBeanContext)
      {
         throw new IllegalArgumentException("Received unexpected replicate call for nested context " + ctx.getId());
      }
      
      try
      {
         putInCache(ctx);
      }
      catch (CacheException e)
      {
         RuntimeException re = convertToRuntimeException(e);
         throw re;
      }
   }

   public void initialize(Container container) throws Exception
   {
      this.ejbContainer = (EJBContainer) container;
      
      log = Logger.getLogger(getClass().getName() + "." + this.ejbContainer.getEjbName());
      
      this.pool = this.ejbContainer.getPool();
      ClassLoader cl = this.ejbContainer.getClassloader();
      this.classloader = new WeakReference<ClassLoader>(cl);
      
      CacheConfig config = (CacheConfig) this.ejbContainer.resolveAnnotation(CacheConfig.class);
      MBeanServer server = MBeanServerLocator.locateJBoss();
      ObjectName cacheON = new ObjectName(config.name());
      TreeCacheMBean mbean = (TreeCacheMBean) MBeanProxyExt.create(TreeCacheMBean.class, cacheON, server);
      cache = (TreeCache) mbean.getInstance();
      
      cacheNode = new Fqn(new Object[] { this.ejbContainer.getDeploymentQualifiedName() });

      // Try to create an eviction region per ejb
      evictRegionManager = cache.getEvictionRegionManager();
      Element element = getElementConfig(cacheNode.toString(), config.idleTimeoutSeconds(),
              config.maxSize());
      Region region = evictRegionManager.createRegion(cacheNode, element);
      
      // JBCACHE-1136.  There's no reason to have state in an inactive region
      cleanBeanRegion();
      
      cache.registerClassLoader(cacheNode.toString(), cl);
      try
      {
         cache.activateRegion(cacheNode.toString());
      }
      catch (RegionNotEmptyException e)
      {
         // this can happen with nested bean contexts if gravitation
         // pulls a parent bean over after the parent region is stopped
         // Clean up and try again
         cleanBeanRegion();
         cache.activateRegion(cacheNode.toString());
      }
      
      log.debug("initialize(): create eviction region: " +region + " for ejb: " +this.ejbContainer.getEjbName());
   
      removalTimeout = config.removalTimeoutSeconds();
      if (removalTimeout > 0)
         removalTask = new RemovalTimeoutTask("SFSB Removal Thread - " + this.ejbContainer.getObjectName().getCanonicalName());
   }

   protected Element getElementConfig(String regionName, long timeToLiveSeconds, int maxNodes) throws Exception {
      String xml = "<region name=\"" +regionName +"\" policyClass=\"org.jboss.ejb3.cache.tree.AbortableLRUPolicy\">\n" +
               "<attribute name=\"maxNodes\">" +maxNodes +"</attribute>\n" +
               "<attribute name=\"timeToLiveSeconds\">"+ timeToLiveSeconds +"</attribute>\n" +
               "</region>";
      return XmlHelper.stringToElement(xml);
   }

   public void start()
   {
      // register to listen for cache events
      
      // TODO this approach may not be scalable when there are many beans 
      // since then we will need to go thru N listeners to figure out which 
      // one this event belongs to. Consider having a singleton listener
      listener = new ClusteredStatefulCacheListener();
      cache.addTreeCacheListener(listener);
      
      if (removalTask != null)
         removalTask.start();
      
      running = true;
   }

   public void stop()
   {
      running = false;
      
      // Remove the listener
      cache.removeTreeCacheListener(listener);

      // Remove locally. We do this to clean up the persistent store,
      // which is not affected by the inactivateRegion call below.
      cleanBeanRegion();    
      
      try
      {
         cache.inactivateRegion(cacheNode.toString());
      }
      catch (Exception e)
      {
         log.error("Caught exception inactivating region " + cacheNode, e);
      }
      try
      {
         cache.unregisterClassLoader(cacheNode.toString());
      }
      catch (RegionNotFoundException e)
      {
         log.error("Caught exception unregistering classloader from  region " + cacheNode, e);
      }

      // Remove the eviction region
      RegionManager rm = cache.getEvictionRegionManager();
      rm.removeRegion(cacheNode);

      log.debug("stop(): StatefulTreeCache stopped successfully for " +cacheNode);
   }
   
   public int getCacheSize()
   {
      int count = 0;
	   try 
	   {
          Set children = null;
          for (int i = 0; i < hashBuckets.length; i++)
          {
             children = cache.getChildrenNames(new Fqn(cacheNode, hashBuckets[i]));
             count += (children == null ? 0 : children.size());
          }
		  count = count - passivatedCount;
	   } 
       catch (CacheException e)
	   {
		   log.error("Caught exception calculating cache size", e);
           count = -1;
	   }
	   return count;
   }
   
   public int getTotalSize()
   {
      return beans.size();
   }
   
   public int getCreateCount()
   {
	   return createCount;
   }
   
   public int getPassivatedCount()
   {
	   return passivatedCount;
   }
   
   public int getRemoveCount()
   {
      return removeCount;
   }
   
   public int getAvailableCount()
   {
      return -1;
   }
   
   public int getMaxSize()
   {
      return -1;
   }
   
   public int getCurrentSize()
   {
      return getCacheSize();
   }
   
   private void putInCache(StatefulBeanContext ctx) throws CacheException
   {
      Boolean active = localActivity.get();
      try
      {
         localActivity.set(Boolean.TRUE);
         ctx.preReplicate();
         cache.put(getFqn(ctx.getId()), "bean", ctx);
         ctx.markedForReplication = false;
      }
      finally
      {
         localActivity.set(active);
      }
      
   }
   
   private Fqn getFqn(Object id)
   {
      String beanId = id.toString();
      int index;
      if (id instanceof GUID)
      {
         index = (id.hashCode()& 0x7FFFFFFF) % hashBuckets.length;
      }
      else
      {
         index = (beanId.hashCode()& 0x7FFFFFFF) % hashBuckets.length;
      }
      
      return new Fqn(cacheNode, hashBuckets[index], beanId);
   }
   
   private void cleanBeanRegion()
   {
      try {
         // Remove locally.
         Option opt = new Option();
         opt.setCacheModeLocal(true);
         cache.remove(cacheNode, opt);
      } 
      catch (CacheException e) 
      {
         log.error("Stop(): can't remove bean from the underlying distributed cache");
      }       
   }
   
   /** 
    * Creates a RuntimeException, but doesn't pass CacheException as the cause
    * as it is a type that likely doesn't exist on a client.
    * Instead creates a RuntimeException with the original exception's
    * stack trace.
    */   
   private RuntimeException convertToRuntimeException(CacheException e)
   {
      RuntimeException re = new RuntimeException(e.getClass().getName() + " " + e.getMessage());
      re.setStackTrace(e.getStackTrace());
      return re;
   }

   /**
    * A TreeCacheListener that allows us to get notifications of passivations and
    * activations and thus notify the cached StatefulBeanContext.
    */
   public class ClusteredStatefulCacheListener extends AbstractTreeCacheListener
   {
      @Override
      public void nodeActivate(Fqn fqn, boolean pre) 
      {
         // Ignore everything but locally originating "post" events 
         // for nodes in our region
         if(pre) return;
         if(fqn.size() != FQN_SIZE) return;
         if(!fqn.isChildOrEquals(cacheNode)) return;

         // Don't activate a bean just so we can replace the object 
         // with a replicated one
         if (Boolean.TRUE != localActivity.get()) 
         {
            // But we do want to record that the bean's now in memory
            --passivatedCount;
            return; 
         }
         
         StatefulBeanContext bean = null;
         try {
            // TODO Can this cause deadlock in the cache level? Should be ok but need review.
            bean = (StatefulBeanContext) cache.get(fqn, "bean");    
         } 
         catch (CacheException e) 
         {
            log.error("nodeActivate(): can't retrieve bean instance from: " +fqn + " with exception: " +e);
            return;
         }
         
         if(bean == null)
         {
            throw new IllegalStateException("nodeActivate(): null bean instance.");
         }
         
         --passivatedCount;

         if(log.isTraceEnabled())
         {
            log.trace("nodeActivate(): sending postActivate event to bean at fqn: " +fqn);
         }
         
         ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
         try
         {  
            ClassLoader cl = classloader.get();
            if (cl != null)
            {
               Thread.currentThread().setContextClassLoader(cl);
            }
            
            bean.activateAfterReplication();
         }
         finally
         {
            Thread.currentThread().setContextClassLoader(oldCl);
         }
      }

      @Override
      public void nodePassivate(Fqn fqn, boolean pre) 
      {
         // Ignore everything but "pre" events for nodes in our region
         if(!pre) return;
         if(fqn.size() != FQN_SIZE) return;
         if(!fqn.isChildOrEquals(cacheNode)) return;

         StatefulBeanContext bean = null;
         ClassLoader oldCl = Thread.currentThread().getContextClassLoader();  
         Boolean active = localActivity.get();       
         try 
         {
            localActivity.set(Boolean.TRUE);
            // EJBTHREE-746 Use peek to bypass interceptors and thus avoid generating another 
            // eviction event (which will cause another attempt to passivate)
            // Caller thread (eviction) already has a lock on the node
            // With JBC 2.0 there is a BypassInterceptors Option we can use
            DataNode node = cache.peek(fqn);
            if (node != null)
            {
               bean = (StatefulBeanContext) node.get("bean");
               if (bean != null)
               {
                  ClassLoader cl = classloader.get();
                  if (cl != null)
                  {
                     Thread.currentThread().setContextClassLoader(cl);
                  }
               
                  if (!bean.getCanPassivate())
                  {
                     // Abort the eviction
                     throw new ContextInUseException("Cannot passivate bean " + fqn + 
                           " -- it or one if its children is currently in use");
                  }
                  
                  if(log.isTraceEnabled())
                  {
                     log.trace("nodePassivate(): send prePassivate event to bean at fqn: " +fqn);
                  }
                  
                  bean.passivateAfterReplication();
                  ++passivatedCount;
               }
            }
         }
         catch (NoSuchEJBException e)
         {
            // TODO is this still necessary? Don't think we
            // should have orphaned proxies any more
            if (bean instanceof ProxiedStatefulBeanContext)
            {
               // This is probably an orphaned proxy; double check and remove it
               try
               {
                  bean.getContainedIn();
                  // If that didn't fail, it's not an orphan
                  throw e;
               }
               catch (NoSuchEJBException n)
               {
                  log.debug("nodePassivate(): removing orphaned proxy at " + fqn);
                  try
                  {
                     cache.remove(fqn);
                  }
                  catch (CacheException c)
                  {
                     log.error("nodePassivate(): could not remove orphaned proxy at " + fqn, c);
                     // Just fall through and let the eviction try
                  }
               }
            }
            else
            {
               throw e;
            }
         }
         finally
         {
            Thread.currentThread().setContextClassLoader(oldCl);
            localActivity.set(active);
         }
      }
   }
   
   private class RemovalTimeoutTask extends Thread
   {
      public RemovalTimeoutTask(String name)
      {
         super(name);
      }

      public void run()
      {
         while (running)
         {
            try
            {
               Thread.sleep(removalTimeout * 1000);
            }
            catch (InterruptedException e)
            {
               running = false;
               return;
            }
            try
            {
               long now = System.currentTimeMillis();
               
               Iterator<Map.Entry<Object, Long>> it = beans.entrySet().iterator();
               while (it.hasNext())
               {
                  
                  Map.Entry<Object, Long> entry = it.next();
                  long lastUsed = entry.getValue();
                  if (now - lastUsed >= removalTimeout * 1000)
                  {
                     remove(entry.getKey());
                  }
               }
            }
            catch (Exception ex)
            {
               log.error("problem removing SFSB thread", ex);
            }
         }
      }
   }
}
