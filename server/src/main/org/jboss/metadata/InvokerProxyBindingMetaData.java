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

/** The configuration information for invoker-proxy bindingss that may be
 * tied to a EJB container.
 *   @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 *   @version $Revision: 57209 $
 */
public class InvokerProxyBindingMetaData extends MetaData
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   /** The unique name of the invoker proxy binding */
   private String name;
   /** The detached invoker MBean service associated with the proxy */
   private String mbean;
   /** The class name of the org.jboss.ejb.EJBProxyFactory implementation used
    * to create proxies for this configuration
    */
   private String proxyFactory;
   /** An arbitary configuration to pass to the EJBProxyFactory implementation
    */
   private Element proxyFactoryConfig;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public InvokerProxyBindingMetaData(String name)
   {
      this.name = name;
   }

   // Public --------------------------------------------------------

   /** Get the unique name of the invoker proxy binding */
   public String getName()
   {
      return name;
   }

   /** Get the detached invoker MBean service name associated with the proxy */
   public String getInvokerMBean()
   {
      return mbean;
   }

   /** Get the class name of the org.jboss.ejb.EJBProxyFactory implementation
    * used to create proxies for this configuration
    */
   public String getProxyFactory()
   {
      return proxyFactory;
   }

   /** An arbitary configuration to pass to the EJBProxyFactory implementation
    */
   public Element getProxyFactoryConfig()
   {
      return proxyFactoryConfig;
   }

   /** Import the jboss.xml jboss/invoker-proxy-bindings/invoker-proxy-binding
    * child elements
    * @param element jboss/invoker-proxy-bindings/invoker-proxy-binding
    * @throws DeploymentException
    */
   public void importJbossXml(Element element) throws DeploymentException
   {
      mbean = getUniqueChildContent(element, "invoker-mbean");
      proxyFactory = getUniqueChildContent(element, "proxy-factory");
      proxyFactoryConfig = getUniqueChild(element, "proxy-factory-config");
   }
}
