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

import java.util.StringTokenizer;

import javax.management.MBeanServerConnection;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Derived implementation of JBossTestCase for cluster testing.
 *
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 65061 $
 * @see org.jboss.test.JBossTestCase
 */
public class JBossClusteredTestCase extends JBossTestCase
{
   JBossTestClusteredServices clusterServices;

   public JBossClusteredTestCase(String name)
   {
      super(name);
   }

   public void initDelegate()
   {
      clusterServices = new JBossTestClusteredServices(getClass().getName());
      delegate = clusterServices;
   }
   
   // Public --------------------------------------------------------
   
   public void testServerFound() throws Exception
   {
      if (deploymentException != null)
         throw deploymentException;
      assertTrue("Server was not found", getServers() != null);
   }


   public MBeanServerConnection[] getAdaptors() throws Exception
   {
      return clusterServices.getAdaptors();
   }

   public String[] getServers() throws Exception
   {
      return clusterServices.getServers();
   }

   public String[] getNamingURLs() throws Exception
   {
      return clusterServices.getNamingURLs();
   }
   public String[] getHANamingURLs() throws Exception
   {
      return clusterServices.getHANamingURLs();
   }
   public String[] getHttpURLs() throws Exception
   {
      return clusterServices.getHttpURLs();
   }

   protected void deploy(MBeanServerConnection server, String name) throws Exception
   {
      clusterServices.deploy(server, name);
   }

   protected void redeploy(MBeanServerConnection server, String name) throws Exception
   {
      clusterServices.redeploy(server, name);
   }

   protected void undeploy(MBeanServerConnection server, String name) throws Exception
   {
      clusterServices.undeploy(server, name);
   }

   public static Test getDeploySetup(final Test test, final String jarNames)
      throws Exception
   {
      JBossTestSetup wrapper = new JBossTestClusteredSetup(test)
      {

         protected void setUp() throws Exception
         {
            if (jarNames == null) return;
            deploymentException = null;
            try
            {
               // deploy the comma seperated list of jars
               StringTokenizer st = new StringTokenizer(jarNames, ", ");
               while (st != null && st.hasMoreTokens())
               {
                  String jarName = st.nextToken();
                  this.redeploy(jarName);
                  this.getLog().debug("deployed package: " + jarName);
               }
            }
            catch (Exception ex)
            {
               // Throw this in testServerFound() instead.
               deploymentException = ex;
            }
                
            // wait a few seconds so that the cluster stabilize
            synchronized (this)
            {
               wait(2000);
            }
         }

         protected void tearDown() throws Exception
         {
            if (jarNames != null)
            {
               // deploy the comma seperated list of jars
               StringTokenizer st = new StringTokenizer(jarNames, ", ");
               String[] depoyments = new String[st.countTokens()];
               for (int i = depoyments.length - 1; i >= 0; i--)
                  depoyments[i] = st.nextToken();
               for (int i = 0; i < depoyments.length; i++)
               {
                  String jarName = depoyments[i];
                  this.getLog().debug("Attempt undeploy of " + jarName);
                  this.undeploy(jarName);
                  this.getLog().debug("undeployed package: " + jarName);
               }            
            }
         }
      };
      return wrapper;
   }

   public static Test getDeploySetup(final Class clazz, final String jarName)
      throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(clazz));
      return getDeploySetup(suite, jarName);
   }

   /**
    * anil
    */
   public void setServerNames(String[] snames) throws Exception
   {
      ((JBossTestClusteredServices) delegate).setServerNames(snames);
   }

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------

}
