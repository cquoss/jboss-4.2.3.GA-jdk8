package org.jboss.test.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieReadServlet extends HttpServlet {
	
	org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(getClass());
	
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
	{	
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head><title>Cookie Read Servlet</title></head><body><pre>");
		Cookie cookies[] = request.getCookies();
		if(cookies == null) {
			log.info("cookie is null");
			setCookies(request,response);				
			out.println("Server set cookies correctly");
		}
		else {			
			for (int i =0; i < cookies.length; i++)  {
				Cookie c = cookies[i];
				out.println("Cookie" + i + "Name " + c.getName() + " value=" + c.getValue());
				if(c.getName().equals("hasSpace") && c.getValue().indexOf("\"") != -1) {
					log.debug("Cookie name: " + c.getName() + " cookie value: " + c.getValue());
					throw new ServletException("cookie with space not retrieved correctly");
				}
				else if(c.getName().equals("hasComma") && c.getValue().indexOf("\"") != -1) {					
					log.debug("Cookie name: " + c.getName() + " cookie value: " + c.getValue());
					throw new ServletException("cookie with comma not retrieved correctly");
				}
			}
			out.println("Server read cookie correctly");
			
		}
		out.println("</pre></body></html>");
		out.close();
	}
	
	public void setCookies(HttpServletRequest request, HttpServletResponse response) {
		response.addCookie(new Cookie("hasSpace", "has space"));			
		response.addCookie(new Cookie("hasComma", "has,comma"));
		
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
