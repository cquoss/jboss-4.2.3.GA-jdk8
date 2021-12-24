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
 * A wrapper for exceptions thrown by MBeans that implement
 * MBeanRegistration. These exceptions are thrown in preRegister and
 * preDeregister.
 *
 * @see javax.management.MBeanRegistration
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 */
public class MBeanRegistrationException
   extends MBeanException
{
   // Constants -----------------------------------------------------

   private static final long serialVersionUID = 4482382455277067805L;

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Construct a new MBeanRegistrationException from a given exception.
    *
    * @param e the exception to wrap.
    */

   public MBeanRegistrationException(Exception e)
   {
      super(e);
   }

   /**
    * Construct a new MBeanRegistrationException from a given exception
    * and message.
    *
    * @param e the exception to wrap.
    * @param message the specified message.
    */
   public MBeanRegistrationException(Exception e, String message)
   {
      super(e, message);
   }

   // Public --------------------------------------------------------

   // MBeanException overrides --------------------------------------

   // Private -------------------------------------------------------
}

