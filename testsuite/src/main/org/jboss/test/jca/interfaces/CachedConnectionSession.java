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
package org.jboss.test.jca.interfaces;

/**
 * Remote interface for CachedConnectionSession.
 */
public interface CachedConnectionSession extends javax.ejb.EJBObject
{
   /**
    * Describe <code>createTable</code> method here.
    */
   public void createTable() throws java.rmi.RemoteException;

   /**
    * Describe <code>dropTable</code> method here.
    */
   public void dropTable() throws java.rmi.RemoteException;

   /**
    * Describe <code>insert</code> method here.
    * @param id a <code>String</code> value
    * @param value a <code>String</code> value
    */
   public void insert(long id, java.lang.String value) throws java.rmi.RemoteException;

   /**
    * Describe <code>fetch</code> method here.
    * @param id a <code>String</code> value
    */
   public java.lang.String fetch(long id) throws java.rmi.RemoteException;

   /**
    * Invoke another bean that opens a thread local connection, we close it.
    */
   public void firstTLTest() throws java.rmi.RemoteException;

   public void secondTLTest() throws java.rmi.RemoteException;

}
