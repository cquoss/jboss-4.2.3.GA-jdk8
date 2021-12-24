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
package org.jboss.mq.server.jmx;

import java.util.List;

/**
 * MBean interface.
 */
public interface TopicMBean extends org.jboss.mq.server.jmx.DestinationMBean
{

   java.lang.String getTopicName();

   int getAllMessageCount();

   int getDurableMessageCount();

   int getNonDurableMessageCount();

   int getAllSubscriptionsCount();

   int getDurableSubscriptionsCount();

   int getNonDurableSubscriptionsCount();

   java.util.List listAllSubscriptions();

   java.util.List listDurableSubscriptions();

   java.util.List listNonDurableSubscriptions();

   java.util.List listMessages(java.lang.String id) throws java.lang.Exception;

   java.util.List listMessages(java.lang.String id, java.lang.String selector) throws java.lang.Exception;

   List listNonDurableMessages(String id, String sub) throws Exception;

   List listNonDurableMessages(String id, String sub, String selector) throws Exception;

   List listDurableMessages(String id, String name) throws Exception;

   List listDurableMessages(String id, String name, String selector) throws Exception;

   long getNonDurableMessageCount(String id, String sub) throws Exception;

   long getDurableMessageCount(String id, String name) throws Exception;

   List listNonDurableScheduledMessages(String id, String sub) throws Exception;

   List listNonDurableScheduledMessages(String id, String sub, String selector) throws Exception;

   List listDurableScheduledMessages(String id, String name) throws Exception;

   List listDurableScheduledMessages(String id, String name, String selector) throws Exception;

   long getNonDurableScheduledMessageCount(String id, String sub) throws Exception;

   long getDurableScheduledMessageCount(String id, String name) throws Exception;

   List listNonDurableInProcessMessages(String id, String sub) throws Exception;

   List listNonDurableInProcessMessages(String id, String sub, String selector) throws Exception;

   List listDurableInProcessMessages(String id, String name) throws Exception;

   List listDurableInProcessMessages(String id, String name, String selector) throws Exception;

   long getNonDurableInProcessMessageCount(String id, String sub) throws Exception;

   long getDurableInProcessMessageCount(String id, String name) throws Exception;
}
