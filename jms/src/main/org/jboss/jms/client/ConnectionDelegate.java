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
package org.jboss.jms.client;

import java.util.Enumeration;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import org.jboss.jms.destination.JBossTemporaryDestination;

/**
 * The implementation of a connection
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public interface ConnectionDelegate
   extends Lifecycle
{
   // Constants -----------------------------------------------------

   // Public --------------------------------------------------------

   /**
    * Create a session
    * 
    * @param transacted whether the session is transacted
    * @param the acknowledgement mode
    * @return the session
    * @throws JMSException for any error
    */
   SessionDelegate createSession(boolean isXA, boolean transacted, int acknowledgeMode) throws JMSException;

   /**
    * Retrieve the extension property names
    * 
    * @return an enumeration of extension properties
    * @throws JMSException for any error
    */
   Enumeration getJMSXPropertyNames() throws JMSException;

   /**
    * Retrieve the client id
    * 
    * @return the client id
    * @throws JMSException for any error
    */
   String getClientID() throws JMSException;

   /**
    * Delete the temporary destination
    * 
    * @param the destination to delete
    * @throws JMSException for any error
    */
   void deleteTempDestination(JBossTemporaryDestination destination);

   /**
    * Set the client id
    * 
    * @param id the client id
    * @throws JMSException for any error
    */
   void setClientID(String id) throws JMSException;

   /**
    * Set the exception listener
    * 
    * @param the new exception listener
    * @throws JMSException for any error 
    */
   void setExceptionListener(ExceptionListener listener) throws JMSException;

   /**
    * Start the connection
    * 
    * @throws JMSException for any error 
    */
   void start() throws JMSException;

   /**
    * Stop the connection
    * 
    * @throws JMSException for any error 
    */
   void stop() throws JMSException;

   // Inner Classes --------------------------------------------------
}
