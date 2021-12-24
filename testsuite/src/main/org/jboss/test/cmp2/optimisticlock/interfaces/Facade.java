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
package org.jboss.test.cmp2.optimisticlock.interfaces;

/**
 * Remote interface for Facade.
 */
public interface Facade
   extends javax.ejb.EJBObject
{

   public void createCmpEntity( String jndiName,Integer id,String stringGroup1,Integer integerGroup1,Double doubleGroup1,String stringGroup2,Integer integerGroup2,Double doubleGroup2 )
      throws Exception, java.rmi.RemoteException;

   public void safeRemove( String jndiName,Integer id )
      throws Exception, java.rmi.RemoteException;

   public void testNullLockedFields( String jndiName,Integer id )
      throws Exception, java.rmi.RemoteException;

   public void testKeygenStrategyPass( String jndiName,Integer id )
      throws Exception, java.rmi.RemoteException;

   public void testKeygenStrategyFail( String jndiName,Integer id )
      throws Exception, java.rmi.RemoteException;

   public void testTimestampStrategyPass( String jndiName,Integer id )
      throws Exception, java.rmi.RemoteException;

   public void testTimestampStrategyFail( String jndiName,Integer id )
      throws Exception, java.rmi.RemoteException;

   public void testVersionStrategyPass( String jndiName,Integer id )
      throws Exception, java.rmi.RemoteException;

   public void testVersionStrategyFail( String jndiName,Integer id )
      throws Exception, java.rmi.RemoteException;

   public void testGroupStrategyPass( String jndiName,Integer id )
      throws Exception, java.rmi.RemoteException;

   public void testGroupStrategyFail( String jndiName,Integer id )
      throws Exception, java.rmi.RemoteException;

   public void testReadStrategyPass( String jndiName,Integer id )
      throws Exception, java.rmi.RemoteException;

   public void testReadStrategyFail( String jndiName,Integer id )
      throws Exception, java.rmi.RemoteException;

   public void testModifiedStrategyPass( String jndiName,Integer id )
      throws Exception, java.rmi.RemoteException;

   public void testModifiedStrategyFail( String jndiName,Integer id )
      throws Exception, java.rmi.RemoteException;

   public void modifyGroup2InRequiresNew( String jndiName,Integer id )
      throws Exception, java.rmi.RemoteException;

   public void modifyGroup1InRequiresNew( String jndiName,Integer id, long sleep )
      throws Exception, java.rmi.RemoteException;

   public void testUpdateLockOnSync( String jndiName,Integer id )
      throws Exception, java.rmi.RemoteException;

   public void testExplicitVersionUpdateOnSync( String jndiName,Integer id )
      throws Exception, java.rmi.RemoteException;

}
