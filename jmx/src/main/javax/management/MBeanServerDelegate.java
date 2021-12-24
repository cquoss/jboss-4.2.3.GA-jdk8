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
package javax.management;

import org.jboss.mx.server.ServerConstants;
import org.jboss.mx.util.AgentID;

/**
 * Mandatory MBean server delegate MBean implementation.
 *
 * @see javax.management.MBeanServerDelegateMBean
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 57200 $
 *   
 */
public class MBeanServerDelegate
   implements MBeanServerDelegateMBean, NotificationEmitter
{

   // Attributes ----------------------------------------------------
   private MBeanNotificationInfo notificationInfo  = null;
   private NotificationBroadcasterSupport notifier = null;
   private String agentID = AgentID.create();
   
   // Constructors --------------------------------------------------
   public MBeanServerDelegate()
   {
      this.notificationInfo = new MBeanNotificationInfo(
                                 new String[] {
                                    MBeanServerNotification.REGISTRATION_NOTIFICATION,
                                    MBeanServerNotification.UNREGISTRATION_NOTIFICATION
                                 },
                                 MBeanServerNotification.class.getName(),
                                 "Describes the MBean registration and unregistration events in a MBean Server."
                              );
      this.notifier = new NotificationBroadcasterSupport();
   }

   // MBeanServerDelegateMBean implementation -----------------------
   public String getMBeanServerId()
   {
      return agentID;
   }

   public String getSpecificationName()
   {
      return ServerConstants.SPECIFICATION_NAME;
   }

   public String getSpecificationVersion()
   {
      return ServerConstants.SPECIFICATION_VERSION;
   }

   public String getSpecificationVendor()
   {
      return ServerConstants.SPECIFICATION_VENDOR;
   }

   public String getImplementationName()
   {
      return ServerConstants.IMPLEMENTATION_NAME;
   }

   public String getImplementationVersion()
   {
      return ServerConstants.IMPLEMENTATION_VERSION;
   }

   public String getImplementationVendor()
   {
      return ServerConstants.IMPLEMENTATION_VENDOR;
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      return new MBeanNotificationInfo[] { notificationInfo };
   }

   public synchronized void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
           throws IllegalArgumentException
   {
      notifier.addNotificationListener(listener, filter, handback);
   }

   public synchronized void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
      throws ListenerNotFoundException
   {
      notifier.removeNotificationListener(listener, filter, handback);
   }

   public synchronized void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException
   {
      notifier.removeNotificationListener(listener);
   }

   public void sendNotification(Notification notification)
   {
      notifier.sendNotification(notification);
   }

}

