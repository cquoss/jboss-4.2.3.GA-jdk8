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
package org.jboss.test;

import java.net.URL;
import java.util.Properties;

import javax.naming.InitialContext;

/**
 * Base class for IIOP tests that sets up the InitialContext to use the
 * cosnaming service.
 * 
 * @author Scott.Stark@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * 
 * @version $Revision: 75685 $
 */
public class JBossIIOPTestCase extends JBossTestCase
{
   // JBAS-5758, cache the initial context to avoid premature ORB destruction
   protected InitialContext ic = null;

   protected Properties jndiProps;
   
   public JBossIIOPTestCase(String name)
   {
      super(name);
   }

   // Override getInitialContext() ----------------------------------
   protected InitialContext getInitialContext() throws Exception
   {
      if (jndiProps == null)
      {
         URL url  = ClassLoader.getSystemResource("cosnaming.jndi.properties");
         jndiProps = new java.util.Properties();
         jndiProps.load(url.openStream());
         String host = System.getProperty("jbosstest.server.host", "localhost");
         String corbaloc = "corbaloc::"+host+":3528/JBoss/Naming/root";
         jndiProps.setProperty("java.naming.provider.url", corbaloc);
      }

      if (ic == null)
         ic = new InitialContext(jndiProps);
      
      return ic;
   }
   
   protected InitialContext getInitialJnpContext() throws Exception
   {
      return super.getInitialContext();
   }
}
