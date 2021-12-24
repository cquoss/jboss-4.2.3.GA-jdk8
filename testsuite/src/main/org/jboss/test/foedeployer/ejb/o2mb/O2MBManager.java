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
package org.jboss.test.foedeployer.ejb.o2mb;

/**
 * Remote interface for O2MBManager.
 */
public interface O2MBManager
   extends javax.ejb.EJBObject
{
   /**
    * Creates a company
    */
   public void createCompany( java.lang.String companyName )
      throws java.rmi.RemoteException;

   /**
    * Creates an employee
    */
   public void createEmployee( java.lang.String employeeName )
      throws java.rmi.RemoteException;

   /**
    * Returns all the companies
    */
   public java.util.Collection getEmployeesForCompany( java.lang.String companyName )
      throws java.rmi.RemoteException;

   /**
    * Returns emaployee's company
    */
   public java.lang.String getCompanyForEmployee( java.lang.String employeeName )
      throws java.rmi.RemoteException;

   /**
    * Creates new emaployee and adds it to a company
    */
   public void createEmployeeForCompany( java.lang.String employeeName,java.lang.String companyName )
      throws java.rmi.RemoteException;

   /**
    * Sets a company for employee
    */
   public void employ( java.lang.String employeeName,java.lang.String companyName )
      throws java.rmi.RemoteException;

   /**
    * Removes a company
    */
   public void removeCompany( java.lang.String companyName )
      throws java.rmi.RemoteException;

   /**
    * Removes a company if it exists
    */
   public void removeCompanyIfExists( java.lang.String companyName )
      throws java.rmi.RemoteException;

}
