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

import javax.management.MBeanServer;

import org.jboss.ha.framework.server.HAPartitionImpl;
import org.jgroups.JChannel;

public class StartupTestHAPartition extends HAPartitionImpl
{
   private RuntimeException setStateException;
   private Exception startupException;
   
   public StartupTestHAPartition(String partitionName, JChannel channel, boolean deadlock_detection, MBeanServer server)
         throws Exception
   {
      super(partitionName, channel, deadlock_detection, server);
   }

   public StartupTestHAPartition(String partitionName, JChannel channel, boolean deadlock_detection) throws Exception
   {
      super(partitionName, channel, deadlock_detection);
   }

   public void setState(byte[] obj)
   {
      try
      {
         super.setState(obj);
      }
      catch (RuntimeException e)
      {
         setStateException = e;
         throw e;
      }
   }

   public void startPartition() throws Exception
   {
      try
      {
         super.startPartition();
      }
      catch (Exception e)
      {
         startupException = e;
         throw e;
      }
   }

   public RuntimeException getSetStateException()
   {
      return setStateException;
   }

   public Exception getStartupException()
   {
      return startupException;
   }
   
   
   
}
