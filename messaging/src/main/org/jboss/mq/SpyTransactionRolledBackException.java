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

import java.io.PrintStream;
import java.io.PrintWriter;

import javax.jms.TransactionRolledBackException;

import org.jboss.util.NestedException;
import org.jboss.util.NestedThrowable;

/**
 * A TransactionRolledBackException with a nested <tt>Throwable</tt> detail
 * object.
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version <tt>$Revision: 57198 $</tt>
 */
public class SpyTransactionRolledBackException extends TransactionRolledBackException implements NestedThrowable
{
   // Constants -----------------------------------------------------

   /** The serialVersionUID */
   static final long serialVersionUID = 1764748894215274537L;
   
   // Attributes ----------------------------------------------------

   /** The nested throwable */
   protected Throwable nested;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------

   /**
    * Construct a <tt>SpyTransactionRolledBackException</tt> with the
    * specified detail message.
    * 
    * @param msg Detail message.
    */
   public SpyTransactionRolledBackException(final String msg)
   {
      super(msg);
      this.nested = null;
   }

   /**
    * Construct a <tt>SpyTransactionRolledBackException</tt> with the
    * specified detail message and nested <tt>Throwable</tt>.
    * 
    * @param msg Detail message.
    * @param nested Nested <tt>Throwable</tt>.
    */
   public SpyTransactionRolledBackException(final String msg, final Throwable nested)
   {
      super(msg);
      this.nested = nested;
      NestedThrowable.Util.checkNested(this, nested);
   }

   /**
    * Construct a <tt>SpyTransactionRolledBackException</tt> with the
    * specified nested <tt>Throwable</tt>.
    * 
    * @param nested Nested <tt>Throwable</tt>.
    */
   public SpyTransactionRolledBackException(final Throwable nested)
   {
      this(nested.getMessage(), nested);
   }
   
   // Public --------------------------------------------------------
   
   // NestedException implementation --------------------------------

   /**
    * Return the nested <tt>Throwable</tt>.
    * 
    * @return Nested <tt>Throwable</tt>.
    */
   public Throwable getNested()
   {
      return nested;
   }
   
   // Throwable overrides -------------------------------------------

   public Throwable getCause()
   {
      return nested;
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
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}