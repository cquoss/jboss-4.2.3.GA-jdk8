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

// $Id: EjbPortComponentMetaData.java 63190 2007-05-23 07:54:49Z thomas.diesler@jboss.com $

import org.jboss.deployment.DeploymentException;
import org.w3c.dom.Element;

/** The metdata data for session/port-component element from jboss.xml
 *
 * @author Scott.Stark@jboss.org
 * @author Thomas.Diesler@jboss.com
 * @version $Revision: 63190 $
 */
public class EjbPortComponentMetaData
{
   private SessionMetaData sessionMetaData;

   private String portComponentName;
   private String portComponentURI;
   private String authMethod;
   private String transportGuarantee;
   private Boolean secureWSDLAccess;

   public EjbPortComponentMetaData(SessionMetaData sessionMetaData)
   {
      this.sessionMetaData = sessionMetaData;
   }

   public String getPortComponentName()
   {
      return portComponentName;
   }

   public String getPortComponentURI()
   {
      return portComponentURI;
   }

   public String getURLPattern()
   {
      String pattern = "/*";
      if (portComponentURI != null)
      {
         return portComponentURI;
      }
      return pattern;
   }

   public String getAuthMethod()
   {
      return authMethod;
   }

   public String getTransportGuarantee()
   {
      return transportGuarantee;
   }

   public Boolean getSecureWSDLAccess()
   {
      return secureWSDLAccess;
   }

   public void importStandardXml(Element element) throws DeploymentException
   {
   }

   /** Parse the port-component contents
    * @param element
    * @throws DeploymentException
    */
   public void importJBossXml(Element element) throws DeploymentException
   {
      // port-component/port-component-name
      portComponentName = MetaData.getUniqueChildContent(element, "port-component-name");

      // port-component/port-component-uri?
      portComponentURI = MetaData.getOptionalChildContent(element, "port-component-uri");
      if (portComponentURI != null)
      {
         if (portComponentURI.charAt(0) != '/')
            portComponentURI = "/" + portComponentURI;
      }
      else
      {
         portComponentURI = "/" + sessionMetaData.getEjbName();
         // The context root will be derived from deployment short name
      }

      // port-component/auth-method?,
      authMethod = MetaData.getOptionalChildContent(element, "auth-method");
      // port-component/transport-guarantee?
      transportGuarantee = MetaData.getOptionalChildContent(element, "transport-guarantee");
      // port-component/secure-wsdl-access?
      if (MetaData.getOptionalChildContent(element, "secure-wsdl-access") != null)
         secureWSDLAccess = Boolean.valueOf(MetaData.getOptionalChildContent(element, "secure-wsdl-access"));
   }
}
