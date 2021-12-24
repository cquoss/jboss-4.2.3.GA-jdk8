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

import java.net.URL;

import java.io.IOException;

import java.util.Collection;

/**
 * Provides the interface for abstracting the actual storage
 * of cached deployments.
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 *
 * @todo Expose urls for cleaning
 *
 * @version <tt>$Revision: 57205 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public interface DeploymentStore
{
   /**
    * Get the stored URL for the given deployment URL.
    *
    * @param url    The original deployment URL.
    * @return       The stored URL or null if not stored.
    *
    * @throws Exception    Failed to get deployment URL from the store.
    *
    * @jmx:managed-operation
    */
   URL get(URL url) throws Exception;

   /**
    * Put a deployment URL into storage.  This will cause
    * the data associated with the given URL to be downloaded.
    * 
    * <p>If there is already a stored URL it will be overwritten.
    *
    * @param url    The original deployment URL.
    * @return       The stored URL.
    *
    * @throws Exception    Failed to put deployment URL into the store.
    *
    * @jmx:managed-operation
    */
   URL put(URL url) throws Exception;
}
