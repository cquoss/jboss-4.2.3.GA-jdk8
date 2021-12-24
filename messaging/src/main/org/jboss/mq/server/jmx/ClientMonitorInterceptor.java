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

import org.jboss.mq.server.JMSServerInterceptor;

/**
 * JMX MBean implementation DelayInterceptor.
 *
 * @jmx:mbean extends="org.jboss.mq.server.jmx.InterceptorMBean"
 * @author     <a href="hiram.chirino@jboss.org">Hiram Chirino</a>
 * @version    $Revision: 57198 $
 */
public class ClientMonitorInterceptor
	extends InterceptorMBeanSupport
	implements ClientMonitorInterceptorMBean {

	private org.jboss.mq.server.ClientMonitorInterceptor interceptor =
		new org.jboss.mq.server.ClientMonitorInterceptor();

	long clientTimeout = 1000 * 60;
	Thread serviceThread;

	public JMSServerInterceptor getInterceptor() {
		return interceptor;
	}

	/**
	 * Returns the clientTimeout.
	 * @return long
	 * @jmx:managed-attribute
	 */
	public long getClientTimeout() {
		return clientTimeout;
	}

	/**
	 * Sets the clientTimeout.
	 * @param clientTimeout The clientTimeout to set
	 * @jmx:managed-attribute
	 */
	public void setClientTimeout(long clientTimeout) {
		this.clientTimeout = clientTimeout;
	}

	/**
	 * @see org.jboss.system.ServiceMBeanSupport#startService()
	 */
	protected void startService() throws Exception {
		super.startService();
		if( serviceThread != null )
			return;
			
		serviceThread = new Thread(new Runnable() {
			public void run() {
				try {
					while(true) {
						Thread.sleep(clientTimeout);				
						interceptor.disconnectInactiveClients(
							System.currentTimeMillis() - clientTimeout);
					}
				} catch (InterruptedException e) {
				}
			}
		}, "ClientMonitor Service Thread");
		serviceThread.setDaemon(true);
		serviceThread.start();
	}

	/**
	 * @see org.jboss.system.ServiceMBeanSupport#stopService()
	 */
	protected void stopService() throws Exception {
		if (serviceThread != null) {
			serviceThread.interrupt();
			serviceThread = null;
		}
		super.stopService();
	}

}
