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
package org.jboss.test.naming.interfaces;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

/**

@author  Scott.Stark@jboss.org
@version $Revision: 60317 $
*/
public interface TestENC extends EJBObject
{
   /** Perform interations count lookups against the ENC
    * @return the time spent in the method
    * @throws RemoteException
    */ 
   public long stressENC(long iterations) throws RemoteException;
   /**
    * Test access of the various types of java:comp entries
    * @throws RemoteException
    */ 
   public void accessENC() throws RemoteException;

   /**
    * Call a bean with the passed jndi name bound using ejb-link
    * using an identity based on using the JndiLoginInitialContextFactory 
    * @param jndiName the name of the bean specified in ejb-ref
    * @return the result of the call or "Failed" on an error
    */
   public String testEjbLinkCallerLocalWithLogin(String jndiName,
      String username, String password)
      throws RemoteException;
}
