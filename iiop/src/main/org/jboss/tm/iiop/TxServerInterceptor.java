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
package org.jboss.tm.iiop;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Transaction;

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.LocalObject;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.PropagationContextHelper;
import org.omg.CosTransactions.otid_t;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.TypeMismatch;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.InvalidSlot;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

import org.jboss.proxy.ejb.ForeignTransaction;
import org.jboss.tm.GlobalId;
import org.jboss.tm.TransactionPropagationContextImporter;


/**
 * This implementation of 
 * <code>org.omg.PortableInterceptor.ServerRequestInterceptor</code>
 * retrieves the transactional context from incoming IIOP requests and
 * makes it available to the servant methods that handle the requests,
 * through the static method <code>getCurrentTransaction</code).
 *
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 57194 $
 */
public class TxServerInterceptor
      extends LocalObject
      implements ServerRequestInterceptor 
{
   /** @since 4.0.1 */
   static final long serialVersionUID = 7474707114565659371L;

   // Static fields -------------------------------------------------

   private static final int txContextId = org.omg.IOP.TransactionService.value;
   private static int slotId;
   private static Codec codec;
   private static org.omg.PortableInterceptor.Current piCurrent = null;
   private static TransactionPropagationContextImporter tpcImporter = null;
   
   // Static methods ------------------------------------------------

   /**
    * Called by <code>TxServerInterceptorInitializer</code> 
    * at ORB initialization time.
    */
   static void init(int slotId, Codec codec, 
                    org.omg.PortableInterceptor.Current piCurrent)
   {
      TxServerInterceptor.slotId = slotId;
      TxServerInterceptor.codec = codec;
      TxServerInterceptor.piCurrent = piCurrent;
   }

   /**
    * Returns the transaction associated with the transaction propagation 
    * context that arrived in the current IIOP request.
    */
   public static Transaction getCurrentTransaction()
   {
      Transaction tx = null;
      if (piCurrent != null)
      {  
         // A non-null piCurrent means that a TxServerInterceptor was 
         // installed: check if there is a transaction propagation context
         try
         {
            Any any = piCurrent.get_slot(slotId);
            if (any.type().kind().value() != TCKind._tk_null) 
            {  
               // Yes, there is a TPC
               PropagationContext pc = PropagationContextHelper.extract(any);
               if (pc.current.coord == null)
                  tx = ForeignTransaction.instance;
               otid_t otid =  pc.current.otid;
               // Build a GlobalId with info taken from the TPC
               GlobalId globalId = 
                  new GlobalId(otid.formatID, otid.bqual_length, otid.tid);
               // Get transaction by GlobalId
               tx = getTPCImporter().importTransactionPropagationContext(globalId);
               // No transaction propagation (add the marker)
               if (tx == null)
                  tx = ForeignTransaction.instance;
            }
         }
         catch (InvalidSlot e)
         {
            throw new RuntimeException("Exception getting slot in " +
                                       "TxServerInterceptor: " + e);
         }
         
      }
      return tx;
   }
   
   /**
    * Get a reference to the transaction importer.
    */
   private static TransactionPropagationContextImporter getTPCImporter()
   {
      if (tpcImporter == null)
      {
         try
         {
            Context ctx = new InitialContext();
            tpcImporter = (TransactionPropagationContextImporter)ctx.lookup(
                                 "java:/TransactionPropagationContextImporter");
         }
         catch (NamingException e)
         {
            throw new RuntimeException(
                  "java:/TransactionPropagationContextImporter lookup failed", 
                  e);
         }
      }
      return tpcImporter;
   }

   // Constructor ---------------------------------------------------

   public TxServerInterceptor() 
   {
      // do nothing
   }

   // org.omg.PortableInterceptor.Interceptor operations ------------

   public String name() 
   {
      return "TxServerInterceptor";
   }
   
   public void destroy() 
   {
      // do nothing
   }
   
   // ServerRequestInterceptor operations ---------------------------

   public void receive_request_service_contexts(ServerRequestInfo ri) 
   {
      try 
      {
         ServiceContext sc = ri.get_request_service_context(txContextId);
         Any any = codec.decode_value(sc.context_data, 
                                      PropagationContextHelper.type());
         ri.set_slot(slotId, any);
      } 
      catch (BAD_PARAM e) 
      {
         // no service context with txContextId: do nothing
      } 
      catch (FormatMismatch e) 
      {
         throw new RuntimeException("Exception decoding context data in " +
                                    "TxServerInterceptor: " + e);
      }
      catch (TypeMismatch e) 
      {
         throw new RuntimeException("Exception decoding context data in " +
                                    "TxServerInterceptor: " + e);
      }
      catch (InvalidSlot e) 
      {
         throw new RuntimeException("Exception setting slot in " +
                                    "TxServerInterceptor: " + e);
      }
   }
   
   public void receive_request(ServerRequestInfo ri) 
   {
      // do nothing
   }

   public void send_reply(ServerRequestInfo ri) 
   {
      // do nothing
   }

   public void send_exception(ServerRequestInfo ri) 
   {
      // do nothing
   }

   public void send_other(ServerRequestInfo ri) 
   {
      // do nothing
   }
}
