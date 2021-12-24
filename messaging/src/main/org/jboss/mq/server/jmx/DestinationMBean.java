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
package org.jboss.mq.server.jmx;

import javax.management.ObjectName;
import org.jboss.system.ServiceMBean;
import org.jboss.mq.MessageStatistics;
import org.jboss.mq.server.MessageCounter;

/**
 * MBean interface for destination managers.
 *
 *
 * @author  <a href="pra@tim.se">Peter Antman</a>
 * @version $Revision: 57198 $
 */
public interface DestinationMBean extends ServiceMBean  
{
   /**
    * Get the value of JBossMQService.
    * @return value of JBossMQService.
    */
   void removeAllMessages() throws Exception; 
   
   /**
    * Get the value of JBossMQService.
    * @return value of JBossMQService.
    */
   ObjectName getDestinationManager(); 
   
   /**
    * Set the value of JBossMQService.
    * @param v  Value to assign to JBossMQService.
    */
   void setDestinationManager(ObjectName  jbossMQService); 

    /**
    * Sets the JNDI name for this destination
    * @param name Name to bind this topic to in the JNDI tree
    */
   void setJNDIName(String name) throws Exception;

   /**
    * Gets the JNDI name use by this destination.
    * @return  The JNDI name currently in use
    */
   String getJNDIName();
   
   /**
    * Sets the security xml config
    */
   //void setSecurityConf(String securityConf) throws Exception;
   void setSecurityConf(org.w3c.dom.Element securityConf) throws Exception;
   
   /**
    * Set the object name of the security manager.
    */
   public void setSecurityManager(ObjectName securityManager);
   
   /**
	* get message counter of all internal queues
	*/
   public MessageCounter[] getMessageCounter();
   
   /**
   * get message statistics of all internal queues
   */
   public MessageStatistics[] getMessageStatistics() throws Exception;

   /**
    * List destination message counter
    * @return String
    */
   public String listMessageCounter();
   
   /**
    * Reset destination message counter
    */
   public void resetMessageCounter();
   
   /**
    * List destination message counter history
    * @return String
    */
   public String listMessageCounterHistory();
   
   /**
    * Reset destination message counter history
    */
   public void resetMessageCounterHistory();

   /**
    * Sets the destination message counter history day limit
    * <0: unlimited, =0: disabled, > 0 maximum day count
    * 
    * @param days  maximum day count 
    */
   public void setMessageCounterHistoryDayLimit( int days );

   /**
    * Gets the destination message counter history day limit
    * @return  Maximum day count 
    */
   public int getMessageCounterHistoryDayLimit();

   /**
    * Retrieve the maximum depth of the queue or individual
    * subscriptions
    * @return the maximum depth
    */
   public int getMaxDepth();
   
   /**
    * Set the maximum depth of the queue or individual subscriptions
    * @param depth the maximum depth, zero means unlimited
    */
   public void setMaxDepth(int depth);

   /**
    * Retrieve the topic/queue in memory mode
    * @return true for in memory
    */
   public boolean getInMemory();

   /**
    * Set the temporary topic/queue in memory mode
    * @parameters true for in memory
    */
   public void setInMemory(boolean mode);
   
   /**
    * Returns the message redelivery limit; the number of redelivery attempts
    * before a message is moved to the DLQ.
    */
   public int getRedeliveryLimit();

   /**
    * Sets the redelivery limit.
    */
   public void setRedeliveryLimit(int limit);

   /**
    * Returns the message redelivery delay, the delay in milliseconds before a
    * rolled back or recovered message is redelivered
    */
   public long getRedeliveryDelay();

   /**
    * Sets the Message redelivery delay in milliseconds.
    */
   public void setRedeliveryDelay(long rDelay);

   /**
    * Returns the implementation class for receivers
    */
   public Class getReceiversImpl();

   /**
    * Sets the class implementating the receivers
    */
   public void setReceiversImpl(Class receivers);

   /**
    * Returns the recovery retries
    */
   public int getRecoveryRetries();

   /**
    * Sets the class implementating the receivers
    */
   public void setRecoveryRetries(int retries);

   /**
    * Returns the expiry destination.
    */
   public ObjectName getExpiryDestination();

   /**
    * Sets the expiry destination.
    */
   public void setExpiryDestination(ObjectName destination);

}
