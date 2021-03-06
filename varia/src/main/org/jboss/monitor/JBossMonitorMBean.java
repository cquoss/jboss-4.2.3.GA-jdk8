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
package org.jboss.monitor;

import org.jboss.system.ServiceMBean;

import javax.management.ObjectName;
import java.util.ArrayList;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 57210 $
 *
 **/
public interface JBossMonitorMBean extends ServiceMBean
{
   String getMonitorName();

   void setMonitorName(String name);

   ObjectName getObservedObject();

   void setObservedObject(ObjectName oname);

   String getObservedAttribute();

   void setObservedAttribute(String attr);

   public String getThreshold();
   public void setThreshold(String val);

   boolean getEnabled();

   void setEnabled(boolean start);

   long getPeriod();

   void setPeriod(long period);

   ArrayList getAlertListeners();
   void setAlertListeners(ArrayList listeners);

   public Object getTriggeredAttributeValue();
   public long getTriggerTime();


   boolean alerted();

   void clearAlert();

}
