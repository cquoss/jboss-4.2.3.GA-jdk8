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
 * This interface must be implemented by all MBeans that emit notifications.
 *
 * This interface should be used in preference to NotificationBroadcaster.
 *
 * @see  javax.management.NotificationBroadcaster
 * @see  javax.management.NotificationFilter
 * @see  javax.management.NotificationListener
 *
 * @author     <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version    $Revision: 57200 $
 */
public interface NotificationEmitter
   extends NotificationBroadcaster
{
   /**
    * Removes a listener from the Emitter.<p>
    *
    * Only the listener, filter, handback triplet is removed
    *
    * @param   listener the listener object to remove
    * @param   filter   the filter registered with the listener
    * @param   handback the handback object associated with the registered listener
    *
    * @throws ListenerNotFoundException if the listener was not found
    */
   public void removeNotificationListener(NotificationListener listener,
                                          NotificationFilter filter,
                                          Object handback)
       throws ListenerNotFoundException;
}
