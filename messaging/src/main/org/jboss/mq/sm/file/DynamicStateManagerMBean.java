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
package org.jboss.mq.sm.file;

/**
 * MBean interface.
 */
public interface DynamicStateManagerMBean extends org.jboss.mq.sm.AbstractStateManagerMBean
{

   /**
    * Set the name of the statefile.
    * @param newStateFile java.lang.String    */
   void setStateFile(java.lang.String newStateFile);

   /**
    * Get name of file.
    * @return java.lang.String    */
   java.lang.String getStateFile();

   boolean hasSecurityManager();

   void setHasSecurityManager(boolean hasSecurityManager);

   void loadConfig() throws java.io.IOException, org.jboss.mq.xml.XElementException;

   void saveConfig() throws java.io.IOException;

   void addUser(java.lang.String name, java.lang.String password, java.lang.String preconfID)
         throws java.lang.Exception;

   void removeUser(java.lang.String name) throws java.lang.Exception;

   void addRole(java.lang.String name) throws java.lang.Exception;

   void removeRole(java.lang.String name) throws java.lang.Exception;

   void addUserToRole(java.lang.String roleName, java.lang.String user) throws java.lang.Exception;

   void removeUserFromRole(java.lang.String roleName, java.lang.String user) throws java.lang.Exception;

}
