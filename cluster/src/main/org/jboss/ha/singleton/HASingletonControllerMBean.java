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
package org.jboss.ha.singleton;

import javax.management.ObjectName;

/** 
 * The management interface for the singleton controller service.
 * 
 * @see org.jboss.ha.singleton.HASingletonMBean
 * 
 * @author <a href="mailto:ivelin@apache.org">Ivelin Ivanov</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:mr@gedoplan.de">Marcus Redeker</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 57824 $
 */
public interface HASingletonControllerMBean extends HASingletonSupportMBean
{
   /** The controlled target Singleton MBean */
   ObjectName getTargetName();
   void setTargetName(ObjectName targetObjectName);   
   
   /** The target method to call when the Singleton is started */
   String getTargetStartMethod();
   void setTargetStartMethod(String targetStartMethod);

   /** The argument to pass to the start method of the singleton MBean */
   String getTargetStartMethodArgument();
   void setTargetStartMethodArgument(String targetStartMethodArgument);  

   /** The target method to call when the Singleton is stopped */
   String getTargetStopMethod();
   void setTargetStopMethod(String targetStopMethod);

   /** The argument to pass to the stop method of the singleton MBean */
   String getTargetStopMethodArgument();
   void setTargetStopMethodArgument(String targetStopMethodArgument);
}
