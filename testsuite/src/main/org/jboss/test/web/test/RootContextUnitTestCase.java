/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.test.web.test;

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.util.web.HttpUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * This class tests a root context deployed as an EAR or
 * a WAR.
 * 
 * @author Stan.Silvert@jboss.org
 */
public class RootContextUnitTestCase extends JBossTestCase
{
   private String baseURL = HttpUtils.getBaseURL(); 
   private HttpClient client = new HttpClient();
   
   public RootContextUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testRootContextWAR() throws Exception
   {
      String response = hitRootContext("root-context.war");
      assertTrue(response.contains("A Root Context Page"));
   }

   public void testRootContextEAR() throws Exception
   {
      String response = hitRootContext("jbosstest-web.ear");
      assertTrue(response.contains("A Root Context Page"));
   }

   /**
    *  Access http://localhost/
    */
   private String hitRootContext(String deploymentUnit) throws Exception
   {
      deploy(deploymentUnit);

      try {
         HttpMethodBase request = new GetMethod(baseURL);
         client.executeMethod(request);

         String responseBody = request.getResponseBodyAsString();
         if (responseBody == null) {
            throw new Exception("Unable to get response from server.");
         }

         return responseBody;
      } finally {
         undeploy(deploymentUnit);
      }
   }   

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(RootContextUnitTestCase.class));
      return suite;
   }
   

}
