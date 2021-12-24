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
package org.jboss.mq.il.oil2;

import java.io.IOException;

import org.jboss.mq.ReceiveRequest;
import org.jboss.mq.SpyDestination;
import org.jboss.mq.il.ClientIL;

/**
 * The OIL2 implementation of the ClientIL object
 *
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 * @version   $Revision:$
 */
public final class OIL2ClientIL
   implements ClientIL,
      java.io.Serializable
{
   static final long serialVersionUID = -2671278802714517625L;
   transient OIL2ServerILService.RequestListner requestListner;
   transient OIL2SocketHandler socketHandler;

   public void setRequestListner(OIL2ServerILService.RequestListner requestListner)
   {
      this.requestListner = requestListner;
      this.socketHandler = requestListner.getSocketHandler();
   }
   
   /**
    * #Description of the Method
    *
    * @exception Exception  Description of Exception
    */
   public void close()
          throws Exception
   {
      try {
         
         OIL2Request request = new OIL2Request(
            OIL2Constants.CLIENT_CLOSE,
            null);
         OIL2Response response = socketHandler.synchRequest(request);
         response.evalThrowsException();
         
      } catch ( IOException ignore ) {
         // Server closes the socket before we get a response..
         // this is ok.
      }
      
      // The close request now went full cycle, from the client
      // to the server, and back from the server to the client.
      // Close up the requestListner
      // This will shut down the sockets and threads.
      requestListner.close();
   }

   /**
    * #Description of the Method
    *
    * @param dest           Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void deleteTemporaryDestination(SpyDestination dest)
          throws Exception
   {
      
      OIL2Request request = new OIL2Request(
         OIL2Constants.CLIENT_DELETE_TEMPORARY_DESTINATION,
         new Object[] {dest});
      OIL2Response response = socketHandler.synchRequest(request);
      response.evalThrowsException();
   }

   /**
    * #Description of the Method
    *
    * @param serverTime     Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void pong(long serverTime)
          throws Exception
   {
      OIL2Request request = new OIL2Request(
         OIL2Constants.CLIENT_PONG,
         new Object[] {new Long(serverTime)});
      OIL2Response response = socketHandler.synchRequest(request);
      response.evalThrowsException();      
   }

   /**
    * #Description of the Method
    *
    * @param messages       Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void receive(ReceiveRequest messages[])
          throws Exception
   {
      
      OIL2Request request = new OIL2Request(
         OIL2Constants.CLIENT_RECEIVE,
         new Object[] {messages});
      OIL2Response response = socketHandler.synchRequest(request);
      response.evalThrowsException();            
   }
   
}
// vim:expandtab:tabstop=3:shiftwidth=3
