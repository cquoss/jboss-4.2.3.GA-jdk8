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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.jms.JMSException;

import org.jboss.mq.SpyMessage;
import org.jboss.mq.pm.Tx;

/**
 * OracleThinPersistenceManager.<p>
 * 
 * Based on information provided by Ron Teeter at TradeBeam Holdings, Inc. 
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class OracleThinPersistenceManager extends PersistenceManager
{
   /** Insert an empty blob */
   protected String INSERT_EMPTY_BLOB = "INSERT INTO JMS_MESSAGES (MESSAGEID, DESTINATION, MESSAGEBLOB, TXID, TXOP) VALUES(?,?,EMPTY_BLOB(),?,?)";
   /** Lock empty blob */
   protected String LOCK_EMPTY_BLOB = "SELECT MESSAGEID, MESSAGEBLOB FROM JMS_MESSAGES WHERE MESSAGEID = ? AND DESTINATION = ? FOR UPDATE";

   /**
    * Create a new OracleThinPersistenceManager.
    * 
    * @throws JMSException for any error
    */
   public OracleThinPersistenceManager() throws JMSException
   {
   }

   protected void add(Connection c, String queue, SpyMessage message, Tx txId, String mark) throws SQLException, IOException
   {
      PreparedStatement stmt = null;
      ResultSet rs = null;
      try
      {
         stmt = c.prepareStatement(INSERT_EMPTY_BLOB);

         stmt.setLong(1, message.header.messageId);
         stmt.setString(2, queue);

         if (txId != null)
            stmt.setLong(3, txId.longValue());
         else
            stmt.setNull(3, java.sql.Types.BIGINT);
         stmt.setString(4, mark);

         int count = stmt.executeUpdate();
         safeClose(stmt, null);
         if (count != 1)
            throw new IOException("Could not insert empty blob in the database: insert affected " + count + " rows. message=" + message);

         stmt = c.prepareStatement(LOCK_EMPTY_BLOB);
         stmt.setLong(1, message.header.messageId);
         stmt.setString(2, queue);

         rs = stmt.executeQuery();
         if (rs.next() == false)
            throw new IOException("Could not lock empty blob in the database. message=" + message);
         safeClose(stmt, rs);

         stmt = c.prepareStatement(UPDATE_MESSAGE);
         setBlob(stmt, 1, message);
         stmt.setLong(2, message.header.messageId);
         stmt.setString(3, queue);

         count = stmt.executeUpdate();
         safeClose(stmt, null);
         if (count != 1)
            throw new IOException("Could not update real blob in the database: update affected " + count + " rows. message=" + message);
      }
      finally
      {
         safeClose(stmt, rs);
      }
   }

   public void startService() throws Exception
   {
      INSERT_EMPTY_BLOB = sqlProperties.getProperty("INSERT_EMPTY_BLOB", INSERT_EMPTY_BLOB);
      LOCK_EMPTY_BLOB = sqlProperties.getProperty("LOCK_EMPTY_BLOB", LOCK_EMPTY_BLOB);
      super.startService();
   }
   
   protected void safeClose(PreparedStatement stmt, ResultSet rs)
   {
      try
      {
         if (rs != null)
         {
            rs.close();
         }
      }
      catch (SQLException ignored)
      {
         log.trace("Ignored", ignored);
      }
      try
      {
         if (stmt != null)
         {
            stmt.close();
         }
      }
      catch (SQLException ignored)
      {
         log.trace("Ignored", ignored);
      }
   }
}
