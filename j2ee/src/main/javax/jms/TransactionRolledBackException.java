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
package javax.jms;

/**
 * <P> This exception must be thrown when a 
 *     call to <CODE>Session.commit</CODE> results in a rollback of the current 
 *     transaction.
 **/

public class TransactionRolledBackException extends JMSException
{
   private static final long serialVersionUID = 9157976009672865857L;

   /** Constructs a <CODE>TransactionRolledBackException</CODE> with the 
    *  specified reason and error code.
    *
    *  @param  reason        a description of the exception
    *  @param  errorCode     a string specifying the vendor-specific
    *                        error code
    *                        
    **/
   public TransactionRolledBackException(String reason, String errorCode)
   {
      super(reason, errorCode);
   }

   /** Constructs a <CODE>TransactionRolledBackException</CODE> with the 
    *  specified reason. The error code defaults to null.
    *
    *  @param  reason        a description of the exception
    **/
   public TransactionRolledBackException(String reason)
   {
      super(reason);
   }

}
