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
package org.jboss.mq.il;

import org.jboss.mq.ReceiveRequest;
import org.jboss.mq.SpyDestination;

/**
 * This interface defines the methods that the server can make asynchronouly to
 * a client. (ie. to deliver messages)
 *
 * @author    Hiram Chirino (Cojonudo14@hotmail.com)
 * @author    Norbert Lataille (Norbert.Lataille@m4x.org)
 * @version   $Revision: 57198 $
 * @created   August 16, 2001
 */
public interface ClientIL
{

   /**
    * One TemporaryDestination has been deleted
    *
    * @param dest           Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void deleteTemporaryDestination(SpyDestination dest)
          throws Exception;

   /**
    * The connection is closed
    *
    * @exception Exception  Description of Exception
    */
   public void close()
          throws Exception;

   //
   /**
    * A message has arrived for the Connection. Deliver messages to client.
    *
    * @param messages       Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void receive(ReceiveRequest messages[])
          throws Exception;

   /**
    *  Response to a ping sent by a client.
    *
    * @param serverTime     Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void pong(long serverTime) throws Exception;
}
