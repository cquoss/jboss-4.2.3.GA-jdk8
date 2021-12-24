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
package org.jboss.mq.kernel;

import javax.jms.JMSException;
import javax.management.MBeanException;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.ReflectionException;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;
import javax.sql.DataSource;
import org.jboss.mq.pm.jdbc2.PersistenceManager;

/**
 * lite wrapper so that this can work in a dependency injection framework.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 57198 $
 */
public class JDBC2PersistenceManager extends PersistenceManager
{
   public JDBC2PersistenceManager()
           throws JMSException
   {
   }

   protected void initializeFields() throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, NamingException
   {
      // complete
   }

   public void setTransactionManager(TransactionManager tm)
   {
      this.tm = tm;
   }

   public void setDatasource(DataSource ds)
   {
      this.datasource = ds;
   }



}
