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
import java.util.EventObject;

/**
 * The ConnectionEvent class provides information about the source of a connection related event.
 * ConnectionEvent objects provide the following information:
 * <ul>
 * <li>the pooled connection that generated the event 
 * <li>the SQLException about to be thrown to the application ( in the case of an error event)
 * </ul>
 */
public class ConnectionEvent extends EventObject {
  private SQLException ex;
  
  /**
   * Construct a ConnectionEvent object. SQLException defaults to null.
   *
   * @param pooledConnection - the pooled connection that is the source of the event
   */
  public ConnectionEvent(PooledConnection pooledConnection) {
    super(pooledConnection);
    ex = null;
  }

  /**
   * Construct a ConnectionEvent object.
   *
   * @param pooledConnection - the pooled connection that is the source of the event
   * @param e - the SQLException about to be thrown to the application
   */
  public ConnectionEvent(PooledConnection pooledConnection, SQLException e) {
    super(pooledConnection);
    ex = null;
    ex = e;
  }

  /**
   * Gets the SQLException about to be thrown. May be null.
   *
   * @return The SQLException about to be thrown
   */
  public SQLException getSQLException() {
    return ex;
  }
}
