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
 * Home interface for Secret.
 */
public interface SecretHome
   extends javax.ejb.EJBHome
{
   public static final String COMP_NAME="java:comp/env/ejb/Secret";
   public static final String JNDI_NAME="ejb/Secret";

   public org.jboss.test.foedeployer.ejb.simple.Secret create(java.lang.String secretKey , java.lang.String secret)
      throws javax.ejb.CreateException,java.rmi.RemoteException;

   public org.jboss.test.foedeployer.ejb.simple.Secret findByPrimaryKey(java.lang.String pk)
      throws javax.ejb.FinderException,java.rmi.RemoteException;

}
