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

/**
 * Thrown when a message cannot be added to a queue or a subscription
 * because it is full.
 *
 * @author  <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version <tt>$Revision: 57198 $</tt>
 */
public class DestinationFullException
   extends SpyJMSException
{
   // Constants -----------------------------------------------------
   
   /** The serialVersionUID */
   private static final long serialVersionUID = -2818359593624770353L;
   
   // Attributes ----------------------------------------------------
   
   /** The text of the message before the nested mangles it */
   protected String text;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------

   /**
    * Create a new DestinationFullException
    *
    * @param msg the message text
    */
   public DestinationFullException(final String msg)
   {
      super(msg);
      this.text = msg;
   }
   
   // Public --------------------------------------------------------

   /**
    * Get the message text
    *
    * @return the text
    */
   public String getText()
   {
      return text;
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
