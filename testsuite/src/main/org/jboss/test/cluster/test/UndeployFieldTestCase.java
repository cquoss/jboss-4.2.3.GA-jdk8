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
package org.jboss.test.cluster.test;

import junit.framework.Test;

import org.jboss.test.JBossClusteredTestCase;

/**
 * Tests that a clustered session still functions properly on the second
 * node after the webapp is undeployed from the first node.
 * <p/>
 * This version tests a FieldBasedClusteredSession.
 * 
 * @author Brian Stansberry
 * @version $Id: UndeployFieldTestCase.java 57211 2006-09-26 12:39:46Z dimitris@jboss.org $
 */
public class UndeployFieldTestCase extends UndeployTestCase
{

   public UndeployFieldTestCase(String name)
   {
      super(name);
   }
   
   protected String getContextPath()
   {
      return "/http-scoped-field/";
   }
   
   protected String getWarName()
   {
      return "http-scoped-field.war";
   }

   public static Test suite() throws Exception
   {
      Test t1 = JBossClusteredTestCase.getDeploySetup(UndeployFieldTestCase.class,
            "http-scoped-field.war");
      return t1;
   }

}
