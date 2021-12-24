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

import javax.jms.DeliveryMode;
import javax.jms.JMSException;

/**
 * Standard validation 
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision: 57195 $
 */
public class JMSValidator
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   /**
    * Validate the delivery mode
    * 
    * @param the delivery mode to validate
    * @throws JMSException for any error 
    */
   public static void validateDeliveryMode(int deliveryMode)
      throws JMSException
   {
      if (deliveryMode != DeliveryMode.NON_PERSISTENT &&
          deliveryMode != DeliveryMode.PERSISTENT)
         throw new JMSException("Invalid delivery mode " + deliveryMode);
   }

   /**
    * Validate the priority
    * 
    * @param the priority to validate
    * @throws JMSException for any error 
    */
   public static void validatePriority(int priority)
      throws JMSException
   {
      if (priority < 0 || priority > 9)
         throw new JMSException("Invalid priority " + priority);
   }

   /**
    * Validate the time to live
    * 
    * @param the ttl to validate
    * @throws JMSException for any error 
    */
   public static void validateTimeToLive(long timeToLive)
      throws JMSException
   {
   }

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // Protected ------------------------------------------------------

   // Package Private ------------------------------------------------

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------
}
