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
package org.jboss.messaging.channel.interfaces;

import org.jboss.messaging.interfaces.MessageReference;

/**
 * A channel.
 * 
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public interface Channel
{
   // Constants -----------------------------------------------------

   // Public --------------------------------------------------------

   /**
    * Send a message to the channel.
    * 
    * @param reference the message reference to send
    */
   void send(MessageReference reference);

   /**
    * Receive a message from the channel.
    * 
    * @param wait the length of time to wait for a message if there are none
    *        immediately available, use -1 for no wait.
    * @return a message or null if there are no messages
    */
   MessageReference receive(long wait);
   
   /**
    * Close the channel
    */
   void close();
   
   // Inner Classes --------------------------------------------------
}
