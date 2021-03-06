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
package org.jboss.test.security.test;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

import org.jboss.security.Util;
import org.jboss.security.plugins.FilePassword;
import org.jboss.test.BaseTestCase;
import org.jboss.util.StringPropertyReplacer;

/**
 org.jboss.security.Util tests
 
 @author Scott.Stark@jboss.org
 @version $Revision: 57211 $
*/
public class SecurityUtilUnitTestCase
   extends BaseTestCase
{
   private File tmpPassword;
   private File password;

   public SecurityUtilUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      // Create a tmp password file for testTmpFilePassword
      tmpPassword = new File(System.getProperty("java.io.tmpdir"), "tmp.password");
      FileWriter writer = new FileWriter(tmpPassword);
      writer.write("password1");
      writer.close();

      // Create the opaque password file for testFilePassword
      password = new File(System.getProperty("java.io.tmpdir")+ "/tst.password");
      String[] args2 = {
         "12345678", // salt
         "17", // count
         "password2", // password
         password.getAbsolutePath() // password-file
      };
      FilePassword.main(args2);
      getLog().info("Created password file: "+args2[2]);
   }
   protected void tearDown() throws Exception
   {
      tmpPassword.delete();
      password.delete();
      super.tearDown();   
   }

   /**
    * Test {CLASS}org.jboss.security.plugins.TmpFilePassword
    * @throws Exception
    */
   public void testTmpFilePassword() throws Exception
   {
      String passwordCmd = "{CLASS}org.jboss.security.plugins.TmpFilePassword:${java.io.tmpdir}/tmp.password,5000";
      passwordCmd = StringPropertyReplacer.replaceProperties(passwordCmd);
      char[] password = Util.loadPassword(passwordCmd);
      assertTrue("password1", Arrays.equals(password, "password1".toCharArray()));
   }
   /**
    * Test {CLASS}org.jboss.security.plugins.FilePassword
    * @throws Exception
    */
   public void testFilePassword() throws Exception
   {
      String passwordCmd = "{CLASS}org.jboss.security.plugins.FilePassword:${java.io.tmpdir}/tst.password";
      passwordCmd = StringPropertyReplacer.replaceProperties(passwordCmd);
      char[] password = Util.loadPassword(passwordCmd);
      assertTrue("password2", Arrays.equals(password, "password2".toCharArray()));
   }
   /**
    * Test {EXT}org.jboss.test.security.test.ExecPasswordCmd
    * @throws Exception
    */
   public void testExtPassword() throws Exception
   {
      // First check for java.exe or java as the binary
      File java = new File(System.getProperty("java.home"), "/bin/java");
      File javaExe = new File(System.getProperty("java.home"), "/bin/java.exe");
      String jre;
      if( java.exists() )
         jre = java.getAbsolutePath();
      else
         jre = javaExe.getAbsolutePath();
      // Build the command to run this jre
      String cmd = jre
      + " -cp "+System.getProperty("java.class.path")
      + " org.jboss.test.security.test.ExecPasswordCmd";
      String passwordCmd = "{EXT}"+cmd;
      char[] password = Util.loadPassword(passwordCmd);
      assertTrue("password3", Arrays.equals(password, "password3".toCharArray()));
   }

}
