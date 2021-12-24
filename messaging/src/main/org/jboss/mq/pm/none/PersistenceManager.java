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
package org.jboss.mq.pm.none;
import javax.jms.JMSException;
import javax.management.ObjectName;

import org.jboss.mq.SpyDestination;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.pm.CacheStore;
import org.jboss.mq.pm.Tx;
import org.jboss.mq.pm.TxManager;
import org.jboss.mq.server.JMSDestination;
import org.jboss.mq.server.MessageCache;
import org.jboss.mq.server.MessageReference;
import org.jboss.system.ServiceMBeanSupport;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedLong;

/**
 * A persistence manager and cache store that does not persistence.
 * 
 * It can do persistence by delegating to a real persistence manager,
 * depending upon destination configuration.
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean, org.jboss.mq.pm.PersistenceManagerMBean, org.jboss.mq.pm.CacheStoreMBean"
 *
 * @author Adrian Brock (adrian@jboss.org)
 *
 *  @version $Revision: 57198 $
 */
public class PersistenceManager
   extends ServiceMBeanSupport
   implements PersistenceManagerMBean, org.jboss.mq.pm.PersistenceManager, CacheStore
{
   // Constants --------------------------------------------------------------------

   // Attributes -------------------------------------------------------------------

   /** The next transaction id */
   SynchronizedLong nextTransactionid = new SynchronizedLong(0l);
   
   /** The oject name of the delegate persistence manger */
   ObjectName delegateName;
   
   /** The delegate persistence manager */
   org.jboss.mq.pm.PersistenceManager delegate;
   
   /** The jbossmq transaction manager */
   TxManager txManager;
   
   /** The cache */
   ConcurrentHashMap cache = new ConcurrentHashMap();
   
   // Constructors -----------------------------------------------------------------
   
   // Public -----------------------------------------------------------------------

   /**
    * Retrieve the delegate persistence manager
    * 
    * @jmx:managed-attribute
    * @return the object name of the delegate persistence manager
    */
   public ObjectName getDelegatePM()
   {
      return delegateName;
   }

   /**
    * Set the delegate persistence manager
    * 
    * @jmx:managed-attribute
    * @param delegatePM the delegate persistence manager
    */
   public void setDelegatePM(ObjectName delegateName)
   {
      this.delegateName = delegateName;
   }

   // PersistenceManager implementation --------------------------------------------

   public void add(MessageReference message, Tx txId) throws JMSException
   {
      if (delegate != null && message.inMemory() == false)
         delegate.add(message, txId);
   }
   
   public void closeQueue(JMSDestination jmsDest, SpyDestination dest) throws JMSException
   {
      if (delegate != null)
         delegate.closeQueue(jmsDest, dest);
   }
   
   public void commitPersistentTx(Tx txId) throws JMSException
   {
      if (delegate != null)
         delegate.commitPersistentTx(txId);
   }
   
   public Tx createPersistentTx() throws JMSException
   {
      if (delegate != null)
         return delegate.createPersistentTx();

      Tx tx = new Tx(nextTransactionid.increment());
      return tx;
   }
   
   public MessageCache getMessageCacheInstance()
   {
      if (delegate != null)
         return delegate.getMessageCacheInstance();
         
      throw new UnsupportedOperationException("This is now set on the destination manager");
   }
   
   public TxManager getTxManager()
   {
      return txManager;
   }
   
   public void remove(MessageReference message, Tx txId) throws JMSException
   {
      if (delegate != null && message.inMemory() == false)
         delegate.remove(message, txId);
   }
   
   public void restoreQueue(JMSDestination jmsDest, SpyDestination dest) throws JMSException
   {
      if (delegate != null)
         delegate.restoreQueue(jmsDest, dest);
   }
   
   public void rollbackPersistentTx(Tx txId) throws JMSException
   {
      if (delegate != null)
         delegate.rollbackPersistentTx(txId);
   }
   
   public void update(MessageReference message, Tx txId) throws JMSException
   {
      if (delegate != null && message.inMemory() == false)
         delegate.update(message, txId);
   }
   
   // PersistenceManagerMBean implementation ---------------------------------------

   public Object getInstance()
   {
      return this;
   }

   /**
    * Unsupported operation
    */
   public ObjectName getMessageCache()
   {
      if (delegateName != null)
      {
         try
         {
            return (ObjectName) server.getAttribute(delegateName, "MessageCache");
         }
         catch (Exception e)
         {
            log.trace("Unable to retrieve message cache from delegate", e);
         }
      }
      throw new UnsupportedOperationException("This is now set on the destination manager");
   }

   /**
    * Unsupported operation
    */
   public void setMessageCache(ObjectName messageCache)
   {
      throw new UnsupportedOperationException("This is now set on the destination manager");
   }

   // CacheStore implementation ----------------------------------------------------

   public SpyMessage loadFromStorage(MessageReference mh) throws JMSException
   {
      if (delegate == null || mh.inMemory())
         return (SpyMessage) cache.get(mh);
      else
         return ((CacheStore) delegate).loadFromStorage(mh);
   }
   
   public void removeFromStorage(MessageReference mh) throws JMSException
   {
      if (delegate == null || mh.inMemory())
      {
         cache.remove(mh);
         mh.setStored(MessageReference.NOT_STORED);
      }
      else
         ((CacheStore) delegate).removeFromStorage(mh);

   }
   
   public void saveToStorage(MessageReference mh, SpyMessage message) throws JMSException
   {
      if (delegate == null || mh.inMemory())
      {
         cache.put(mh, message);
         mh.setStored(MessageReference.STORED);
      }
      else
         ((CacheStore) delegate).saveToStorage(mh, message);
   }
   
   // ServerMBeanSupport overrides -------------------------------------------------

   protected void startService() throws Exception
   {
      //Is there a delegate?
      if (delegateName != null)
      {
         delegate = (org.jboss.mq.pm.PersistenceManager) getServer().getAttribute(delegateName, "Instance");
         if ((delegate instanceof CacheStore) == false)
            throw new UnsupportedOperationException("The delegate persistence manager must also be a CacheStore");
         txManager = delegate.getTxManager();
      }
      else
      {
         txManager = new TxManager(this);
      }
   }
   
   // Protected --------------------------------------------------------------------

   // Inner Classes ----------------------------------------------------------------
}
