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

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;

/**
 * EARDeployer MBean interface.
 * 
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @version $Revision: 60680 $
 */
public interface EARDeployerMBean extends SubDeployerExtMBean
{
   /** The default ObjectName */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.j2ee:service=EARDeployer");

   // Attributes ----------------------------------------------------
   
   boolean isIsolated();
   void setIsolated(boolean isolated);

   boolean isCallByValue();
   void setCallByValue(boolean callByValue);

   /** A flag the enables the default behavior of the ee5 library-directory.
    * If true, the lib contents of an ear are assumed to be the default value
    * for library-directory in the absence of an explicit library-directory. If
    * false, there must be an explicit library-directory.
    */
   public boolean isEnablelibDirectoryByDefault();
   /**
    * Set the implicit library-directory behavior flag.
    * @param flag - if true, the lib contents of an ear are assumed to be the
    * default value for library-directory in the absence of an explicit
    * library-directory. If false, there must be an explicit library-directory.
    */
   public void setEnablelibDirectoryByDefault(boolean flag);
}
