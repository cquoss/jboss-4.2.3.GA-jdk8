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
package org.jboss.mq;

import javax.jms.QueueSession;
import javax.jms.XAQueueSession;

/**
 * This class implements javax.jms.QueueSession and javax.jms.XAQueueSession
 *
 * @author Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class SpyQueueSession extends SpySession implements QueueSession, XAQueueSession
{
   /**
    * Create a new SpyQueueSession
    *
    * @param myConnection the connection
    * @param transacted true for transacted, false otherwise
    * @param acknowledgeMode the acknowledgement mode
    */
   SpyQueueSession(Connection myConnection, boolean transacted, int acknowledgeMode)
   {
      this(myConnection, transacted, acknowledgeMode, false);
   }

   /**
    * Create a new SpyQueueSession
    *
    * @param myConnection the connection
    * @param transacted true for transacted, false otherwise
    * @param acknowledgeMode the acknowledgement mode
    * @param xaSession true for xa, false otherwise
    */
   SpyQueueSession(Connection myConnection, boolean transacted, int acknowledgeMode, boolean xaSession)
   {
      super(myConnection, transacted, acknowledgeMode, xaSession);
   }

   public QueueSession getQueueSession() throws javax.jms.JMSException
   {
      return this;
   }
}