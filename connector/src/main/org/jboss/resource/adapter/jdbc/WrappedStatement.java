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
package org.jboss.resource.adapter.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A wrapper for a statement.
 *
 * @todo remove the org.jboss.ejb.plugins.cmp.jdbc.WrappedStatement dependency
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 75426 $
 */
public abstract class WrappedStatement extends JBossWrapper implements Statement, StatementAccess,
   org.jboss.ejb.plugins.cmp.jdbc.WrappedStatement
{
   private final WrappedConnection lc;
   private final Statement s;

   /** The result sets */
   private HashMap<WrappedResultSet, Throwable> resultSets;

   /** Whether we are closed */
   private AtomicBoolean closed = new AtomicBoolean(false);

   public WrappedStatement(final WrappedConnection lc, Statement s)
   {
      this.lc = lc;
      this.s = s;
      lc.registerStatement(this);
   }

   protected void lock() throws SQLException
   {
      lc.lock();
   }

   protected void unlock()
   {
      lc.unlock();
   }

   public void close() throws SQLException
   {
      if (closed.get())
         return;
         
      closed.set(true);
      lc.unregisterStatement(this);
      internalClose();
   }

   public boolean execute(String sql) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            checkConfiguredQueryTimeout();
            return s.execute(sql);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public boolean execute(String sql, int autoGeneratedKeys) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            checkConfiguredQueryTimeout();
            return s.execute(sql, autoGeneratedKeys);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public boolean execute(String sql, int[] columnIndexes) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            checkConfiguredQueryTimeout();
            return s.execute(sql, columnIndexes);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public boolean execute(String sql, String[]columnNames ) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            checkConfiguredQueryTimeout();
            return s.execute(sql, columnNames);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public Connection getConnection() throws SQLException
   {
      return lc;
   }

   public SQLWarning getWarnings() throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            return s.getWarnings();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void clearWarnings() throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            s.clearWarnings();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public ResultSet executeQuery(String sql) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            checkConfiguredQueryTimeout();
            ResultSet result = s.executeQuery(sql);
            return registerResultSet(result);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public int executeUpdate(String sql) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            checkConfiguredQueryTimeout();
            return s.executeUpdate(sql);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            checkConfiguredQueryTimeout();
            return s.executeUpdate(sql, autoGeneratedKeys);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public int executeUpdate(String sql, int[] columnIndexes) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            checkConfiguredQueryTimeout();
            return s.executeUpdate(sql, columnIndexes);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public int executeUpdate(String sql, String[] columnNames) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            checkConfiguredQueryTimeout();
            return s.executeUpdate(sql, columnNames);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public int getMaxFieldSize() throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            return s.getMaxFieldSize();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setMaxFieldSize(int max) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            s.setMaxFieldSize(max);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public int getMaxRows() throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            return s.getMaxRows();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setMaxRows(int max) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            s.setMaxRows(max);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setEscapeProcessing(boolean enable) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            s.setEscapeProcessing(enable);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public int getQueryTimeout() throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            return s.getQueryTimeout();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setQueryTimeout(int timeout) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            s.setQueryTimeout(timeout);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void cancel() throws SQLException
   {
      checkState();
      try
      {
         s.cancel();
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setCursorName(String name) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            s.setCursorName(name);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public ResultSet getResultSet() throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            ResultSet result = s.getResultSet();
            if (result == null)
               return null;
            else
               return registerResultSet(result);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public int getUpdateCount() throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            return s.getUpdateCount();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public boolean getMoreResults() throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            return s.getMoreResults();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public boolean getMoreResults(int current) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            return s.getMoreResults(current);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setFetchDirection(int direction) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            s.setFetchDirection(direction);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public int getFetchDirection() throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            return s.getFetchDirection();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setFetchSize(int rows) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            s.setFetchSize(rows);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public int getFetchSize() throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            return s.getFetchSize();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public int getResultSetConcurrency() throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            return s.getResultSetConcurrency();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public int getResultSetType() throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            return s.getResultSetType();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void addBatch(String sql) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            s.addBatch(sql);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void clearBatch() throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            s.clearBatch();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public int[] executeBatch() throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            checkConfiguredQueryTimeout();
            return s.executeBatch();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public ResultSet getGeneratedKeys() throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            ResultSet resultSet = s.getGeneratedKeys();
            return registerResultSet(resultSet);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public int getResultSetHoldability() throws SQLException
   {
      lock();
      try
      {
         checkState();
         try
         {
            return s.getResultSetHoldability();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public Statement getUnderlyingStatement() throws SQLException
   {
      lock();
      try
      {
         checkState();
         return s;
      }
      finally
      {
         unlock();
      }
   }

   protected Statement getWrappedObject() throws SQLException
   {
      return getUnderlyingStatement();
   }

   protected SQLException checkException(Throwable t)
      throws SQLException
   {
      throw lc.checkException(t);
   }

   protected void checkTransaction()
      throws SQLException
   {
      checkState();
      lc.checkTransaction();
   }

   protected void checkConfiguredQueryTimeout() throws SQLException
   {
      lc.checkConfiguredQueryTimeout(this);
   }

   protected void checkTransactionActive() throws SQLException
   {
      lc.checkTransactionActive();
   }

   protected void internalClose() throws SQLException
   {
      closed.set(true);
      try
      {
         closeResultSets();
      }
      finally
      {
         s.close();
      }
   }

   void checkState() throws SQLException
   {
      if (closed.get())
         throw new SQLException("The statement is closed.");
   }

   protected abstract WrappedResultSet wrapResultSet(ResultSet resultSet);
   
   protected ResultSet registerResultSet(ResultSet resultSet)
   {
      if (resultSet != null)
         resultSet = wrapResultSet(resultSet);
      
      if (lc.getTrackStatements() == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_FALSE_INT)
         return resultSet;

      WrappedResultSet wrapped = (WrappedResultSet) resultSet;
      
      synchronized (this)
      {
         if (resultSets == null)
            resultSets = new HashMap<WrappedResultSet, Throwable>();
         
         if (lc.getTrackStatements() == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_TRUE_INT)
            resultSets.put(wrapped, new Throwable("STACKTRACE"));
         else
            resultSets.put(wrapped, null);
      }
      return resultSet;
   }

   protected void unregisterResultSet(WrappedResultSet resultSet)
   {
      if (lc.getTrackStatements() == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_FALSE_INT)
         return;

      synchronized (this)
      {
         if (resultSets != null)
            resultSets.remove(resultSet);
      }
   }

   protected void closeResultSets()
   {
      if (lc.getTrackStatements() == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_FALSE_INT)
         return;

      synchronized (this)
      {
         if (resultSets == null)
            return;
         for (Iterator<Map.Entry<WrappedResultSet, Throwable>> i = resultSets.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry<WrappedResultSet, Throwable> entry = i.next();
            WrappedResultSet resultSet = entry.getKey();
            if (lc.getTrackStatements() == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_TRUE_INT)
            {
               Throwable stackTrace = entry.getValue();
               lc.getLogger().warn("Closing a result set you left open! Please close it yourself.", stackTrace);
            }
            try
            {
               resultSet.internalClose();
            }
            catch (Throwable t)
            {
               lc.getLogger().warn("Error closing a result set you left open! Please close it yourself.", t);
            }
         }
         resultSets.clear();
      }
   }
}