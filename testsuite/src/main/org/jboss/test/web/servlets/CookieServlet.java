package org.jboss.test.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jboss.test.web.util.Util;

/** A servlet that is used to test different way of setting and retrieving cookies.
 
 @author  prabhat.jha@jboss.com
 @version $Revision$
 */


public class CookieServlet extends HttpServlet {
	
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
	{
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head><title>Cookie Servlet</title></head><body><pre>");
		setRFC2019cookies(request,response);
		out.println("sever set some cookies. verify on the client that you can see them");
		out.println("</pre></body></html>");
		out.close();
	}
	
	private void setRFC2019cookies(HttpServletRequest request, HttpServletResponse response) {
		
		//A very simple cookie
		Cookie cookie = new Cookie("simpleCookie","jboss");
		response.addCookie(cookie);		
		
		//A cookie with space in the value. As per ASPATCH-70, there has been some issue with this.
		cookie = new Cookie("withSpace", "jboss rocks");
		response.addCookie(cookie);
		
		//cookie with comment
		cookie = new Cookie("commented", "commented cookie");
		cookie.setComment("This is a comment");
		response.addCookie(cookie);
		
		//cookie with expiry time. This cookie must not be set on client side
		cookie = new Cookie("expired","expired cookie");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
		
		cookie = new Cookie("withComma","little,comma");
		response.addCookie(cookie);
		
		cookie = new Cookie("expireIn10Sec","will expire in 10 seconds");
		cookie.setMaxAge(10);
		response.addCookie(cookie);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
	{
		processRequest(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
	{
		processRequest(request, response);
	}
	
}
