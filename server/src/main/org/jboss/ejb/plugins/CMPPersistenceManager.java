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
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;

import javax.ejb.EntityBean;
import javax.ejb.RemoveException;
import javax.ejb.EJBException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EntityCache;
import org.jboss.ejb.EntityPersistenceStore;
import org.jboss.ejb.AllowedOperationsAssociation;
import org.jboss.ejb.GenericEntityObjectFactory;
import org.jboss.metadata.ConfigurationMetaData;

/**
 * The CMP Persistence Manager implements the semantics of the CMP
 * EJB 1.1 call back specification.
 * <p/>
 * This Manager works with a "EntityPersistenceStore" that takes care of the
 * physical storing of instances (JAWS, JDBC O/R, FILE, Object).
 *
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:danch@nvisia.com">Dan Christopherson</a>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 * @version $Revision: 57209 $
 */
public class CMPPersistenceManager
   implements EntityPersistenceManager
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   EntityContainer con;
   // Physical persistence implementation
   EntityPersistenceStore store;


   HashMap createMethods = new HashMap();
   HashMap postCreateMethods = new HashMap();
   private boolean insertAfterEjbPostCreate;
   private boolean ejbStoreForClean;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------
   public void setContainer(Container c)
   {
      con = (EntityContainer) c;

      if(store != null)
      {
         store.setContainer(c);
      }

      if(con != null)
      {
         ConfigurationMetaData configuration = con.getBeanMetaData().getContainerConfiguration();
         ejbStoreForClean = configuration.isEjbStoreForClean();
      }
   }

   /**
    * Gets the entity persistence store.
    */
   public EntityPersistenceStore getPersistenceStore()
   {
      return store;
   }

   public void setPersistenceStore(EntityPersistenceStore store)
   {
      this.store = store;

      //Give it the container
      if(con != null) store.setContainer(con);
   }

   public void create()
      throws Exception
   {
      if(con.getHomeClass() != null)
      {
         Method[] methods = con.getHomeClass().getMethods();
         createMethodCache(methods);
      }

      if(con.getLocalHomeClass() != null)
      {
         Method[] methods = con.getLocalHomeClass().getMethods();
         createMethodCache(methods);
      }

      insertAfterEjbPostCreate = con.getBeanMetaData().getContainerConfiguration().isInsertAfterEjbPostCreate();

      store.create();
   }

   /**
    * Returns a new instance of the bean class or a subclass of the bean class.
    *
    * @return the new instance
    */
   public Object createBeanClassInstance() throws Exception
   {
      return store.createBeanClassInstance();
   }

   private void createMethodCache(Method[] methods)
      throws NoSuchMethodException
   {
      // Create cache of create methods
      Class beanClass = con.getBeanClass();
      for(int i = 0; i < methods.length; i++)
      {
         String name = methods[i].getName();
         if(name.startsWith("create"))
         {
            Class[] types = methods[i].getParameterTypes();
            try
            {
               String nameSuffix = name.substring(0, 1).toUpperCase() + name.substring(1);
               Method beanMethod = beanClass.getMethod("ejb" + nameSuffix, types);
               createMethods.put(methods[i], beanMethod);
               beanMethod = beanClass.getMethod("ejbPost" + nameSuffix, types);
               postCreateMethods.put(methods[i], beanMethod);
            }
            catch(NoSuchMethodException nsme)
            {
               throw new NoSuchMethodException("Can't find ejb[Post]Create in " + beanClass.getName());
            }
         }
      }
   }

   public void start()
      throws Exception
   {
      store.start();
   }

   public void stop()
   {
      store.stop();
   }

   public void destroy()
   {
      store.destroy();
   }

   public void createEntity(Method m, Object[] args, EntityEnterpriseContext ctx)
      throws Exception
   {
      // Deligate initialization of bean to persistence store
      store.initEntity(ctx);

      // Call ejbCreate on the target bean
      try
      {
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_CREATE);
         Method createMethod = (Method) createMethods.get(m);
         createMethod.invoke(ctx.getInstance(), args);
      }
      catch(IllegalAccessException e)
      {
         // Throw this as a bean exception...(?)
         throw new EJBException(e);
      }
      catch(InvocationTargetException ite)
      {
         Throwable e = ite.getTargetException();
         if(e instanceof EJBException)
         {
            // Rethrow exception
            throw (EJBException) e;
         }
         else if(e instanceof RuntimeException)
         {
            // Wrap runtime exceptions
            throw new EJBException((Exception) e);
         }
         else if(e instanceof Exception)
         {
            // Remote, Create, or custom app. exception
            throw (Exception) e;
         }
         else
         {
            throw (Error) e;
         }
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }

      // if insertAfterEjbPostCreate == true, this will INSERT entity
      // otherwise, primary key is extracted from the context and returned
      Object id;
      try
      {
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_CREATE);
         id = store.createEntity(m, args, ctx);
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }

      // Set the key on the target context
      ctx.setId(id);

      // Create a new CacheKey
      Object cacheKey = ((EntityCache) con.getInstanceCache()).createCacheKey(id);

      // Give it to the context
      ctx.setCacheKey(cacheKey);
   }

   public void postCreateEntity(Method m, Object[] args, EntityEnterpriseContext ctx)
      throws Exception
   {
      // this call should go first as it sets up relationships
      // for fk fields mapped to pk fields
      store.postCreateEntity(m, args, ctx);

      try
      {
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_POST_CREATE);

         Method postCreateMethod = (Method) postCreateMethods.get(m);
         postCreateMethod.invoke(ctx.getInstance(), args);
         if(insertAfterEjbPostCreate)
         {
            store.createEntity(m, args, ctx);
         }
      }
      catch(IllegalAccessException e)
      {
         // Throw this as a bean exception...(?)
         throw new EJBException(e);
      }
      catch(InvocationTargetException ite)
      {
         Throwable e = ite.getTargetException();
         if(e instanceof EJBException)
         {
            // Rethrow exception
            throw (EJBException) e;
         }
         else if(e instanceof RuntimeException)
         {
            // Wrap runtime exceptions
            throw new EJBException((Exception) e);
         }
         else if(e instanceof Exception)
         {
            // Remote, Create, or custom app. exception
            throw (Exception) e;
         }
         else
         {
            throw (Error) e;
         }
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }
   }

   public Object findEntity(Method finderMethod,
                            Object[] args,
                            EntityEnterpriseContext ctx,
                            GenericEntityObjectFactory factory)
      throws Exception
   {
      try
      {
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_FIND);
         return store.findEntity(finderMethod, args, ctx, factory);
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }
   }

   /**
    * find multiple entities
    */
   public Collection findEntities(Method finderMethod,
                                  Object[] args,
                                  EntityEnterpriseContext ctx,
                                  GenericEntityObjectFactory factory)
      throws Exception
   {
      try
      {
         // return the finderResults so that the invoker layer can extend this back
         // giving the client an OO 'cursor'
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_FIND);
         return store.findEntities(finderMethod, args, ctx, factory);
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }
   }

   /*
    * activateEntity(EnterpriseContext ctx)
    *
    * The method calls the target beans for spec compliant callbacks.
    * Since these are pure EJB calls it is not obvious that the store should
    * expose the interfaces.  In case of jaws however we found that store specific
    * contexts could be set in the activateEntity calls and hence a propagation of
    * the call made sense.  The persistence store is called for "extension" purposes.
    *
    * @see activateEntity on EntityPersistenceStore.java
    */
   public void activateEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      // Create a new CacheKey
      Object id = ctx.getId();
      Object cacheKey = ((EntityCache) con.getInstanceCache()).createCacheKey(id);

      // Give it to the context
      ctx.setCacheKey(cacheKey);

      try
      {
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_ACTIVATE);
         EntityBean eb = (EntityBean) ctx.getInstance();
         eb.ejbActivate();
      }
      catch(Exception e)
      {
         if(e instanceof RemoteException)
         {
            // Rethrow exception
            throw (RemoteException) e;
         }
         else if(e instanceof EJBException)
         {
            // Rethrow exception
            throw (EJBException) e;
         }
         else
         {
            // Wrap runtime exceptions
            throw new EJBException((Exception) e);
         }
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }

      // The implementation of the call can be left absolutely empty, the
      // propagation of the call is just a notification for stores that would
      // need to know that an instance is being activated
      store.activateEntity(ctx);
   }

   public void loadEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      //long lStart = System.currentTimeMillis();
      // Have the store load the fields of the instance
      store.loadEntity(ctx);
      //mLoad.add( System.currentTimeMillis() - lStart );

      invokeLoad(ctx);
   }

   public boolean isStoreRequired(EntityEnterpriseContext ctx) throws Exception
   {
      return store.isStoreRequired(ctx);
   }

   public boolean isModified(EntityEnterpriseContext ctx) throws Exception
   {
      return store.isModified(ctx);
   }

   public void storeEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_STORE);
      try
      {
         store.storeEntity(ctx);
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }
   }

   public void invokeEjbStore(EntityEnterpriseContext ctx) throws RemoteException
   {
      AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_STORE);

      try
      {
         // if call-ejb-store-for-clean=true then invoke ejbStore first (the last chance to modify the instance)
         if(ejbStoreForClean)
         {
            ejbStore(ctx);
         }
         else
         {
            // else check whether the instance is dirty and invoke ejbStore only if it is really dirty
            boolean modified = false;
            try
            {
               modified = isStoreRequired(ctx);
            }
            catch(Exception e)
            {
               throwRemoteException(e);
            }

            if(modified)
            {
               ejbStore(ctx);
            }
         }
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }
   }

   public void passivateEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      try
      {
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_PASSIVATE);
         EntityBean eb = (EntityBean) ctx.getInstance();
         eb.ejbPassivate();
      }
      catch(Exception e)
      {
         throwRemoteException(e);
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }

      store.passivateEntity(ctx);
      ctx.setEJBObject(null);
      ctx.setEJBLocalObject(null);
   }

   public void removeEntity(EntityEnterpriseContext ctx)
      throws RemoteException, RemoveException
   {
      try
      {
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_REMOVE);
         EntityBean eb = (EntityBean) ctx.getInstance();
         eb.ejbRemove();
      }
      catch(Exception e)
      {
         if(e instanceof RemoveException)
         {
            // Rethrow exception
            throw (RemoveException) e;
         }
         else
         {
            throwRemoteException(e);
         }
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }

      store.removeEntity(ctx);
   }

   protected void invokeLoad(EntityEnterpriseContext ctx) throws RemoteException
   {
      try
      {
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_LOAD);

         EntityBean eb = (EntityBean) ctx.getInstance();
         eb.ejbLoad();
      }
      catch(Exception e)
      {
         throwRemoteException(e);
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }
   }

   // Private

   private void ejbStore(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      try
      {
         EntityBean eb = (EntityBean) ctx.getInstance();
         eb.ejbStore();
      }
      catch(Exception e)
      {
         throwRemoteException(e);
      }
   }

   private void throwRemoteException(Exception e)
      throws RemoteException
   {
      if(e instanceof RemoteException)
      {
         // Rethrow exception
         throw (RemoteException) e;
      }
      else if(e instanceof EJBException)
      {
         // Rethrow exception
         throw (EJBException) e;
      }
      else
      {
         // Wrap runtime exceptions
         throw new EJBException((Exception) e);
      }
   }
}
