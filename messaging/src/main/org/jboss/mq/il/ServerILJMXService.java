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
package org.jboss.mq.il;

import java.util.Properties;

import javax.jms.IllegalStateException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.mq.GenericConnectionFactory;
import org.jboss.mq.SpyConnectionFactory;
import org.jboss.mq.SpyXAConnectionFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * This abstract class handles life cycle managment of the ServeIL. Should be
 * extended to provide a full implementation.
 * 
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks </a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57198 $
 * 
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 */
public abstract class ServerILJMXService extends ServiceMBeanSupport implements ServerILJMXServiceMBean
{

   private ObjectName jbossMQService;

   protected Invoker jmsServer;

   protected String connectionFactoryJNDIRef;

   protected String xaConnectionFactoryJNDIRef;

   protected long pingPeriod = 60000L;
   
   /** The client id */
   protected String clientID;

   /**
    * Get the value of JBossMQService.
    * 
    * @return value of JBossMQService.
    * 
    * @jmx:managed-attribute
    */
   public ObjectName getJBossMQService()
   {
      return jbossMQService;
   }

   /**
    * Set the value of JBossMQService.
    * 
    * @param v Value to assign to JBossMQService.
    * 
    * @jmx:managed-attribute
    */
   public void setInvoker(ObjectName jbossMQService)
   {
      this.jbossMQService = jbossMQService;
   }

   public void startService() throws Exception
   {
      jmsServer = (Invoker) getServer().getAttribute(jbossMQService, "Invoker");
      if (jmsServer == null)
      {
         throw new IllegalStateException("Cannot find JBossMQService!");
      } // end of if ()
   }

   public void stopService() throws Exception
   {
      jmsServer = null;
   }

   /**
    * @param newConnectionFactoryJNDIRef the JNDI reference where the
    *           connection factory should be bound to
    * 
    * @jmx:managed-attribute
    */
   public void setConnectionFactoryJNDIRef(java.lang.String newConnectionFactoryJNDIRef)
   {
      connectionFactoryJNDIRef = newConnectionFactoryJNDIRef;
   }

   /**
    * @param newXaConnectionFactoryJNDIRef java.lang.String the JNDI reference
    *           where the xa connection factory should be bound to
    * 
    * @jmx:managed-attribute
    */
   public void setXAConnectionFactoryJNDIRef(java.lang.String newXaConnectionFactoryJNDIRef)
   {
      xaConnectionFactoryJNDIRef = newXaConnectionFactoryJNDIRef;
   }

   /**
    * @return The ClientConnectionProperties value @returns Properties contains
    *         all the parameters needed to create a connection from the client
    *         to this IL
    */
   public java.util.Properties getClientConnectionProperties()
   {
      Properties rc = new Properties();
      rc.setProperty(ServerILFactory.PING_PERIOD_KEY, "" + pingPeriod);
      if (clientID != null)
         rc.setProperty(ServerILFactory.CLIENTID, clientID);
      return rc;
   }

   /**
    * @return The ServerIL value @returns ServerIL An instance of the Server
    *         IL, used for
    */
   public abstract ServerIL getServerIL();

   /**
    * @return java.lang.String the JNDI reference where the connection factory
    *         should be bound to
    * 
    * @jmx:managed-attribute
    */
   public java.lang.String getConnectionFactoryJNDIRef()
   {
      return connectionFactoryJNDIRef;
   }

   /**
    * @return java.lang.String the JNDI reference where the xa connection
    *         factory should be bound to
    * 
    * @jmx:managed-attribute
    */
   public java.lang.String getXAConnectionFactoryJNDIRef()
   {
      return xaConnectionFactoryJNDIRef;
   }

   /**
    * Binds the connection factories for this IL
    * 
    * @throws javax.naming.NamingException it cannot be unbound
    */
   public void bindJNDIReferences() throws javax.naming.NamingException
   {
      GenericConnectionFactory gcf = new GenericConnectionFactory(getServerIL(), getClientConnectionProperties());
      SpyConnectionFactory scf = new SpyConnectionFactory(gcf);
      SpyXAConnectionFactory sxacf = new SpyXAConnectionFactory(gcf);

      // Get an InitialContext
      InitialContext ctx = getInitialContext();
      rebind(ctx, connectionFactoryJNDIRef, scf);
      rebind(ctx, xaConnectionFactoryJNDIRef, sxacf);

   }

   protected InitialContext getInitialContext()
           throws NamingException
   {
      InitialContext ctx = new InitialContext();
      return ctx;
   }

   protected void rebind(Context ctx, String name, Object val) throws NamingException
   {
      // Bind val to name in ctx, and make sure that all intermediate contexts
      // exist
      javax.naming.Name n = ctx.getNameParser("").parse(name);
      while (n.size() > 1)
      {
         String ctxName = n.get(0);
         try
         {
            ctx = (Context) ctx.lookup(ctxName);
         }
         catch (javax.naming.NameNotFoundException e)
         {
            ctx = ctx.createSubcontext(ctxName);
         }
         n = n.getSuffix(1);
      }

      ctx.rebind(n.get(0), val);
   }

   /**
    * Unbinds the connection factories for this IL
    * 
    * @throws javax.naming.NamingException it cannot be unbound
    */
   public void unbindJNDIReferences() throws javax.naming.NamingException
   {
      // Get an InitialContext
      InitialContext ctx = getInitialContext();
      ctx.unbind(connectionFactoryJNDIRef);
      ctx.unbind(xaConnectionFactoryJNDIRef);
   }

   /**
    * @return Description of the Returned Value
    * @exception Exception Description of Exception
    * @throws javax.naming.NamingException if the server is not found
    */
   public Invoker getJMSServer()
   {
      return jmsServer;
   }

   /**
    * @return Description of the Returned Value
    * @exception Exception Description of Exception
    * @throws javax.naming.NamingException if the server is not found
    */
   public Invoker lookupJMSServer()
   {
      return getJMSServer();
   }

   /**
    * @return long the period of time in ms to wait between connection pings
    *         factory should be bound to
    * 
    * @jmx:managed-attribute
    */
   public long getPingPeriod()
   {
      return pingPeriod;
   }

   /**
    * @param period long the period of time in ms to wait between connection
    *           pings
    * 
    * @jmx:managed-attribute
    */
   public void setPingPeriod(long period)
   {
      pingPeriod = period;
   }

   /**
    * Get the client id for this connection factory
    *  
    * @jmx:managed-attribute
    * @return the client id
    */
   public String getClientID()
   {
      return clientID;
   }

   /**
    * Set the client id for this connection factory
    * 
    * @jmx:managed-attribute
    * @param clientID the client id
    */
   public void setClientID(String clientID)
   {
      this.clientID = clientID;
   }

}
