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
package org.jboss.ejb3.test.localfromremote.unit;

import javax.naming.InitialContext;

import org.jboss.ejb3.test.localfromremote.StatefulRemote;
import org.jboss.ejb3.test.localfromremote.StatefulRemoteHome;
import org.jboss.ejb3.test.localfromremote.StatelessRemote;
import org.jboss.ejb3.test.localfromremote.StatelessRemoteHome;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class LocalTestCase extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(LocalTestCase.class);

   static boolean deployed = false;
   static int test = 0;

   public LocalTestCase(String name)
   {
      super(name);
   }
 
   public void testStatelessLocalFromRemote() throws Exception
   {
      StatefulRemote bean = (StatefulRemote) getInitialContext().lookup("StatefulBean/remote");
      assertNotNull(bean);
      
      try
      {
         bean.localCall();
         fail("should not be allowed to call local interface remotely");
      }
      catch (javax.ejb.EJBException e)
      {}
   }
   
   public void testStatelessLocalHomeFromRemoteHome() throws Exception
   {
      StatefulRemoteHome home = (StatefulRemoteHome) getInitialContext().lookup("StatefulBean/home");
      StatefulRemote bean = home.create();
      assertNotNull(bean);
      
      try
      {
         bean.localHomeCall();
         fail("should not be allowed to call local interface remotely");
      }
      catch (javax.ejb.EJBException e)
      {}
   }
   
   public void testStatelessRemoteFromRemote() throws Exception
   {
      StatefulRemote bean = (StatefulRemote) getInitialContext().lookup("StatefulBean/remote");
      assertNotNull(bean);
      
      int methodCount = bean.remoteCall();
      assertEquals(1, methodCount);
   }
   
   public void testStatelessRemoteHomeFromRemoteHome() throws Exception
   {
      StatefulRemoteHome home = (StatefulRemoteHome) getInitialContext().lookup("StatefulBean/home");
      StatefulRemote bean = home.create();
      assertNotNull(bean);
      
      int methodCount = bean.remoteHomeCall();
      assertEquals(1, methodCount);
   }
   
   public void testStatefulRemoteFromRemote() throws Exception
   {
      StatelessRemote bean = (StatelessRemote) getInitialContext().lookup("StatelessBean/remote");
      assertNotNull(bean);
      
      int methodCount = bean.remoteCall();
      assertEquals(1, methodCount);
   }
   
   public void testStatefulRemoteHomeFromRemoteHome() throws Exception
   {
      StatelessRemoteHome home = (StatelessRemoteHome) getInitialContext().lookup("StatelessBean/home");
      StatelessRemote bean = home.create();
      assertNotNull(bean);
      
      int methodCount = bean.remoteHomeCall();
      assertEquals(1, methodCount);
   }
   
   public void testStatefulLocalFromRemote() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      StatelessRemote bean = (StatelessRemote) jndiContext.lookup("StatelessBean/remote");
      assertNotNull(bean);
      
      try
      {
         bean.localCall();
         fail("should not be allowed to call local interface remotely");
      }
      catch (javax.naming.NamingException e)
      {
         if (e.getCause() == null || !(e.getCause() instanceof javax.ejb.EJBException))
            fail("should not be allowed to call local interface remotely");
      }
   }
   
   public void testStatefulLocalHomeFromRemoteHome() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      StatelessRemoteHome home = (StatelessRemoteHome) jndiContext.lookup("StatelessBean/home");
      StatelessRemote bean = home.create();
      assertNotNull(bean);
      
      try
      {
         bean.localHomeCall();
         fail("should not be allowed to call local interface remotely");
      }
      catch (javax.ejb.EJBException e)
      {
      }
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(LocalTestCase.class, ""); 
   }
}