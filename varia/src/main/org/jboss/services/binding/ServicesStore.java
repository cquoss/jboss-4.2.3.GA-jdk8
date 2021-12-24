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
package org.jboss.services.binding;

import java.net.URL;
import javax.management.ObjectName;

/** Interface for API to persist, read, and look up service configs
 *
 * @version $Revision: 57210 $
 * @author <a href="mailto:bitpushr@rochester.rr.com">Mike Finn</a>.
 * @author Scott.Stark@jboss.org
 */
public interface ServicesStore 
{
   /** Load the contents of a store.
    * @param storeURL the URL representing the location of the store
    * @exception Exception thrown on any failure to load the store
    */
   public void load(URL storeURL) throws Exception;
   /** Save the current store contents
    * @param storeURL the URL representing the location of the store
    * @exception Exception thrown on any failure to save the store
    */
   public void store(URL storeURL) throws Exception;

   /** Obtain a ServiceConfig object for the given server instance and target
    * service JMX ObjectName. This is called by the JBoss service configuration
    * layer to obtain service attribute binding overrides.
    *
    * @param serverName the name identifying the JBoss server instance in
    *    which the service is running.
    * @param serviceName the JMX ObjectName of the service
    * @return The ServiceConfig if one exists for the <serverName, serviceName>
    *    pair, null otherwise.
    */
   public ServiceConfig getService(String serverName, ObjectName serviceName);

   /** Add a ServiceConfig to the store. This is an optional method not used
    * by the JBoss service configuration layer.
    *
    * @param serverName the name identifying the JBoss server instance in
    *    which the service is running.
    * @param serviceName the JMX ObjectName of the service
    * @param serviceConfig the configuration to add
    * @throws DuplicateServiceException thrown if a configuration for the
    *    <serverName, serviceName> pair already exists.
    */
   public void addService(String serverName, ObjectName serviceName,
      ServiceConfig serviceConfig)
      throws DuplicateServiceException;

   /** Remove a service configuration from the store. This is an optonal method
    * not used by the JBoss service configuration layer.
    *
    * @param serverName the name identifying the JBoss server instance in
    *    which the service is running.
    * @param serviceName the JMX ObjectName of the service
    */
   public void removeService(String serverName, ObjectName serviceName);
}
