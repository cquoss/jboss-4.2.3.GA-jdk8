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
package org.jboss.ejb.plugins.inflow;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.ejb.EJBMetaData;
import javax.management.ObjectName;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.Container;
import org.jboss.ejb.EJBProxyFactory;
import org.jboss.ejb.MessageDrivenContainer;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.InvokerInterceptor;
import org.jboss.metadata.ActivationConfigPropertyMetaData;
import org.jboss.metadata.InvokerProxyBindingMetaData;
import org.jboss.metadata.MessageDestinationMetaData;
import org.jboss.metadata.MessageDrivenMetaData;
import org.jboss.metadata.MetaData;
import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.proxy.GenericProxyFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;

/**
 * EJBProxyFactory for inflow message driven beans
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a> .
 * @version <tt>$Revision: 57209 $</tt>
 */
public class JBossMessageEndpointFactory
   extends ServiceMBeanSupport
   implements EJBProxyFactory, MessageEndpointFactory, JBossMessageEndpointFactoryMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------

   /** Whether trace is enabled */
   protected boolean trace = log.isTraceEnabled();
   
   /** Our container */
   protected MessageDrivenContainer container;
   
   /** Our meta data */
   protected MessageDrivenMetaData metaData;
   
   /** The invoker binding */
   protected String invokerBinding;
   
   /** The invoker meta data */
   protected InvokerProxyBindingMetaData invokerMetaData;
   
   /** The activation properties */
   protected HashMap properties = new HashMap();
   
   /** The proxy factory */
   protected GenericProxyFactory proxyFactory = new GenericProxyFactory();
   
   /** The messaging type class */
   protected Class messagingTypeClass;
   
   /** The resource adapter name */
   protected String resourceAdapterName;
   
   /** The resource adapter object name */
   protected ObjectName resourceAdapterObjectName;
   
   /** The activation spec */
   protected ActivationSpec activationSpec;
   
   /** The interceptors */
   protected ArrayList interceptors;
   
   /** The interfaces */
   protected Class[] interfaces;
   
   /** The next proxy id */
   protected SynchronizedInt nextProxyId = new SynchronizedInt(0);
   
   // Static --------------------------------------------------------
   
   /** The signature for createActivationSpec */
   protected String[] createActivationSpecSig = new String[]
   {
      Class.class.getName(),
      Collection.class.getName()
   };              
   
   /** The signature for activate/deactivateEndpint */
   protected String[] activationSig = new String[]
   {
      MessageEndpointFactory.class.getName(),
      ActivationSpec.class.getName()
   };              
         
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   /**
    * Get the message driven container
    * 
    * @return the container
    */
   public MessageDrivenContainer getContainer()
   {
      return container;
   }
   
   /**
    * Display the configuration
    * 
    * @jmx:managed-attribute
    * 
    * @return the configuration
    */
   public String getConfig()
   {
      return toString();
   }
   
   // MessageEndpointFactory implementation -------------------------

   public MessageEndpoint createEndpoint(XAResource resource) throws UnavailableException
   {
      trace = log.isTraceEnabled(); 
      
      if (getState() != STARTED && getState() != STARTING)
         throw new UnavailableException("The container is not started");
      
      HashMap context = new HashMap();
      context.put(MessageEndpointInterceptor.MESSAGE_ENDPOINT_FACTORY, this);
      context.put(MessageEndpointInterceptor.MESSAGE_ENDPOINT_XARESOURCE, resource);

      String ejbName = container.getBeanMetaData().getContainerObjectNameJndiName();

      if (trace)
         log.trace("createEndpoint " + this + " xaResource=" + resource);
      
      MessageEndpoint endpoint = (MessageEndpoint) proxyFactory.createProxy
      (
         ejbName + "@" + nextProxyId.increment(),  
         container.getServiceName(),
         InvokerInterceptor.getLocal(),
         null,
         null,
         interceptors,
         container.getClassLoader(),
         interfaces,
         context
      );
      
      if (trace)
         log.trace("Created endpoint " + endpoint + " from " + this);

      return endpoint;
   }

   public boolean isDeliveryTransacted(Method method) throws NoSuchMethodException
   {
      boolean result = false;
      int transType = metaData.getMethodTransactionType(method.getName(), method.getParameterTypes(), InvocationType.LOCAL);
      if (transType == MetaData.TX_REQUIRED)
         result = true;
      if (trace)
         log.trace("isDeliveryTransacted " + container.getBeanMetaData().getContainerObjectNameJndiName() + " method=" + method + " result=" + result);
      return result;
   }
   
   // ServiceMBeanSupport overrides ---------------------------------
   
   protected void startService() throws Exception
   {
      // Lets take a reference to our metadata
      metaData = (MessageDrivenMetaData) container.getBeanMetaData();
      // Resolve the message listener
      resolveMessageListener();
      // Resolve the resource adapter
      resolveResourceAdapter();
      // Create the activation config
      createActivationSpec();
      // Set up proxy parameters
      setupProxyParameters();
      // Activate
      activate();
   }
   
   protected void stopService() throws Exception
   {
      // Deactivate
      deactivate();
   }
   
   // EJBProxyFactory implementation --------------------------------

   public boolean isIdentical(Container container, Invocation mi)
   {
      throw new Error("Not valid for MessageDriven beans");
   }
   
   public Object getEJBHome()
   {
      throw new Error("Not valid for MessageDriven beans");
   }
   
   public EJBMetaData getEJBMetaData()
   {
      throw new Error("Not valid for MessageDriven beans");
   }
   
   public Collection getEntityCollection(Collection collection)
   {
      throw new Error("Not valid for MessageDriven beans");
   }
   
   public Object getEntityEJBObject(Object id)
   {
      throw new Error("Not valid for MessageDriven beans");
   }
   
   public Object getStatefulSessionEJBObject(Object id)
   {
      throw new Error("Not valid for MessageDriven beans");
   }
   
   public Object getStatelessSessionEJBObject()
   {
      throw new Error("Not valid for MessageDriven beans");
   }
   
   public void setInvokerBinding(String binding)
   {
      this.invokerBinding = binding;
   }
   
   public void setInvokerMetaData(InvokerProxyBindingMetaData imd)
   {
      this.invokerMetaData = imd;
   }
   
   // ContainerService implementation -------------------------------
   
   /**
    * Set the container for which this is an invoker to.
    *
    * @param container  The container for which this is an invoker to.
    */
   public void setContainer(final Container container)
   {
      this.container = (MessageDrivenContainer) container;
   }
   
   // Object overrides ----------------------------------------------
   
   /**
    * Return a string representation of the current config state.
    */
   public String toString()
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append(super.toString());
      buffer.append("{ resourceAdapter=").append(resourceAdapterObjectName);
      buffer.append(", messagingType=").append(messagingTypeClass.getName());
      buffer.append(", ejbName=").append(container.getBeanMetaData().getContainerObjectNameJndiName());
      buffer.append(", activationConfig=").append(properties.values());
      buffer.append(", activationSpec=").append(activationSpec);
      buffer.append("}");
      return buffer.toString();
   }   
   
   // Protected -----------------------------------------------------

   /**
    * Resolve message listener class
    * 
    * @throws DeploymentException for any error
    */
   protected void resolveMessageListener() throws DeploymentException
   {
      String messagingType = metaData.getMessagingType();
      try
      {
         messagingTypeClass = GetTCLAction.getContextClassLoader().loadClass(messagingType);
      }
      catch (Exception e)
      {
         DeploymentException.rethrowAsDeploymentException("Could not load messaging-type class " + messagingType, e);
      }
   }

   /**
    * Resolve the resource adapter name
    * 
    * @return the resource adapter name
    * @throws DeploymentException for any error
    */
   protected String resolveResourceAdapterName() throws DeploymentException
   {
      return metaData.getResourceAdapterName();
   }

   /**
    * Resolve the resource adapter
    * 
    * @throws DeploymentException for any error
    */
   protected void resolveResourceAdapter() throws DeploymentException
   {
      resourceAdapterName = resolveResourceAdapterName();
      try
      {
         resourceAdapterObjectName = new ObjectName("jboss.jca:service=RARDeployment,name='" + resourceAdapterName + "'");
         int state = ((Integer) server.getAttribute(resourceAdapterObjectName, "State")).intValue();
         if (state != STARTED)
            throw new DeploymentException("The resource adapter is not started " + resourceAdapterName);
      }
      catch (Exception e)
      {
         DeploymentException.rethrowAsDeploymentException("Cannot locate resource adapter deployment " + resourceAdapterName, e);
      }
   }
   
   /**
    * Set up the proxy parametrs
    * 
    * @throws DeploymentException
    */
   protected void setupProxyParameters() throws DeploymentException
   {
      // Set the interfaces
      interfaces = new Class[] { MessageEndpoint.class, messagingTypeClass };
      
      // Set the interceptors
      interceptors = new ArrayList();
      Element proxyConfig = invokerMetaData.getProxyFactoryConfig();
      Element endpointInterceptors = MetaData.getOptionalChild(proxyConfig, "endpoint-interceptors", null);
      if (endpointInterceptors == null)
         throw new DeploymentException("No endpoint interceptors found");
      else
      {
         NodeList children = endpointInterceptors.getElementsByTagName("interceptor");
         for (int i = 0; i < children.getLength(); ++i)
         {
            Node currentChild = children.item(i);
            if (currentChild.getNodeType() == Node.ELEMENT_NODE)
            {
               Element interceptor = (Element) children.item(i);
               String className = MetaData.getElementContent(interceptor);
               try
               {
                  Class clazz = container.getClassLoader().loadClass(className);
                  interceptors.add(clazz);
               }
               catch (Throwable t)
               {
                  DeploymentException.rethrowAsDeploymentException("Error loading interceptor class " + className, t);
               }
            }
         }
      }
   }
   
   /**
    * Add activation config properties
    * 
    * @throws DeploymentException for any error
    */
   protected void augmentActivationConfigProperties() throws DeploymentException
   {
      // Allow activation config properties from invoker proxy binding
      Element proxyConfig = invokerMetaData.getProxyFactoryConfig();
      Element activationConfig = MetaData.getOptionalChild(proxyConfig, "activation-config");
      if (activationConfig != null)
      {
         Iterator iterator = MetaData.getChildrenByTagName(activationConfig, "activation-config-property");
         while (iterator.hasNext())
         {
            Element resourceRef = (Element) iterator.next();
            ActivationConfigPropertyMetaData metaData = new ActivationConfigPropertyMetaData();
            metaData.importXml(resourceRef);
            if (properties.containsKey(metaData.getName()) == false)
               properties.put(metaData.getName(), metaData);
         }
      }
      
      // Message Destination Link
      String link = metaData.getDestinationLink();
      if (link != null)
      {
         link = link.trim();
         if (link.length() > 0)
         {
            if (properties.containsKey("destination"))
               log.warn("Ignoring message-destination-link '" + link + "' when the destination " +
                  "is already in the activation-config.");
            else
            {
               MessageDestinationMetaData destinationMetaData = container.getMessageDestination(link);
               if (destinationMetaData == null)
                  throw new DeploymentException("Unresolved message-destination-link '" + link + "' no message-destination in ejb-jar.xml");
               String jndiName = destinationMetaData.getJNDIName();
               if (jndiName == null)
                  throw new DeploymentException("The message-destination '" + link + "' has no jndi-name in jboss.xml");
               properties.put("destination", jndiName);
            }
         }
      }
   }   
   
   /**
    * Create the activation spec
    * 
    * @throws DeploymentException for any error
    */
   protected void createActivationSpec() throws DeploymentException
   {
      properties = new HashMap(metaData.getActivationConfigProperties());
      augmentActivationConfigProperties();
      
      Object[] params = new Object[] 
      {
         messagingTypeClass,
         properties.values()
      };
      try
      {
         activationSpec = (ActivationSpec) server.invoke(resourceAdapterObjectName, "createActivationSpec", params, createActivationSpecSig);
      }
      catch (Throwable t)
      {
         t = JMXExceptionDecoder.decode(t);
         DeploymentException.rethrowAsDeploymentException("Unable to create activation spec ra=" + resourceAdapterObjectName + 
               " messaging-type=" + messagingTypeClass.getName() + " properties=" + metaData.getActivationConfigProperties(), t);
      }
   }
   
   /**
    * Activate
    * 
    * @throws DeploymentException for any error
    */
   protected void activate() throws DeploymentException
   {
      Object[] params = new Object[] { this, activationSpec };
      try
      {
         server.invoke(resourceAdapterObjectName, "endpointActivation", params, activationSig);
      }
      catch (Throwable t)
      {
         t = JMXExceptionDecoder.decode(t);
         DeploymentException.rethrowAsDeploymentException("Endpoint activation failed ra=" + resourceAdapterObjectName + 
               " activationSpec=" + activationSpec, t);
      }
   }
   
   /**
    * Deactivate
    */
   protected void deactivate()
   {
      Object[] params = new Object[] { this, activationSpec };
      try
      {
         server.invoke(resourceAdapterObjectName, "endpointDeactivation", params, activationSig);
      }
      catch (Throwable t)
      {
         t = JMXExceptionDecoder.decode(t);
         log.warn("Endpoint activation failed ra=" + resourceAdapterObjectName + 
               " activationSpec=" + activationSpec, t);
      }
   }
   
   // Package Private -----------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner Classes -------------------------------------------------
}
