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

package org.jboss.resource.connectionmanager.xa;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;

/**
 * A XAResourceWrapper supporting JCA specific constructs for XAResource management primarily
 * the isSameRMOverrideValue.
 * 
 * @author <a href="weston.price@jboss.org">Weston Price</a>
 * @version $Revision: 1.1 $
 */
public class JcaXAResourceWrapper implements XAResource
{
   private static final Logger log = Logger.getLogger(JcaXAResourceWrapper.class);
   
   private final XAResource resource;
   private Boolean overrideSameRM;
  
   public JcaXAResourceWrapper(final XAResource resource, final Boolean overrideSameRM)
   {
      this.resource = resource;
      this.overrideSameRM = overrideSameRM;
   }
   
   public void commit(Xid xid, boolean onePhase) throws XAException
   {
      resource.commit(xid, onePhase);
   }

   public void end(Xid xid, int flags) throws XAException
   {
      resource.end(xid, flags);
   }

   public void forget(Xid xid) throws XAException
   {
      resource.forget(xid);
   }

   public int getTransactionTimeout() throws XAException
   {
      return resource.getTransactionTimeout();
   }

   public boolean isSameRM(XAResource xaRes) throws XAException
   {
      
      if(overrideSameRM != null)
      {
         return overrideSameRM.booleanValue();         
      }
      
      if(xaRes instanceof JcaXAResourceWrapper)
      {
         final JcaXAResourceWrapper jca = (JcaXAResourceWrapper)xaRes;         
         xaRes = jca.getUnderlyingXAResource();         
      }
      
      final XAResource current = getUnderlyingXAResource();
      
      return current.isSameRM(xaRes);
      
   }

   public int prepare(Xid xid) throws XAException
   {
      return resource.prepare(xid);
   }

   public Xid[] recover(int flag) throws XAException
   {
      return resource.recover(flag);
   }

   public void rollback(Xid xid) throws XAException
   {
      resource.rollback(xid);
   }

   public boolean setTransactionTimeout(int seconds) throws XAException
   {
      return resource.setTransactionTimeout(seconds);
   }

   public void start(Xid xid, int flags) throws XAException
   {
      resource.start(xid, flags);
   }
   
   public XAResource getUnderlyingXAResource()
   {
      return this.resource;
   }
      
}
