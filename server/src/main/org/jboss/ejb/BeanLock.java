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
package org.jboss.ejb;


import javax.transaction.Transaction;

import org.jboss.invocation.Invocation;

/**
 * BeanLock interface
 *
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @author <a href="marc.fleury@jboss.org">Marc Fleury</a>
 *
 * @version $Revision: 57209 $
 *
 * <p><b>Revisions:</b><br>
*  <p><b>2001/07/29: marcf</b>
*  <ol>
*   <li>Initial revision
*  </ol>
*  <p><b>20010802: marcf</b>
*  <ol>
*   <li>Moved to a pluggable framework for the locking policies
*   <li>you specify in jboss.xml what locking-policy you want, eg. pessimistic/optimistic
*   <li>The BeanLock is now an interface and implementations can be found under ejb/plugins/lock
*  </ol>
*/
public interface BeanLock
{
   /**
    *  Get the bean instance cache id for the bean we are locking for.
    *
    *  @return The cache key for the bean instance we are locking for.
    */
   public Object getId();

   /**
    *  Set the bean instance cache id for the bean we are locking for.
    *
    *  @param id The cache key for the bean instance we are locking for.
    */
   public void setId(Object id);

   /**
    *  Change long we should wait for a lock.
    */
   public void setTimeout(int timeout);

   /**
    *  set the ejb container of this lock.
    */
   public void setContainer(Container container);
   /**
    *  Obtain exclusive access to this lock instance.
    */
   public void sync();

   /**
    *  Release exclusive access to this lock instance.
    */
   public void releaseSync();
	
   /**
    *  This method implements the actual logic of the lock.
    *  In the case of an EJB lock it must at least implement
    *  the serialization of calls 
    *
    *  @param mi The method invocation that needs a lock.
    */
   public void schedule(Invocation mi) 
      throws Exception;
		
   /**
    *  Set the transaction currently associated with this lock.
    *  The current transaction is associated by the schedule call.
    *
    *  @param tx The transaction to associate with this lock.
    */
   public void setTransaction(Transaction tx);

   /**
    *  Get the transaction currently associated with this lock.
    *
    *  @return The transaction currently associated with this lock,
    *          or <code>null</code> if no transaction is currently
    *          associated with this lock.
    */
   public Transaction getTransaction();

   /**
    *  Informs the lock that the given transaction has ended.
    *
    *  @param tx The transaction that has ended.
    */
   public void endTransaction(Transaction tx);
	
   /**
    *  Signifies to the lock that the transaction will not Synchronize
    *  (Tx demarcation not seen).
    *  <p>
    *  OSH: This method does not seem to be called from anywhere.
    *  What is it meant for? To be called on a timeout before the
    *  transaction has terminated?
    */
   public void wontSynchronize(Transaction tx);

   /**
    *  Callback to the BeanLock to inform it that a method invocation has ended.
    *  A common use of this method is to release a method lock.
    */
   public void endInvocation(Invocation mi);
 
   /**
    *  Increment the reference count of this lock.
    */
   public void addRef();

   /**
    *  Decrement the reference count of this lock.
    */
   public void removeRef();

   /**
    *  Get the current reference count of this lock.
    *
    *  @return The current reference count.
    */
   public int getRefs();
}
