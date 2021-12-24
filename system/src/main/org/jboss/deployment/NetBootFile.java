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
 * Represents a file/directory representation read from a distant HTTP server
 *
 * @see org.jboss.deployment.NetBootHelper
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 57205 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>7 novembre 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public class NetBootFile
{
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   String name = null;
   long size = 0;
   long lastModified = 0;
   boolean isDirectory = false;
   String lister = null;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public NetBootFile () {}

   public NetBootFile (String name, long size, long lastModified, boolean isDir, String lister)
   {
      this.name = name;
      this.size = size;
      this.lastModified = lastModified;
      this.isDirectory = isDir;
      this.lister = lister;
   }
   
   // Public --------------------------------------------------------
   
   public String getName ()
   {
      return this.name;
   }
   
   public long getSize ()
   {
      return this.size;
   }
   
   public long LastModified ()
   {
      return this.lastModified;
   }
   
   public boolean isDirectory() 
   {
      return this.isDirectory;
   }
   
   public String getListerUrl ()
   {
      return this.lister;
   }
   
   // Z implementation ----------------------------------------------
   
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}
