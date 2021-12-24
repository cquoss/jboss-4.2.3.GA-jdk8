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
package org.jboss.mq.server;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;

import org.jboss.mq.ConnectionToken;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.TransactionRequest;

/**
 * Makes all messages Non Persistent
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class NonPersistentInterceptor extends JMSServerInterceptorSupport
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // JMSServerInterceptorSupport overrides -------------------------

   public void addMessage(ConnectionToken dc, SpyMessage message) throws JMSException
   {
      makeNonPersistent(message);
      super.addMessage(dc, message);
   }

   public void transact(ConnectionToken dc, TransactionRequest t) throws JMSException
   {
      if (t.messages != null)
      {
         for (int i = 0; i < t.messages.length; ++i)
            makeNonPersistent(t.messages[i]);
      }
      super.transact(dc, t);
   }
   
   // Protected -----------------------------------------------------

   /**
    * Overrides the message to be Non Persistent
    * 
    * @param message the message
    * @throws JMSException for any error
    */
   protected void makeNonPersistent(SpyMessage message) throws JMSException
   {
      message.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
   }
   
   // Package Private -----------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
