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

import org.jboss.xb.binding.MarshallingContext;
import org.jboss.xb.binding.ObjectModelProvider;
import org.jboss.logging.Logger;

/**
 * @version <tt>$Revision: 57211 $</tt>
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 */
public class JDBCPmMetaDataProvider
   implements ObjectModelProvider
{
   private static final Logger log = Logger.getLogger(JDBCPmMetaDataFactory.class);

   private final JDBCPm pm;

   public JDBCPmMetaDataProvider(JDBCPm pm)
   {
      this.pm = pm;
   }

   public Object getRoot(Object o, MarshallingContext ctx, String namespaceURI, String localName)
   {
      log.debug("getRoot(): o=" + o.getClass());
      return pm;
   }

   public Object getChildren(JDBCPm pm, String namespaceUri, String localName)
   {
      log.debug("getChildren> ns=" + namespaceUri + ", localName=" + localName);
      return null;
   }

   public Object getElementValue(JDBCPm pm, String namespaceUri, String localName)
   {
      log.debug("getValue> ns=" + namespaceUri + ", localName=" + localName);
      Object child = null;
      if("datasource".equals(localName))
      {
         child = pm.getDatasource();
      }
      else if("table".equals(localName))
      {
         child = pm.getTable();
      }
      return child;
   }
}
