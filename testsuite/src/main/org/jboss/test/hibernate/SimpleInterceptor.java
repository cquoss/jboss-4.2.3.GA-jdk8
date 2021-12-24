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
package org.jboss.test.hibernate;

import org.hibernate.Interceptor;
import org.hibernate.CallbackException;
import org.hibernate.EntityMode;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Implementation of SimpleInterceptor.
 *
 * @author Steve Ebersole
 */
public class SimpleInterceptor implements Interceptor
{
   public boolean onLoad(Object o, Serializable serializable, Object[] objects, String[] strings, Type[] types) throws CallbackException
   {
      return false;
   }

   public boolean onFlushDirty(Object o, Serializable serializable, Object[] objects, Object[] objects1, String[] strings, Type[] types) throws CallbackException
   {
      return false;
   }

   public boolean onSave(Object o, Serializable serializable, Object[] objects, String[] strings, Type[] types) throws CallbackException
   {
      return false;
   }

   public void onDelete(Object o, Serializable serializable, Object[] objects, String[] strings, Type[] types) throws CallbackException
   {
   }

   public void onCollectionRecreate(Object object, Serializable serializable) throws CallbackException
   {
   }

   public void onCollectionRemove(Object object, Serializable serializable) throws CallbackException
   {
   }

   public void onCollectionUpdate(Object object, Serializable serializable) throws CallbackException
   {
   }

   public void preFlush(Iterator iterator) throws CallbackException
   {
   }

   public void postFlush(Iterator iterator) throws CallbackException
   {
   }

   public Boolean isTransient(Object o)
   {
      return null;
   }

   public int[] findDirty(Object o, Serializable serializable, Object[] objects, Object[] objects1, String[] strings, Type[] types)
   {
      return null;
   }

   public Object instantiate(String name, EntityMode entityMode, Serializable serializable) throws CallbackException
   {
      return null;
   }

   public String getEntityName(Object o) throws CallbackException
   {
      return null;
   }

   public Object getEntity(String name, Serializable serializable) throws CallbackException
   {
      return null;
   }

   public void afterTransactionBegin(Transaction transaction)
   {
   }

   public void beforeTransactionCompletion(Transaction transaction)
   {
   }

   public void afterTransactionCompletion(Transaction transaction)
   {
   }

   public String onPrepareStatement(String string)
   {
      return string;
   }
}
