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
package org.jboss.ejb3.test.pkg.unit;

import javax.management.ObjectName;

import org.jboss.ejb3.ClientKernelAbstraction;
import org.jboss.ejb3.KernelAbstractionFactory;
import org.jboss.ejb3.test.pkg.Customer;
import org.jboss.ejb3.test.pkg.EntityTest;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id: EntityUnitTestCase.java 63336 2007-06-04 19:18:26Z bdecoste $
 */

public class EntityUnitTestCase
extends JBossTestCase
{
   org.apache.log4j.Logger log = getLog();

   static boolean deployed = false;
   static int test = 0;

   public EntityUnitTestCase(String name)
   {

      super(name);

   }

   public void testAll() throws Exception
   {
      EntityTest tester = (EntityTest) getInitialContext().lookup("EntityTestBean/remote");
      Customer c = tester.createCustomer();
      c = tester.findByCustomerId(c.getId());

      assertEquals(c.getName(), "Bill");
      assertEquals(c.getState(), "MA");
   }

   public static Test suite() throws Exception
   {
      ClientKernelAbstraction kernel = KernelAbstractionFactory.getClientInstance();
      ObjectName propertiesServiceON = new ObjectName("jboss:type=Service,name=SystemProperties");
      kernel.invoke(
            propertiesServiceON,
            "set",
            new Object[]{"jta-data-source","java:/DefaultDS"},
            new String[]{"java.lang.String", "java.lang.String"}
      );
      
      kernel.invoke(
            propertiesServiceON,
            "set",
            new Object[]{"hibernate.hbm2ddl.auto","create-drop"},
            new String[]{"java.lang.String", "java.lang.String"}
      );
      
      return getDeploySetup(EntityUnitTestCase.class, "pkg-test.jar");
   }

}
