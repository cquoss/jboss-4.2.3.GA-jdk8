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
package org.jboss.tm.iiop.client;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.transaction.UserTransaction;

import org.omg.CORBA.BAD_PARAM;

import org.jboss.logging.Logger;
import org.jboss.tm.iiop.TransactionFactoryExtHelper;
import org.jboss.tm.usertx.client.ServerVMClientUserTransaction;
     
/**
 *  This is an object factory for producing client-side UserTransactions
 *  for standalone RMI/IIOP clients.
 *      
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 *  @version $Revision: 63217 $
 */
public class IIOPClientUserTransactionObjectFactory
      implements ObjectFactory
{
   private static final Logger log = 
      Logger.getLogger(IIOPClientUserTransactionObjectFactory.class);
   private static final boolean traceEnabled = log.isTraceEnabled();

   /**
    *  The <code>UserTransaction</code> this factory will return.
    *  This is evaluated lazily in {@link #getUserTransaction()}.
    */
   private static UserTransaction userTransaction = null;

   /**
    *  Get the <code>UserTransaction</code> this factory will return.
    *  This may return a cached value from a previous call.
    */
   private static UserTransaction getUserTransaction()
   {
      if (userTransaction == null) 
      {
         // See if we have a local TM
         try 
         {
            new InitialContext().lookup("java:/TransactionManager");
            
            // We execute in the server.
            userTransaction = ServerVMClientUserTransaction.getSingleton();
         } 
         catch (NamingException ex) 
         {
            // We execute in a stand-alone client.
            userTransaction = IIOPClientUserTransaction.getSingleton();
         }
      }
      return userTransaction;
   }
   
   public Object getObjectInstance(Object obj, Name name,
                                   Context nameCtx, Hashtable environment)
      throws Exception
   {

      if (traceEnabled) 
         log.trace("getObjectInstance: obj=" + obj +
                   "\n                  name=" + name +
                   "\n               nameCtx= " + nameCtx);
      
      if (!name.toString().equals("UserTransaction"))
         return null;
      if (!(obj instanceof org.omg.CORBA.Object))
         return null;
      try 
      {
         TransactionFactoryExtHelper.narrow((org.omg.CORBA.Object)obj);
      }
      catch (BAD_PARAM e)
      {
         return null;
      }
      return getUserTransaction();
   }
}

