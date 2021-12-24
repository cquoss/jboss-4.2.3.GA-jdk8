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

/**
 * This interface must be implemented by all MBeans that emit notifications.<p>
 *
 * New code should implement the NotificationEmitter interface
 *
 * @see  javax.management.NotificationListener
 * @see  javax.management.NotificationFilter
 * @see  javax.management.NotificationEmitter
 *
 * @author     <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version    $Revision: 57200 $
 */
public interface NotificationBroadcaster
{
   /**
    * Add a listener to an MBean.
    *
    * @param   listener    implementation of the listener object
    * @param   filter      implementation of the filter object or <tt>null</tt>
    *                      if no filtering is required
    * @param   handback    A handback object associated with each notification
    *                      sent by this notification broadcaster.
    *
    * @throws  IllegalArgumentException if listener is <tt>null</tt>
    */
   public void addNotificationListener(NotificationListener listener,
                                       NotificationFilter filter,
                                       Object handback)
       throws IllegalArgumentException;

   /**
    * Removes a listener from an MBean.
    *
    * @param   listener the listener object to remove
    *
    * @throws ListenerNotFoundException if the listener was not found
    */
   public void removeNotificationListener(NotificationListener listener)
       throws ListenerNotFoundException;

   /**
    * Returns the notification metadata associated with the MBean.
    *
    * @see  javax.management.MBeanNotificationInfo
    *
    * @return  MBean's notification metadata
    */
   public MBeanNotificationInfo[] getNotificationInfo();

}

