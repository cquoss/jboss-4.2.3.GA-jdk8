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
package org.jboss.ejb3.test.security;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;

/** An implmentation of the Session interface that should not
be accessible by external users.

@author Scott.Stark@jboss.org
@version $Revision: 60233 $ 
*/
@Stateless(name="PrivateSession")
@Remote(org.jboss.ejb3.test.security.Session.class)
@RemoteBinding(jndiBinding = "spec.PrivateSession")
@SecurityDomain("spec-test")
@RolesAllowed({"InternalUser"})
public class PrivateSessionBean
{
   @Resource SessionContext sessionContext;

    public String echo(String arg)
    {
        System.out.println("PrivateSessionBean.echo, arg="+arg);
        Principal p = sessionContext.getCallerPrincipal();
        System.out.println("PrivateSessionBean.echo, callerPrincipal="+p);
        System.out.println("PrivateSessionBean.echo, isCallerInRole('InternalUser')="+sessionContext.isCallerInRole("InternalUser"));
        return arg;
    }
    public void noop() 
    {
        System.out.println("PrivateSessionBean.noop");
        Principal p = sessionContext.getCallerPrincipal();
        System.out.println("PrivateSessionBean.noop, callerPrincipal="+p);
    }
    public void restricted() 
    {
        System.out.println("PrivateSessionBean.restricted");
        Principal p = sessionContext.getCallerPrincipal();
        System.out.println("PrivateSessionBean.restricted, callerPrincipal="+p);
    }
}
