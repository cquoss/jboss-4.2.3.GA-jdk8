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
package org.jboss.metadata;

import org.w3c.dom.Element;

import org.jboss.deployment.DeploymentException;

/** The metadata object for the security-role-ref element.
The security-role-ref element contains the declaration of a security
role reference in the enterprise bean’s code. The declaration con-sists
of an optional description, the security role name used in the
code, and an optional link to a defined security role.
The value of the role-name element must be the String used as the
parameter to the EJBContext.isCallerInRole(String roleName) method.
The value of the role-link element must be the name of one of the
security roles defined in the security-role elements.

Used in: entity and session

 *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 *   @version $Revision: 57209 $
 */
public class SecurityRoleRefMetaData extends MetaData {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
	private String name;
    private String link;
    private String description;
	
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    public SecurityRoleRefMetaData () {
	}
	
    // Public --------------------------------------------------------
	
	public String getName() { return name; }
	
	public String getLink() { return link; }
	public String getDescription() { return description; }

    public void importEjbJarXml(Element element) throws DeploymentException {
		name = getElementContent(getUniqueChild(element, "role-name"));
		link = getElementContent(getOptionalChild(element, "role-link"));
		description = getElementContent(getOptionalChild(element, "description"));
	}		

}
