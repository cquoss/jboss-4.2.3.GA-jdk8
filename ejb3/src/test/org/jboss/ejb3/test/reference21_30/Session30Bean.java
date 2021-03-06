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
package org.jboss.ejb3.test.reference21_30;

import javax.naming.*;
import javax.ejb.EJB;
import javax.ejb.EJBs;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.Local;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.ejb3.Container;
import org.jboss.logging.Logger;

/**
 * @version <tt>$Revision: 61034 $</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@Stateless(name="Session30")
@Remote(Session30.class)
@Local(LocalSession30.class)
@RemoteBinding(jndiBinding = "Session30Remote")
@LocalBinding(jndiBinding = "LocalSession30")
@EJBs({@EJB(name="injected", beanInterface=org.jboss.ejb3.test.reference21_30.Session21.class, beanName="Session21")})
public class Session30Bean 
{
   private static final Logger log = Logger.getLogger(Session30Bean.class);

   @EJB(mappedName="Session21") Object testMappedName;
   
   public String access()
   {
      return "Session30";
   }
   
   public String access21()
   {
      try {
         InitialContext jndiContext = new InitialContext();
         Session21 session = (Session21)jndiContext.lookup(Container.ENC_CTX_NAME + "/env/injected");
         return session.access();
      } catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }
   
   public String globalAccess21()
   {
      try {
         InitialContext jndiContext = new InitialContext();
         Session21Home home = (Session21Home)jndiContext.lookup("Session21");
         Session21 session = (Session21)home.create();
         return session.access();
      } catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }
   
   public String accessLocalStateful()
   {
      try {
         InitialContext jndiContext = new InitialContext();
         StatefulSession30LocalHome localHome = (StatefulSession30LocalHome)jndiContext.lookup("StatefulSession30/localHome");
         LocalStatefulSession30 localSession = localHome.create();
         return localSession.getLocalValue();
      } catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }
   
   public String accessLocalStateful(String value)
   {
      try {
         InitialContext jndiContext = new InitialContext();
         StatefulSession30LocalHome localHome = (StatefulSession30LocalHome)jndiContext.lookup("StatefulSession30/localHome");
         LocalStatefulSession30 localSession = localHome.create(value);
         return localSession.getLocalValue();
      } catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }
   
   public String accessLocalStateful(String value, Integer suffix)
   {
      try {
         InitialContext jndiContext = new InitialContext();
         StatefulSession30LocalHome localHome = (StatefulSession30LocalHome)jndiContext.lookup("StatefulSession30/localHome");
         LocalStatefulSession30 localSession = localHome.create(value, suffix);
         return localSession.getLocalValue();
      } catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }
   
}
