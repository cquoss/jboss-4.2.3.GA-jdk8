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
package org.jboss.deployment.services;

import java.io.File;
import java.util.Map;

import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.management.ObjectName;

import org.jboss.deployment.spi.SerializableTargetModuleID;

/**
 * An mbean service interface for the server side functionality needed for the
 * DeploymentManager implementation.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 60132 $
 */
public interface DeploymentManagerServiceMBean
{
   public File getUploadDir();

   public void setUploadDir(File uploadDir);

   boolean isDeleteOnUndeploy();

   void setDeleteOnUndeploy(boolean deleteOnUndeploy);

   boolean isFailOnCollision();

   void setFailOnCollision(boolean failOnCollision);

   public ObjectName getMainDeployer();

   public void setMainDeployer(ObjectName deployer);

   ObjectName getEARDeployer();

   void setEARDeployer(ObjectName name);

   ObjectName getEJBDeployer();

   void setEJBDeployer(ObjectName name);

   ObjectName getRARDeployer();

   void setRARDeployer(ObjectName name);

   ObjectName getWARDeployer();

   void setWARDeployer(ObjectName name);

   ObjectName getCARDeployer();

   void setCARDeployer(ObjectName name);

   public Map getModuleMap();

   public void deploy(SerializableTargetModuleID moduleID) throws Exception;

   public void start(String url) throws Exception;

   public void stop(String url) throws Exception;

   public void undeploy(String url) throws Exception;

   SerializableTargetModuleID[] getAvailableModules(int moduleType) throws TargetException;
}
