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

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.naming.Reference;

/**
 * The implementation
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public interface ImplementationDelegate 
{
   // Constants -----------------------------------------------------

   // Public --------------------------------------------------------

   /**
    * Create a connection delegate
    * 
    * @param the user name
    * @param the password
    * @return the connection delegate
    * @throws JMSException for any error
    */
   ConnectionDelegate createConnection(String userName, String password) throws JMSException;

   /**
    * Get a reference to the connection factory
    * 
    * @return a Reference
    * @throws NamingException for any error
    */
   Reference getReference() throws NamingException;

   // Inner Classes --------------------------------------------------
}
