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
package org.jboss.mx.notification;

import javax.management.JMException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

/**
 * A notification listener registration factory.
 * 
 * @see org.jboss.mx.notification.ListenerRegistry
 * @see org.jboss.mx.notification.ListenerRegistration
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 57200 $
 */
public interface ListenerRegistrationFactory
{
   /**
    * Create a listener registration
    *
    * @param listener the notification listener
    * @param filter the notification filter
    * @param handback the handback object
    * @return the listener registration
    * @exception JMException for an error
    */
   ListenerRegistration create(NotificationListener listener,
                               NotificationFilter filter,
                               Object handback)
      throws JMException;
}
