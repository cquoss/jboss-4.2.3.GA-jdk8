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

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.CallableStatement;

import org.jboss.ejb.plugins.cmp.jdbc.JDBCIdentityColumnCreateCommand;
import org.jboss.ejb.plugins.cmp.jdbc.SQLUtil;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCStoreManager;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityCommandMetaData;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.deployment.DeploymentException;

/**
 * Create command for use with Oracle that uses a sequence in conjuction with
 * a RETURNING clause to generate keys in a single statement
 * 
 * The sequence is called by the parameter attribute "sequence_name".
 * As an example, the sequence_name could be %%t_sequence to use <table_name>_sequence
 * for each distinct table.
 * 
 * @author Guillaume Compagnon
 * @version $Revision: 57209 $
 */
public class JDBCOracleSequenceCreateCommand extends JDBCIdentityColumnCreateCommand
{
   private String sequence_name;
   private int pkIndex;
   private int jdbcType;

   public void init(JDBCStoreManager manager) throws DeploymentException
   {
      super.init(manager);
   }

   protected void initEntityCommand(JDBCEntityCommandMetaData entityCommand) throws DeploymentException
   {
      super.initEntityCommand(entityCommand);
      sequence_name = entityCommand.getAttribute("sequence_name");
      if (sequence_name == null) {
         throw new DeploymentException("sequence_name attribute must be specified inside <entity-command>");
      }
   }

   protected void initInsertSQL()
   {
      pkIndex = 1 + insertFields.length;
      jdbcType = pkField.getJDBCType().getJDBCTypes()[0];

      StringBuffer sql = new StringBuffer();
      sql.append("{call INSERT INTO ").append(entity.getTableName());
      sql.append(" (");
      SQLUtil.getColumnNamesClause(pkField, sql)
         .append(", ");

      SQLUtil.getColumnNamesClause(insertFields, sql);

      sql.append(")");
      sql.append(" VALUES (");
      String sequence_name_inst = replaceTable(sequence_name,entity.getTableName());

      sql.append(sequence_name_inst+".NEXTVAL, ");
      SQLUtil.getValuesClause(insertFields, sql);
      sql.append(")");
      sql.append(" RETURNING ");
      SQLUtil.getColumnNamesClause(pkField, sql)
         .append(" INTO ? }");
      insertSQL = sql.toString();
      if (debug) {
         log.debug("Insert Entity SQL: " + insertSQL);
      }
   }

   protected PreparedStatement prepareStatement(Connection c, String sql, EntityEnterpriseContext ctx) throws SQLException
   {
      return c.prepareCall(sql);
   }

   protected int executeInsert(int paramInd, PreparedStatement ps, EntityEnterpriseContext ctx) throws SQLException
   {
      CallableStatement cs = (CallableStatement) ps;
      cs.registerOutParameter(pkIndex, jdbcType);
      cs.execute();
      Object pk = JDBCUtil.getParameter(log, cs, pkIndex, jdbcType, pkField.getFieldType());
      pkField.setInstanceValue(ctx, pk);
      return 1;
   }
   
   /**
	* Replace %%t in the sql command with the current table name
	*
	* @param in sql statement with possible %%t to substitute with table name
	* @param table the table name
	* @return String with sql statement
	*/
   private static String replaceTable(String in, String table)
   {
	  int pos;

	  pos = in.indexOf("%%t");
	  // No %%t -> return input
	  if(pos == -1)
		 return in;

	  String first = in.substring(0, pos);
	  String last = in.substring(pos + 3);

	  return first + table + last;
   }
   
}
