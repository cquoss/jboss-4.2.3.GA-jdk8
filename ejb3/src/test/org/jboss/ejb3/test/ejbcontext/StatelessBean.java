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
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision: 65864 $
 */
@Stateless(name = "Stateless")
@Local(StatelessLocal.class)
@Remote(org.jboss.ejb3.test.ejbcontext.Stateless.class)
@LocalBinding(jndiBinding = "StatelessLocal")
@RemoteBinding(jndiBinding = "Stateless")
public class StatelessBean
{
   private static final Logger log = Logger.getLogger(StatelessBean.class);

   @Resource
   SessionContext sessionContext;

   StatelessLocal ejbLocalObject;

   org.jboss.ejb3.test.ejbcontext.Stateless ejbObject;

   public void noop()
   {

   }

   public void testEjbContextLookup() throws Exception
   {
      Stateful stateful = (Stateful) sessionContext.lookup("Stateful");
      stateful.test();
   }

   public Class testInvokedBusinessInterface() throws Exception
   {
      return sessionContext.getInvokedBusinessInterface();
   }

   public Object testBusinessObject(Class businessInterface) throws Exception
   {
      return sessionContext.getBusinessObject(businessInterface);
   }

   public void testEjbObject() throws Exception
   {
      javax.ejb.EJBObject ejbObject = sessionContext.getEJBObject();
      ejbObject.getHandle();
   }

   public void testEjbLocalObject() throws Exception
   {
      javax.ejb.EJBLocalObject ejbObject = sessionContext.getEJBLocalObject();
      ejbObject.getClass();
   }

   public void testSessionContext() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Stateful stateful = (Stateful) jndiContext.lookup("Stateful");
      stateful.setState("testSessionContext");

      EJBLocalObject ejbLocalObject = stateful.getEJBLocalObject();

      StatefulLocal sameLocalBean = (StatefulLocal) ejbLocalObject;
      String state = sameLocalBean.getState();
      if (!state.equals("testSessionContext"))
         throw new Exception("EJBLocalObject does not match originating bean: " + state + " != " + "testSessionContext");

      EJBObject ejbObject = stateful.getEJBObject();
      Stateful sameBean = (Stateful) ejbObject;
      state = sameBean.getState();
      if (!state.equals("testSessionContext"))
         throw new Exception("EJBObject does not match originating bean: " + state + " != " + "testSessionContext");

      this.ejbLocalObject.noop();

      this.ejbObject.noop();
   }

   @PostConstruct
   public void postConstruct()
   {
      ejbLocalObject = (StatelessLocal) sessionContext.getEJBLocalObject();
      ejbObject = (org.jboss.ejb3.test.ejbcontext.Stateless) sessionContext.getEJBObject();
   }
}
