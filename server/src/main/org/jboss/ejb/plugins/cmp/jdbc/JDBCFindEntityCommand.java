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

import java.lang.reflect.Method;
import java.util.Collection;

import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.GenericEntityObjectFactory;

/**
 * JDBCFindEntityCommand finds a single entity, by deligating to
 * find entities and checking that only entity is returned.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard ?berg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 57209 $
 */
public final class JDBCFindEntityCommand
{
   private static final String NO_SUCH_ENTITY = "No such entity!";

   private final JDBCStoreManager manager;

   public JDBCFindEntityCommand(JDBCStoreManager manager)
   {
      this.manager = manager;
   }

   public Object execute(Method finderMethod, Object[] args, EntityEnterpriseContext ctx, GenericEntityObjectFactory factory)
      throws FinderException
   {

      JDBCQueryCommand query = manager.getQueryManager().getQueryCommand(finderMethod);

      Collection result = query.execute(finderMethod, args, ctx, factory);
      if(result.isEmpty())
      {
         throw new ObjectNotFoundException(NO_SUCH_ENTITY);
      }
      else if(result.size() == 1)
      {
         return result.iterator().next();
      }
      else
      {
         throw new FinderException("More than one entity matches the finder criteria: " + result);
      }
   }
}
