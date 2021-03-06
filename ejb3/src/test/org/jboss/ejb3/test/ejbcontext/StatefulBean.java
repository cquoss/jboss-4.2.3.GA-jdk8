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
package org.jboss.ejb3.test.ejbcontext;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;

import javax.naming.InitialContext;

import javax.ejb.Remote;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.RemoteBindings;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision: 63485 $
 */
@Stateful(name="Stateful")
@Remote({org.jboss.ejb3.test.ejbcontext.Stateful.class, StatefulRemote.class})
@Local(StatefulLocal.class)
@LocalBinding(jndiBinding="StatefulLocal")
@RemoteBindings({@RemoteBinding(jndiBinding = "Stateful"), @RemoteBinding(jndiBinding = "StatefulRemote")})
public class StatefulBean
   implements StatefulRemote
{
   private static final Logger log = Logger.getLogger(StatefulBean.class);
   
   @Resource
   SessionContext sessionContext;
   
   @EJB(mappedName="StatefulRemote")
   StatefulRemote statefulRemote = null;
   
   @EJB StatefulLocalOnly statefulLocalOnly;

   EJBLocalObject ejbLocalObject;
   EJBObject ejbObject;
   String state = "";
   
   public void testEjbContext() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      EJBContext ejbContext = (EJBContext)jndiContext.lookup("java:comp.ejb3/EJBContext");
      log.info("EJBContext " + ejbContext);
   }
   
   public void test()
   {
      
   }
   
   public EJBLocalObject getEJBLocalObject()
   {
      return ejbLocalObject;
   }
   
   public EJBObject getEJBObject()
   {
      return ejbObject;
   }

   public String getState()
   {
      return state;
   }

   public void setState(String state)
   {
      this.state = state;
   }

   public Object getBusinessObject() throws Exception
   {
      return sessionContext.getBusinessObject(org.jboss.ejb3.test.ejbcontext.Stateful.class);
   }


   public Class testInvokedBusinessInterface() throws Exception
   {
      return sessionContext.getInvokedBusinessInterface();
   }
   
   public Class testInvokedBusinessInterface2() throws Exception
   {
      return sessionContext.getInvokedBusinessInterface();
   }
   
   public Class testLocalInvokedBusinessInterface() throws Exception
   {
      return statefulRemote.testInvokedBusinessInterface2();
   }
   
   @PostConstruct
   public void postConstruct()
   {
      ejbLocalObject = sessionContext.getEJBLocalObject();
      ejbObject = sessionContext.getEJBObject();
   }
   
   public Object testLocalOnlyGetBusinessObject() throws Exception
   {
	   return statefulLocalOnly.getBusinessObject();
   }
}
