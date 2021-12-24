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

import org.jboss.deployment.DeploymentException;
import org.w3c.dom.Element;

/**
 * The meta data object for the security-identity element.
 * The security-identity element specifies whether the caller�s security
 * identity is to be used for the execution of the methods of the enterprise
 * bean or whether a specific run-as role is to be used. It
 * contains an optional description and a specification of the security
 * identity to be used.
 * <p/>
 * Used in: session, entity, message-driven
 *
 * @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 * @author <a href="mailto:Thomas.Diesler@jboss.org">Thomas Diesler</a>.
 * @version $Revision: 57209 $
 */
public class SecurityIdentityMetaData extends MetaData
{
   private String description;
   /**
    *  The use-caller-identity element specifies that the caller�s security
    * identity be used as the security identity for the execution of the
    * enterprise bean�s methods.
    */
   private boolean useCallerIdentity;
   /**
    * The run-as/role-name element specifies the run-as security role name
    * to be used for the execution of the methods of an enterprise bean.
    */
   private String runAsRoleName;
   /**
    * The principal that corresponds to run-as role
    */
   private String runAsPrincipalName;

   public String getDescription()
   {
      return description;
   }

   public boolean getUseCallerIdentity()
   {
      return useCallerIdentity;
   }
   public void setUseCallerIdentity(boolean flag)
   {
      this.useCallerIdentity = flag;
   }

   public String getRunAsRoleName()
   {
      return runAsRoleName;
   }
   public void setRunAsRoleName(String runAsRoleName)
   {
      this.runAsRoleName = runAsRoleName;
   }

   public String getRunAsPrincipalName()
   {
      return runAsPrincipalName;
   }

   public void setRunAsPrincipalName(String principalName)
   {
      this.runAsPrincipalName = principalName;
   }

   /**
    * @param element the security-identity element from the ejb-jar
    */
   public void importEjbJarXml(Element element) throws DeploymentException
   {
      description = getElementContent(getOptionalChild(element, "description"));
      Element callerIdent = getOptionalChild(element, "use-caller-identity");
      Element runAs = getOptionalChild(element, "run-as");
      if (callerIdent == null && runAs == null)
         throw new DeploymentException("security-identity: either use-caller-identity or run-as must be specified");
      if (callerIdent != null && runAs != null)
         throw new DeploymentException("security-identity: only one of use-caller-identity or run-as can be specified");
      if (callerIdent != null)
      {
         useCallerIdentity = true;
      }
      else
      {
         runAsRoleName = getElementContent(getUniqueChild(runAs, "role-name"));
      }
   }
}
