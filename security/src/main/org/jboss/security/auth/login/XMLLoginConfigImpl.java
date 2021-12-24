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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import javax.security.auth.AuthPermission;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.jboss.logging.Logger;
import org.jboss.security.SecurityConstants;
import org.jboss.security.auth.spi.UsersObjectModelFactory;
import org.jboss.xb.binding.JBossXBException;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;

/** An concrete implementation of the javax.security.auth.login.Configuration
 class that parses an xml configuration of the form:

 <policy>
 <application-policy name = "test-domain">
 <authentication>
 <login-module code = "org.jboss.security.plugins.samples.IdentityLoginModule"
 flag = "required">
 <module-option name = "principal">starksm</module-option>
 </login-module>
 </authentication>
 </application-policy>
 </policy>

 @see javax.security.auth.login.Configuration

 @author Scott.Stark@jboss.org
 @version $Revision: 57203 $
 */
public class XMLLoginConfigImpl extends Configuration
{

   private static final AuthPermission REFRESH_PERM = new AuthPermission("refreshLoginConfiguration");
   private static Logger log = Logger.getLogger(XMLLoginConfigImpl.class);
   /** A mapping of application name to AppConfigurationEntry[] 
   protected Map appConfigs = Collections.synchronizedMap(new HashMap());
   */
   PolicyConfig appConfigs = new PolicyConfig();
   /** The URL to the XML or Sun login configuration */
   protected URL loginConfigURL;
   /** The inherited configuration we delegate to */
   protected Configuration parentConfig;
   /** A flag indicating if XML configs should be validated */
   private boolean validateDTD = true;

   // --- Begin Configuration method overrrides
   public void refresh()
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPermission(REFRESH_PERM);
      if (log.isTraceEnabled())
         log.trace("Begin refresh");      
      appConfigs.clear();
      loadConfig();
      if (log.isTraceEnabled())
         log.trace("End refresh");      
   }

   public AppConfigurationEntry[] getAppConfigurationEntry(String appName)
   {
      if (log.isTraceEnabled())
         log.trace("Begin getAppConfigurationEntry("+appName+"), size="+appConfigs.size());
      // If the config has not been loaded try to do so
      if (loginConfigURL == null)
      {
         loadConfig();
      }

      AppConfigurationEntry[] entry = null;
      AuthenticationInfo authInfo = (AuthenticationInfo) appConfigs.get(appName);
      if (authInfo == null)
      {
         if (log.isTraceEnabled())
            log.trace("getAppConfigurationEntry("+appName+"), no entry in appConfigs, tyring parentCont: "+parentConfig);
         if (parentConfig != null)
            entry = parentConfig.getAppConfigurationEntry(appName);
         if (entry == null)
         {
            if (log.isTraceEnabled())
               log.trace("getAppConfigurationEntry("+appName+"), no entry in parentConfig, trying: "+SecurityConstants.DEFAULT_APPLICATION_POLICY);
         }
         authInfo = (AuthenticationInfo) appConfigs.get(SecurityConstants.DEFAULT_APPLICATION_POLICY);
      }

      if (authInfo != null)
      {
         if (log.isTraceEnabled())
            log.trace("End getAppConfigurationEntry("+appName+"), authInfo=" + authInfo);
         // Make a copy of the authInfo object
         final AuthenticationInfo theAuthInfo = authInfo;
         PrivilegedAction action = new PrivilegedAction()
         {
            public Object run()
            {
               return theAuthInfo.copyAppConfigurationEntry();
            }
         };
         entry = (AppConfigurationEntry[]) AccessController.doPrivileged(action);
      }
      else
      {
         if (log.isTraceEnabled())
            log.trace("End getAppConfigurationEntry("+appName+"), failed to find entry");
      }

      return entry;
   }
   // --- End Configuration method overrrides

   /** Set the URL of the XML login configuration file that should
    be loaded by this mbean on startup.
    */
   public URL getConfigURL()
   {
      return loginConfigURL;
   }

   /** Set the URL of the XML login configuration file that should
    be loaded by this mbean on startup.
    */
   public void setConfigURL(URL loginConfigURL)
   {
      this.loginConfigURL = loginConfigURL;
   }

   public void setConfigResource(String resourceName)
      throws IOException
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      loginConfigURL = tcl.getResource(resourceName);
      if (loginConfigURL == null)
         throw new IOException("Failed to find resource: " + resourceName);
   }

   public void setParentConfig(Configuration parentConfig)
   {
      this.parentConfig = parentConfig;
   }

   /** Get whether the login config xml document is validated againsts its DTD
    */
   public boolean getValidateDTD()
   {
      return this.validateDTD;
   }

   /** Set whether the login config xml document is validated againsts its DTD
    */
   public void setValidateDTD(boolean flag)
   {
      this.validateDTD = flag;
   }

   /** Add an application configuration
    */
   public void addAppConfig(String appName, AppConfigurationEntry[] entries)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPermission(REFRESH_PERM);
      AuthenticationInfo authInfo = new AuthenticationInfo(appName);
      authInfo.setAppConfigurationEntry(entries);
      if (log.isTraceEnabled())
         log.trace("addAppConfig("+appName+"), authInfo=" + authInfo);
      appConfigs.add(authInfo);
   }

   public void removeAppConfig(String appName)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPermission(REFRESH_PERM);
      if (log.isTraceEnabled())
         log.trace("removeAppConfig, appName="+appName);      
      appConfigs.remove(appName);
   }

   public void clear()
   {

   }

   /** Called to try to load the config from the java.security.auth.login.config
    * property value when there is no loginConfigURL.
    */
   public void loadConfig()
   {
      // Try to load the java.security.auth.login.config property
      String loginConfig = System.getProperty("java.security.auth.login.config");
      if (loginConfig == null)
         loginConfig = "login-config.xml";

      // If there is no loginConfigURL build it from the loginConfig
      if (loginConfigURL == null)
      {
         try
         {
            // Try as a URL
            loginConfigURL = new URL(loginConfig);
         }
         catch (MalformedURLException e)
         {
            // Try as a resource
            try
            {
               setConfigResource(loginConfig);
            }
            catch (IOException ignore)
            {
               // Try as a file
               File configFile = new File(loginConfig);
               try
               {
                  setConfigURL(configFile.toURL());
               }
               catch (MalformedURLException ignore2)
               {
               }
            }
         }
      }

      if (loginConfigURL == null)
      {
         log.warn("Failed to find config: " + loginConfig);
         return;
      }

      if (log.isTraceEnabled())
         log.trace("Begin loadConfig, loginConfigURL="+loginConfigURL);      
      // Try to load the config if found
      try
      {
         loadConfig(loginConfigURL);
         if (log.isTraceEnabled())
            log.trace("End loadConfig, loginConfigURL="+loginConfigURL);      
      }
      catch (Exception e)
      {
         log.warn("End loadConfig, failed to load config: " + loginConfigURL, e);
      }
   }

   protected String[] loadConfig(URL config) throws Exception
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPermission(REFRESH_PERM);

      ArrayList configNames = new ArrayList();
      log.debug("Try loading config as XML, url=" + config);
      try
      {
         loadXMLConfig(config, configNames);
      }
      catch(Throwable e)
      {
         log.debug("Failed to load config as XML", e);
         log.debug("Try loading config as Sun format, url=" + config);
         loadSunConfig(config, configNames);
      }
      String[] names = new String[configNames.size()];
      configNames.toArray(names);
      return names;
   }

   private void loadSunConfig(URL sunConfig, ArrayList configNames)
      throws Exception
   {
      InputStream is = sunConfig.openStream();
      if (is == null)
         throw new IOException("InputStream is null for: " + sunConfig);

      InputStreamReader configFile = new InputStreamReader(is);
      boolean trace = log.isTraceEnabled();
      SunConfigParser.doParse(configFile, this, trace);
   }

   private void loadXMLConfig(URL loginConfigURL, ArrayList configNames)
      throws IOException, JBossXBException
   {
      LoginConfigObjectModelFactory lcomf = new LoginConfigObjectModelFactory();
      UsersObjectModelFactory uomf = new UsersObjectModelFactory();

      InputStreamReader xmlReader = loadURL(loginConfigURL);
      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance()
            .newUnmarshaller();
      unmarshaller.mapFactoryToNamespace(uomf, "http://www.jboss.org/j2ee/schemas/XMLLoginModule");
      Object root = null;
      PolicyConfig config = (PolicyConfig) unmarshaller.unmarshal(xmlReader, lcomf, root);
      configNames.addAll(config.getConfigNames());
      appConfigs.copy(config);
   }

   private InputStreamReader loadURL(URL configURL)
      throws IOException
   {
      InputStream is = configURL.openStream();
      if (is == null)
         throw new IOException("Failed to obtain InputStream from url: " + configURL);
      InputStreamReader xmlReader = new InputStreamReader(is);
      return xmlReader;
   }

}
