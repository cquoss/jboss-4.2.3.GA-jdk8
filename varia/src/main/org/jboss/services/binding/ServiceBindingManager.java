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

import javax.management.Attribute;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.system.server.ServerConfigLocator;

/** The services configuration binding manager mbean implementation.
 *
 * <p>The ServiceBindingManager enables the centralized management
 * of ports, by service. The port configuration store is abstracted out
 * using the ServicesStore and ServicesStoreFactory interfaces. Note that
 * this class does not implement the JBoss services lifecycle methods
 * and hook its behavior off of those because this service is used to
 * configure other services before any of the lifecycle methods are invoked.
 *
 * @version $Revision: 57210 $
 * @author  <a href="mailto:bitpushr@rochester.rr.com">Mike Finn</a>
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 *
 * @jmx:mbean
 */
public class ServiceBindingManager
   implements MBeanRegistration, ServiceBindingManagerMBean
{
   private static Logger log = Logger.getLogger(ServiceBindingManager.class);

   /** The MBeanServer we are registered
    */
   private MBeanServer server;
   
   /** The ObjectName of this MBean
    */
   private ObjectName serviceName;
   
   /** The name of the server this manager is associated with. This is a
    logical name used to lookup ServiceConfigs from the ServicesStore.
    */
   private String serverName;
   
   /** The name of the class implementation for the ServicesStoreFactory. The
    default value is org.jboss.services.binding.XMLServicesStoreFactory
    */
   private String storeFactoryClassName = "org.jboss.services.binding.XMLServicesStoreFactory";
   
   /** The ObjectName of the ServiceController we'll be registering at
    */
   private ObjectName serviceController;
   
   /** The ServiceConfig store instance
    */
   private ServicesStore store;
   
   /** The URL of the configuration store
    */
   private URL storeURL;

   // Attributes ----------------------------------------------------
   
   /**
    * @jmx:managed-attribute
    */
   public String getServerName()
   {
      return this.serverName;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setServerName(String serverName)
   {
      this.serverName = serverName;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getStoreFactoryClassName()
   {
      return this.storeFactoryClassName;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setStoreFactoryClassName(String storeFactoryClassName)
   {
      this.storeFactoryClassName = storeFactoryClassName;
   }

   /**
    * @jmx:managed-attribute
    */
   public URL getStoreURL()
   {
      return storeURL;
   }

   /** Set the string representation of the store URL
    * @jmx:managed-attribute
    */
   public void setStoreURL(URL storeURL)
   {
      this.storeURL = storeURL;
   }

   /**
    * Get the ObjectName of the ServiceController
    * @jmx:managed-attribute
    */
   public ObjectName getServiceController()
   {
      return serviceController;
   }
   
   /**
    * Set the ObjectName of the ServiceController.
    * 
    * @jmx:managed-attribute
    * 
    * @param serviceController the name of the controller to register to
    * @throws Exception if there is a problem registering with the controller
    */
   public void setServiceController(ObjectName serviceController) throws Exception
   {
      this.serviceController = serviceController;
      
      // try to register a proxy with the controller
      registerProxy();
   }
   
   // MBeanRegistration overrides -----------------------------------
   
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      this.server = server;
      this.serviceName = name;
      
      return name;
   }
   
   public void postRegister(Boolean registrationDone)
   {
      if (registrationDone.booleanValue())
      {
         // Register with the default ServiceController
         try
         {
            setServiceController(ServiceControllerMBean.OBJECT_NAME);
         }
         catch (Exception ignore)
         {
            // Ignore any exception at this point; the user
            // might expicitly set the controller objectname
         }
      }
   }
   
   public void preDeregister()
      throws Exception
   {
      // unregister with ServiceController
      this.unregisterProxy();
      
      // shutdown store
      if (store != null)
      {
         store.store(storeURL);
      }
   }
   
   public void postDeregister()
   {
      // empty
   }

   // Operations ----------------------------------------------------
   
   /**
    * Looks up the service config for the given service using the
    * server name bound to this mbean.
    *
    * @jmx:managed-operation
    *
    * @param  serviceName  the JMX ObjectName of the service
    * @return ServiceConfig instance if found, null otherwise
    */
   public ServiceConfig getServiceConfig(ObjectName serviceName)
      throws Exception
   {
      log.debug("getServiceConfig, server:" + serverName + ", serviceName:" + serviceName);      
      
      if (store == null)
      {
         initStore();
      }

      // Look up the service config
      ServiceConfig svcConfig = store.getService(serverName, serviceName);
      ServiceConfig configCopy = null;
      if (svcConfig != null)
      {
         configCopy = (ServiceConfig) svcConfig.clone();
      }
      return configCopy;
   }

   /**
    * Looks up the service config for the requested service using the
    * server name bound to this mbean and invokes the configuration delegate
    * to apply the bindings to the service. If no config if found then this
    * method is a noop.
    *
    * @jmx:managed-operation
    *
    * @param  serviceName  the JMX ObjectName of the service
    * @exception Exception, thrown on failure to apply an existing configuration
    */
   public void applyServiceConfig(ObjectName serviceName)
      throws Exception
   {
      if (store == null)
      {
         initStore();
      }
      // Look up the service config
      ServiceConfig svcConfig = store.getService(serverName, serviceName);
      if (svcConfig != null)
      {
         log.debug("applyServiceConfig, server:" + serverName + ", serviceName:" + serviceName + ", config=" + svcConfig);

         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         String delegateName = svcConfig.getServiceConfigDelegateClassName();
         if (delegateName != null)
         {
            Class delegateClass = loader.loadClass(delegateName);
            ServicesConfigDelegate delegate = (ServicesConfigDelegate) delegateClass.newInstance();
            delegate.applyConfig(svcConfig, server);
         }
      }
   }

   // Private -------------------------------------------------------
   
   /**
    * Register a dynamic proxy of myself with the ServiceController
    */
   private void registerProxy() throws Exception
   {
      if (serviceController != null)
      {
         log.debug("Registering with ServiceController: " + serviceController);
         
         // Create a dynamic proxy pointing to me, over the MBeanServer
         org.jboss.system.ServiceBinding proxy = (org.jboss.system.ServiceBinding)
            MBeanProxyExt.create(org.jboss.system.ServiceBinding.class, serviceName, server);
         
         try
         {
            server.setAttribute(serviceController, new Attribute("ServiceBinding", proxy));
         }
         catch (Exception e)
         {
            // Log a debug message indicating we couldn't register and rethrow
            Throwable t = JMXExceptionDecoder.decode(e);
            log.debug("Failed to register with ServiceController: " + serviceController + ", reason: " + t.getMessage());
            throw (Exception)t;
         }
      }
   }
   
   /**
    * Unregister with ServiceController
    * by setting ServiceBinding to null
    */
   private void unregisterProxy()
   {
      if (serviceController != null)
      {
         log.debug("Unregistering with ServiceController: " + serviceController);
         try
         {
            server.setAttribute(serviceController, new Attribute("ServiceBinding", null));
         }
         catch (Exception e)
         {
            // Log a debug message indicating we couldn't unregister
            Throwable t = JMXExceptionDecoder.decode(e);
            log.debug("Failed to unregister with ServiceController: " + serviceController + ", reason: " + t.getMessage());         
         }
      }
   }
   
   /**
    * Create and load the services store
    */
   private void initStore() throws Exception
   {
      log.info("Initializing store");
      
      // If no store url identified, use the ServerConfigURL
      if (this.storeURL == null)
      {
         this.storeURL = ServerConfigLocator.locate().getServerConfigURL();
      }
      log.info("Using StoreURL: "+storeURL);

      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class factoryClass = loader.loadClass(storeFactoryClassName);
      ServicesStoreFactory storeFactory = (ServicesStoreFactory) factoryClass.newInstance();

      // Create and load the store
      store = storeFactory.newInstance();
      store.load(storeURL);
   }
}
