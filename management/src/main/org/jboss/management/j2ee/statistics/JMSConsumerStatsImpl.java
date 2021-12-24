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
package org.jboss.management.j2ee.statistics;

import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.JMSConsumerStats;
import javax.management.j2ee.statistics.TimeStatistic;

/**
 * Represents a statistics provided by a JMS message producer
 *
 * @author <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @author Andreas Schaefer
 * @version $Revision: 57197 $
 */
public final class JMSConsumerStatsImpl
        extends JMSEndpointStatsImpl
        implements JMSConsumerStats
{
   // Constants -----------------------------------------------------

   /** @since 4.0.2 */
   private static final long serialVersionUID = -8387556742416393266L;
   
   // Attributes ----------------------------------------------------

   private String mOrigin;

   // Constructors --------------------------------------------------

   public JMSConsumerStatsImpl(String pOrigin,
                               CountStatistic pMessageCount,
                               CountStatistic pPendingMessageCount,
                               CountStatistic pExpiredMessageCount,
                               TimeStatistic pMessageWaitTime)
   {
      super(pMessageCount, pPendingMessageCount, pExpiredMessageCount, pMessageWaitTime);
      mOrigin = pOrigin;
   }

   // javax.management.j2ee.JMSConsumerStats implementation ---------

   public String getOrigin()
   {
      return mOrigin;
   }
}
