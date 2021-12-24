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
package org.jboss.test;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.jboss.logging.Logger;

/**
 * An abstract Test Case.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57204 $
 */
public abstract class AbstractTestCase extends TestCase
{
   /** The start time */
   long startTime;
   
   /**
    * Create a new abstract test case
    *
    * @param name the test name
    */
   public AbstractTestCase(String name)
   {
      super(name);
   }

   /**
    * Get the log for this test
    * 
    * @return the log
    */
   public abstract Logger getLog();
   
   public URL getResource(final String name)
   {
      final Class clazz = getClass();
      PrivilegedAction action = new PrivilegedAction()
      {
         public Object run()
         {
            return clazz.getResource(name);
         }
      };
      return (URL) AccessController.doPrivileged(action);
   }

   protected void setUp() throws Exception
   {
      log("Starting");
      startTime = System.currentTimeMillis();
   }

   protected void tearDown() throws Exception
   {
      getLog().debug(getName() + " took " + (System.currentTimeMillis() - startTime) + "ms");
      log("Stopping");
   }

   /**
    * Callback for configuring logging at the start of the test
    */
   protected void configureLogging()
   {
   }

   /**
    * Enable trace for a logging category
    *
    * @param name the logging category
    */
   protected abstract void enableTrace(String name);

   /**
    * Assert two float values are equal
    *
    * @param one the expected value
    * @param two the actual value
    */
   protected void assertEquals(float one, float two)
   {
      assertEquals(one, two, 0f);
   }

   /**
    * Assert two double values are equal
    *
    * @param one the expected value
    * @param two the actual value
    */
   protected void assertEquals(double one, double two)
   {
      assertEquals(one, two, 0f);
   }

   /**
    * Assert to arrays are equal
    * 
    * @param expected the expected array
    * @param actual the actual array
    */
   protected void assertEquals(Object[] expected, Object[] actual)
   {
      if (Arrays.equals(expected, actual) == false)
         throw new AssertionFailedError("expected: " + Arrays.asList(expected) + " actual: " + Arrays.asList(actual));
   }

   /**
    * Assert to arrays are equal
    * 
    * @param context the context
    * @param expected the expected array
    * @param actual the actual array
    */
   protected void assertEquals(String context, Object[] expected, Object[] actual)
   {
      if (Arrays.equals(expected, actual) == false)
         throw new AssertionFailedError(context + " expected: " + Arrays.asList(expected) + " actual: " + Arrays.asList(actual));
   }

   /**
    * Check we have the expected exception
    * 
    * @param expected the excepted class of the exception
    * @param throwable the real exception
    */
   protected void checkThrowable(Class expected, Throwable throwable)
   {
      if (expected == null)
         fail("Must provide an expected class");
      if (throwable == null)
         fail("Must provide a throwable for comparison");
      if (expected.equals(throwable.getClass()) == false)
      {
         getLog().error("Unexpected throwable", throwable);
         fail("Unexpected throwable: " + throwable);
      }
      else
      {
         getLog().debug("Got expected " + expected.getName() + "(" + throwable + ")");
      }
   }

   /**
    * Log an event with the given context
    *
    * @param context the context
    */   
   private void log(String context)
   {
      getLog().debug("==== " + context + " " + getName() + " ====");
   }
}
