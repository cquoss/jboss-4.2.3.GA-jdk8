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
package org.jboss.test.ejbconf.test;

import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.invocation.InvocationType;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.MethodMetaData;
import org.jboss.metadata.XmlFileLoader;
import org.jboss.security.SimplePrincipal;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Tests of ejb-jar.xml metadata parsing.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57211 $
 */
public class MetaDataUnitTestCase extends TestCase 
{
   public MetaDataUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite(MetaDataUnitTestCase.class);
      return suite;
   }

   public void testMethodPermissions() throws Exception
   {
      URL configURL = getClass().getResource("/ejbconf/ejb-jar-permission.xml");
      if( configURL == null )
         throw new Exception("Failed to find /ejbconf/ejb-jar-permission.xml");
      Document configDoc = XmlFileLoader.getDocument(configURL, true);
      ApplicationMetaData appData = new ApplicationMetaData();
      appData.importEjbJarXml(configDoc.getDocumentElement());

      SimplePrincipal echo = new SimplePrincipal("Echo");
      SimplePrincipal echoLocal = new SimplePrincipal("EchoLocal");
      SimplePrincipal internal = new SimplePrincipal("InternalRole");

      BeanMetaData ss = appData.getBeanByEjbName("StatelessSession");
      Class[] sig = {};
      Set perms = ss.getMethodPermissions("create", sig, InvocationType.HOME);
      assertTrue("Echo can invoke StatelessSessionHome.create", perms.contains(echo));
      assertTrue("EchoLocal cannot invoke StatelessSessionHome.create", perms.contains(echoLocal) == false);

      perms = ss.getMethodPermissions("create", sig, InvocationType.LOCALHOME);
      assertTrue("Echo can invoke StatelessSessionLocalHome.create", perms.contains(echo));
      assertTrue("EchoLocal can invoke StatelessSessionLocalHome.create", perms.contains(echoLocal));
   }

}
