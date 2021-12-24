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
package org.jboss.test.deployment.test;

import javax.management.ObjectName;

import org.jboss.test.JBossTestCase;

/**
 * A test that validates the ear library-directory configuration
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57211 $
 */
public class EARLibUnitTestCase extends JBossTestCase
{
   public EARLibUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Validate that the earlib-custom.ear/custom-lib jars are added to
    * the ear classpath.
    * 
    * @throws Exception
    */
   public void testEarLibCustom() throws Exception
   {
      getLog().debug("+++ testEarLibCustom");
      try
      {
         deploy("earlib-custom.ear");
         ObjectName name = new ObjectName("jboss.test:name=libService");
         String[] sig = {String.class.getName()};
         Object[] args = {"org.jboss.test.deployment.earlib.util.EarLibClass"};
         Boolean ok = (Boolean) super.invoke(name, "loadClass", args, sig);
         assertTrue("loadClass was ok", ok.booleanValue());
      }
      catch(Exception e)
      {
         getLog().info("Failed to access EarLibClass in earlib-custom.ear", e);
         throw e;
      }
      finally
      {
         undeploy("earlib-custom.ear");
      }
      
   }
   /**
    * Validate that the unpacked-earlib-custom.ear/custom-lib jars are added
    * to the ear classpath.
    * 
    * @throws Exception
    */
   public void testUnpackedEarLibCustom() throws Exception
   {
      getLog().debug("+++ testUnpackedEarLibCustom");
      try
      {
         deploy("unpacked-earlib-custom.ear");
         ObjectName name = new ObjectName("jboss.test:name=libService");
         String[] sig = {String.class.getName()};
         Object[] args = {"org.jboss.test.deployment.earlib.util.EarLibClass"};
         Boolean ok = (Boolean) super.invoke(name, "loadClass", args, sig);
         assertTrue("loadClass was ok", ok.booleanValue());
      }
      catch(Exception e)
      {
         getLog().info("Failed to access EarLibClass in unpacked-earlib-custom.ear", e);
         throw e;
      }
      finally
      {
         undeploy("unpacked-earlib-custom.ear");
      }
      
   }

   /**
    * Validate that the earlib-implicit.ear/lib jars are added to
    * the ear classpath.
    * 
    * @throws Exception
    */
   public void testEarLibImplicit() throws Exception
   {
      getLog().debug("+++ testEarLibImplicit");
      try
      {
         deploy("earlib-implicit.ear");
         ObjectName name = new ObjectName("jboss.test:name=libService");
         String[] sig = {String.class.getName()};
         Object[] args = {"org.jboss.test.deployment.earlib.util.EarLibClass"};
         Boolean ok = (Boolean) super.invoke(name, "loadClass", args, sig);
         assertTrue("loadClass was ok", ok.booleanValue());
      }
      catch(Exception e)
      {
         getLog().info("Failed to access EarLibClass in earlib-implicit.ear", e);
         throw e;
      }
      finally
      {
         undeploy("earlib-implicit.ear");
      }
      
   }

   /**
    * Validate that the unpacked-earlib-implicit.ear/lib jars are added
    * to the ear classpath.
    * 
    * @throws Exception
    */
   public void testUnpackedEarLibImplicit() throws Exception
   {
      getLog().debug("+++ testUnpackedEarLibImplicit");
      try
      {
         deploy("unpacked-earlib-implicit.ear");
         ObjectName name = new ObjectName("jboss.test:name=libService");
         String[] sig = {String.class.getName()};
         Object[] args = {"org.jboss.test.deployment.earlib.util.EarLibClass"};
         Boolean ok = (Boolean) super.invoke(name, "loadClass", args, sig);
         assertTrue("loadClass was ok", ok.booleanValue());
      }
      catch(Exception e)
      {
         getLog().info("Failed to access EarLibClass in unpacked-earlib-implicit.ear", e);
         throw e;
      }
      finally
      {
         undeploy("unpacked-earlib-implicit.ear");
      }
      
   }

   /**
    * Validate that the earlib-nolib.ear/lib jars are NOT added to
    * the ear classpath.
    * 
    * @throws Exception
    */
   public void testNoEarLib() throws Exception
   {
      getLog().debug("+++ testNoEarLib");
      try
      {
         deploy("earlib-nolib.ear");
         ObjectName name = new ObjectName("jboss.test:name=libService");
         String[] sig = {String.class.getName()};
         Object[] args = {"org.jboss.test.deployment.earlib.util.EarLibClass"};
         Boolean ok = (Boolean) super.invoke(name, "loadClass", args, sig);
         fail("loadClass was ok, "+ok);
      }
      catch(ClassNotFoundException e)
      {
         getLog().info("Failed to access EarLibClass in earlib-nolib.ear", e);
      }
      finally
      {
         undeploy("earlib-nolib.ear");
      }
      
   }
}
