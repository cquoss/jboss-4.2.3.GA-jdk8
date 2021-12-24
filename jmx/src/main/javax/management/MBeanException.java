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
 * A wrapper for exceptions thrown by MBeans.
 *
 * @author <a href="mailto:juha@jboss.org">Juha Lindfors</a>
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20020313 Juha Lindfors:</b>
 * <ul>
 * <li> Overriding toString() to print out the root exception </li>
 * </ul>
 *
 * <p><b>20020710 Adrian Brock:</b>
 * <ul>
 * <li> Serialization </li>
 * </ul>
 */
public class MBeanException
   extends JMException
{
   // Attributes ----------------------------------------------------

   /**
    * The wrapped exception.
    */
   private Exception exception = null;


   private static final long serialVersionUID = 4066342430588744142L;

   // Constructors --------------------------------------------------

   /**
    * Construct a new MBeanException from a given exception.
    *
    * @param exception the exception to wrap.
    */
   public MBeanException(Exception exception)
   {
      super();
      this.exception = exception;
   }

   /**
    * Construct a new MBeanException from a given exception and message.
    *
    * @param e the exception to wrap.
    * @param message the specified message.
    */
   public MBeanException(Exception exception, String message)
   {
      super(message);
      this.exception = exception;
   }

   
   // Public --------------------------------------------------------

   /**
    * Retrieves the wrapped exception.
    *
    * @return the wrapped exception.
    */
   public Exception getTargetException()
   {
     return exception;
   }

   /**
    * Retrieves the wrapped exception.
    *
    * @return the wrapped exception.
    */
   public Throwable getCause()
   {
      return exception;
   }

   
   // JMException overrides -----------------------------------------

   /**
    * Returns a string representation of this exception. The returned string
    * contains this exception name, message and a string representation of the
    * target exception if it has been set.
    *
    * @return string representation of this exception
    */
   public String toString()
   {
      return "MBeanException: " + getMessage() 
         + ((exception == null) ? "" : " Cause: " + exception.toString());
   }

}

