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
package javax.management.openmbean;

import java.io.Serializable;

/**
 * Thrown when an open type of an open data is not correct.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 */
public class InvalidOpenTypeException
   extends IllegalArgumentException
   implements Serializable
{
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   private static final long serialVersionUID = -2837312755412327534L;

   // Constructors --------------------------------------------------

   /**
    * Construct an invalid open type exception with no message.
    */
   public InvalidOpenTypeException()
   {
      super();
   }

   /**
    * Construct an invalid opent type with the passed message.
    *
    * @param message the message
    */
   public InvalidOpenTypeException(String message)
   {
      super(message);
   }
}

