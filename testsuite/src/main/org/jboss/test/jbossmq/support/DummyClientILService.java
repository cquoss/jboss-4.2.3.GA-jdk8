/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

import java.util.Properties;

import org.jboss.mq.Connection;
import org.jboss.mq.ReceiveRequest;
import org.jboss.mq.SpyDestination;
import org.jboss.mq.il.ClientIL;
import org.jboss.mq.il.ClientILService;
import org.jboss.mq.il.ServerIL;

/**
 * DummyClientILService.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class DummyClientILService implements ClientILService, ClientIL
{
   private Connection connection;
   
   public ClientIL getClientIL() throws Exception
   {
      return this;
   }

   public void init(Connection connection, Properties props) throws Exception
   {
      this.connection = connection;
      DummyServerIL serverIL = (DummyServerIL) connection.getServerIL();
      serverIL.setClientIL(this);
   }

   public void start() throws Exception
   {
      // Nothing
   }

   public void stop() throws Exception
   {
      throw new org.jboss.util.NotImplementedException("stop");
   }

   public void close() throws Exception
   {
      connection.asynchClose();
   }

   public void deleteTemporaryDestination(SpyDestination dest) throws Exception
   {
      throw new org.jboss.util.NotImplementedException("deleteTemporaryDestination");
   }

   public void pong(long serverTime) throws Exception
   {
      connection.asynchPong(serverTime);
   }

   public void receive(ReceiveRequest[] messages) throws Exception
   {
      throw new org.jboss.util.NotImplementedException("receive");
   }
}
