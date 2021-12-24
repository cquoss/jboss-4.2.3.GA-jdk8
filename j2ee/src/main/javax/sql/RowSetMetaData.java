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
package javax.sql;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * The RowSetMetaData interface extends ResultSetMetaData with methods that allow a metadata object to be initialized.
 * A RowSetReader may create a RowSetMetaData and pass it to a rowset when new data is read.
 */
public interface RowSetMetaData extends ResultSetMetaData {

  /**
   * Specify whether the is column automatically numbered, thus read-only.
   *
   * @param columnIndex - the first column is 1, the second is 2, ...
   * @param flag - is either true or false.
   * @exception SQLException - if a database-access error occurs.
   */
  public void setAutoIncrement(int columnIndex, boolean flag)
    throws SQLException;

  /**
   * Specify whether the column is case sensitive.
   *
   * @param columnIndex - the first column is 1, the second is 2, ...
   * @param flag - is either true or false.
   * @exception SQLException - if a database-access error occurs.
   */
  public void setCaseSensitive(int columnIndex, boolean flag)
    throws SQLException;

  /**
   * Specify the column's table's catalog name, if any.
   *
   * @param columnIndex - the first column is 1, the second is 2, ...
   * @param string - column's catalog name.
   * @exception SQLException - if a database-access error occurs.
   */
  public void setCatalogName(int columnIndex, String string)
    throws SQLException;

  /**
   * Set the number of columns in the RowSet.
   *
   * @param i - number of columns.
   * @exception SQLException - if a database-access error occurs.
   */
  public void setColumnCount(int i)
    throws SQLException;

  /**
   * Specify the column's normal max width in chars.
   *
   * @param columnIndex - the first column is 1, the second is 2, ...
   * @param size - size of the column
   * @exception SQLException - if a database-access error occurs.
   */
  public void setColumnDisplaySize(int columnIndex, int size)
    throws SQLException;

  /**
   * Specify the suggested column title for use in printouts and displays, if any.
   *
   * @param columnIndex - the first column is 1, the second is 2, ...
   * @param label - the column title
   * @exception SQLException - if a database-access error occurs.
   */
  public void setColumnLabel(int columnIndex, String label)
    throws SQLException;

  /**
   * Specify the column name.
   *
   * @param columnIndex - the first column is 1, the second is 2, ...
   * @param name - the column name
   * @exception SQLException - if a database-access error occurs.
   */
  public void setColumnName(int columnIndex, String name)
    throws SQLException;

  /**
   * Specify the column's SQL type.
   *
   * @param columnIndex - the first column is 1, the second is 2, ...
   * @param sqltype - column's SQL type.
   * @exception SQLException - if a database-access error occurs.
   */
  public void setColumnType(int columnIndex, int sqltype)
    throws SQLException;

  /**
   * Specify the column's data source specific type name, if any.
   *
   * @param columnIndex - the first column is 1, the second is 2, ...
   * @param sqltype - column's SQL type name.
   * @exception SQLException - if a database-access error occurs.
   */
  public void setColumnTypeName(int columnIndex, String typeName)
    throws SQLException;

  /**
   * Specify whether the column is a cash value.
   *
   * @param columnIndex - the first column is 1, the second is 2, ...
   * @param flag - is either true or false.
   * @exception SQLException - if a database-access error occurs.
   */
  public void setCurrency(int columnIndex, boolean flag)
    throws SQLException;

  /**
   * Specify whether the column's value can be set to NULL.
   *
   * @param columnIndex - the first column is 1, the second is 2, ...
   * @param property - is either true or false.
   * @exception SQLException - if a database-access error occurs.
   */
  public void setNullable(int columnIndex, int property)
    throws SQLException;

  /**
   * Specify the column's number of decimal digits.
   *
   * @param columnIndex - the first column is 1, the second is 2, ...
   * @param precision - number of decimal digits.
   * @exception SQLException - if a database-access error occurs.
   */
  public void setPrecision(int columnIndex, int precision)
    throws SQLException;

  /**
   * Specify the column's number of digits to right of the decimal point.
   *
   * @param columnIndex - the first column is 1, the second is 2, ...
   * @param scale - number of digits to right of decimal point.
   * @exception SQLException - if a database-access error occurs.
   */
  public void setScale(int columnIndex, int scale)
    throws SQLException;

  /**
   * Specify the column's table's schema, if any.
   *
   * @param columnIndex - the first column is 1, the second is 2, ...
   * @param schemaName - the schema name
   * @exception SQLException - if a database-access error occurs.
   */
  public void setSchemaName(int columnIndex, String schemaName)
    throws SQLException;

  /**
   * Specify whether the column can be used in a where clause.
   *
   * @param columnIndex - the first column is 1, the second is 2, ...
   * @param flag - is either true or false.
   * @exception SQLException - if a database-access error occurs.
   */
  public void setSearchable(int columnIndex, boolean flag)
    throws SQLException;

  /**
   * Specify whether the column is a signed number.
   *
   * @param columnIndex - the first column is 1, the second is 2, ...
   * @param flag - is either true or false.
   * @exception SQLException - if a database-access error occurs.
   */
  public void setSigned(int columnIndex, boolean flag)
    throws SQLException;

  /**
   * Specify the column's table name, if any.
   *
   * @param columnIndex - the first column is 1, the second is 2, ...
   * @param tableName - column's table name.
   * @exception SQLException - if a database-access error occurs.
   */
  public void setTableName(int columnIndex, String tableName)
    throws SQLException;
}
