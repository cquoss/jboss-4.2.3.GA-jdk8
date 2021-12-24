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
 * @see org.jboss.deployment.scanner.URLDeploymentScanner). It is designed to deploy direct URLs and to scan directories. The distinction between the two is that this class forces the user to specify which entries are directories to scan, and which are urls to deploy.
 */
public interface URLDirectoryScannerMBean extends org.jboss.deployment.scanner.DeploymentScannerMBean
{

   void addScanURL(java.net.URL url);

   void addScanURL(java.lang.String url) throws java.net.MalformedURLException;

   void addScanDir(java.net.URL url, java.util.Comparator comp, java.io.FileFilter filter);

   void addScanDir(java.lang.String urlSpec, java.lang.String compClassName, java.lang.String filterClassName)
         throws java.net.MalformedURLException;

   void removeScanURL(java.net.URL url);

   void setURLs(org.w3c.dom.Element elem);

   void setURLComparator(java.lang.String comparatorClassName);

   java.lang.String getURLComparator();

   void setFilter(java.lang.String filterClassName);

   java.lang.String getFilter();

   void scan();

}
