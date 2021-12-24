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
package org.jboss.ejb3.stateful;

import org.jboss.ejb3.ServiceDelegateWrapper;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class StatefulDelegateWrapper extends ServiceDelegateWrapper implements StatefulDelegateWrapperMBean
{
   public StatefulDelegateWrapper(Object delegate)
   {
	   super(delegate);
   }
   
   public int getCacheSize()
   {
	   return ((StatefulContainer)delegate).getCache().getCacheSize();
   }
   
   public int getTotalSize()
   {
      return ((StatefulContainer)delegate).getCache().getTotalSize();
   }
   
   public int getPassivatedCount()
   {
	   return ((StatefulContainer)delegate).getCache().getPassivatedCount();
   }
   
   public int getCreateCount()
   {
	   return ((StatefulContainer)delegate).getCache().getCreateCount();
   }
   
   public int getRemoveCount()
   {
      return ((StatefulContainer)delegate).getCache().getRemoveCount();
   }
   
   public int getAvailableCount()
   {
      return ((StatefulContainer)delegate).getCache().getAvailableCount();
   }
   
   public int getMaxSize()
   {
      return ((StatefulContainer)delegate).getCache().getMaxSize();
   }
   
   public int getCurrentSize()
   {
      return ((StatefulContainer)delegate).getCache().getCurrentSize();
   }
   
}
