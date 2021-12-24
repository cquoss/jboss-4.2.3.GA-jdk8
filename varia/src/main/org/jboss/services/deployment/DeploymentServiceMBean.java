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
package org.jboss.services.deployment;

/**
 * MBean interface.
 */
public interface DeploymentServiceMBean extends org.jboss.system.ListenerServiceMBean
{

   void setTemplateDir(java.lang.String templateDir);

   java.lang.String getTemplateDir();

   java.lang.String getUndeployDir();

   void setUndeployDir(java.lang.String undeployDir);

   java.lang.String getDeployDir();

   void setDeployDir(java.lang.String deployDir);

   java.util.Set listModuleTemplates();

   java.util.List getTemplatePropertyInfo(java.lang.String template) throws java.lang.Exception;

   java.lang.String createModule(java.lang.String module, java.lang.String template, java.util.HashMap properties)
         throws java.lang.Exception;

   /**
    * Used primarily for testing through the jmx-console
    */
   java.lang.String createModule(java.lang.String module, java.lang.String template, java.lang.String[] properties)
         throws java.lang.Exception;

   boolean removeModule(java.lang.String module);

   void deployModuleAsynch(java.lang.String module) throws java.lang.Exception;

   java.net.URL getDeployedURL(java.lang.String module) throws java.lang.Exception;

   void undeployModuleAsynch(java.lang.String module) throws java.lang.Exception;

   java.net.URL getUndeployedURL(java.lang.String module) throws java.lang.Exception;

   /**
    * Upload a new library to server lib dir. A different filename may be specified, when writing the library. If the target filename exists, upload is not performed.
    * @param src the source url to copy
    * @param filename the filename to use when copying (optional)
    * @return true if upload was succesful, false otherwise    */
   boolean uploadLibrary(java.net.URL src, java.lang.String filename);

}
