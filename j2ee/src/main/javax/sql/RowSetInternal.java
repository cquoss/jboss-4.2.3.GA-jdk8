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

import java.sql.*;

/**
 * A rowset object presents itself to a reader or writer as an instance of RowSetInternal.
 * The RowSetInternal interface contains additional methods that let the reader or writer access
 * and modify the internal state of the rowset.
 */
public interface RowSetInternal {

  /**
   * Get the Connection passed to the rowset.
   *
   * @return the Connection passed to the rowset, or null if none
   * @exception SQLException - if a database-access error occurs.
   */
  public Connection getConnection()
    throws SQLException;

  /**
   * Returns a result set containing the original value of the rowset. The cursor is positioned before the
   * first row in the result set. Only rows contained in the result set returned by getOriginal() are said to
   * have an original value.
   *
   * @return the original value of the rowset
   * @exception SQLException - if a database-access error occurs.
   */
  public ResultSet getOriginal()
    throws SQLException;

  /**
   * Returns a result set containing the original value of the current row. If the current row has no original
   * value an empty result set is returned. If there is no current row a SQLException is thrown.
   *
   * @return the original value of the row
   * @exception SQLException - if a database-access error occurs.
   */
  public ResultSet getOriginalRow()
    throws SQLException;

  /**
   * Get the parameters that were set on the rowset.
   *
   * @return an array of parameters
   * @exception SQLException - if a database-access error occurs.
   */
  public Object[] getParams()
    throws SQLException;

  /**
   * Set the rowset's metadata.
   *
   * @param rowSetMetaData - metadata object
   * @exception SQLException - if a database-access error occurs.
   */
  public void setMetaData(RowSetMetaData rowSetMetaData)
    throws SQLException;
}
