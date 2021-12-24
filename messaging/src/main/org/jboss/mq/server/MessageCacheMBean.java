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
package org.jboss.mq.server;

/**
 * MBean interface.
 */
public interface MessageCacheMBean extends org.jboss.system.ServiceMBean
{

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory
         .create("jboss.mq:service=MessageCache");

   /**
    * The <code>getInstance</code> method
    * @return a <code>MessageCache</code> value
    */
   org.jboss.mq.server.MessageCache getInstance();

   /**
    * Gets the hardRefCacheSize
    * @return Returns a int
    */
   int getHardRefCacheSize();

   /**
    * The <code>getSoftenedSize</code> method
    * @return a <code>long</code> value
    */
   long getSoftenedSize();

   /**
    * Gets the softRefCacheSize
    * @return Returns a int
    */
   int getSoftRefCacheSize();

   /**
    * Gets the totalCacheSize
    * @return Returns a int
    */
   int getTotalCacheSize();

   /**
    * Gets the cacheMisses
    * @return Returns a int
    */
   long getCacheMisses();

   /**
    * Gets the cacheHits
    * @return Returns a long
    */
   long getCacheHits();

   /**
    * Gets whether to make soft references
    * @return true when making soft references    */
   boolean getMakeSoftReferences();

   /**
    * Sets whether to make soft references
    * @param true to make soft references    */
   void setMakeSoftReferences(boolean makeSoftReferences);

   /**
    * Gets the minimum number of hard messages
    * @return the minimum number of hard messages    */
   int getMinimumHard();

   /**
    * Sets the minimum number of hard messages
    * @param minimumHard the minimum number of hard messages    */
   void setMinimumHard(int minimumHard);

   /**
    * Gets the maximum number of hard messages
    * @return the minimum number of hard messages    */
   int getMaximumHard();

   /**
    * Sets the maximum number of hard messages
    * @param maximumHard the maximum number of hard messages    */
   void setMaximumHard(int maximumHard);

   /**
    * Gets the length of time to wait before checking whether we should soften
    * @return the time to wait    */
   long getSoftenWaitMillis();

   /**
    * Sets the length of time to wait before checking whether we should soften
    * @param millis the time to wait in millis    */
   void setSoftenWaitMillis(long millis);

   /**
    * Gets the minimum length between softening checks
    * @return the time to wait    */
   long getSoftenNoMoreOftenThanMillis();

   /**
    * Sets the minimum length between softening checks
    * @param wait the time between checks    */
   void setSoftenNoMoreOftenThanMillis(long millis);

   /**
    * Gets the maximum length between softening checks
    * @return the time    */
   long getSoftenAtLeastEveryMillis();

   /**
    * Sets the minimum length between softening checks
    * @param wait the time between checks    */
   void setSoftenAtLeastEveryMillis(long millis);

   /**
    * Gets the highMemoryMark
    * @return Returns a long
    */
   long getHighMemoryMark();

   /**
    * Sets the highMemoryMark
    * @param highMemoryMark The highMemoryMark to set
    */
   void setHighMemoryMark(long highMemoryMark);

   /**
    * Gets the maxMemoryMark
    * @return Returns a long
    */
   long getMaxMemoryMark();

   /**
    * Sets the maxMemoryMark
    * @param maxMemoryMark The maxMemoryMark to set
    */
   void setMaxMemoryMark(long maxMemoryMark);

   /**
    * Gets the CurrentMemoryUsage
    * @return Returns a long
    */
   long getCurrentMemoryUsage();

   void setCacheStore(javax.management.ObjectName cacheStoreName);

   /**
    * The <code>getCacheStore</code> method
    * @return an <code>ObjectName</code> value
    */
   javax.management.ObjectName getCacheStore();

}
