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
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.acl.Group;

import javax.management.MBeanServer;
import javax.management.MBeanServerBuilder;
import javax.management.MBeanServerFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.Subject;

import org.apache.log4j.Logger;

import org.jboss.logging.XLevel;
import org.jboss.security.auth.login.XMLLoginConfigImpl;
import org.jboss.security.auth.spi.Users;
import org.jboss.security.auth.login.LoginConfigObjectModelFactory;
import org.jboss.security.auth.login.PolicyConfig;
import org.jboss.security.auth.login.AuthenticationInfo;
import org.jboss.security.auth.spi.UsersObjectModelFactory;
import org.jboss.security.auth.callback.UsernamePasswordHandler;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.Util;
import org.jboss.util.StringPropertyReplacer;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.DefaultSchemaResolver;

/**
 * Tests of the LoginModule classes using the XMLLoginConfigImpl implementation
 * of the JAAS login module configuration.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 70422 $
 */
public class XMLLoginModulesUnitTestCase extends LoginModulesUnitTestCase
{

   public XMLLoginModulesUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      // Setup the replacement properties
      System.setProperty("users.properties", "/security/config/users.properites");
      System.setProperty("roles.properties", "/security/config/roles.properites");

      // Install the custom JAAS configuration
      XMLLoginConfigImpl config = new XMLLoginConfigImpl();
      config.setConfigResource("security/login-config.xml");
      config.loadConfig();
      Configuration.setConfiguration(config);

      // Turn on trace level logging
      Logger root = Logger.getRootLogger();
      root.setLevel(XLevel.TRACE);
   }

   public void testGargantusRealm() throws Exception
   {
      Configuration config = Configuration.getConfiguration();
      AppConfigurationEntry[] entries = config.getAppConfigurationEntry("testGargantusRealm");
      assertTrue("entries.length == 1", entries.length == 1);
      AppConfigurationEntry entry = entries[0];
      LoginModuleControlFlag flag = entry.getControlFlag();
      assertTrue("flag == required", flag == LoginModuleControlFlag.REQUIRED);
      Map options = entry.getOptions();
      String principal = (String) options.get("principal");
      assertTrue("principal(" + principal + ") = sa",
         principal.equals("sa"));
      String userName = (String) options.get("userName");
      assertTrue("userName(" + userName + ") = sa",
         userName.equals("sa"));      
      String password = (String) options.get("password");
      assertTrue("password(" + password + ") = ''",
         password.equals(""));      
      String managedConnectionFactoryName = (String) options.get("managedConnectionFactoryName");
      assertTrue("managedConnectionFactoryName(" + managedConnectionFactoryName + ") = ''",
         managedConnectionFactoryName.equals("jboss.jca:service=LocalTxCM,name=DefaultDS"));      
   }

   public void testPropertyReplacement() throws Exception
   {
      Configuration config = Configuration.getConfiguration();
      AppConfigurationEntry[] entries = config.getAppConfigurationEntry("testPropertyReplacement");
      assertTrue("entries.length == 1", entries.length == 1);
      AppConfigurationEntry entry = entries[0];
      LoginModuleControlFlag flag = entry.getControlFlag();
      assertTrue("flag == required", flag == LoginModuleControlFlag.REQUIRED);
      Map options = entry.getOptions();
      String users = (String) options.get("usersProperties");
      assertTrue("usersProperties(" + users + ") = /security/config/users.properites",
         users.equals("/security/config/users.properites"));
      String roles = (String) options.get("rolesProperties");
      assertTrue("rolesProperties(" + roles + ") = /security/config/roles.properites",
         roles.equals("/security/config/roles.properites"));
   }

   public void testPBEIdentityLoginModule()
      throws Exception
   {
      // Create our own MBeanServer
      MBeanServer server = MBeanServerFactory.createMBeanServer("jboss");
      getLog().info("testPBEIdentityLoginModule");
      // Login using the testPBEIdentityLoginModuleTmpFilePassword config
      UsernamePasswordHandler handler = new UsernamePasswordHandler("sa", "thesecret".toCharArray());
      LoginContext lc = new LoginContext("testPBEIdentityLoginModule", handler);
      lc.login();
      Subject subject = lc.getSubject();
      Set pcs = subject.getPrivateCredentials(PasswordCredential.class);
      assertEquals("PasswordCredential", 1, pcs.size());
      PasswordCredential pc = (PasswordCredential) pcs.iterator().next();
      assertEquals("PasswordCredential.sa", "sa", pc.getUserName());
      assertEquals("PasswordCredential.password", "thesecret", new String(pc.getPassword()));
      lc.logout();
      MBeanServerFactory.releaseMBeanServer(server);
   }
   public void testPBEIdentityLoginModuleTmpFilePassword()
      throws Exception
   {
      // Create a tmp password file for TmpFilePassword
      File tmpPassword = new File(System.getProperty("java.io.tmpdir"), "tmp.password");
      FileWriter writer = new FileWriter(tmpPassword);
      writer.write("testPBEIdentityLoginModuleTmpFilePassword");
      writer.close();

      // Create our own MBeanServer
      MBeanServer server = MBeanServerFactory.createMBeanServer("jboss");
      getLog().info("testPBEIdentityLoginModuleTmpFilePassword");
      // Login using the testPBEIdentityLoginModuleTmpFilePassword config
      UsernamePasswordHandler handler = new UsernamePasswordHandler("sa", "thesecret2".toCharArray());
      LoginContext lc = new LoginContext("testPBEIdentityLoginModuleTmpFilePassword", handler);
      lc.login();
      Subject subject = lc.getSubject();
      Set pcs = subject.getPrivateCredentials(PasswordCredential.class);
      assertEquals("PasswordCredential", 1, pcs.size());
      PasswordCredential pc = (PasswordCredential) pcs.iterator().next();
      assertEquals("PasswordCredential.sa", "sa", pc.getUserName());
      assertEquals("PasswordCredential.password", "thesecret2", new String(pc.getPassword()));
      lc.logout();
      MBeanServerFactory.releaseMBeanServer(server);
      tmpPassword.delete();
   }

   /**
    * @throws Exception
    */
   public void testXmlLoginModuleParsing() throws Exception
   {
      LoginConfigObjectModelFactory lcomf = new LoginConfigObjectModelFactory();
      UsersObjectModelFactory uomf = new UsersObjectModelFactory();

      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("security/login-config2.xml");
      InputStreamReader xmlReader = new InputStreamReader(is);
      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance()
            .newUnmarshaller();
      unmarshaller.mapFactoryToNamespace(uomf, "http://www.jboss.org/j2ee/schemas/XMLLoginModule");
      PolicyConfig config = (PolicyConfig) unmarshaller.unmarshal(xmlReader, lcomf, null);
      AuthenticationInfo info = config.get("testXMLLoginModule");
      assertTrue("test-xml-config != null", info != null);
      AppConfigurationEntry[] entries = info.getAppConfigurationEntry();
      assertTrue("entries.length == 1", entries.length == 1);
      AppConfigurationEntry ace = entries[0];
      assertTrue("org.jboss.security.auth.spi.XMLLoginModule",
         ace.getLoginModuleName().equals("org.jboss.security.auth.spi.XMLLoginModule"));
      Map options = ace.getOptions();
      assertTrue("Options.size == 2", options.size() == 2);
      String guest = (String) options.get("unauthenticatedIdentity");
      assertTrue("guest", guest.equals("guest"));
      Users users = (Users) options.get("userInfo");
      Users.User user = users.getUser("jdukeman");
      String name = user.getName();
      assertTrue("name == jdukeman", name.equals("jdukeman"));
      String passwrd = user.getPassword();
      assertTrue("passwrd == anotherduke", passwrd.equals("anotherduke"));
      String[] roleNames = user.getRoleNames();
      //Take care of Map implementations on various platforms
      assertTrue("roles in (Role2, Role3)",
         roleNames[0].equals("Role2") && roleNames[1].equals("Role3") ||
         roleNames[0].equals("Role3") && roleNames[1].equals("Role2"));

      // Test that 
      AuthenticationInfo testNoModuleOptions = config.get("testNoModuleOptions");
      assertTrue("testNoModuleOptions != null", testNoModuleOptions != null);
      AppConfigurationEntry[] testNoModuleOptionsEntries = testNoModuleOptions.getAppConfigurationEntry();
      assertTrue("entries.length == 1", testNoModuleOptionsEntries.length == 1);
      AppConfigurationEntry testNoModuleOptionsACE = testNoModuleOptionsEntries[0];
      assertTrue("org.jboss.security.auth.spi.XMLLoginModule",
         testNoModuleOptionsACE.getLoginModuleName().equals("org.jboss.security.auth.spi.XMLLoginModule"));
      Map testNoModuleOptionsMap = testNoModuleOptionsACE.getOptions();
      assertTrue("testNoModuleOptionsMap.size("+testNoModuleOptionsMap+") == 0", testNoModuleOptionsMap.size() == 0);
   }

   public void testXMLLoginModule() throws Exception
   {
      // Install the custom JAAS configuration
      XMLLoginConfigImpl config = new XMLLoginConfigImpl();
      config.setConfigResource("security/login-config2.xml");
      config.loadConfig();
      Configuration.setConfiguration(config);

      getLog().info("testXMLLoginModule");
      UsernamePasswordHandler handler = new UsernamePasswordHandler("scott", "echoman".toCharArray());
      LoginContext lc = new LoginContext("testXMLLoginModule", handler);
      lc.login();
      Subject subject = lc.getSubject();
      Set groups = subject.getPrincipals(Group.class);
      assertTrue("Principals contains scott", subject.getPrincipals().contains(new SimplePrincipal("scott")));
      assertTrue("Principals contains Roles", groups.contains(new SimplePrincipal("Roles")));
      assertTrue("Principals contains CallerPrincipal", groups.contains(new SimplePrincipal("CallerPrincipal")));
      Group roles = (Group) groups.iterator().next();
      Iterator groupsIter = groups.iterator();
      while (groupsIter.hasNext())
      {
         roles = (Group) groupsIter.next();
         if (roles.getName().equals("Roles"))
         {
            assertTrue("Echo is a role", roles.isMember(new SimplePrincipal("Echo")));
            assertTrue("Java is NOT a role", roles.isMember(new SimplePrincipal("Java")) == false);
            assertTrue("Coder is NOT a role", roles.isMember(new SimplePrincipal("Coder")) == false);
         }
         else if (roles.getName().equals("CallerPrincipal"))
         {
            getLog().info("CallerPrincipal is " + roles.members().nextElement());
            boolean isMember = roles.isMember(new SimplePrincipal("callerScott"));
            assertTrue("CallerPrincipal is callerScott", isMember);
         }
      }
      lc.logout();

      handler = new UsernamePasswordHandler("stark", "javaman".toCharArray());
      lc = new LoginContext("testXMLLoginModule", handler);
      lc.login();
      subject = lc.getSubject();
      groups = subject.getPrincipals(Group.class);
      assertTrue("Principals contains stark", subject.getPrincipals().contains(new SimplePrincipal("stark")));
      assertTrue("Principals contains Roles", groups.contains(new SimplePrincipal("Roles")));
      assertTrue("Principals contains CallerPrincipal", groups.contains(new SimplePrincipal("CallerPrincipal")));
      groupsIter = groups.iterator();
      while (groupsIter.hasNext())
      {
         roles = (Group) groupsIter.next();
         if (roles.getName().equals("Roles"))
         {
            assertTrue("Echo is NOT a role", roles.isMember(new SimplePrincipal("Echo")) == false);
            assertTrue("Java is a role", roles.isMember(new SimplePrincipal("Java")));
            assertTrue("Coder is a role", roles.isMember(new SimplePrincipal("Coder")));
         }
         else if (roles.getName().equals("CallerPrincipal"))
         {
            getLog().info("CallerPrincipal is " + roles.members().nextElement());
            boolean isMember = roles.isMember(new SimplePrincipal("callerStark"));
            assertTrue("CallerPrincipal is callerStark", isMember);
         }
      }
      lc.logout();

      // Test the usernames with common prefix
      getLog().info("Testing similar usernames");
      handler = new UsernamePasswordHandler("jdukeman", "anotherduke".toCharArray());
      lc = new LoginContext("testXMLLoginModule", handler);
      lc.login();
      subject = lc.getSubject();
      groups = subject.getPrincipals(Group.class);
      assertTrue("Principals contains jdukeman", subject.getPrincipals().contains(new SimplePrincipal("jdukeman")));
      assertTrue("Principals contains Roles", groups.contains(new SimplePrincipal("Roles")));
      assertTrue("Principals contains CallerPrincipal", groups.contains(new SimplePrincipal("CallerPrincipal")));
      groupsIter = groups.iterator();
      while (groupsIter.hasNext())
      {
         roles = (Group) groupsIter.next();
         if (roles.getName().equals("Roles"))
         {
            assertTrue("Role1 is NOT a role", roles.isMember(new SimplePrincipal("Role1")) == false);
            assertTrue("Role2 is a role", roles.isMember(new SimplePrincipal("Role2")));
            assertTrue("Role3 is a role", roles.isMember(new SimplePrincipal("Role3")));
         }
         else if (roles.getName().equals("CallerPrincipal"))
         {
            getLog().info("CallerPrincipal is " + roles.members().nextElement());
            boolean isMember = roles.isMember(new SimplePrincipal("callerJdukeman"));
            assertTrue("CallerPrincipal is callerJdukeman", isMember);
         }
      }
      lc.logout();
   }
   
   /**
    * JBAS-2702
    * @throws Exception
    */
   public void testXmlLoginModuleJaxbParsing() throws Exception
   {
      UsersObjectModelFactory uomf = new UsersObjectModelFactory();

      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("security/login-config3.xml");
      InputStreamReader xmlReader = new InputStreamReader(is);
      
      DefaultSchemaResolver resolver = new DefaultSchemaResolver();

      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      unmarshaller.mapFactoryToNamespace(uomf, "http://www.jboss.org/j2ee/schemas/XMLLoginModule");
      PolicyConfig config = (PolicyConfig) unmarshaller.unmarshal(xmlReader, resolver);
      assertNotNull(config);

   }   
}
