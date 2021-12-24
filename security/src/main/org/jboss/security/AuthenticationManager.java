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
package org.jboss.security;

import java.security.Principal;
import javax.security.auth.Subject;

/** The AuthenticationManager is responsible for validating credentials
 * associated with principals.
 *      
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57203 $
 */
public interface AuthenticationManager
{
   /** Get the security domain from which the security manager is from. Every
       security manager belongs to a named domain. The meaning of the security
       domain name depends on the implementation. Examples range from as fine
       grained as the name of EJBs to J2EE application names to DNS domain names.
   @return the security domain name. May be null in which case the security
       manager belongs to the logical default domain.
   */
   String getSecurityDomain();

   /** The isValid method is invoked to see if a user identity and associated
    credentials as known in the operational environment are valid proof of the
    user identity. Typically this is implemented as a call to isValid with a
    null Subject.

    @see #isValid(Principal, Object, Subject)

    @param principal - the user identity in the operation environment 
    @param credential - the proof of user identity as known in the
    operation environment 
    @return true if the principal, credential pair is valid, false otherwise.
   */
   public boolean isValid(Principal principal, Object credential);

   /** The isValid method is invoked to see if a user identity and associated
       credentials as known in the operational environment are valid proof of the
       user identity. This extends AuthenticationManager version to provide a
       copy of the resulting authenticated Subject. This allows a caller to
       authenticate a user and obtain a Subject whose state cannot be modified
       by other threads associated with the same principal.
    @param principal - the user identity in the operation environment 
    @param credential - the proof of user identity as known in the
    operation environment
    @param activeSubject - the Subject which should be populated with the
      validated Subject contents. A JAAS based implementation would typically
      populate the activeSubject with the LoginContext.login result.
    @return true if the principal, credential pair is valid, false otherwise.
   */
   boolean isValid(Principal principal, Object credential,
      Subject activeSubject);

   /** Get the currently authenticated subject. Historically implementations of
    AuthenticationManager isValid methods had the side-effect of setting the
    active Subject. This caused problems with multi-threaded usecases where the
    Subject instance was being shared by multiple threads. This is now deprecated
    in favor of the JACC PolicyContextHandler getContext(key, data) method.

    @deprecated Use the JACC PolicyContextHandler using key "javax.security.auth.Subject.container"
    @see javax.security.jacc.PolicyContextHandler#getContext(String, Object)

    @return The previously authenticated Subject if isValid succeeded, null if
        isValid failed or has not been called for the active thread.
    */
   Subject getActiveSubject();
}
