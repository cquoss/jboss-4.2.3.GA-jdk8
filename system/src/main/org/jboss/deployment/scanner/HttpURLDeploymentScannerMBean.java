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
package org.jboss.deployment.scanner;

/**
 * MBean interface.
 */
public interface HttpURLDeploymentScannerMBean extends org.jboss.deployment.scanner.URLDeploymentScannerMBean
{

   /**
    * Default URL to be used when listing files on a remote HTTP folder If none is provided, the one found in jboss.netboot.listing.url is used If the URL is X, the resulting URL that is used to list the content of folder "foo" will be "Xdir=foo": the provided URL must support this naming convention
    */
   java.lang.String getDefaultHttpDirectoryListerUrl();

   void setDefaultHttpDirectoryListerUrl(java.lang.String url);

   /**
    * Default URL to be used when downloading files from a remote HTTP folder If none is provided, the one found in jboss.server.home.url is used
    */
   java.lang.String getDefaultHttpDirectoryDownloadUrl();

   void setDefaultHttpDirectoryDownloadUrl(java.lang.String url);

   void setURLList(java.util.List list);

   void setURLs(java.lang.String listspec) throws java.net.MalformedURLException;

   void scan() throws java.lang.Exception;

}
