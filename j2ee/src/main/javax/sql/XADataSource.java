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

import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * A factory for XAConnection objects. An object that implements the XADataSource interface
 * is typically registered with a JNDI service provider. 
 */
public interface XADataSource {

  /**
   * <p>Get the log writer for this data source.</p>
   *
   * <p>The log writer is a character output stream to which all logging and tracing messages
   * for this data source object instance will be printed. This includes messages printed by the
   * methods of this object, messages printed by methods of other objects manufactured by this object,
   * and so on. Messages printed to a data source specific log writer are not printed to the log writer
   * associated with the java.sql.Drivermanager class. When a data source object is created the log
   * writer is initially null, in other words, logging is disabled.</p>
   *
   * @return the log writer for this data source, null if disabled
   * @exception SQLException - if a database-access error occurs.
   */
  public PrintWriter getLogWriter()
    throws SQLException;

  /**
   * Gets the maximum time in seconds that this data source can wait while attempting to connect to
   * a database. A value of zero means that the timeout is the default system timeout if there is one;
   * otherwise it means that there is no timeout. When a data source object is created the login timeout
   * is initially zero.
   *
   * @return the data source login time limit
   * @exception SQLException - if a database-access error occurs.
   */
  public int getLoginTimeout()
    throws SQLException;

  /**
   * Attempt to establish a database connection.
   *
   * @return a Connection to the database
   * @exception SQLException - if a database-access error occurs.
   */
  public XAConnection getXAConnection()
    throws SQLException;

  /**
   * Attempt to establish a database connection.
   *
   * @param user - the database user on whose behalf the Connection is being made
   * @param password - the user's password
   * @return a Connection to the database
   * @exception SQLException - if a database-access error occurs.
   */
  public XAConnection getXAConnection(String user, String password)
    throws SQLException;

  /**
   * <p>Set the log writer for this data source.</p>
   *
   * <p>The log writer is a character output stream to which all logging and tracing messages
   * for this data source object instance will be printed. This includes messages printed by the
   * methods of this object, messages printed by methods of other objects manufactured by this object,
   * and so on. Messages printed to a data source specific log writer are not printed to the log writer
   * associated with the java.sql.Drivermanager class. When a data source object is created the log writer
   * is initially null, in other words, logging is disabled.</p>
   *
   * @param printWriter - the new log writer; to disable, set to null
   * @exception SQLException - if a database-access error occurs.
   */
  public void setLogWriter(PrintWriter printWriter)
    throws SQLException;

  /**
   * Sets the maximum time in seconds that this data source will wait while attempting to connect
   * to a database. A value of zero specifies that the timeout is the default system timeout if there
   * is one; otherwise it specifies that there is no timeout. When a data source object is created the
   * login timeout is initially zero.
   *
   * @param sec - the data source login time limit
   * @exception SQLException - if a database-access error occurs.
   */
  public void setLoginTimeout(int sec)
    throws SQLException;
}
