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
package org.jboss.ejb.plugins.cmp.jdbc2;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.jdbc2.bridge.JDBCEntityBridge2;
import org.jboss.deployment.DeploymentException;

import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import java.lang.reflect.Method;
import java.sql.SQLException;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 57209 $</tt>
 */
public class ApplicationPkCreateCommand
   implements CreateCommand
{
   private JDBCEntityBridge2 entityBridge;

   public void init(JDBCStoreManager2 manager) throws DeploymentException
   {
      this.entityBridge = (JDBCEntityBridge2) manager.getEntityBridge();
   }

   public Object execute(Method m, Object[] args, EntityEnterpriseContext ctx) throws CreateException
   {
      Object pk;
      PersistentContext pctx = (PersistentContext) ctx.getPersistenceContext();
      if(ctx.getId() == null)
      {
         pk = entityBridge.extractPrimaryKeyFromInstance(ctx);

         if(pk == null)
         {
            throw new CreateException("Primary key for created instance is null.");
         }

         pctx.setPk(pk);
      }
      else
      {
         // insert-after-ejb-post-create
         try
         {
            pctx.flush();
         }
         catch(SQLException e)
         {
            if("23000".equals(e.getSQLState()))
            {
               throw new DuplicateKeyException("Unique key violation or invalid foreign key value: pk=" + ctx.getId());
            }
            else
            {
               throw new CreateException("Failed to create instance: pk=" + ctx.getId() +
                  ", state=" + e.getSQLState() +
                  ", msg=" + e.getMessage());
            }
         }
         pk = ctx.getId();
      }
      return pk;
   }
}
