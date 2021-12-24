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
package org.jboss.mq.security;

import java.util.Random;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Generator of session id for ConnecttionToken.
 *
 * There should be nonguessabe and none repeting as long as the server is
 * alive.
 *
 * This could by all mean be made much more secure!
 *
 * @author     <a href="pra@tim.se">Peter Antman</a>
 * @author     <a href="hiram.chirino@jboss.org">Hiram Chirino</a>
 * @version $Revision: 57198 $
 */

public class SessionIDGenerator {
   int id = 0;
   public SessionIDGenerator() {
      
   }

   public String nextSessionId() throws NoSuchAlgorithmException {
      int myid = -1;
      synchronized(this) {
         myid=id;
         id++;
      }
      String key = randString();
      String data = randString();
      MessageDigest 
         md5=java.security.MessageDigest.getInstance("MD5");
      md5.update(String.valueOf(myid).getBytes());
      md5.update(data.getBytes());
      md5.update(data.getBytes());
      byte[]  byteHash = md5.digest(key.getBytes());
      return byteArrayToHexString(byteHash);
   }
   String randString() {
      Random r = new Random( System.currentTimeMillis());
      return ""+r.nextLong();
   }
   private final String byteArrayToHexString(byte[] byteArray)
    {
        String res = "";
        for (int i = 0; i < byteArray.length; i++) {
            int x = byteArray[i];
            if (x < 0)
                x += 256;
            String xs = Integer.toHexString(x);
            while (xs.length() < 2)
                xs = "0" + xs;
            res += xs;
        }
        return res;
    }
   public static void main(String[] args) throws Exception{
      int rounds = 1000;
      if (args.length == 1)
         rounds = Integer.parseInt(args[0]);
      
      SessionIDGenerator gen = new  SessionIDGenerator();
      for (int i =0;i<rounds;i++) {
         System.out.println(gen.nextSessionId());
      }
   }
   
} // SessionIDGenerator
