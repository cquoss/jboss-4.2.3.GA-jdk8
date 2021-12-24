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
package org.jboss.test.foedeployer.ejb.simple;

/**
 * Remote interface for SecretManager.
 */
public interface SecretManager
   extends javax.ejb.EJBObject
{
   /**
    * Creates a new secret
    */
   public void createSecret( java.lang.String secretKey,java.lang.String secret )
      throws java.rmi.RemoteException;

   /**
    * Removes secret
    */
   public void removeSecret( java.lang.String secretKey )
      throws java.rmi.RemoteException;

   /**
    * Returns secret
    */
   public java.lang.String getSecret( java.lang.String secretKey )
      throws java.rmi.RemoteException;

}
