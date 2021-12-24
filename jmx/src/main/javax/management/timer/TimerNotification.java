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

import javax.management.Notification;

/**
 * A notification from the timer service.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 57200 $
 *
 * <p><b>Revisions:</b>
 * <p><b>20020816 Adrian Brock:</b>
 * <ul>
 * <li> Serialization </li>
 * </ul>
 */
public class TimerNotification
  extends Notification
{
  // Constants -----------------------------------------------------
  
  // Attributes ----------------------------------------------------
  
  /**
   * The notification id of this timer notification.
   */
  private Integer notificationID;

  // Static --------------------------------------------------------

   private static final long serialVersionUID = 1798492029603825750L;

  // Constructors --------------------------------------------------

  /**
   * Construct a new timer notification.
   *
   * @param type the notification type.
   * @param source the notification source.
   * @param sequenceNumber the notification sequence within the source object.
   * @param timeStamp the time the notification was sent.
   * @param message the detailed message.
   * @param id the timer notification id.
   * @param userData additional notification user data
   */
  public TimerNotification(String type, Object source, long sequenceNumber,
     long timeStamp, String message, Integer id) 
  {
    super(type, source, sequenceNumber, timeStamp, message);
    this.notificationID = id;
  }

  // Public --------------------------------------------------------

  /**
   * Retrieves the notification id of this timer notification.
   *
   * @return the notification id.
   */
  public Integer getNotificationID()
  {
    return notificationID;
  }

  // X implementation ----------------------------------------------

  // Notification overrides ----------------------------------------

   /**
    * @return human readable string.
    */
   public String toString()
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append(getClass().getName()).append(":");
      buffer.append(" type=").append(getType());
      buffer.append(" source=").append(getSource());
      buffer.append(" sequence=").append(getSequenceNumber());
      buffer.append(" time=").append(getTimeStamp());
      buffer.append(" message=").append(getMessage());
      buffer.append(" id=").append(getNotificationID());
      buffer.append(" userData=").append(getUserData());
      return buffer.toString();
   }

  // Package protected ---------------------------------------------

  // Protected -----------------------------------------------------

  // Private -------------------------------------------------------

  // Inner classes -------------------------------------------------
}
