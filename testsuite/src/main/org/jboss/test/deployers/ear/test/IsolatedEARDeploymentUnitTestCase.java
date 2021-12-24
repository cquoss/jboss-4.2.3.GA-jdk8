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
package org.jboss.test.deployers.ear.test;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;

import junit.framework.Test;

import org.jboss.deployment.EARDeployerMBean;
import org.jboss.test.JBossTestSetup;

/**
 * A test that deploys everything in an EAR in isolated mode.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57211 $
 */
public class IsolatedEARDeploymentUnitTestCase extends EARDeploymentUnitTestCase
{
   public IsolatedEARDeploymentUnitTestCase(String test)
   {
      super(test);
   }

   public static Test suite() throws Exception
   {
      JBossTestSetup delegate = (JBossTestSetup) getDeploySetup(IsolatedEARDeploymentUnitTestCase.class, ear1Deployment);
      return new JBossTestSetup(delegate)
      {
         public void setUp() throws Exception
         {
            isolateDeployments(getServer(), Boolean.TRUE);
         }
         
         public void tearDown() throws Exception
         {
            isolateDeployments(getServer(), Boolean.FALSE);
         }
      };
   }
   
   private static void isolateDeployments(MBeanServerConnection server, Boolean value) throws Exception
   {
      server.setAttribute(EARDeployerMBean.OBJECT_NAME, new Attribute("Isolated", value));
      server.setAttribute(EARDeployerMBean.OBJECT_NAME, new Attribute("CallByValue", value));
   }
}
