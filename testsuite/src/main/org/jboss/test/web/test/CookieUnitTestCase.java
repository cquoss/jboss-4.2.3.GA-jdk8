package org.jboss.test.web.test;

import java.net.HttpURLConnection;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.util.web.HttpUtils;

/**
 *Test case for cookie
 *@author  prabhat.jha@jboss.com
 *@version $Revision$
 */

public class CookieUnitTestCase extends JBossTestCase 

{
	protected static String[] cookieNames= {"simpleCookie","withSpace","commented","expired"}; 
	private String baseURL = HttpUtils.getBaseURL(); 
	
	public CookieUnitTestCase(String name) 
	{
		super(name);
	}
	
	public void testCookieSetCorrectly() throws Exception
	{
		log.info("testCookieSetCorrectly");
		URL url = new URL(baseURL+"jbosstest-cookie/CookieReadServlet");
		HttpClient httpClient = new HttpClient();	      
		HttpMethodBase request = HttpUtils.createMethod(url,HttpUtils.GET);
		//sending a blank request
		httpClient.executeMethod(request);
		
		log.info("sending request with cookie");		
		request = HttpUtils.createMethod(url,HttpUtils.POST);
		int responseCode = httpClient.executeMethod(request);
		assertEquals(responseCode,HttpURLConnection.HTTP_OK);		
	}
	
	public void testCookieRetrievedCorrectly() throws Exception
	{
		URL url = new URL(baseURL+"jbosstest-cookie/CookieServlet");
		HttpClient httpClient = new HttpClient();	      
		HttpMethodBase request = HttpUtils.createMethod(url,HttpUtils.GET);	     
		int responseCode =httpClient.executeMethod(request);
		//assert that we are able to hit servlet successfully
		assertEquals(responseCode,HttpURLConnection.HTTP_OK);
		request.getResponseHeader("Set-Cookie");
		
		Cookie[] cookies = httpClient.getState().getCookies();	      
		//verify that expired cookie is not set by server
		assertTrue("sever did not set expired cookie on client", checkNoExpiredCookie(cookies));
		
		for(int i = 0; i < cookies.length; i++) {
			log.info("Cookie " + i + " : " + cookies[i].toExternalForm());
			if(cookies[i].getName().equals("simpleCookie")) {
				assertTrue("cookie value should be jboss", cookies[i].getValue().equals("jboss"));
				assertEquals("cookie path", "/jbosstest-cookie", cookies[i].getPath());
				assertEquals("cookie persistence", false, cookies[i].isPersistent());
			}
			else if(cookies[i].getName().equals("withSpace"))	    		  
				assertEquals("should be no quote in cookie with space", cookies[i].getValue().indexOf("\""),-1);
			else if(cookies[i].getName().equals("commented"))	{	    		  
				log.info("comment in cookie: " +  cookies[i].getComment());
				//RFC2109:Note that there is no Comment attribute in the Cookie request header
				//corresponding to the one in the Set-Cookie response header.  The user
				//agent does not return the comment information to the origin server.
				
				assertTrue(cookies[i].getComment() == null);
			}	else if(cookies[i].getName().equals("withComma")) {
				assertTrue("should contain a comma", cookies[i].getValue().indexOf(",") != -1);
			}
			else if(cookies[i].getName().equals("expireIn10Sec"))	{
				log.info("will sleep for 5 seconds to see if cookie expires");
				Thread.sleep(5000);
				assertTrue("cookies should not be expired by now", !cookies[i].isExpired());
				log.info("will sleep for 5 more secs and it should expire");
				Thread.sleep(5000);
				assertTrue("cookies should be expired by now", cookies[i].isExpired());	    		  
			}	
			
		}
	}
	
	protected boolean checkNoExpiredCookie(Cookie[] cookies)  
	{
		for(int i = 0; i < cookies.length; i++) 
			if(cookies[i].getName().equals("expired"))
				return false;
		return true;
	}
	
	/**
	 * Setup the test suite.
	 */
	public static Test suite() throws Exception
	{
		TestSuite suite = new TestSuite(CookieUnitTestCase.class);
		
		// Create an initializer for the test suite
		Test wrapper = new JBossTestSetup(suite)
		{
			protected void setUp() throws Exception
			{
				super.setUp();
				redeploy("jbosstest-cookie.war");	            
			}
			protected void tearDown() throws Exception
			{
				undeploy("jbosstest-cookie.war");
				super.tearDown();
			}
		};
		return wrapper;
	}
}
