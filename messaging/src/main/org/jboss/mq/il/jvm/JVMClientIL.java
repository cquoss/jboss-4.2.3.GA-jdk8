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
package org.jboss.mq.il.jvm;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;

import org.jboss.mq.Connection;
import org.jboss.mq.ReceiveRequest;
import org.jboss.mq.SpyDestination;
import org.jboss.mq.il.ClientIL;

/**
 * The RMI implementation of the ConnectionReceiver object
 *
 * @author    Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author    Hiram Chirino (Cojonudo14@hotmail.com)
 * @version   $Revision: 57198 $
 * @created   August 16, 2001
 */
public class JVMClientIL implements ClientIL
{

   // A reference to the connection
   Connection connection;
   // Are we running
   boolean stopped = true;

   JVMClientIL(Connection c)
   {
      connection = c;
   }

   /**
    * #Description of the Method
    *
    * @exception Exception  Description of Exception
    */
   public void close()
          throws Exception
   {
      if (stopped)
      {
         throw new IllegalStateException("The client IL is stopped");
      }
      connection.asynchClose();
   }

   //One TemporaryDestination has been deleted
   /**
    * #Description of the Method
    *
    * @param dest              Description of Parameter
    * @exception JMSException  Description of Exception
    */
   public void deleteTemporaryDestination(SpyDestination dest)
          throws JMSException
   {
      if (stopped)
      {
         throw new IllegalStateException("The client IL is stopped");
      }
      connection.asynchDeleteTemporaryDestination(dest);
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
      if (stopped)
      {
         throw new IllegalStateException("The client IL is stopped");
      }
      
      //copy messages to avoid server side problems when messages are edited client side.
      for (int i = 0; i < messages.length; i++)
      {
         messages[i].message = messages[i].message.myClone();
      }
      
      connection.asynchDeliver(messages);
   }

   /**
    * pong method comment.
    *
    * @param serverTime                 Description of Parameter
    * @exception IllegalStateException  Description of Exception
    */
   public void pong(long serverTime)
          throws IllegalStateException
   {
      if (stopped)
      {
         throw new IllegalStateException("The client IL is stopped");
      }
      connection.asynchPong(serverTime);
   }
}
