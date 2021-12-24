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
package org.jboss.test.deployers;

import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServerConnection;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.DeploymentState;
import org.jboss.deployment.MainDeployerMBean;
import org.jboss.test.JBossTestCase;

/**
 * Abstract deployment test.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57211 $
 */
public class AbstractDeploymentTest extends JBossTestCase
{
   public static final String ear1Deployment = "testdeployers-ear1.ear";
   public static final String ear1DeploymentUnpacked = "unpacked-ear1.ear";
   public static final String ear2DeploymentUnpacked = "unpacked-ear2.ear";
   public static final String bean1Deployment = "testdeployers-bean1ejb.jar";
   public static final String bean1DeploymentUnpacked = "unpacked-bean1ejb.jar";
   public static final String notBean1Deployment = "bean1ejb-not.ajar";
   public static final String notBean1DeploymentUnpacked = "unpacked-bean1ejb-not.ajar";
   public static final String web1Deployment = "testdeployers-web1.war";
   public static final String web1DeploymentUnpacked = "unpacked-web1.war";
   public static final String notWeb1Deployment = "web1-not.awar";
   public static final String notWeb1DeploymentUnpacked = "unpacked-web1-not.awar";
   public static final String rar1Deployment = "testdeployers-mcf1.rar";
   public static final String rar1DeploymentUnpacked = "unpacked-mcf1.rar";
   public static final String notRar1Deployment = "mcf1-not.arar";
   public static final String notRar1DeploymentUnpacked = "unpacked-mcf1-not.arar";
   public static final String rarjar1Deployment = "testdeployers-mcf1.jar";
   public static final String client1Deployment = "testdeployers-client1.jar";
   public static final String client1DeploymentUnpacked = "unpacked-client1.jar";
   public static final String notClient1Deployment = "client1-not.ajar";
   public static final String notClient1DeploymentUnpacked = "unpacked-client1-not.ajar";
   public static final String ds1Deployment = "testdeployers-mcf1-ds.xml";
   public static final String ds1DeploymentUnpacked = "unpacked-mcf1-ds.xml";
   public static final String ds1DeploymentUnpacked2 = "unpacked2-mcf1-ds.xml";
   public static final String service1Deployment = "testdeployers-1-service.xml";
   public static final String sar1Deployment = "testdeployers-mbean1.sar";
   public static final String sar1DeploymentUnpacked = "unpacked-mbean1.sar";
   public static final String notSar1Deployment = "mbean1-not.asar";
   public static final String notSar1DeploymentUnpacked = "unpacked-mbean1-not.asar";
   
   protected DeploymentInfo assertDeployed(String deployment) throws Exception
   {
      DeploymentInfo result = getDeploymentInfo(deployment);
      assertNotNull("Unable to retrieve deployment info for " + deployment, result);
      return result;
   }
   
   protected void assertNotDeployed(String deployment) throws Exception
   {
      DeploymentInfo result = getDeploymentInfo(deployment);
      assertNull("Should not be deployed " + result, result);
   }
   
   protected DeploymentInfo getDeploymentInfo(String deployment) throws Exception
   {
      MBeanServerConnection server = getServer();
      URL deployURL = getDeployURL(deployment);
      return (DeploymentInfo) server.invoke(MainDeployerMBean.OBJECT_NAME, "getDeployment", new Object[] { deployURL } , new String[] { URL.class.getName() });
   }
   
   public AbstractDeploymentTest(String test)
   {
      super(test);
   }
   
   public static class DeploymentInfoVisitor
   {
      public void start(DeploymentInfo topLevel)
      {
         doVisit(topLevel);
      }
      
      protected void doVisit(DeploymentInfo info)
      {
         visit(info);

         Set subDeployments = info.subDeployments;
         if (subDeployments == null || subDeployments.size() == 0)
            return;
         
         for (Iterator i = subDeployments.iterator(); i.hasNext(); )
         {
            DeploymentInfo child = (DeploymentInfo) i.next();
            doVisit(child);
         }
      }
      
      public void visit(DeploymentInfo info)
      {
      }
   }
   
   public class CheckExpectedDeploymentInfoVisitor extends DeploymentInfoVisitor
   {
      protected HashSet expected;
      
      public CheckExpectedDeploymentInfoVisitor(HashSet expected)
      {
         this.expected = expected;
      }
      
      public void visit(DeploymentInfo info)
      {
         String shortName = info.shortName;
         log.info("Found deployment " + shortName);
         boolean found = expected.remove(shortName);
         if (found == false)
            fail(shortName + " not expected, or duplicate?");
         else
         {
            DeploymentState state = info.state;
            assertEquals("Should be fully deployed: " + shortName + " state=" + state, DeploymentState.STARTED, state);
         }
      }
   }
}
