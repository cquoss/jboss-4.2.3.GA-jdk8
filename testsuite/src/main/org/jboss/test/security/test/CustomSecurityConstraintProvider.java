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
package org.jboss.test.security.test;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.catalina.deploy.SecurityConstraint;
import org.jboss.logging.Logger;

//$Id: CustomSecurityConstraintProvider.java 57211 2006-09-26 12:39:46Z dimitris@jboss.org $

/**
 *  JBAS-2519: Delegate to JACC provider for unsecured resources in web.xml
 *  Custom Security Constraint provider class that overrides
 *  the findSecurityConstraint method of the Realm interface
 *  
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Apr 27, 2006
 *  @version $Revision: 57211 $
 */
public class CustomSecurityConstraintProvider
{ 
   private static Logger log = Logger.getLogger(CustomSecurityConstraintProvider.class);
  
   public CustomSecurityConstraintProvider()
   { 
      log.debug("Constructed.");
   } 
   
   /**
    * Key method that provides the Tomcat AuthenticatorBase with an array 
    * of SecurityConstraint such that a call happens to the Realm
    * 
    * @param request
    * @param context
    * @return
    */
   public SecurityConstraint[] findSecurityConstraints(Request request, 
         Context context)
   {
      log.debug("findSecurityConstraint method called");
      if("/jacc-delegate".equals(context.getName()))
         return getSecurityConstraints(request,context);
      return new SecurityConstraint[]{};
   }
   
   //Private Methods
   /*
    * Return Security Constraint array only when the request is for
    * index.html
    */
   private SecurityConstraint[] getSecurityConstraints(Request request, 
         Context context)
   {
      SecurityConstraint[] scarr = null; 
      if(request.getRequestURI().indexOf("index.html") > -1)
      {
         SecurityConstraint sc = new SecurityConstraint(); 
         sc.setAuthConstraint(false);
         scarr = new SecurityConstraint[]{sc};
      }
      log.debug("getSecurityConstraints is for request uri="+request.getRequestURI()
            + " and SecurityConstraint[]="+scarr);
      return scarr;
   }
}
