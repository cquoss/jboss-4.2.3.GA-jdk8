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
package org.jboss.security.plugins;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.ByteArrayOutputStream;

import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;

import org.jboss.logging.Logger;

/** Read a password in opaque form to a file for use with the FilePassword
 accessor in conjunction with the JaasSecurityDomain
 {CLASS}org.jboss.security.plugins.FilePassword:password-file
 format of the KeyStorePass attribute. The original opaque password file
 can be created by running:
   java org.jboss.security.plugins.FilePassword salt count password password-file
 Running
   java org.jboss.security.plugins.FilePassword
 will generate a usage message.

 Note that this is security by obscurity in that the password is not store
 in plaintext, but it can be recovered by simply using the code from this
 class.

 @see #main(String[])

 @author Scott.Stark@jboss.org
 @version $Revison:$
 */
public class FilePassword
{
   private File passwordFile;

   public FilePassword(String file)
   {
      passwordFile = new File(file);
   }

   public char[] toCharArray()
      throws IOException
   {
      RandomAccessFile raf = new RandomAccessFile(passwordFile, "rws");
      try
      {
         char[] password = decode(raf);
         return password;
      }
      catch(Exception e)
      {
         Logger log = Logger.getLogger(FilePassword.class);
         log.error("Failed to decode password file: "+passwordFile, e);
         throw new IOException(e.getMessage());
      }
   }

   static char[] decode(RandomAccessFile passwordFile)
      throws Exception
   {
      byte[] salt = new byte[8];
      passwordFile.readFully(salt);
      int count = passwordFile.readInt();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      int b;
      while( (b = passwordFile.read()) >= 0 )
         baos.write(b);
      passwordFile.close();
      byte[] secret = baos.toByteArray();

      PBEParameterSpec cipherSpec = new PBEParameterSpec(salt, count);
      PBEKeySpec keySpec = new PBEKeySpec("78aac249a60a13d5e882927928043ebb".toCharArray());
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEwithMD5andDES");
      SecretKey cipherKey = factory.generateSecret(keySpec);
      Cipher cipher = Cipher.getInstance("PBEwithMD5andDES");
      cipher.init(Cipher.DECRYPT_MODE, cipherKey, cipherSpec);
      byte[] decode = cipher.doFinal(secret);
      return new String(decode, "UTF-8").toCharArray();
   }
   static void encode(RandomAccessFile passwordFile, byte[] salt, int count,
      byte[] secret)
      throws Exception
   {
      PBEParameterSpec cipherSpec = new PBEParameterSpec(salt, count);
      PBEKeySpec keySpec = new PBEKeySpec("78aac249a60a13d5e882927928043ebb".toCharArray());
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEwithMD5andDES");
      SecretKey cipherKey = factory.generateSecret(keySpec);
      Cipher cipher = Cipher.getInstance("PBEwithMD5andDES");
      cipher.init(Cipher.ENCRYPT_MODE, cipherKey, cipherSpec);
      byte[] encode = cipher.doFinal(secret);
      passwordFile.write(salt);
      passwordFile.writeInt(count);
      passwordFile.write(encode);
      passwordFile.close();

   }
   /** Write a password in opaque form to a file for use with the FilePassword
    * accessor in conjunction with the JaasSecurityDomain
    * {CLASS}org.jboss.security.plugins.FilePassword:password-file
    * format of the KeyStorePass attribute.
    * 
    * @param args
    */ 
   public static void main(String[] args) throws Exception
   {
      if( args.length != 4 )
      {
         System.err.println(
            "Write a password in opaque form to a file for use with the FilePassword accessor"
           +"Usage: FilePassword salt count password password-file"
           +"  salt  : an 8 char sequence for PBEKeySpec"
           +"  count : iteration count for PBEKeySpec"
           +"  password : the clear text password to write"
           +"  password-file : the path to the file to write the password to"
         );
      }
      byte[] salt = args[0].substring(0, 8).getBytes();
      int count = Integer.parseInt(args[1]);
      byte[] passwordBytes = args[2].getBytes("UTF-8");
      RandomAccessFile passwordFile = new RandomAccessFile(args[3], "rws");
      encode(passwordFile, salt, count, passwordBytes);
   }
}
