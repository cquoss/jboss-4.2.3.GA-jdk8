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
package org.jboss.test.deployers.web.test;

import java.util.HashSet;

import junit.framework.Test;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.test.deployers.AbstractDeploymentTest;

/**
 * A test that deploys WARs.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57211 $
 */
public class WEBDeploymentUnitTestCase extends AbstractDeploymentTest
{
  
   public void testWEBDeployment() throws Exception
   {
      final HashSet expected = new HashSet();
      expected.add(web1Deployment);
      
      DeploymentInfo topInfo = assertDeployed(web1Deployment);
      CheckExpectedDeploymentInfoVisitor visitor = new CheckExpectedDeploymentInfoVisitor(expected);
      visitor.start(topInfo);
      assertTrue("Expected subdeployments: " + expected, expected.isEmpty());
   }
   
   public WEBDeploymentUnitTestCase(String test)
   {
      super(test);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(WEBDeploymentUnitTestCase.class, web1Deployment);
   }
}
