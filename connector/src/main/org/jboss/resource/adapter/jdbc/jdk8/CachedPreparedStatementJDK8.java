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
package org.jboss.resource.adapter.jdbc.jdk8;

import java.io.InputStream;
import java.io.Reader;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;

import org.jboss.resource.adapter.jdbc.CachedPreparedStatement;

public class CachedPreparedStatementJDK8 extends CachedPreparedStatement
{
   public CachedPreparedStatementJDK8(PreparedStatement ps) throws SQLException
   {
      super(ps);
   }

   @Override
   public boolean isCloseOnCompletion() throws SQLException {
      return false;
   }

   @Override
   public void closeOnCompletion() throws SQLException {
      // do nothing
   }

   public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException
   {
      getWrappedObject().setAsciiStream(parameterIndex, x, length);
   }

   public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException
   {
      getWrappedObject().setAsciiStream(parameterIndex, x);
   }

   public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException
   {
      getWrappedObject().setBinaryStream(parameterIndex, x, length);
   }

   public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException
   {
      getWrappedObject().setBinaryStream(parameterIndex, x);
   }

   public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException
   {
      getWrappedObject().setBlob(parameterIndex, inputStream, length);
   }

   public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException
   {
      getWrappedObject().setBlob(parameterIndex, inputStream);
   }

   public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException
   {
      getWrappedObject().setCharacterStream(parameterIndex, reader, length);
   }

   public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException
   {
      getWrappedObject().setCharacterStream(parameterIndex, reader);
   }

   public void setClob(int parameterIndex, Reader reader, long length) throws SQLException
   {
      getWrappedObject().setClob(parameterIndex, reader, length);
   }

   public void setClob(int parameterIndex, Reader reader) throws SQLException
   {
      getWrappedObject().setClob(parameterIndex, reader);
   }

   public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException
   {
      getWrappedObject().setNCharacterStream(parameterIndex, value, length);
   }

   public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException
   {
      getWrappedObject().setNCharacterStream(parameterIndex, value);
   }

   public void setNClob(int parameterIndex, NClob value) throws SQLException
   {
      getWrappedObject().setNClob(parameterIndex, value);
   }

   public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException
   {
      getWrappedObject().setNClob(parameterIndex, reader, length);
   }

   public void setNClob(int parameterIndex, Reader reader) throws SQLException
   {
      getWrappedObject().setNClob(parameterIndex, reader);
   }

   public void setNString(int parameterIndex, String value) throws SQLException
   {
      getWrappedObject().setNString(parameterIndex, value);
   }

   public void setRowId(int parameterIndex, RowId x) throws SQLException
   {
      getWrappedObject().setRowId(parameterIndex, x);
   }

   public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException
   {
      getWrappedObject().setSQLXML(parameterIndex, xmlObject);
   }

   public boolean isClosed() throws SQLException
   {
      return getWrappedObject().isClosed();
   }

   public boolean isPoolable() throws SQLException
   {
      return getWrappedObject().isPoolable();
   }

   public void setPoolable(boolean poolable) throws SQLException
   {
      getWrappedObject().setPoolable(poolable);
   }
}
