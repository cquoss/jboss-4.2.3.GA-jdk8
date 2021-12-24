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
package org.jboss.web.tomcat.security;

import java.security.Principal;
import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.servlet.http.HttpServletRequest;

import org.jboss.security.auth.callback.SecurityAssociationHandler;

/**
 * An implementation of CallbackHandler that extends the default
 * SecurityAssociationHandler to add Callbacks that only have sense in a Web
 * environment.
 * 
 * In order to use it you need to override the default CallbackHandler used by
 * the JaasSecurityManager.
 * 
 * @see javax.security.auth.callback.CallbackHandler
 * @see #handle(Callback[])
 * 
 * @deprecated Use the standard JACC PolicyContext handler for the servlet request
 *
 * @author Ricardo Arguello (ricardoarguello@users.sourceforge.net)
 * @version $Revision: 57206 $
 */
public class WebCallbackHandler extends SecurityAssociationHandler implements
      CallbackHandler
{
   public WebCallbackHandler()
   {
      super();
   }

   /**
    * Initialize the HttpServletRequestCallbackHandler with the principal and
    * credentials to use.
    */
   public WebCallbackHandler(Principal principal, Object credential)
   {
      super(principal, credential);
   }

   /**
    * @see org.jboss.security.auth.callback.SecurityAssociationHandler#setSecurityInfo(java.security.Principal,
    *      java.lang.Object)
    */
   public void setSecurityInfo(Principal principal, Object credential)
   {
      super.setSecurityInfo(principal, credential);
   }

   /**
    * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
    */
   public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
   {
      try
      {
         super.handle(callbacks);
      }
      catch (UnsupportedCallbackException uce)
      {
         Callback c = uce.getCallback();

         if (c instanceof HttpServletRequestCallback)
         {
            // Get the HttpServletRequest from the Valve
            HttpServletRequest request = getHttpServletRequestFromValve();

            // Set it in the Callback
            HttpServletRequestCallback hsrc = (HttpServletRequestCallback) c;
            hsrc.setHttpServletRequest(request);
         }
         else
         {
            throw new UnsupportedCallbackException(c, "Unrecognized Callback");
         }
      }
   }

   /**
    * Obtains the HttpServletRequest saved inside the HttpServletRequestValve.
    * 
    * @return an HttpServletRequest.
    */
   protected HttpServletRequest getHttpServletRequestFromValve()
   {
      return (HttpServletRequest) HttpServletRequestValve.httpRequest.get();
   }
}