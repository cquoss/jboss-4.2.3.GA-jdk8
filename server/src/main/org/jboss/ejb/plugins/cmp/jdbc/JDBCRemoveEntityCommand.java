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
package org.jboss.ejb.plugins.cmp.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;
import java.rmi.RemoteException;
import javax.ejb.RemoveException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.logging.Logger;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.deployment.DeploymentException;


/**
 * JDBCRemoveEntityCommand executes a DELETE FROM table WHERE command.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard ?berg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version $Revision: 57209 $
 */
public final class JDBCRemoveEntityCommand
{
   private final JDBCStoreManager manager;
   private final JDBCEntityBridge entity;
   private final Logger log;
   private final String removeEntitySQL;
   private final boolean syncOnCommitOnly;
   private boolean batchCascadeDelete;

   public JDBCRemoveEntityCommand(JDBCStoreManager manager)
      throws DeploymentException
   {
      this.manager = manager;
      entity = (JDBCEntityBridge) manager.getEntityBridge();

      // Create the Log
      log = Logger.getLogger(
         this.getClass().getName() +
         "." +
         manager.getMetaData().getName());

      StringBuffer sql = new StringBuffer();
      sql.append(SQLUtil.DELETE_FROM)
         .append(entity.getQualifiedTableName())
         .append(SQLUtil.WHERE);
      SQLUtil.getWhereClause(entity.getPrimaryKeyFields(), sql);

      removeEntitySQL = sql.toString();
      if(log.isDebugEnabled())
         log.debug("Remove SQL: " + removeEntitySQL);

      ConfigurationMetaData containerConfig = manager.getContainer().
         getBeanMetaData().getContainerConfiguration();
      syncOnCommitOnly = containerConfig.getSyncOnCommitOnly();

      JDBCCMRFieldBridge[] cmrFields = (JDBCCMRFieldBridge[]) entity.getCMRFields();
      for(int i = 0; i < cmrFields.length; ++i)
      {
         if(cmrFields[i].isBatchCascadeDelete())
         {
            batchCascadeDelete = true;
            break;
         }
      }
   }

   public void execute(EntityEnterpriseContext ctx)
      throws RemoveException, RemoteException
   {
      if(entity.isRemoved(ctx))
      {
         throw new IllegalStateException("Instance was already removed: id=" + ctx.getId());
      }

      entity.setIsBeingRemoved(ctx);

      // remove entity from all relations
      Object[] oldRelationsRef = new Object[1];
      boolean needsSync = entity.removeFromRelations(ctx, oldRelationsRef);

      // update the related entities (stores the removal from relationships)
      // if one of the store fails an EJBException will be thrown
      if(!syncOnCommitOnly && needsSync)
      {
         EntityContainer.synchronizeEntitiesWithinTransaction(ctx.getTransaction());
      }

      if(!batchCascadeDelete)
      {
         if(!entity.isScheduledForBatchCascadeDelete(ctx))
         {
            executeDeleteSQL(ctx);
         }
         else
         {
            if(log.isTraceEnabled())
               log.trace("Instance is scheduled for cascade delete. id=" + ctx.getId());
         }
      }

      // cascate-delete to old relations, if relation uses cascade.
      if(oldRelationsRef[0] != null)
      {
         Map oldRelations = (Map)oldRelationsRef[0];
         entity.cascadeDelete(ctx, oldRelations);
      }

      if(batchCascadeDelete)
      {
         if(!entity.isScheduledForBatchCascadeDelete(ctx))
         {
            executeDeleteSQL(ctx);
         }
         else
         {
            if(log.isTraceEnabled())
               log.debug("Instance is scheduled for cascade delete. id=" + ctx.getId());
         }
      }

      entity.setRemoved(ctx);
      manager.getReadAheadCache().removeCachedData(ctx.getId());
   }

   private void executeDeleteSQL(EntityEnterpriseContext ctx) throws RemoveException
   {
      Object key = ctx.getId();
      Connection con = null;
      PreparedStatement ps = null;
      int rowsAffected = 0;
      try
      {
         if(log.isDebugEnabled())
            log.debug("Executing SQL: " + removeEntitySQL);

         // get the connection
         con = entity.getDataSource().getConnection();
         ps = con.prepareStatement(removeEntitySQL);

         // set the parameters
         entity.setPrimaryKeyParameters(ps, 1, key);

         // execute statement
         rowsAffected = ps.executeUpdate();
      }
      catch(Exception e)
      {
         log.error("Could not remove " + key, e);
         throw new RemoveException("Could not remove " + key + ": " + e.getMessage());
      }
      finally
      {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }

      // check results
      if(rowsAffected == 0)
      {
         log.error("Could not remove entity " + key);
         throw new RemoveException("Could not remove entity");
      }

      if(log.isTraceEnabled())
         log.trace("Remove: Rows affected = " + rowsAffected);
   }
}