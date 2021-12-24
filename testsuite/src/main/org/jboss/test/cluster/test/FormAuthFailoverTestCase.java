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

import java.net.HttpURLConnection;

import junit.framework.Test;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jboss.test.JBossClusteredTestCase;

/** Tests of web app single sign-on
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57211 $
 */
public class FormAuthFailoverTestCase 
      extends JBossClusteredTestCase
{
   public FormAuthFailoverTestCase(String name)
   {
      super(name);
   }

   /** 
    * Test ability for a FORM auth based app to have failover without requiring
    * a new sign-on
    * 
    * @throws Exception
    */ 
   public void testFormAuthFailover() throws Exception
   {
      log.info("+++ testFormAuthFailover");
      String[] httpURLs  = super.getHttpURLs();

      String serverA = httpURLs[0];
      String serverB = httpURLs[1];
      log.info(System.getProperties());
      log.info("serverA: "+serverA);
      log.info("serverB: "+serverB);
      
      // Start by accessing the secured index.html of war1
      HttpClient httpConn = new HttpClient();
      GetMethod indexGet = new GetMethod(serverA+"/war1/index.html");
      int responseCode = httpConn.executeMethod(indexGet);
      String body = indexGet.getResponseBodyAsString();
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);
      assertTrue("Redirected to login page", body.indexOf("j_security_check") > 0 );

      HttpState state = httpConn.getState();
      Cookie[] cookies = state.getCookies();
      String sessionID = null;
      for(int c = 0; c < cookies.length; c ++)
      {
         Cookie k = cookies[c];
         if( k.getName().equalsIgnoreCase("JSESSIONID") )
            sessionID = k.getValue();
      }
      log.debug("Saw JSESSIONID="+sessionID);

      // Submit the login form
      PostMethod formPost = new PostMethod(serverA+"/war1/j_security_check");
      formPost.addRequestHeader("Referer", serverA+"/war1/login.html");
      formPost.addParameter("j_username", "jduke");
      formPost.addParameter("j_password", "theduke");
      responseCode = httpConn.executeMethod(formPost.getHostConfiguration(),
         formPost, state);
      String response = formPost.getStatusText();
      log.debug("responseCode="+responseCode+", response="+response);
      assertTrue("Saw HTTP_MOVED_TEMP("+responseCode+")",
         responseCode == HttpURLConnection.HTTP_MOVED_TEMP);

      //  Follow the redirect to the index.html page
      Header location = formPost.getResponseHeader("Location");
      String indexURI = location.getValue();
      GetMethod war1Index = new GetMethod(indexURI);
      responseCode = httpConn.executeMethod(war1Index.getHostConfiguration(),
         war1Index, state);
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);
      body = war1Index.getResponseBodyAsString();
      if( body.indexOf("j_security_check") > 0 )
         fail("get of "+indexURI+" redirected to login page");

      cookies = state.getCookies();
      sessionID = null;
      for(int c = 0; c < cookies.length; c ++)
      {
         Cookie k = cookies[c];
         if( k.getName().equalsIgnoreCase("JSESSIONID") )
         {
            sessionID = k.getValue();
            if (serverA.equals(serverB) == false) 
            {
               // Make a session cookie to send to serverB
               Cookie copy = copyCookie(k, serverB);
               state.addCookie(copy);
               log.debug("Added state cookie: "+copy);
            }
         }
      }
      assertTrue("Saw JSESSIONID", sessionID != null);
      log.debug("Saw JSESSIONID="+sessionID);

      // Now try getting the war1 index on the second server 
      log.debug("Prepare /war1/index.html get");
      GetMethod war2Index = new GetMethod(serverB+"/war2/index.html");
      responseCode = httpConn.executeMethod(war2Index.getHostConfiguration(),
         war2Index, state);
      response = war2Index.getStatusText();
      log.debug("responseCode="+responseCode+", response="+response);
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);
      body = war2Index.getResponseBodyAsString();
      log.debug("body: "+body);
      if( body.indexOf("j_security_check") > 0 )
         fail("get of /war1/index.html redirected to login page");

      /* Access a secured servlet that calls a secured ejb in war2 to test
      propagation of the identity to the ejb container.
      */
      GetMethod war2Servlet = new GetMethod(serverB+"/war1/EJBServlet");
      responseCode = httpConn.executeMethod(war2Servlet.getHostConfiguration(),
         war2Servlet, state);
      response = war2Servlet.getStatusText();
      log.debug("responseCode="+responseCode+", response="+response);
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);
      body = war2Servlet.getResponseBodyAsString();
      log.debug("body: "+body);
      if( body.indexOf("j_security_check") > 0 )
         fail("get of /war1/EJBServlet redirected to login page");
      
   }
   
   private static Cookie copyCookie(Cookie toCopy, String targetServer)
   {
      // Parse the target server down to a domain name
      int index = targetServer.indexOf("://");
      if (index > -1)
      {
         targetServer = targetServer.substring(index + 3);
      }
      index = targetServer.indexOf(":");
      if (index > -1)
      {
         targetServer = targetServer.substring(0, index);         
      }
      index = targetServer.indexOf("/");
      if (index > -1)
      {
         targetServer = targetServer.substring(0, index);
      }
      
      Cookie copy = new Cookie(targetServer,
                               toCopy.getName(),
                               toCopy.getValue(),
                               "/",
                               null,
                               false);
      return copy;
   }

   /** One time setup for all unit tests
    */
   public static Test suite() throws Exception
   {
      Test t1 = JBossClusteredTestCase.getDeploySetup(FormAuthFailoverTestCase.class,
                                                      "web-sso.ear");
      return t1;
   }
}
