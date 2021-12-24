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
package org.jboss.jms.client.p2p;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.jboss.jms.MessageImpl;
import org.jboss.jms.client.ProducerDelegate;
import org.jboss.jms.message.JBossMessage;

/**
 * The p2p producer
 * 
 * @author <a href="mailto:nathan@jboss.org">Nathan Phelps</a>
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class P2PProducerDelegate
   implements ProducerDelegate
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private P2PSessionDelegate session = null;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public P2PProducerDelegate(P2PSessionDelegate session, Destination destination)
      throws JMSException
   {
      this.session = session;
   }

   // Public --------------------------------------------------------

   // ProducerDelegate implementation -------------------------------

	public void close() throws JMSException
	{
	}

	public void closing() throws JMSException
	{
	}

   public void send(Message message)
      throws JMSException
   {
      this.session.send((MessageImpl) ((MessageImpl) message).clone());
   }

   public JBossMessage encapsulateMessage(Message message)
   {
      // TODO encapsulateMessage
      return null;
   }

   // Protected ------------------------------------------------------

   // Package Private ------------------------------------------------

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------

}
