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
package test.implementation.notification.support;

import java.util.ArrayList;
import java.util.HashMap;

import javax.management.Notification;
import javax.management.NotificationListener;

/**
 * A listener
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 */
public class Listener
   implements NotificationListener
{
   // Attributes ----------------------------------------------------------------

   /**
    * The notifications received by handback object
    */
   public HashMap notifications = new HashMap();

   public boolean waiting = false;

   // Constructor ---------------------------------------------------------------

   /**
    * Constructor
    */
   public Listener()
   {
   }

   // Notification Listener Implementation --------------------------------------

   /**
    * Handle the notification
    */
   public void handleNotification(Notification notification, Object handback)
   {
      doWait(true);

      synchronized(notifications)
      {
         ArrayList received = (ArrayList) notifications.get(handback);
         if (received == null)
         {
            received = new ArrayList();
            notifications.put(handback, received);
         }
         received.add(notification);
      }

      doNotify(false);
   }

   public synchronized void doWait(boolean value)
   {
      try
      {
         waiting = value;
         this.wait();
      }
      catch (InterruptedException e)
      {
         throw new RuntimeException(e.toString());
      }
   }

   public synchronized void doNotify(boolean expected)
   {
      try
      {
         while (waiting != expected)
            this.wait(10);
         this.notifyAll();
      }
      catch (InterruptedException e)
      {
         throw new RuntimeException(e.toString());
      }
   }
}
