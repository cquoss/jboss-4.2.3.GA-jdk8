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
package javax.jms;

import java.util.Enumeration;

/** A client uses a <CODE>QueueBrowser</CODE> object to look at messages on a 
 * queue without removing them.
 *
 * <P>The <CODE>getEnumeration</CODE> method returns a 
 * <CODE>java.util.Enumeration</CODE> that is used to scan 
 * the queue's messages. It may be an enumeration of the entire content of a 
 * queue, or it may contain only the messages matching a message selector.
 *
 * <P>Messages may be arriving and expiring while the scan is done. The JMS API
 * does 
 * not require the content of an enumeration to be a static snapshot of queue 
 * content. Whether these changes are visible or not depends on the JMS 
 * provider.
 *
 *<P>A <CODE>QueueBrowser</CODE> can be created from either a 
 * <CODE>Session</CODE> or a <CODE> QueueSession</CODE>. 
 *
 *  @see         javax.jms.Session#createBrowser
 * @see         javax.jms.QueueSession#createBrowser
 * @see         javax.jms.QueueReceiver
 */

public interface QueueBrowser
{

   /** Gets the queue associated with this queue browser.
    * 
    * @return the queue
    *  
    * @exception JMSException if the JMS provider fails to get the
    *                         queue associated with this browser
    *                         due to some internal error.
    */

   Queue getQueue() throws JMSException;

   /** Gets this queue browser's message selector expression.
    *  
    * @return this queue browser's message selector, or null if no
    *         message selector exists for the message consumer (that is, if 
    *         the message selector was not set or was set to null or the 
    *         empty string)
    *
    * @exception JMSException if the JMS provider fails to get the
    *                         message selector for this browser
    *                         due to some internal error.
    */

   String getMessageSelector() throws JMSException;

   /** Gets an enumeration for browsing the current queue messages in the
    * order they would be received.
    *
    * @return an enumeration for browsing the messages
    *  
    * @exception JMSException if the JMS provider fails to get the
    *                         enumeration for this browser
    *                         due to some internal error.
    */

   Enumeration getEnumeration() throws JMSException;

   /** Closes the <CODE>QueueBrowser</CODE>.
    *
    * <P>Since a provider may allocate some resources on behalf of a 
    * QueueBrowser outside the Java virtual machine, clients should close them
    * when they 
    * are not needed. Relying on garbage collection to eventually reclaim 
    * these resources may not be timely enough.
    *
    * @exception JMSException if the JMS provider fails to close this
    *                         browser due to some internal error.
    */

   void close() throws JMSException;
}
