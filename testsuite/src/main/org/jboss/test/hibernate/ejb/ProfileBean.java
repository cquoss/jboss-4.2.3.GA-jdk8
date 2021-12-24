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
package org.jboss.test.hibernate.ejb;

import org.jboss.test.hibernate.model.User;
import org.jboss.test.hibernate.ProfileService;

import javax.ejb.SessionBean;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import java.rmi.RemoteException;
import java.util.List;

import org.hibernate.HibernateException;

/**
 * An ejb to test the ejb method interception style of transparent
 * session management.
 *
 * @author <a href="mailto:steve@hibernate.org">Steve Ebersole</a>
 * @version $Revision: 57211 $
 *
 * @ejb:bean   name="ProfileService"
 *             jndi-name="ProfileService"
 *             view-type="remote"
 *             type="Stateless"
 */
public class ProfileBean implements SessionBean
{
   private ProfileService delegate = new ProfileService();

   /**
    * @exception EJBException if an error occurs
    * @ejb:interface-method
    */
   public User storeUser(User user) throws EJBException
   {
      try
      {
         return delegate.storeUser(user);
      }
      catch(HibernateException e)
      {
         throw new EJBException("Error performing store", e);
      }
   }

   /**
    * @exception EJBException if an error occurs
    * @ejb:interface-method
    */
   public User loadUser(long id) throws EJBException
   {
      try
      {
         return delegate.loadUser(id);
      }
      catch(HibernateException e)
      {
         throw new EJBException("Error performing load", e);
      }
   }

   /**
    * @exception EJBException if an error occurs
    * @ejb:interface-method
    */
   public User loadUser(Long id) throws EJBException
   {
      try
      {
         return delegate.loadUser(id);
      }
      catch(HibernateException e)
      {
         throw new EJBException("Error performing load", e);
      }
   }

   /**
    * @exception EJBException if an error occurs
    * @ejb:interface-method
    */
   public List listUsers() throws EJBException
   {
      try
      {
         return delegate.listUsers();
      }
      catch(HibernateException e)
      {
         throw new EJBException("Error performing list", e);
      }
   }

   /**
    * @ejb:create-method
    */
   public void ejbCreate()
   {
   }

   public void ejbActivate() throws EJBException, RemoteException
   {
   }

   public void ejbPassivate() throws EJBException, RemoteException
   {
   }

   public void ejbRemove() throws EJBException, RemoteException
   {
   }

   public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException
   {
   }
}
