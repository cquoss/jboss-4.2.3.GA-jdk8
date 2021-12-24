/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
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

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@Stateless(name="StatelessBean")
@RemoteBinding(jndiBinding = "Stateless")
@Remote(org.jboss.ejb3.test.stateful.Stateless.class)
public class StatelessBean implements org.jboss.ejb3.test.stateful.Stateless
{
	private static final Logger log = Logger.getLogger(StatelessBean.class);
   
	@EJB(beanName="StatefulBean")
	private Stateful stateful;
	
	@EJB
	private ClusteredStateful clusteredStateful;
	   
	public void testInjection() throws Exception
	{
		stateful.getState();
	   
		clusteredStateful.getState();
      
      InitialContext jndiContext = new InitialContext();
      list(jndiContext, "java:comp");
      list(jndiContext, "java:comp/env");
      list(jndiContext, "java:comp/env/ejb");
      list(jndiContext, "java:comp.ejb3");
      list(jndiContext, "java:comp.ejb3/env");
      list(jndiContext, "java:comp.ejb3/env/ejb");
      
      StatefulLocal sfsb = (StatefulLocal)jndiContext.lookup("java:comp/env/ejb/StatefulLocal");
      sfsb.getState();
      
      sfsb = (StatefulLocal)jndiContext.lookup("java:comp.ejb3/env/ejb/StatefulLocal");
      sfsb.getState();
	}
   
   private void list(Context ctx, String name) throws javax.naming.NamingException
   {
      log.info("*** Listing ... " + name);
      javax.naming.NamingEnumeration<javax.naming.NameClassPair> bindings = ctx.list(name);
      while(bindings.hasMore())
      {
         javax.naming.NameClassPair pair = bindings.next();
         log.info("  " + " " + pair.getName() + " " + pair.getClassName());
      }
   }
}
