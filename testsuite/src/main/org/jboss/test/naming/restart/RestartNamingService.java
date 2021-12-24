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

package org.jboss.test.naming.restart;

import java.rmi.server.UnicastRemoteObject;

import org.jboss.naming.NamingService;
import org.jnp.server.Main;

/**
 * Overrides NamingService to unexport the naming stub in stopService().
 * Used to test what happens when this is done.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 65552 $
 */
public class RestartNamingService extends NamingService 
   implements RestartNamingServiceMBean
{

   protected void startService() throws Exception
   {
      Main main = getNamingServer();
      main.setUseGlobalService(false);
      main.setInstallGlobalService(false);
      
      super.startService();
   }

   protected void stopService() throws Exception
   {
      super.stopService();
      UnicastRemoteObject.unexportObject(getNamingInstance(), true);
   }
   
   
   
}
