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
package org.jboss.test.bank.interfaces;

import java.rmi.RemoteException;
import javax.ejb.CreateException;

/**
 *      
 *   @author Rickard Oberg
 *   @author $Author: dimitris@jboss.org $
 *   @version $Revision: 57211 $
 */
public class AccountData
   implements java.io.Serializable
{
   // Constants -----------------------------------------------------
   static final long serialVersionUID = -1514857573601018681L; 
   // Attributes ----------------------------------------------------
   public String id;
   public float balance;
   public Customer owner;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public String getId()
   {
      return id;
   }
   
   public void setId(String id)
   {
      this.id = id;
   }
   
   public float getBalance()
   {
      return balance;
   }
   
   public void setBalance(float balance)
   {
      this.balance = balance;
   }
   
   public Customer getOwner()
   {
      return owner;
   }
   
   public void setOwner(Customer owner)
   {
      this.owner = owner;
   }
}

/*
 *   $Id: AccountData.java 57211 2006-09-26 12:39:46Z dimitris@jboss.org $
 *   Currently locked by:$Locker$
 *   Revision:
 *   $Log$
 *   Revision 1.2.32.3  2005/10/29 05:04:34  starksm
 *   Update the LGPL header
 *
 *   Revision 1.2.32.2  2005/04/06 16:25:04  starksm
 *   Fix the license header
 *
 *   Revision 1.2.32.1  2005/04/03 07:26:22  starksm
 *   Add the missing serialVersionUIDs
 *
 *   Revision 1.2  2001/01/07 23:14:35  peter
 *   Trying to get JAAS to work within test suite.
 *
 *   Revision 1.1.1.1  2000/06/21 15:52:38  oberg
 *   Initial import of jBoss test. This module contains CTS tests, some simple examples, and small bean suites.
 *
 *
 *  
 */
