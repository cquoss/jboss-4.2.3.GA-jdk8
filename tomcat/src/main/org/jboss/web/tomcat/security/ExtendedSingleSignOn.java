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

import org.apache.catalina.Session;
import org.apache.catalina.authenticator.SingleSignOn;

/**
 * <p>
 * An extension of the <code>SingleSignOn</code> valve that exposes some protected methods of
 * the superclass as <code>public</code>, allowing the <code>WebAuthentication</code> class
 * to delegate single sign-on behaviour to this valve.
 * </p>
 * 
 * @author sguilhen@redhat.com
 */
public class ExtendedSingleSignOn extends SingleSignOn
{

   /*
    * (non-Javadoc)
    * @see org.apache.catalina.authenticator.SingleSignOn#associate(java.lang.String, org.apache.catalina.Session)
    */
   @Override
   public void associate(String ssoId, Session session)
   {
      super.associate(ssoId, session);
   }
   
   /*
    * (non-Javadoc)
    * @see org.apache.catalina.authenticator.SingleSignOn#register(java.lang.String, java.security.Principal, java.lang.String, java.lang.String, java.lang.String)
    */
   @Override
   public void register(String ssoId, Principal principal, String authType, String username, String password)
   {
      super.register(ssoId, principal, authType, username, password);
   }

   /*
    * (non-Javadoc)
    * @see org.apache.catalina.authenticator.SingleSignOn#deregister(java.lang.String)
    */
   @Override
   public void deregister(String ssoId)
   {
      super.deregister(ssoId);
   }
   
   /*
    * (non-Javadoc)
    * @see org.apache.catalina.authenticator.SingleSignOn#update(java.lang.String, java.security.Principal, java.lang.String, java.lang.String, java.lang.String)
    */
   @Override
   public void update(String ssoId, Principal principal, String authType, String username, String password)
   {
      super.update(ssoId, principal, authType, username, password);
   }
}
