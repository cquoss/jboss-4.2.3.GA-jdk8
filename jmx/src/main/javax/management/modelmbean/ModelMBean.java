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
package javax.management.modelmbean;

import javax.management.DynamicMBean;
import javax.management.PersistentMBean;
import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;
import javax.management.InstanceNotFoundException;

/**
 * Defines Model MBean.
 *
 * @see javax.management.DynamicMBean
 * @see javax.management.PersistentMBean
 * @see javax.management.modelmbean.ModelMBeanNotificationBroadcaster
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 57200 $  
 */
public interface ModelMBean
   extends DynamicMBean, PersistentMBean, ModelMBeanNotificationBroadcaster
{

   public void setModelMBeanInfo(ModelMBeanInfo inModelMBeanInfo)
   throws MBeanException, RuntimeOperationsException;

   public void setManagedResource(Object mr, String mr_type)
   throws MBeanException, RuntimeOperationsException, InstanceNotFoundException, InvalidTargetObjectTypeException;

}

