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
package org.jboss.ejb.plugins.cmp.jdbc.bridge;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.ejb.plugins.cmp.bridge.SelectorBridge;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCQueryCommand;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCStoreManager;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.GenericEntityObjectFactory;

/**
 * JDBCSelectorBridge represents one ejbSelect method.
 * <p/>
 * Life-cycle:
 * Tied to the EntityBridge.
 * <p/>
 * Multiplicity:
 * One for each entity bean ejbSelect method.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 67396 $
 */
public class JDBCSelectorBridge implements SelectorBridge
{
   private final JDBCQueryMetaData queryMetaData;
   private final JDBCStoreManager manager;
   private TransactionManager tm;
   private boolean syncBeforeSelect;

   public JDBCSelectorBridge(JDBCStoreManager manager, JDBCQueryMetaData queryMetaData)
   {
      this.queryMetaData = queryMetaData;
      this.manager = manager;

      EntityContainer container = manager.getContainer();
      tm = container.getTransactionManager();
      syncBeforeSelect = !container.getBeanMetaData().getContainerConfiguration().getSyncOnCommitOnly();
   }

   // BridgeInvoker implementation

   public Object invoke(EntityEnterpriseContext ctx, Method method, Object[] args)
      throws Exception
   {
      Transaction tx = (ctx != null ? ctx.getTransaction() : tm.getTransaction());

      if(syncBeforeSelect)
      {
         EntityContainer.synchronizeEntitiesWithinTransaction(tx);
      }

      return execute(args);
   }

   // SelectorBridge implementation

   public String getSelectorName()
   {
      return queryMetaData.getMethod().getName();
   }

   public Method getMethod()
   {
      return queryMetaData.getMethod();
   }

   private Class getReturnType()
   {
      return queryMetaData.getMethod().getReturnType();
   }

   public Object execute(Object[] args) throws FinderException
   {
      Collection retVal;
      Method method = getMethod();
      try
      {
         JDBCQueryCommand query = manager.getQueryManager().getQueryCommand(method);
         EntityContainer selectedContainer = query.getSelectManager().getContainer();
         GenericEntityObjectFactory factory;
         if(queryMetaData.isResultTypeMappingLocal() && selectedContainer.getLocalHomeClass() != null)
         {
            factory = selectedContainer.getLocalProxyFactory();
         }
         else
         {
            factory = selectedContainer.getProxyFactory();
         }

         retVal = query.execute(method, args, null, factory);
      }
      catch(FinderException e)
      {
         throw e;
      }
      catch(EJBException e)
      {
         throw e;
      }
      catch(Exception e)
      {
         throw new EJBException("Error in " + getSelectorName(), e);
      }

      if(!Collection.class.isAssignableFrom(getReturnType()))
      {
         // single object
         if(retVal.size() == 0)
         {
            throw new ObjectNotFoundException();
         }
         if(retVal.size() > 1)
         {
            throw new FinderException(getSelectorName() +
               " returned " + retVal.size() + " objects");
         }

         Object o = retVal.iterator().next();
         if(o == null && method.getReturnType().isPrimitive())
         {
            throw new FinderException(
               "Cannot return null as a value of primitive type " + method.getReturnType().getName()
            );
         }

         return o;
      }
      else
      {
         // collection or set
         if(Set.class.isAssignableFrom(getReturnType()))
         {
            return new HashSet(retVal);
         }
         else
         {
            return retVal;
         }
      }
   }
}
