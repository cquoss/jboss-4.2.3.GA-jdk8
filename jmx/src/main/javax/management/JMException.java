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
 * Exceptions thrown by JMX implementations. These types of errors
 * are due to incorrect invocations as opposed to JMRuntimeExceptions
 * which are errors during the invocation.
 *
 * @see javax.management.JMRuntimeException
 *
 * @author <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 */
public class JMException
   extends Exception
{
   // Constants -----------------------------------------------------

   private static final long serialVersionUID = 350520924977331825L;

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Construct a new JMException with no message.
    */
   public JMException()
   {
      super();
   }

   /**
    * Construct a new JMException with the given message.
    *
    * @param message the error message.
    */
   public JMException(String message)
   {
      super(message);
   }

   // Public --------------------------------------------------------

   // Exception overrides -------------------------------------------

   // Private -------------------------------------------------------
}
