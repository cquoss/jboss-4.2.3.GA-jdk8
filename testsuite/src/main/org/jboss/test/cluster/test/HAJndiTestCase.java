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
package org.jboss.test.cluster.test;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.Test;

import org.jboss.test.cluster.ejb.CalledHome;
import org.jboss.test.cluster.ejb.CalledRemote;
import org.jboss.test.JBossClusteredTestCase;

/**
 * HA-JNDI clustering tests.
 *
 * @author Jerry Gauthier
 * @version $Revision: 61204 $
 */
public class HAJndiTestCase
      extends JBossClusteredTestCase      
{
   // ENVIRONMENT PROPERTIES
   private static final String NODE0 = System.getProperty("node0");
   private static final String NODE0_JNDI = System.getProperty("node0.jndi.url");
   private static final String NODE1_JNDI = System.getProperty("node1.jndi.url");
   private static final String NODE0_HAJNDI = System.getProperty("node0.hajndi.url");
   private static final String NODE1_HAJNDI = System.getProperty("node1.hajndi.url");
   private static final String DISCOVERY_GROUP = System.getProperty("jbosstest.udpGroup");
   private static final String DISCOVERY_TTL = System.getProperty("jbosstest.udp.ip_ttl", "1");
   
   // BINDING KEYS and VALUES
   private static final String LOCAL0_KEY = "org.jboss.test.cluster.test.Local0Key";
   private static final String LOCAL0_VALUE = "Local0Value";
   private static final String LOCAL1_KEY = "org.jboss.test.cluster.test.Local1Key";
   private static final String LOCAL1_VALUE = "Local1Value";
   private static final String GLOBAL0_KEY = "org.jboss.test.cluster.test.Global0Key";
   private static final String GLOBAL0_VALUE = "Global0Value";
   private static final String CALLED_HOME_KEY = "cluster.ejb.CalledHome";
   private static final String JNDI_KEY = "org.jboss.test.cluster.test.JNDIKey";
   private static final String JNDI_KEY3 = "org.jboss.test.cluster.test.JNDIKey3";
   private static final String JNDI_VALUE1 = "JNDIValue1";
   private static final String JNDI_VALUE2 = "JNDIValue2";
   private static final String JNDI_VALUE3 = "JNDIValue3";
	
   public HAJndiTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(HAJndiTestCase.class, "cross-server.jar");
   }

   /**
    * Test local bindings using local and HA-JNDI lookups
    *
    * @throws Exception
    */
   public void testLocalBinding()
      throws Exception
   {
      getLog().debug("HAJndiTestCase.testLocalBinding()");
      validateUrls();
      
      // bind to node0 locally
      Context naming = getContext(NODE0_JNDI);
      naming.bind(LOCAL0_KEY, LOCAL0_VALUE);
      closeContext(naming);
      
      // lookup binding locally on Node0 - should succeed
      naming = getContext(NODE0_JNDI);
      String value = (String)lookup(naming, LOCAL0_KEY, true);
      closeContext(naming);
      assertEquals("lookup local binding on same server", LOCAL0_VALUE, value);
      
      // lookup binding locally on Node1 - should fail
      naming = getContext(NODE1_JNDI);
      value = (String)lookup(naming, LOCAL0_KEY, false);
      closeContext(naming);
      assertNull("lookup local binding on different server in cluster", value);
      
      // lookup binding using HA-JNDI on Node0 - should succeed
      naming = getContext(NODE0_HAJNDI);
      value = (String)lookup(naming, LOCAL0_KEY, true);
      closeContext(naming);
      assertEquals("lookup local binding on same server using HA-JNDI", LOCAL0_VALUE, value);
      
      // lookup binding using HA-JNDI on Node1 - should succeed
      naming = getContext(NODE1_HAJNDI);
      value = (String)lookup(naming, LOCAL0_KEY, true);
      closeContext(naming);
      assertEquals("lookup local binding on different server in cluster using HA-JNDI", LOCAL0_VALUE, value);
   }
   
   /**
    * Test HA-JNDI bindings using local and HA-JNDI lookups
    *
    * @throws Exception
    */
   public void testHAJndiBinding()
      throws Exception
   {
      getLog().debug("HAJndiTestCase.testHAJndiBinding()");
      validateUrls();
      
      // bind to node0 using HA-JNDI
      Context naming = getContext(NODE0_HAJNDI);
      naming.bind(GLOBAL0_KEY, GLOBAL0_VALUE);
      closeContext(naming);
      
      // lookup binding locally on Node0 - should fail
      naming = getContext(NODE0_JNDI);
      String value = (String)lookup(naming, GLOBAL0_KEY, false);
      closeContext(naming);
      assertNull("lookup HA-JNDI binding on same server using local JNDI", value);
      
      // lookup binding locally on Node1 - should fail
      naming = getContext(NODE1_JNDI);
      value = (String)lookup(naming, GLOBAL0_KEY, false);
      closeContext(naming);
      assertNull("lookup HA-JNDI binding on different server in cluster using local JNDI", value);
      
      // lookup binding using HA-JNDI on Node0 - should succeed
      naming = getContext(NODE0_HAJNDI);
      value = (String)lookup(naming, GLOBAL0_KEY, true);
      closeContext(naming);
      assertEquals("lookup HA-JNDI binding on same server using HA-JNDI", GLOBAL0_VALUE, value);
      
      // lookup binding using HA-JNDI on Node1 - should succeed
      naming = getContext(NODE1_HAJNDI);
      value = (String)lookup(naming, GLOBAL0_KEY, true);
      closeContext(naming);
      assertEquals("lookup HA-JNDI binding on different server in cluster using HA-JNDI", GLOBAL0_VALUE, value);
 
   }
   
   /**
    * Test HA-JNDI operations
    *
    * @throws Exception
    */
   public void testHAJndiOperations()
      throws Exception
   {
      getLog().debug("HAJndiTestCase.testHAJndiOperations()");
      validateUrls();
      
      // bind to node0 using HA-JNDI
      Context naming = getContext(NODE0_HAJNDI);
      naming.bind(JNDI_KEY, JNDI_VALUE1);     
     
      // lookup binding using HA-JNDI on Node0 - should succeed
      String value = (String)lookup(naming, JNDI_KEY, true);
      assertEquals("lookup after initial HA-JNDI binding operation", JNDI_VALUE1, value);
      
      // bind it again - this should fail with NameAlreadyBoundException      
      try
      {
         naming.bind(JNDI_KEY, JNDI_VALUE1);
         fail("binding key a second time in HA-JNDI should throw NamingException");
      }
      catch (NamingException ne)
      {
         assertTrue("binding key a second time in HA-JNDI should throw NamingException", ne instanceof NamingException);
      }
      
      // rebind it using a different value
      naming.rebind(JNDI_KEY, JNDI_VALUE2);  
      
      // lookup binding - should return new value
      value = (String)lookup(naming, JNDI_KEY, true);
      assertEquals("lookup after HA-JNDI rebind operation", JNDI_VALUE2, value);
      
      // unbind it
      naming.unbind(JNDI_KEY);  
      
      // lookup binding - should fail with NamingException
      value = (String)lookup(naming, JNDI_KEY, false);
      assertNull("lookup after HA-JNDI unbind operation", value);
      
      closeContext(naming);  
   }

   /**
    * Test EJB Bindings using local and HA-JNDI lookups
    *
    * @throws Exception
    */
   public void testEJBBinding()
      throws Exception
   {
      getLog().debug("HAJndiTestCase.testEJBBinding()");
      validateUrls();
      
      // CalledHome EJB is contained in cross-server.jar and bound during deployment
 
      // lookup binding locally on Node0 - should succeed
      Context naming = getContext(NODE0_JNDI);
      CalledHome home = (CalledHome)lookup(naming, CALLED_HOME_KEY, true);
      if (home != null)
      { // ensure that EJB is operational  
         CalledRemote remote = home.create();
         remote.remove();
      }
      closeContext(naming);
      assertNotNull("lookup EJB binding on same server using local JNDI", home);
      
      // lookup binding locally on Node1 - should succeed
      naming = getContext(NODE1_JNDI);
      home = (CalledHome)lookup(naming, CALLED_HOME_KEY, true);
      if (home != null)
      {   // ensure that EJB is operational 
         CalledRemote remote = home.create();
         remote.remove();
      }
      closeContext(naming);
      assertNotNull("lookup EJB binding on different server in cluster using local JNDI", home);
      
      // lookup binding using HA-JNDI on Node0 - should succeed
      naming = getContext(NODE0_HAJNDI);
      home = (CalledHome)lookup(naming, CALLED_HOME_KEY, true);
      if (home != null)
      {   // ensure that EJB is operational 
         CalledRemote remote = home.create();
         remote.remove();
      }
      closeContext(naming);
      assertNotNull("lookup EJB binding on same server using HA-JNDI", home);
      
      // lookup binding using HA-JNDI on Node1 - should succeed
      naming = getContext(NODE1_HAJNDI);
      home = (CalledHome)lookup(naming, CALLED_HOME_KEY, true);
      if (home != null)
      {   // ensure that EJB is operational 
         CalledRemote remote = home.create();
         remote.remove();
      }
      closeContext(naming);
      assertNotNull("lookup EJB binding on different server in cluster using HA-JNDI", home);

   }
   
   /**
    * Test HA-JNDI AutoDiscovery
    *
    * @throws Exception
    */
   public void testAutoDiscovery()
      throws Exception
   {
      getLog().debug("HAJndiTestCase.testAutoDiscovery()");
      validateUrls();
      
      // this test doesn't run properly if node0=localhost
      if (NODE0 != null && NODE0.equalsIgnoreCase("localhost") )
      {
         getLog().debug("testAutoDiscovery() - test skipped because node0=localhost");
         return;
      }
      
      // bind to node1 locally
      Context naming = getContext(NODE1_JNDI);
      naming.bind(LOCAL1_KEY, LOCAL1_VALUE);
      closeContext(naming);
      
      // bind to node0 using HA-JNDI
      naming = getContext(NODE0_HAJNDI);
      naming.bind(JNDI_KEY3, JNDI_VALUE3);  
      closeContext(naming);
     
      //create context with AutoDiscovery enabled
      naming = getAutoDiscoveryContext(false);
      
      // lookup local binding using HA-JNDI AutoDiscovery - should succeed
      String value = (String)lookup(naming, LOCAL1_KEY, true);
      assertEquals("local lookup with AutoDiscovery enabled", LOCAL1_VALUE, value);
      
      // lookup HA binding using HA-JNDI AutoDiscovery - should succeed
      value = (String)lookup(naming, JNDI_KEY3, true);
      assertEquals("lookup of HA-JNDI binding with AutoDiscovery enabled", JNDI_VALUE3, value);     
      
      // now disable AutoDiscovery and confirm that the same lookups fail
      closeContext(naming);
      naming = getAutoDiscoveryContext(true);
      
      // lookup local binding without HA-JNDI AutoDiscovery - should fail
      value = (String)lookup(naming, LOCAL1_KEY, false);
      assertNull("local lookup with AutoDiscovery disabled", value);
      
      // lookup HA binding without HA-JNDI AutoDiscovery - should fail
      value = (String)lookup(naming, JNDI_KEY3, false);
      assertNull("lookup of HA-JNDI binding with AutoDiscovery disabled", value);
      
      closeContext(naming);

   }
  
   private Context getContext(String url)
      throws Exception
   {
      Properties env = new Properties();        
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
      env.setProperty(Context.PROVIDER_URL, url);
      env.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
		
      Context naming = new InitialContext (env);
      return naming;

   }
   
   private Context getAutoDiscoveryContext(boolean autoDisabled)
      throws Exception
   {
      // do not add any urls to the context
      Properties env = new Properties();        
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
      env.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
      // Don't let the discovery packet off the test server so we don't
      // get spurious responses from other servers on the network
      env.setProperty("jnp.discoveryTTL", DISCOVERY_TTL);

      if (autoDisabled)
      {
         env.put("jnp.disableDiscovery", "true");
      }
      else if (DISCOVERY_GROUP != null && "".equals(DISCOVERY_GROUP) == false)
      {
         // USe the multicast address this test environment is using
         env.put("jnp.discoveryGroup", DISCOVERY_GROUP);
      }
        
      Context naming = new InitialContext (env);
      return naming;

   }
	
   private void closeContext(Context context)
   {
      try 
      {
         context.close();		   
      }
      catch (NamingException e)
      {
         // no action required
      }
   }	
   
   private Object lookup(Context context, String name, boolean failIfMissing)
   {	   
      try
      {
         Object o = context.lookup(name);
         log.info(name + " binding value: " + o);
         return o;
      }
      catch (NamingException e)
      {
         if (failIfMissing)
         {
           String msg =   "Name " + name + " not found. " + e.getLocalizedMessage();
           log.info(msg, e);
           fail(msg);
         }
         else
         {
            log.debug("Name " + name + " not found.");
         }
         return null;
      }	   
   }
   
   private void validateUrls()
      throws Exception      
   {
      if (NODE0_JNDI == null)
         throw new Exception("node0.jndi.url not defined.");
         
      if (NODE1_JNDI == null)
         throw new Exception("node1.jndi.url not defined.");
         
      if (NODE0_HAJNDI == null)
         throw new Exception("node0.hajndi.url not defined.");
         
      if (NODE1_HAJNDI == null)
         throw new Exception("node1.hajndi.url not defined.");

   }

}
