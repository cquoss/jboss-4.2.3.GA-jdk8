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
package javax.ejb;

import java.io.Serializable;
import java.util.Date;

/**
 * The Timer interface contains information about a timer that was created
 * through the EJB Timer Service
 **/
public interface Timer {
   
   /**
    * Cause the timer and all its associated expiration notifications to be cancelled.
    *
    * @throws IllegalStateException If this method is invoked while the instance is in
    *                               a state that does not allow access to this method.
    * @throws NoSuchObjectLocalException If invoked on a timer that has expired or has been cancelled.
    * @throws EJBException If this method could not complete due to a system-level failure.
    **/
   public void cancel()
      throws
         IllegalStateException,
         NoSuchObjectLocalException,
         EJBException;
   
   /**
    * Get the number of milliseconds that will elapse before the next scheduled timer expiration.
    *
    * @return Number of milliseconds that will elapse before the next scheduled timer expiration.
    *
    * @throws IllegalStateException If this method is invoked while the instance is in
    *                               a state that does not allow access to this method.
    * @throws NoSuchObjectLocalException If invoked on a timer that has expired or has been cancelled.
    * @throws EJBException If this method could not complete due to a system-level failure.
    **/
   public long getTimeRemaining()
      throws
         IllegalStateException,
         NoSuchObjectLocalException,
         EJBException;
   
   /**
    * Get the point in time at which the next timer expiration is scheduled to occur.
    *
    * @return Get the point in time at which the next timer expiration is scheduled to occur.
    *
    * @throws IllegalStateException If this method is invoked while the instance is in
    *                               a state that does not allow access to this method.
    * @throws NoSuchObjectLocalException If invoked on a timer that has expired or has been cancelled.
    * @throws EJBException If this method could not complete due to a system-level failure.
    **/
   public Date getNextTimeout()
      throws
         IllegalStateException,
         NoSuchObjectLocalException,
         EJBException;
   
   /**
    * Get the information associated with the timer at the time of creation.
    *
    * @return The Serializable object that was passed in at timer creation, or null if the
    *         info argument passed in at timer creation was null.
    *
    * @throws IllegalStateException If this method is invoked while the instance is in
    *                               a state that does not allow access to this method.
    * @throws NoSuchObjectLocalException If invoked on a timer that has expired or has been cancelled.
    * @throws EJBException If this method could not complete due to a system-level failure.
    **/
   public Serializable getInfo()
      throws
         IllegalStateException,
         NoSuchObjectLocalException,
         EJBException;
   
   /**
    * Get a serializable handle to the timer. This handle can be used at a later time to
    * re-obtain the timer reference.
    *
    * @return Handle of the Timer
    *
    * @throws IllegalStateException If this method is invoked while the instance is in
    *                               a state that does not allow access to this method.
    * @throws NoSuchObjectLocalException If invoked on a timer that has expired or has been cancelled.
    * @throws EJBException If this method could not complete due to a system-level failure.
    **/
   public TimerHandle getHandle()
      throws
         IllegalStateException,
         NoSuchObjectLocalException,
         EJBException;
}
