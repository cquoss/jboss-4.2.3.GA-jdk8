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

import java.net.URL;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;

/**
 * ServiceBindingManager MBean interface
 * 
 * @author <a href="mailto:bitpushr@rochester.rr.com">Mike Finn</a>
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 57210 $
 */
public interface ServiceBindingManagerMBean extends org.jboss.system.ServiceBinding
{
   /** Default ObjectName */
   static final ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.system:service=ServiceBindingManager");
   
   // Attributes ----------------------------------------------------

   /**
    * The name of the server this manager is associated with. This is a
    * logical name used to lookup ServiceConfigs from the ServicesStore.
    */   
   void setServerName(String serverName);
   String getServerName();

   /**
    * The name of the class implementation of the ServicesStoreFatory. The
    * default value is org.jboss.services.binding.XMLServicesStoreFactory.
    */
   void setStoreFactoryClassName(String storeFactoryClassName);
   String getStoreFactoryClassName();
   
   /**
    * The URL of the configuration store.
    */
   void setStoreURL(URL storeURL);   
   URL getStoreURL();

   /**
    * The ObjectName of the ServiceController,
    * defaults to ServiceControllerMBean.OBJECT_NAME
    */
   void setServiceController(ObjectName serviceController) throws Exception;
   ObjectName getServiceController();
   
   // Operations ----------------------------------------------------

   /**
    * Looks up the service config for the given service,
    * using the server name bound to this mbean.
    * 
    * @param serviceName the JMX ObjectName of the service
    * @return ServiceConfig instance if found, null otherwise 
    */
   ServiceConfig getServiceConfig(ObjectName serviceName) throws Exception;

}
