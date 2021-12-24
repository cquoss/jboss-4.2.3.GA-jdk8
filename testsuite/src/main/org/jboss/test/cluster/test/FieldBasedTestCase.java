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

import javax.management.MBeanServerConnection;

import junit.framework.Test;

import org.apache.commons.httpclient.HttpClient;
import org.jboss.test.JBossClusteredTestCase;

/**
 * Clustering test case of get/set under scoped class loader.
 * Replication granularity is attribute-based.
 *
 * @author Ben Wang
 * @author Brian Stansberry
 * @version $Id: FieldBasedTestCase.java 65056 2007-09-04 18:22:33Z bstansberry@jboss.com $
 */
public class FieldBasedTestCase
      extends ScopedTestCase
{
   
   protected String modifySubjectUrl;
   protected String modifySubjectUrlBase_;
   protected String modifyCollectionUrl;
   protected String modifyCollectionUrlBase_;
   
   public FieldBasedTestCase(String name)
   {
      super(name);
      warName_ = "/http-field/";
      modifySubjectUrlBase_ = "modifySubject.jsp";
      modifyCollectionUrlBase_ = "modifyCollection.jsp";
      concatenate();
   }

   public static Test suite() throws Exception
   {
      Test t1 = JBossClusteredTestCase.getDeploySetup(FieldBasedTestCase.class,
            "http-field.war");
      return t1;
   }

   protected void concatenate()
   {
      super.concatenate();
      modifySubjectUrl = warName_ + modifySubjectUrlBase_;
      modifyCollectionUrl = warName_ + modifyCollectionUrlBase_;
}
   public void testSubjectObserver() throws Exception
   {
      getLog().debug("Enter testSubjectObserver");

      getLog().debug(setUrl + ":::::::" + modifySubjectUrl);

      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();

      // Set the session attribute first
      makeGet(client, baseURL0_ +setUrl);
      
      // Find out the session id and use it to build an FQN
      String sessionID = getSessionID(client, servers_[0]);
      // Strip off the jvmRoute, if there is one
      sessionID = stripJvmRoute(sessionID);
      String sessionFqn = "/JSESSION/localhost" + warName_ + sessionID;
      
      MBeanServerConnection[] adaptors = getAdaptors();
      
      // Get the session version from the cache
      Object origVersion = SessionTestUtil.getSessionVersion(adaptors[0], sessionFqn); 
      
      // Modify the POJO stored in the session, but don't touch the
      // session attributes (a reference to the POJO is stored in the 
      // servlet context)
      makeGet(client, baseURL0_ +modifySubjectUrl);
      
      // Get the session from the cache
      Object newVersion = SessionTestUtil.getSessionVersion(adaptors[0], sessionFqn);
      
      // The byte[] should have been updated because the POJO was
      assertFalse("Session body has been updated --" + origVersion + " vs " + newVersion, 
                  origVersion.equals(newVersion));
      
      this.sleepThread(DEFAULT_SLEEP);
      
      // Get the session from the server1 cache
      Object replVersion = SessionTestUtil.getSessionVersion(adaptors[1], sessionFqn);
      
      if (replVersion == null)
      {
         // Since we haven't accessed the session on server 1,
         // see if it is in a buddy subtree
         replVersion = SessionTestUtil.getBuddySessionVersion(adaptors[1], sessionFqn);
      }
      
      // Should match the one on server0
      assertEquals("Session body was replicated", 
                   newVersion, replVersion);
   }
   
   public void testObserverRemoval() throws Exception
   {
      getLog().debug("Enter testObserverRemoval");

      getLog().debug(setUrl + ":::::::" + modifySubjectUrl);

      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();

      // Set the session attribute first
      makeGet(client, baseURL0_ +setUrl);
      
      // Find out the session id and use it to build an FQN
      String sessionID = getSessionID(client, servers_[0]);
      // Strip off the jvmRoute, if there is one
      sessionID = stripJvmRoute(sessionID);
      String sessionFqn = "/JSESSION/localhost" + warName_ + sessionID;
      
      this.sleepThread(DEFAULT_SLEEP);
      
      // Switch over to server1 and get the POJO, which will also
      // cause it to be stored in the servlet context on that server
      setCookieDomainToThisServer(client, servers_[1]);
      makeGet(client, baseURL1_ + getUrl);
      
      this.sleepThread(DEFAULT_SLEEP);
      
      // Now switch back to server0 and remove the POJO from the session
      setCookieDomainToThisServer(client, servers_[0]);
      makeGet(client, baseURL0_ + removeUrl);
      
      MBeanServerConnection[] adaptors = getAdaptors();
      
      // Get the session from the cache
      Object origVersion = SessionTestUtil.getSessionVersion(adaptors[0], sessionFqn); 
      
      // Modify the POJO that was originally stored in the session
      // (a reference to the POJO is stored in the servlet context)
      // Use a new client with no session cookie so we don't touch the session
      HttpClient client2 = new HttpClient();
      makeGet(client2, baseURL0_ +modifySubjectUrl);
      
      // Get the session from the cache
      Object newVersion = SessionTestUtil.getSessionVersion(adaptors[0], sessionFqn); 
      
      // The version should not have been updated
      assertEquals("Session version has not been updated", 
                   origVersion, newVersion);
      
      this.sleepThread(DEFAULT_SLEEP);
      
      // Get the session from the server1 cache
      Object replVersion = SessionTestUtil.getSessionVersion(adaptors[1], sessionFqn); 
      
      if (replVersion == null)
      {
         // Since we haven't accessed the session on server 1,
         // see if it is in a buddy subtree
         replVersion = SessionTestUtil.getBuddySessionVersion(adaptors[1], sessionFqn);
      }
      
      // Should match the one on server0
      assertEquals("Session body was replicated", 
                   newVersion, replVersion);
      
      // Make connection to server 1 and again modify the POJO via servlet ctx
      // Again use a fresh client so we don't touch the session
      HttpClient client3 = new HttpClient();
      makeGet(client3, baseURL1_ + modifySubjectUrl);
      
      // Again get the session from the server1 cache
      Object newReplVersion = SessionTestUtil.getSessionVersion(adaptors[1], sessionFqn); 
      
      if (newReplVersion == null)
      {
         // Since we haven't accessed the session on server 1,
         // see if it is in a buddy subtree
         newReplVersion = SessionTestUtil.getBuddySessionVersion(adaptors[1], sessionFqn);
      }
      
      // The byte[] should not have been updated
      assertEquals("Session body has not been updated on server1", 
                   replVersion, newReplVersion);
      
   }
   
   public void testCollectionModification() throws Exception
   {
      getLog().debug("Enter testCollectionModification");

      getLog().debug(setUrl + ":::::::" + modifyCollectionUrl);

      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();

      // Set the session attribute first
      makeGet(client, baseURL0_ +setUrl);
      
      // Find out the session id and use it to build an FQN
      String sessionID = getSessionID(client, servers_[0]);
      // Strip off the jvmRoute, if there is one
      sessionID = stripJvmRoute(sessionID);
      String sessionFqn = "/JSESSION/localhost" + warName_ + sessionID;
      
      // Modify the data, as that's the JSP that adds the language collection
      makeGet(client, baseURL0_ + modifyUrl);
      
      MBeanServerConnection[] adaptors = getAdaptors();
      
      // Get the session version from the cache
      Object origVersion = SessionTestUtil.getSessionVersion(adaptors[0], sessionFqn); 
      
      // Modify the languages collection
      makeGet(client, baseURL0_ +modifyCollectionUrl);
      
      // Get the session from the cache
      Object newVersion = SessionTestUtil.getSessionVersion(adaptors[0], sessionFqn);
      
      // The version should have been updated because the data was
      assertFalse("Session body has been updated", 
                  origVersion.equals(newVersion));
      
      this.sleepThread(DEFAULT_SLEEP);
      
      // Get the session from the server1 cache
      Object replVersion = SessionTestUtil.getSessionVersion(adaptors[1], sessionFqn);
      
      if (replVersion == null)
      {
         // Since we haven't accessed the session on server 1,
         // see if it is in a buddy subtree
         replVersion = SessionTestUtil.getBuddySessionVersion(adaptors[1], sessionFqn);
      }
      
      // Should match the one on server0
      assertTrue("Session body was replicated", 
                 newVersion.equals(replVersion));
      
   }

}
