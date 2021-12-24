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
package org.jboss.test.txpropiiop.test;

import java.rmi.RemoteException;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import junit.framework.Test;
import org.jboss.iiop.CorbaORB;
import org.jboss.test.JBossIIOPTestCase;
import org.jboss.test.txpropiiop.interfaces.a.SessionA;
import org.jboss.test.txpropiiop.interfaces.a.SessionAHome;
import org.jboss.test.txpropiiop.interfaces.b.SessionB;
import org.jboss.test.txpropiiop.interfaces.b.SessionBHome;
import org.jboss.tm.iiop.TxClientInterceptor;
import org.omg.CORBA.ORB;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.TransIdentity;
import org.omg.CosTransactions.otid_t;

/**
 * A Tx Propagation UnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57211 $
 */
public class TxPropIIOPUnitTestCase extends JBossIIOPTestCase
{
   private static SessionA sessionA;
   private static SessionB sessionB;
   
   public TxPropIIOPUnitTestCase(String name) throws Exception
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(TxPropIIOPUnitTestCase.class, "txpropiiopB.jar");
   }
   
   protected void setUp() throws Exception
   {
      log.debug("====== Starting test " + getName());
      /*deploy("txpropiiopA.jar");
      try
      {
         deploy("txpropiiopB.jar");
      }
      catch (Throwable t)
      {
         log.error("Error deploying txpropiiopB", t);
         undeploy("txpropiiopA.jar");
      }*/
   }
   
   protected void tearDown() throws Exception
   {
      log.debug("====== Ended test " + getName());
      /*try
      {
         undeploy("txpropiiopB.jar");
      }
      finally
      {
         undeploy("txpropiiopA.jar");
      }*/
   }
   
   /* public void testRedeployment() throws Exception
   {
      getSessionA().invokeSessionB();
      undeploy("txpropiiopB.jar");
      deploy("txpropiiopB.jar");
      getSessionA().invokeSessionB();
   }*/
   
   public void testTxToNotSupported() throws Exception
   {
      setTransaction();
      try
      {
         String hello = "Hello";
         String result = getSessionB().testNotSupported(hello);
         validate(hello, result);
      }
      finally
      {
         unsetTransaction();
      }
   }
   
   public void testTxToRequired() throws Exception
   {
      setTransaction();
      try
      {
         String hello = "Hello";
         String result = getSessionB().testRequired(hello);
         fail("Expected remote exception");
      }
      catch (RemoteException expected)
      {
      }
      finally
      {
         unsetTransaction();
      }
   }
   
   public void testTxToRequiresNew() throws Exception
   {
      setTransaction();
      try
      {
         String hello = "Hello";
         String result = getSessionB().testRequiresNew(hello);
         validate(hello, result);
      }
      finally
      {
         unsetTransaction();
      }
   }
   
   public void testTxToSupports() throws Exception
   {
      setTransaction();
      try
      {
         String hello = "Hello";
         String result = getSessionB().testSupports(hello);
         fail("Expected remote exception");
      }
      catch (RemoteException expected)
      {
      }
      finally
      {
         unsetTransaction();
      }
   }
   
   public void testTxToMandatory() throws Exception
   {
      setTransaction();
      try
      {
         String hello = "Hello";
         String result = getSessionB().testMandatory(hello);
         fail("Expected remote exception");
      }
      catch (RemoteException expected)
      {
      }
      finally
      {
         unsetTransaction();
      }
   }
   
   public void testTxToNever() throws Exception
   {
      setTransaction();
      try
      {
         String hello = "Hello";
         String result = getSessionB().testNever(hello);
         fail("Expected remote exception");
      }
      catch (RemoteException expected)
      {
      }
      finally
      {
         unsetTransaction();
      }
   }
   
   public void testNoTxToNotSupported() throws Exception
   {
      String hello = "Hello";
      String result = getSessionB().testNotSupported(hello);
      validate(hello, result);
   }
   
   public void testNoTxToRequired() throws Exception
   {
      String hello = "Hello";
      String result = getSessionB().testNotSupported(hello);
      validate(hello, result);
   }
   
   public void testNoTxToRequiresNew() throws Exception
   {
      String hello = "Hello";
      String result = getSessionB().testNotSupported(hello);
      validate(hello, result);
   }
   
   public void testNoTxToSupports() throws Exception
   {
      String hello = "Hello";
      String result = getSessionB().testNotSupported(hello);
      validate(hello, result);
   }
   
   public void testNoTxToMandatory() throws Exception
   {
      try
      {
         String hello = "Hello";
         String result = getSessionB().testMandatory(hello);
         fail("Expected remote exception");
      }
      catch (RemoteException expected)
      {
      }
   }
   
   public void testNoTxToNever() throws Exception
   {
      String hello = "Hello";
      String result = getSessionB().testNotSupported(hello);
      validate(hello, result);
   }
   
   private static void validate(String hello, String result) throws Exception
   {
      if (hello == result)
         fail("Should be pass by value");
      assertEquals(hello, result);
   }

   private SessionA getSessionA() throws Exception
   {
      if (sessionA == null)
      {
         SessionAHome home = (SessionAHome) lookup("SessionA", SessionAHome.class);
         sessionA = home.create();
      }
      return sessionA;
   }
   
   private SessionB getSessionB() throws Exception
   {
      if (sessionB == null)
      {
         SessionBHome home = (SessionBHome) lookup("SessionB", SessionBHome.class);
         sessionB = home.create();
      }
      return sessionB;
   }

   protected void setTransaction() throws Exception
   {
      PropagationContext pc = new PropagationContext();
      pc.parents = new TransIdentity[0];
      pc.current = new TransIdentity();
      pc.current.otid = new otid_t();
      pc.current.otid.formatID = 666;
      pc.current.otid.bqual_length = 1;
      pc.current.otid.tid = new byte[] { (byte) 1 };
      pc.implementation_specific_data = getORB().create_any();
      pc.implementation_specific_data.insert_long(1);
      TxClientInterceptor.setOutgoingPropagationContext(pc);
   }
   
   protected void unsetTransaction()
   {
      TxClientInterceptor.unsetOutgoingPropagationContext();
   }
   
   private static ORB getORB() throws Exception
   {
      return CorbaORB.getInstance();
   }

   private Object lookup(String name, Class clazz) throws Exception
   {
      Context ctx = getInitialContext();
      Object obj = ctx.lookup(name);
      Object result = PortableRemoteObject.narrow(obj, clazz);
      return result;
   }
}
