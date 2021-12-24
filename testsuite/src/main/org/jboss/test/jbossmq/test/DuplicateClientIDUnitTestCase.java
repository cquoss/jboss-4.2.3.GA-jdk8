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
package org.jboss.test.jbossmq.test;

import javax.jms.InvalidClientIDException;
import javax.jms.JMSException;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;

import org.jboss.test.JBossTestCase;

/**
 * Duplciate client id tests
 *
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version <tt>$Revision: 57211 $</tt>
 */
public class DuplicateClientIDUnitTestCase extends JBossTestCase
{
   static String TOPIC_FACTORY = "ConnectionFactory";
   
   public DuplicateClientIDUnitTestCase(String name) throws Exception
   {
      super(name);
   }

   public void testDuplicateClientID() throws Exception
   {
      TopicConnection c1 = getTopicConnectionFactory().createTopicConnection();
      try
      {
         c1.setClientID("testClientID");
         TopicConnection c2 = getTopicConnectionFactory().createTopicConnection();
         try
         {
            c2.setClientID("testClientID");
            fail("Should not be here - duplicate client id");
         }
         catch (InvalidClientIDException expected)
         {
         }
         finally
         {
            c2.close();
         }
      }
      finally
      {
         c1.close();
      }
   }

   public void testPreconfiguredDuplicateClientID() throws Exception
   {
      TopicConnection c1 = getTopicConnectionFactory().createTopicConnection("john", "needle");
      try
      {
         try
         {
            TopicConnection c2 = getTopicConnectionFactory().createTopicConnection("john", "needle");
            c2.close();
            fail("Should not be here - duplicate client id");
         }
         catch (JMSException expected)
         {
         }
      }
      finally
      {
         c1.close();
      }
   }

   public void testNotDuplicateClientID() throws Exception
   {
      TopicConnection c1 = getTopicConnectionFactory().createTopicConnection();
      try
      {
         TopicConnection c2 = getTopicConnectionFactory().createTopicConnection();
         c2.close();
      }
      finally
      {
         c1.close();
      }
   }

   protected TopicConnectionFactory getTopicConnectionFactory() throws Exception
   {
      Context context = getInitialContext();
      return (TopicConnectionFactory) context.lookup(TOPIC_FACTORY);
   }
}

