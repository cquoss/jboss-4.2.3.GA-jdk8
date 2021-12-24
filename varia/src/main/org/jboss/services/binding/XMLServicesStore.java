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
package org.jboss.services.binding;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.management.ObjectName;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.jboss.util.StringPropertyReplacer;
import org.jboss.logging.Logger;

/**
 * XML implementation of ServicesStore.
 *
 * <p>Reads/writes/manages the XML config file for the ServiceBinding Manager module
 *
 * @author  <a href="mailto:bitpushr@rochester.rr.com">Mike Finn</a>.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57210 $
 */
public class XMLServicesStore implements ServicesStore
{
   private final Logger log = Logger.getLogger(getClass());

   /** A thread-safe map of Map<ObjectName,ServiceConfig> keyed by server name.
    */
   private Map servers = Collections.synchronizedMap(new HashMap());

   /** This method is not usable in this implementation as XMLServiceStore is read-only
    *
    * @param serverName
    * @param serviceName
    * @param config
    * @exception DuplicateServiceException never thrown
    * @exception UnsupportedOperationException("XMLServiceStore is read-only") always thrown
    */
   public void addService(String serverName, ObjectName serviceName, ServiceConfig config)
      throws DuplicateServiceException
   {
      throw new UnsupportedOperationException("XMLServiceStore is read-only");
   }

   /**
    *  Looks up a service, by server name and service name.
    *  If the server or service does not exist, a null object is returned.
    *
    *  @param serverName The name of the server in the config file
    *  @param serviceName The name of the service (i.e. the JMX object name)
    *
    *  @returns ServiceConfig object. Null if server or service is not found.
    */
   public ServiceConfig getService(String serverName, ObjectName serviceName)
   {
      Map serverMap = (Map) this.servers.get(serverName);
      ServiceConfig config = null;
      if( serverMap != null )
      {
         config = (ServiceConfig) serverMap.get(serviceName);
      }
      return config;
   }

   /** This method is not usable in this implementation as XMLServiceStore is read-only
    *
    * @param serverName
    * @param serviceName
    * @exception UnsupportedOperationException("XMLServiceStore is read-only") always thrown
    */
   public void removeService(String serverName, ObjectName serviceName)
   {
      throw new UnsupportedOperationException("XMLServiceStore is read-only");
   }

   /** Loads XML config file into memory and parses it into ServiceConfig
    * objects.
    *
    * @throws Exception on any parse error
    */
   public void load(URL cfgURL)
      throws Exception
   {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder parser = factory.newDocumentBuilder();
      InputStream cfgIS = cfgURL.openStream();
      Document configDoc = parser.parse(cfgIS, cfgURL.toString());
      Element serviceBindings = configDoc.getDocumentElement();
      NodeList servers = serviceBindings.getElementsByTagName("server");
      int length = servers.getLength();
      for(int s = 0; s < length; s ++)
      {
         Element server = (Element) servers.item(s);
         parseServer(server);
      }
   }

   /** Store is a noop as this is a read-only store
    */
   public void store(URL cfgURL)
      throws Exception
   {
   }

   /** Parse /service-bindings/server/service-config element into
    a Map<ObjectName,ServiceConfig> objects that are stored in the servers
    map keyed by server.name.
    */
   private void parseServer(Element server)
      throws Exception
   {
      String serverName = server.getAttribute("name");
      HashMap serverConfigurations = new HashMap();
      NodeList serviceConfigs = server.getElementsByTagName("service-config");
      int length = serviceConfigs.getLength();
      for(int c = 0; c < length; c ++)
      {
         Element config = (Element) serviceConfigs.item(c);
         ServiceConfig serviceConfig = new ServiceConfig();
         ObjectName serviceObjectName = parseConfig(config, serviceConfig);
         serverConfigurations.put(serviceObjectName, serviceConfig);
      }
      this.servers.put(serverName, serverConfigurations);
   }

   /** Parse /service-bindings/server/service-config element into
    the given ServiceConfig object.
    */
   private ObjectName parseConfig(Element config, ServiceConfig serviceConfig)
      throws Exception
   {
      String serviceName = config.getAttribute("name");
      ObjectName serviceObjectName = new ObjectName(serviceName);
      serviceConfig.setServiceName(serviceName);

      // Parse the delegate info
      String delegateClass = config.getAttribute("delegateClass");
      if( delegateClass.length() == 0 )
         delegateClass = "org.jboss.services.binding.AttributeMappingDelegate";
      Element delegateConfig = null;
      NodeList delegateConfigs = config.getElementsByTagName("delegate-config");
      if( delegateConfigs.getLength() > 0 )
         delegateConfig = (Element) delegateConfigs.item(0);
      serviceConfig.setServiceConfigDelegateClassName(delegateClass);
      serviceConfig.setServiceConfigDelegateConfig(delegateConfig);

      // Parse the service bindings
      ArrayList bindingsArray = new ArrayList();
      NodeList bindings = config.getElementsByTagName("binding");
      int length = bindings.getLength();
      for(int b = 0; b < length; b ++)
      {
         Element binding = (Element) bindings.item(b);
         ServiceBinding sb = parseBinding(binding);
         bindingsArray.add(sb);
      }
      ServiceBinding[] tmp = new ServiceBinding[bindingsArray.size()];
      bindingsArray.toArray(tmp);
      serviceConfig.setBindings(tmp);
      return serviceObjectName;
   }

   /** Parse /service-bindings/server/service-config/binding element into
    a ServiceBinding object. Any attributes whose value contains a system
    property reference of the form ${x} will be replaced with the correcsponding
    System.getProperty("x") value if one exists.
    */
   private ServiceBinding parseBinding(Element binding)
      throws Exception
   {
      String name = binding.getAttribute("name");
      if (name != null)
      {
         name = StringPropertyReplacer.replaceProperties(name);
      }
      String hostName = binding.getAttribute("host");
      if (hostName != null)
      {
         hostName = StringPropertyReplacer.replaceProperties(hostName);
      }
      if (hostName.length() == 0)
         hostName = null;
      String portStr = binding.getAttribute("port");
      if (portStr != null)
      {
         portStr = StringPropertyReplacer.replaceProperties(portStr);
      }
      if (portStr.length() == 0)
         portStr = "0";
      log.debug("parseBinding, name='" + name + "', host='" + hostName + "'"
         + ", port='" + portStr + "'");
      int port = Integer.parseInt(portStr);
      ServiceBinding sb = new ServiceBinding(name, hostName, port);
      return sb;
   }
}
