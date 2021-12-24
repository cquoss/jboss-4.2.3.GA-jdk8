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

/**
 * MBean interface.
 */
public interface ClasspathExtensionMBean extends org.jboss.system.ServiceMBean
{

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory
         .create("jboss:type=Service,service=ClasspathExtension");

   /**
    * mbean get-set pair for field metadataURL Get the value of metadataURL
    * @return value of metadataURL
    */
   java.lang.String getMetadataURL();

   /**
    * Set the value of metadataURL
    * @param metadataURL Value to assign to metadataURL
    */
   void setMetadataURL(java.lang.String metadataURL);

   /**
    * mbean get-set pair for field loaderRepository Get the value of loaderRepository
    * @return value of loaderRepository
    */
   javax.management.ObjectName getLoaderRepository();

   /**
    * Set the value of loaderRepository
    * @param loaderRepository Value to assign to loaderRepository
    */
   void setLoaderRepository(javax.management.ObjectName loaderRepository);

}
