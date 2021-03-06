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
package org.jboss.jms.recovery;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.jms.jndi.JMSProviderAdapter;
import org.jboss.logging.Logger;
import org.jboss.util.naming.Util;

/**
 * XAResourceWrapper.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57209 $
 */
public class XAResourceWrapper implements XAResource, ExceptionListener
{
   /** The log */
   private static final Logger log = Logger.getLogger(XAResourceWrapper.class);

   /** The jms provider name */
   private String providerName;
   
   /** The state lock */
   private static final Object lock = new Object();
   
   /** The connection */
   private XAConnection connection;
   
   /** The delegate XAResource */
   private XAResource delegate;

   /**
    * Get the providerName.
    * 
    * @return the providerName.
    */
   public String getProviderName()
   {
      return providerName;
   }

   /**
    * Set the providerName.
    * 
    * @param providerName the providerName.
    */
   public void setProviderName(String providerName)
   {
      this.providerName = providerName;
   }
   
   public Xid[] recover(int flag) throws XAException
   {
      log.debug("Recover " + providerName);
      XAResource xaResource = getDelegate();
      try
      {
         return xaResource.recover(flag);
      }
      catch (XAException e)
      {
         throw check(e);
      }
   }

   public void commit(Xid xid, boolean onePhase) throws XAException
   {
      log.debug("Commit " + providerName + " xid " + " onePhase=" + onePhase);
      XAResource xaResource = getDelegate();
      try
      {
         xaResource.commit(xid, onePhase);
      }
      catch (XAException e)
      {
         throw check(e);
      }
   }

   public void rollback(Xid xid) throws XAException
   {
      log.debug("Rollback " + providerName + " xid ");
      XAResource xaResource = getDelegate();
      try
      {
         xaResource.rollback(xid);
      }
      catch (XAException e)
      {
         throw check(e);
      }
   }

   public void forget(Xid xid) throws XAException
   {
      log.debug("Forget " + providerName + " xid ");
      XAResource xaResource = getDelegate();
      try
      {
         xaResource.forget(xid);
      }
      catch (XAException e)
      {
         throw check(e);
      }
   }

   public boolean isSameRM(XAResource xaRes) throws XAException
   {
      if (xaRes instanceof XAResourceWrapper)
         xaRes = ((XAResourceWrapper) xaRes).getDelegate();

      XAResource xaResource = getDelegate();
      try
      {
         return xaResource.isSameRM(xaRes);
      }
      catch (XAException e)
      {
         throw check(e);
      }
   }

   public int prepare(Xid xid) throws XAException
   {
      XAResource xaResource = getDelegate();
      try
      {
         return xaResource.prepare(xid);
      }
      catch (XAException e)
      {
         throw check(e);
      }
   }

   public void start(Xid xid, int flags) throws XAException
   {
      XAResource xaResource = getDelegate();
      try
      {
         xaResource.start(xid, flags);
      }
      catch (XAException e)
      {
         throw check(e);
      }
   }

   public void end(Xid xid, int flags) throws XAException
   {
      XAResource xaResource = getDelegate();
      try
      {
         xaResource.end(xid, flags);
      }
      catch (XAException e)
      {
         throw check(e);
      }
   }

   public int getTransactionTimeout() throws XAException
   {
      XAResource xaResource = getDelegate();
      try
      {
         return xaResource.getTransactionTimeout();
      }
      catch (XAException e)
      {
         throw check(e);
      }
   }

   public boolean setTransactionTimeout(int seconds) throws XAException
   {
      XAResource xaResource = getDelegate();
      try
      {
         return xaResource.setTransactionTimeout(seconds);
      }
      catch (XAException e)
      {
         throw check(e);
      }
   }

   public void onException(JMSException exception)
   {
      log.warn("Notified of connection failure in recovery delegate for provider " + providerName, exception);
      close();
   }
   
   /**
    * Get the delegate XAResource
    * 
    * @return the delegate
    * @throws XAException for any problem
    */
   public XAResource getDelegate() throws XAException
   {
      XAResource result = null;
      Exception error = null;
      try
      {
         result = connect();
      }
      catch (Exception e)
      {
         error = e;
      }

      if (result == null)
      {
         XAException xae = new XAException("Error trying to connect to provider " + providerName);
         xae.errorCode = XAException.XAER_RMERR;
         if (error != null)
            xae.initCause(error);
         log.debug("Cannot get delegate XAResource", xae);
         throw xae;
      }
      
      return result;
   }
   
   /**
    * Connect to the server if not already done so
    * 
    * @return the delegate XAResource
    * @throws Exception for any problem
    */
   protected XAResource connect() throws Exception
   {
      // Do we already have a valid delegate?
      synchronized (lock)
      {
         if (delegate != null)
            return delegate;
      }
      
      // Create the connection
      XAConnection xaConnection = getConnectionFactory().createXAConnection();
      synchronized (lock)
      {
         connection = xaConnection;
      }

      // Retrieve the delegate XAResource
      try
      {
         XASession session = connection.createXASession();
         XAResource result = session.getXAResource();
         synchronized (lock)
         {
            delegate = result;
         }
         return delegate;
      }
      catch (Exception e)
      {
         close();
         throw e;
      }
   }

   /**
    * Get the XAConnectionFactory
    * 
    * @return the connection
    * @throws Exception for any problem
    */
   protected XAConnectionFactory getConnectionFactory() throws Exception
   {
      // Get the JMS Provider Adapter
      if (providerName == null)
         throw new IllegalArgumentException("Null provider name");
      String providerAdapterJNDI = providerName;
      if (providerAdapterJNDI.startsWith("java:") == false)
         providerAdapterJNDI = "java:" + providerAdapterJNDI;
      Context ctx = new InitialContext();
      JMSProviderAdapter adapter = (JMSProviderAdapter) Util.lookup(providerAdapterJNDI, JMSProviderAdapter.class);

      // Determine the XAConnectionFactory name
      String connectionFactoryRef = adapter.getFactoryRef();
      if (connectionFactoryRef == null)
         throw new IllegalStateException("Provider '" + providerName + "' has no FactoryRef");
      
      // Lookup the connection factory
      ctx = adapter.getInitialContext();
      try
      {
         return (XAConnectionFactory) Util.lookup(ctx, connectionFactoryRef, XAConnectionFactory.class);
      }
      finally
      {
         ctx.close();
      }
   }
   
   /**
    * Close the connection
    */
   public void close()
   {
      try
      {
         XAConnection oldConnection = null;
         synchronized (lock)
         {
            oldConnection = connection;
            connection = null;
            delegate = null;
         }
         if (oldConnection != null)
            oldConnection.close();
      }
      catch (Exception ignored)
      {
         log.trace("Ignored error during close", ignored);
      }
   }

   /**
    * Check whether an XAException is fatal. If it is an RM problem
    * we close the connection so the next call will reconnect.
    * 
    * @param e the xa exception
    * @return never
    * @throws XAException always
    */
   protected XAException check(XAException e) throws XAException
   {
      if (e.errorCode == XAException.XAER_RMERR || e.errorCode == XAException.XAER_RMFAIL)
      {
         log.debug("Fatal error in provider " + providerName, e);
         close();
      }
      throw e;
   }

   protected void finalize() throws Throwable
   {
      close();
   }
}
