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
package org.jboss.ejb3.test.standalone.flushmodenever;

import org.jboss.annotation.IgnoreDependency;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 60233 $
 */
@Stateless
@Remote(Session2.class)
public class Session2Bean implements Session2
{
   @PersistenceContext(unitName="manager2") EntityManager manager2;
   @PersistenceContext(unitName="../flushmodenever-session1.jar#manager1") EntityManager manager1;
   @PersistenceUnit(unitName="manager2") EntityManagerFactory factory2;
   @PersistenceUnit(unitName="../flushmodenever-session1.jar#manager1") EntityManagerFactory factory1;
   @EJB @IgnoreDependency Session1 session1;

   //@Resource SessionContext ctx;

   public Entity1 find1FromManager(int id)
   {
      return manager1.find(Entity1.class, id);
   }
   public void find1FromFactory(int id)
   {
      EntityManager m = factory1.createEntityManager();
      Entity1 one = m.find(Entity1.class, id);
      try
      {
         if (one == null) throw new RuntimeException("NOT FOUND");
      }
      finally
      {
         m.close();
      }

   }
   public Entity2 find2FromManager(int id)
   {
      return manager2.find(Entity2.class, id);
   }
   public void find2FromFactory(int id)
   {
      EntityManager m = factory2.createEntityManager();
      Entity2 two = m.find(Entity2.class, id);
      if (two == null) throw new RuntimeException("NOT FOUND");
      // leak m.close();
   }

   public Util findUtil1FromManager(int id)
   {
      return manager1.find(Util.class, id);
   }

   public Util findUtil2FromManager(int id)
   {
      return manager2.find(Util.class, id);
   }
}
