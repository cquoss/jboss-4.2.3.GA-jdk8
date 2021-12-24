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

/**
 * This class defines the interface which is used by the ConnectionReceiver to
 * send messages to the consumers.
 * 
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public interface SpyConsumer
{
   // Public --------------------------------------------------------

   /**
    * A ConnectionReceiver uses this method to load a Consumer with a message
    *
    * @param mes the message
    * @throws JMSException for any error
    */
   void addMessage(SpyMessage mes) throws JMSException;

   /**
    * This is used to know what type of messages the consumer wants
    *
    * @return the subscription
    */
   Subscription getSubscription();
   
   // Inner classes -------------------------------------------------
}