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
package org.jboss.jms.asf;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ServerSessionPool;
import org.jboss.tm.XidFactoryMBean;

/**
 * Defines the model for creating <tt>ServerSessionPoolFactory</tt> objects. <p>
 *
 * @author    <a href="mailto:peter.antman@tim.se">Peter Antman</a> .
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a> .
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version   $Revision: 66805 $
 */
public interface ServerSessionPoolFactory
{
   /**
    * Set the name of the factory.
    *
    * @param name  The name of the factory.
    */
   void setName(String name);

   /**
    * Get the name of the factory.
    *
    * @return   The name of the factory.
    */
   String getName();

   /**
    * The <code>setXidFactory</code> method supplies the XidFactory that 
    * server sessions will use to get Xids to control local transactions.
    *
    * @param xidFactory a <code>XidFactoryMBean</code> value
    */
   void setXidFactory(XidFactoryMBean xidFactory);

   /**
    * The <code>getXidFactory</code> method returns the XidFactory that 
    * server sessions will use to get xids..
    *
    * @return a <code>XidFactoryMBean</code> value
    */
   XidFactoryMBean getXidFactory();

   /**
    * Create a new <tt>ServerSessionPool</tt>.
    * 
    * @param destination the destination
    * @param con the jms connection
    * @param minSession the minimum number of sessions
    * @param maxSession the maximum number of sessions
    * @param keepAlive the time to keep sessions alive
    * @param forceClear whether or not to force clearing the pool
    * @param forceClearInterval the interval to wait between pool clearing attempts
    * @param forceClearAttempts the number of attempts for clearing the pool
    * @param isTransacted whether the pool is transacted
    * @param ack the acknowledegement method
    * @param listener the listener
    * @param useLocalTX whether to use local transactions
    * @return A new pool.
    * @throws JMSException for any error
    */
   ServerSessionPool getServerSessionPool(Destination destination, Connection con, int minSession, int maxSession,
         long keepAlive, boolean forceClear, long forceClearInterval, int forceClearAttempts,boolean isTransacted, int ack, boolean useLocalTX, MessageListener listener)
         throws JMSException;
}