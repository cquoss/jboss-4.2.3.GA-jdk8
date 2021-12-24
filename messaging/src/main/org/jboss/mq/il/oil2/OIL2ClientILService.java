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
import java.rmi.RemoteException;

import org.jboss.mq.Connection;
import org.jboss.mq.ReceiveRequest;
import org.jboss.mq.SpyDestination;

/**
 * The OIL2 implementation of the ClientILService object
 *
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 * @version   $Revision: $
 */
public final class OIL2ClientILService
   implements org.jboss.mq.il.ClientILService, OIL2RequestListner
{
   //A link on my connection
   private Connection connection;

   // The OIL2Server that created the socketHandler
   OIL2ServerIL serverIL;

   // The SocketHandler we will be sharing with the ServerIL
   OIL2SocketHandler socketHandler;

   /**
    * getClientIL method comment.
    *
    * @return                         The ClientIL value
    * @exception java.lang.Exception  Description of Exception
    */
   public org.jboss.mq.il.ClientIL getClientIL() throws java.lang.Exception
   {
      return new OIL2ClientIL();
   }

   /**
    * init method comment.
    *
    * @param connection               Description of Parameter
    * @param props                    Description of Parameter
    * @exception java.lang.Exception  Description of Exception
    */
   public void init(org.jboss.mq.Connection connection, java.util.Properties props) throws java.lang.Exception
   {
      this.connection = connection;

   }

   /**
    * start method comment.
    *
    * @exception java.lang.Exception  Description of Exception
    */
   public void start() throws java.lang.Exception
   {
      serverIL = (OIL2ServerIL) connection.getServerIL();
      socketHandler = serverIL.socketHandler;
      socketHandler.setRequestListner(this);
   }

   /**
    * @exception java.lang.Exception  Description of Exception
    */
   public void stop() throws java.lang.Exception
   {
   }

   public void handleConnectionException(Exception e)
   {
      connection.asynchFailure("Connection failure", e);
      serverIL.close();
   }

   public void handleRequest(OIL2Request request)
   {
      Object result = null;
      Exception resultException = null;

      // now based upon the input directive, preform the 
      // requested action. Any exceptions are processed
      // and potentially returned to the client.
      //
      try
      {
         switch (request.operation)
         {
            case OIL2Constants.CLIENT_RECEIVE :
               connection.asynchDeliver((ReceiveRequest[]) request.arguments[0]);
               break;
            case OIL2Constants.CLIENT_DELETE_TEMPORARY_DESTINATION :
               connection.asynchDeleteTemporaryDestination((SpyDestination) request.arguments[0]);
               break;

            case OIL2Constants.CLIENT_CLOSE :
               connection.asynchClose();
               break;

            case OIL2Constants.CLIENT_PONG :
               connection.asynchPong(((Long) request.arguments[0]).longValue());
               break;
            default :
               throw new RemoteException("Bad method code !");
         } // switch
      }
      catch (Exception e)
      {
         resultException = e;
      } // try

      try
      {
         OIL2Response response = new OIL2Response(request);
         response.result = result;
         response.exception = resultException;
         socketHandler.sendResponse(response);
      }
      catch (IOException e)
      {
         handleConnectionException(e);
      }

   }

}
// vim:expandtab:tabstop=3:shiftwidth=3
