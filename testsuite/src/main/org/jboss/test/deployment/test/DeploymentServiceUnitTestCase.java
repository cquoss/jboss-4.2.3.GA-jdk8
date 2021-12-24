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
package org.jboss.test.deployment.test;

import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Hashtable;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.test.JBossTestCase;

/** 
 * DeploymentService tests
 *
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 57211 $
 */
public class DeploymentServiceUnitTestCase extends JBossTestCase
{
   private ObjectName deploymentService = ObjectNameFactory.create("jboss:service=DeploymentService");
   private ObjectName mainDeployer = ObjectNameFactory.create("jboss.system:service=MainDeployer");
   
   public DeploymentServiceUnitTestCase(String name)
   {
      super(name);
   }

   /** 
    * Check if I can get the available templates
    */
   public void testListModuleTemplates() throws Exception
   {
      log.info("+++ testListModuleTemplates");
      MBeanServerConnection server = getServer();
      try
      {
         boolean isRegistered = server.isRegistered(deploymentService);
         assertTrue(deploymentService + " is registered", isRegistered);
         log.info("Loaded templates: " + server.invoke(
               deploymentService,
               "listModuleTemplates",
               new Object[] {},
               new String[] {}));
      }
      finally
      {
         // empty
      }
   }
  
   /**
    * Try to create a no-tx-datasource (don't deploy)
    */
   public void testCreateNoTxDataSource() throws Exception
   {
      log.info("+++ testCreateNoTxDataSource");
      try
      {
         String module = "test-no-tx-hsqldb-ds.xml"; 
         
         // remove module in case it exists
         removeModule(module);
         
         // Prepare the template properties
         HashMap props = new HashMap();
         props.put("jndi-name", "TestNoTxDataSource");
         props.put("connection-url", "jdbc:hsqldb:hsql://" + getServerHost() + ":1701");
         props.put("driver-class", "org.hsqldb.jdbcDriver");
         
         // Add some fake connection properties
         Hashtable ht = new Hashtable();
         ht.put("property1", "someString");
         ht.put("property2", new Boolean(true));
         ht.put("property3", new Integer(666));
         props.put("connection-properties", ht);
         
         props.put("user-name", "sa");
         props.put("password", "");
         
         props.put("min-pool-size", new Integer(5));
         props.put("max-pool-size", new Integer(20));
         props.put("track-statements", "NOWARN");
         props.put("security-config", "APPLICATION-MANAGED-SECURITY");
         props.put("type-mapping", "Hypersonic SQL");
         props.put("dependencies", new ObjectName[] { new ObjectName("jboss:service=Hypersonic") });
         
         String template = "no-tx-datasource";
         
         // In case of any problem an exception will be thrown
         module = createModule(module, template, props);
      }
      catch (Exception e)
      {
         super.fail("Caught exception, message: " + e.getMessage());
      }
      finally
      {
         // empty
      }
   }
   
   /**
    * Try to create an xa-datasource (don't deploy)
    */
   public void testCreateXaDataSource() throws Exception
   {
      log.info("+++ testCreateXaDataSource");
      try
      {
         String module = "test-xa-oracle-ds.xml"; 

         // remove module in case it exists
         removeModule(module);
         
         // Prepare the template properties
         HashMap props = new HashMap();
         props.put("jndi-name", "TestOracleXaDataSource");
         props.put("track-connection-by-tx", new Boolean(true));
         props.put("is-same-RM-override-value", new Boolean(false));
         props.put("xa-datasource-class", "oracle.jdbc.xa.client.OracleXADataSource");
         
         // Add some xa-datasource-properties
         Hashtable ht = new Hashtable();
         ht.put("URL", "jdbc:oracle:oci8:@tc");
         ht.put("User", "scott");
         ht.put("Password", "tiger");         
         props.put("xa-datasource-properties", ht);
         
         props.put("exception-sorter-class-name", "org.jboss.resource.adapter.jdbc.vendor.OracleExceptionSorter");
         props.put("no-tx-separate-pools", new Boolean(true));
         props.put("type-mapping", "Oracle9i");
         
         String template = "xa-datasource";
         
         // In case of any problem an exception will be thrown
         module = createModule(module, template, props);
      }
      catch (Exception e)
      {
         super.fail("Caught exception, message: " + e.getMessage());
      }
      finally
      {
         // empty
      }
   }
   
   /**
    * Try to create and deploy a local-tx-datasource
    */
   public void testCreateAndDeployLocalTxDataSource() throws Exception
   {
      log.info("+++ testCreateAndDeployLocalTxDataSource");
      try
      {
         String module = "test-local-tx-hsqldb-ds.xml"; 
         String jndiName = "TestLocalTxHsqlDataSource";
         
         // undeploye module in case it's deployed
         undeployModule(module);
         // remove module in case it exists
         removeModule(module);
         
         // Prepare the template properties
         HashMap props = new HashMap();
         props.put("jndi-name", jndiName);   // use a name other than default
         props.put("use-java-context", new Boolean(false)); // set this to false to allow remote lookup
         props.put("connection-url", "jdbc:hsqldb:hsql://" + getServerHost() + ":1701"); // using hsqldb
         props.put("driver-class", "org.hsqldb.jdbcDriver");
         props.put("user-name", "sa");
         props.put("password", "");
         props.put("min-pool-size", new Integer(5));
         props.put("max-pool-size", new Integer(20));
         props.put("idle-timeout-minutes", new Integer(0));
         props.put("track-statements", "TRUE");
         props.put("security-config", "APPLICATION-MANAGED-SECURITY");
         props.put("type-mapping", "Hypersonic SQL");
         props.put("dependencies", new ObjectName[] { new ObjectName("jboss:service=Hypersonic") });
         
         String template = "local-tx-datasource";
         
         // In case of any problem an exception will be thrown
         module = createModule(module, template, props);
         
         boolean isDeployed = deployModule(module);
         
         // was deployment succesful?
         assertTrue("deployed succesfull : " + isDeployed, isDeployed);
         
         // see if we can get the a connection
         InitialContext ic = new InitialContext();
         DataSource ds = (DataSource)ic.lookup(jndiName);
         Connection connection = ds.getConnection();
         connection.close();
         
         // undeploy module
         undeployModule(module);
         // remove module
         removeModule(module);
         
         // regenerate with wrong usename
         props.put("user-name", "rogue-admin");
         module = createModule(module, template, props);
         
         // deploy again
         isDeployed = deployModule(module);
         
         // was deployment succesful?
         assertTrue("deployed succesfull : " + isDeployed, isDeployed);
         
         // lookup the datasource again and see if we can get a connection
         // it should fail this time
         try
         {
            ds = (DataSource)ic.lookup(jndiName);
            connection = ds.getConnection();
            fail("Shouldn't reach this point");
         }
         catch (Exception e)
         {
            // ok
         }
         // undeploy module
         undeployModule(module);
      }
      finally
      {
         // empty
      }
   }
   
   private String createModule(String module, String template, HashMap props) throws Exception
   {
      MBeanServerConnection server = getServer();
      
      // create the module
      module = (String)server.invoke(
            deploymentService,
            "createModule",
            new Object[] { module, template, props },
            new String[] { "java.lang.String", "java.lang.String", "java.util.HashMap" });
      
      log.info("Module '" + module + "' created: " + module);
      return module;
   }
   
   private boolean removeModule(String module) throws Exception
   {
      MBeanServerConnection server = getServer();
      
      // remove the module, in case it exists
      Boolean removed = (Boolean)server.invoke(
            deploymentService,
            "removeModule",
            new Object[] { module },
            new String[] { "java.lang.String" });
      
      log.info("Module '" + module + "' removed: " + removed);
      
      return removed.booleanValue();
   }
   
   private boolean deployModule(String module) throws Exception
   {
      MBeanServerConnection server = getServer();
      
      // Deploy the module (move to ./deploy)
      server.invoke(
            deploymentService,
            "deployModuleAsynch",
            new Object[] { module },
            new String[] { "java.lang.String" });
      
      // Get the deployed URL
      URL deployedURL = (URL)server.invoke(
            deploymentService,
            "getDeployedURL",
            new Object[] { module },
            new String[] { "java.lang.String" });
      
      // Ask the MainDeployer every 3 secs, 5 times (15secs max wait) if the module was deployed
      Boolean isDeployed = new Boolean(false);
      for (int tries = 0; tries < 5; tries++)
      {
         // sleep for 3 secs
         Thread.sleep(3000);
         isDeployed = (Boolean)server.invoke(
               mainDeployer,
               "isDeployed",
               new Object[] { deployedURL },
               new String[] { "java.net.URL" });
         
         if (isDeployed.booleanValue())
         {
            break;
         }
      }
      log.info("Module '" + module + "' deployed: " + isDeployed);
      return isDeployed.booleanValue();
   }
   
   private boolean undeployModule(String module) throws Exception
   {
      MBeanServerConnection server = getServer();
      try
      {
         // Get the deployed URL
         URL deployedURL = (URL)server.invoke(
               deploymentService,
               "getDeployedURL",
               new Object[] { module },
               new String[] { "java.lang.String" });
         
         // Undeploy the module (move to ./undeploy)
         server.invoke(
               deploymentService,
               "undeployModuleAsynch",
               new Object[] { module },
               new String[] { "java.lang.String" });
         
         // Ask the MainDeployer every 3 secs, 5 times (15secs max wait) if the module was undeployed
         Boolean isDeployed = new Boolean(false);
         for (int tries = 0; tries < 5; tries++)
         {
            // sleep for 3 secs
            Thread.sleep(3000);
            isDeployed = (Boolean)server.invoke(
                  mainDeployer,
                  "isDeployed",
                  new Object[] { deployedURL },
                  new String[] { "java.net.URL" });
            
            if (!isDeployed.booleanValue())
            {
               break;
            }
         }
         log.info("Module '" + module + "' deployed: " + isDeployed);
         return isDeployed.booleanValue();
      }
      catch (Exception e)
      {
         // the module does not exist
         log.info("Ignoring caught exception, message: " + e.getMessage());
         return false;
      }
   }
}
