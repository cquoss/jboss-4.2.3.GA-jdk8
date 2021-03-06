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
package org.jboss.test.perf.ejb;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.test.perf.interfaces.TxSession;

public class TxSessionBean implements SessionBean
{
   private SessionContext sessionContext;
   private InitialContext iniCtx;
   
   public void ejbCreate() throws CreateException
   {
   }
   
   public void ejbActivate()
   {
   }
   
   public void ejbPassivate()
   {
   }
   
   public void ejbRemove()
   {
   }

   public void setSessionContext(SessionContext context)
   {
      sessionContext = context;
      try
      {
         iniCtx = new InitialContext();
      }
      catch(NamingException e)
      {
         throw new EJBException(e);
      }
   }
   
  /*
   * This method is defined with "Required"
   */
   public String txRequired()
   {
      Transaction  tx = getDaTransaction();
      if (tx == null)
         throw new EJBException("Required sees no transaction");
      else
         return ("required sees a transaction "+tx.hashCode());
   }
   
   
  /*
   * This method is defined with "Requires_new"
   */
   public String txRequiresNew()
   {
      Object tx =getDaTransaction();
      if (tx == null)
         throw new EJBException("RequiresNew sees no transaction");
      else
         return ("requiresNew sees a transaction "+tx.hashCode());
   }
   
   /*
    * testSupports is defined with Supports
    */
   public String txSupports()
   {
      Object tx =getDaTransaction();
      if (tx == null)
         return "supports sees no transaction";
      else
         return "supports sees a transaction "+tx.hashCode();
   }

  /*
   * This method is defined with "Mandatory"
   */
   public String txMandatory()
   {
      Object tx =getDaTransaction();
      if (tx == null)
         throw new EJBException("Mandatory sees no transaction");
      else
         return ("mandatory sees a transaction "+tx.hashCode());
   }
   
   /*
    * This method is defined with "Never"
    */
   public String txNever()
   {
      Object tx =getDaTransaction();
      if (tx == null)
         return "never sees no transaction";
      else
         throw new EJBException("txNever sees a transaction");
   }
   
    /*
     * This method is defined with "TxNotSupported"
     */
   public String txNotSupported()
   {
      Object tx =getDaTransaction();
      if (tx == null)
         return "notSupported sees no transaction";
      else
         throw new EJBException("txNotSupported sees a transaction");
   }
   
  /*
   * This method is defined with "Required" and it passes it to a Supports Tx
   */
   public String requiredToSupports() throws RemoteException
   {
      String message;
      Object tx =getDaTransaction();
      if (tx == null)
         throw new EJBException("Required doesn't see a transaction");
      else
         message = "Required sees a transaction "+tx.hashCode()+ " Supports should see the same ";
      
      message = message + ((TxSession) sessionContext.getEJBObject()).txSupports();
      
      // And after invocation we should have the same transaction
      tx =getDaTransaction();
      if (tx == null)
         throw new EJBException("Required doesn't see a transaction COMING BACK");
      else
         return message + " on coming back Required sees a transaction "+tx.hashCode() ;
      
   }

    /*
     * This method is defined with "Required" and it passes it to a Mandatory Tx
     */
   public String requiredToMandatory() throws RemoteException
   {     
      String message;
      Object tx =getDaTransaction();
      if (tx == null)
         throw new EJBException("Required doesn't see a transaction");
      else
         message = "Required sees a transaction "+tx.hashCode()+ " NotSupported should see the same ";
      
      
      message = message + ((TxSession) sessionContext.getEJBObject()).txMandatory();
      
      // And after invocation we should have the same transaction
      tx =getDaTransaction();
      if (tx == null)
         throw new EJBException("Required doesn't see a transaction COMING BACK");
      else
         return message + " on coming back Required sees a transaction "+tx.hashCode() ;
   }
   
   public String requiredToRequiresNew() throws RemoteException
   {
      String message;
      Object tx =getDaTransaction();
      if (tx == null)
         throw new EJBException("Required doesn't see a transaction");
      else
         message = "Required sees a transaction "+tx.hashCode()+ " Requires new should see a new transaction ";
      
      message =  message + ((TxSession) sessionContext.getEJBObject()).txRequiresNew();
      
      // And after invocation we should have the same transaction
      tx = getDaTransaction();
      if (tx == null)
         throw new EJBException("Required doesn't see a transaction COMING BACK");
      else
         return message + " on coming back Required sees a transaction "+tx.hashCode() ;
   }

   private Transaction getDaTransaction()
   {
      Transaction t = null;
      try
      {
         TransactionManager tm = (TransactionManager) iniCtx.lookup("java:/TransactionManager");
         t = tm.getTransaction();
      }
      catch(Exception e)
      {
      }
      return t;
   }
   
}
