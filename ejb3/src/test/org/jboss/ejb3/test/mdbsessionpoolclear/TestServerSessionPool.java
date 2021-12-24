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
package org.jboss.ejb3.test.mdbsessionpoolclear;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.XAQueueConnection;
import javax.jms.XAQueueSession;
import javax.jms.XASession;
import javax.jms.XATopicConnection;
import javax.jms.XATopicSession;
import javax.transaction.TransactionManager;

import org.jboss.jms.asf.StdServerSession;
import org.jboss.logging.Logger;
import org.jboss.tm.XidFactoryMBean;


public class TestServerSessionPool extends org.jboss.jms.asf.StdServerSessionPool
{
   private static final Logger log = Logger.getLogger(TestServerSessionPool.class);
   
   public TestServerSessionPool(final Destination destination,
                              final Connection con,
                              final boolean transacted,
                              final int ack,
                              final boolean useLocalTX,
                              final MessageListener listener,
                              final int minSession,
                              final int maxSession,
                              final long keepAlive,
                              final boolean forceClear,
                              final long forceClearInterval,
                              final int forceClearAttempts,
                              final XidFactoryMBean xidFactory,
                              final TransactionManager tm)
      throws JMSException
   {
      super(destination, con, transacted, ack, useLocalTX, listener, minSession, maxSession, keepAlive, forceClear, forceClearInterval, forceClearAttempts, xidFactory, tm);
   }
   
   protected void create() throws JMSException
   {
      for (int index = 0; index < poolSize; index++)
      {
         // Here is the meat, that MUST follow the spec
         Session ses = null;
         XASession xaSes = null;

         log.debug("initializing with connection: " + con);

         if (destination instanceof Topic && con instanceof XATopicConnection)
         {
            xaSes = ((XATopicConnection)con).createXATopicSession();
            ses = ((XATopicSession)xaSes).getTopicSession();
         }
         else if (destination instanceof Queue && con instanceof XAQueueConnection)
         {
            xaSes = ((XAQueueConnection)con).createXAQueueSession();
            ses = ((XAQueueSession)xaSes).getQueueSession();
         }
         else if (destination instanceof Topic && con instanceof TopicConnection)
         {
            ses = ((TopicConnection)con).createTopicSession(transacted, ack);
            log.warn("Using a non-XA TopicConnection.  " +
                  "It will not be able to participate in a Global UOW");
         }
         else if (destination instanceof Queue && con instanceof QueueConnection)
         {
            ses = ((QueueConnection)con).createQueueSession(transacted, ack);
            log.warn("Using a non-XA QueueConnection.  " +
                  "It will not be able to participate in a Global UOW");
         }
         else
         {
            throw new JMSException("Connection was not reconizable: " + con + " for destination " + destination);
         }

         // create the server session and add it to the pool - it is up to the
         // server session to set the listener
         StdServerSession serverSession = new TestServerSession(this, ses, xaSes,
            listener, useLocalTX, xidFactory, tm);

         sessionPool.add(serverSession);
         numServerSessions++;

         log.debug("added server session to the pool: " + serverSession);
      }
   }
}
