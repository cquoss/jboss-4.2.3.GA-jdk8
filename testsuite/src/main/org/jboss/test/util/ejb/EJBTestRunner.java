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
package org.jboss.test.util.ejb;

import java.rmi.RemoteException;
import java.util.Properties;
import javax.ejb.EJBObject;

/**
 * The remote interface of the server side test runner. The EJBTestClient calls
 * run with the names of the test class and test method to execute. Then run
 * calls setUpEJB, runTestCase, and tearDownEJB in sepperate transactions. In
 * order for the the tests to run as expected by the client the EJBTestRunner
 * bean must be setup exactly as follows in the ejb-jar.xml file:
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;!DOCTYPE ejb-jar PUBLIC 
 *       "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN"
 *       "http://java.sun.com/j2ee/dtds/ejb-jar_2_0.dtd"&gt;
 * &lt;ejb-jar&gt;
 *    &lt;enterprise-beans&gt;
 *       &lt;session&gt;
 *          &lt;description&gt;JUnit Session Bean Test Runner&lt;/description&gt;
 *          &lt;ejb-name&gt;EJBTestRunnerEJB&lt;/ejb-name&gt;
 *          &lt;home&gt;org.jboss.test.util.ejb.EJBTestRunnerHome&lt;/home&gt;
 *          &lt;remote&gt;org.jboss.test.util.ejb.EJBTestRunner&lt;/remote&gt;
 *          &lt;ejb-class&gt;org.jboss.test.util.ejb.EJBTestRunnerBean&lt;/ejb-class&gt;
 *          &lt;session-type&gt;Stateless&lt;/session-type&gt;
 *          &lt;transaction-type&gt;Bean&lt;/transaction-type&gt;
 *       &lt;/session&gt;
 *    &lt;/enterprise-beans&gt;
 * &lt;/ejb-jar&gt;
 * </pre>
 *
 * Additionally, the home interface must be bount to the jndi name:
 * "ejb/EJBTestRunner"
 *
 * It is recomended that the test classes and the classes of JUnitEJB be 
 * packaged into a single jar.
 *
 * @see EJBTestCase
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 61778 $
 */
public interface EJBTestRunner extends EJBObject
{
   /** Runs the specified test method on the specified class by calling
    * run(className, methodName, props) with props built from the java:comp/env
    * bindings.
    * 
    * @param className the name of the test class
    * @param methodName the name of the test method
    * @throws RemoteTestException If any throwable is thrown during execution 
    * of the method, it is wrapped with a RemoteTestException and rethrown.
    */
   public void run(String className, String methodName)
      throws RemoteTestException, RemoteException;

   /**
    * Runs the specified test method on the specified class.
    * @param className the name of the test class
    * @param methodName the name of the test method
    * @param props any properties passed in from the client for use by the
    *    server side tests
    * @throws RemoteTestException If any throwable is thrown during execution 
    * of the method, it is wrapped with a RemoteTestException and rethrown.
    */
   public void run(String className, String methodName, Properties props)
      throws RemoteTestException, RemoteException;
}
