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
package org.jboss.resource.deployment;

import javax.management.ObjectName;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkManager;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.ObjectModelFactorySimpleSubDeployerSupport;
import org.jboss.xb.binding.ObjectModelFactory;

/**
 * A resource adapter deployer
 *
 * @author  <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57189 $
 */
public class RARDeployer extends ObjectModelFactorySimpleSubDeployerSupport
   implements RARDeployerMBean
{
   /** The work manager name */
   protected ObjectName workManagerName;
   
   /** The work manager */
   protected WorkManager workManager;

   /** The xa terminator */
   protected XATerminator xaTerminator;

   /** The xa terminator name */
   protected ObjectName xaTerminatorName;
   
   public RARDeployer()
   {
      setEnhancedSuffixes(new String[] { "250:.rar" });
   }

   public String getExtension()
   {
      return ".rar";
   }

   public String getMetaDataURL()
   {
      return "META-INF/ra.xml";
   }

   public String getObjectName(DeploymentInfo di) throws DeploymentException
   {
      String name = di.shortName;
      di = di.parent;
      while (di != null)
      {
         name = di.shortName + "#" + name;
         di = di.parent;
      }
      return "jboss.jca:service=RARDeployment,name='" + name + "'";
   }

   public String getDeploymentClass()
   {
      return RARDeployment.class.getName();
   }

   public ObjectModelFactory getObjectModelFactory()
   {
      return new ResourceAdapterObjectModelFactory();
   }

   public ObjectName getWorkManagerName()
   {
      return workManagerName;
   }

   public void setWorkManagerName(ObjectName workManagerName)
   {
      this.workManagerName = workManagerName;
   }

   public ObjectName getXATerminatorName()
   {
      return xaTerminatorName;
   }

   public void setXATerminatorName(ObjectName xaTerminatorName)
   {
      this.xaTerminatorName = xaTerminatorName;
   }
   
   protected void startService() throws Exception
   {
      workManager = (WorkManager) server.getAttribute(workManagerName, "Instance");
      xaTerminator = (XATerminator) server.getAttribute(xaTerminatorName, "XATerminator");
      super.startService();
   }
}
