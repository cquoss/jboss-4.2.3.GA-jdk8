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
package org.jboss.security.auth.callback;

import java.security.Principal;
import javax.security.auth.callback.Callback;


/** An implementation of Callback useful on the server side for
propagating the request Principal and credentials to LoginModules.

@author  Scott.Stark@jboss.org
@version $Revision: 57203 $
*/
public class SecurityAssociationCallback implements Callback
{
    private transient Principal principal;
    private transient Object credential;

    /** Initialize the SecurityAssociationCallback
    */
    public SecurityAssociationCallback()
    {
    }

    public Principal getPrincipal()
    {
        return principal;
    }
    public void setPrincipal(Principal principal)
    {
        this.principal = principal;
    }

    public Object getCredential()
    {
        return credential;
    }
    public void setCredential(Object credential)
    {
        this.credential = credential;
    }
    public void clearCredential()
    {
        this.credential = null;
    }
}

