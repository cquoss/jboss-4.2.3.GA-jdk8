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
import org.jboss.test.hibernate.ejb.interfaces.ProfileServiceHome;
import org.jboss.test.hibernate.ejb.interfaces.ProfileServiceUtil;
import org.jboss.test.hibernate.ejb.interfaces.ProfileService;

import javax.ejb.SessionBean;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.CreateException;
import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.util.List;

/**
 * An ejb which simply wraps calls to the ProfileBean service.
 * Used to test the issue with sessions, nested ejb calls,
 * and the CachedConnectionManager.
 *
 * @author <a href="mailto:steve@hibernate.org">Steve Ebersole</a>
 * @version $Revision: 57211 $
 *
 * @ejb:bean   name="AggregateProfileService"
 *             jndi-name="AggregateProfileService"
 *             view-type="remote"
 *             type="Stateless"
 */
public class AggregateProfileBean implements SessionBean
{
   /**
    * @exception javax.ejb.EJBException if an error occurs
    * @ejb:interface-method
    */
   public User storeUser(User user) throws EJBException
   {
      ProfileService service = null;
      try
      {
         service = locateService();
         return service.storeUser( user );
      }
      catch( EJBException e )
      {
         throw e;
      }
      catch( RemoteException e )
      {
         throw new EJBException( "Unable to access profile service", e );
      }
      finally
      {
         release( service );
      }
   }

   /**
    * @exception javax.ejb.EJBException if an error occurs
    * @ejb:interface-method
    */
   public User loadUser(long id) throws EJBException
   {
      ProfileService service = null;
      try
      {
         service = locateService();
         return service.loadUser(id);
      }
      catch( EJBException e )
      {
         throw e;
      }
      catch( RemoteException e )
      {
         throw new EJBException( "Unable to access profile service", e );
      }
      finally
      {
         release( service );
      }
   }

   /**
    * @exception javax.ejb.EJBException if an error occurs
    * @ejb:interface-method
    */
   public User loadUser(Long id) throws EJBException
   {
      ProfileService service = null;
      try
      {
         service = locateService();
         return service.loadUser(id);
      }
      catch( EJBException e )
      {
         throw e;
      }
      catch( RemoteException e )
      {
         throw new EJBException( "Unable to access profile service", e );
      }
      finally
      {
         release( service );
      }
   }

   /**
    * @exception javax.ejb.EJBException if an error occurs
    * @ejb:interface-method
    */
   public List listUsers() throws EJBException
   {
      ProfileService service = null;
      try
      {
         service = locateService();
         return service.listUsers();
      }
      catch( EJBException e )
      {
         throw e;
      }
      catch( RemoteException e )
      {
         throw new EJBException( "Unable to access profile service", e );
      }
      finally
      {
         release( service );
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

   private ProfileService locateService() throws EJBException
   {
      try
      {
         ProfileServiceHome home = ProfileServiceUtil.getHome();
         return home.create();
      }
      catch( NamingException e )
      {
         throw new EJBException( "Unable to locate profile service", e );
      }
      catch( CreateException e )
      {
         throw new EJBException( "Unable to create ref to profile service", e );
      }
      catch( RemoteException e )
      {
         throw new EJBException( "Unable to access profile service", e );
      }
   }

   private void release(ProfileService service)
   {
      if ( service != null )
      {
         try
         {
            service.remove();
         }
         catch( Throwable ignore )
         {
         }
      }
   }
}
