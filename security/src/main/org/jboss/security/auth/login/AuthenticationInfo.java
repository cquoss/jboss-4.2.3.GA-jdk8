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
package org.jboss.security.auth.login;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.security.auth.AuthPermission;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.AppConfigurationEntry;

import org.jboss.security.SecurityConstants;

/** The login module configuration information.
 
 @author Scott.Stark@jboss.org
 @version $Revision: 67068 $
 */
public class AuthenticationInfo
{
   public static final AuthPermission GET_CONFIG_ENTRY_PERM = new AuthPermission("getLoginConfiguration");
   public static final AuthPermission SET_CONFIG_ENTRY_PERM = new AuthPermission("setLoginConfiguration");
   private String name;
   private ArrayList loginModules = new ArrayList();
   private CallbackHandler callbackHandler;

   public AuthenticationInfo()
   {
      this(null);
   }
   public AuthenticationInfo(String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }

   /** Get a copy of the  application authentication configuration. This requires
    an AuthPermission("getLoginConfiguration") access.
    */
   public AppConfigurationEntry[] copyAppConfigurationEntry()
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission(GET_CONFIG_ENTRY_PERM);
      AppConfigurationEntry[] copy = new AppConfigurationEntry[loginModules.size()];
      for(int i = 0; i < copy.length; i ++)
      {
         AppConfigurationEntry entry = (AppConfigurationEntry) loginModules.get(i);
	     HashMap options = new HashMap(entry.getOptions()); 
         if(!disableSecurityDomainInOptions())
         {
            options.put(SecurityConstants.SECURITY_DOMAIN_OPTION, this.getName());
         }
         copy[i] = new AppConfigurationEntry(entry.getLoginModuleName(),
					     entry.getControlFlag(), options);
      }
      return copy;
   }

   public void addAppConfigurationEntry(AppConfigurationEntry entry)
   {
      loginModules.add(entry);
   }
   /** Get an application authentication configuration. This requires an
    AuthPermission("getLoginConfiguration") access.
    */
   public AppConfigurationEntry[] getAppConfigurationEntry()
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission(GET_CONFIG_ENTRY_PERM);
      AppConfigurationEntry[] entries = new AppConfigurationEntry[loginModules.size()];
      loginModules.toArray(entries);
      return entries;
   }
   /** Set an application authentication configuration. This requires an
    AuthPermission("setLoginConfiguration") access.
    */
   public void setAppConfigurationEntry(AppConfigurationEntry[] loginModules)
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission(SET_CONFIG_ENTRY_PERM);
      
      this.loginModules.addAll(Arrays.asList(loginModules));
   }

   /**
    */
   public CallbackHandler getAppCallbackHandler()
   {
      return callbackHandler;
   }
   public void setAppCallbackHandler(CallbackHandler handler)
   {
      this.callbackHandler = handler;
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer("AppConfigurationEntry[]:\n");
      for(int i = 0; i < loginModules.size(); i ++)
      {
         AppConfigurationEntry entry = (AppConfigurationEntry) loginModules.get(i);
         buffer.append("["+i+"]");
         buffer.append("\nLoginModule Class: "+entry.getLoginModuleName());
         buffer.append("\nControlFlag: "+entry.getControlFlag());
         buffer.append("\nOptions:");
         Map options = entry.getOptions();
         Iterator iter = options.entrySet().iterator();
         while( iter.hasNext() )
         {
            Entry e = (Entry) iter.next();
            buffer.append("name="+e.getKey());
            buffer.append(", value="+e.getValue());
            buffer.append("\n");
         }
      }
      return buffer.toString();
   }
   
   private boolean disableSecurityDomainInOptions()
   {
      String sysprop = (String)AccessController.doPrivileged(new PrivilegedAction()
      {
         public Object run()
         {
            return System.getProperty(SecurityConstants.DISABLE_SECDOMAIN_OPTION);
         }
      });
      return "true".equalsIgnoreCase(sysprop); 
   }
}