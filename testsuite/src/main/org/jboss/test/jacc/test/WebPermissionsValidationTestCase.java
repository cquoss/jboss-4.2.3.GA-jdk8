/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
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
package org.jboss.test.jacc.test;
 
import java.io.InputStream;
import java.net.URL;
import java.security.Permissions;

import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.metadata.WebMetaData;
import org.jboss.test.JBossTestCase;
import org.jboss.web.WebPermissionMapping;
import org.w3c.dom.Document;

//$Id$

/**
 *  Validate the parsing of web.xml and the creation of JACC Permissions
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Dec 18, 2006 
 *  @version $Revision$
 */
public class WebPermissionsValidationTestCase extends JBossTestCase
{ 
   private boolean DEBUG = true;
   
   public WebPermissionsValidationTestCase(String name)
   {
      super(name); 
   }
   
   public WebMetaData getWebMetaData(InputStream webxml) 
   throws Exception
   { 
	  WebMetaData wmd = new WebMetaData();
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      Document doc = factory.newDocumentBuilder().parse(webxml); 
      wmd.importXml(doc.getDocumentElement()); 
      return wmd; 
   } 
   
   public void testWebPermissions() throws Exception 
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL webxml = cl.getResource("security/jacc/webperm/web.xml");
      assertNotNull("web.xml exists?", webxml);
      WebMetaData wmd = getWebMetaData(webxml.openStream());
      TestJBossPolicyConfiguration tpc = new TestJBossPolicyConfiguration("dummy");
      WebPermissionMapping.createPermissions(wmd, tpc); 
      checkUncheckedPermissions(tpc.getUncheckedPolicy());
      checkExcludedPermissions(tpc.getExcludedPolicy());
      if(DEBUG)
      {
         System.out.println("Unchecked=" + tpc.getUncheckedPolicy().toString());
         System.out.println("Excluded=" + tpc.getExcludedPolicy().toString());
      }
   }  
    
   
   private void checkUncheckedPermissions(Permissions p)
   {
      //WebResourcePermissions
      assertTrue(p.implies(new WebResourcePermission("/unchecked.jsp", (String) null)));
      assertTrue(p.implies(new WebResourcePermission("/sslprotected.jsp", "DELETE,HEAD,OPTIONS,PUT,TRACE")));
      assertTrue(p.implies(new WebResourcePermission("/:/secured.jsp:/unchecked.jsp:/excluded.jsp:/sslprotected.jsp",
            (String) null)));
      assertTrue(p.implies(new WebResourcePermission("/excluded.jsp", "DELETE,HEAD,OPTIONS,PUT,TRACE")));
      assertTrue(p.implies(new WebResourcePermission("/secured.jsp", "DELETE,HEAD,OPTIONS,PUT,TRACE")));
   
      //WebUserDataPermissions
      assertTrue(p.implies(new WebUserDataPermission("/sslprotected.jsp", "GET,POST:CONFIDENTIAL")));
      assertTrue(p.implies(new WebUserDataPermission("/secured.jsp", (String) null)));
      assertTrue(p.implies(new WebUserDataPermission("/:/unchecked.jsp:/secured.jsp:/sslprotected.jsp:/excluded.jsp",
            (String) null)));
      assertTrue(p.implies(new WebUserDataPermission("/sslprotected.jsp", "DELETE,HEAD,OPTIONS,PUT,TRACE")));
      assertTrue(p.implies(new WebUserDataPermission("/unchecked.jsp", (String) null))); 
      assertTrue(p.implies(new WebUserDataPermission("/excluded.jsp", "DELETE,HEAD,OPTIONS,PUT,TRACE"))); 
   }
   
   private void checkExcludedPermissions(Permissions p)
   {
      assertTrue(p.implies(new WebResourcePermission("/excluded.jsp", "GET,POST"))); 
      assertTrue(p.implies(new WebUserDataPermission("/excluded.jsp", "GET,POST")));
   }
}
