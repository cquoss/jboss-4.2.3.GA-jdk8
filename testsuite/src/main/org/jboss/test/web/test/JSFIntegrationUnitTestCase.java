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

import com.sun.faces.util.FacesLogger;

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.util.web.HttpUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;

/** Tests of JSF integration into the JBoss server. This test
 requires than a web container and JSF implementation be integrated 
 into the JBoss server. The tests currently do NOT use the 
 java.net.HttpURLConnection and associated http client and these do 
 not return valid HTTP error codes so if a failure occurs it is best 
 to connect the webserver using a browser to look for additional error
 info. 
 
 @author Stan.Silvert@jboss.org
 @version $Revision: 75507 $
 */
public class JSFIntegrationUnitTestCase extends JBossTestCase
{
   // Call Log4jService to set logging levels for the JSF impl before the WAR
   // is deployed
   public static void setLoggingLevels() throws Exception
   {
      HttpClient client = new HttpClient();
      client.executeMethod(new GetMethod(makeSetLoggerCmd(FacesLogger.MANAGEDBEAN.getLoggerName(), "ALL")));
      client.executeMethod(new GetMethod(makeSetLoggerCmd(FacesLogger.TAGLIB.getLoggerName(), "FATAL")));
      client.executeMethod(new GetMethod(makeSetLoggerCmd(FacesLogger.APPLICATION.getLoggerName(), "ERROR")));
      client.executeMethod(new GetMethod(makeSetLoggerCmd(FacesLogger.CONTEXT.getLoggerName(), "WARN")));
      client.executeMethod(new GetMethod(makeSetLoggerCmd(FacesLogger.CONFIG.getLoggerName(), "INFO")));
      client.executeMethod(new GetMethod(makeSetLoggerCmd(FacesLogger.LIFECYCLE.getLoggerName(), "DEBUG")));
      client.executeMethod(new GetMethod(makeSetLoggerCmd(FacesLogger.TIMING.getLoggerName(), "TRACE")));
      client.executeMethod(new GetMethod(makeSetLoggerCmd(FacesLogger.RENDERKIT.getLoggerName(), "OFF")));
   }

   private static String makeSetLoggerCmd(String log4jLogger, String log4jPriority)
   {
      String baseURL = HttpUtils.getBaseURL();
      return baseURL + 
             "jmx-console/HtmlAdaptor?" +
             "action=invokeOp&" +
             "name=jboss.system:type=Log4jService,service=Logging&" +
             "methodIndex=1&" +
             "arg0=" + log4jLogger + "&" +
             "arg1=" + log4jPriority;
   }

   public JSFIntegrationUnitTestCase(String name)
   {
      super(name);
   }
   
   /** Access the http://localhost/jbosstest-jsf/index.jsf.
    */
   public void testJSFIntegrated() throws Exception
   {
      HttpClient client = new HttpClient();
      client.executeMethod(makeRequest());
      
      HttpMethodBase result = makeRequest();

      // need to hit it twice with the same session for test to pass
      client.executeMethod(result);

      String responseBody = result.getResponseBodyAsString();
      if (responseBody == null) {
         throw new Exception("Unable to get response from server.");
      }

      assertTrue(contains(responseBody, "@PostConstruct was called."));
      assertTrue(contains(responseBody, "@PreDestroy was called."));
      assertTrue(contains(responseBody, "Datasource was injected."));

      // Tests JSF/JSTL integration
      assertTrue(contains(responseBody, "number one"));
      assertTrue(contains(responseBody, "number two"));
      assertTrue(contains(responseBody, "number three"));

      // Tests enum support 
      assertTrue(contains(responseBody, "JBoss Color selection is PURPLE"));

      // Test logging
      assertFalse(contains(responseBody, "Logged SEVERE message in RENDERKIT_LOGGER"));
      assertFalse(contains(responseBody, "Logged SEVERE message in TAGLIB_LOGGER"));
      assertTrue(contains(responseBody, "Logged SEVERE message in APPLICATION_LOGGER"));
      assertFalse(contains(responseBody, "Logged WARNING message in APPLICATION_LOGGER"));
      assertTrue(contains(responseBody, "Logged WARNING message in CONTEXT_LOGGER"));
      assertFalse(contains(responseBody, "Logged INFO message in CONTEXT_LOGGER"));
      assertTrue(contains(responseBody, "Logged INFO message in CONFIG_LOGGER"));
      assertFalse(contains(responseBody, "Logged FINE message in CONFIG_LOGGER"));
      assertTrue(contains(responseBody, "Logged FINE message in LIFECYCLE_LOGGER"));
      assertFalse(contains(responseBody, "Logged FINER message in LIFECYCLE_LOGGER"));
      assertFalse(contains(responseBody, "Logged FINEST message in LIFECYCLE_LOGGER"));
      assertTrue(contains(responseBody, "Logged FINER message in TIMING_LOGGER"));
      assertTrue(contains(responseBody, "Logged FINEST message in TIMING_LOGGER"));
      assertTrue(contains(responseBody, "Logged FINEST message in MANAGEDBEAN_LOGGER"));
      
   }   

   private boolean contains(String base, String target) {
      return base.indexOf(target) != -1;
   }

   private GetMethod makeRequest() {
      String baseURL = HttpUtils.getBaseURL();
      return new GetMethod(baseURL+"jbosstest-jsf/index.jsf");
   }

   public static Test suite() throws Exception
   {
      // need to set up the JSF loggers before the WAR is deployed
      setLoggingLevels();
      
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(JSFIntegrationUnitTestCase.class));

      // Create an initializer for the test suite
      Test wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            deploy("jbosstest-jsf.war");           
         }
         protected void tearDown() throws Exception
         {
            undeploy("jbosstest-jsf.war");
            super.tearDown();            
         }
      };
      return wrapper;
   }
   

}
