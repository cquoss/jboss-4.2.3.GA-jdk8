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

// $Id: ClientMetaData.java 65717 2007-10-01 17:01:38Z thomas.diesler@jboss.com $

import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.serviceref.ServiceRefDelegate;
import org.jboss.wsf.spi.serviceref.ServiceRefMetaData;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.net.URLClassLoader;

/** The metdata data from a j2ee application-client.xml descriptor
 * 
 * @author Scott.Stark@jboss.org
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 65717 $
 */
public class ClientMetaData
{
   /** The application-client/display-name */
   private String displayName;
   /** The location for the server side client context ENC bindings */
   private String jndiName;
   /** An ArrayList<EnvEntryMetaData> for the env-entry element(s) */
   private ArrayList environmentEntries = new ArrayList();
   /** A HashMap<String, EjbRefMetaData> for the ejb-ref element(s) */
   private HashMap ejbReferences = new HashMap();
   /** The HashMap<String, ServiceRefMetaData> service-ref element(s) info */
   private HashMap<String,ServiceRefMetaData> serviceReferences = new HashMap<String,ServiceRefMetaData>();
   /** A  HashMap<String, ResourceRefMetaData> resource-ref element(s) info */
   private HashMap resourceReferences = new HashMap();
   /** A  HashMap<String, ResourceEnvRefMetaData> resource-env-ref element(s) info */
   private HashMap resourceEnvReferences = new HashMap();
   /** A  HashMap<String, ArrayList<ResourceEnvRefMetaData>> of
    * message-destination-ref that resolve to a jndi-name via a message-destination
    * via a message-destination-link
    */
   private HashMap resourceEnvReferenceLinks = new HashMap();
   /** The JAAS callback handler */
   private String callbackHandler;

   /** The ClassLoader to load additional resources */
   private URLClassLoader resourceCl;

   /** Set the ClassLoader to load additional resources */
   public void setResourceClassLoader(URLClassLoader resourceCl)
   {
      this.resourceCl = resourceCl;
   }

   /** The application-client/display-name
    * @return application-client/display-name value
    */ 
   public String getDisplayName()
   {
      return displayName;
   }

   /** The location for the server side client context ENC bindings
    * @return the JNDI name for the server side client context ENC bindings. This
    * is either the jboss-client/jndi-name or the application-client/display-name
    * value.
    */ 
   public String getJndiName()
   {
      String name = jndiName;
      if( name == null )
         name = displayName;
      return name;
   }

   /**
    * @return ArrayList<EnvEntryMetaData>
    */ 
   public ArrayList getEnvironmentEntries()
   {
      return environmentEntries;
   }
   /**
    * @return HashMap<EjbRefMetaData>
    */ 
   public HashMap getEjbReferences()
   {
      return ejbReferences;
   }
   /**
    * @return HashMap<ResourceRefMetaData>
    */ 
   public HashMap getResourceReferences()
   {
      return resourceReferences;
   }
   /**
    * @return HashMap<ResourceEnvRefMetaData>
    */
   public HashMap getResourceEnvReferences()
   {
      return resourceEnvReferences;
   }
   /** 
    * @return The CallbackHandler if defined, null otherwise
    */ 
   public String getCallbackHandler()
   {
      return callbackHandler;
   }
   /**
    * @return HashMap<ServiceRefMetaData>
    */
   public HashMap<String,ServiceRefMetaData> getServiceReferences()
   {
      return serviceReferences;
   }

   public void importClientXml(Element element)
      throws DeploymentException
   {
      displayName = MetaData.getOptionalChildContent(element, "display-name");

      // set the environment entries
      Iterator iterator = MetaData.getChildrenByTagName(element, "env-entry");

      while (iterator.hasNext())
      {
         Element envEntry = (Element) iterator.next();

         EnvEntryMetaData envEntryMetaData = new EnvEntryMetaData();
         envEntryMetaData.importEjbJarXml(envEntry);

         environmentEntries.add(envEntryMetaData);
      }

      // set the ejb references
      iterator = MetaData.getChildrenByTagName(element, "ejb-ref");

      while (iterator.hasNext())
      {
         Element ejbRef = (Element) iterator.next();

         EjbRefMetaData ejbRefMetaData = new EjbRefMetaData();
         ejbRefMetaData.importEjbJarXml(ejbRef);

         ejbReferences.put(ejbRefMetaData.getName(), ejbRefMetaData);
      }

      // Parse the service-ref elements
      iterator = MetaData.getChildrenByTagName(element, "service-ref");
      while (iterator.hasNext())
      {
         Element serviceRef = (Element) iterator.next();
         ServiceRefMetaData refMetaData = new ServiceRefDelegate().newServiceRefMetaData();
         refMetaData.importStandardXml(serviceRef);
         serviceReferences.put(refMetaData.getServiceRefName(), refMetaData);
      }

      // The callback-handler element
      Element callbackElement = MetaData.getOptionalChild(element,
         "callback-handler");
      if (callbackElement != null)
      {
         callbackHandler = MetaData.getElementContent(callbackElement);
      }

      // set the resource references
      iterator = MetaData.getChildrenByTagName(element, "resource-ref");
      while (iterator.hasNext())
      {
         Element resourceRef = (Element) iterator.next();

         ResourceRefMetaData resourceRefMetaData = new ResourceRefMetaData();
         resourceRefMetaData.importEjbJarXml(resourceRef);

         resourceReferences.put(resourceRefMetaData.getRefName(),
            resourceRefMetaData);
      }

      // Parse the resource-env-ref elements
      iterator = MetaData.getChildrenByTagName(element, "resource-env-ref");
      while (iterator.hasNext())
      {
         Element resourceRef = (Element) iterator.next();
         ResourceEnvRefMetaData refMetaData = new ResourceEnvRefMetaData();
         refMetaData.importEjbJarXml(resourceRef);
         resourceEnvReferences.put(refMetaData.getRefName(), refMetaData);
      }

      // Parse the message-destination-ref elements
      iterator = MetaData.getChildrenByTagName(element, "message-destination-ref");
      while (iterator.hasNext())
      {
         Element resourceRef = (Element) iterator.next();
         ResourceEnvRefMetaData refMetaData = new ResourceEnvRefMetaData();
         refMetaData.importEjbJarXml(resourceRef);
         /* A message-destination-ref is linked to a jndi-name either via
         the message-destination-ref/message-destination-ref-name mapping to
         a jboss resource-env-ref/resource-env-ref-name if there is no
         message-destination-link, or by the message-destination-link ->
         message-destination/message-destination-name mapping to a jboss
         resource-env-ref/resource-env-ref-name.
         */
         String refName = refMetaData.getRefName();
         String link = refMetaData.getLink();
         if( link != null )
         {
            ArrayList linkedRefs = (ArrayList) resourceEnvReferenceLinks.get(link);
            if( linkedRefs == null )
            {
               linkedRefs = new ArrayList();
               resourceEnvReferenceLinks.put(link, linkedRefs);
            }
            linkedRefs.add(refMetaData);
         }
         resourceEnvReferences.put(refName, refMetaData);            
      }
   }

   public void importJbossClientXml(Element element) throws DeploymentException
   {
      jndiName = MetaData.getOptionalChildContent(element, "jndi-name");

      // Get the JNDI names of ejb-refs
      Iterator iterator = MetaData.getChildrenByTagName(element, "ejb-ref");
      while (iterator.hasNext())
      {
         Element ejbRef = (Element) iterator.next();
         String ejbRefName = MetaData.getElementContent(
            MetaData.getUniqueChild(ejbRef, "ejb-ref-name"));
         EjbRefMetaData ejbRefMetaData = (EjbRefMetaData) ejbReferences.get(ejbRefName);
         if (ejbRefMetaData == null)
         {
            throw new DeploymentException("ejb-ref " + ejbRefName
               + " found in jboss-client.xml but not in application-client.xml");
         }
         ejbRefMetaData.importJbossXml(ejbRef);
      }

      // Parse the service-ref elements
      iterator = MetaData.getChildrenByTagName(element, "service-ref");
      while (iterator.hasNext())
      {
         Element serviceRef = (Element) iterator.next();
         String serviceRefName = MetaData.getUniqueChildContent(serviceRef, "service-ref-name");
         ServiceRefMetaData refMetaData = (ServiceRefMetaData)serviceReferences.get(serviceRefName);
         if (refMetaData == null)
         {
            throw new DeploymentException("service-ref " + serviceRefName
               + " found in jboss-client.xml but not in application-client.xml");
         }
         refMetaData.importJBossXml(serviceRef);
      }

      // Get the JNDI name binding for resource-refs
      iterator = MetaData.getChildrenByTagName(element, "resource-ref");
      while (iterator.hasNext())
      {
         Element resourceRef = (Element) iterator.next();
         String resRefName = MetaData.getElementContent(
            MetaData.getUniqueChild(resourceRef, "res-ref-name"));
         ResourceRefMetaData resourceRefMetaData =
            (ResourceRefMetaData) resourceReferences.get(resRefName);
         if (resourceRefMetaData == null)
         {
            throw new DeploymentException("resource-ref " + resRefName
               + " found in jboss-client.xml but not in application-client.xml");
         }
         resourceRefMetaData.importJbossXml(resourceRef);
      }

      // Get the JNDI name binding resource-env-refs
      iterator = MetaData.getChildrenByTagName(element, "resource-env-ref");
      while (iterator.hasNext())
      {
         Element resourceRef = (Element) iterator.next();
         String resRefName = MetaData.getElementContent(
            MetaData.getUniqueChild(resourceRef, "resource-env-ref-name"));
         ResourceEnvRefMetaData refMetaData =
            (ResourceEnvRefMetaData) resourceEnvReferences.get(resRefName);
         if (refMetaData == null)
         {
            // Try the resourceEnvReferenceLinks
            ArrayList linkedRefs = (ArrayList) resourceEnvReferenceLinks.get(resRefName);
            if( linkedRefs != null )
            {
               for(int n = 0; n < linkedRefs.size(); n ++)
               {
                  refMetaData = (ResourceEnvRefMetaData) linkedRefs.get(n);
                  refMetaData.importJbossXml(resourceRef);
               }
            }
            else
            {
               throw new DeploymentException("resource-env-ref " + resRefName
                  + " found in jboss-client.xml but not in application-client.xml");
            }
         }
         else
         {
            refMetaData.importJbossXml(resourceRef);
         }
      }
   }
}
