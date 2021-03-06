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
package org.jboss.test.banknew.interfaces;

/**
 * Remote interface for bank/TellerSession.
 */
public interface TellerSession extends javax.ejb.EJBObject
{

   public void deposit(java.lang.String pToAccountId, float pAmount)
         throws org.jboss.test.banknew.interfaces.BankException, java.rmi.RemoteException;

   public void transfer(java.lang.String pFromAccountId, java.lang.String pToAccountId, float pAmount)
         throws org.jboss.test.banknew.interfaces.BankException, java.rmi.RemoteException;

   public void withdraw(java.lang.String pFromAccountId, float pAmount)
         throws org.jboss.test.banknew.interfaces.BankException, java.rmi.RemoteException;

   public org.jboss.test.banknew.interfaces.AccountData createAccount(java.lang.String pCustomerId, int pType,
         float pInitialDeposit) throws org.jboss.test.banknew.interfaces.BankException, java.rmi.RemoteException;

   public void removeAccount(java.lang.String pAccountId) throws org.jboss.test.banknew.interfaces.BankException,
         java.rmi.RemoteException;

   public void removeAccount(java.lang.String pCustomerId, int pType)
         throws org.jboss.test.banknew.interfaces.BankException, java.rmi.RemoteException;

   public java.util.Collection getAccounts(java.lang.String pCustomerId)
         throws org.jboss.test.banknew.interfaces.BankException, java.rmi.RemoteException;

   public org.jboss.test.banknew.interfaces.AccountData getAccount(java.lang.String pCustomerId, int pType)
         throws org.jboss.test.banknew.interfaces.BankException, java.rmi.RemoteException;

   public org.jboss.test.banknew.interfaces.AccountData getAccount(java.lang.String pAccountId)
         throws org.jboss.test.banknew.interfaces.BankException, java.rmi.RemoteException;

   public org.jboss.test.banknew.interfaces.CustomerData createCustomer(java.lang.String pBankId,
         java.lang.String pName, float pInitialDeposit) throws org.jboss.test.banknew.interfaces.BankException,
         java.rmi.RemoteException;

   public void removeCustomer(java.lang.String pCustomerId) throws org.jboss.test.banknew.interfaces.BankException,
         java.rmi.RemoteException;

   public org.jboss.test.banknew.interfaces.CustomerData getCustomer(java.lang.String pCustomerId)
         throws org.jboss.test.banknew.interfaces.BankException, java.rmi.RemoteException;

   public java.util.Collection getCustomers(java.lang.String pBankId)
         throws org.jboss.test.banknew.interfaces.BankException, java.rmi.RemoteException;

}
