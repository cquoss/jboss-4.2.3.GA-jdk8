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
package org.jboss.test.txpropiiop.ejb.a;

import java.rmi.RemoteException;
import java.util.Properties;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.jboss.logging.Logger;
import org.jboss.test.txpropiiop.interfaces.b.SessionB;
import org.jboss.test.txpropiiop.interfaces.b.SessionBHome;
import org.jboss.tm.iiop.TxClientInterceptor;
import org.omg.CORBA.ORB;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.TransIdentity;
import org.omg.CosTransactions.otid_t;

/**
 * A SessionA.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57211 $
 */
public class SessionAEJB implements SessionBean
{
   private static final Logger log = Logger.getLogger(SessionAEJB.class);
   
   private static SessionB sessionB;
   
   public void invokeSessionB()
   {
      try
      {
         SessionB session = getSessionB();
         
         String hello = "Hello";
         String result = session.sayHello(hello);
         validate(hello, result);
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
   }
   
   public void testTxToNotSupported()
   {
      setTransaction();
      try
      {
         SessionB session = getSessionB();
         
         String hello = "Hello";
         String result = session.testNotSupported(hello);
         validate(hello, result);
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
      finally
      {
         unsetTransaction();
      }
   }
   
   public void testTxToRequired()
   {
      setTransaction();
      try
      {
         SessionB session = getSessionB();
         
         String hello = "Hello";
         String result = session.testRequired(hello);
         throw new EJBException("Expected RemoteException");
      }
      catch (EJBException e)
      {
         throw e;
      }
      catch (RemoteException expected)
      {
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
      finally
      {
         unsetTransaction();
      }
   }
   
   public void testTxToSupports()
   {
      setTransaction();
      try
      {
         SessionB session = getSessionB();
         
         String hello = "Hello";
         String result = session.testSupports(hello);
         throw new EJBException("Expected RemoteException");
      }
      catch (EJBException e)
      {
         throw e;
      }
      catch (RemoteException expected)
      {
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
      finally
      {
         unsetTransaction();
      }
   }
   
   public void testTxToRequiresNew()
   {
      setTransaction();
      try
      {
         SessionB session = getSessionB();
         
         String hello = "Hello";
         String result = session.testRequiresNew(hello);
         validate(hello, result);
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
      finally
      {
         unsetTransaction();
      }
   }
   
   public void testTxToMandatory()
   {
      setTransaction();
      try
      {
         SessionB session = getSessionB();
         
         String hello = "Hello";
         String result = session.testMandatory(hello);
         throw new EJBException("Expected RemoteException");
      }
      catch (EJBException e)
      {
         throw e;
      }
      catch (RemoteException expected)
      {
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
      finally
      {
         unsetTransaction();
      }
   }
   
   public void testTxToNever()
   {
      setTransaction();
      try
      {
         SessionB session = getSessionB();
         
         String hello = "Hello";
         String result = session.testNever(hello);
         throw new EJBException("Expected RemoteException");
      }
      catch (EJBException e)
      {
         throw e;
      }
      catch (RemoteException expected)
      {
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
      finally
      {
         unsetTransaction();
      }
   }
   
   public void testNoTxToNotSupported()
   {
      try
      {
         SessionB session = getSessionB();
         
         String hello = "Hello";
         String result = session.testNotSupported(hello);
         validate(hello, result);
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
   }
   
   public void testNoTxToRequired()
   {
      try
      {
         SessionB session = getSessionB();
         
         String hello = "Hello";
         String result = session.testRequired(hello);
         validate(hello, result);
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
   }
   
   public void testNoTxToSupports()
   {
      try
      {
         SessionB session = getSessionB();
         
         String hello = "Hello";
         String result = session.testSupports(hello);
         validate(hello, result);
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
   }
   
   public void testNoTxToRequiresNew()
   {
      try
      {
         SessionB session = getSessionB();
         
         String hello = "Hello";
         String result = session.testRequiresNew(hello);
         validate(hello, result);
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
   }
   
   public void testNoTxToMandatory()
   {
      try
      {
         SessionB session = getSessionB();
         
         String hello = "Hello";
         String result = session.testMandatory(hello);
         throw new EJBException("Expected RemoteException");
      }
      catch (EJBException e)
      {
         throw e;
      }
      catch (RemoteException expected)
      {
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
   }
   
   public void testNoTxToNever()
   {
      try
      {
         SessionB session = getSessionB();
         
         String hello = "Hello";
         String result = session.testNever(hello);
         validate(hello, result);
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
   }
   
   public void ejbCreate() throws CreateException
   {
   }
   
   public void ejbActivate()
   {
   }

   public void ejbPassivate()
   {
   }
   
   public void ejbRemove()
   {
   }
   
   public void setSessionContext(SessionContext ctx)
   {
   }

   protected void setTransaction()
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
   
   private static void validate(String hello, String result) throws Exception
   {
      if (hello == result)
         throw new EJBException("Should be pass by value");
      if (hello.equals(result) == false)
         throw new EJBException("Did not get expected 'Hello'");
   }
   
   private static SessionB getSessionB() throws Exception
   {
      if (sessionB == null)
      {
         SessionBHome home = (SessionBHome) lookup("SessionB", SessionBHome.class);
         sessionB = home.create();
      }
      return sessionB;
   }

   private static Object lookup(String name, Class clazz) throws Exception
   {
      Properties jndiProps = new Properties();
      jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");
      jndiProps.put(Context.PROVIDER_URL, "corbaloc::" + System.getProperty("jboss.bind.address" , "localhost") + ":3528/JBoss/Naming/root");
      Context ctx = new InitialContext(jndiProps);
      Object obj = ctx.lookup(name);
      Object result = PortableRemoteObject.narrow(obj, clazz);
      return result;
   }
   
   private static ORB getORB()
   {
      try
      {
         return (ORB) new InitialContext().lookup("java:comp/ORB");
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
   }
}
