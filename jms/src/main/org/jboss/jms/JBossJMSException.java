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
package org.jboss.jms;

import javax.jms.JMSException;

/**
 * A JMS exception that allows for an embedded exception 
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class JBossJMSException
   extends JMSException
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /**
    * The causing exception
    */
   private Throwable cause;

   // Static --------------------------------------------------------

   /**
    * Handle an exception
    * 
    * @param t the exception
    * @return the resultant JMSException
    */
   public static JMSException handle(Throwable t)
   {
      if (t instanceof JMSException)
         return (JMSException) t;
      return new JBossJMSException("Error",t);
   }

   // Constructors --------------------------------------------------

   /**
    * Construct a new JBossJMSException with the give message
    *
    * @param message the message
    */
   public JBossJMSException(String message)
   {
      super(message);
   }

   /**
    * Construct a new JBossJMSException with the give message and
    * cause
    *
    * @param message the message
    * @param cause the cause
    */
   public JBossJMSException(String message, Throwable cause)
   {
      super(message);
      this.cause = cause;
   }

   // Public --------------------------------------------------------

   // X implementation ----------------------------------------------

   // Throwable overrides -------------------------------------------

   public Throwable getCause()
   {
      return cause;
   }

   // Protected ------------------------------------------------------

   // Package Private ------------------------------------------------

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------
}
