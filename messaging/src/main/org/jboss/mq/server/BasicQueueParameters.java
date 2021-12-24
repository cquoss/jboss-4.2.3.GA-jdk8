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
package org.jboss.mq.server;

import org.jboss.mq.SpyDestination;

/**
 * Parameters controlling a basic queue
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class BasicQueueParameters
{
   /** The maximum depth of a queue */
   public int maxDepth = 0;

   /** Whether we should do things in memory (needs support from pm) */
   public boolean inMemory = false;
   
   /** Whether we are late cloning */
   public boolean lateClone = false;

   /** The delay in milliseconds before a rolled back or recovered message is redelivered */
   public long redeliveryDelay = 0;

   /** 
    * The number of times a message will be redelivered after a recover or rollback. 
    * The value <code>-1</code> means there is no configured limit.
    */
   public int redeliveryLimit =-1 ;
   
   /** The receivers implementation class */
   public Class receiversImpl;
   
   /** The message counter history */
   public int messageCounterHistoryDayLimit = 0;
   
   /** The number of retries when recovering a queue/durable subscription */
   public int recoveryRetries = 0;
   
   /** The destination that receives expired messages */
   public SpyDestination expiryDestination = null;
}
