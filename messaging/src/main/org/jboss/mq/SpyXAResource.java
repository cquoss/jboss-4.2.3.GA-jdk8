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

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;

/**
 * This class implements the XAResource interface for used with an XASession.
 * 
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57198 $
 */
public class SpyXAResource implements XAResource
{
   /** The log */
   private static final Logger log = Logger.getLogger(SpyXAResource.class);
   
   /** Whether trace is enabled */
   private static boolean trace = log.isTraceEnabled();

   /** The session */
   SpySession session;

   /**
    * Create a new SpyXAResource
    *
    * @param session the session
    */
   SpyXAResource(SpySession session)
   {
      trace = log.isTraceEnabled();
      
      this.session = session;
      
      if (trace)
         log.trace("Created " + this);
   }

   public boolean setTransactionTimeout(int arg1) throws XAException
   {
      return false;
   }

   public int getTransactionTimeout() throws XAException
   {
      return 0;
   }

   public boolean isSameRM(XAResource arg1) throws XAException
   {
      if (!(arg1 instanceof SpyXAResource))
         return false;
      return ((SpyXAResource) arg1).session.connection.spyXAResourceManager == session.connection.spyXAResourceManager;
   }

   public void commit(Xid xid, boolean onePhase) throws XAException
   {
      if (trace)
         log.trace("Commit xid=" + xid + ", onePhase=" + onePhase + " " + this);

      xid = convertXid(xid);
      try
      {
         session.connection.spyXAResourceManager.commit(xid, onePhase);
      }
      catch (Throwable t)
      {
         throw SpyXAException.getAsXAException("Resource manager error during commit", t);
      }
   }

   public void end(Xid xid, int flags) throws XAException
   {
      if (trace)
         log.trace("End xid=" + xid + ", flags=" + flags + " " +this);

      xid = convertXid(xid);
      synchronized (session.runLock)
      {

         switch (flags)
         {
            case TMSUSPEND :
               session.unsetCurrentTransactionId(xid);
               session.connection.spyXAResourceManager.suspendTx(xid);
               break;
            case TMFAIL :
               session.unsetCurrentTransactionId(xid);
               session.connection.spyXAResourceManager.endTx(xid, false);
               break;
            case TMSUCCESS :
               session.unsetCurrentTransactionId(xid);
               session.connection.spyXAResourceManager.endTx(xid, true);
               break;
         }
      }
   }

   public void forget(Xid xid) throws XAException
   {
      if (trace)
         log.trace("Forget xid=" + xid + " " + this);
      
      xid = convertXid(xid);
      try
      {
         session.connection.spyXAResourceManager.forget(xid);
      }
      catch (Throwable t)
      {
         throw SpyXAException.getAsXAException("Resource manager error during forget", t);
      }
   }

   public int prepare(Xid xid) throws XAException
   {
      if (trace)
         log.trace("Prepare xid=" + xid + " " + this);

      xid = convertXid(xid);
      try
      {
         return session.connection.spyXAResourceManager.prepare(xid);
      }
      catch (Throwable t)
      {
         throw SpyXAException.getAsXAException("Resource manager error during prepare", t);
      }
   }

   public Xid[] recover(int arg1) throws XAException
   {
      if (trace)
         log.trace("Recover arg1=" + arg1 + " " + this);

      try
      {
         return session.connection.spyXAResourceManager.recover(arg1);
      }
      catch (Throwable t)
      {
         throw SpyXAException.getAsXAException("Resource manager error during recover", t);
      }
   }

   public void rollback(Xid xid) throws XAException
   {
      if (trace)
         log.trace("Rollback xid=" + xid + " " + this);

      xid = convertXid(xid);
      try
      {
         session.connection.spyXAResourceManager.rollback(xid);
      }
      catch (Throwable t)
      {
         throw SpyXAException.getAsXAException("Resource manager error during rollback", t);
      }
   }

   public void start(Xid xid, int flags) throws XAException
   {
      if (trace)
         log.trace("Start xid=" + xid + ", flags=" + flags + " " + this);

      xid = convertXid(xid);

      boolean convertTx = false;
      if (session.getCurrentTransactionId() != null)
      {
         if (flags == TMNOFLAGS && session.getCurrentTransactionId() instanceof Long)
         {
            convertTx = true;
         }
         else
         {
            XAException e = new XAException("Attempt to enlist in " + xid + " with TMNOFLAGS when already enlisted " + session.getCurrentTransactionId());
            e.errorCode = XAException.XAER_OUTSIDE; // REVIEW: Is this the correct error code?
            throw e;
         }
      }

      synchronized (session.runLock)
      {

         switch (flags)
         {
            case TMNOFLAGS :
               if (convertTx)
               {
                  // it was an anonymous TX, TM is now taking control over it.
                  // convert it over to a normal XID tansaction.
                  session.setCurrentTransactionId(session.connection.spyXAResourceManager
                        .convertTx((Long) session.getCurrentTransactionId(), xid));
               }
               else
               {
                  session.setCurrentTransactionId(session.connection.spyXAResourceManager.startTx(xid));
               }
               break;
            case TMJOIN :
               session.setCurrentTransactionId(session.connection.spyXAResourceManager.joinTx(xid));
               break;
            case TMRESUME :
               session.setCurrentTransactionId(session.connection.spyXAResourceManager.resumeTx(xid));
               break;
         }
         session.runLock.notify();
      }
   }
   
   public String toString()
   {
      return "SpyXAResource[session=" + session + ']';
   }
   
   /**
    * Convert the external xid to our internal one
    * 
    * @param xid the xid
    * @return our internal xid
    * @throws XAException for a null xid
    */
   protected Xid convertXid(Xid xid) throws XAException
   {
      if (xid == null)
      {
         XAException e = new XAException("Null xid");
         e.errorCode = XAException.XAER_NOTA;
         throw e;
      }
      
      if (xid instanceof JBossMQXid)
         return xid;
      
      return new JBossMQXid(xid);
   }
}