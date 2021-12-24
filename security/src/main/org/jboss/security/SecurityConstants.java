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

//$Id: SecurityConstants.java 67068 2007-11-14 15:04:33Z anil.saldhana@jboss.com $

/**
 *  Defines Constants for usage in the Security Layer
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Dec 30, 2005 
 *  @version $Revision: 67068 $
 */
public interface SecurityConstants
{
   /**
    * Default Application Policy 
    */
   String DEFAULT_APPLICATION_POLICY = "other";

   /**
    * The String option name used to pass in the security-domain 
    * name the LoginModule was configured in.
    */
   String SECURITY_DOMAIN_OPTION = "jboss.security.security_domain";
   
   /**
    * System Property that disables the addition of security domain
    * in the module options passed to login module
    */
   String DISABLE_SECDOMAIN_OPTION = "jboss.security.disable.secdomain.option";
}
