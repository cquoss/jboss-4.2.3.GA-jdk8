/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.test.cluster.web.jvmroute;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.catalina.session.StandardSessionFacade;
import org.jboss.web.tomcat.service.session.AbstractJBossManager;
import org.jboss.web.tomcat.service.session.ClusteredSession;

/**
 * @author Brian Stansberry
 *
 */
public class MockSession extends ClusteredSession
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /**
    * Create a new MockSession.
    * 
    * @param manager
    */
   public MockSession(MockJBossManager manager)
   {
      super(manager, true);
   }

   @Override
   public void tellNew()
   {
      // no-op
   }
   
   @Override
   public String getId()
   {
      // bypass any expiration logic
      return getIdInternal();
   }

   @Override
   public HttpSession getSession()
   {
      return new StandardSessionFacade(this);
   }

   // Inherited abstract methods
   
   @Override
   protected Object getJBossInternalAttribute(String name)
   {
      return null;
   }

   @Override
   protected Map getJBossInternalAttributes()
   {
      return null;
   }

   @Override
   public void initAfterLoad(AbstractJBossManager manager)
   {
      
   }

   @Override
   public void processSessionRepl()
   {
   }

   @Override
   protected Object removeJBossInternalAttribute(String name)
   {
      return null;
   }

   @Override
   public void removeMyself()
   {
   }

   @Override
   public void removeMyselfLocal()
   {
   }

   @Override
   protected Object setJBossInternalAttribute(String name, Object value)
   {
      return null;
   }
   
   
}
