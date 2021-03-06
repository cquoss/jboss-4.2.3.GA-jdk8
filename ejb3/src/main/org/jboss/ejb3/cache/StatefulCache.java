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
package org.jboss.ejb3.cache;

import javax.ejb.EJBException;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.stateful.StatefulBeanContext;

/**
 * Stateful StatelessBean Bean cache
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 61329 $
 */
public interface StatefulCache
{
   /**
    * returns a key not the actual object
    */
   public StatefulBeanContext create();

   public StatefulBeanContext create(Class[] initTypes, Object[] initValues);

   /**
    * Gets the context with the given id.
    * <p/>
    * Same as <code>getContext(key, true)</code>
    * 
    * @param key the id
    * @return the context
    * 
    * @throws NoSuchEJBException if no context with the given id exists or
    *                            if the context exists but has been marked
    *                            as removed
    * @throws EJBException                       
    */
   public StatefulBeanContext get(Object key) throws EJBException;

   /**
    * Get the context with the given id, optionally marking the context as
    * being in use.
    * 
    * @param key  the context's id
    * @param markInUse if <code>true</code>, marks any returned context as
    *                  being in use.  If <code>false</code>, will return 
    *                  contexts that are marked as removed; otherwise will 
    *                  throw NoSuchEJBException if such a context is found
    *                     
    * @return the context
    * 
    * @throws NoSuchEJBException if no context with the given id exists or
    *                            if the context exists but has been marked
    *                            as removed and <code>markInUse</code> is 
    *                            <code>true</code>
    * @throws EJBException                       
    */
   public StatefulBeanContext get(Object key, boolean markInUse) throws EJBException;

   public void remove(Object key);

   public void finished(StatefulBeanContext ctx);

   public void initialize(Container container) throws Exception;

   void start();

   void stop();
   
   int getCacheSize();
   
   int getTotalSize();
   
   int getCreateCount();
   
   int getPassivatedCount();
   
   int getRemoveCount();
   
   int getAvailableCount();
   
   int getMaxSize();
   
   int getCurrentSize();
}
