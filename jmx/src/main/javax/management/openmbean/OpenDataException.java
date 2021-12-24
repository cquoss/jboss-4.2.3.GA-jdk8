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
import javax.management.JMException;

/**
 * Thrown when an open type, open data or open mbean meta data object is
 * attempted to be constructed that is not valid.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 */
public class OpenDataException
   extends JMException
   implements Serializable
{
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   private static final long serialVersionUID = 8346311255433349870L;

   // Constructors --------------------------------------------------

   /**
    * Construct an open data exception with no message.
    */
   public OpenDataException()
   {
      super();
   }

   /**
    * Construct an open data exception with the passed message.
    *
    * @param message the message
    */
   public OpenDataException(String message)
   {
      super(message);
   }
}

