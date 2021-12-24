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
package javax.management.timer;

import java.util.Date;
import java.util.Vector;

import javax.management.InstanceNotFoundException;

/**
 * The timer service MBean interface. <p>
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 */

public interface TimerMBean
{
   // Constants -----------------------------------------------------

   // Static --------------------------------------------------------

   // Public --------------------------------------------------------

   /**
    * Starts the timer. If there are any notifications before the current time
    * these notifications are processed. The notification only takes place
    * when send past notiications is true.
    */
   public void start();

   /**
    * Stops the timer.
    */
   public void stop();

   /**
    * Creates a new timer notification with the specified type, message and userData and inserts it into the list of notifications with a given date, period and number of occurences.
    * <p/>
    * If the timer notification to be inserted has a date that is before the current date, the method behaves as if the specified date were the current date.
    * For once-off notifications, the notification is delivered immediately.
    * For periodic notifications, the first notification is delivered immediately and the subsequent ones are spaced as specified by the period parameter.
    * <p/>
    * Note that once the timer notification has been added into the list of notifications, its associated date, period and number of occurences cannot be updated.
    * <p/>
    * In the case of a periodic notification, the value of parameter fixedRate is used to specify the execution scheme, as specified in Timer.
    *
    * @param type         The timer notification type.
    * @param message      The timer notification detailed message.
    * @param userData     The timer notification user data object.
    * @param date         The date when the notification occurs.
    * @param period       The period of the timer notification (in milliseconds).
    * @param nbOccurences The total number the timer notification will be emitted.
    * @param fixedRate    If true and if the notification is periodic, the notification is scheduled with a fixed-rate execution scheme. If false and if the notification is periodic, the notification is scheduled with a fixed-delay execution scheme. Ignored if the notification is not periodic.
    * @return The identifier of the new created timer notification.
    * @throws IllegalArgumentException The period or the number of occurences is negative
    */
   public Integer addNotification(String type, String message, Object userData, Date date, long period, long nbOccurences, boolean fixedRate)
           throws IllegalArgumentException;

   /**
    * Creates a new timer notification for a specific date/time, with an
    * optional repeat period and a maximum number of occurences.<p>
    * <p/>
    * If the date and time is before the the current date and time the period
    * is repeatedly added until a date after the current date and time is
    * found. If the number of occurences is exceeded before the
    * current date and time is reached, an IllegalArgumentException is raised.
    *
    * @param type       the notification type.
    * @param message    the notification's message string.
    * @param userData   the notification's user data.
    * @param date       the date/time the notification will occur.
    * @param period     the repeat period in milli-seconds. Passing zero means
    *                   no repeat.
    * @param occurences the maximum number of repeats. When the period is not
    *                   zero and this parameter is zero, it will repeat indefinitely.
    * @return the notification id for this notification.
    * @throws IllegalArgumentException when the date is before the current
    *                                  date, the period is negative or the number of repeats is
    *                                  negative.
    */
   public Integer addNotification(String type, String message, Object userData, Date date, long period, long occurences)
           throws IllegalArgumentException;

   /**
    * Creates a new timer notification for a specific date/time, with an
    * optional repeat period.
    * When the repeat period is not zero, the notification repeats forever.<p>
    * <p/>
    * If the date and time is before the the current date and time the period
    * is repeatedly added until a date after the current date and time is
    * found.
    *
    * @param type     the notification type.
    * @param message  the notification's message string.
    * @param userData the notification's user data.
    * @param date     the date/time the notification will occur.
    * @param period   the repeat period in milli-seconds. Passing zero means
    *                 no repeat.
    * @return the notification id for this notification.
    * @throws IllegalArgumentException when the date is before the current
    *                                  date or the period is negative.
    */
   public Integer addNotification(String type, String message, Object userData, Date date, long period)
           throws IllegalArgumentException;

   /**
    * Creates a new timer notification for a specific date/time.
    * The notification is performed once.
    *
    * @param type     the notification type.
    * @param message  the notification's message string.
    * @param userData the notification's user data.
    * @param date     the date/time the notification will occur.
    * @return the notification id for this notification.
    * @throws IllegalArgumentException when the date is before the current
    *                                  date.
    */
   public Integer addNotification(String type, String message, Object userData, Date date)
           throws IllegalArgumentException;

   /**
    * Removes a notification from the timer MBean with the specified
    * notification id.
    *
    * @param id the notification id.
    * @throws InstanceNotFoundException when there are no notification
    *                                   registered with the id passed.
    */
   public void removeNotification(Integer id)
           throws InstanceNotFoundException;

   /**
    * Removes all notifications from the timer MBean of the specified
    * notification type.
    *
    * @param type the notification type.
    * @throws InstanceNotFoundException when there are no notifications of
    *                                   the type passed.
    */
   public void removeNotifications(String type)
           throws InstanceNotFoundException;

   /**
    * Removes all notifications from the timer MBean.
    */
   public void removeAllNotifications();

   /**
    * Retrieves the number of registered timer notifications.
    *
    * @return the number of notifications.
    */
   public int getNbNotifications();

   /**
    * Retrieves all timer notifications ids.
    *
    * @return a vector of Integers containing the ids. The list is empty
    *         when there are no timer notifications.
    */
   public Vector getAllNotificationIDs();

   /**
    * Retrieves all timer notifications ids of the passed notification type.
    *
    * @param type the notification type.
    * @return a vector of Integers containing the ids. The list is empty
    *         when there are no timer notifications of the passed type.
    */
   public Vector getNotificationIDs(String type);

   /**
    * Retrieves the notification type for a passed notification id.
    *
    * @param id the notification id.
    * @return the notification type or null when the notification id is
    *         not registered.
    */
   public String getNotificationType(Integer id);

   /**
    * Retrieves the notification message for a passed notification id.
    *
    * @param id the notification id.
    * @return the notification message or null when the notification id is
    *         not registered.
    */
   public String getNotificationMessage(Integer id);

   /**
    * Retrieves the notification user data for a passed notification id.
    *
    * @param id the notification id.
    * @return the notification user data or null when the notification id is
    *         not registered.
    */
   public Object getNotificationUserData(Integer id);

   /**
    * Retrieves a copy of the notification date for a passed notification id.
    *
    * @param id the notification id.
    * @return a copy of the notification date or null when the notification id
    *         is not registered.
    */
   public Date getDate(Integer id);

   /**
    * Retrieves a copy of the notification period for a passed notification id.
    *
    * @param id the notification id.
    * @return a copy of the notification period or null when the notification
    *         id is not registered.
    */
   public Long getPeriod(Integer id);

   /**
    * Retrieves a copy of the maximum notification occurences for a passed
    * notification id.
    *
    * @param id the notification id.
    * @return a copy of the maximum notification occurences or null when the
    *         notification id is not registered.
    */
   public Long getNbOccurences(Integer id);

   /**
    * Gets a copy of the flag indicating whether a peridic notification is executed at fixed-delay or at fixed-rate.
    *
    * @param id The timer notification identifier.
    * @return A copy of the flag indicating whether a peridic notification is executed at fixed-delay or at fixed-rate.
    */
   public Boolean getFixedRate(Integer id);

   /**
    * Retrieves the flag indicating whether past notifications are sent.
    *
    * @return true when past notifications are sent, false otherwise.
    */
   public boolean getSendPastNotifications();

   /**
    * Sets the flag indicating whether past notifications are sent.
    *
    * @param value the new value of the flag. true when past notifications
    *              are sent, false otherwise.
    */
   public void setSendPastNotifications(boolean value);

   /**
    * Test whether the timer MBean is active.
    *
    * @return true when timer is active, false otherwise.
    */
   public boolean isActive();

   /**
    * Test whether the timer MBean has any registered notifications.
    *
    * @return true when timer has no registered notifications, false otherwise.
    */
   public boolean isEmpty();

}
