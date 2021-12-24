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
package org.jboss.mq.sm.jdbc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jms.InvalidClientIDException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;
import org.jboss.mq.DurableSubscriptionID;
import org.jboss.mq.SpyJMSException;
import org.jboss.mq.SpyTopic;
import org.jboss.mq.sm.AbstractStateManager;
import org.jboss.mq.sm.StateManager;
import org.jboss.tm.TransactionManagerService;

/**
 * A state manager which does not create the table schema in a tx
 *
 * Based on http://jira.jboss.com/jira/browse/JBAS-4260
 *
 * @author Luc Texier (ltexier@redhat.com)
 * @version $Revision$
 */
public class MSSQLJDBCStateManager extends JDBCStateManager
{
   static final Logger log = Logger.getLogger(MSSQLJDBCStateManager.class);

   protected void initDB() throws Exception
   {
      CREATE_USER_TABLE = sqlProperties.getProperty("CREATE_USER_TABLE", CREATE_USER_TABLE);
      CREATE_ROLE_TABLE = sqlProperties.getProperty("CREATE_ROLE_TABLE", CREATE_ROLE_TABLE);
      CREATE_SUBSCRIPTION_TABLE = sqlProperties.getProperty("CREATE_SUBSCRIPTION_TABLE", CREATE_SUBSCRIPTION_TABLE);
      GET_SUBSCRIPTION = sqlProperties.getProperty("GET_SUBSCRIPTION", GET_SUBSCRIPTION);
      GET_SUBSCRIPTIONS_FOR_TOPIC = sqlProperties.getProperty("GET_SUBSCRIPTIONS_FOR_TOPIC",
            GET_SUBSCRIPTIONS_FOR_TOPIC);
      LOCK_SUBSCRIPTION = sqlProperties.getProperty("LOCK_SUBSCRIPTION", LOCK_SUBSCRIPTION);
      INSERT_SUBSCRIPTION = sqlProperties.getProperty("INSERT_SUBSCRIPTION", INSERT_SUBSCRIPTION);
      UPDATE_SUBSCRIPTION = sqlProperties.getProperty("UPDATE_SUBSCRIPTION", UPDATE_SUBSCRIPTION);
      REMOVE_SUBSCRIPTION = sqlProperties.getProperty("REMOVE_SUBSCRIPTION", REMOVE_SUBSCRIPTION);
      GET_USER_BY_CLIENTID = sqlProperties.getProperty("GET_USER_BY_CLIENTID", GET_USER_BY_CLIENTID);
      GET_USER = sqlProperties.getProperty("GET_USER", GET_USER);

      // Read the queries to populate the tables with initial data
      for (Iterator i = sqlProperties.entrySet().iterator(); i.hasNext();)
      {
         Map.Entry entry = (Map.Entry) i.next();
         String key = (String) entry.getKey();
         if (key.startsWith("POPULATE.TABLES."))
            POPULATE_TABLES.add(entry.getValue());
      }

      String createString = sqlProperties.getProperty("CREATE_TABLES_ON_START_UP");
      if (createString == null)
         createString = sqlProperties.getProperty("CREATE_TABLES_ON_STARTUP");
      if (createString == null)
         createTables = true;
      else
         createTables = createString.trim().equalsIgnoreCase("true");

      if (createTables)
      {
         Connection connection = null;
         connection = dataSource.getConnection();

         try
         {
            PreparedStatement statement;
            try
            {
               statement = connection.prepareStatement(CREATE_USER_TABLE);
               statement.executeUpdate();
            }
            catch (SQLException ignored)
            {
               log.trace("Error creating table: " + CREATE_USER_TABLE, ignored);
            }
            try
            {
               statement = connection.prepareStatement(CREATE_ROLE_TABLE);
               statement.executeUpdate();
            }
            catch (SQLException ignored)
            {
               log.trace("Error creating table: " + CREATE_ROLE_TABLE, ignored);
            }
            try
            {
               statement = connection.prepareStatement(CREATE_SUBSCRIPTION_TABLE);
               statement.executeUpdate();
            }
            catch (SQLException ignored)
            {
               log.trace("Error creating table: " + CREATE_SUBSCRIPTION_TABLE, ignored);
            }

            Iterator iter = POPULATE_TABLES.iterator();
            String nextQry = null;
            while (iter.hasNext())
            {
               try
               {
                  nextQry = (String) iter.next();
                  statement = connection.prepareStatement(nextQry);
                  statement.execute();
               }
               catch (SQLException ignored)
               {
                  log.trace("Error populating tables: " + nextQry, ignored);
               }
            }
         }
         finally
         {
            try {
               if(connection != null)
                   connection.close();

            }catch(SQLException sqle){
                log.trace("Error when closing connection " +sqle);
            }
         }
      }
   }

}
