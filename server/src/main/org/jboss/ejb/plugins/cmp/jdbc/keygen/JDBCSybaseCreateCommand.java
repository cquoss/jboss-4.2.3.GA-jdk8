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
package org.jboss.ejb.plugins.cmp.jdbc.keygen;

import org.jboss.ejb.plugins.cmp.jdbc.JDBCIdentityColumnCreateCommand;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityCommandMetaData;
import org.jboss.deployment.DeploymentException;

/**
 * Create command for Sybase that uses the value from an IDENTITY columns.
 * By default uses "SELECT @@IDENTITY"; can be overridden with "pk-sql" attribute
 *
 * @author <a href="mailto:jeremy@boynes.com">Jeremy Boynes</a>
 * @version $Revision: 57209 $
 */
public class JDBCSybaseCreateCommand extends JDBCIdentityColumnCreateCommand
{
   protected void initEntityCommand(JDBCEntityCommandMetaData entityCommand) throws DeploymentException
   {
      super.initEntityCommand(entityCommand);
      pkSQL = entityCommand.getAttribute("pk-sql");
      if(pkSQL == null)
      {
         pkSQL = "SELECT @@IDENTITY";
      }
   }
}
