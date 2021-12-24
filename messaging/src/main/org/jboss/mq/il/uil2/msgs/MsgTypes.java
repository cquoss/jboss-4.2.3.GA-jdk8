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
package org.jboss.mq.il.uil2.msgs;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57198 $
 */
public interface MsgTypes
{
   final static int m_acknowledge = 1;
   final static int m_addMessage = 2;
   final static int m_browse = 3;
   final static int m_checkID = 4;
   final static int m_connectionClosing = 5;
   final static int m_createQueue = 6;
   final static int m_createTopic = 7;
   final static int m_deleteTemporaryDestination = 8;
   final static int m_getID = 9;
   final static int m_getTemporaryQueue = 10;
   final static int m_getTemporaryTopic = 11;
   final static int m_receive = 13;
   final static int m_setEnabled = 14;
   final static int m_setSpyDistributedConnection = 15;
   final static int m_subscribe = 16;
   final static int m_transact = 17;
   final static int m_unsubscribe = 18;
   final static int m_destroySubscription = 19;
   final static int m_checkUser = 20;
   final static int m_ping = 21;
   final static int m_authenticate = 22;
   final static int m_close = 23;
   final static int m_pong = 24;
   final static int m_receiveRequest = 25;
   final static int m_recover = 26;
}
