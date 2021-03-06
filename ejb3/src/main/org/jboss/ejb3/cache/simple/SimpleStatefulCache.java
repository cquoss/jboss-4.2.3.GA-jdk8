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
package org.jboss.ejb3.cache.simple;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ejb.EJBException;
import javax.ejb.NoSuchEJBException;
import org.jboss.annotation.ejb.cache.simple.CacheConfig;
import org.jboss.annotation.ejb.cache.simple.PersistenceManager;
import org.jboss.aop.Advisor;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Pool;
import org.jboss.ejb3.cache.StatefulCache;
import org.jboss.ejb3.stateful.StatefulBeanContext;
import org.jboss.util.id.GUID;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 62768 $
 */
public class SimpleStatefulCache implements StatefulCache
{
   private Logger log = Logger.getLogger(SimpleStatefulCache.class);

   private Pool pool;
   private CacheMap cacheMap;
   private int maxSize = 1000;
   private StatefulSessionPersistenceManager pm;
   private long sessionTimeout = 300; // 5 minutes
   private long removalTimeout = 0; 
   private SessionTimeoutTask timeoutTask;
   private RemovalTimeoutTask removalTask = null;
   private boolean running = true;
   protected int createCount = 0;
   protected int passivatedCount = 0;
   protected int removeCount = 0;

   private class CacheMap extends LinkedHashMap
   {
      private static final long serialVersionUID = 4514182777643616159L;

      public CacheMap()
      {
         super(maxSize, 0.75F, true);
      }

      public boolean removeEldestEntry(Map.Entry entry)
      {
         boolean removeIt = size() > maxSize;
         if (removeIt)
         {
            StatefulBeanContext centry = (StatefulBeanContext) entry.getValue();
            synchronized (centry)
            {
               if (centry.getCanPassivate())
               {
                  passivate(centry);
                  // its ok to evict because bean will be passivated.
               }
               else
               {
                  centry.markedForPassivation = true;
                  
                  if (!centry.isInUse())
                  {
                     // Can't passivate but not in use means a child bean is 
                     // in use.
                     // It's not ok to evict because bean will not be passivated
                     removeIt = false;
                  }
               }               
            }
         }
         return removeIt;
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
               
               synchronized (cacheMap)
               {
                  if (!running) return;
                   
                  Iterator it = cacheMap.entrySet().iterator();
                  while (it.hasNext())
                  {
                     Map.Entry entry = (Map.Entry) it.next();
                     StatefulBeanContext centry = (StatefulBeanContext) entry.getValue();
                     if (now - centry.lastUsed >= removalTimeout * 1000)
                     {
                        synchronized (centry)
                        {                                                                    
                           it.remove();
                        }
                     }
                  }                  
               }
               
               List<StatefulBeanContext> beans = pm.getPassivatedBeans();           
               Iterator<StatefulBeanContext> it = beans.iterator();
               while (it.hasNext())
               {       
                  StatefulBeanContext centry = it.next();
                  if (now - centry.lastUsed >= removalTimeout * 1000)
                  {
                     get(centry.getId(), false);
                     remove(centry.getId());
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

   private class SessionTimeoutTask extends Thread
   {
      public SessionTimeoutTask(String name)
      {
         super(name);
      }

      public void run()
      {
         while (running)
         {
            try
            {
               Thread.sleep(sessionTimeout * 1000);
            }
            catch (InterruptedException e)
            {
               running = false;
               return;
            }
            try
            {
               synchronized (cacheMap)
               {
                  if (!running) return;
                  
                  boolean trace = log.isTraceEnabled();
                  Iterator it = cacheMap.entrySet().iterator();
                  long now = System.currentTimeMillis();
                  while (it.hasNext())
                  {
                     Map.Entry entry = (Map.Entry) it.next();
                     StatefulBeanContext centry = (StatefulBeanContext) entry.getValue();
                     if (now - centry.lastUsed >= sessionTimeout * 1000)
                     {
                        synchronized (centry)
                        {                     
                           if (centry.getCanPassivate())
                           {
                              if (!centry.getCanRemoveFromCache())
                              {
                                 passivate(centry);
                              }
                              else if (trace)
                              {
                                 log.trace("Removing " + entry.getKey() + " from cache");
                              }
                           }
                           else
                           {
                              centry.markedForPassivation = true;                              
                           }
                           // its ok to evict because it will be passivated
                           // or we determined above that we can remove it
                           it.remove();
                        }
                     }
                     else if (trace)
                     {
                        log.trace("Not passivating; id=" + centry.getId() +
                              " only inactive " + Math.max(0, now - centry.lastUsed) + " ms");
                     }
                  }
               }
            }
            catch (Exception ex)
            {
               log.error("problem passivation thread", ex);
            }
         }
      }
   }

   public void initialize(Container container) throws Exception
   {
      Advisor advisor = (Advisor) container;
      this.pool = container.getPool();
      cacheMap = new CacheMap();
      PersistenceManager pmConfig = (PersistenceManager) advisor.resolveAnnotation(PersistenceManager.class);
      this.pm = (StatefulSessionPersistenceManager) pmConfig.value().newInstance();
      pm.initialize(container);
      CacheConfig config = (CacheConfig) advisor.resolveAnnotation(CacheConfig.class);
      maxSize = config.maxSize();
      sessionTimeout = config.idleTimeoutSeconds();
      removalTimeout = config.removalTimeoutSeconds();
      log = Logger.getLogger(getClass().getName() + "." + container.getEjbName());
      log.debug("Initializing SimpleStatefulCache with maxSize: " +maxSize + " timeout: " +sessionTimeout +
              " for " +container.getObjectName().getCanonicalName() );
      timeoutTask = new SessionTimeoutTask("SFSB Passivation Thread - " + container.getObjectName().getCanonicalName());
      
      if (removalTimeout > 0)
         removalTask = new RemovalTimeoutTask("SFSB Removal Thread - " + container.getObjectName().getCanonicalName());
   }

   public SimpleStatefulCache()
   {
   }

   public void start()
   {
      running = true;
      timeoutTask.start();
      
      if (removalTask != null)
         removalTask.start();
   }

   public void stop()
   {
      synchronized (cacheMap)
      {
         running = false;
         timeoutTask.interrupt();
         if (removalTask != null)
            removalTask.interrupt();
         cacheMap.clear();
         try
         {
            pm.destroy();
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
   }

   protected void passivate(StatefulBeanContext ctx)
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(((EJBContainer) ctx.getContainer()).getClassloader());
         pm.passivateSession(ctx);
         ++passivatedCount;
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

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
         synchronized (cacheMap)
         {
            cacheMap.put(ctx.getId(), ctx);
         }
         ctx.setInUse(true);
         ctx.lastUsed = System.currentTimeMillis();
         ++createCount;
      }
      catch (EJBException e)
      {
         e.printStackTrace();
         throw e;
      }
      catch (Exception e)
      {
         e.printStackTrace();
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
         synchronized (cacheMap)
         {
            cacheMap.put(ctx.getId(), ctx);
         }
         ctx.setInUse(true);
         ctx.lastUsed = System.currentTimeMillis();
         ++createCount;
      }
      catch (EJBException e)
      {
         e.printStackTrace();
         throw e;
      }
      catch (Exception e)
      {
         e.printStackTrace();
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
      synchronized (cacheMap)
      {
         entry = (StatefulBeanContext) cacheMap.get(key);
      }
      if (entry == null)
      {
         entry = (StatefulBeanContext) pm.activateSession(key);
         if (entry == null)
         {
            throw new NoSuchEJBException("Could not find stateful bean: " + key);
         }
         --passivatedCount;
          
         // We cache the entry even if we will throw an exception below
         // as we may still need it for its children and XPC references
         if (log.isTraceEnabled())
         {
            log.trace("Caching activated context " + entry.getId() + " of type " + entry.getClass());
         }

         synchronized (cacheMap)
         {
            cacheMap.put(key, entry);
         }
      }
      
      // Now we know entry isn't null
      if (markInUse)
      { 
         if (entry.isRemoved())
         {
            throw new NoSuchEJBException("Could not find stateful bean: " + key +
                                         " (bean was marked as removed");
         }      
      
         entry.setInUse(true);
         entry.lastUsed = System.currentTimeMillis();
      }
      
      return entry;
   }

   public void finished(StatefulBeanContext ctx)
   {
      synchronized (ctx)
      {
         ctx.setInUse(false);
         ctx.lastUsed = System.currentTimeMillis();
         if (ctx.markedForPassivation)
         {
            passivate(ctx);
         }
      }
   }

   public void remove(Object key)
   {
      StatefulBeanContext ctx = null;
      synchronized (cacheMap)
      {
         ctx = (StatefulBeanContext) cacheMap.get(key);
      }
      if (ctx != null) 
      {
         if (!ctx.isRemoved())
            pool.remove(ctx);
         
         ++removeCount;
         
         if (ctx.getCanRemoveFromCache())
         {
            synchronized (cacheMap)
            {
               cacheMap.remove(key);
            }
         }
      }
   }

   public int getCacheSize()
   {
	   return cacheMap.size();
   }
   
   public int getTotalSize()
   {
      return cacheMap.size() + pm.getNumPassivatedBeans();
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
      return maxSize;
   }
   
   public int getCurrentSize()
   {
      return cacheMap.size();
   }
}
