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
package org.jboss.deployment.cache;

/**
 * MBean interface.
 * @todo Validate the urlMap
 */
public interface FileDeploymentStoreMBean extends org.jboss.deployment.cache.DeploymentStoreMBean
{

   /**
    * Set the local directory where cache data will be stored.
    * @param dir The local directory where cache data will be stored.
    * @throws IOException File not found, not a directory, can't write...
    */
   void setDirectory(java.io.File dir) throws java.io.IOException;

   /**
    * Returns the local directory where cache data is stored.
    * @return The local directory where cache data is stored.
    */
   java.io.File getDirectory();

   /**
    * Set the name of the local directory where cache data will be stored. <p>Invokes {@link #setDirectory}.
    * @param dirname The name of the local directory where cache data will be stored.
    * @throws IOException File not found, not a directory, can't write...
    */
   void setDirectoryName(java.lang.String dirname) throws java.io.IOException;

   /**
    * Get the name of the local directory where cache data is stored.
    * @return The name of the local directory where cache data is stored.
    */
   java.lang.String getDirectoryName();

   java.net.URL get(java.net.URL url);

   java.net.URL put(java.net.URL url) throws java.lang.Exception;

}
