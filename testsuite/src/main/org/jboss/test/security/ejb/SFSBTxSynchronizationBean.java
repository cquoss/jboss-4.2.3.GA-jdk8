/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.security.ejb;

import java.rmi.RemoteException;
import java.security.Principal;
import javax.ejb.CreateException; 
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.SessionSynchronization; 
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.apache.log4j.Logger;
import org.jboss.test.security.interfaces.StatefulSession;
import org.jboss.test.security.interfaces.StatefulSessionHome;

/**
 * SFSB that has the session synchronization methods that invoke
 * the getCallerPrincipal (Reference: JBAS-4087)
 *
 * @author Anil.Saldhana@redhat.com
 * @version $Revision: 57211 $
 */
public class SFSBTxSynchronizationBean implements SessionBean,SessionSynchronization
{ 
   private static final long serialVersionUID = 1L;
   
   private static Logger log = Logger.getLogger(SFSBTxSynchronizationBean.class);
   private SessionContext sessionContext;
   private String state;

   public void ejbCreate(String state) throws CreateException
   {
      this.state = state;
      log.debug("ejbCreate("+state+") called");
      Principal p = sessionContext.getCallerPrincipal();
      log.debug("ejbCreate, callerPrincipal="+p);
   }

   public void ejbActivate()
   {
      log.debug("ejbActivate() called");
   }

   public void ejbPassivate()
   {
      log.debug("ejbPassivate() called");
   }

   public void ejbRemove()
   {
      log.debug("ejbRemove() called");
   }

   public void setSessionContext(SessionContext context)
   {
      sessionContext = context;
   }

   public String echo(String arg)
   {
      log.debug("echo, arg="+arg);
      Principal p = sessionContext.getCallerPrincipal();
      log.debug("echo, callerPrincipal="+p);
      
      //Now check whether we are able to call the bean with run-as role
      try
      { 
         InitialContext jndiContext = new InitialContext();
         Object obj = jndiContext.lookup("java:comp/env/ejb/RunAsSFSB");
         obj = PortableRemoteObject.narrow(obj, StatefulSessionHome.class);
         StatefulSessionHome home = (StatefulSessionHome) obj; 
         log.debug("Found StatefulSessionHome");
         // The create should be allowed to call getCallerPrincipal
         StatefulSession bean = home.create("testStatefulCreateCaller");
         // Need to invoke a method to ensure an ejbCreate call
         bean.echo("testStatefulCreateCaller"); 
      }
      catch(Exception e)
      {
         throw new RuntimeException(e);
      }
      return arg;
   }

   public void afterBegin() throws EJBException, RemoteException
   {  
      Principal p = sessionContext.getCallerPrincipal();
      log.error("afterBegin():callerPrincipal="+p);
   }

   public void afterCompletion(boolean committed) throws EJBException, RemoteException
   {  
      Principal p = sessionContext.getCallerPrincipal();
      log.error("afterCompletion:callerPrincipal="+p);
   }

   public void beforeCompletion() throws EJBException, RemoteException
   {  
      Principal p = sessionContext.getCallerPrincipal();
      log.error("beforeCompletion():callerPrincipal="+p);
   }
}
