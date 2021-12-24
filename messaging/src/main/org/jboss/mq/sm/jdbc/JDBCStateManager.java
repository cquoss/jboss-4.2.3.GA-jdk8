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
 * A state manager that stores state in the database. 
 * 
 * @jmx:mbean extends="org.jboss.mq.sm.AbstractStateManagerMBean" 
 * @todo add support for jmx operations to maintain the database 
 * @todo create indices
 * 
 * @author Adrian Brock (Adrian@jboss.org)
 * @author Ivelin Ivanov (ivelin@jboss.org)
 * @version $Revision: 64570 $
 */
public class JDBCStateManager extends AbstractStateManager implements JDBCStateManagerMBean
{
   static final Logger log = Logger.getLogger(JDBCStateManager.class);

   /** The connection manager */
   private ObjectName connectionManagerName;

   /** The data source */
   protected DataSource dataSource;

   /** The connection retries */
   protected  int connectionRetryAttempts = 5;

   /** Whether there is a security manager */
   private boolean hasSecurityManager = true;

   /** The transaction manager */
   protected TransactionManager tm;

   /** The sql properties */
   protected Properties sqlProperties = new Properties();

   /** Whether to create tables */
   protected boolean createTables = true;

   /** Create the user table */
   protected String CREATE_USER_TABLE = "CREATE TABLE JMS_USERS (USERID VARCHAR(32) NOT NULL, PASSWD VARCHAR(32) NOT NULL, CLIENTID VARCHAR(128),"
         + " PRIMARY KEY(USERID))";

   /** Create the role table */
   protected String CREATE_ROLE_TABLE = "CREATE TABLE JMS_ROLES (ROLEID VARCHAR(32) NOT NULL, USERID VARCHAR(32) NOT NULL,"
         + " PRIMARY KEY(USERID, ROLEID))";

   protected String CREATE_SUBSCRIPTION_TABLE = "CREATE TABLE JMS_SUBSCRIPTIONS (CLIENTID VARCHAR(128) NOT NULL, NAME VARCHAR(128) NOT NULL,"
         + " TOPIC VARCHAR(255) NOT NULL, SELECTOR VARCHAR(255)," + " PRIMARY KEY(CLIENTID, NAME))";

   /** Get a subscription */
   protected String GET_SUBSCRIPTION = "SELECT TOPIC, SELECTOR FROM JMS_SUBSCRIPTIONS WHERE CLIENTID=? AND NAME=?";

   /** Get subscriptions for a topic */
   protected String GET_SUBSCRIPTIONS_FOR_TOPIC = "SELECT CLIENTID, NAME, SELECTOR FROM JMS_SUBSCRIPTIONS WHERE TOPIC=?";

   /** Lock a subscription */
   protected String LOCK_SUBSCRIPTION = "SELECT TOPIC, SELECTOR FROM JMS_SUBSCRIPTIONS WHERE CLIENTID=? AND NAME=?";

   /** Insert a subscription */
   protected String INSERT_SUBSCRIPTION = "INSERT INTO JMS_SUBSCRIPTIONS (CLIENTID, NAME, TOPIC, SELECTOR) VALUES(?,?,?,?)";

   /** Update a subscription */
   protected String UPDATE_SUBSCRIPTION = "UPDATE JMS_SUBSCRIPTIONS SET TOPIC=?, SELECTOR=? WHERE CLIENTID=? AND NAME=?";

   /** Remove a subscription */
   protected String REMOVE_SUBSCRIPTION = "DELETE FROM JMS_SUBSCRIPTIONS WHERE CLIENTID=? AND NAME=?";

   /** Get a user with the given client id */
   protected String GET_USER_BY_CLIENTID = "SELECT USERID, PASSWD, CLIENTID FROM JMS_USERS WHERE CLIENTID=?";

   /** Get a user with the given user id */
   protected String GET_USER = "SELECT PASSWD, CLIENTID FROM JMS_USERS WHERE USERID=?";

   /** Populate tables with initial data */
   protected List POPULATE_TABLES = new ArrayList();

   public ObjectName getConnectionManager()
   {
      return connectionManagerName;
   }

   public void setConnectionManager(ObjectName connectionManagerName)
   {
      this.connectionManagerName = connectionManagerName;
   }

   public boolean hasSecurityManager()
   {
      return hasSecurityManager;
   }

   public void setHasSecurityManager(boolean hasSecurityManager)
   {
      this.hasSecurityManager = hasSecurityManager;
   }

   public String getSqlProperties()
   {
      try
      {
         ByteArrayOutputStream boa = new ByteArrayOutputStream();
         sqlProperties.store(boa, "");
         return new String(boa.toByteArray());
      }
      catch (IOException shouldnothappen)
      {
         return "";
      }
   }

   public void setSqlProperties(String value)
   {
      try
      {

         ByteArrayInputStream is = new ByteArrayInputStream(value.getBytes());
         sqlProperties = new Properties();
         sqlProperties.load(is);

      }
      catch (IOException shouldnothappen)
      {
      }
   }

   public void setConnectionRetryAttempts(int value)
   {
      this.connectionRetryAttempts = value;
   }

   public int getConnectionRetryAttempts()
   {
      return this.connectionRetryAttempts;
   }

   protected DurableSubscription getDurableSubscription(DurableSubscriptionID sub) throws JMSException
   {
      JDBCSession session = new JDBCSession();
      try
      {
         PreparedStatement statement = session.prepareStatement(GET_SUBSCRIPTION);
         statement.setString(1, sub.getClientID());
         statement.setString(2, sub.getSubscriptionName());
         ResultSet rs = statement.executeQuery();
         session.addResultSet(rs);
         if (rs.next() == false)
            return null;

         return new DurableSubscription(sub.getClientID(), sub.getSubscriptionName(), rs.getString(1), rs.getString(2));
      }
      catch (SQLException e)
      {
         session.setRollbackOnly();
         throw new SpyJMSException("Error getting durable subscription " + sub, e);
      }
      finally
      {
         session.close();
      }
   }

   protected void saveDurableSubscription(DurableSubscription ds) throws JMSException
   {
      JDBCSession session = new JDBCSession();
      try
      {
         PreparedStatement statement = session.prepareStatement(LOCK_SUBSCRIPTION);
         statement.setString(1, ds.getClientID());
         statement.setString(2, ds.getName());
         ResultSet rs = statement.executeQuery();
         session.addResultSet(rs);
         if (rs.next() == false)
         {
            statement = session.prepareStatement(INSERT_SUBSCRIPTION);
            statement.setString(1, ds.getClientID());
            statement.setString(2, ds.getName());
            statement.setString(3, ds.getTopic());
            statement.setString(4, ds.getSelector());
         }
         else
         {
            statement = session.prepareStatement(UPDATE_SUBSCRIPTION);
            statement.setString(1, ds.getTopic());
            statement.setString(2, ds.getSelector());
            statement.setString(3, ds.getClientID());
            statement.setString(4, ds.getName());
         }
         if (statement.executeUpdate() != 1)
         {
            session.setRollbackOnly();
            throw new SpyJMSException("Insert subscription failed " + ds);
         }
      }
      catch (SQLException e)
      {
         session.setRollbackOnly();
         throw new SpyJMSException("Error saving durable subscription " + ds, e);
      }
      finally
      {
         session.close();
      }
   }

   protected void removeDurableSubscription(DurableSubscription ds) throws JMSException
   {
      JDBCSession session = new JDBCSession();
      try
      {
         PreparedStatement statement = session.prepareStatement(REMOVE_SUBSCRIPTION);
         statement.setString(1, ds.getClientID());
         statement.setString(2, ds.getName());
         if (statement.executeUpdate() != 1)
            throw new JMSException("Durable subscription does not exist " + ds);
      }
      catch (SQLException e)
      {
         session.setRollbackOnly();
         throw new SpyJMSException("Error removing durable subscription " + ds, e);
      }
      finally
      {
         session.close();
      }
   }

   public Collection getDurableSubscriptionIdsForTopic(SpyTopic topic) throws JMSException
   {
      ArrayList result = new ArrayList();

      JDBCSession session = new JDBCSession();
      try
      {
         PreparedStatement statement = session.prepareStatement(GET_SUBSCRIPTIONS_FOR_TOPIC);
         statement.setString(1, topic.getName());
         ResultSet rs = statement.executeQuery();
         session.addResultSet(rs);
         while (rs.next())
         {
            result.add(new DurableSubscriptionID(rs.getString(1), rs.getString(2), rs.getString(3)));
         }

         return result;
      }
      catch (SQLException e)
      {
         session.setRollbackOnly();
         throw new SpyJMSException("Error getting durable subscriptions for topic " + topic, e);
      }
      finally
      {
         session.close();
      }
   }

   protected void checkLoggedOnClientId(String clientID) throws JMSException
   {
      JDBCSession session = new JDBCSession();
      try
      {
         PreparedStatement statement = session.prepareStatement(GET_USER_BY_CLIENTID);
         statement.setString(1, clientID);
         ResultSet rs = statement.executeQuery();
         session.addResultSet(rs);
         if (rs.next())
            throw new InvalidClientIDException("This client id is password protected " + clientID);
      }
      catch (SQLException e)
      {
         session.setRollbackOnly();
         throw new SpyJMSException("Error checking logged on client id " + clientID, e);
      }
      finally
      {
         session.close();
      }
   }

   protected String getPreconfClientId(String logon, String passwd) throws JMSException
   {
      JDBCSession session = new JDBCSession();
      try
      {
         PreparedStatement statement = session.prepareStatement(GET_USER);
         statement.setString(1, logon);
         ResultSet rs = statement.executeQuery();
         session.addResultSet(rs);
         if (rs.next() == false)
         {
            if (hasSecurityManager)
               return null;
            else
               throw new JMSSecurityException("This user does not exist " + logon);
         }

         if (hasSecurityManager == false && passwd.equals(rs.getString(1)) == false)
            throw new JMSSecurityException("Bad password for user " + logon);

         return rs.getString(2);
      }
      catch (SQLException e)
      {
         session.setRollbackOnly();
         throw new SpyJMSException("Error retrieving preconfigured user " + logon, e);
      }
      finally
      {
         session.close();
      }
   }

   public StateManager getInstance()
   {
      return this;
   }

   protected void startService() throws Exception
   {
      if (connectionManagerName == null)
         throw new IllegalStateException("No connection manager configured");

      //Find the ConnectionFactoryLoader MBean so we can find the datasource
      String dsName = (String) getServer().getAttribute(connectionManagerName, "BindName");

      InitialContext ctx = new InitialContext();
      try
      {
         dataSource = (DataSource) ctx.lookup(dsName);
         tm = (TransactionManager) ctx.lookup(TransactionManagerService.JNDI_NAME);
      }
      finally
      {
         ctx.close();
      }

      try
      {
         initDB();
      }
      catch (Exception e)
      {
         log.warn("Error initialising state manager db", e);
      }
   }

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
         JDBCSession session = new JDBCSession();
         try
         {
            PreparedStatement statement;
            try
            {
               statement = session.prepareStatement(CREATE_USER_TABLE);
               statement.executeUpdate();
            }
            catch (SQLException ignored)
            {
               log.trace("Error creating table: " + CREATE_USER_TABLE, ignored);
            }
            try
            {
               statement = session.prepareStatement(CREATE_ROLE_TABLE);
               statement.executeUpdate();
            }
            catch (SQLException ignored)
            {
               log.trace("Error creating table: " + CREATE_ROLE_TABLE, ignored);
            }
            try
            {
               statement = session.prepareStatement(CREATE_SUBSCRIPTION_TABLE);
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
                  statement = session.prepareStatement(nextQry);
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
            session.close();
         }
      }
   }

   /**
    * This inner class helps handle the jdbc connections.
    */
   class JDBCSession
   {
      boolean trace = log.isTraceEnabled();

      Transaction threadTx;

      boolean rollback = false;
      
      Connection connection;

      HashSet statements = new HashSet();

      HashSet resultSets = null;

      JDBCSession() throws JMSException
      {
         try
         {
            // Suspend any previous transaction
            threadTx = tm.suspend();
            try
            {
               // Always begin a transaction
               tm.begin();
               try
               {
                  // Retrieve a connection
                  connection = getConnection();
               }
               catch (Throwable t)
               {
                  // Rollback the previously started transaction
                  try
                  {
                     tm.rollback();
                  }
                  catch (Throwable ignored)
                  {
                     log.warn("Unable to rollback transaction", ignored);
                  }
                  throw t;
               }
            }
            catch (Throwable t)
            {
               // Resume the previous transaction
               try
               {
                  if (threadTx != null)
                     tm.resume(threadTx);
               }
               catch (Throwable ignored)
               {
                  log.warn("Unable to resume transaction " + threadTx, ignored);
               }
               throw t;
            }
         }
         catch (Throwable t)
         {
            throw new SpyJMSException("Error creating connection to the database.", t);
         }
      }

      PreparedStatement prepareStatement(String sql) throws SQLException
      {
         PreparedStatement result = connection.prepareStatement(sql);
         statements.add(result);
         return result;
      }

      void setRollbackOnly() throws JMSException
      {
         rollback = true;
      }

      void addResultSet(ResultSet rs)
      {
         if (resultSets == null)
            resultSets = new HashSet();
         resultSets.add(rs);
      }

      void close() throws JMSException
      {
         if (resultSets != null)
         {
            for (Iterator i = resultSets.iterator(); i.hasNext();)
            {
               ResultSet rs = (ResultSet) i.next();
               try
               {
                  rs.close();
               }
               catch (Throwable ignored)
               {
                  if (trace)
                     log.trace("Unable to close result set", ignored);
               }
            }
         }

         for (Iterator i = statements.iterator(); i.hasNext();)
         {
            Statement s = (Statement) i.next();
            try
            {
               s.close();
            }
            catch (Throwable ignored)
            {
               if (trace)
                  log.trace("Unable to close statement", ignored);
            }
         }

         try
         {
            if (connection != null)
               connection.close();
         }
         catch (Throwable ignored)
         {
            if (trace)
               log.trace("Unable to close connection", ignored);
         }

         try
         {
            if (rollback)
            {
               tm.rollback();
            }
            else
            {
               tm.commit();
            }
         }
         catch (Exception e)
         {
            throw new SpyJMSException("Could not commit/rollback a transaction with the transaction manager.", e);
         }
         finally
         {
            try
            {
               if (threadTx != null)
                  tm.resume(threadTx);
            }
            catch (Throwable ignored)
            {
               log.warn("Unable to resume transaction " + threadTx, ignored);
            }
         }
      }

      /**
       * Gets a connection from the datasource, retrying as needed.  This was
       * implemented because in some minimal configurations (i.e. little logging
       * and few services) the database wasn't ready when we tried to get a
       * connection.  We, therefore, implement a retry loop wich is controled
       * by the ConnectionRetryAttempts attribute.  Submitted by terry@amicas.com
       *
       * @exception SQLException if an error occurs.
       */
      protected Connection getConnection() throws SQLException
      {
         int attempts = connectionRetryAttempts;
         int attemptCount = 0;
         SQLException sqlException = null;
         while (attempts-- > 0)
         {
            if (++attemptCount > 1)
            {
               log.debug("Retrying connection: attempt # " + attemptCount);
            }
            try
            {
               sqlException = null;
               return dataSource.getConnection();
            }
            catch (SQLException exception)
            {
               log.debug("Connection attempt # " + attemptCount + " failed with SQLException", exception);
               sqlException = exception;
            }
            finally
            {
               if (sqlException == null && attemptCount > 1)
               {
                  log.debug("Connection succeeded on attempt # " + attemptCount);
               }
            }

            if (attempts > 0)
            {
               try
               {
                  Thread.sleep(1500);
               }
               catch (InterruptedException interruptedException)
               {
                  break;
               }
            }
         }
         if (sqlException != null)
         {
            throw sqlException;
         }
         throw new SQLException("connection attempt interrupted");
      }
   }
}
