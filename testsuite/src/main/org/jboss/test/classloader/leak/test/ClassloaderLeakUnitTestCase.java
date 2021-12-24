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
package org.jboss.test.classloader.leak.test;

import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.classloader.leak.ejb.interfaces.StatefulSession;
import org.jboss.test.classloader.leak.ejb.interfaces.StatefulSessionHome;
import org.jboss.test.classloader.leak.ejb.interfaces.StatelessSession;
import org.jboss.test.classloader.leak.ejb.interfaces.StatelessSessionHome;

/**
 * Test for classloader leaks following deployment, use and undeployment
 * of various packages (wars, ejb jars and ears with and without scoped
 * classloaders).
 * <p/>
 * These tests were originally written to test for leaks caused by Jakarta 
 * Commons Logging.  As a result, there are various permutations of the tests
 * that store copies of commons-logging in different locations on the classpath.
 * <p/>
 * If these tests are run with JBoss Profiler's jbossAgent (.dll or .so) on the path
 * and the AS is started with -agentlib:jbossAgent, in case of classloader leakage
 * an extensive report will be logged to the server log, showing the path to root of
 * all references to the classloader.
 * 
 * @author Brian Stansberry
 */
public class ClassloaderLeakUnitTestCase extends ClassloaderLeakTestBase
{
   private static final String SIMPLE_WAR = "classloader-leak-simple.war";
   private static final String WAR_WITH_JCL = "classloader-leak-in-war.war";
   private static final String SIMPLE_EJB = "classloader-leak-ejb.jar"; 
   private static final String SIMPLE_EAR = "classloader-leak-simple.ear"; 
   private static final String EAR_WITH_JCL = "classloader-leak-in-ear.ear";
   private static final String SIMPLE_ISOLATED_EAR = "classloader-leak-simple-isolated.ear";
   private static final String ISOLATED_EAR_WITH_JCL = "classloader-leak-in-ear-isolated.ear";
   private static final String EAR_WITH_DUAL_JCL = "classloader-leak-dual.ear";
   private static final String ISOLATED_EAR_WITH_DUAL_JCL = "classloader-leak-dual-isolated.ear";
   private static final String NO_WEB_EAR = "classloader-leak-noweb.ear";
   private static final String ISOLATED_NO_WEB_EAR = "classloader-leak-noweb-isolated.ear";
   
   private static final String EJB2_SLSB = "EJB2_SLSB";
   private static final String EJB2_SFSB = "EJB2_SFSB";
   private static final String EJB2_SLSB_TCCL = "EJB2_SLSB_TCCL";
   private static final String EJB2_SFSB_TCCL = "EJB2_SFSB_TCCL";
   
   private static final String[] EJB2 = new String[]{ EJB2_SLSB, EJB2_SLSB_TCCL, EJB2_SFSB, EJB2_SFSB_TCCL };
   
   
   public ClassloaderLeakUnitTestCase(String name)
   {
      super(name);
   }


   public static Test suite() throws Exception
   {
      return getDeploySetup(ClassloaderLeakUnitTestCase.class, "classloader-leak-test.sar");
   }
   
   public void testSimpleWar() throws Exception
   {
      warTest(SIMPLE_WAR);
   }
   
   public void testWarWithJcl() throws Exception
   {
      warTest(WAR_WITH_JCL);
   }
   
   public void testSimpleEjb() throws Exception
   {
      ejbTest(SIMPLE_EJB);
   }
   
   public void testSimpleEar() throws Exception
   {
      earTest(SIMPLE_EAR);
   }
   
   public void testEarWithJcl() throws Exception
   {
      earTest(EAR_WITH_JCL);
   }
   
   public void testNoWebEar() throws Exception
   {
      ejbTest(NO_WEB_EAR);
   }
   
   public void testSimpleIsolatedEar() throws Exception
   {
      earTest(SIMPLE_ISOLATED_EAR);
   }
   
   public void testIsolatedNoWebEar() throws Exception
   {
      ejbTest(ISOLATED_NO_WEB_EAR);
   }
   
   public void testIsolatedEarWithJcl() throws Exception
   {
      earTest(ISOLATED_EAR_WITH_JCL);
   }
   
   public void testEarWithDualJcl() throws Exception
   {
      earTest(EAR_WITH_DUAL_JCL);
   }
   
   public void testIsolatedEarWithDualJcl() throws Exception
   {
      earTest(ISOLATED_EAR_WITH_DUAL_JCL);
   }
   
   protected String getWarContextPath()
   {
      return "classloader-leak";
   }
   
   protected String[] getEjbKeys()
   {
      return EJB2;
   }
   
   protected void makeEjbRequests() throws Exception
   {
      InitialContext ctx = new InitialContext();
      StatelessSessionHome slsbhome = (StatelessSessionHome) ctx.lookup("ClassloaderLeakStatelessSession");
      StatelessSession slsbbean = slsbhome.create();
      slsbbean.log("EJB");
      StatefulSessionHome sfsbhome = (StatefulSessionHome) ctx.lookup("ClassloaderLeakStatefulSession");
      StatefulSession sfsbbean = sfsbhome.create();
      sfsbbean.log("EJB");
   }
}
