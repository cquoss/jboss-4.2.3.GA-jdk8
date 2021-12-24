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
package org.jboss.test.iiop.test;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.test.JBossIIOPTestCase;

/**
 * A test for iiop without the webservice deployed for codebase
 * 
 * @author adrian@jboss.org
 * @version $Revision: 57211 $
 */
public class NoCodebaseUnitTestCase
   extends JBossIIOPTestCase
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   public NoCodebaseUnitTestCase(String name) 
   {
      super(name);
   }
   
   public void test_getStringWithNoCodebase() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName ejbDeployer = new ObjectName("jboss.ejb:service=EJBDeployer");
      ObjectName webService = (ObjectName) server.getAttribute(ejbDeployer, "WebServiceName");
      server.setAttribute(ejbDeployer, new Attribute("WebServiceName", null));
      try
      {
         deploy("iiop.jar");
      }
      finally
      {
         server.setAttribute(ejbDeployer, new Attribute("WebServiceName", webService));
         undeploy("iiop.jar");
      }
   }
}
