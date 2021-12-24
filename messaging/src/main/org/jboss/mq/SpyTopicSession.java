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

import javax.jms.JMSException;
import javax.jms.TopicSession;
import javax.jms.XATopicSession;

/**
 * This class implements <tt>javax.jms.TopicSession</tt> and <tt>javax.jms.XATopicSession</tt>.
 *
 * @author Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class SpyTopicSession extends SpySession implements TopicSession, XATopicSession
{
   /**
    * Create a new SpyTopicSession
    *
    */
   SpyTopicSession(Connection myConnection, boolean transacted, int acknowledgeMode)
   {
      this(myConnection, transacted, acknowledgeMode, false);
   }

   /**
    * Create a new SpyTopicSession
    *
    * @param myConnection the connection
    * @param transacted true for transacted, false otherwise
    * @param acknowledgeMode the acknowledgement mode
    * @param xaSession true for xa, false otherwise
    */
   SpyTopicSession(Connection myConnection, boolean transacted, int acknowledgeMode, boolean xaSession)
   {
      super(myConnection, transacted, acknowledgeMode, xaSession);
   }

   public TopicSession getTopicSession() throws JMSException
   {
      return this;
   }
}