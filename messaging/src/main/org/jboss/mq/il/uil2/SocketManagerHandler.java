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
package org.jboss.mq.il.uil2;

import org.jboss.mq.il.uil2.msgs.BaseMsg;
import org.jboss.util.stream.StreamListener;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57198 $
 */
public interface SocketManagerHandler
   extends StreamListener
{
   /**
    * Handle the message
    * @param msg the message to handler
    * @exception Exception for any error
    */
   public void handleMsg(BaseMsg msg) throws Exception;

   /**
    * Handle a stream notification
    *
    * @param stream the stream
    * @param size the bytes since the last notification
    */
   public void onStreamNotification(Object stream, int size);

   /**
    * Report a connection failure
    * @param error the error text
    * @param throwable the error
    */
   public void asynchFailure(String error, Throwable e);

   /**
    * Handle closedown, this maybe invoked many times
    * due to an explicit close and/or a connection failure.
    */
   public void close();
}
