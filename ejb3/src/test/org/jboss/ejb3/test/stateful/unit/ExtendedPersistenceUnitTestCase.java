/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
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
package org.jboss.ejb3.test.stateful.unit;

import junit.framework.Test;

import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * Tests for ExtendedPersistenceContext management.
 * 
 * This class uses a delegate to execute the tests so the clustered
 * version of the tests (which derive from a different base class)
 * can use the same delegate code.
 *
 * @author Ben Wang
 * @version $Id: ExtendedPersistenceUnitTestCase.java 60776 2007-02-21 17:38:52Z bstansberry@jboss.com $
 */
public class ExtendedPersistenceUnitTestCase extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(ExtendedPersistenceUnitTestCase.class);
   
   private XPCTestRunner runner;
   
   public ExtendedPersistenceUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ExtendedPersistenceUnitTestCase.class,
                               "stateful-test.jar");
   }

   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      runner = new XPCTestRunner(getInitialContext(), log);
      runner.setUp();
      // Use a sleep time just a bit longer than twice the bean timeout
      runner.setSleepTime(2100L);
   }


   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
      
      if (runner != null)
         runner.tearDown();
   }


   public void testBasic() throws Exception
   {
      runner.testBasic();
   }
   
   public void testDependentLifecycle() throws Exception
   {
      runner.testDependentLifecycle();
   }
   
   public void testXPCSharing() throws Exception
   {
      runner.testXPCSharing();
   }

   public void testPassivation() throws Exception
   {
      runner.testPassivation();
   }

}
