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
package org.jboss.security.plugins;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.security.Principal;
import javax.security.auth.Subject;

import org.jboss.security.RealmMapping;
import org.jboss.security.SubjectSecurityManager;
import org.jboss.security.NobodyPrincipal;


/** An implementation of SubjectSecurityManager, RealmMapping does not allow
 any authentication and every check for a role fails.

@see #isValid(java.security.Principal, Object, Subject)
@see #getPrincipal(java.security.Principal)
@see #doesUserHaveRole(java.security.Principal, java.util.Set)

@author Scott.Stark@jboss.org
@version $Revision: 57203 $
*/
public class NoAccessSecurityManager
    implements SubjectSecurityManager, RealmMapping, Serializable
{
    static final long serialVersionUID = -5922913661708382384L;
    private String securityDomain;

    /** Creates a default JaasSecurityManager for with the
        given securityDomain name.
    */
    public NoAccessSecurityManager(String securityDomain)
    {
        this.securityDomain = securityDomain;
    }

    /** Get the name of the security domain associated with this security mgr.
        @return Name of the security manager security domain.
     */
    public String getSecurityDomain()
    {
        return securityDomain;
    }
    /** Get the currently authenticated Subject.
        @return Always returns null.
     */
    public Subject getActiveSubject()
    {
        return null;
    }

    /** Validate that the given credential is correct for principal.
    @return always returns true.
     */
    public boolean isValid(Principal principal, Object credential)
    {
        return false;
    }
    /** Validate that the given credential is correct for principal. This does
     not populate the activeSubject with any state since no authentication
     is performed.
    @return always returns true.
     */
    public boolean isValid(Principal principal, Object credential,
      Subject activeSubject)
    {
       return false;
    }

    /** Always returns the argument principal.
    @return The argument principal
     */
    public Principal getPrincipal(Principal principal)
    {
        Principal result = principal;
        return result;
    }

    /** Does the current Subject have a role(a Principal) that equates to one
        of the role names. This method always returns true.
    @param principal - ignored.
    @param roleNames - ignored.
    @return Always returns true.
    */
    public boolean doesUserHaveRole(Principal principal, Set roleNames)
    {
        boolean hasRole = false;
        return hasRole;
    }

    /** Return the set of domain roles the principal has been assigned.
    @return The Set<Principal> with the NobodyPrincipal as the sole role.
     */
    public Set getUserRoles(Principal principal)
    {
        HashSet roles = new HashSet();
        roles.add(NobodyPrincipal.NOBODY_PRINCIPAL);
        return roles;
    }

    /** Authenticate principal against credential
     * @param principal - the user id to authenticate
     * @param credential - an opaque credential.
     * @return Always returns true.
     */
    private boolean authenticate(Principal principal, Object credential)
    {
        boolean authenticated = false;
        return authenticated;
    }
}
