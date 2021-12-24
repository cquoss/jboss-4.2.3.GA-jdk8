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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.logging.Logger;

/**
 * A AbstractTestCaseWithSetup.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57204 $
 */
public class AbstractTestCaseWithSetup extends AbstractTestCase
{
   /** Single run setup */
   private AbstractTestSetup setup;
   
   /**
    * Create a new test case
    *
    * @param name the test name
    */
   public AbstractTestCaseWithSetup(String name)
   {
      super(name);
   }

   public Logger getLog()
   {
      return getDelegate().getLog();
   }

   protected void enableTrace(String name)
   {
      getDelegate().enableTrace(name);
   }
   
   /**
    * Get the delegate
    * 
    * @return the delegate
    */
   protected AbstractTestDelegate getDelegate()
   {
      return AbstractTestSetup.delegate;
   }

   protected void setUp() throws Exception
   {
      // This is a single test run
      if (AbstractTestSetup.delegate == null)
      {
         setup = new AbstractTestSetup(this.getClass(), this);
         setup.setUp();
      }
      super.setUp();
   }

   protected void tearDown() throws Exception
   {
      super.tearDown();
      if (setup != null)
         setup.tearDown();
   }

   /**
    * Bootstrap the test
    * 
    * @param clazz the test class
    * @return the bootstrap wrapper test
    */
   public static Test suite(Class clazz)
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(clazz));
      return new AbstractTestSetup(clazz, suite);
   }
}
