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
package org.jboss.ejb3.test.dd.web.servlets;           

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.security.auth.callback.UsernamePasswordHandler;
import org.jboss.ejb3.test.dd.web.interfaces.StatelessSession;

/** A servlet that performs a JAAS login to access a secure EJB.

@author Scott.Stark@jboss.org
@version $Revision: 57207 $
*/
public class ClientLoginServlet extends HttpServlet
{
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        LoginContext lc = null;
        String echoMsg = null;
        try
        {
            lc = doLogin("jduke", "theduke");
            InitialContext ctx = new InitialContext();
            StatelessSession bean = (StatelessSession)ctx.lookup("java:comp/env/ejb/SecuredEJB");
            echoMsg = bean.echo("ClientLoginServlet called SecuredEJB.echo");
        }
        catch(LoginException e)
        {
            throw new ServletException("Failed to login to client-login domain as jduke", e);
        }
        catch(Exception e)
        {
            throw new ServletException("Failed to access SecuredEJB", e);
        }
        finally
        {
            if( lc != null )
            {
                try
                {
                    lc.logout();
                }
                catch(LoginException e)
                {
                }
            }
        }

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>ClientLoginServlet</title></head>");
        out.println("<h1>ClientLoginServlet Accessed</h1>");
        out.println("<body>Login as user=jduke succeeded.<br>SecuredEJB.echo returned:"+echoMsg+"</body>");
        out.println("</html>");
        out.close();
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

    private LoginContext doLogin(String username, String password) throws LoginException
    {
        UsernamePasswordHandler handler = new UsernamePasswordHandler(username, password.toCharArray());
        LoginContext lc = new LoginContext("client-login", handler);
        lc.login();
        return lc;
    }
}
