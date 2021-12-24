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

import javax.jms.JMSException;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XAQueueConnection;
import javax.jms.XAQueueConnectionFactory;
import javax.jms.XATopicConnection;
import javax.jms.XATopicConnectionFactory;
import javax.naming.NamingException;
import javax.naming.Reference;

/**
 * This class implements <code>javax.jms.XATopicConnectionFactory</code> and
 * <code>javax.jms.XAQueueConnectionFactory</code>.
 * 
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version <tt>$Revision: 57198 $</tt>
 */
public class SpyXAConnectionFactory extends SpyConnectionFactory
   implements Serializable, XAConnectionFactory, XAQueueConnectionFactory, XATopicConnectionFactory
{
   // Constants -----------------------------------------------------

   /** The serialVersionUID */
   static final long serialVersionUID = -3869656253676593051L;
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------

   /**
    * Create a new SpyXAConnectionFactory
    *
    * @param factory the generic connection factory
    */
   public SpyXAConnectionFactory(GenericConnectionFactory factory)
   {
      super(factory);
   }

   /**
    * Create a new SpyXAConnectionFactory
    *
    * @param config the configuration
    */
   public SpyXAConnectionFactory(Properties config)
   {
      super(config);
   }
   
   // Public --------------------------------------------------------
   
   // XAConnectionFactory implementation ----------------------------

   public XAConnection createXAConnection() throws JMSException
   {
      try
      {
         return new SpyXAConnection(factory);
      }
      catch (JMSException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new SpyJMSException("Failed to create XAConnection", e);
      }
   }

   public XAConnection createXAConnection(String userName, String password) throws JMSException
   {
      try
      {
         if (userName == null)
            throw new SpyJMSException("Username is null");
         if (password == null)
            throw new SpyJMSException("Password is null");

         return new SpyXAConnection(userName, password, factory);
      }
      catch (JMSException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new SpyJMSException("Failed to create XAConnection", e);
      }
   }
   
   // XAQueueConnectionFactory implementation -----------------------

   public XAQueueConnection createXAQueueConnection() throws JMSException
   {
      return (XAQueueConnection) createXAConnection();
   }

   public XAQueueConnection createXAQueueConnection(String userName, String password) throws JMSException
   {
      return (XAQueueConnection) createXAConnection(userName, password);
   }
   
   // XATopicConnectionFactory implementation -----------------------

   public XATopicConnection createXATopicConnection() throws JMSException
   {
      return (XATopicConnection) createXAConnection();
   }

   public XATopicConnection createXATopicConnection(String userName, String password) throws JMSException
   {
      return (XATopicConnection) createXAConnection(userName, password);
   }
   
   // Referenceable implementation ----------------------------------

   public Reference getReference() throws NamingException
   {

      return new Reference("org.jboss.mq.SpyXAConnectionFactory", new org.jboss.mq.referenceable.ObjectRefAddr("DCF",
            factory),
            "org.jboss.mq.referenceable.SpyConnectionFactoryObjectFactory", null);
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}