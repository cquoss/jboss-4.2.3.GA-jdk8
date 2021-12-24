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

import javax.security.auth.login.AppConfigurationEntry;

import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.UnmarshallingContext;
import org.jboss.util.StringPropertyReplacer;
import org.jboss.logging.Logger;
import org.xml.sax.Attributes;

/** A JBossXB object factory for parsing the login-config.xml object model. 
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57203 $
 */
public class LoginConfigObjectModelFactory implements ObjectModelFactory
{
   private static Logger log = Logger.getLogger(LoginConfigObjectModelFactory.class);
   private boolean trace;

   public Object completeRoot(Object root, UnmarshallingContext ctx,
         String uri, String name)
   {
      if( trace )
         log.trace("completeRoot");
      return root;
   }

   public Object newRoot(Object root, UnmarshallingContext navigator,
      String namespaceURI, String localName, Attributes attrs)
   {
      trace = log.isTraceEnabled();
      if (!localName.equals("policy"))
      {
         throw new IllegalStateException("Unexpected root element: was expecting 'policy' but got '" + localName + "'");
      }
      if( trace )
         log.trace("newRoot, created PolicyConfig for policy element");
      return new PolicyConfig();
   }

   
   public Object newChild(PolicyConfig config, UnmarshallingContext navigator,
      String namespaceUri, String localName, Attributes attrs)
   {
      Object child = null;
      if( trace )
         log.trace("newChild.PolicyConfig, localName: "+localName);
      if("application-policy".equals(localName))
      {
         String name = attrs.getValue("name");
         name = StringPropertyReplacer.replaceProperties(name);
         child = new AuthenticationInfo(name);
         if( trace )
            log.trace("newChild.PolicyConfig, AuthenticationInfo: "+name);
      }
      return child;
   }
   public Object newChild(AuthenticationInfo info, UnmarshallingContext navigator,
      String namespaceUri, String localName, Attributes attrs)
   {
      Object child = null;
      if( trace )
         log.trace("newChild.AuthenticationInfo, localName: "+localName);
      if("login-module".equals(localName))
      {
         String code = attrs.getValue("code");
         code = StringPropertyReplacer.replaceProperties(code.trim());
         String flag = attrs.getValue("flag");
         flag = StringPropertyReplacer.replaceProperties(flag.trim());
         AppConfigurationEntryHolder holder = new AppConfigurationEntryHolder(code, flag);
         child = holder;
         if( trace )
            log.trace("newChild.AuthenticationInfo, login-module code: "+code);
      }

      return child;
   }
   public Object newChild(AppConfigurationEntryHolder entry, UnmarshallingContext navigator,
      String namespaceUri, String localName, Attributes attrs)
   {
      Object child = null;
      if( trace )
         log.trace("newChild.AppConfigurationEntryHolder, localName: "+localName);
      if("module-option".equals(localName))
      {
         String name = attrs.getValue("name");         
         child = new ModuleOption(name);
         if( trace )
            log.trace("newChild.AppConfigurationEntryHolder, module-option name: "+name);
      }

      return child;
   }

   public void setValue(ModuleOption option, UnmarshallingContext navigator,
      String namespaceUri, String localName, String value)
   {
      if("module-option".equals(localName))
      {
         String valueWithReplacement = StringPropertyReplacer.replaceProperties(value.trim());
         option.setValue(valueWithReplacement);
         if( trace )
            log.trace("setValue.ModuleOption, name: "+localName);
      }
   }

   public void addChild(PolicyConfig config, AuthenticationInfo authInfo,
      UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      config.add(authInfo);
      if( trace )
         log.trace("addChild.PolicyConfig, name: "+authInfo.getName());
   }
   public void addChild(AuthenticationInfo authInfo, AppConfigurationEntryHolder entryInfo,
      UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      AppConfigurationEntry entry = entryInfo.getEntry();
      authInfo.addAppConfigurationEntry(entry);
      if( trace )
         log.trace("addChild.AuthenticationInfo, name: "+entry.getLoginModuleName());
   }
   public void addChild(AppConfigurationEntryHolder entryInfo, ModuleOption option,
      UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      entryInfo.addOption(option);
      if( trace )
         log.trace("addChild.AppConfigurationEntryHolder, name: "+option.getName());
   }
   public void addChild(ModuleOption option, Object value,
      UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      option.setValue(value);
      if( trace )
         log.trace("addChild.ModuleOption, name: "+option.getName());
   }

}
