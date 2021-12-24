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
package org.jboss.deployment;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.metadata.MetaData;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.security.SecurityRoleMetaData;
import org.w3c.dom.Element;

/**
 * A representation of the application.xml and jboss-app.xml deployment
 * descriptors.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Scott.Stark@jboss.org
 * @version $Revision: 60677 $
 * @see org.jboss.metadata.XmlLoadable
 */
public class J2eeApplicationMetaData
        extends MetaData
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   private String displayName;
   private String description;
   private String smallIcon;
   private String largeIcon;

   /**
    * The security-roles
    */
   private HashMap securityRoles = new HashMap();
   /**
    * The jboss-app.xml JNDI name of the security domain implementation
    */
   private String securityDomain;
   /**
    * The  unauthenticated-principal value assigned to the application
    */
   private String unauthenticatedPrincipal;
   /** The application.xml name->modules in definition order */
   private Map modules = new LinkedHashMap();
   
   /** The jmx name */
   private String jmxName;

   /** The library-directory name */
   private String libraryDirectory;

   /** 
    * Module order will be based on the deployment sorter (implicit) or on the
    * ordering of modules defined in application.xml and jboss-app.xml (strict). 
    */
   private String moduleOrder = "implicit";

   // Static --------------------------------------------------------

   // Public --------------------------------------------------------

   public String getDisplayName()
   {
      return displayName;
   }

   public String getDescription()
   {
      return description;
   }

   public String getSmallIcon()
   {
      return smallIcon;
   }

   public String getLargeIcon()
   {
      return largeIcon;
   }

   public Iterator getModules()
   {
      return modules.values().iterator();
   }

   public boolean hasModule(String name)
   {
      return modules.containsKey(name);
   }
   
   public Map getSecurityRoles()
   {
      return new HashMap(securityRoles);
   }

   public String getSecurityDomain()
   {
      return securityDomain;
   }

   public String getUnauthenticatedPrincipal()
   {
      return unauthenticatedPrincipal;
   }

   public String getJMXName()
   {
      return jmxName;
   }

   public String getLibraryDirectory()
   {
      return libraryDirectory;
   }

   public String getModuleOrder()
   {
	  return moduleOrder; 
   }
   /**
    * Imports either the application.xml or jboss-app.xml from the given element.
    *
    * @param rootElement The element to import.
    * @throws DeploymentException Unrecognized root tag.
    */
   public void importXml(Element rootElement) throws DeploymentException
   {
      String rootTag = rootElement.getOwnerDocument().getDocumentElement().getTagName();
      if (rootTag.equals("application"))
      {
         importApplicationXml(rootElement);
      }
      else if (rootTag.equals("jboss-app"))
      {
         importJBossAppXml(rootElement);
      }
      else
      {
         throw new DeploymentException("Unrecognized root tag: " + rootTag);
      }
   }

   protected void importApplicationXml(Element rootElement) throws DeploymentException
   {
      // j2ee_1_4.xsd describes display-name as minOccurs="0" to maxOccurs="unbounded"
      displayName = super.getOptionalChildContent(rootElement, "display-name", "");

      Element descrElement = getOptionalChild(rootElement, "description");
      description = descrElement != null ? getElementContent(descrElement) : "";

      Element iconElement = getOptionalChild(rootElement, "icon");
      if (iconElement != null)
      {
         Element element = getOptionalChild(iconElement, "small-icon");
         smallIcon = element != null ? getElementContent(element) : "";

         element = getOptionalChild(iconElement, "large-icon");
         largeIcon = element != null ? getElementContent(element) : "";
      }
      else
      {
         smallIcon = "";
         largeIcon = "";
      }

      // extract modules...
      for (Iterator it = getChildrenByTagName(rootElement, "module"); it.hasNext();)
      {
         J2eeModuleMetaData moduleMetaData = new J2eeModuleMetaData();
         moduleMetaData.importXml((Element) it.next());
         modules.put(moduleMetaData.getFileName(), moduleMetaData);
      }
   }

   protected void importJBossAppXml(Element rootElement) throws DeploymentException
   {
      // Get the security domain name
      Element securityDomainElement = getOptionalChild(rootElement, "security-domain");
      if (securityDomainElement != null)
      {
         securityDomain = getElementContent(securityDomainElement);
      }

      // Get the unauthenticated-principal name
      Element unauth = getOptionalChild(rootElement, "unauthenticated-principal");
      if (unauth != null)
      {
         unauthenticatedPrincipal = getElementContent(unauth);
      }
      else
      {
         try
         {
            MBeanServer server = MBeanServerLocator.locateJBoss();
            ObjectName oname = new ObjectName("jboss.security:service=JaasSecurityManager");
            unauthenticatedPrincipal = (String) server.getAttribute(oname, "DefaultUnauthenticatedPrincipal");
         }
         catch (Exception e)
         {
            log.error("Cannot obtain unauthenticated principal");
         }
      }

      // set the security roles (optional)
      Iterator iterator = getChildrenByTagName(rootElement, "security-role");
      while (iterator.hasNext())
      {
         Element securityRole = (Element) iterator.next();
         String roleName = getElementContent(getUniqueChild(securityRole, "role-name"));
         SecurityRoleMetaData srMetaData = new SecurityRoleMetaData(roleName);

         Iterator itPrincipalNames = getChildrenByTagName(securityRole, "principal-name");
         while (itPrincipalNames.hasNext())
         {
            String principalName = getElementContent((Element) itPrincipalNames.next());
            srMetaData.addPrincipalName(principalName);
         }
         securityRoles.put(roleName, srMetaData);
      }

      // Get any user defined JMX name
      Element jmxNameElement = getOptionalChild(rootElement, "jmx-name");
      if (jmxNameElement != null)
         jmxName = getElementContent(jmxNameElement);

      // Get any library-directory. The default empty elements means there is no library directory
      Element libDirElement = getOptionalChild(rootElement, "library-directory");
      if (libDirElement != null)
         libraryDirectory = getElementContent(libDirElement, "");

      // extract modules...
      for (Iterator it = getChildrenByTagName(rootElement, "module"); it.hasNext();)
      {
         J2eeModuleMetaData moduleMetaData = new J2eeModuleMetaData();
         moduleMetaData.importXml((Element) it.next());
         modules.put(moduleMetaData.getFileName(), moduleMetaData);
      }
      
      //Get the Deployment Ordering style 
      Element moduleOrderElement = getOptionalChild(rootElement, "module-order");
      if (moduleOrderElement != null)
      {
    	  moduleOrder = getElementContent(moduleOrderElement);
      }
   }
}
