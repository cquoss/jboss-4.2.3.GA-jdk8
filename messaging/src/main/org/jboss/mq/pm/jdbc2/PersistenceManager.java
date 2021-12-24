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
package org.jboss.mq.pm.jdbc2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.jms.JMSException;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.Xid;

import org.jboss.mq.SpyDestination;
import org.jboss.mq.SpyJMSException;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.SpyTopic;
import org.jboss.mq.pm.CacheStore;
import org.jboss.mq.pm.PersistenceManagerExt;
import org.jboss.mq.pm.Tx;
import org.jboss.mq.pm.TxManager;
import org.jboss.mq.server.JMSDestination;
import org.jboss.mq.server.MessageCache;
import org.jboss.mq.server.MessageReference;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.tm.TransactionManagerService;
import org.jboss.tm.TransactionTimeoutConfiguration;
import org.jboss.util.UnreachableStatementException;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedLong;

/**
 * This class manages all persistence related services for JDBC based
 * persistence.
 *
 * @author Jayesh Parayali (jayeshpk1@yahoo.com)
 * @author Hiram Chirino (cojonudo14@hotmail.com)
 * @author Adrian Brock (adrian@jboss.com)
 * @version $Revision: 73847 $
 */
public class PersistenceManager extends ServiceMBeanSupport
   implements PersistenceManagerMBean, org.jboss.mq.pm.PersistenceManager, CacheStore, PersistenceManagerExt
{
   /** FAQ about concurrency problems */
   private static String CONCURRENCY_WARNING = "\nCommon reasons for missing messages are \n1) You have multiple JBossMQs running over the same database.\n2) You are using a replicating database that is not keeping up with replication.";
   
   /////////////////////////////////////////////////////////////////////////////////
   //
   // TX state attibutes
   //
   /////////////////////////////////////////////////////////////////////////////////

   /** The next transaction id */
   protected SynchronizedLong nextTransactionId = new SynchronizedLong(0l);
   
   /** The jta transaction manager */
   protected TxManager txManager;
   
   /** The DataSource */
   protected DataSource datasource;
   
   /** The JBossMQ transaction mananger */
   protected TransactionManager tm;
   
   /** The override recovery timeout */
   private int recoveryTimeout = 0;
   
   /** The recovery retries */
   private int recoveryRetries = 0;
   
   /** The recover messages chunk  */
   private int recoverMessagesChunk = 0;

   /** The statement retries */
   private int statementRetries = 5;
   
   /////////////////////////////////////////////////////////////////////////////////
   //
   // JDBC Access Attributes
   //
   /////////////////////////////////////////////////////////////////////////////////

   protected String UPDATE_MARKED_MESSAGES = "UPDATE JMS_MESSAGES SET TXID=?, TXOP=? WHERE TXOP=?";
   protected String UPDATE_MARKED_MESSAGES_XARECOVERY = "UPDATE JMS_MESSAGES SET TXID=?, TXOP=? WHERE TXOP=? AND TXID NOT IN (SELECT TXID FROM JMS_TRANSACTIONS WHERE XID IS NOT NULL)";
   protected String UPDATE_MARKED_MESSAGES_WITH_TX = "UPDATE JMS_MESSAGES SET TXID=?, TXOP=? WHERE TXOP=? AND TXID=?";
   protected String DELETE_MARKED_MESSAGES_WITH_TX =
      "DELETE FROM JMS_MESSAGES WHERE TXID IN (SELECT TXID FROM JMS_TRANSACTIONS) AND TXOP=?";
   protected String DELETE_MARKED_MESSAGES_WITH_TX_XARECOVERY =
      "DELETE FROM JMS_MESSAGES WHERE TXID IN (SELECT TXID FROM JMS_TRANSACTIONS WHERE XID = NULL) AND TXOP=?";
   protected String DELETE_TX = "DELETE FROM JMS_TRANSACTIONS WHERE TXID = ?";
   protected String DELETE_MARKED_MESSAGES = "DELETE FROM JMS_MESSAGES WHERE TXID=? AND TXOP=?";
   protected String DELETE_TEMPORARY_MESSAGES = "DELETE FROM JMS_MESSAGES WHERE TXOP = 'T'";
   protected String INSERT_TX = "INSERT INTO JMS_TRANSACTIONS (TXID) values(?)";
   protected String INSERT_TX_XARECOVERY = "INSERT INTO JMS_TRANSACTIONS (TXID, XID) values(?, ?)";
   protected String DELETE_ALL_TX = "DELETE FROM JMS_TRANSACTIONS";
   protected String DELETE_ALL_TX_XARECOVERY = "DELETE FROM JMS_TRANSACTIONS WHERE XID = NULL";
   protected String SELECT_MAX_TX = "SELECT MAX(TXID) FROM (SELECT MAX(TXID) FROM JMS_TRANSACTIONS UNION SELECT MAX(TXID) FROM JMS_MESSAGES)";
   protected String SELECT_ALL_TX_XARECOVERY = "SELECT TXID, XID FROM JMS_TRANSACTIONS";
   protected String SELECT_MESSAGES_IN_DEST = "SELECT MESSAGEID, MESSAGEBLOB FROM JMS_MESSAGES WHERE DESTINATION=?";
   protected String SELECT_MESSAGES_IN_DEST_XARECOVERY = "SELECT MESSAGEID, MESSAGEBLOB, TXID, TXOP FROM JMS_MESSAGES WHERE DESTINATION=?";
   protected String SELECT_MESSAGE_KEYS_IN_DEST = "SELECT MESSAGEID FROM JMS_MESSAGES WHERE DESTINATION=?";
   protected String SELECT_MESSAGE = "SELECT MESSAGEID, MESSAGEBLOB FROM JMS_MESSAGES WHERE MESSAGEID=? AND DESTINATION=?";
   protected String SELECT_MESSAGE_XARECOVERY = "SELECT MESSAGEID, MESSAGEBLOB, TXID, TXOP FROM JMS_MESSAGES WHERE MESSAGEID=? AND DESTINATION=?";
   protected String INSERT_MESSAGE =
      "INSERT INTO JMS_MESSAGES (MESSAGEID, DESTINATION, MESSAGEBLOB, TXID, TXOP) VALUES(?,?,?,?,?)";
   protected String MARK_MESSAGE = "UPDATE JMS_MESSAGES SET TXID=?, TXOP=? WHERE MESSAGEID=? AND DESTINATION=?";
   protected String DELETE_MESSAGE = "DELETE FROM JMS_MESSAGES WHERE MESSAGEID=? AND DESTINATION=?";
   protected String UPDATE_MESSAGE = "UPDATE JMS_MESSAGES SET MESSAGEBLOB=? WHERE MESSAGEID=? AND DESTINATION=?";
   protected String CREATE_MESSAGE_TABLE =
      "CREATE TABLE JMS_MESSAGES ( MESSAGEID INTEGER NOT NULL, "
         + "DESTINATION VARCHAR(32) NOT NULL, TXID INTEGER, TXOP CHAR(1),"
         + "MESSAGEBLOB OBJECT, PRIMARY KEY (MESSAGEID, DESTINATION) )";
   protected String CREATE_IDX_MESSAGE_TXOP_TXID = "CREATE INDEX JMS_MESSAGES_TXOP_TXID ON JMS_MESSAGES (TXOP, TXID)";
   protected String CREATE_IDX_MESSAGE_DESTINATION = "CREATE INDEX JMS_MESSAGES_DESTINATION ON JMS_MESSAGES (DESTINATION)";
   protected String CREATE_TX_TABLE = "CREATE TABLE JMS_TRANSACTIONS ( TXID INTEGER, PRIMARY KEY (TXID) )";
   protected String CREATE_TX_TABLE_XARECOVERY = "CREATE TABLE JMS_TRANSACTIONS ( TXID INTEGER, XID OBJECT, PRIMARY KEY (TXID) )";

   protected static final int OBJECT_BLOB = 0;
   protected static final int BYTES_BLOB = 1;
   protected static final int BINARYSTREAM_BLOB = 2;
   protected static final int BLOB_BLOB = 3;

   protected int blobType = OBJECT_BLOB;
   protected boolean createTables;

   protected  int connectionRetryAttempts = 5;

   protected boolean xaRecovery = false;
   
   /////////////////////////////////////////////////////////////////////////////////
   //
   // Constructor.
   //
   /////////////////////////////////////////////////////////////////////////////////
   public PersistenceManager() throws javax.jms.JMSException
   {
      txManager = new TxManager(this);
   }

   /**
    * This inner class helps handle the tx management of the jdbc connections.
    * 
    */
   protected class TransactionManagerStrategy
   {
      boolean rollback = false;

      Transaction threadTx;

      void startTX() throws JMSException
      {
         try
         {
            // Thread arriving must be clean (jboss doesn't set the thread
            // previously). However optimized calls come with associated
            // thread for example. We suspend the thread association here, and
            // resume in the finally block of the following try.
            threadTx = tm.suspend();

            // Always begin a transaction
            tm.begin();
         }
         catch (Exception e)
         {
            try
            {
               if (threadTx != null)
                  tm.resume(threadTx);
            }
            catch (Exception ignore)
            {
            }
            throw new SpyJMSException("Could not start a transaction with the transaction manager.", e);
         }
      }

      void setRollbackOnly() throws JMSException
      {
         rollback = true;
      }

      void endTX() throws JMSException
      {
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
            throw new SpyJMSException("Could not start a transaction with the transaction manager.", e);
         }
         finally
         {
            try
            {
               if (threadTx != null)
                  tm.resume(threadTx);
            }
            catch (Exception ignore)
            {
            }
         }
      }
   }

   /////////////////////////////////////////////////////////////////////////////////
   //
   // TX Resolution.
   //
   /////////////////////////////////////////////////////////////////////////////////

   synchronized protected void createSchema() throws JMSException
   {
      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      PreparedStatement stmt = null;
      boolean threadWasInterrupted = Thread.interrupted();
      try
      {
         if (createTables)
         {
            c = this.getConnection();

            boolean createdMessageTable = false;
            try
            {
               stmt = c.prepareStatement(CREATE_MESSAGE_TABLE);
               stmt.executeUpdate();
               createdMessageTable = true;
            }
            catch (SQLException e)
            {
               log.debug("Could not create table with SQL: " + CREATE_MESSAGE_TABLE, e);
            }
            finally
            {
               try
               {
                  if (stmt != null)
                     stmt.close();
               }
               catch (Throwable ignored)
               {
                  log.trace("Ignored: " + ignored);
               }
               stmt = null;
            }

            if (createdMessageTable)
            {
               try
               {
                  stmt = c.prepareStatement(CREATE_IDX_MESSAGE_TXOP_TXID);
                  stmt.executeUpdate();
               }
               catch (SQLException e)
               {
                  log.debug("Could not create index with SQL: " + CREATE_IDX_MESSAGE_TXOP_TXID, e);
               }
               finally
               {
                  try
                  {
                     if (stmt != null)
                        stmt.close();
                  }
                  catch (Throwable ignored)
                  {
                     log.trace("Ignored: " + ignored);
                  }
                  stmt = null;
               }
               try
               {
                  stmt = c.prepareStatement(CREATE_IDX_MESSAGE_DESTINATION);
                  stmt.executeUpdate();
               }
               catch (SQLException e)
               {
                  log.debug("Could not create index with SQL: " + CREATE_IDX_MESSAGE_DESTINATION, e);
               }
               finally
               {
                  try
                  {
                     if (stmt != null)
                        stmt.close();
                  }
                  catch (Throwable ignored)
                  {
                     log.trace("Ignored: " + ignored);
                  }
                  stmt = null;
               }
            }

            String createTxTable = CREATE_TX_TABLE;
            if (xaRecovery)
               createTxTable = CREATE_TX_TABLE_XARECOVERY;
            try
            {
               stmt = c.prepareStatement(createTxTable);
               stmt.executeUpdate();
            }
            catch (SQLException e)
            {
               log.debug("Could not create table with SQL: " + createTxTable, e);
            }
            finally
            {
               try
               {
                  if (stmt != null)
                     stmt.close();
               }
               catch (Throwable ignored)
               {
                  log.trace("Ignored: " + ignored);
               }
               stmt = null;
            }
         }
      }
      catch (SQLException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not get a connection for jdbc2 table construction ", e);
      }
      finally
      {
         try
         {
            if (stmt != null)
               stmt.close();
         }
         catch (Throwable ignore)
         {
         }
         stmt = null;
         try
         {
            if (c != null)
               c.close();
         }
         catch (Throwable ignore)
         {
         }
         c = null;
         tms.endTX();

         // Restore the interrupted state of the thread
         if (threadWasInterrupted)
            Thread.currentThread().interrupt();
      }
   }

   synchronized protected void resolveAllUncommitedTXs() throws JMSException
   {
      // We perform recovery in a different thread to the table creation
      // Postgres doesn't like create table failing in the same transaction
      // as other operations

      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      boolean threadWasInterrupted = Thread.interrupted();
      try
      {
         c = this.getConnection();

         // Find out what the next TXID should be
         stmt = c.prepareStatement(SELECT_MAX_TX);
         rs = stmt.executeQuery();
         if (rs.next())
            nextTransactionId.set(rs.getLong(1) + 1);
         rs.close();
         rs = null;
         stmt.close();
         stmt = null;

         // Delete all the temporary messages.
         stmt = c.prepareStatement(DELETE_TEMPORARY_MESSAGES);
         stmt.executeUpdate();
         stmt.close();
         stmt = null;

         // Delete all the messages that were added but thier tx's were not commited.
         String deleteMarkedMessagesWithTx = DELETE_MARKED_MESSAGES_WITH_TX;
         if (xaRecovery)
            deleteMarkedMessagesWithTx = DELETE_MARKED_MESSAGES_WITH_TX_XARECOVERY;
         stmt = c.prepareStatement(deleteMarkedMessagesWithTx);
         stmt.setString(1, "A");
         stmt.executeUpdate();
         stmt.close();
         stmt = null;

         // Restore all the messages that were removed but their tx's were not commited.
         String updateMarkedMessages = UPDATE_MARKED_MESSAGES;
         if (xaRecovery)
            updateMarkedMessages = UPDATE_MARKED_MESSAGES_XARECOVERY;
         stmt = c.prepareStatement(updateMarkedMessages);
         stmt.setNull(1, java.sql.Types.BIGINT);
         stmt.setString(2, "A");
         stmt.setString(3, "D");
         stmt.executeUpdate();
         stmt.close();
         stmt = null;

         // Now recovery is complete, clear the transaction table.
         String deleteAllTx = DELETE_ALL_TX;
         if (xaRecovery)
            deleteAllTx = DELETE_ALL_TX_XARECOVERY;
         stmt = c.prepareStatement(deleteAllTx);
         stmt.execute();
         stmt.close();
         stmt = null;

         // If we are doing XARecovery restore the prepared transactions
         if (xaRecovery)
         {
            stmt = c.prepareStatement(SELECT_ALL_TX_XARECOVERY);
            rs = stmt.executeQuery();
            while (rs.next())
            {
               long txid = rs.getLong(1);
               Xid xid = extractXid(rs, 2);
               Tx tx = new Tx(txid);
               tx.setXid(xid);
               tx.checkPersisted();
               txManager.restoreTx(tx);
            }
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
         }
      }
      catch (Exception e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not resolve uncommited transactions.  Message recovery may not be accurate", e);
      }
      finally
      {
         try
         {
            if (rs != null)
               rs.close();
         }
         catch (Throwable ignore)
         {
         }
         try
         {
            if (stmt != null)
               stmt.close();
         }
         catch (Throwable ignore)
         {
         }
         try
         {
            if (c != null)
               c.close();
         }
         catch (Throwable ignore)
         {
         }
         tms.endTX();

         // Restore the interrupted state of the thread
         if (threadWasInterrupted)
            Thread.currentThread().interrupt();
      }
   }

   /////////////////////////////////////////////////////////////////////////////////
   //
   // Message Recovery
   //
   /////////////////////////////////////////////////////////////////////////////////
   
   synchronized public void restoreQueue(JMSDestination jmsDest, SpyDestination dest) throws JMSException
   {
      if (jmsDest == null)
         throw new IllegalArgumentException("Must supply non null JMSDestination to restoreQueue");
      if (dest == null)
         throw new IllegalArgumentException("Must supply non null SpyDestination to restoreQueue");

      boolean canOverrideTimeout = (tm instanceof TransactionTimeoutConfiguration);
      int previousTimeout = 0;
      try
      {
         // Set our timeout
         if (recoveryTimeout != 0)
         {
            if (canOverrideTimeout)
            {
               previousTimeout = ((TransactionTimeoutConfiguration) tm).getTransactionTimeout();
               tm.setTransactionTimeout(recoveryTimeout);
            }
            else
            {
               log.debug("Cannot override recovery timeout, TransactionManager does implement " + TransactionTimeoutConfiguration.class.getName());
            }
         }
         
         // restore the queue
         try
         {
            internalRestoreQueue(jmsDest, dest);
         }
         finally
         {
            // restore the transaction timeout
            if (recoveryTimeout != 0 && canOverrideTimeout)
               tm.setTransactionTimeout(previousTimeout);
         }
      }
      catch (Exception e)
      {
         SpyJMSException.rethrowAsJMSException("Unexpected error in recovery", e);
      }
   }
   
   synchronized protected void internalRestoreQueue(JMSDestination jmsDest, SpyDestination dest) throws JMSException
   {
      // Work out the prepared transactions
      Map prepared = null;
      if (xaRecovery)
      {
         prepared = new HashMap();
         Map map = txManager.getPreparedTransactions();
         for (Iterator i = map.values().iterator(); i.hasNext();)
         {
            TxManager.PreparedInfo info = (TxManager.PreparedInfo) i.next();
            for (Iterator j = info.getTxids().iterator(); j.hasNext();)
            {
               Tx tx = (Tx) j.next();
               prepared.put(new Long(tx.longValue()), tx);
            }
         }
      }
      
      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      PreparedStatement stmt = null;
      PreparedStatement stmt2 = null; 
      ResultSet rs = null;
      boolean threadWasInterrupted = Thread.interrupted();
      try
      {
         String selectMessagesInDest = SELECT_MESSAGES_IN_DEST;
         String selectMessage = SELECT_MESSAGE;
         if (xaRecovery)
         {
            selectMessagesInDest = SELECT_MESSAGES_IN_DEST_XARECOVERY;
            selectMessage = SELECT_MESSAGE_XARECOVERY;
         }
         c = this.getConnection();
         if (recoverMessagesChunk == 0)
            stmt = c.prepareStatement(selectMessagesInDest);
         else
         {
            stmt = c.prepareStatement(SELECT_MESSAGE_KEYS_IN_DEST);
            stmt2 = c.prepareStatement(selectMessage);
         }
         stmt.setString(1, dest.toString());

         long txid = 0;
         String txop = null;
         rs = stmt.executeQuery();
         int counter = 0;
         int recovery = 0;
         while (rs.next())
         {
            long msgid = rs.getLong(1);
            SpyMessage message = null;
            if (recoverMessagesChunk == 0)
            {
               message = extractMessage(rs);
               if (xaRecovery)
               {
                  txid = rs.getLong(3);
                  txop = rs.getString(4);
               }
            }
            else
            {
               ResultSet rs2 = null;
               try
               {
                  stmt2.setLong(1, msgid);
                  stmt2.setString(2, dest.toString());
                  rs2 = stmt2.executeQuery();
                  if (rs2.next())
                  {
                     message = extractMessage(rs2);
                     if (xaRecovery)
                     {
                        txid = rs2.getLong(3);
                        txop = rs2.getString(4);
                     }
                  }
                  else
                     log.warn("Failed to find message msgid=" + msgid +" dest=" + dest);
               }
               finally
               {
                  if (rs2 != null)
                  {
                     try
                     {
                        rs2.close();
                     }
                     catch (Exception ignored)
                     {
                     }
                  }
               }
            }
            // The durable subscription is not serialized
            if (dest instanceof SpyTopic)
               message.header.durableSubscriberID = ((SpyTopic) dest).getDurableSubscriptionID();

            if (xaRecovery == false || txid == 0 || txop == null)
               jmsDest.restoreMessage(message);
            else
            {
               Tx tx = (Tx) prepared.get(new Long(txid));
               if (tx == null)
                  jmsDest.restoreMessage(message);
               else if ("A".equals(txop))
               {
                  jmsDest.restoreMessage(message, tx, Tx.ADD);
                  recovery++;
               }
               else if ("D".equals(txop))
               {
                  jmsDest.restoreMessage(message, tx, Tx.REMOVE);
                  recovery++;
               }
               else
                  throw new IllegalStateException("Unknown txop=" + txop + " for msg=" + msgid + " dest=" + dest);
            }
            counter++;
         }

         log.debug("Restored " + counter + " message(s) to: " + dest + " " + recovery + " need recovery.");
      }
      catch (IOException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not restore messages to destination : " + dest.toString(), e);
      }
      catch (SQLException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not restore messages to destination : " + dest.toString(), e);
      }
      finally
      {
         try
         {
            if (rs != null)
               rs.close();
         }
         catch (Throwable ignore)
         {
         }
         try
         {
            if (stmt != null)
               stmt.close();
         }
         catch (Throwable ignore)
         {
         }
         try
         {
            if (c != null)
               c.close();
         }
         catch (Throwable ignore)
         {
         }
         tms.endTX();

         // Restore the interrupted state of the thread
         if (threadWasInterrupted)
            Thread.currentThread().interrupt();
      }

   }

   SpyMessage extractMessage(ResultSet rs) throws SQLException, IOException
   {
      try
      {
         long messageid = rs.getLong(1);

         SpyMessage message = null;

         if (blobType == OBJECT_BLOB)
         {

            message = (SpyMessage) rs.getObject(2);

         }
         else if (blobType == BYTES_BLOB)
         {

            byte[] st = rs.getBytes(2);
            ByteArrayInputStream baip = new ByteArrayInputStream(st);
            ObjectInputStream ois = new ObjectInputStream(baip);
            message = SpyMessage.readMessage(ois);

         }
         else if (blobType == BINARYSTREAM_BLOB)
         {

            ObjectInputStream ois = new ObjectInputStream(rs.getBinaryStream(2));
            message = SpyMessage.readMessage(ois);

         }
         else if (blobType == BLOB_BLOB)
         {

            ObjectInputStream ois = new ObjectInputStream(rs.getBlob(2).getBinaryStream());
            message = SpyMessage.readMessage(ois);
         }

         message.header.messageId = messageid;
         return message;
      }
      catch (StreamCorruptedException e)
      {
         throw new IOException("Could not load the message: " + e);
      }
   }

   Xid extractXid(ResultSet rs, int column) throws SQLException, IOException, ClassNotFoundException
   {
      try
      {
         Xid xid = null;

         if (blobType == OBJECT_BLOB)
         {
            xid = (Xid) rs.getObject(column);
         }
         else if (blobType == BYTES_BLOB)
         {
            byte[] st = rs.getBytes(column);
            ByteArrayInputStream baip = new ByteArrayInputStream(st);
            ObjectInputStream ois = new ObjectInputStream(baip);
            xid = (Xid) ois.readObject();
         }
         else if (blobType == BINARYSTREAM_BLOB)
         {
            ObjectInputStream ois = new ObjectInputStream(rs.getBinaryStream(column));
            xid = (Xid) ois.readObject();
         }
         else if (blobType == BLOB_BLOB)
         {
            ObjectInputStream ois = new ObjectInputStream(rs.getBlob(column).getBinaryStream());
            xid = (Xid) ois.readObject();
         }

         return xid;
      }
      catch (StreamCorruptedException e)
      {
         throw new IOException("Could not load the message: " + e);
      }
   }
   
   public void forcePersistentTx(Tx txId) throws javax.jms.JMSException
   {
      // No need if already done or not doing xa recovery
      if (txId.wasPersisted() || xaRecovery == false)
         return;
      
      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      boolean threadWasInterrupted = Thread.interrupted();
      try
      {

         c = this.getConnection();
         insertPersistentTx(tms, c, txId);
      }
      catch (SQLException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not commit tx: " + txId, e);
      }
      finally
      {
         try
         {
            if (c != null)
               c.close();
         }
         catch (Throwable ignore)
         {
         }
         tms.endTX();

         // Restore the interrupted state of the thread
         if (threadWasInterrupted)
            Thread.currentThread().interrupt();
      }
   }

   /////////////////////////////////////////////////////////////////////////////////
   //
   // TX Commit
   //
   /////////////////////////////////////////////////////////////////////////////////
   public void commitPersistentTx(Tx txId) throws javax.jms.JMSException
   {
      if (txId.wasPersisted() == false)
         return;
      
      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      boolean threadWasInterrupted = Thread.interrupted();
      try
      {

         c = this.getConnection();
         removeMarkedMessages(c, txId, "D");
         removeTXRecord(c, txId.longValue());

      }
      catch (SQLException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not commit tx: " + txId, e);
      }
      finally
      {
         try
         {
            if (c != null)
               c.close();
         }
         catch (Throwable ignore)
         {
         }
         tms.endTX();

         // Restore the interrupted state of the thread
         if (threadWasInterrupted)
            Thread.currentThread().interrupt();
      }
   }

   public void removeMarkedMessages(Connection c, Tx txid, String mark) throws SQLException
   {
      PreparedStatement stmt = null;
      try
      {
         stmt = c.prepareStatement(DELETE_MARKED_MESSAGES);
         stmt.setLong(1, txid.longValue());
         stmt.setString(2, mark);
         stmt.executeUpdate();
      }
      finally
      {
         try
         {
            if (stmt != null)
               stmt.close();
         }
         catch (Throwable e)
         {
         }
      }
   }

   public void addTXRecord(Connection c, Tx txid) throws SQLException, IOException
   {
      PreparedStatement stmt = null;
      try
      {
         String insertTx = INSERT_TX;
         if (xaRecovery)
            insertTx = INSERT_TX_XARECOVERY;
         stmt = c.prepareStatement(insertTx);
         stmt.setLong(1, txid.longValue());
         if (xaRecovery)
         {
            Xid xid = txid.getXid();
            if (xid != null)
               setBlob(stmt, 2, xid);
            else
               stmt.setNull(2, java.sql.Types.BLOB);
         }
         stmt.executeUpdate();
      }
      finally
      {
         try
         {
            if (stmt != null)
               stmt.close();
         }
         catch (Throwable e)
         {
         }
      }
   }

   public void removeTXRecord(Connection c, long txid) throws SQLException
   {
      PreparedStatement stmt = null;
      try
      {
         stmt = c.prepareStatement(DELETE_TX);
         stmt.setLong(1, txid);
         stmt.executeUpdate();
      }
      finally
      {
         try
         {
            if (stmt != null)
               stmt.close();
         }
         catch (Throwable e)
         {
         }
      }
   }

   /////////////////////////////////////////////////////////////////////////////////
   //
   // TX Rollback
   //
   /////////////////////////////////////////////////////////////////////////////////
   public void rollbackPersistentTx(Tx txId) throws JMSException
   {
      if (txId.wasPersisted() == false)
         return;

      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      PreparedStatement stmt = null;
      boolean threadWasInterrupted = Thread.interrupted();
      try
      {

         c = this.getConnection();
         removeMarkedMessages(c, txId, "A");
         removeTXRecord(c, txId.longValue());

         // Restore all the messages that were logically removed.
         stmt = c.prepareStatement(UPDATE_MARKED_MESSAGES_WITH_TX);
         stmt.setNull(1, java.sql.Types.BIGINT);
         stmt.setString(2, "A");
         stmt.setString(3, "D");
         stmt.setLong(4, txId.longValue());
         stmt.executeUpdate();
         stmt.close();
         stmt = null;
      }
      catch (SQLException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not rollback tx: " + txId, e);
      }
      finally
      {
         try
         {
            if (stmt != null)
               stmt.close();
         }
         catch (Throwable ignore)
         {
         }
         try
         {
            if (c != null)
               c.close();
         }
         catch (Throwable ignore)
         {
         }
         tms.endTX();

         // Restore the interrupted state of the thread
         if (threadWasInterrupted)
            Thread.currentThread().interrupt();
      }

   }

   /////////////////////////////////////////////////////////////////////////////////
   //
   // TX Creation
   //
   /////////////////////////////////////////////////////////////////////////////////
   public Tx createPersistentTx() throws JMSException
   {
      Tx id = new Tx(nextTransactionId.increment());
      return id;
   }

   public void insertPersistentTx(TransactionManagerStrategy tms, Connection c, Tx tx) throws JMSException
   {
      try
      {
         if (tx != null && tx.checkPersisted() == false)
            addTXRecord(c, tx);
      }
      catch (Exception e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not create tx: " + tx.longValue(), e);
      }
   }

   /////////////////////////////////////////////////////////////////////////////////
   //
   // Adding a message
   //
   /////////////////////////////////////////////////////////////////////////////////
   public void add(MessageReference messageRef, Tx txId) throws javax.jms.JMSException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("About to add message " + messageRef + " transaction=" + txId);

      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      boolean threadWasInterrupted = Thread.interrupted();
      try
      {
         c = this.getConnection();

         // Lazily write the peristent transaction
         insertPersistentTx(tms, c, txId);
         
         // Synchronize on the message to avoid a race with the softener
         synchronized (messageRef)
         {
            SpyMessage message = messageRef.getMessage();

            // has it allready been stored by the message cache interface??
            if (messageRef.stored == MessageReference.STORED)
            {
               if (trace)
                  log.trace("Updating message " + messageRef + " transaction=" + txId);

               markMessage(c, messageRef.messageId, messageRef.getPersistentKey(), txId, "A");
            }
            else
            {
               if (trace)
                  log.trace("Inserting message " + messageRef + " transaction=" + txId);

               add(c, messageRef.getPersistentKey(), message, txId, "A");
               messageRef.setStored(MessageReference.STORED);
            }
            if (trace)
               log.trace("Added message " + messageRef + " transaction=" + txId);
         }
      }
      catch (IOException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not store message: " + messageRef, e);
      }
      catch (SQLException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not store message: " + messageRef, e);
      }
      finally
      {
         try
         {
            if (c != null)
               c.close();
         }
         catch (Throwable ignore)
         {
         }
         tms.endTX();

         // Restore the interrupted state of the thread
         if (threadWasInterrupted)
            Thread.currentThread().interrupt();
      }
   }

   protected void add(Connection c, String queue, SpyMessage message, Tx txId, String mark)
      throws SQLException, IOException
   {
      PreparedStatement stmt = null;
      try
      {

         stmt = c.prepareStatement(INSERT_MESSAGE);

         stmt.setLong(1, message.header.messageId);
         stmt.setString(2, queue);
         setBlob(stmt, 3, message);

         if (txId != null)
            stmt.setLong(4, txId.longValue());
         else
            stmt.setNull(4, java.sql.Types.BIGINT);
         stmt.setString(5, mark);

         stmt.executeUpdate();
      }
      finally
      {
         try
         {
            if (stmt != null)
               stmt.close();
         }
         catch (Throwable ignore)
         {
         }
      }
   }

   public void markMessage(Connection c, long messageid, String destination, Tx txId, String mark)
      throws SQLException
   {
      PreparedStatement stmt = null;
      try
      {

         stmt = c.prepareStatement(MARK_MESSAGE);
         if (txId == null)
         {
            stmt.setNull(1, java.sql.Types.BIGINT);
         }
         else
         {
            stmt.setLong(1, txId.longValue());
         }
         stmt.setString(2, mark);
         stmt.setLong(3, messageid);
         stmt.setString(4, destination);
         stmt.executeUpdate();
      }
      finally
      {
         try
         {
            if (stmt != null)
               stmt.close();
         }
         catch (Throwable ignore)
         {
         }
      }

   }

   public void setBlob(PreparedStatement stmt, int column, SpyMessage message) throws IOException, SQLException
   {
      if (blobType == OBJECT_BLOB)
      {
         stmt.setObject(column, message);
      }
      else if (blobType == BYTES_BLOB)
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         SpyMessage.writeMessage(message, oos);
         oos.flush();
         byte[] messageAsBytes = baos.toByteArray();
         stmt.setBytes(column, messageAsBytes);
      }
      else if (blobType == BINARYSTREAM_BLOB)
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         SpyMessage.writeMessage(message, oos);
         oos.flush();
         byte[] messageAsBytes = baos.toByteArray();
         ByteArrayInputStream bais = new ByteArrayInputStream(messageAsBytes);
         stmt.setBinaryStream(column, bais, messageAsBytes.length);
      }
      else if (blobType == BLOB_BLOB)
      {

         throw new RuntimeException("BLOB_TYPE: BLOB_BLOB is not yet implemented.");
         /** TODO:
         ByteArrayOutputStream baos= new ByteArrayOutputStream();
         ObjectOutputStream oos= new ObjectOutputStream(baos);
         oos.writeObject(message);
         byte[] messageAsBytes= baos.toByteArray();
         ByteArrayInputStream bais= new ByteArrayInputStream(messageAsBytes);
         stmt.setBsetBinaryStream(column, bais, messageAsBytes.length);
         */
      }
   }

   public void setBlob(PreparedStatement stmt, int column, Xid xid) throws IOException, SQLException
   {
      if (blobType == OBJECT_BLOB)
      {
         stmt.setObject(column, xid);
      }
      else if (blobType == BYTES_BLOB)
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(xid);
         oos.flush();
         byte[] messageAsBytes = baos.toByteArray();
         stmt.setBytes(column, messageAsBytes);
      }
      else if (blobType == BINARYSTREAM_BLOB)
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(xid);
         oos.flush();
         byte[] messageAsBytes = baos.toByteArray();
         ByteArrayInputStream bais = new ByteArrayInputStream(messageAsBytes);
         stmt.setBinaryStream(column, bais, messageAsBytes.length);
      }
      else if (blobType == BLOB_BLOB)
      {

         throw new RuntimeException("BLOB_TYPE: BLOB_BLOB is not yet implemented.");
         /** TODO:
         ByteArrayOutputStream baos= new ByteArrayOutputStream();
         ObjectOutputStream oos= new ObjectOutputStream(baos);
         oos.writeObject(xid);
         byte[] messageAsBytes= baos.toByteArray();
         ByteArrayInputStream bais= new ByteArrayInputStream(messageAsBytes);
         stmt.setBsetBinaryStream(column, bais, messageAsBytes.length);
         */
      }
   }

   /////////////////////////////////////////////////////////////////////////////////
   //
   // Updating a message
   //
   /////////////////////////////////////////////////////////////////////////////////
   public void update(MessageReference messageRef, Tx txId) throws javax.jms.JMSException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("Updating message " + messageRef + " transaction=" + txId);

      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      PreparedStatement stmt = null;
      boolean threadWasInterrupted = Thread.interrupted();
      try
      {

         c = this.getConnection();
         if (txId == null)
         {

            stmt = c.prepareStatement(UPDATE_MESSAGE);
            setBlob(stmt, 1, messageRef.getMessage());
            stmt.setLong(2, messageRef.messageId);
            stmt.setString(3, messageRef.getPersistentKey());
            int rc = stmt.executeUpdate();
            if (rc != 1)
               throw new SpyJMSException(
                  "Could not update the message in the database: update affected " + rc + " rows");
         }
         else
         {
            throw new SpyJMSException("NYI: Updating a message in a transaction is not currently used");
         }
         if (trace)
            log.trace("Updated message " + messageRef + " transaction=" + txId);

      }
      catch (IOException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not update message: " + messageRef, e);
      }
      catch (SQLException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not update message: " + messageRef, e);
      }
      finally
      {
         try
         {
            if (stmt != null)
               stmt.close();
         }
         catch (Throwable ignore)
         {
         }
         try
         {
            if (c != null)
               c.close();
         }
         catch (Throwable ignore)
         {
         }
         tms.endTX();

         // Restore the interrupted state of the thread
         if (threadWasInterrupted)
            Thread.currentThread().interrupt();
      }

   }

   /////////////////////////////////////////////////////////////////////////////////
   //
   // Removing a message
   //
   /////////////////////////////////////////////////////////////////////////////////
   public void remove(MessageReference messageRef, Tx txId) throws javax.jms.JMSException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("Removing message " + messageRef + " transaction=" + txId);

      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      PreparedStatement stmt = null;
      boolean threadWasInterrupted = Thread.interrupted();
      try
      {
         c = this.getConnection();

         // Lazily write the peristent transaction
         insertPersistentTx(tms, c, txId);
         
         // Synchronize on the message to avoid a race with the softener
         synchronized (messageRef)
         {
            if (txId == null)
            {
               stmt = c.prepareStatement(DELETE_MESSAGE);
               stmt.setLong(1, messageRef.messageId);
               stmt.setString(2, messageRef.getPersistentKey());

               // Adrian Brock:
               // Remove the message from the cache, but don't 
               // return it to the pool just yet. The queue still holds
               // a reference to the message and will return it
               // to the pool once it gets enough time slice.
               // The alternative is to remove the validation
               // for double removal from the cache, 
               // which I don't want to do because it is useful
               // for spotting errors
               messageRef.setStored(MessageReference.NOT_STORED);
               messageRef.removeDelayed();
            }
            else
            {
               stmt = c.prepareStatement(MARK_MESSAGE);
               stmt.setLong(1, txId.longValue());
               stmt.setString(2, "D");
               stmt.setLong(3, messageRef.messageId);
               stmt.setString(4, messageRef.getPersistentKey());
            }

             int tries = 0;
             while (true)
             {
                try
                {
                   int rc = stmt.executeUpdate();

                   if (tries > 0)
                   {
                      if (rc != 1)
                         throw new SpyJMSException(
                           "Could not mark the message as deleted in the database: update affected " + rc + " rows." + CONCURRENCY_WARNING);

                      log.warn("Remove operation worked after " +tries +" retries");
                   }
                   break;
                }
                catch (SQLException e)
                {
                   log.warn("SQLException caught - assuming deadlock detected, try:" + (tries + 1), e);
                   tries++;
                   if (tries >= statementRetries)
                   {
                      log.error("Retried " + tries + " times, now giving up");
                      throw new IllegalStateException("Could not remove message after " +tries + "attempts");
                   }
                   log.warn("Trying again after a pause");
                   //Now we wait for a random amount of time to minimise risk of deadlock
                   Thread.sleep((long)(Math.random() * 500));
                }
             }

            if (trace)
               log.trace("Removed message " + messageRef + " transaction=" + txId);
         }
      }
      catch (Exception e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not remove message: " + messageRef, e);
      }
      finally
      {
         try
         {
            if (stmt != null)
               stmt.close();
         }
         catch (Throwable ignore)
         {
         }
         try
         {
            if (c != null)
               c.close();
         }
         catch (Throwable ignore)
         {
         }
         tms.endTX();

         // Restore the interrupted state of the thread
         if (threadWasInterrupted)
            Thread.currentThread().interrupt();
      }

   }

   /////////////////////////////////////////////////////////////////////////////////
   //
   // Misc. PM functions
   //
   /////////////////////////////////////////////////////////////////////////////////

   public TxManager getTxManager()
   {
      return txManager;
   }

   public void closeQueue(JMSDestination jmsDest, SpyDestination dest) throws JMSException
   {
      // Nothing to clean up, all the state is in the db.
   }

   public SpyMessage loadFromStorage(MessageReference messageRef) throws JMSException
   {
      if (log.isTraceEnabled())
         log.trace("Loading message from storage " + messageRef);

      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      boolean threadWasInterrupted = Thread.interrupted();
      try
      {

         c = this.getConnection();
         stmt = c.prepareStatement(SELECT_MESSAGE);
         stmt.setLong(1, messageRef.messageId);
         stmt.setString(2, messageRef.getPersistentKey());

         rs = stmt.executeQuery();
         if (rs.next())
            return extractMessage(rs);
         else
            throw new SpyJMSException("Could not load message from storage: " + messageRef + " " + CONCURRENCY_WARNING);

      }
      catch (Exception e)
      {
         tms.setRollbackOnly();
         SpyJMSException.rethrowAsJMSException("Could not load message : " + messageRef, e);
         throw new UnreachableStatementException();
      }
      finally
      {
         try
         {
            if (rs != null)
               rs.close();
         }
         catch (Throwable ignore)
         {
         }
         try
         {
            if (stmt != null)
               stmt.close();
         }
         catch (Throwable ignore)
         {
         }
         try
         {
            if (c != null)
               c.close();
         }
         catch (Throwable ignore)
         {
         }
         tms.endTX();

         // Restore the interrupted state of the thread
         if (threadWasInterrupted)
            Thread.currentThread().interrupt();
      }
   }

   /////////////////////////////////////////////////////////////////////////////////
   //
   // CacheStore Functions
   //
   /////////////////////////////////////////////////////////////////////////////////   
   public void removeFromStorage(MessageReference messageRef) throws JMSException
   {
      // We don't remove persistent messages sent to persistent queues
      if (messageRef.isPersistent())
         return;

      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("Removing message from storage " + messageRef);

      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      PreparedStatement stmt = null;
      boolean threadWasInterrupted = Thread.interrupted();
      try
      {
         c = this.getConnection();
         stmt = c.prepareStatement(DELETE_MESSAGE);
         stmt.setLong(1, messageRef.messageId);
         stmt.setString(2, messageRef.getPersistentKey());
         stmt.executeUpdate();
         messageRef.setStored(MessageReference.NOT_STORED);

         if (trace)
            log.trace("Removed message from storage " + messageRef);
      }
      catch (SQLException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not remove message: " + messageRef, e);
      }
      finally
      {
         try
         {
            if (stmt != null)
               stmt.close();
         }
         catch (Throwable ignore)
         {
         }
         try
         {
            if (c != null)
               c.close();
         }
         catch (Throwable ignore)
         {
         }
         tms.endTX();

         // Restore the interrupted state of the thread
         if (threadWasInterrupted)
            Thread.currentThread().interrupt();
      }
   }

   public void saveToStorage(MessageReference messageRef, SpyMessage message) throws JMSException
   {
      // Ignore save operations for persistent messages sent to persistent queues
      // The queues handle the persistence
      if (messageRef.isPersistent())
         return;

      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("Saving message to storage " + messageRef);

      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      boolean threadWasInterrupted = Thread.interrupted();
      try
      {

         c = this.getConnection();
         add(c, messageRef.getPersistentKey(), message, null, "T");
         messageRef.setStored(MessageReference.STORED);

         if (trace)
            log.trace("Saved message to storage " + messageRef);
      }
      catch (IOException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not store message: " + messageRef, e);
      }
      catch (SQLException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not store message: " + messageRef, e);
      }
      finally
      {
         try
         {
            if (c != null)
               c.close();
         }
         catch (Throwable ignore)
         {
         }
         tms.endTX();

         // Restore the interrupted state of the thread
         if (threadWasInterrupted)
            Thread.currentThread().interrupt();
      }
   }

   /**
    * Gets a connection from the datasource, retrying as needed.  This was
    * implemented because in some minimal configurations (i.e. little logging
    * and few services) the database wasn't ready when we tried to get a
    * connection.  We, therefore, implement a retry loop wich is controled
    * by the ConnectionRetryAttempts attribute.  Submitted by terry@amicas.com
    *
    * @return the connection
    * @exception SQLException if an error occurs.
    */
   protected Connection getConnection() throws SQLException
   {
      int attempts = this.connectionRetryAttempts;
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
            return datasource.getConnection();
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

   /////////////////////////////////////////////////////////////////////////////////
   //
   // JMX Interface 
   //
   /////////////////////////////////////////////////////////////////////////////////
   
   /** The object name of the DataSource */
   protected ObjectName connectionManagerName;
   
   /** The SQL properties */
   protected Properties sqlProperties = new Properties();

   public void startService() throws Exception
   {
      UPDATE_MARKED_MESSAGES = sqlProperties.getProperty("UPDATE_MARKED_MESSAGES", UPDATE_MARKED_MESSAGES);
      UPDATE_MARKED_MESSAGES_XARECOVERY = 
         sqlProperties.getProperty("UPDATE_MARKED_MESSAGES_XARECOVERY", UPDATE_MARKED_MESSAGES_XARECOVERY);
      UPDATE_MARKED_MESSAGES_WITH_TX =
         sqlProperties.getProperty("UPDATE_MARKED_MESSAGES_WITH_TX", UPDATE_MARKED_MESSAGES_WITH_TX);
      DELETE_MARKED_MESSAGES_WITH_TX =
         sqlProperties.getProperty("DELETE_MARKED_MESSAGES_WITH_TX", DELETE_MARKED_MESSAGES_WITH_TX);
      DELETE_MARKED_MESSAGES_WITH_TX_XARECOVERY =
         sqlProperties.getProperty("DELETE_MARKED_MESSAGES_WITH_TX_XARECOVERY", DELETE_MARKED_MESSAGES_WITH_TX_XARECOVERY);
      DELETE_TX = sqlProperties.getProperty("DELETE_TX", DELETE_TX);
      DELETE_MARKED_MESSAGES = sqlProperties.getProperty("DELETE_MARKED_MESSAGES", DELETE_MARKED_MESSAGES);
      DELETE_TEMPORARY_MESSAGES = sqlProperties.getProperty("DELETE_TEMPORARY_MESSAGES", DELETE_TEMPORARY_MESSAGES);
      INSERT_TX = sqlProperties.getProperty("INSERT_TX", INSERT_TX);
      INSERT_TX_XARECOVERY = sqlProperties.getProperty("INSERT_TX_XARECOVERY", INSERT_TX_XARECOVERY);
      DELETE_ALL_TX = sqlProperties.getProperty("DELETE_ALL_TX", DELETE_ALL_TX);
      DELETE_ALL_TX_XARECOVERY = sqlProperties.getProperty("DELETE_ALL_TX_XARECOVERY", DELETE_ALL_TX_XARECOVERY);
      SELECT_ALL_TX_XARECOVERY = sqlProperties.getProperty("SELECT_ALL_TX_XARECOVERY", SELECT_ALL_TX_XARECOVERY);
      SELECT_MAX_TX = sqlProperties.getProperty("SELECT_MAX_TX", SELECT_MAX_TX);
      SELECT_MESSAGES_IN_DEST = sqlProperties.getProperty("SELECT_MESSAGES_IN_DEST", SELECT_MESSAGES_IN_DEST);
      SELECT_MESSAGES_IN_DEST_XARECOVERY = sqlProperties.getProperty("SELECT_MESSAGES_IN_DEST_XARECOVERY", SELECT_MESSAGES_IN_DEST_XARECOVERY);
      SELECT_MESSAGE_KEYS_IN_DEST = sqlProperties.getProperty("SELECT_MESSAGE_KEYS_IN_DEST", SELECT_MESSAGE_KEYS_IN_DEST);
      SELECT_MESSAGE = sqlProperties.getProperty("SELECT_MESSAGE", SELECT_MESSAGE);
      SELECT_MESSAGE_XARECOVERY = sqlProperties.getProperty("SELECT_MESSAGE_XARECOVERY", SELECT_MESSAGE_XARECOVERY);
      INSERT_MESSAGE = sqlProperties.getProperty("INSERT_MESSAGE", INSERT_MESSAGE);
      MARK_MESSAGE = sqlProperties.getProperty("MARK_MESSAGE", MARK_MESSAGE);
      DELETE_MESSAGE = sqlProperties.getProperty("DELETE_MESSAGE", DELETE_MESSAGE);
      UPDATE_MESSAGE = sqlProperties.getProperty("UPDATE_MESSAGE", UPDATE_MESSAGE);
      CREATE_MESSAGE_TABLE = sqlProperties.getProperty("CREATE_MESSAGE_TABLE", CREATE_MESSAGE_TABLE);
      CREATE_IDX_MESSAGE_TXOP_TXID = sqlProperties.getProperty("CREATE_IDX_MESSAGE_TXOP_TXID", CREATE_IDX_MESSAGE_TXOP_TXID);
      CREATE_IDX_MESSAGE_DESTINATION = sqlProperties.getProperty("CREATE_IDX_MESSAGE_DESTINATION", CREATE_IDX_MESSAGE_DESTINATION);
      CREATE_TX_TABLE = sqlProperties.getProperty("CREATE_TX_TABLE", CREATE_TX_TABLE);
      CREATE_TX_TABLE_XARECOVERY = sqlProperties.getProperty("CREATE_TX_TABLE_XARECOVERY", CREATE_TX_TABLE_XARECOVERY);
      createTables = sqlProperties.getProperty("CREATE_TABLES_ON_STARTUP", "true").equalsIgnoreCase("true");
      String s = sqlProperties.getProperty("BLOB_TYPE", "OBJECT_BLOB");

      if (s.equals("OBJECT_BLOB"))
      {
         blobType = OBJECT_BLOB;
      }
      else if (s.equals("BYTES_BLOB"))
      {
         blobType = BYTES_BLOB;
      }
      else if (s.equals("BINARYSTREAM_BLOB"))
      {
         blobType = BINARYSTREAM_BLOB;
      }
      else if (s.equals("BLOB_BLOB"))
      {
         blobType = BLOB_BLOB;
      }


      // initialize tm and datasource
      initializeFields();

      log.debug("Creating Schema");
      try
      {
         createSchema();
      }
      catch (Exception e)
      {
         log.warn("Error creating schema", e);
      }
      
      log.debug("Resolving uncommited TXS");
      Throwable error = null;
      for (int i = 0; i <= recoveryRetries; ++i)
      {
         try
         {
            resolveAllUncommitedTXs();
            
            // done
            break;
         }
         catch (Throwable t)
         {
            if (i < recoveryRetries)
               log.warn("Error resolving transactions retries=" + i + " of " + recoveryRetries, t);
            else
               error = t;
         }
      }
      
      if (error != null)
         SpyJMSException.rethrowAsJMSException("Unable to resolve transactions retries=" + recoveryRetries, error);
   }

   protected void initializeFields()
           throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, NamingException
   {
      //Find the ConnectionFactoryLoader MBean so we can find the datasource
      String dsName = (String) getServer().getAttribute(connectionManagerName, "BindName");
      //Get an InitialContext

      InitialContext ctx = new InitialContext();
      datasource = (DataSource) ctx.lookup(dsName);

      //Get the Transaction Manager so we can control the jdbc tx
      tm = (TransactionManager) ctx.lookup(TransactionManagerService.JNDI_NAME);
   }

   public Object getInstance()
   {
      return this;
   }

   public ObjectName getMessageCache()
   {
      throw new UnsupportedOperationException("This is now set on the destination manager");
   }

   public void setMessageCache(ObjectName messageCache)
   {
      throw new UnsupportedOperationException("This is now set on the destination manager");
   }

   public ObjectName getConnectionManager()
   {
      return connectionManagerName;
   }

   public void setConnectionManager(ObjectName connectionManagerName)
   {
      this.connectionManagerName = connectionManagerName;
   }

   public MessageCache getMessageCacheInstance()
   {
      throw new UnsupportedOperationException("This is now set on the destination manager");
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
  
   public int getRecoveryTimeout()
   {
      return recoveryTimeout;
   }

   public void setRecoveryTimeout(int timeout)
   {
      this.recoveryTimeout = timeout;
   }
   
   public int getRecoveryRetries()
   {
      return recoveryRetries;
   }

   public void setRecoveryRetries(int retries)
   {
      this.recoveryRetries = retries;
   }

   public int getRecoverMessagesChunk()
   {
      return recoverMessagesChunk;
   }

   public void setRecoverMessagesChunk(int recoverMessagesChunk)
   {
      if (recoverMessagesChunk != 0 && recoverMessagesChunk != 1)
      {
         log.warn("Only the values 0 and 1 are currently support for chunk size, using chunk size=1");
         recoverMessagesChunk = 1;
      }
      this.recoverMessagesChunk = recoverMessagesChunk;
   }

   public boolean isXARecovery()
   {
      return xaRecovery;
   }

   public void setXARecovery(boolean xaRecovery)
   {
      this.xaRecovery = xaRecovery;
   }

   public int getStatementRetries()
   {
      return statementRetries;
   }

   public void setStatementRetries(int statementRetries)
   {
      if (statementRetries < 0)
         statementRetries = 0;
      this.statementRetries = statementRetries;
   }
}
