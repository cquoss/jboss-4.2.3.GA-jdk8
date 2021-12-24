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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import junit.framework.Test;

import org.apache.commons.httpclient.HttpClient;
import org.jboss.cache.Fqn;
import org.jboss.test.JBossClusteredTestCase;

/**
 * Tests the use of the TreeCache.activateRegion()/inactivateRegion().
 * 
 * TODO add a concurrency test.
 * 
 * @author <a href="mailto://brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision$
 */
public class StateTransferTestCase extends BaseTest
{

   private static final Fqn BUDDY_BACKUP_SUBTREE_FQN = Fqn.fromString("_BUDDY_BACKUP_");

   private static final Object[] NULL_ARGS = new Object[] {};
   private static final String[] NULL_TYPES = new String[] {};
   
   private static final ObjectName CACHE_OBJECT_NAME;
   static
   {
      try
      {
         CACHE_OBJECT_NAME =
            new ObjectName("jboss.cache:service=TomcatClusteringCache");
      }
      catch (MalformedObjectNameException e)
      {
         throw new ExceptionInInitializerError(e);
      }
   }
   
   protected String setUrl_;
   protected String getUrl_;
   protected String setUrlBase_;
   protected String getUrlBase_;
   
   private ObjectName warObjectName;
   private MBeanServerConnection adaptor0_;
   private MBeanServerConnection adaptor1_;
   private String warFqn_;
   
   /**
    * Create a new StateTransferTestCase.
    * 
    * @param name
    */
   public StateTransferTestCase(String name)
   {
      super(name);
      setUrlBase_ = "setSession.jsp";
      getUrlBase_ = "getAttribute.jsp";
      concatenate();
   }

   protected void concatenate()
   {
      String contextPath = "/" + getWarName() + "/";
      setUrl_ = contextPath +setUrlBase_;
      getUrl_ = contextPath +getUrlBase_;
   }

   public static Test suite() throws Exception
   {
      Test t1 = JBossClusteredTestCase.getDeploySetup(StateTransferTestCase.class,
            "http-scoped.war");
      return t1;
   }
   
   protected void setUp() throws Exception
   {
      super.setUp();
      
      if (warObjectName == null)
      {
         String oname = "jboss.web:J2EEApplication=none,J2EEServer=none," +
                        "j2eeType=WebModule,name=//localhost/" + getWarName();
         warObjectName = new ObjectName(oname);
         
         MBeanServerConnection[] adaptors = getAdaptors();
         adaptor0_ = adaptors[0];
         adaptor1_ = adaptors[1];
         
         Object[] names = {"JSESSION", "localhost", getWarName() };
         Fqn fqn = new Fqn(names);
         warFqn_ = fqn.toString();
      }
   }
   
   protected String getWarName()
   {
      return "http-scoped";
   }
   
   public void testActivationInactivation() throws Exception
   {
      getLog().debug("Enter testActivationInactivation");

      getLog().debug(setUrl_ + ":::::::" + getUrl_);
      
      // Stop the war on server1
      adaptor1_.invoke(warObjectName, "stop" , NULL_ARGS, NULL_TYPES);

      // Confirm the war isn't available on server1
      HttpClient client0 = new HttpClient();
      makeGetFailed(client0, baseURL1_ +setUrl_);
      
      // Create 3 sessions on server0
      HttpClient[] clients = new HttpClient[3];
      String[] attrs = new String[clients.length];
      for (int i = 0; i < clients.length; i++)
      {
         clients[i] = new HttpClient();
         makeGet(clients[i], baseURL0_ +setUrl_);
         attrs[i] = makeGet(clients[i], baseURL0_ + getUrl_);
         // Set cookie domain to server1
         this.setCookieDomainToThisServer(clients[i], servers_[1]);
      }
      
      getLog().debug("Sessions created");
      
      // Confirm there are no sessions in the server1 cache
      Set sessions = getSessionIds(adaptor1_);
      
      assertTrue("server1 has no cached sessions", sessions.size() == 0);
      
      getLog().debug("Server1 has no cached sessions");
      
      // Start the war on server1
      adaptor1_.invoke(warObjectName, "start" , NULL_ARGS, NULL_TYPES);
      
      getLog().debug("Server1 started");
      
      // Confirm the sessions are in the server1 cache
      sessions = getSessionIds(adaptor1_);

      assertEquals("server1 has cached sessions", clients.length, sessions.size());
      
      getLog().debug("Server1 has cached sessions");
      
      for (int i = 0; i < clients.length; i++)
      {
         String attr = makeGet(clients[i], baseURL1_ + getUrl_);
         assertEquals("attribute matches for client " + i, attrs[i], attr);
      }
      
      getLog().debug("Attributes match");
      
      // Sleep a bit in case the above get triggers replication that takes
      // a while -- don't want a repl to arrive after the cache is cleared
      sleep(500);
      
      // Stop the war on server0
      adaptor0_.invoke(warObjectName, "stop" , NULL_ARGS, NULL_TYPES);
      
      // Confirm there are no sessions in the server0 cache
      sessions = getSessionIds(adaptor0_);
      
      assertTrue("server0 has no cached sessions", sessions.size() == 0);
      
      getLog().debug("Server0 has no cached sessions");
   }
   
   private Set getSessionIds(MBeanServerConnection adaptor) throws Exception
   {
      Set result = new HashSet();
      Set s = (Set) adaptor.invoke(CACHE_OBJECT_NAME, 
                                        "getChildrenNames",
                                        new Object[] { warFqn_ },
                                        new String[] { String.class.getName() });      
      if (s != null)
         result.addAll(s);
      
//    Check in the buddy backup tree
      Set buddies = (Set) adaptor.invoke(CACHE_OBJECT_NAME, 
            "getChildrenNames",
            new Object[] { BUDDY_BACKUP_SUBTREE_FQN },
            new String[] { Fqn.class.getName() });
      
      if (buddies != null)
      {
         for (Iterator it = buddies.iterator(); it.hasNext(); )
         {
            Fqn fqn = new Fqn(BUDDY_BACKUP_SUBTREE_FQN, it.next());
            fqn = new Fqn(fqn, Fqn.fromString(warFqn_));
            s = (Set) adaptor.invoke(CACHE_OBJECT_NAME, 
                  "getChildrenNames",
                  new Object[] { fqn.toString() },
                  new String[] { String.class.getName() });
            if (s != null)
               result.addAll(s);
         }
      }
      
      return result;
   }

}
