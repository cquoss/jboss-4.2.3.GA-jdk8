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
 * Thrown when an item name for composite data or a row index for tabular
 * data is invalid.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 */
public class InvalidKeyException
   extends IllegalArgumentException
   implements Serializable
{
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   private static final long serialVersionUID = 4224269443946322062L;

   // Constructors --------------------------------------------------

   /**
    * Construct an invalid key exception with no message.
    */
   public InvalidKeyException()
   {
      super();
   }

   /**
    * Construct an invalid key exception with the passed message.
    *
    * @param message the message
    */
   public InvalidKeyException(String message)
   {
      super(message);
   }
}

