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
package org.jboss.ha.jndi;


import java.io.Serializable;
import java.util.Collection;

import javax.naming.Name;
import javax.naming.NamingException;

import org.jnp.interfaces.Naming;

import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.interfaces.HAPartition.HAPartitionStateTransfer;
import org.jboss.logging.Logger;

/** 
 *   This class extends the JNP JNDI implementation.
 *   binds and unbinds will be distributed to all members of the cluster
 *   that are running HAJNDI.
 *   lookups will look for Names in HAJNDI then delegate to the local InitialContext
 *
 *   @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *   @author Scott.Stark@jboss.org
 *   @version $Revision: 57188 $
 */
public class HAJNDI
   implements HAPartitionStateTransfer, Serializable, org.jnp.interfaces.Naming
{
   /** @since 1.12.2.4, jboss-3.2.2 */
   static final long serialVersionUID = -6277328603304171620L;

   // Attributes --------------------------------------------------------
   private static Logger log = Logger.getLogger(HAJNDI.class);

   protected HAPartition partition;
   protected TreeHead delegate;
   protected Naming haStub;

   // Constructor --------------------------------------------------------
   
   public HAJNDI(HAPartition partition)
      throws NamingException
   {
      if (partition == null)
         throw new IllegalArgumentException("Null partition");
      this.partition = partition;
      delegate = new TreeHead();
      delegate.setPartition(this.partition);
      delegate.setHARMIHead(this);
   }
   
   // Public --------------------------------------------------------

   public void init() throws Exception
   {
      log.debug("subscribeToStateTransferEvents");
      partition.subscribeToStateTransferEvents("HAJNDI", this);
      delegate.init();
   }

   public void stop() throws Exception
   {
      delegate.stop();
   }
   
   public void destroy() throws Exception
   {
      delegate.destroy();
      partition.unsubscribeFromStateTransferEvents("HAJNDI", this);      
   }

   public void setHAStub (Naming stub)
   {
      this.haStub = stub;
   }

   public Naming getHAStub ()
   {
      return this.haStub;
   }

   // HAPartition.HAPartitionStateTransfer Implementation --------------------------------------------------------
   
   public Serializable getCurrentState()
   {
      if( log.isTraceEnabled() )
         log.trace("getCurrentState called");
      return delegate;
   }

   public void setCurrentState(Serializable newState)
   {
      if( log.isTraceEnabled() )
         log.trace("setCurrentState called");

      try
      {
         delegate.stop();
         delegate = (TreeHead)newState;
         delegate.setPartition(this.partition);
         delegate.setHARMIHead (this);
         delegate.init();
      }
      catch (Exception failed)
      {
         log.warn("Problem restoring state to HA-JNDI", failed);
      }
   }

   // Naming implementation -----------------------------------------
   

   public synchronized void bind(Name name, Object obj, String className)
      throws NamingException
   {
      delegate.bind (name, obj, className);
   }

   public synchronized void rebind(Name name, Object obj, String className)
      throws NamingException
   {
      delegate.rebind (name, obj, className);
   }

   public synchronized void unbind(Name name)
      throws NamingException
   {
      delegate.unbind (name);
   }

   public Object lookup(Name name)
      throws NamingException
   {
      return delegate.lookup (name);
   }

   public Collection list(Name name)
      throws NamingException
   {
      return delegate.list(name) ;
   }
    
   public Collection listBindings(Name name)
      throws NamingException
   {
      return delegate.listBindings(name);
   }
   
   public javax.naming.Context createSubcontext(Name name)
      throws NamingException
   {
      return delegate.createSubcontext(name);
   }
}
