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
package test.compliance.openmbean;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the openmbeans.
 *
 * @author <a href="mailto:AdrianBrock@HappeningTimes.com">Adrian Brock</a>.
 */
public class OpenMBeanSUITE
  extends TestSuite
{
  /**
   * Run the tests
   * 
   * @param args the arguments for the test
   */
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  /**
   * Get a list of tests.
   *
   * @return the tests
   */
  public static Test suite()
  {
    TestSuite suite = new TestSuite("OpenMBean Tests");

    suite.addTest(new TestSuite(OpenTypeTestCase.class));
    suite.addTest(new TestSuite(SimpleTypeTestCase.class));
    suite.addTest(new TestSuite(CompositeTypeTestCase.class));
    suite.addTest(new TestSuite(TabularTypeTestCase.class));
    suite.addTest(new TestSuite(ArrayTypeTestCase.class));
    suite.addTest(new TestSuite(CompositeDataSupportTestCase.class));
    suite.addTest(new TestSuite(TabularDataSupportTestCase.class));
    suite.addTest(new TestSuite(OpenMBeanParameterInfoSupportTestCase.class));
    suite.addTest(new TestSuite(OpenMBeanAttributeInfoSupportTestCase.class));
    suite.addTest(new TestSuite(OpenMBeanConstructorInfoSupportTestCase.class));
    suite.addTest(new TestSuite(OpenMBeanOperationInfoSupportTestCase.class));
    suite.addTest(new TestSuite(OpenMBeanInfoSupportTestCase.class));

    return suite;
  }
}
