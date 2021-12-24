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
package org.jboss.security.auth.callback;

import java.io.BufferedReader;
import java.io.IOException; 
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.jboss.security.auth.callback.ByteArrayCallback;

//$Id: AppCallbackHandler.java 57203 2006-09-26 12:19:06Z dimitris@jboss.org $

/** 
 * JBAS-3109:AppCallbackHandler as the default CallbackHandler in the 
 * security module
 * 
 * An implementation of the JAAS CallbackHandler interface that 
 * handles NameCallbacks, PasswordCallback, TextInputCallback 
 * and the JBoss ByteArrayCallback.
 * All JBoss Callbacks must be handled.
 * - MapCallback
 * 
 * @see javax.security.auth.callback.CallbackHandler
 * @see #handle(Callback[])
 * 
 * @author Scott.Stark@jboss.org
 * @author Anil.Saldhana@jboss.org
 * @version $Revision: 57203 $
 */
public class AppCallbackHandler implements CallbackHandler
{
   private String username;
   private char[] password;
   private byte[] data;
   private String text; 
   
   private transient String prompt;
   private transient Object credential;
   
   private Map keyValuePair;  
   
   /** Whether this handler gets the username/password from the console */
   private boolean consoleHandler = false; 

   public AppCallbackHandler(String username, char[] password)
   {
      this.username = username;
      this.password = password;
   }
   public AppCallbackHandler(String username, char[] password, byte[] data)
   {
      this.username = username;
      this.password = password;
      this.data = data;
   }
   public AppCallbackHandler(String username, char[] password, byte[] data, String text)
   {
      this.username = username;
      this.password = password;
      this.data = data;
      this.text = text;
   } 
   
   /**
    * 
    * Create a new AppCallbackHandler.
    * 
    * @param isConsoleHandler Denotes whether the input is from
    *                         the console.
    */
   public AppCallbackHandler(boolean isConsoleHandler)
   {
      this.consoleHandler = isConsoleHandler; 
   } 
   
   /**
    * 
    * Create a new AppCallbackHandler.
    * 
    * @param prompt Prompt meaningful to the LoginModule
    */
   public AppCallbackHandler(String prompt)
   {
       this.prompt = prompt;
   }
   
   /**
    * 
    * Create a new AppCallbackHandler.
    * 
    * @param mapOfValues Key Value Pair
    */
   public AppCallbackHandler(Map mapOfValues)
   { 
      this.keyValuePair = mapOfValues;
   }

   public String getPrompt()
   {
       return prompt;
   }
   public Object getCredential()
   {
       return credential;
   }
   public void setCredential(Object credential)
   {
       this.credential = credential;
   }
   public void clearCredential()
   {
       this.credential = null;
   } 

   public void handle(Callback[] callbacks) throws
         IOException, UnsupportedCallbackException
   {
      for (int i = 0; i < callbacks.length; i++)
      {
         Callback c = callbacks[i]; 
         if( c instanceof NameCallback )
         {
            NameCallback nc = (NameCallback) c; 
            String prompt = nc.getPrompt();
            if( prompt == null )
               prompt = "Enter Username: ";
            if(this.consoleHandler)
               nc.setName(getUserNameFromConsole(prompt));
            else
               nc.setName(username);
         }
         else if( c instanceof PasswordCallback )
         {
            PasswordCallback pc = (PasswordCallback) c;
            String prompt = pc.getPrompt();
            if( prompt == null )
               prompt = "Enter Password: ";
            if(this.consoleHandler)
               pc.setPassword(getPasswordFromConsole(prompt));
            else
               pc.setPassword(password);
         }
         else if( c instanceof TextInputCallback )
         {
            TextInputCallback tc = (TextInputCallback) c;
            tc.setText(text);
         }
         else if( c instanceof ByteArrayCallback )
         {
            ByteArrayCallback bac = (ByteArrayCallback) c;
            bac.setByteArray(data);
         }
         else if (c instanceof ObjectCallback)
         {
            ObjectCallback oc = (ObjectCallback) c;
            oc.setCredential(credential);
         }
         else if( c instanceof MapCallback )
         {
            MapCallback mc = (MapCallback) c;
            if(keyValuePair != null && !keyValuePair.isEmpty())
            {
               Iterator iter = keyValuePair.keySet().iterator();
               while(iter.hasNext())
               {
                  Object key = iter.next();
                  if(key instanceof String == false)
                     throw new SecurityException("key is not a String");
                  mc.setInfo((String)key, keyValuePair.get(key));
               }  
            }
         }
         else
         {
            throw new UnsupportedCallbackException(c, "Unrecognized Callback");
         }
      }
   }
   
   private String getUserNameFromConsole(String prompt)
   {
      String uName = "";
      System.out.print(prompt);
      InputStreamReader isr = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(isr);
      try
      {
         uName = br.readLine(); 
      }
      catch(IOException e)
      {
         throw new SecurityException("Failed to obtain username, ioe="+e.getMessage());
      }
      return uName;
   }
   
   private char[] getPasswordFromConsole(String prompt)
   {
      String pwd = "";
      //Prompt the user for the username
      System.out.print(prompt);
      InputStreamReader isr = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(isr);
      try
      {
         pwd = br.readLine();   
      }
      catch(IOException e)
      {
         throw new SecurityException("Failed to obtain password, ioe="+e.getMessage());
      }
      return pwd.toCharArray();
   }
}

