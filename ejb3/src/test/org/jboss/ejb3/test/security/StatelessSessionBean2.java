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
package org.jboss.ejb3.test.security;

import java.security.Principal;
import javax.ejb.EJB;
import javax.ejb.EJBs;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBException;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.ejb3.Container;
import org.jboss.logging.Logger;

/** A SessionBean that access the Entity bean to test Principal
identity propagation.

@author Scott.Stark@jboss.org
@version $Revision: 60233 $
*/
@Stateless(name="StatelessSession2")
@Remote(org.jboss.ejb3.test.security.StatelessSession.class)
@RemoteBinding(jndiBinding = "spec.StatelessSession2")
@RolesAllowed({"Echo"})
@SecurityDomain("spec-test")
@EJBs({@EJB(name="Session", beanInterface=org.jboss.ejb3.test.security.StatelessSession.class, beanName="StatelessSession")})
public class StatelessSessionBean2
{
   private static final Logger log = Logger
   .getLogger(StatelessSessionBean2.class);
   
   @Resource SessionContext sessionContext;
    
    public void testGetBusinessObject()
    {
       StatelessSession ss = (StatelessSession)sessionContext.getBusinessObject(org.jboss.ejb3.test.security.StatelessSession.class);
       ss.noop();
    }

    public String echo(String arg)
    {
        log.info("echo, arg="+arg);
 
        // This call should fail if the bean is not secured
        Principal p = sessionContext.getCallerPrincipal();
        log.info("echo, callerPrincipal="+p);
        
        String echo = null;
        try
        {
            InitialContext ctx = new InitialContext();
            StatefulSession bean = (StatefulSession) ctx.lookup("spec.StatefulSession");
            echo = bean.echo(arg);
        }
        catch(Exception e)
        {
            throw new EJBException("Stateful.echo failed", e);
        }
        
        return echo;
    }

    public String forward(String echoArg)
    {
        log.info("forward, echoArg="+echoArg);
        String echo = null;
        try
        {
            InitialContext ctx = new InitialContext();
            StatelessSession bean = (StatelessSession)ctx.lookup(Container.ENC_CTX_NAME + "/env/ejb/Session");
            echo = bean.echo(echoArg);
        }
        catch(Exception e)
        {
            log.info("StatelessSession.echo failed", e);
            throw new EJBException("StatelessSession.echo failed", e);
        }
        return echo;
    }

    public void noop()
    {
        log.info("noop");
    }

    public void npeError()
    {
        log.info("npeError");
        Object obj = null;
        obj.toString();
    }
    public void unchecked()
    {
        Principal p = sessionContext.getCallerPrincipal();
        log.info("StatelessSessionBean.unchecked, callerPrincipal="+p);
    }

    public void excluded()
    {
        throw new EJBException("StatelessSessionBean.excluded, no access should be allowed");
    }
}
