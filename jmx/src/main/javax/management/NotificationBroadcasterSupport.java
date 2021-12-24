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

import org.jboss.mx.util.JBossNotificationBroadcasterSupport;

/**
 * A helper class for notification broadcasters/emitters
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 */
public class NotificationBroadcasterSupport implements NotificationEmitter
{
   /** The delegate */
   private JBossNotificationBroadcasterSupport delegate = new JBossNotificationBroadcasterSupport();

   /**
    * Construct the new notification broadcaster support object
    */
   public NotificationBroadcasterSupport()
   {
   }

   public void addNotificationListener(NotificationListener listener,
                                       NotificationFilter filter,
                                       Object handback)
   {
      delegate.addNotificationListener(listener, filter, handback);
   }

   public void removeNotificationListener(NotificationListener listener)
       throws ListenerNotFoundException
   {
      delegate.removeNotificationListener(listener);
   }

   public void removeNotificationListener(NotificationListener listener,
                                          NotificationFilter filter,
                                          Object handback)
      throws ListenerNotFoundException
   {
      delegate.removeNotificationListener(listener, filter, handback);
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      return delegate.getNotificationInfo();
   }

   public void sendNotification(Notification notification)
   {
      delegate.sendNotification(notification);
   }

   /**
    * Handle the notification, the default implementation is to synchronously invoke the listener.
    *
    * @param listener the listener to notify
    * @param notification the notification
    * @param handback the handback object
    */
   protected void handleNotification(NotificationListener listener,
                                     Notification notification,
                                     Object handback)
   {
      delegate.handleNotification(listener, notification, handback);
   }
}
