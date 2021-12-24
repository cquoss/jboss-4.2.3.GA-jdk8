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
 * Wraps runtime exceptions thrown by MBeans.
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
 * <p><b>20020711 Adrian Brock:</b>
 * <ul>
 * <li> Serialization </li>
 * </ul>
 */
public class RuntimeMBeanException
   extends JMRuntimeException
{
   // Constants -----------------------------------------------------

   private static final long serialVersionUID = 5274912751982730171L;

   // Attributes ----------------------------------------------------

   /**
    * The wrapped runtime exception.
    */
   private RuntimeException runtimeException = null;


   // Constructors --------------------------------------------------

   /**
    * Construct a new RuntimeMBeanException from a given runtime exception.
    *
    * @param e the runtime exception to wrap.
    */
   public RuntimeMBeanException(RuntimeException e)
   {
      super();
      this.runtimeException = e;
   }

   /**
    * Construct a new RuntimeMBeanException from a given runtime exception
    * and message.
    *
    * @param e the runtime exception to wrap.
    * @param message the specified message.
    */
   public RuntimeMBeanException(RuntimeException e, String message)
   {
      super(message);
      this.runtimeException = e;
   }

   
   // Public --------------------------------------------------------

   /**
    * Retrieves the wrapped runtime exception.
    *
    * @return the wrapped runtime exception.
    */
   public RuntimeException getTargetException()
   {
      return runtimeException;
   }

   /**
    * Retrieves the wrapped runtime exception.
    *
    * @return the wrapped runtime exception.
    */
   public Throwable getCause()
   {
      return runtimeException;
   }

   
   // JMRuntimeException overrides ----------------------------------
   
   /**
    * Returns a string representation of this exception. The returned string
    * contains this exception name, message and a string representation of the
    * target exception if it has been set.
    *
    * @return string representation of this exception
    */
   public String toString()
   {
      return "RuntimeMBeanException: " + getMessage() + 
             ((runtimeException == null) ? "" : " Cause: " +
             runtimeException.toString());
   }

}

