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

import javax.jms.JMSException;

import org.jboss.mq.SpyDestination;
import org.jboss.mq.SpyJMSException;
import org.jboss.mq.pm.Tx;

/**
 *  This class implements a persistent version of the basic queue.
 *
 * @author     David Maplesden (David.Maplesden@orion.co.nz)
 * @created    August 16, 2001
 */

public class PersistentQueue extends org.jboss.mq.server.BasicQueue
{
   SpyDestination destination;

   public PersistentQueue(JMSDestinationManager server, SpyDestination destination, BasicQueueParameters parameters) throws JMSException
   {
      super(server, destination.toString(), parameters);
      this.destination = destination;
   }

   public SpyDestination getSpyDestination()
   {
      return destination;
   }

   public void addMessage(MessageReference mesRef, Tx txId) throws JMSException
   {
      if (mesRef.isPersistent())
      {
         try
         {
            server.getPersistenceManager().add(mesRef, txId);
         }
         catch (Throwable t)
         {
            String error = "Error storing message: " + mesRef;
            log.debug(error, t);
            try
            {
               server.getMessageCache().remove(mesRef);
            }
            catch (Throwable ignored)
            {
               log.trace("Ignored error while removing from cache.", ignored);
            }
            SpyJMSException.rethrowAsJMSException(error, t);
         }
      }

      super.addMessage(mesRef, txId);
   }
}
