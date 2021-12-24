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

import java.util.Arrays;
import java.io.File;
import java.io.FileWriter;
import javax.management.ObjectName;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.security.plugins.FilePassword;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;


/** Tests of the JaasSecurityDomain service.
 
 @author Scott.Stark@jboss.org
 @version $Revision: 57211 $
 */
public class JaasSecurityDomainUnitTestCase
   extends JBossTestCase
{
   public JaasSecurityDomainUnitTestCase(String name)
   {
      super(name);
   }

   public void testTmpFilePassword() throws Exception
   {
      ObjectName name = new ObjectName("jboss.security:service=JaasSecurityDomain,domain=testTmpFilePassword");
      byte[] secret = "secret".getBytes();
      Object[] args = {secret};
      String[] sig = {secret.getClass().getName()};
      byte[] encode = (byte[]) super.invoke(name, "encode", args, sig);
      assertTrue("secret != encode", Arrays.equals(secret, encode) == false);

      PBEParameterSpec cipherSpec = new PBEParameterSpec("abcdefgh".getBytes(), 13);
      PBEKeySpec keySpec = new PBEKeySpec("password1".toCharArray());
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEwithMD5andDES");
      SecretKey cipherKey = factory.generateSecret(keySpec);
      Cipher cipher = Cipher.getInstance("PBEwithMD5andDES");
      cipher.init(Cipher.DECRYPT_MODE, cipherKey, cipherSpec);
      byte[] decode = cipher.doFinal(encode);
      assertTrue("secret == decode", Arrays.equals(secret, decode));
   }

   public void testFilePassword() throws Exception
   {
      ObjectName name = new ObjectName("jboss.security:service=JaasSecurityDomain,domain=testFilePassword");
      byte[] secret = "secret".getBytes();
      Object[] args = {secret};
      String[] sig = {secret.getClass().getName()};
      byte[] encode = (byte[]) super.invoke(name, "encode", args, sig);
      assertTrue("secret != encode", Arrays.equals(secret, encode) == false);

      PBEParameterSpec cipherSpec = new PBEParameterSpec("abcdefgh".getBytes(), 13);
      PBEKeySpec keySpec = new PBEKeySpec("password2".toCharArray());
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEwithMD5andDES");
      SecretKey cipherKey = factory.generateSecret(keySpec);
      Cipher cipher = Cipher.getInstance("PBEwithMD5andDES");
      cipher.init(Cipher.DECRYPT_MODE, cipherKey, cipherSpec);
      byte[] decode = cipher.doFinal(encode);
      assertTrue("secret == decode", Arrays.equals(secret, decode));
   }

   public void testEncodeDecode() throws Exception
   {
      ObjectName name = new ObjectName("jboss.security:service=JaasSecurityDomain,domain=encode-decode");
      byte[] secret = "secret".getBytes();
      Object[] args = {secret};
      String[] sig = {secret.getClass().getName()};
      byte[] encode = (byte[]) super.invoke(name, "encode", args, sig);
      assertTrue("secret != encode", Arrays.equals(secret, encode) == false);
      args = new Object[]{encode};
      byte[] decode = (byte[]) super.invoke(name, "decode", args, sig);
      assertTrue("secret == decode", Arrays.equals(secret, decode));
   }
   public void testEncodeDecode64() throws Exception
   {
      ObjectName name = new ObjectName("jboss.security:service=JaasSecurityDomain,domain=encode-decode");
      byte[] secret = "secret".getBytes();
      Object[] args = {secret};
      String[] sig = {secret.getClass().getName()};
      String encode = (String) super.invoke(name, "encode64", args, sig);
      Object[] args2 = {encode};
      String[] sig2 = {"java.lang.String"};
      byte[] decode = (byte[]) super.invoke(name, "decode64", args2, sig2);
      assertTrue("secret == decode", Arrays.equals(secret, decode));
   }

   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(JaasSecurityDomainUnitTestCase.class));

      // Create an initializer for the test suite
      TestSetup wrapper = new JBossTestSetup(suite)
      {
         File tmpPassword;
         File password;
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

            String url = getResourceURL("security/jaassecdomain-tests-service.xml");
            deploy(url);
            flushAuthCache("unit-tests");
         }
         protected void tearDown() throws Exception
         {
            tmpPassword.delete();
            password.delete();
            String url = getResourceURL("security/jaassecdomain-tests-service.xml");
            undeploy(url);
            super.tearDown();
         
         }
      };
      return wrapper;
   }

}
