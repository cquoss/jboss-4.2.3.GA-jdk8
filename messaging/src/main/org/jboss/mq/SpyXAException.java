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

import javax.transaction.xa.XAException;

import org.jboss.util.NestedException;
import org.jboss.util.NestedThrowable;

/**
 * An XAException with a nested throwable
 * 
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version <tt>$Revision: 65291 $</tt>
 */
public class SpyXAException extends XAException implements NestedThrowable
{
   /** The serialVersionUID */
   static final long serialVersionUID = 7814140228056884098L;

   /** The nested throwable */
   protected Throwable nested;

   /**
    * Converts a throwable to an XAException if it is not already
    * 
    * @param message any message to add to a constructed XAException
    * @param t the throwable
    * @return XAException never (always throws)
    * @throws XAException always
    */
   public static XAException rethrowAsXAException(String message, Throwable t) throws XAException
   {
      throw getAsXAException(message, t);
   }
   
   /**
    * Converts a throwable to an XAException if it is not already
    * 
    * @param message any message to add to a constructed XAException
    * @param t the throwable
    * @return an XAException
    */
   public static XAException getAsXAException(String message, Throwable t)
   {
      if (t instanceof XAException)
         return (XAException) t;
      else
      {
         SpyXAException e = new SpyXAException(message, t);
         e.errorCode = XAException.XAER_RMFAIL;
         return e;
      }
   }

   /**
    * Construct a <tt>SpyXAException</tt>
    */
   public SpyXAException()
   {
      super();
      this.nested = null;
   }

   /**
    * Construct a <tt>SpyXAException</tt> with the specified detail message.
    * 
    * @param msg Detail message.
    */
   public SpyXAException(final String msg)
   {
      super(msg);
      this.nested = null;
   }

   /**
    * Construct a <tt>SpyXAException</tt> with the specified detail message
    * and error code.
    * 
    * @param code Error code.
    */
   public SpyXAException(final int code)
   {
      super(code);
      this.nested = null;
   }

   /**
    * Construct a <tt>SpyXAException</tt>
    * 
    * @param throwable the nested throwable.
    */
   public SpyXAException(Throwable t)
   {
      super();
      this.nested = t;
   }

   /**
    * Construct a <tt>SpyXAException</tt> with the specified detail message.
    * 
    * @param msg Detail message.
    * @param throwable the nested throwable.
    */
   public SpyXAException(final String msg, Throwable t)
   {
      super(msg);
      this.nested = t;
   }

   /**
    * Construct a <tt>SpyXAException</tt> with the specified detail message
    * and error code.
    * 
    * @param code Error code.
    * @param throwable the nested throwable.
    */
   public SpyXAException(final int code, Throwable t)
   {
      super(code);
      this.nested = t;
   }

   public Throwable getNested()
   {
      return nested;
   }

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
      //
      // jason: this is bad, but whatever... the jms folks should have had more
      // insight
      //
      if (nested == null)
         return this;
      if (nested instanceof Exception)
      {
         return (Exception) nested;
      }
      return new NestedException(nested);
   }

   public String getMessage()
   {
      return NestedThrowable.Util.getMessage(super.getMessage(), nested);
   }

   public void printStackTrace(final PrintStream stream)
   {
      if (nested == null || NestedThrowable.PARENT_TRACE_ENABLED)
      {
         super.printStackTrace(stream);
      }
      NestedThrowable.Util.print(nested, stream);
   }

   public void printStackTrace(final PrintWriter writer)
   {
      if (nested == null || NestedThrowable.PARENT_TRACE_ENABLED)
      {
         super.printStackTrace(writer);
      }
      NestedThrowable.Util.print(nested, writer);
   }

   public void printStackTrace()
   {
      printStackTrace(System.err);
   }
}