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

import java.util.Map;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBeanSupport;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;

/**
 * An EAR Deployment 
 *
 * @see EARDeployer
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian.Brock</a>
 * @version $Revision: 57209 $
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 */
public class EARDeployment
   extends ServiceMBeanSupport
   implements EARDeploymentMBean
{
   // Constants -----------------------------------------------------

   public static final String BASE_EAR_DEPLOYMENT_NAME = "jboss.j2ee:service=EARDeployment";

   public static final ObjectName EAR_DEPLOYMENT_QUERY_NAME = ObjectNameFactory.create(BASE_EAR_DEPLOYMENT_NAME + ",*");
   
   // Attributes ----------------------------------------------------

   private DeploymentInfo deploymentInfo;
   private ConcurrentReaderHashMap metadata = new ConcurrentReaderHashMap();

   // Static --------------------------------------------------------
   
   public static String getJMXName(J2eeApplicationMetaData metaData, DeploymentInfo di)
   {
      String name = metaData.getJMXName();
      if( name == null )
         name = BASE_EAR_DEPLOYMENT_NAME + ",url='" + di.shortName + "'";
      return name;
   }
   
   // Constructors --------------------------------------------------

   public EARDeployment(final DeploymentInfo di)
   {
      this.deploymentInfo = di;
   }
   
   // Public --------------------------------------------------------
   
   public String getJMXName() throws Exception
   {
      J2eeApplicationMetaData metaData = (J2eeApplicationMetaData) deploymentInfo.metaData;
      return getJMXName(metaData, deploymentInfo);
   }

   /**
    * @jmx:managed-operation
    *
    */
   public Object resolveMetaData(Object key)
   {
      return metadata.get(key);
   }

    /**
     * @jmx:managed-operation
     *
     */
   public void addMetaData(Object key, Object value)
   {
      metadata.put(key, value);
   }

    /**
     * @jmx:managed-operation
     *
     */
   public Map getMetaData()
   {
      return metadata;
   }
}
