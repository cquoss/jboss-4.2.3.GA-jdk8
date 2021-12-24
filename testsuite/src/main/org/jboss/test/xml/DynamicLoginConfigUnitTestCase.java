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
package org.jboss.test.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import javax.security.auth.login.AppConfigurationEntry;

import junit.framework.TestCase;
import org.jboss.security.auth.spi.Users;
import org.jboss.security.auth.login.PolicyConfig;
import org.jboss.security.auth.login.AuthenticationInfo;
import org.jboss.util.xml.JBossEntityResolver;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.DefaultSchemaResolver;

/**
 * Test unmarshalling xml documents conforming to mbean-service_1_0.xsd into
 * the org.jboss.test.xml.mbeanserver.Services and related objects.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57211 $
 */
public class DynamicLoginConfigUnitTestCase
   extends TestCase
{
   /**
    * A test of unmarshalling an element from a document without any knowledge
    * of the associated schema.
    * 
    * @throws Exception
    */ 
   public void testConfig() throws Exception
   {
      System.out.println("DynamicLoginConfigUnitTestCase.testConfig");
      // Set the jboss url protocol handler path
      System.setProperty("java.protocol.handler.pkgs", "org.jboss.net.protocol");
      InputStream is = getResource("xml/loginconfig/config.xml");

      /* Parse the element content using the Unmarshaller starting with an
      empty schema since we don't know anything about it. This is not quite
      true as we set the schema baseURI to the resources/xml/loginconfig/ directory
      so that the xsds can be found, but this baseURI
      can be easily specified to the SARDeployer, or the schema can be made
      available to the entity resolver via some other configuration.
      */
      System.out.println("DefaultSchemaResolver.CodeSource: "+DefaultSchemaResolver.class.getProtectionDomain().getCodeSource());
      DefaultSchemaResolver resolver = new DefaultSchemaResolver(new JBossEntityResolver());
      URL url = Thread.currentThread().getContextClassLoader().getResource("xml/loginconfig/");
      System.out.println("Using baseURI: "+url);
      resolver.setBaseURI(url.toString());
      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      Object root = unmarshaller.unmarshal(is, resolver);
      PolicyConfig config = (PolicyConfig) root;
      is.close();

      // Validate the bindings
      AuthenticationInfo info = config.get("conf1");
      assertNotNull("conf1", info);
      AppConfigurationEntry[] entry = info.getAppConfigurationEntry();
      assertTrue("entry.length == 1", entry.length == 1);
      assertTrue("entry[0].getLoginModuleName() == XMLLoginModule",
         entry[0].getLoginModuleName().equals("org.jboss.security.auth.spi.XMLLoginModule"));
      Map options = entry[0].getOptions();
      assertTrue("There are two options", options.size() == 2);
      String unauthenticatedIdentity = (String) options.get("unauthenticatedIdentity");
      assertNotNull("options.unauthenticatedIdentity exists", unauthenticatedIdentity);
      assertTrue("options.unauthenticatedIdentity == guest",
         unauthenticatedIdentity.equals("guest"));

      Users users = (Users) options.get("userInfo");
      assertNotNull("options.userInfo is a Users", users);
      assertTrue("Users.size("+users.size()+") is 6", users.size() == 6);
      Users.User jduke = users.getUser("jduke");
      assertNotNull("jduke is a user", jduke);
      assertTrue("jduke.password == theduke", jduke.getPassword().equals("theduke"));
      String[] roleNames = jduke.getRoleNames("Roles");
      HashSet roles = new HashSet(Arrays.asList(roleNames));
      assertTrue("jduke has roles", roles.size() == 3);
      assertTrue("Role1 is a role", roles.contains("Role1"));
      assertTrue("Role2 is a role", roles.contains("Role2"));
      assertTrue("Echo is a role", roles.contains("Echo"));
   }

   // Private

   private InputStream getResource(String path)
      throws IOException
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource(path);
      if(url == null)
      {
         fail("URL not found: " + path);
      }
      return url.openStream();
   }
}
