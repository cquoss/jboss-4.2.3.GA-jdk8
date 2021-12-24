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

import java.util.HashMap;

/** The meta data information specific to session beans.
 *
 *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @author Scott.Stark@jboss.org
 *   @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>.
 *   @version $Revision: 59651 $
 */
public class SessionMetaData extends BeanMetaData
{
   // Constants -----------------------------------------------------
   public static final String DEFAULT_STATEFUL_INVOKER = "stateful-unified-invoker";
   public static final String DEFAULT_CLUSTERED_STATEFUL_INVOKER = "clustered-stateful-unified-invoker";
   public static final String DEFAULT_STATELESS_INVOKER = "stateless-unified-invoker";
   public static final String DEFAULT_CLUSTERED_STATELESS_INVOKER = "clustered-stateless-unified-invoker";
   public static final String DEFAULT_WEBSERVICE_INVOKER = "session-webservice-invoker";
    
   // Attributes ----------------------------------------------------
   /** whether it is a stateful or stateless bean */
   private boolean stateful;
   /** the service-endpoint element contains the fully-qualified
    *  name of the session bean´s web service interface
    */
   protected String serviceEndpointClass;
   /** the jboss port-component binding for a ejb webservice
    */
   protected EjbPortComponentMetaData portComponent;

   // Static --------------------------------------------------------
    
   // Constructors --------------------------------------------------
   public SessionMetaData(ApplicationMetaData app)
   {
      super(app, BeanMetaData.SESSION_TYPE);
   }

   // Public --------------------------------------------------------
   public boolean isStateful()
   {
      return stateful;
   }

   public boolean isStateless()
   {
      return !stateful;
   }

   public boolean isWebservice()
   {
      return getServiceEndpoint() != null;
   }

   public String getServiceEndpoint()
   {
      return serviceEndpointClass;
   }

   public EjbPortComponentMetaData getPortComponent()
   {
      return portComponent;
   }

   public String getDefaultConfigurationName()
   {
      if (isStateful())
      {
         if (this.isClustered())
            return ConfigurationMetaData.CLUSTERED_STATEFUL_13;
         else
            return ConfigurationMetaData.STATEFUL_13;
      }
      else
      {
         if (this.isClustered())
            return ConfigurationMetaData.CLUSTERED_STATELESS_13;
         else
            return ConfigurationMetaData.STATELESS_13;
      }
   }

   protected void defaultInvokerBindings()
   {
      invokerBindings = new HashMap();
      if (isClustered())
      {
         if (stateful)
         {
            invokerBindings.put(DEFAULT_CLUSTERED_STATEFUL_INVOKER,
               getJndiName());
         }
         else
         {
            invokerBindings.put(DEFAULT_CLUSTERED_STATELESS_INVOKER,
               getJndiName());
         }
         if (isWebservice())
            invokerBindings.put(DEFAULT_WEBSERVICE_INVOKER, getJndiName());
      }
      else
      {
         if (stateful)
         {
            invokerBindings.put(DEFAULT_STATEFUL_INVOKER, getJndiName());
         }
         else
         {
            invokerBindings.put(DEFAULT_STATELESS_INVOKER, getJndiName());
         }
         if (isWebservice())
            invokerBindings.put(DEFAULT_WEBSERVICE_INVOKER, getJndiName());
      }
   }

   public void importEjbJarXml(Element element) throws DeploymentException
   {
      super.importEjbJarXml(element);
		
      // set the session type 
      String sessionType = getElementContent(getUniqueChild(element, "session-type"));
      if (sessionType.equals("Stateful"))
      {
         stateful = true;
      }
      else if (sessionType.equals("Stateless"))
      {
         stateful = false;
      }
      else
      {
         throw new DeploymentException("session type should be 'Stateful' or 'Stateless'");
      }
			
      // set the transaction type
      String transactionType = getElementContent(getUniqueChild(element, "transaction-type"));
      if (transactionType.equals("Bean"))
      {
         containerManagedTx = false;
      }
      else if (transactionType.equals("Container"))
      {
         containerManagedTx = true;
      }
      else
      {
         throw new DeploymentException("transaction type should be 'Bean' or 'Container'");
      }

      serviceEndpointClass = getElementContent(getOptionalChild(element, "service-endpoint"));
   }

   public void importJbossXml(Element element) throws DeploymentException
   {
      super.importJbossXml(element);
      // port-component optional element
      Element portElement = getOptionalChild(element, "port-component");
      if (portElement != null)
      {
         portComponent = new EjbPortComponentMetaData(this);
         portComponent.importJBossXml(portElement);
      }
   }

}
