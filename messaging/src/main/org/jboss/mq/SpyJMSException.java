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

import java.io.PrintWriter;
import java.io.PrintStream;

import javax.jms.JMSException;

import org.jboss.util.NestedThrowable;
import org.jboss.util.NestedException;

/**
 * A common superclass for <tt>JMSException</tt> classes that can contain a
 * nested <tt>Throwable</tt> detail object.
 * 
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version <tt>$Revision: 57198 $</tt>
 */
public class SpyJMSException extends JMSException implements NestedThrowable
{
   /** The serialVersionUID */
   static final long serialVersionUID = 5216406958161784593L;

   /** The nested throwable */
   protected Throwable nested;

   /**
    * Converts a throwable to a JMSException if it is not already
    * 
    * @param message any message to add to a constructed JMSException
    * @param t the throwable
    * @throws JMSException always
    */
   public static void rethrowAsJMSException(String message, Throwable t) throws JMSException
   {
      throw getAsJMSException(message, t);
   }
   
   /**
    * Converts a throwable to a JMSException if it is not already
    * 
    * @param message any message to add to a constructed JMSException
    * @param t the throwable
    * @return a JMSException
    */
   public static JMSException getAsJMSException(String message, Throwable t)
   {
      if (t instanceof JMSException)
         return (JMSException) t;
      else
         return new SpyJMSException(message, t);
   }

   /**
	 * Construct a <tt>SpyJMSException</tt> with the specified detail message.
	 * 
	 * @param msg Detail message.
	 */
   public SpyJMSException(final String msg)
   {
      super(msg);
      this.nested = null;
   }

   /**
	 * Construct a <tt>SpyJMSException</tt> with the specified detail message
	 * and error code.
	 * 
	 * @param msg Detail message.
	 * @param code Error code.
	 */
   public SpyJMSException(final String msg, final String code)
   {
      super(msg, code);
      this.nested = null;
   }

   /**
	 * Construct a <tt>SpyJMSException</tt> with the specified detail message
	 * and nested <tt>Throwable</tt>.
	 * 
	 * @param msg Detail message.
	 * @param nested Nested <tt>Throwable</tt>.
	 */
   public SpyJMSException(final String msg, final Throwable nested)
   {
      super(msg);
      this.nested = nested;
      NestedThrowable.Util.checkNested(this, nested);
   }

   /**
	 * Construct a <tt>SpyJMSException</tt> with the specified nested <tt>Throwable</tt>.
	 * 
	 * @param nested Nested <tt>Throwable</tt>.
	 */
   public SpyJMSException(final Throwable nested)
   {
      this(nested.getMessage(), nested);
   }

   public void setLinkedException(final Exception e)
   {
      this.nested = e;
   }

   public Exception getLinkedException()
   {
      // jason: this is bad, but whatever... the jms folks should have had more
      // insight
      if (nested == null)
         return this;
      if (nested instanceof Exception)
         return (Exception) nested;
      return new NestedException(nested);
   }

   public Throwable getNested()
   {
      return nested;
   }

   public Throwable getCause()
   {
      return nested;
   }

   public String getMessage()
   {
      return NestedThrowable.Util.getMessage(super.getMessage(), nested);
   }

   public void printStackTrace(final PrintStream stream)
   {
      if (nested == null || NestedThrowable.PARENT_TRACE_ENABLED)
         super.printStackTrace(stream);
      NestedThrowable.Util.print(nested, stream);
   }

   public void printStackTrace(final PrintWriter writer)
   {
      if (nested == null || NestedThrowable.PARENT_TRACE_ENABLED)
         super.printStackTrace(writer);
      NestedThrowable.Util.print(nested, writer);
   }

   public void printStackTrace()
   {
      printStackTrace(System.err);
   }
}