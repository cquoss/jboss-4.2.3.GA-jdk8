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
package org.jboss.test.web.servlets;

import java.io.IOException;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;
import org.jboss.test.security.interfaces.StatelessSession;
import org.jboss.test.security.interfaces.StatelessSessionHome;

//$Id: SecureServletSecureEJB.java 57211 2006-09-26 12:39:46Z dimitris@jboss.org $

/**
 *  Servlet that is secured by the web container and which accesses 
 *  some secured EJBs in its service method
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Apr 3, 2006
 *  @version $Revision: 57211 $
 */
public class SecureServletSecureEJB extends HttpServlet
{ 
   /** The serialVersionUID */
   private static final long serialVersionUID = 3116454567023980935L;
   
   private String username = "scott";
   
   private static Logger log = Logger.getLogger(SecureServletSecureEJB.class);
   
   /**
    * Access Secured EJBs
    */
   protected void service(HttpServletRequest request, 
         HttpServletResponse response) throws ServletException, IOException
   { 
      StatelessSession bean =  null;
      try
      {
         InitialContext context = new InitialContext();
         Object obj = context.lookup("java:comp/env/ejb/StatelessSession");
         obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
         StatelessSessionHome home = (StatelessSessionHome) obj;
         log.debug("Found StatelessSession");
         bean = home.create();
         log.debug("Created StatelessSession");
         // Test that the bean sees username as its principal
         String echo = bean.echo(username);
         log.debug("bean.echo(username) = "+echo);
         if(echo.equals(username) == false)
            throw new IllegalStateException("username == echo"); 
      }
      catch (Exception e)
      {
         throw new ServletException(e);
      } 
      finally
      {
         if(bean != null)
         try
         {
               bean.remove();
         }
         catch (Exception e)
         {
            throw new ServletException(e);
         } 
      } 
   } 
}
