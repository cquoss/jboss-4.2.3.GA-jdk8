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
package org.jboss.ejb3.test.stateful;

import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.ejb.Init;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.ejb.PrePassivate;
import javax.ejb.PostActivate;
import javax.interceptor.Interceptors;
import javax.naming.InitialContext;

import org.jboss.ejb3.Container;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.cache.simple.CacheConfig;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.logging.Logger;
import org.jboss.serial.io.JBossObjectOutputStream;
import org.jboss.serial.io.JBossObjectInputStream;

/**
 * Comment
 *
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision: 65906 $
 */
@Stateful(name="StatefulBean")
@Remote({org.jboss.ejb3.test.stateful.Stateful.class, Stateful21.class})
@Local(org.jboss.ejb3.test.stateful.StatefulLocal.class)
@RemoteBinding(jndiBinding = "Stateful",
               interceptorStack="RemoteBindingStatefulSessionClientInterceptors",
               factory = org.jboss.ejb3.test.stateful.StatefulRemoteProxyFactory.class)
@CacheConfig(maxSize = 1000, idleTimeoutSeconds = 1)
@SecurityDomain("test")
@Resources({@Resource(name="jdbc/ds", mappedName="java:/DefaultDS")})
public class StatefulBean implements org.jboss.ejb3.test.stateful.Stateful
{
   private static final Logger log = Logger.getLogger(StatefulBean.class);
   
   @Resource
   private SessionContext sessionContext;
   
   @Resource(mappedName="java:/DefaultDS")
   private transient javax.sql.DataSource datasource;
   
   @Resource(mappedName="java:/ConnectionFactory")
   public transient javax.jms.QueueConnectionFactory connectionFactory; 
   
   private StatefulLocal localStateful;

   private String state;
   private boolean wasPassivated = false;
   
   public void lookupStateful() throws Exception
   {
      localStateful = (StatefulLocal)new InitialContext().lookup("StatefulBean/local");
   }
   
   public void testStateful() throws Exception
   {
      localStateful.getState();
   }

   @Interceptors(MyInterceptor.class)
   public String getInterceptorState()
   {
      throw new RuntimeException("NOT REACHABLE");
   }

   @Interceptors(MyInterceptor.class)
   public void setInterceptorState(String param)
   {
      throw new RuntimeException("NOT REACHABLE");
   }
   
   public boolean testSessionContext()
   {
      return sessionContext.isCallerInRole("role");
   }
   
   public void testResources() throws Exception
   {
      datasource.toString();
      connectionFactory.toString();
      
      javax.sql.DataSource ds = (javax.sql.DataSource)new InitialContext().lookup(Container.ENC_CTX_NAME + "/env/jdbc/ds");
      ds.toString();
   }

   public String getState() throws Exception
   {
      Thread.sleep(1000);
      return state;
   }

   public void setState(String state) throws Exception
   {
      Thread.sleep(1000);
      this.state = state;
   }

   public boolean interceptorAccessed()
   {
      return RemoteBindingInterceptor.accessed;
   }

   public void testThrownException() throws Exception
   {
      throw new Exception();
   }

   public void testExceptionCause() throws Exception
   {
      Object o = null;
      o.toString();
   }

   @PrePassivate
   public void passivate()
   {
      log.info("************ passivating");  
      wasPassivated = true;
   }
   
   @PostActivate
   public void activate()
   {
      log.info("************ activating");
   }

   public void testSerializedState(String state)
   {
      this.state = state;

      StatefulBean bean = null;
      try
      {
         ObjectOutputStream out;

         ByteArrayOutputStream baos = new ByteArrayOutputStream();

         out = new JBossObjectOutputStream(baos, false);
         out.writeObject(this);
         out.flush();

         ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

         JBossObjectInputStream is = new JBossObjectInputStream(bais);
         bean = (StatefulBean)is.readObject();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException(e);
      }

      if (!state.equals(bean.state)) throw new RuntimeException("failed to serialize: " + bean.state);
   }

   public boolean wasPassivated()
   {
      return wasPassivated;
   }

   public void clearPassivated()
   {
      wasPassivated = false;
   }

   @Init
   public void ejbCreate(Integer state)
   {
      this.state=state.toString();
   }

   @Init
   public void ejbCreate(State state)
   {
      this.state=state.getState();
   }

   @Init
   public void ejbCreate(String state)
   {
      this.state=state;
   }
   
   @Remove
   public void removeBean()
   {
      
   }
}
