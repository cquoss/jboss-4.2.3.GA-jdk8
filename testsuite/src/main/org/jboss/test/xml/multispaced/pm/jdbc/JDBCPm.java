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
package org.jboss.test.xml.multispaced.pm.jdbc;

/**
 * @version <tt>$Revision: 57211 $</tt>
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 */
public class JDBCPm
{
   private String datasource;
   private String table;

   public String getDatasource()
   {
      return datasource;
   }

   public void setDatasource(String datasource)
   {
      this.datasource = datasource;
   }

   public String getTable()
   {
      return table;
   }

   public void setTable(String table)
   {
      this.table = table;
   }

   public String toString()
   {
      return "[datasource=" + datasource + ", table=" + table + ']';
   }

   public boolean equals(Object o)
   {
      if(this == o) return true;
      if(!(o instanceof JDBCPm)) return false;

      final JDBCPm jdbcPm = (JDBCPm)o;

      if(datasource != null ? !datasource.equals(jdbcPm.datasource) : jdbcPm.datasource != null) return false;
      if(table != null ? !table.equals(jdbcPm.table) : jdbcPm.table != null) return false;

      return true;
   }

   public int hashCode()
   {
      int result;
      result = (datasource != null ? datasource.hashCode() : 0);
      result = 29 * result + (table != null ? table.hashCode() : 0);
      return result;
   }
}
