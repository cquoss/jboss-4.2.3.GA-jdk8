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
package org.jboss.test.cts.interfaces;


import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/** The remote home interface for stateful session tests
 *
 *   @author Scott.Stark@jboss.org
 *   @version $Revision: 57211 $
 */
public interface StatefulSessionHome
   extends EJBHome
{
   public StatefulSession create(String testName)
      throws RemoteException, CreateException;
   /** A test of the alternate ejbCreate<METHOD> form of create
    * @param testName
    * @return The StatefulSession remote proxy
    * @throws RemoteException thrown on transport error
    * @throws CreateException thrown on container error
    */ 
   public StatefulSession createAlt(String testName)
      throws RemoteException, CreateException;
}

