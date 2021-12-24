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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.StandardQueryCache;
import org.jboss.cache.Fqn;
import org.jboss.cache.config.Option;
import org.jboss.cache.lock.TimeoutException;

/**
 * Subclass of the standard <code>org.hibernate.cache.TreeCache</code> used as
 * a workaround until issues related to JBCLUSTER-150 are resolved in Hibernate.
 *
 * @author Gavin King
 * @author Brian Stansberry
 */
public class JBCCache implements Cache {
	
	private static final Log log = LogFactory.getLog(JBCCache.class);

	private static final String ITEM = "item";

	private org.jboss.cache.TreeCache cache;
	private final String regionName;
	private final Fqn regionFqn;
	private final TransactionManager transactionManager;
    private boolean localWritesOnly;

	public JBCCache(org.jboss.cache.TreeCache cache, String regionName, 
                    String regionPrefix, TransactionManager transactionManager) 
	throws CacheException {
		this.cache = cache;
		this.regionName = regionName;
        this.regionFqn = Fqn.fromString(SecondLevelCacheUtil.createRegionFqn(regionName, regionPrefix));
		this.transactionManager = transactionManager;
        if (cache.getUseRegionBasedMarshalling())
        {           
           localWritesOnly = StandardQueryCache.class.getName().equals(regionName);
           
           boolean fetchState = cache.getFetchInMemoryState();
           try
           {
              // We don't want a state transfer for the StandardQueryCache,
              // as it can include classes from multiple scoped classloaders
              if (localWritesOnly)
                 cache.setFetchInMemoryState(false);
              
              // We always activate
              activateCacheRegion(regionFqn.toString());
           }
           finally
           {
              // Restore the normal state transfer setting
              if (localWritesOnly)
                 cache.setFetchInMemoryState(fetchState);              
           }
        }
        else
        {
           log.debug("TreeCache is not configured for region based marshalling");
        }
	}

	public Object get(Object key) throws CacheException {
		Transaction tx = suspend();
		try {
			return read(key);
		}
		finally {
			resume( tx );
		}
	}
	
	public Object read(Object key) throws CacheException {
		try {
			return cache.get( new Fqn( regionFqn, key ), ITEM );
		}
		catch (Exception e) {
			throw SecondLevelCacheUtil.convertToHibernateException(e);
		}
	}

	public void update(Object key, Object value) throws CacheException {
		try {
            if (localWritesOnly) {
               Option option = new Option();
               option.setCacheModeLocal(true);
               cache.put( new Fqn( regionFqn, key ), ITEM, value, option );
            }
            else {               
                cache.put( new Fqn( regionFqn, key ), ITEM, value );
            }
		}
		catch (Exception e) {
			throw SecondLevelCacheUtil.convertToHibernateException(e);
		}
	}

	public void put(Object key, Object value) throws CacheException {
		Transaction tx = suspend();
		try {
           if (localWritesOnly) {
              Option option = new Option();
              option.setCacheModeLocal(true);
              // Overloaded method isn't available, so have to use InvocationContext
              cache.getInvocationContext().setOptionOverrides(option);
              try {
                  // do the failfast put outside the scope of the JTA txn
                  cache.putFailFast( new Fqn( regionFqn, key ), ITEM, value, 0 );
              }
              finally {
                 cache.getInvocationContext().setOptionOverrides(null);
              }
           }
           else {               
               //do the failfast put outside the scope of the JTA txn
			   cache.putFailFast( new Fqn( regionFqn, key ), ITEM, value, 0 );
           }
		}
		catch (TimeoutException te) {
			//ignore!
			log.debug("ignoring write lock acquisition failure");
		}
		catch (Exception e) {
			throw SecondLevelCacheUtil.convertToHibernateException(e);
		}
		finally {
			resume( tx );
		}
	}

	private void resume(Transaction tx) {
		try {
			if (tx!=null) transactionManager.resume(tx);
		}
		catch (Exception e) {
			throw new CacheException("Could not resume transaction", e);
		}
	}

	private Transaction suspend() {
		Transaction tx = null;
		try {
			if ( transactionManager!=null ) {
				tx = transactionManager.suspend();
			}
		}
		catch (SystemException se) {
			throw new CacheException("Could not suspend transaction", se);
		}
		return tx;
	}

	public void remove(Object key) throws CacheException {
		try {
           if (localWritesOnly) {
              Option option = new Option();
              option.setCacheModeLocal(true);
              cache.remove( new Fqn( regionFqn, key ), option );
           }
           else {               
               cache.remove( new Fqn( regionFqn, key ) );
           }
		}
		catch (Exception e) {
			throw SecondLevelCacheUtil.convertToHibernateException(e);
		}
	}

	public void clear() throws CacheException {
		try {
			cache.remove( regionFqn );
		}
		catch (Exception e) {
			throw SecondLevelCacheUtil.convertToHibernateException(e);
		}
	}

	public void destroy() throws CacheException {
		try {
			// NOTE : Hibernate's class uses evict() but that isn't recursive!
			//cache.evict( regionFqn );
            Option opt = new Option();
            opt.setCacheModeLocal(true);
            cache.remove(regionFqn, opt);
            
            if (cache.getUseRegionBasedMarshalling() && !SecondLevelCacheUtil.isSharedClassLoaderRegion(regionName))
            {
               inactivateCacheRegion();
            }
		}
        catch (CacheException e)
        {
           throw e;
        }
		catch( Exception e ) {
			throw SecondLevelCacheUtil.convertToHibernateException(e);
		}
	}

	public void lock(Object key) throws CacheException {
		throw new UnsupportedOperationException( "TreeCache is a fully transactional cache: " + regionName );
	}

	public void unlock(Object key) throws CacheException {
		throw new UnsupportedOperationException( "TreeCache is a fully transactional cache: " + regionName );
	}

	public long nextTimestamp() {
		return System.currentTimeMillis() / 100;
	}

	public int getTimeout() {
		return 600; //60 seconds
	}

	public String getRegionName() {
		return regionName;
	}

	public long getSizeInMemory() {
		return -1;
	}

	public long getElementCountInMemory() {
		try {
			Set children = cache.getChildrenNames( regionFqn );
			return children == null ? 0 : children.size();
		}
		catch (Exception e) {
			throw SecondLevelCacheUtil.convertToHibernateException(e);
		}
	}

	public long getElementCountOnDisk() {
		return 0;
	}
	
	public Map toMap() {
		try {
			Map result = new HashMap();
			Set childrenNames = cache.getChildrenNames( regionFqn );
			if (childrenNames != null) {
				Iterator iter = childrenNames.iterator();
				while ( iter.hasNext() ) {
					Object key = iter.next();
					result.put( 
							key, 
							cache.get( new Fqn( regionFqn, key ), ITEM )
						);
				}
			}
			return result;
		}
		catch (Exception e) {
			throw SecondLevelCacheUtil.convertToHibernateException(e);
		}
	}
	
	public String toString() {
		return "JBCCache(" + regionName + ')';
	}
    
    private void activateCacheRegion(String regionName) throws CacheException
    {
       String fqnString = regionFqn.toString();
       // FIXME -- find a way that doesn't involve this API
       if (cache.getMarshaller().isInactive(fqnString))
       {
          try
          {
             // Only register the classloader if it's not a shared region.  
             // If it's shared, no single classloader is valid
             if (!SecondLevelCacheUtil.isSharedClassLoaderRegion(regionName))
             {
                cache.registerClassLoader(fqnString, Thread.currentThread().getContextClassLoader());
             }
             cache.activateRegion(fqnString);
          }
          catch (Exception e)
          {
             throw SecondLevelCacheUtil.convertToHibernateException(e);
          }
       }
       else
       {
          log.debug("activateCacheRegion(): Region " + fqnString + " is already active");
       }
    }
    
    private void inactivateCacheRegion() throws CacheException
    {
       String fqnString = regionFqn.toString();
       // FIXME -- find a way that doesn't involve this API
       if (!cache.getMarshaller().isInactive(fqnString))
       {
          try
          {
             cache.inactivateRegion(fqnString);
             cache.unregisterClassLoader(fqnString);
          }
          catch (Exception e)
          {
             throw SecondLevelCacheUtil.convertToHibernateException(e);
          }
       }     
       else
       {
          log.debug("inactivateCacheRegion(): Region " + fqnString + " is already inactive");
       }
    }	
}
