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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.DeploymentState;
import org.jboss.deployment.MainDeployerMBean;
import org.jboss.deployment.spi.SerializableTargetModuleID;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.file.Files;

/**
 * A service that supports the JSR-88 DeploymentManager operations.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 60132 $
 */
public class DeploymentManagerService extends ServiceMBeanSupport implements DeploymentManagerServiceMBean
{
   private Map moduleMap = Collections.synchronizedMap(new HashMap());
   /**
    * The name of the MainDeployer
    */
   private ObjectName mainDeployer;
   private ObjectName carDeployer;
   private ObjectName earDeployer;
   private ObjectName ejbDeployer;
   private ObjectName rarDeployer;
   private ObjectName warDeployer;

   /**
    * The local directory for uploaded content.
    */
   File uploadDir;
   private boolean failOnCollision;
   private boolean deleteOnUndeploy;

   public ObjectName getMainDeployer()
   {
      return mainDeployer;
   }

   public void setMainDeployer(ObjectName mainDeployer)
   {
      this.mainDeployer = mainDeployer;
   }

   /**
    * @return The EARDeployer mbean name
    * @jmx:managed-attribute
    */
   public ObjectName getEARDeployer()
   {
      return earDeployer;
   }

   /**
    * @param name The EARDeployer mbean name
    * @jmx:managed-attribute
    */
   public void setEARDeployer(ObjectName name)
   {
      this.earDeployer = name;
   }

   /**
    * @return The EJBDeployer mbean name
    * @jmx:managed-attribute
    */
   public ObjectName getEJBDeployer()
   {
      return ejbDeployer;
   }

   /**
    * @param name The EJBDeployer mbean name
    * @jmx:managed-attribute
    */
   public void setEJBDeployer(ObjectName name)
   {
      this.ejbDeployer = name;
   }

   /**
    * @return The RARDeployer mbean name
    * @jmx:managed-attribute
    */
   public ObjectName getRARDeployer()
   {
      return rarDeployer;
   }

   /**
    * @param name The RARDeployer mbean name
    * @jmx:managed-attribute
    */
   public void setRARDeployer(ObjectName name)
   {
      this.rarDeployer = name;
   }

   /**
    * @return The WARDeployer mbean name
    * @jmx:managed-attribute
    */
   public ObjectName getWARDeployer()
   {
      return warDeployer;
   }

   /**
    * @param name The WARDeployer mbean name
    * @jmx:managed-attribute
    */
   public void setWARDeployer(ObjectName name)
   {
      this.warDeployer = name;
   }

   /**
    * @return The CARDeployer mbean name
    * @jmx:managed-attribute
    */
   public ObjectName getCARDeployer()
   {
      return carDeployer;
   }

   /**
    * @param name The CARDeployer mbean name
    * @jmx:managed-attribute
    */
   public void setCARDeployer(ObjectName name)
   {
      this.carDeployer = name;
   }

   public File getUploadDir()
   {
      return this.uploadDir;
   }

   public void setUploadDir(File uploadDir)
   {
      this.uploadDir = uploadDir;
   }

   public boolean isDeleteOnUndeploy()
   {
      return deleteOnUndeploy;
   }

   public void setDeleteOnUndeploy(boolean deleteOnUndeploy)
   {
      this.deleteOnUndeploy = deleteOnUndeploy;
   }

   public boolean isFailOnCollision()
   {
      return failOnCollision;
   }

   public void setFailOnCollision(boolean failOnCollision)
   {
      this.failOnCollision = failOnCollision;
   }

   public Map getModuleMap()
   {
      return Collections.unmodifiableMap(moduleMap);
   }

   public void deploy(SerializableTargetModuleID moduleID) throws Exception
   {
      String url = moduleID.getModuleID();

      // Create a status object
      URL deployURL = new URL(url);
      URLConnection conn = deployURL.openConnection();
      int contentLength = conn.getContentLength();
      log.debug("Begin deploy, url: " + deployURL + ", contentLength: " + contentLength);

      // Create the local path
      File path = new File(deployURL.getFile());
      String archive = path.getName();
      File deployFile = new File(uploadDir, archive);
      if (failOnCollision && deployFile.exists())
         throw new IOException("deployURL(" + deployURL + ") collides with: " + deployFile.getPath());

      if (deployFile.exists())
         Files.delete(deployFile);

      File parentFile = deployFile.getParentFile();
      if (parentFile.exists() == false)
      {
         if (parentFile.mkdirs() == false)
            throw new IOException("Failed to create local path: " + parentFile);
      }

      InputStream is = conn.getInputStream();
      BufferedInputStream bis = new BufferedInputStream(is);
      byte[] buffer = new byte[4096];
      FileOutputStream fos = new FileOutputStream(deployFile);
      BufferedOutputStream bos = new BufferedOutputStream(fos);
      int count = 0;
      while ((count = bis.read(buffer)) > 0)
      {
         bos.write(buffer, 0, count);
      }
      bis.close();
      bos.close();

      moduleMap.put(url, moduleID);
   }

   public void start(String url) throws Exception
   {
      SerializableTargetModuleID moduleID = (SerializableTargetModuleID)moduleMap.get(url);
      if (moduleID == null)
         throw new IOException("deployURL(" + url + ") has not been distributed");

      URL deployURL = new URL(url);
      // Build the local path
      File path = new File(deployURL.getFile());
      String archive = path.getName();
      File deployFile = new File(uploadDir, archive);
      if (deployFile.exists() == false)
         throw new IOException("deployURL(" + url + ") has no local archive");

      MainDeployerMBean proxy = (MainDeployerMBean)MBeanServerInvocationHandler.newProxyInstance(server, mainDeployer, MainDeployerMBean.class, false);
      proxy.deploy(deployFile.toURL());
      moduleID.setRunning(true);
      moduleID.clearChildModuleIDs();
      // Repopulate the child modules
      DeploymentInfo info = proxy.getDeployment(deployFile.toURL());
      fillChildrenTargetModuleID(moduleID, info);
   }

   public void stop(String url) throws Exception
   {
      SerializableTargetModuleID moduleID = (SerializableTargetModuleID)moduleMap.get(url);
      if (moduleID == null)
         throw new IOException("deployURL(" + url + ") has not been distributed");

      URL deployURL = new URL(url);
      // Build the local path
      File path = new File(deployURL.getFile());
      String archive = path.getName();
      File deployFile = new File(uploadDir, archive);
      if (deployFile.exists() == false)
         throw new IOException("deployURL(" + url + ") has no local archive");

      MainDeployerMBean proxy = (MainDeployerMBean)MBeanServerInvocationHandler.newProxyInstance(server, mainDeployer, MainDeployerMBean.class, false);
      proxy.undeploy(deployFile.toURL());
      moduleID.setRunning(false);
   }

   public void undeploy(String url) throws Exception
   {
      SerializableTargetModuleID moduleID = (SerializableTargetModuleID)moduleMap.get(url);
      if (moduleID == null)
         return;

      // If the module is still running, stop it
      if (moduleID.isRunning())
         stop(url);

      URL deployURL = new URL(url);
      // Build the local path
      File path = new File(deployURL.getFile());
      String archive = path.getName();
      File deployFile = new File(uploadDir, archive);
      if (deployFile.exists() == false)
         throw new IOException("deployURL(" + url + ") has not been distributed");

      if (deleteOnUndeploy)
         Files.delete(deployFile);

      moduleMap.remove(url);
   }

   public SerializableTargetModuleID[] getAvailableModules(int moduleType) throws TargetException
   {
      ArrayList matches = new ArrayList();
      Iterator modules = moduleMap.values().iterator();
      while (modules.hasNext())
      {
         SerializableTargetModuleID module = (SerializableTargetModuleID)modules.next();
         if (module.getModuleType() == moduleType)
            matches.add(module);
      }

      SerializableTargetModuleID[] ids = new SerializableTargetModuleID[matches.size()];
      matches.toArray(ids);
      return ids;
   }

   private void fillChildrenTargetModuleID(SerializableTargetModuleID moduleID, DeploymentInfo info)
   {
      Iterator it = info.subDeployments.iterator();
      while (it.hasNext())
      {
         DeploymentInfo sdi = (DeploymentInfo)it.next();
         try
         {
            ModuleType type = getModuleType(sdi);
            // Discard unknown ModuleTypes
            if (type != null)
            {
               String module = sdi.url.toString();
               boolean isRunning = (DeploymentState.STARTED == sdi.state);
               SerializableTargetModuleID child = new SerializableTargetModuleID(moduleID, module, type.getValue(), isRunning);
               moduleID.addChildTargetModuleID(child);
               fillChildrenTargetModuleID(child, sdi);
            }
         }
         catch (UnsupportedOperationException e)
         {
            if (log.isTraceEnabled())
               log.trace("Ignoring", e);
         }
      }
   }

   private ModuleType getModuleType(DeploymentInfo info)
   {
      if (info.deployer == null)
         return null;

      ModuleType type = null;
      ObjectName deployer = info.deployer.getServiceName();

      // Validate an ejb vs car based on the watch url
      if (deployer.equals(carDeployer))
      {
         type = ModuleType.CAR;
      }
      else if (deployer.equals(ejbDeployer))
      {
         type = ModuleType.EJB;
      }
      else if (deployer.equals(earDeployer))
      {
         type = ModuleType.EAR;
      }
      else if (deployer.equals(rarDeployer))
      {
         type = ModuleType.RAR;
      }
      else if (deployer.equals(warDeployer))
      {
         type = ModuleType.WAR;
      }

      return type;
   }

}
