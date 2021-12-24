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

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TopicSession;
import javax.jms.XAConnection;
import javax.jms.XAQueueConnection;
import javax.jms.XAQueueSession;
import javax.jms.XASession;
import javax.jms.XATopicConnection;
import javax.jms.XATopicSession;

/**
 * This class implements javax.jms.XAQueueConnection
 * 
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class SpyXAConnection extends SpyConnection implements Serializable, XAConnection, XATopicConnection, XAQueueConnection
{
   /** The serialVersionUID */
   static final long serialVersionUID = 1258716704996031025L;

   /**
    * Create a new SpyXAConnection
    *
    * @param userid the user
    * @param password the password
    * @param gcf the constructing class
    * @throws JMSException for any error
    */
   public SpyXAConnection(String userid, String password, GenericConnectionFactory gcf) throws JMSException
   {
      super(userid, password, gcf);
   }

   /**
    * Create a new SpyXAConnection
    *
    * @param gcf the constructing class
    * @throws JMSException for any error
    */
   public SpyXAConnection(GenericConnectionFactory gcf) throws JMSException
   {
      super(gcf);
   }
   
   public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException
   {
      return createXASession();
   }
   
   public QueueSession createQueueSession(boolean transacted, int acknowledgeMode) throws JMSException
   {
      return (QueueSession) createXAQueueSession();
   }

   public TopicSession createTopicSession(boolean transacted, int acknowledgeMode) throws JMSException
   {
      return (TopicSession) createXATopicSession();
   }

   public XASession createXASession() throws JMSException
   {
      checkClosed();
      checkClientID();

      XASession session = new SpySession(this, true, 0, true);
      //add the new session to the createdSessions list
      synchronized (createdSessions)
      {
         createdSessions.add(session);
      }
      return session;
   }

   public XAQueueSession createXAQueueSession() throws JMSException
   {
      checkClosed();
      checkClientID();

      XAQueueSession session = new SpyQueueSession(this, true, 0, true);

      //add the new session to the createdSessions list
      synchronized (createdSessions)
      {
         createdSessions.add(session);
      }

      return session;
   }

   public XATopicSession createXATopicSession() throws javax.jms.JMSException
   {
      checkClosed();
      checkClientID();

      XATopicSession session = new SpyTopicSession(this, true, 0, true);
      //add the new session to the createdSessions list
      synchronized (createdSessions)
      {
         createdSessions.add(session);
      }
      return session;
   }
}