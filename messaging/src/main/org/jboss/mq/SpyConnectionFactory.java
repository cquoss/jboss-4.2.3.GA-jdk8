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
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;

import org.jboss.mq.referenceable.ObjectRefAddr;

/**
 * This class implements <code>javax.jms.TopicConnectionFactory</code> and
 * <code>javax.jms.QueueConnectionFactory</code>.
 * 
 * @author Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version <tt>$Revision: 57198 $</tt>
 */
public class SpyConnectionFactory
   implements Serializable, ConnectionFactory, QueueConnectionFactory, TopicConnectionFactory, Referenceable
{
   /** The serialVersionUID */
   static final long serialVersionUID = 3392566934963731105L;

   /** The delegate factory */
   protected GenericConnectionFactory factory;

   /**
    * Create a new SpyConnectionFactory
    *
    * @param factory the delegate factory
    */
   public SpyConnectionFactory(GenericConnectionFactory factory)
   {
      this.factory = factory;
   }

   /**
    * Create a new SpyConnectionFactory
    *
    * @param config the configuration
    */
   public SpyConnectionFactory(Properties config)
   {
      this.factory = new GenericConnectionFactory(null, config);
   }

   /** 
    * For testing
    */
   public Properties getProperties()
   {
      if (factory == null)
         return null;
      else
         return factory.getProperties();
   }

   public Reference getReference() throws NamingException
   {
      return new Reference("org.jboss.mq.SpyConnectionFactory", new ObjectRefAddr("DCF", factory),
            "org.jboss.mq.referenceable.SpyConnectionFactoryObjectFactory", null);
   }

   public Connection createConnection() throws JMSException
   {
      return internalCreateConnection(SpyConnection.UNIFIED);
   }

   public Connection createConnection(String userName, String password) throws JMSException
   {
      return internalCreateConnection(SpyConnection.UNIFIED, userName, password);
   }

   public QueueConnection createQueueConnection() throws JMSException
   {
      return (QueueConnection) internalCreateConnection(SpyConnection.QUEUE);
   }

   public QueueConnection createQueueConnection(String userName, String password) throws JMSException
   {
      return (QueueConnection) internalCreateConnection(SpyConnection.QUEUE, userName, password);
   }

   public TopicConnection createTopicConnection() throws JMSException
   {
      return (TopicConnection) internalCreateConnection(SpyConnection.TOPIC);
   }

   public TopicConnection createTopicConnection(String userName, String password) throws JMSException
   {
      return (TopicConnection) internalCreateConnection(SpyConnection.TOPIC, userName, password);
   }

   /**
    * Create a connection
    * 
    * @param type the type
    * @return the connection
    * @throws JMSException for any error
    */
   protected Connection internalCreateConnection(int type) throws JMSException
   {
      try
      {
         return new SpyConnection(type, factory);
      }
      catch (JMSException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new SpyJMSException("Failed to create Connection", e);
      }
   }

   /**
    * Create a connection
    * 
    * @param type the type
    * @param userName the user name
    * @param password the password
    * @return the connection
    * @throws JMSException for any error
    */
   protected Connection internalCreateConnection(int type, String userName, String password) throws JMSException
   {
      try
      {
         if (userName == null)
            throw new SpyJMSException("Username is null");
         if (password == null)
            throw new SpyJMSException("Password is null");

         return new SpyConnection(type, userName, password, factory);
      }
      catch (JMSException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new SpyJMSException("Failed to create Connection", e);
      }
   }
}