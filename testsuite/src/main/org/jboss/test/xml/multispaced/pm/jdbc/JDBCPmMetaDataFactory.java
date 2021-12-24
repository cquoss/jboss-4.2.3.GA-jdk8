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

import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.UnmarshallingContext;
import org.jboss.test.xml.multispaced.XMBeanMetaData;
import org.xml.sax.Attributes;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 57211 $</tt>
 */
public class JDBCPmMetaDataFactory
   implements ObjectModelFactory
{
   public static final JDBCPmMetaDataFactory INSTANCE = new JDBCPmMetaDataFactory();

   private JDBCPmMetaDataFactory()
   {
   }

   public Object newRoot(Object root,
                         UnmarshallingContext navigator,
                         String namespaceURI,
                         String localName,
                         Attributes attrs)
   {
      return new JDBCPm();
   }

   public Object completeRoot(Object root, UnmarshallingContext ctx, String uri, String name)
   {
      return root;
   }
   
   public void setValue(JDBCPm pm, UnmarshallingContext navigator, String namespaceUri, String localName, String value)
   {
      if("datasource".equals(localName))
      {
         pm.setDatasource(value);
      }
      else if("table".equals(localName))
      {
         pm.setTable(value);
      }
   }
}
