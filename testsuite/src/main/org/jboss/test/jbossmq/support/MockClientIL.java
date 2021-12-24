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
package org.jboss.test.jbossmq.support;

import java.util.ArrayList;

import org.jboss.mq.ReceiveRequest;
import org.jboss.mq.SpyDestination;
import org.jboss.mq.il.ClientIL;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;

/**
 * MonitorCloseTestClientIL.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class MockClientIL implements ClientIL
{
   public SynchronizedBoolean closed = new SynchronizedBoolean(false);

   public ArrayList deletedDestinations = new ArrayList();
   
   public ArrayList pongs = new ArrayList();
   
   public ArrayList received = new ArrayList();
   
   public void close() throws Exception
   {
      closed.set(true);
   }

   public void deleteTemporaryDestination(SpyDestination dest) throws Exception
   {
      deletedDestinations.add(dest);
   }

   public void pong(long serverTime) throws Exception
   {
      pongs.add(new Long(serverTime));
   }

   public void receive(ReceiveRequest[] messages) throws Exception
   {
      for (int i = 0; i < messages.length; ++i)
         received.add(messages);
   }
   
   public ArrayList getReceivedMessages()
   {
      ArrayList result = new ArrayList();
      for (int i = 0; i < received.size(); ++i)
      {
         ReceiveRequest request = (ReceiveRequest) received.get(i);
         result.add(request.message);
      }
      return result;
   }
}
