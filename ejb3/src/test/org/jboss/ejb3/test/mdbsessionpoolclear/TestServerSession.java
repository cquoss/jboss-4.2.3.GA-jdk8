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
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.XASession;
import javax.transaction.TransactionManager;

import org.jboss.jms.asf.StdServerSessionPool;
import org.jboss.logging.Logger;
import org.jboss.tm.XidFactoryMBean;


public class TestServerSession extends org.jboss.jms.asf.StdServerSession
{
   private static final Logger log = Logger.getLogger(TestServerSession.class);
   
   public TestServerSession(final StdServerSessionPool pool,
                           final Session session,
                           final XASession xaSession,
                           final MessageListener delegateListener,
                           boolean useLocalTX,
                           final XidFactoryMBean xidFactory,
                           final TransactionManager tm)
      throws JMSException
   {
      super(pool, session, xaSession, delegateListener, useLocalTX, xidFactory, tm);
   }
   
   public void run()
   {  
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("running...");
      try
      {
         if (xaSession != null)
            xaSession.run();
         else
            session.run();
      }
      finally
      {
         // don't recycle
      }
   }
}
