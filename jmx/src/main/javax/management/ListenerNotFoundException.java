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
package javax.management;

/**
 * Thrown when a specified Listener does not exist.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 */
public class ListenerNotFoundException
   extends OperationsException
{
   // Constants -----------------------------------------------------

   private static final long serialVersionUID = -7242605822448519061L;

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Construct a new ListenerNotFoundException with no message.
    */
   public ListenerNotFoundException()
   {
      super();
   }

   /**
    * Construct a new ListenerNotFoundException with the given message.
    *
    * @param message the error message.
    */
   public ListenerNotFoundException(java.lang.String message)
   {
      super(message);
   }

   // Public --------------------------------------------------------

   // OperationsException overrides ---------------------------------

   // Private -------------------------------------------------------
}
