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
package org.jboss.test.web.test;

import javax.management.MBeanInfo;
import javax.management.ObjectName;

import org.jboss.test.JBossTestCase;

/**
 *  JBAS-2410: Update tomcat5 to support multiple services.
 *  Needs a seperate test configuration (example - tomcat-webctx) such
 *  that the tomcat server.xml contains multiple service elements.
 *  @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 *  @since  Jan 11, 2006
 *  @version $Revision: 57211 $
 */
public class MultipleTomcatServicesTestCase extends JBossTestCase
{ 
   private String service1Name = "jboss.web:serviceName=jboss.web,type=Service";
   private String service2Name = "my.web:serviceName=my.web,type=Service";
   
   public MultipleTomcatServicesTestCase(String name)
   {
      super(name); 
   }
   
   public void testTomcatServices() throws Exception
   {
      MBeanInfo info = getServer().getMBeanInfo(new ObjectName(service1Name));
      assertNotNull(service1Name + " exists?", info);
      info = getServer().getMBeanInfo(new ObjectName(service2Name));
      assertNotNull(service2Name + " exists?", info); 
   } 
}
