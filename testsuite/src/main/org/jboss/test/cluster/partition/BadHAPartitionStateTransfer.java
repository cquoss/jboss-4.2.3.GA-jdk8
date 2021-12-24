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
package org.jboss.test.cluster.partition;

import java.io.Serializable;

import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.interfaces.HAPartition.HAPartitionStateTransfer;
import org.jboss.ha.jmx.HAServiceMBeanSupport;

public class BadHAPartitionStateTransfer 
      extends HAServiceMBeanSupport 
      implements HAPartitionStateTransfer, BadHAPartitionStateTransferMBean
{   
   private HAPartition haPartition;
   private boolean returnState;

   public Serializable getCurrentState()
   {
      if (returnState)
         return new BadHAPartitionState();
      
      throw new BadHAPartitionStateException("Configured not to return state");
   }

   public void setCurrentState(Serializable newState)
   {
      // no-op
   }

   protected void setupPartition() throws Exception
   {
      if (haPartition == null)
      {
         super.setupPartition();
         haPartition = getPartition();
         haPartition.subscribeToStateTransferEvents(getServiceHAName(), this);
      }
   }

   protected void createService() throws Exception
   {
      super.createService();
      setupPartition();
   }

   public boolean getReturnState()
   {
      return returnState;
   }

   public void setReturnState(boolean returnState)
   {
      this.returnState = returnState;
   }
   
   

}
