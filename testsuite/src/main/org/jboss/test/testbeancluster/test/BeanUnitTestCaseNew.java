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
package org.jboss.test.testbeancluster.test;


import java.util.Date;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.testbeancluster.interfaces.StatelessSession;

import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.testbeancluster.interfaces.StatelessSessionHome;
import org.jboss.proxy.ejb.RetryInterceptor;

/**
 * Test SLSB for load-balancing behaviour
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 57211 $
 */
public class BeanUnitTestCaseNew extends JBossClusteredTestCase
{
   static boolean deployed = false;
   public static int test = 0;
   static Date startDate = new Date();

   public BeanUnitTestCaseNew(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      Test t1 = JBossClusteredTestCase.getDeploySetup(BeanUnitTestCaseNew.class, "testbeancluster.jar");
      return t1;
   }

   public void testStatelessRetryInterceptor()
      throws Exception
   {
      getLog().debug("+++ testStatelessRetryInterceptor");

      // Connect to the server0 JNDI
      String[] urls = getNamingURLs();
      Properties env1 = new Properties();
      env1.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jnp.interfaces.NamingContextFactory");
      env1.setProperty(Context.PROVIDER_URL, urls[0]);
      InitialContext ctx = new InitialContext(env1);

      RetryInterceptor.setRetryEnv(env1);

      getLog().debug("Looking up nextgen_RetryInterceptorStatelessSession");
      StatelessSessionHome statelessSessionHome =
         (StatelessSessionHome) ctx.lookup("nextgen_RetryInterceptorStatelessSession");
      StatelessSession statelessSession = statelessSessionHome.create();

      statelessSession.makeCountedCall();
      System.err.println("Sleeping 60 seconds, restart servers");
      Thread.sleep(60 * 1000);
      statelessSession.makeCountedCall();
      getLog().debug("Called makeCountedCall");
      statelessSession.remove();
      getLog().debug("Called remove");
   }


}
