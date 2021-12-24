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
package org.jboss.tm.usertx.client;


import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.Iterator;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;


/**
 *  The client-side UserTransaction implementation for clients
 *  operating in the same VM as the server.
 *  This will delegate all UserTransaction calls to the
 *  <code>TransactionManager</code> of the server.
 *
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 57209 $
 */
public class ServerVMClientUserTransaction
   implements UserTransaction
{
   // Static --------------------------------------------------------

   /**
    *  Our singleton instance.
    */
   private final static ServerVMClientUserTransaction singleton = new ServerVMClientUserTransaction();


   /**
    *  The <code>TransactionManagerz</code> we delegate to.
    */
   private final TransactionManager tm;


   private final Collection listeners = new ArrayList();

   /**
    *  Return a reference to the singleton instance.
    */
   public static ServerVMClientUserTransaction getSingleton()
   {
      return singleton;
   }


   // Constructors --------------------------------------------------

   /**
    *  Create a new instance.
    */
   private ServerVMClientUserTransaction()
   {
      // Lookup the local TM
      TransactionManager local = null;
      try {
         local = (TransactionManager)new InitialContext().lookup("java:/TransactionManager");

      } catch (NamingException ex) 
      {
         //throw new RuntimeException("TransactionManager not found: " + ex);
      }
      tm = local;
   }
   //public constructor for TESTING ONLY
   public ServerVMClientUserTransaction(final TransactionManager tm)
   {
      this.tm = tm;
   }

   // Public --------------------------------------------------------

   //Registration for TransactionStartedListeners.

   public void registerTxStartedListener(UserTransactionStartedListener txStartedListener)
   {
      listeners.add(txStartedListener);
   }

   public void unregisterTxStartedListener(UserTransactionStartedListener txStartedListener)
   {
      listeners.remove(txStartedListener);
   }

   //
   // implements interface UserTransaction
   //

   public void begin()
      throws NotSupportedException, SystemException
   {
      tm.begin();
      for (Iterator i = listeners.iterator(); i.hasNext(); )
      {
         ((UserTransactionStartedListener)i.next()).userTransactionStarted();
      } // end of for ()
      
   }

   public void commit()
      throws RollbackException,
             HeuristicMixedException,
             HeuristicRollbackException,
             SecurityException,
             IllegalStateException,
             SystemException
   {
      tm.commit();
   }

   public void rollback()
      throws SecurityException,
             IllegalStateException,
             SystemException
   {
      tm.rollback();
   }

   public void setRollbackOnly()
      throws IllegalStateException,
             SystemException
   {
      tm.setRollbackOnly();
   }

   public int getStatus()
      throws SystemException
   {
      return tm.getStatus();
   }

   public void setTransactionTimeout(int seconds)
      throws SystemException
   {
      tm.setTransactionTimeout(seconds);
   }

   public interface UserTransactionStartedListener extends EventListener 
   {
      void userTransactionStarted() throws SystemException;
   }
                                                       

}
