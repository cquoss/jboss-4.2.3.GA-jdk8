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
package org.jboss.mq;

import java.io.Serializable;
import java.util.Map;
import java.util.ArrayList;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;

import javax.jms.JMSException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;

/**
 * This class implements the ResourceManager used for the XAResources used int
 * JBossMQ.
 *
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class SpyXAResourceManager implements Serializable
{
   /** The serialVersionUID */
   static final long serialVersionUID = -6268132972627753772L;
   /** The log */
   private static final Logger log = Logger.getLogger(SpyXAResourceManager.class);
   /** Whether trace is enabled */
   private static boolean trace = log.isTraceEnabled();

   //Valid tx states:
   private final static byte TX_OPEN = 0;
   private final static byte TX_ENDED = 1;
   private final static byte TX_PREPARED = 3;
   private final static byte TX_COMMITED = 4;
   private final static byte TX_ROLLEDBACK = 5;
   private final static byte TX_READONLY = 6;

   /** The connection */
   private Connection connection;
   /** The transactions */
   private Map transactions = new ConcurrentReaderHashMap();
   /** The next xid */
   private long nextInternalXid = Long.MIN_VALUE;
   
   /**
    * Create a new SpyXAResourceManager
    *
    * @param conn the connection
    */
   public SpyXAResourceManager(Connection conn)
   {
      super();
      connection = conn;
   }

   /**
    * Acknowledge a message
    *
    * @param xid the xid
    * @param msg the message
    * @throws JMSException for any error
    */
   public void ackMessage(Object xid, SpyMessage msg) throws JMSException
   {
      if (xid == null)
      {
         if (trace)
            log.trace("No Xid, acking message " + msg.header.jmsMessageID);
         msg.doAcknowledge();
         return;
      }

      if (trace)
         log.trace("Adding acked message xid=" + xid + " " + msg.header.jmsMessageID);

      TXState state = (TXState) transactions.get(xid);
      if (state == null)
         throw new JMSException("Invalid transaction id.");
      AcknowledgementRequest item = msg.getAcknowledgementRequest(true);
      state.ackedMessages.add(item);
   }

   public void addMessage(Object xid, SpyMessage msg) throws JMSException
   {
      if (xid == null)
      {
         if (trace)
            log.trace("No Xid, sending message to server " + msg.header.jmsMessageID);
         connection.sendToServer(msg);
         return;
      }
      
      if (trace)
         log.trace("Adding message xid=" + xid + ", message=" + msg.header.jmsMessageID);

      TXState state = (TXState) transactions.get(xid);
      if (trace)
         log.trace("TXState=" + state);

      if (state == null)
         throw new JMSException("Invalid transaction id.");

      state.sentMessages.add(msg);
   }

   public void commit(Object xid, boolean onePhase) throws XAException, JMSException
   {
      if (trace)
         log.trace("Commiting xid=" + xid + ", onePhase=" + onePhase);

      TXState state = (TXState) transactions.remove(xid);
      if (state == null)
      {
         XAException e = new XAException("Unknown transaction during commit " + xid);
         e.errorCode = XAException.XAER_NOTA;
         throw e;
      }

      if (onePhase)
      {
         if (state.isReadOnly())
         {
            if (trace)
               log.trace("Nothing to do for " + xid);
         }
         
         TransactionRequest transaction = new TransactionRequest();
         transaction.requestType = TransactionRequest.ONE_PHASE_COMMIT_REQUEST;
         transaction.xid = null;
         if (state.sentMessages.size() != 0)
         {
            SpyMessage job[] = new SpyMessage[state.sentMessages.size()];
            job = (SpyMessage[]) state.sentMessages.toArray(job);
            transaction.messages = job;
         }
         if (state.ackedMessages.size() != 0)
         {
            AcknowledgementRequest job[] = new AcknowledgementRequest[state.ackedMessages.size()];
            job = (AcknowledgementRequest[]) state.ackedMessages.toArray(job);
            transaction.acks = job;
         }
         connection.send(transaction);
      }
      else
      {
         if (state.txState == TX_READONLY)
         {
            if (trace)
               log.trace("Nothing to do for " + xid);
            return;
         }
         if (state.txState != TX_PREPARED)
         {
            XAException e = new XAException("Cannot complete 2 phase commit, the transaction has not been prepared " + xid);
            e.errorCode = XAException.XAER_PROTO;
            throw e;
         }
         TransactionRequest transaction = new TransactionRequest();
         transaction.xid = xid;
         transaction.requestType = TransactionRequest.TWO_PHASE_COMMIT_COMMIT_REQUEST;
         connection.send(transaction);
      }
      state.txState = TX_COMMITED;
   }

   public void endTx(Object xid, boolean success) throws XAException
   {
      if (trace)
         log.trace("Ending xid=" + xid + ", success=" + success);

      TXState state = (TXState) transactions.get(xid);
      if (state == null)
      {
         XAException e = new XAException("Unknown transaction during delist " + xid);
         e.errorCode = XAException.XAER_NOTA;
         throw e;
      }
      state.txState = TX_ENDED;
   }

   public Object joinTx(Xid xid) throws XAException
   {
      if (trace)
         log.trace("Joining tx xid=" + xid);

      if (!transactions.containsKey(xid))
      {
         XAException e = new XAException("Unknown transaction during join " + xid);
         e.errorCode = XAException.XAER_NOTA;
         throw e;
      }
      return xid;
   }

   public int prepare(Object xid) throws XAException, JMSException
   {
      if (trace)
         log.trace("Preparing xid=" + xid);

      TXState state = (TXState) transactions.get(xid);
      if (state == null)
      {
         XAException e = new XAException("Unknown transaction during prepare " + xid);
         e.errorCode = XAException.XAER_NOTA;
         throw e;
      }

      if (state.isReadOnly())
      {
         if (trace)
            log.trace("Vote read only for " + xid);
         state.txState = TX_READONLY;
         return XAResource.XA_RDONLY;
      }
      
      TransactionRequest transaction = new TransactionRequest();
      transaction.requestType = TransactionRequest.TWO_PHASE_COMMIT_PREPARE_REQUEST;
      transaction.xid = xid;
      if (state.sentMessages.size() != 0)
      {
         SpyMessage job[] = new SpyMessage[state.sentMessages.size()];
         job = (SpyMessage[]) state.sentMessages.toArray(job);
         transaction.messages = job;
      }
      if (state.ackedMessages.size() != 0)
      {
         AcknowledgementRequest job[] = new AcknowledgementRequest[state.ackedMessages.size()];
         job = (AcknowledgementRequest[]) state.ackedMessages.toArray(job);
         transaction.acks = job;
      }
      connection.send(transaction);
      state.txState = TX_PREPARED;
      return XAResource.XA_OK;
   }

   public Object resumeTx(Xid xid) throws XAException
   {
      if (trace)
         log.trace("Resuming tx xid=" + xid);

      if (!transactions.containsKey(xid))
      {
         XAException e = new XAException("Unknown transaction during resume " + xid);
         e.errorCode = XAException.XAER_NOTA;
         throw e;
      }
      return xid;
   }

   public void rollback(Object xid) throws XAException, JMSException
   {
      if (trace)
         log.trace("Rolling back xid=" + xid);

      TXState state = (TXState) transactions.remove(xid);
      if (state == null)
      {
         XAException e = new XAException("Unknown transaction during rollback " + xid);
         e.errorCode = XAException.XAER_NOTA;
         throw e;
      }
      if (state.txState == TX_READONLY)
      {
         if (trace)
            log.trace("Nothing to do for " + xid);
         return;
      }
      if (state.txState != TX_PREPARED)
      {
         TransactionRequest transaction = new TransactionRequest();
         transaction.requestType = TransactionRequest.ONE_PHASE_COMMIT_REQUEST;
         transaction.xid = null;
         if (state.ackedMessages.size() != 0)
         {
            AcknowledgementRequest job[] = new AcknowledgementRequest[state.ackedMessages.size()];
            job = (AcknowledgementRequest[]) state.ackedMessages.toArray(job);
            transaction.acks = job;
            //Neg Acknowlege all consumed messages
            for (int i = 0; i < transaction.acks.length; i++)
            {
               transaction.acks[i].isAck = false;
            }
         }
         connection.send(transaction);
      }
      else
      {
         TransactionRequest transaction = new TransactionRequest();
         transaction.xid = xid;
         transaction.requestType = TransactionRequest.TWO_PHASE_COMMIT_ROLLBACK_REQUEST;
         connection.send(transaction);
      }
      state.txState = TX_ROLLEDBACK;
   }

   public Xid[] recover(int arg) throws XAException, JMSException
   {
      if (trace)
         log.trace("Recover arg=" + arg);
      
      Xid[] xids = connection.recover(arg);

      // Make sure we have a reference to each xid
      for (int i = 0; i < xids.length; ++i)
      {
         if (transactions.containsKey(xids[i]) == false)
         {
            TXState state = new TXState();
            state.txState = TX_PREPARED;
            transactions.put(xids[i], state);
         }
      }
      return xids;
   }

   public void forget(Xid xid) throws XAException, JMSException
   {
      if (trace)
         log.trace("Forget xid=" + xid);

      TXState state = (TXState) transactions.get(xid);
      if (state == null)
         return;
      if (state.txState != TX_PREPARED)
         transactions.remove(xid);
      rollback(xid);
   }
   
   public synchronized Long getNewXid()
   {
      return new Long(nextInternalXid++);
   }

   public Object startTx()
   {
      Long newXid = getNewXid();
      transactions.put(newXid, new TXState());

      if (trace)
         log.trace("Starting tx with new xid=" + newXid);

      return newXid;
   }

   public Object startTx(Xid xid) throws XAException
   {
      if (trace)
         log.trace("Starting tx xid=" + xid);

      if (transactions.containsKey(xid))
      {
         XAException e = new XAException("Duplicate transaction id during enlist " + xid);
         e.errorCode = XAException.XAER_DUPID;
         throw e;
      }
      transactions.put(xid, new TXState());
      return xid;
   }

   public Object suspendTx(Xid xid) throws XAException
   {
      if (trace)
         log.trace("Suppending tx xid=" + xid);

      if (!transactions.containsKey(xid))
      {
         XAException e = new XAException("Unknown transaction during suspend " + xid);
         e.errorCode = XAException.XAER_NOTA;
         throw e;
      }
      return xid;
   }

   public Object convertTx(Long anonXid, Xid xid) throws XAException
   {
      if (trace)
         log.trace("Converting tx anonXid=" + anonXid + ", xid=" + xid);

      if (!transactions.containsKey(anonXid))
      {
         XAException e = new XAException("Unknown transaction during convert " + anonXid);
         e.errorCode = XAException.XAER_NOTA;
         throw e;
      }
      if (transactions.containsKey(xid))
      {
         XAException e = new XAException("Duplicate transaction during convert " + xid);
         e.errorCode = XAException.XAER_DUPID;
         throw e;
      }
      TXState s = (TXState) transactions.remove(anonXid);

      transactions.put(xid, s);
      return xid;
   }

   /**
    * The transaction state
	*/
   static class TXState
   {
      byte txState = TX_OPEN;
      ArrayList sentMessages = new ArrayList();
      ArrayList ackedMessages = new ArrayList();

      public boolean isReadOnly()
      {
         return sentMessages.size() == 0 && ackedMessages.size() == 0;
      }
      
      public String toString()
      {
         StringBuffer buffer = new StringBuffer(100);
         buffer.append("TxState txState=").append(txState);
         buffer.append(" sent=").append(sentMessages);
         buffer.append(" acks=").append(ackedMessages);
         return buffer.toString();
      }
   }
}
