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

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A PooledConnection object is a connection object that provides hooks for connection pool management.
 * A PooledConnection object represents a physical connection to a data source.
 */
public interface PooledConnection {

  /**
   * Add an event listener.
   *
   * @param connectionEventListener - The listener
   */
  public void addConnectionEventListener(ConnectionEventListener connectionEventListener);

  /**
   * Close the physical connection.
   *
   * @exception SQLException - if a database-access error occurs.
   */
  public void close()
    throws SQLException;

  /**
   * Create an object handle for this physical connection. The object returned is a temporary handle used by
   * application code to refer to a physical connection that is being pooled.
   *
   * @return a Connection object
   * @exception SQLException - if a database-access error occurs.
   */
  public Connection getConnection()
    throws SQLException;

  /**
   * Remove an event listener.
   *
   * @param connectionEventListener - The listener
   */
  public void removeConnectionEventListener(ConnectionEventListener connectionEventListener);
}
