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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.deployment.DeploymentException;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.security.SecurityRoleMetaData;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

/**
 * The top level meta data from the jboss.xml and ejb-jar.xml descriptor.
 *
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:criege@riege.com">Christian Riege</a>
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>.
 * @author <a href="mailto:Thomas.Diesler@jboss.org">Thomas Diesler</a>.
 *
 * @version $Revision: 57209 $
 */
public class ApplicationMetaData
        extends MetaData
{
   public static final int EJB_1x = 1;
   public static final int EJB_2x = 2;
   /** The ejb jar URL */
   private URL url;
   /** version of the dtd used to create ejb-jar.xml */
   protected int ejbVersion;
   protected int ejbMinorVersion;
   /** ArrayList<BeanMetaData> for the ejbs */
   private ArrayList beans = new ArrayList();
   /** A HashMap<String, String> for webservice description publish locations */
   private HashMap wsdlPublishLocationMap = new HashMap();
   /** True if this is a web service deployment */
   private boolean webServiceDeployment;
   /** The optional JBossWS config-name */
   private String configName;
   /** The optional JBossWS config-file */
   private String configFile;
   /** List<RelationMetaData> of relations in this application. */
   private ArrayList relationships = new ArrayList();
   /** The assembly-descriptor */
   private AssemblyDescriptorMetaData assemblyDescriptor = new AssemblyDescriptorMetaData();
   /** A HashMap<String, ConfigurationMetaData> for container configs */
   private HashMap configurations = new HashMap();
   /** A HashMap<String, InvokerProxyBindingMetaData> for invoker bindings */
   private HashMap invokerBindings = new HashMap();
   /** A HashMap<String, String> of res-name to JNDI name/URL */
   private HashMap resources = new HashMap();
   private HashMap plugins = new HashMap();
   /** The user defined JMX name for the EJBModule */
   private String jmxName;
   /** The security-domain value assigned to the application */
   private String securityDomain;
   /** The  unauthenticated-principal value assigned to the application */
   private String unauthenticatedPrincipal;
   /** The web context root to use for web services */
   private String webServiceContextRoot;
   /** An unused flag if the spec security restrictions should be enforced */
   private boolean enforceEjbRestrictions;
   /** The missing-method-permissions-excluded-mode value */
   private boolean excludeMissingMethods = true;
   /** Whether to throw an exception on a rollback if there is no exception */
   private boolean exceptionRollback = false;

   /** The ClassLoader to load additional resources */
   private URLClassLoader resourceCl;

   public ApplicationMetaData()
   {
   }

   /** Get the ClassLoader to load additional resources */
   public URLClassLoader getResourceCl()
   {
      return resourceCl;
   }

   /** Set the ClassLoader to load additional resources */
   public void setResourceClassLoader(URLClassLoader resourceCl)
   {
      this.resourceCl = resourceCl;
   }

   public URL getUrl()
   {
      return url;
   }

   public void setUrl(URL u)
   {
      url = u;
   }

   public boolean isEJB1x()
   {
      return ejbVersion == 1;
   }

   public boolean isEJB2x()
   {
      return ejbVersion == 2;
   }

   public boolean isEJB21()
   {
      return ejbVersion == 2 && ejbMinorVersion == 1;
   }

   public Iterator getEnterpriseBeans()
   {
      return beans.iterator();
   }

   /**
    * Get an EJB by its declared &lt;ejb-name&gt; tag
    *
    * @param ejbName EJB to return
    *
    * @return BeanMetaData pertaining to the given ejb-name,
    *   <code>null</code> if none found
    */
   public BeanMetaData getBeanByEjbName(String ejbName)
   {
      Iterator iterator = getEnterpriseBeans();
      while (iterator.hasNext())
      {
         BeanMetaData current = (BeanMetaData)iterator.next();
         if (current.getEjbName().equals(ejbName))
         {
            return current;
         }
      }

      // not found
      return null;
   }

   public String getConfigFile()
   {
      return configFile;
   }

   public void setConfigFile(String configFile)
   {
      this.configFile = configFile;
   }

   public String getConfigName()
   {
      return configName;
   }

   public void setConfigName(String configName)
   {
      this.configName = configName;
   }
   public String getWsdlPublishLocationByName(String name)
   {
      // if not found, the we will use default
      return (String)wsdlPublishLocationMap.get(name);
   }

   public String getWebServiceContextRoot()
   {
      return webServiceContextRoot;
   }

   public void setWebServiceContextRoot(String webServiceContextRoot)
   {
      this.webServiceContextRoot = webServiceContextRoot;
   }

   public boolean isWebServiceDeployment()
   {
      return webServiceDeployment;
   }

   public void setWebServiceDeployment(boolean webServiceDeployment)
   {
      this.webServiceDeployment = webServiceDeployment;
   }

   /**
    * Get the container managed relations in this application.
    * Items are instance of RelationMetaData.
    */
   public Iterator getRelationships()
   {
      return relationships.iterator();
   }

   public AssemblyDescriptorMetaData getAssemblyDescriptor()
   {
      return assemblyDescriptor;
   }

   public Iterator getConfigurations()
   {
      return configurations.values().iterator();
   }

   public ConfigurationMetaData getConfigurationMetaDataByName(String name)
   {
      return (ConfigurationMetaData)configurations.get(name);
   }

   public Iterator getInvokerProxyBindings()
   {
      return invokerBindings.values().iterator();
   }

   public InvokerProxyBindingMetaData getInvokerProxyBindingMetaDataByName(String name)
   {
      return (InvokerProxyBindingMetaData)invokerBindings.get(name);
   }

   public String getResourceByName(String name)
   {
      // if not found, the container will use default
      return (String)resources.get(name);
   }

   public void addPluginData(String pluginName, Object pluginData)
   {
      plugins.put(pluginName, pluginData);
   }

   public Object getPluginData(String pluginName)
   {
      return plugins.get(pluginName);
   }

   public String getJmxName()
   {
      return jmxName;
   }

   public String getSecurityDomain()
   {
      return securityDomain;
   }

   /**
    * Set the security domain for this web application
    */
   public void setSecurityDomain(String securityDomain)
   {
      this.securityDomain = securityDomain;
   }

   public String getUnauthenticatedPrincipal()
   {
      return unauthenticatedPrincipal;
   }

   public void setUnauthenticatedPrincipal(String unauthenticatedPrincipal)
   {
      this.unauthenticatedPrincipal = unauthenticatedPrincipal;
   }

   public boolean getEnforceEjbRestrictions()
   {
      return enforceEjbRestrictions;
   }

   public boolean isExcludeMissingMethods()
   {
      return excludeMissingMethods;
   }

   public MessageDestinationMetaData getMessageDestination(String name)
   {
      return assemblyDescriptor.getMessageDestinationMetaData(name);
   }

   public boolean getExceptionRollback()
   {
      return exceptionRollback;
   }

   /**
    * Import data provided by ejb-jar.xml
    *
    * @throws DeploymentException When there was an error encountered
    *         while parsing ejb-jar.xml
    */
   public void importEjbJarXml(Element element)
           throws DeploymentException
   {
      // EJB version is determined by the doc type that was used to
      // verify the ejb-jar.xml.
      DocumentType docType = element.getOwnerDocument().getDoctype();

      if (docType == null)
      {
         // test if this is a 2.1 schema-based descriptor
         if ("http://java.sun.com/xml/ns/j2ee".equals(element.getNamespaceURI()))
         {
            ejbVersion = 2;
            ejbMinorVersion = 1;
         }
         else
         {
            // No good, EJB 1.1/2.1 requires a DOCTYPE declaration
            throw new DeploymentException("ejb-jar.xml must either obey " +
                    "the right xml schema or define a valid DOCTYPE!");
         }
      }
      else
      {
         String publicId = docType.getPublicId();
         if (publicId == null)
         {
            // We need a public Id
            throw new DeploymentException("The DOCTYPE declaration in " +
                    "ejb-jar.xml must define a PUBLIC id");
         }

         // Check for a known public Id
         if (publicId.startsWith("-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0"))
         {
            ejbVersion = 2;
         }
         else if (publicId.startsWith("-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1.1"))
         {
            ejbVersion = 1;
         }
         else
         {
            // Unknown
            throw new DeploymentException("Unknown PUBLIC id in " +
                    "ejb-jar.xml: " + publicId);
         }
      }

      // find the beans
      Element enterpriseBeans = getUniqueChild(element, "enterprise-beans");

      // Entity Beans
      HashMap schemaNameMap = new HashMap();
      Iterator iterator = getChildrenByTagName(enterpriseBeans, "entity");
      while (iterator.hasNext())
      {
         Element currentEntity = (Element)iterator.next();
         EntityMetaData entityMetaData = new EntityMetaData(this);
         try
         {
            entityMetaData.importEjbJarXml(currentEntity);
         }
         catch (DeploymentException e)
         {
            throw new DeploymentException("Error in ejb-jar.xml " +
                    "for Entity Bean " + entityMetaData.getEjbName() + ": " +
                    e.getMessage());
         }

         // Ensure unique-ness of <abstract-schema-name>
         String abstractSchemaName = entityMetaData.getAbstractSchemaName();
         if (abstractSchemaName != null)
         {
            if (schemaNameMap.containsKey(abstractSchemaName))
            {
               //
               throw new DeploymentException(entityMetaData.getEjbName() +
                       ": Duplicate abstract-schema name '" + abstractSchemaName +
                       "'. Already defined for Entity '" +
                       ((EntityMetaData)schemaNameMap.get(abstractSchemaName)).getEjbName() + "'.");
            }
            schemaNameMap.put(abstractSchemaName, entityMetaData);
         }

         beans.add(entityMetaData);
      }

      // Session Beans
      iterator = getChildrenByTagName(enterpriseBeans, "session");
      while (iterator.hasNext())
      {
         Element currentSession = (Element)iterator.next();
         SessionMetaData sessionMetaData = new SessionMetaData(this);
         try
         {
            sessionMetaData.importEjbJarXml(currentSession);
         }
         catch (DeploymentException e)
         {
            throw new DeploymentException("Error in ejb-jar.xml for " +
                    "Session Bean " + sessionMetaData.getEjbName() + ": " +
                    e.getMessage());
         }
         beans.add(sessionMetaData);
      }

      // Message Driven Beans
      iterator = getChildrenByTagName(enterpriseBeans, "message-driven");
      while (iterator.hasNext())
      {
         Element currentMessageDriven = (Element)iterator.next();
         MessageDrivenMetaData messageDrivenMetaData =
                 new MessageDrivenMetaData(this);

         try
         {
            messageDrivenMetaData.importEjbJarXml(currentMessageDriven);
         }
         catch (DeploymentException e)
         {
            throw new DeploymentException("Error in ejb-jar.xml for " +
                    "Message Driven Bean " +
                    messageDrivenMetaData.getEjbName() + ": " + e.getMessage());
         }
         beans.add(messageDrivenMetaData);
      }

      // Enforce unique-ness of declared ejb-name Elements
      Set ejbNames = new HashSet();
      Iterator beanIt = beans.iterator();
      while (beanIt.hasNext())
      {
         BeanMetaData bmd = (BeanMetaData)beanIt.next();

         String beanName = bmd.getEjbName();
         if (ejbNames.contains(beanName))
         {
            throw new DeploymentException("Duplicate definition of an " +
                    "EJB with name '" + beanName + "'.");
         }

         ejbNames.add(beanName);
      }

      // Relationships
      Element relationshipsElement = getOptionalChild(element,
              "relationships");
      if (relationshipsElement != null)
      {
         // used to assure that a relationship name is not reused
         Set relationNames = new HashSet();

         iterator = getChildrenByTagName(relationshipsElement,
                 "ejb-relation");
         while (iterator.hasNext())
         {
            Element relationElement = (Element)iterator.next();
            RelationMetaData relationMetaData = new RelationMetaData();
            try
            {
               relationMetaData.importEjbJarXml(relationElement);
            }
            catch (DeploymentException e)
            {
               throw new DeploymentException("Error in ejb-jar.xml " +
                       "for relation " + relationMetaData.getRelationName() +
                       ": " + e.getMessage());
            }

            // if the relationship has a name, assure that it has not
            // already been used
            String relationName = relationMetaData.getRelationName();
            if (relationName != null)
            {
               if (relationNames.contains(relationName))
               {
                  throw new DeploymentException("ejb-relation-name must " +
                          "be unique in ejb-jar.xml file: ejb-relation-name is " +
                          relationName);
               }
               relationNames.add(relationName);
            }

            relationships.add(relationMetaData);
         }
      }

      // read the assembly descriptor (optional)
      Element descrElement = getOptionalChild(element, "assembly-descriptor");
      if (descrElement != null)
      {
         // set the security roles (optional)
         iterator = getChildrenByTagName(descrElement, "security-role");
         while (iterator.hasNext())
         {
            Element securityRole = (Element)iterator.next();
            try
            {
               String roleName = getElementContent(getUniqueChild(securityRole, "role-name"));
               SecurityRoleMetaData srMetaData = new SecurityRoleMetaData(roleName);
               assemblyDescriptor.addSecurityRoleMetaData(srMetaData);
            }
            catch (DeploymentException e)
            {
               throw new DeploymentException("Error in ejb-jar.xml " +
                       "for security-role: " + e.getMessage());
            }
         }

         // set the method permissions (optional)
         iterator = getChildrenByTagName(descrElement,
                 "method-permission");
         try
         {
            while (iterator.hasNext())
            {
               Element methodPermission = (Element)iterator.next();
               // Look for the unchecked element
               Element unchecked = getOptionalChild(methodPermission,
                       "unchecked");

               boolean isUnchecked = false;
               Set roles = null;
               if (unchecked != null)
               {
                  isUnchecked = true;
               }
               else
               {
                  // Get the role-name elements
                  roles = new HashSet();
                  Iterator rolesIterator = getChildrenByTagName(methodPermission, "role-name");
                  while (rolesIterator.hasNext())
                  {
                     roles.add(getElementContent((Element)rolesIterator.next()));
                  }
                  if (roles.size() == 0)
                     throw new DeploymentException("An unchecked " +
                             "element or one or more role-name elements " +
                             "must be specified in method-permission");
               }

               // find the methods
               Iterator methods = getChildrenByTagName(methodPermission,
                       "method");
               while (methods.hasNext())
               {
                  // load the method
                  MethodMetaData method = new MethodMetaData();
                  method.importEjbJarXml((Element)methods.next());
                  if (isUnchecked)
                  {
                     method.setUnchecked();
                  }
                  else
                  {
                     method.setRoles(roles);
                  }

                  // give the method to the right bean
                  BeanMetaData bean = getBeanByEjbName(method.getEjbName());
                  if (bean == null)
                  {
                     throw new DeploymentException(method.getEjbName() +
                             " doesn't exist");
                  }
                  bean.addPermissionMethod(method);
               }
            }
         }
         catch (DeploymentException e)
         {
            throw new DeploymentException("Error in ejb-jar.xml, " +
                    "in method-permission: " + e.getMessage());
         }

         // set the container transactions (optional)
         iterator = getChildrenByTagName(descrElement,
                 "container-transaction");
         try
         {
            while (iterator.hasNext())
            {
               Element containerTransaction = (Element)iterator.next();

               // find the type of the transaction
               byte transactionType;
               String type = getElementContent(getUniqueChild(containerTransaction, "trans-attribute"));

               if (type.equalsIgnoreCase("NotSupported") ||
                       type.equalsIgnoreCase("Not_Supported"))
               {
                  transactionType = TX_NOT_SUPPORTED;
               }
               else if (type.equalsIgnoreCase("Supports"))
               {
                  transactionType = TX_SUPPORTS;
               }
               else if (type.equalsIgnoreCase("Required"))
               {
                  transactionType = TX_REQUIRED;
               }
               else if (type.equalsIgnoreCase("RequiresNew") ||
                       type.equalsIgnoreCase("Requires_New"))
               {
                  transactionType = TX_REQUIRES_NEW;
               }
               else if (type.equalsIgnoreCase("Mandatory"))
               {
                  transactionType = TX_MANDATORY;
               }
               else if (type.equalsIgnoreCase("Never"))
               {
                  transactionType = TX_NEVER;
               }
               else
               {
                  throw new DeploymentException("invalid " +
                          "<transaction-attribute> : " + type);
               }

               // find the methods
               Iterator methods = getChildrenByTagName(containerTransaction, "method");
               while (methods.hasNext())
               {
                  // load the method
                  MethodMetaData method = new MethodMetaData();
                  method.importEjbJarXml((Element)methods.next());
                  method.setTransactionType(transactionType);

                  // give the method to the right bean
                  BeanMetaData bean = getBeanByEjbName(method.getEjbName());
                  if (bean == null)
                  {
                     throw new DeploymentException("bean " +
                             method.getEjbName() + " doesn't exist");
                  }
                  bean.addTransactionMethod(method);
               }
            }
         }
         catch (DeploymentException e)
         {
            throw new DeploymentException("Error in ejb-jar.xml, " +
                    "in <container-transaction>: " + e.getMessage());
         }

         // Get the exclude-list methods
         Element excludeList = getOptionalChild(descrElement,
                 "exclude-list");
         if (excludeList != null)
         {
            iterator = getChildrenByTagName(excludeList, "method");
            while (iterator.hasNext())
            {
               Element methodInf = (Element)iterator.next();
               // load the method
               MethodMetaData method = new MethodMetaData();
               method.importEjbJarXml(methodInf);
               method.setExcluded();

               // give the method to the right bean
               BeanMetaData bean = getBeanByEjbName(method.getEjbName());
               if (bean == null)
               {
                  throw new DeploymentException("bean " +
                          method.getEjbName() + " doesn't exist");
               }
               bean.addExcludedMethod(method);
            }
         }

         // set the message destinations (optional)
         iterator = getChildrenByTagName(descrElement, "message-destination");
         while (iterator.hasNext())
         {
            Element messageDestination = (Element)iterator.next();
            try
            {
               MessageDestinationMetaData messageDestinationMetaData = new MessageDestinationMetaData();
               messageDestinationMetaData.importEjbJarXml(messageDestination);
               assemblyDescriptor.addMessageDestinationMetaData(messageDestinationMetaData);
            }
            catch (Throwable t)
            {
               throw new DeploymentException("Error in ejb-jar.xml " +
                       "for message destination: " + t.getMessage());
            }
         }
      }
   }

   public void importJbossXml(Element element)
           throws DeploymentException
   {
      Iterator iterator;

      // all the tags are optional

      // Get the enforce-ejb-restrictions
      Element enforce = getOptionalChild(element, "enforce-ejb-restrictions");
      if (enforce != null)
      {
         String tmp = getElementContent(enforce);
         enforceEjbRestrictions = Boolean.valueOf(tmp).booleanValue();
      }

      // Get any user defined JMX name
      Element jmxNameElement = getOptionalChild(element,
              "jmx-name");
      if (jmxNameElement != null)
      {
         jmxName = getElementContent(jmxNameElement);
      }

      // Throw an exception when marked rollback with no exception thrown
      exceptionRollback = MetaData.getOptionalChildBooleanContent(element, "exception-on-rollback", false);

      // Get the security domain name
      Element securityDomainElement = getOptionalChild(element,
              "security-domain");
      if (securityDomainElement != null)
      {
         securityDomain = getElementContent(securityDomainElement);
      }

      // Get the missing-method-permissions-excluded-mode flag
      excludeMissingMethods = MetaData.getOptionalChildBooleanContent(element,
              "missing-method-permissions-excluded-mode", true);

      // Get the unauthenticated-principal name
      Element unauth = getOptionalChild(element,
              "unauthenticated-principal");
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
            unauthenticatedPrincipal = (String)server.getAttribute(oname, "DefaultUnauthenticatedPrincipal");
         }
         catch (Exception e)
         {
            log.error("Cannot obtain unauthenticated principal");
         }
      }

      // find the invoker configurations
      Element invokerConfs = getOptionalChild(element,
              "invoker-proxy-bindings");
      if (invokerConfs != null)
      {
         iterator = getChildrenByTagName(invokerConfs,
                 "invoker-proxy-binding");

         while (iterator.hasNext())
         {
            Element invoker = (Element)iterator.next();
            String invokerName = getElementContent(getUniqueChild(invoker, "name"));

            // find the configuration if it has already been defined
            // (allow jboss.xml to modify a standard conf)
            InvokerProxyBindingMetaData invokerMetaData =
                    getInvokerProxyBindingMetaDataByName(invokerName);

            // create it if necessary
            if (invokerMetaData == null)
            {
               invokerMetaData = new InvokerProxyBindingMetaData(invokerName);
               invokerBindings.put(invokerName, invokerMetaData);
            }

            try
            {
               invokerMetaData.importJbossXml(invoker);
            }
            catch (DeploymentException e)
            {
               throw new DeploymentException("Error in jboss.xml " +
                       "for invoker-proxy-binding " + invokerMetaData.getName() +
                       ": " + e.getMessage());
            }
         }
      }

      // find the container configurations (we need them first to use
      // them in the beans)
      Element confs = getOptionalChild(element, "container-configurations");
      if (confs != null)
      {
         iterator = getChildrenByTagName(confs, "container-configuration");

         while (iterator.hasNext())
         {
            Element conf = (Element)iterator.next();
            String confName = getElementContent(getUniqueChild(conf,
                    "container-name"));
            String parentConfName = conf.getAttribute("extends");
            if (parentConfName != null && parentConfName.trim().length() == 0)
            {
               parentConfName = null;
            }

            // Allow the configuration to inherit from a standard
            // configuration. This is determined by looking for a
            // configuration matching the name given by the extends
            // attribute, or if extends was not specified, an
            // existing configuration with the same.
            ConfigurationMetaData configurationMetaData = null;
            if (parentConfName != null)
            {
               configurationMetaData = getConfigurationMetaDataByName(parentConfName);
               if (configurationMetaData == null)
               {
                  throw new DeploymentException("Failed to find " +
                          "parent config=" + parentConfName);
               }

               // Make a copy of the existing configuration
               configurationMetaData =
                       (ConfigurationMetaData)configurationMetaData.clone();
               configurations.put(confName, configurationMetaData);
            }

            if (configurationMetaData == null)
            {
               configurationMetaData =
                       getConfigurationMetaDataByName(confName);
            }

            // Create a new configuration if none was found
            if (configurationMetaData == null)
            {
               configurationMetaData = new ConfigurationMetaData(confName);
               configurations.put(confName, configurationMetaData);
            }

            try
            {
               configurationMetaData.importJbossXml(conf);
            }
            catch (DeploymentException e)
            {
               throw new DeploymentException("Error in jboss.xml " +
                       "for container-configuration " +
                       configurationMetaData.getName() + ": " + e.getMessage());
            }
         }
      }

      // webservice meta data that are common to all EJB endpoints
      Element webservices = getOptionalChild(element, "webservices");
      if (webservices != null)
      {
         // <context-root>
         Element contextRoot = getOptionalChild(webservices, "context-root");
         if (contextRoot != null)
         {
            webServiceContextRoot = getElementContent(contextRoot);
            if (webServiceContextRoot.charAt(0) != '/')
               webServiceContextRoot = "/" + webServiceContextRoot;
         }
         
         // <webservice-description>
         iterator = getChildrenByTagName(webservices, "webservice-description");
         while (iterator.hasNext())
         {
            Element wsd = (Element)iterator.next();
            String wsdName = getElementContent(getUniqueChild(wsd, "webservice-description-name"));
            configName = MetaData.getOptionalChildContent(wsd, "config-name");
            configFile = MetaData.getOptionalChildContent(wsd, "config-file");
            String wsdlPublishLocation = getOptionalChildContent(wsd, "wsdl-publish-location");
            wsdlPublishLocationMap.put(wsdName, wsdlPublishLocation);
         }
      }
      
      // update the enterprise beans
      Element entBeans = getOptionalChild(element, "enterprise-beans");
      if (entBeans != null)
      {
         String ejbName = null;
         try
         {
            // Entity Beans
            iterator = getChildrenByTagName(entBeans, "entity");
            while (iterator.hasNext())
            {
               Element bean = (Element)iterator.next();
               ejbName = getElementContent(getUniqueChild(bean, "ejb-name"));
               BeanMetaData beanMetaData = getBeanByEjbName(ejbName);
               if (beanMetaData == null)
               {
                  throw new DeploymentException("found in jboss.xml " +
                          "but not in ejb-jar.xml");
               }
               beanMetaData.importJbossXml(bean);
            }

            // Session Beans
            iterator = getChildrenByTagName(entBeans, "session");
            while (iterator.hasNext())
            {
               Element bean = (Element)iterator.next();
               ejbName = getElementContent(getUniqueChild(bean, "ejb-name"));
               BeanMetaData beanMetaData = getBeanByEjbName(ejbName);
               if (beanMetaData == null)
               {
                  throw new DeploymentException("found in jboss.xml " +
                          "but not in ejb-jar.xml");
               }
               beanMetaData.importJbossXml(bean);
            }

            // Message Driven Beans
            iterator = getChildrenByTagName(entBeans, "message-driven");
            while (iterator.hasNext())
            {
               Element bean = (Element)iterator.next();
               ejbName = getElementContent(getUniqueChild(bean, "ejb-name"));
               BeanMetaData beanMetaData = getBeanByEjbName(ejbName);
               if (beanMetaData == null)
               {
                  throw new DeploymentException("found in jboss.xml " +
                          "but not in ejb-jar.xml");
               }
               beanMetaData.importJbossXml(bean);
            }
         }
         catch (DeploymentException e)
         {
            throw new DeploymentException("Error in jboss.xml for " +
                    "Bean " + ejbName + ": " + e.getMessage());
         }
      }

      // read the assembly descriptor (optional)
      Element descrElement = getOptionalChild(element, "assembly-descriptor");
      if (descrElement != null)
      {
         // set the security roles (optional)
         iterator = getChildrenByTagName(descrElement, "security-role");
         while (iterator.hasNext())
         {
            Element securityRole = (Element)iterator.next();
            String roleName = getElementContent(getUniqueChild(securityRole, "role-name"));
            SecurityRoleMetaData securityRoleMetaData = assemblyDescriptor.getSecurityRoleByName(roleName);
            if (securityRoleMetaData == null)
            {
               // Create a new SecurityRoleMetaData
               securityRoleMetaData = new SecurityRoleMetaData(roleName);
               assemblyDescriptor.addSecurityRoleMetaData(securityRoleMetaData);
            }

            Iterator itPrincipalNames = getChildrenByTagName(securityRole, "principal-name");
            while (itPrincipalNames.hasNext())
            {
               String principalName = getElementContent((Element)itPrincipalNames.next());
               securityRoleMetaData.addPrincipalName(principalName);
            }
         }

         // set the message destinations (optional)
         iterator = getChildrenByTagName(descrElement, "message-destination");
         while (iterator.hasNext())
         {
            Element messageDestination = (Element)iterator.next();
            try
            {
               String messageDestinationName = getUniqueChildContent(messageDestination, "message-destination-name");
               MessageDestinationMetaData messageDestinationMetaData = getMessageDestination(messageDestinationName);
               if (messageDestinationMetaData == null)
                  throw new DeploymentException("message-destination " + messageDestinationName + " found in jboss.xml but not in ejb-jar.xml");
               messageDestinationMetaData.importJbossXml(messageDestination);
            }
            catch (Throwable t)
            {
               throw new DeploymentException("Error in ejb-jar.xml " +
                       "for message destination: " + t.getMessage());
            }
         }
      }

      // set the resource managers
      Element resmans = getOptionalChild(element, "resource-managers");
      if (resmans != null)
      {
         iterator = getChildrenByTagName(resmans, "resource-manager");
         try
         {
            while (iterator.hasNext())
            {
               Element resourceManager = (Element)iterator.next();
               String resName = getElementContent(getUniqueChild(resourceManager, "res-name"));

               String jndi = getElementContent(getOptionalChild(resourceManager, "res-jndi-name"));

               String url = getElementContent(getOptionalChild(resourceManager, "res-url"));

               if (jndi != null && url == null)
               {
                  resources.put(resName, jndi);
               }
               else if (jndi == null && url != null)
               {
                  resources.put(resName, url);
               }
               else
               {
                  throw new DeploymentException(resName +
                          " : expected res-url or res-jndi-name tag");
               }
            }
         }
         catch (DeploymentException e)
         {
            throw new DeploymentException("Error in jboss.xml, in " +
                    "resource-manager: " + e.getMessage());
         }
      }
   }

}

/*
vim:ts=3:sw=3:et
*/
