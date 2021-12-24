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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.jms.JMSException;

import org.jboss.mq.SpyJMSException;

/**
 * MSSQLPersistenceManager.<p>
 *
 * Based on http://jira.jboss.com/jira/browse/JBAS-2369
 *
 * @author <a href="luc.texier@jboss.com">Luc Texier</a>
 * @version $Revision: 61855 $
 */
public class MSSQLPersistenceManager extends PersistenceManager
{

    protected String CREATE_IDX_MESSAGE_MESSAGEID_DESTINATION = "CREATE UNIQUE CLUSTERED INDEX JMS_MESSAGES_IDX ON JMS_MESSAGES (MESSAGEID, DESTINATION)";

   /**
    * Create a new MSSQLPersistenceManager.
    *
    * @throws JMSException for any error
    */
   public MSSQLPersistenceManager() throws JMSException
   {
   }


   synchronized protected void createSchema() throws JMSException
   {
      Connection c = null;
      PreparedStatement stmt = null;
      boolean threadWasInterrupted = Thread.interrupted();

      try
      {
          innerCreateSchema(c, stmt);

      }
      catch (SQLException e)
      {
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

         // Restore the interrupted state of the thread
         if (threadWasInterrupted)
            Thread.currentThread().interrupt();
      }
   }


    protected void innerCreateSchema(Connection c, PreparedStatement stmt) throws SQLException
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
              try
              {
                 stmt = c.prepareStatement(CREATE_IDX_MESSAGE_MESSAGEID_DESTINATION);
                 stmt.executeUpdate();
              }
              catch (SQLException e)
              {
                 log.debug("Could not create index with SQL: " + CREATE_IDX_MESSAGE_MESSAGEID_DESTINATION, e);
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

          try
          {
             stmt = c.prepareStatement(CREATE_TX_TABLE);
             stmt.executeUpdate();
          }
          catch (SQLException e)
          {
             log.debug("Could not create table with SQL: " + CREATE_TX_TABLE, e);
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


   public void startService() throws Exception
   {
      CREATE_IDX_MESSAGE_MESSAGEID_DESTINATION = sqlProperties.getProperty("CREATE_IDX_MESSAGE_MESSAGEID_DESTINATION", CREATE_IDX_MESSAGE_MESSAGEID_DESTINATION);

      super.startService();
   }


}
