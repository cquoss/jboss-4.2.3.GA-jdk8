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

import junit.framework.Test;

import org.apache.commons.httpclient.HttpClient;
import org.jboss.test.JBossClusteredTestCase;

/**
 * Simple clustering test case of get/set.
 *
 * @author Ben Wang
 * @version $Revision: 1.0
 */
public class SimpleTestCase
      extends BaseTest
{

   public SimpleTestCase(String name)
   {
      super(name);

   }

   public static Test suite() throws Exception
   {
      Test t1 = JBossClusteredTestCase.getDeploySetup(SimpleTestCase.class,
            "http-sr.war");
      return t1;
   }

   /**
    * Main method that deals with the Http Session Replication Test
    *
    * @throws Exception
    */
   public void testHttpSessionReplication()
         throws Exception
   {
      String attr = "";
      getLog().debug("Enter testHttpSessionReplication");

      String setURLName = "/http-sr/testsessionreplication.jsp";
      String getURLName = "/http-sr/getattribute.jsp";

      getLog().debug(setURLName + ":::::::" + getURLName);

      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();

      // Set the session attribute first
      makeGet(client, baseURL0_ +setURLName);

      // Get the Attribute set by testsessionreplication.jsp
      attr = makeGetWithState(client, baseURL0_ +getURLName);

      sleepThread(DEFAULT_SLEEP);

      // Let's switch to server 2 to retrieve the session attribute.
      setCookieDomainToThisServer(client, servers_[1]);
      String attr2 = makeGet(client, baseURL1_ +getURLName);

      // Check the result
      assertEquals("Http session replication attribtues retrieved from both servers ", attr, attr2);

      getLog().debug("Http Session Replication has happened");
      getLog().debug("Exit testHttpSessionReplication");
   }

   /**
    * Tests that sessions time out properly and that activity
    * on one cluster node prevents timeout on another.
    */
   public void testSessionTimeout()
      throws Exception
   {
      String attr  = "";
      String attr2 = "";
      getLog().debug("Enter testSessionTimeout");

      String setURLName = "/http-sr/testsessionreplication.jsp";
      String getURLName = "/http-sr/getattribute.jsp";

      getLog().debug(setURLName + ":::::::" + getURLName);

      // Create a session on server0
      HttpClient client = new HttpClient();
      makeGet(client, baseURL0_ +setURLName);

      sleepThread(6000);
      
      // Set the server0 session to ensure replication occurs
      attr = makeGetWithState(client, baseURL0_ +setURLName);
      // Get the Attribute set by testsessionreplication.jsp
      attr = makeGetWithState(client, baseURL0_ +getURLName);
      assertNotNull("Http session get", attr);
      
      // Sleep 15 secs.  This plus the previous 6 secs is enough to expire
      // session0 on server1 if replication failed to keep it alive
      sleepThread(15000);
      
      // Switch to the other server and check if 1st session is alive
      setCookieDomainToThisServer(client, servers_[1]);
      attr2 = makeGetWithState(client, baseURL1_ +getURLName);
      
      // Check the result
      assertEquals("Http session replication attributes retrieved from both servers ", attr, attr2);
      
      getLog().debug("Replication has kept the session alive");
      
      // sleep 6 more seconds so session0 will expire on server0
      sleepThread(6000);  
      
      // Confirm first session is expired on node 0
      setCookieDomainToThisServer(client, servers_[0]);
      attr = makeGetWithState(client, baseURL0_ +getURLName);
      assertFalse("Original session not present", attr2.equals(attr));
      
      getLog().debug("Exit testSessionTimeout");
   }
   
   /**
    * Tests that a request before maxUnreplicatedInterval has passed doesn't
    * trigger replication.
    */
   public void testMaxUnreplicatedInterval()
   {
      getLog().debug("Enter testMaxUnreplicatedInterval2");

      String setURLName = "/http-sr/testsessionreplication.jsp";
      String getURLName = "/http-sr/getattribute.jsp";
      String versionURLName = "/http-sr/version.jsp";

      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();

      // Set the session attribute first
      makeGet(client, baseURL0_ +setURLName);
      // Get the Attribute set by testsessionreplication.jsp
      String attr = makeGetWithState(client, baseURL0_ +getURLName);
      
      // Sleep a bit, but less than the 16 sec maxUnreplicatedInterval
      sleepThread(500);
      
      // Access the session without touching any attribute
      String ver = makeGetWithState(client, baseURL0_ +versionURLName);
      
      // Sleep some more, long enough for the session to replicate
      // if the last request incorrectly caused replication 
      sleepThread(2000);
      
      // Switch servers
      setCookieDomainToThisServer(client, servers_[1]);
      
      String ver1 = makeGetWithState(client, baseURL1_ +versionURLName);
      
      assertEquals("Session version count unchanged", ver, ver1);
      
      // Get the Attribute set by testsessionreplication.jsp
      String attr1 = makeGetWithState(client, baseURL1_ +getURLName);
      
      assertEquals("Session still present", attr, attr1);
   }
}
