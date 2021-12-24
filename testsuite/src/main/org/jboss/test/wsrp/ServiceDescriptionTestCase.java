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
package org.jboss.test.wsrp;

import javax.naming.InitialContext;
import javax.xml.rpc.Service;

import junit.framework.Test;

import org.jboss.test.webservice.WebserviceTestBase;
import org.jboss.test.wsrp.core.GetServiceDescription;
import org.jboss.test.wsrp.core.ServiceDescription;

/**
 * Tests WSRP Service Description
 *
 * @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @since 2.4 (Feb 20, 2006)
 */
public class ServiceDescriptionTestCase extends WebserviceTestBase
{
   public ServiceDescriptionTestCase(String name)
   {
      super(name);
   }

   /**
    * deploy the test archives
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(ServiceDescriptionTestCase.class, "wsrp.war, wsrp-client.jar");
   }

   public void testGetServiceDescription() throws Exception
   {
      InitialContext iniCtx = getClientContext("wsrp-client");
      Service service = (Service)iniCtx.lookup("java:comp/env/service/ServiceDescriptionService");
      WSRP_v1_ServiceDescription_PortType endpoint =
         (WSRP_v1_ServiceDescription_PortType)service.getPort(WSRP_v1_ServiceDescription_PortType.class);
      GetServiceDescription gs = new GetServiceDescription();
      String[] locales = new String[]{};

      gs.setDesiredLocales(locales);

      //Invoke the Web Service
      ServiceDescription sd = endpoint.getServiceDescription(gs);
      assertNotNull("sd != null", sd);
   }

}
