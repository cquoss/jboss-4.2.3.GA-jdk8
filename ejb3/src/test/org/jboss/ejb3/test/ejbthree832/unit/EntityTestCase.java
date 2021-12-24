/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.test.ejbthree832.unit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.ejb3.test.JBossWithKnownIssuesTestCase;
import org.jboss.ejb3.test.ejbthree832.EntityTest;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class EntityTestCase extends JBossWithKnownIssuesTestCase
{

   public EntityTestCase(String name)
   {
      super(name);
   }

   private String getConfiguration() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName name = new ObjectName("jboss.system:type=ServerConfig");
      return (String) server.getAttribute(name, "ServerName");
   }
   
   public void test1() throws Exception
   {
      String configuration = getConfiguration();
      if(configuration.equals("all"))
      {
         showKnownIssue("EJBTHREE-832");
         return;
      }
      assertEquals("Wrong server configuration", "default", configuration);
      
      EntityTest test = (EntityTest) this.getInitialContext().lookup("EntityTestBean/remote");
      test.test1();
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(EntityTestCase.class, "ejbthree832.jar");
   }

}
