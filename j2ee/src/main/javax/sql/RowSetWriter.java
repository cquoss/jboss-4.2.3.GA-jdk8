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

import java.sql.SQLException;

/**
 * An object that implements the RowSetWriter interface may be registered with a RowSet object
 * that supports the reader/writer paradigm. The RowSetWriter.writeRow() method is called internally
 * by a RowSet that supports the reader/writer paradigm to write the contents of the rowset to a
 * data source.
 */
public interface RowSetWriter {

  /**
   * This method is called to write data to the data source that is backing the rowset.
   *
   * @param rowSetInternal - the calling rowset
   * @return true if the row was written, false if not due to a conflict
   * @exception SQLException - if a database-access error occur
   */
  public boolean writeData(RowSetInternal rowSetInternal)
    throws SQLException;
}
