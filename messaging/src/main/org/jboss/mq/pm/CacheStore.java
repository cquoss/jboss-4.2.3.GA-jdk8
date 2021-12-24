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
package org.jboss.mq.pm;

import javax.jms.JMSException;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.server.MessageReference;

/**
 * A cache store.
 * 
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author Paul Kendall (paul.kendall@orion.co.nz)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public interface CacheStore
{
   // Constants -----------------------------------------------------

   // Public --------------------------------------------------------

   /**
	 * Reads the message refered to by the MessagReference back as a SpyMessage
	 * 
	 * @param mh the message reference
    * @return the message
	 * @throws JMSException for any error
	 */
   SpyMessage loadFromStorage(MessageReference mh) throws JMSException;

   /**
	 * Stores the given message to secondary storeage. You should be able to use
	 * the MessagReference to load the message back later.
	 * 
    * @param mh the message reference
    * @param message the message
    * @throws JMSException for any error
	 */
   void saveToStorage(MessageReference mh, SpyMessage message) throws JMSException;

   /**
	 * Removes the message that was stored in secondary storage.
	 * 
    * @param mh the message reference
    * @throws JMSException for any error
	 */
   void removeFromStorage(MessageReference mh) throws JMSException;
}