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
package org.jboss.test.hibernate.timers;

import java.util.List;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.LockMode;
import org.hibernate.HibernateException;

/**
 @author Scott.Stark@jboss.org
 @version $Revision: 57211 $
 */
public class TimersFactory
{
   private static final Logger log = Logger.getLogger(Timers.class);

   private final SessionFactory sessionFactory = getSessionFactory();

   protected SessionFactory getSessionFactory()
   {
      try
      {
         InitialContext ctx = new InitialContext();
         SessionFactory factory = (SessionFactory) ctx.lookup("java:/hib-timers/SessionFactory");
         ctx.close();
         return factory;
      }
      catch (Exception e)
      {
         log.error("Could not locate SessionFactory in JNDI", e);
         throw new IllegalStateException("Could not locate SessionFactory in JNDI");
      }
   }

   public void persist(Timers transientInstance)
   {
      log.debug("persisting Timers instance");
      try
      {
         sessionFactory.getCurrentSession().persist(transientInstance);
         log.debug("persist successful");
      }
      catch (RuntimeException re)
      {
         log.error("persist failed", re);
         throw re;
      }
   }

   public void attachDirty(Timers instance)
   {
      log.debug("attaching dirty Timers instance");
      try
      {
         sessionFactory.getCurrentSession().saveOrUpdate(instance);
         log.debug("attach successful");
      }
      catch (RuntimeException re)
      {
         log.error("attach failed", re);
         throw re;
      }
   }

   public void attachClean(Timers instance)
   {
      log.debug("attaching clean Timers instance");
      try
      {
         sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
         log.debug("attach successful");
      }
      catch (RuntimeException re)
      {
         log.error("attach failed", re);
         throw re;
      }
   }

   public void delete(Timers persistentInstance)
   {
      log.debug("deleting Timers instance");
      try
      {
         sessionFactory.getCurrentSession().delete(persistentInstance);
         log.debug("delete successful");
      }
      catch (RuntimeException re)
      {
         log.error("delete failed", re);
         throw re;
      }
   }

   public Timers merge(Timers detachedInstance)
   {
      log.debug("merging Timers instance");
      try
      {
         Timers result = (Timers) sessionFactory.getCurrentSession()
            .merge(detachedInstance);
         log.debug("merge successful");
         return result;
      }
      catch (RuntimeException re)
      {
         log.error("merge failed", re);
         throw re;
      }
   }

   public List listUsers() throws HibernateException
   {
      return sessionFactory.getCurrentSession()
            .createQuery("from Timers")
            .list();
   }

   public Timers findById(TimersID id)
   {
      log.debug("getting Timers instance with id: " + id);
      try
      {
         Timers instance = (Timers) sessionFactory.getCurrentSession()
            .get("org.jboss.ejb.txtimer.data.Timers", id);
         if (instance == null)
         {
            log.debug("get successful, no instance found");
         }
         else
         {
            log.debug("get successful, instance found");
         }
         return instance;
      }
      catch (RuntimeException re)
      {
         throw re;
      }
   }

}