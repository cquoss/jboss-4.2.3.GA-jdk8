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
package org.jboss.test.cmp2.cacheinvalidation.ejb;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 57211 $</tt>
 */
public interface Facade
   extends javax.ejb.EJBObject
{
   public void setup()
      throws java.lang.Exception, java.rmi.RemoteException;

   public void tearDown()
      throws java.lang.Exception, java.rmi.RemoteException;

   public java.lang.String readFirstName(java.lang.String jndiName, java.lang.Long id)
      throws java.lang.Exception, java.rmi.RemoteException;

   public void writeFirstName(java.lang.String jndiName, java.lang.Long id, java.lang.String name)
      throws java.lang.Exception, java.rmi.RemoteException;

   public java.lang.String readRelatedAFirstName(java.lang.String jndiName, java.lang.Long id)
      throws java.lang.Exception, java.rmi.RemoteException;

   public void removeA(java.lang.String jndiName, java.lang.Long id)
      throws java.lang.Exception, java.rmi.RemoteException;
}
