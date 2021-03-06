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
package org.jboss.test.timer.interfaces;

/**
 * @author Thomas Diesler
 * @author Scott.Stark@jboss.org
 * @version $Revision: 57211 $
 */
public interface TimerEntityHome
   extends javax.ejb.EJBHome
{
   public static final String COMP_NAME="java:comp/env/ejb/test/timer/TimerEntity";
   public static final String JNDI_NAME="ejb/test/timer/TimerEntity";
   public static final String SECURED_JNDI_NAME="ejb/test/timer/SecuredTimerEntity";

   public org.jboss.test.timer.interfaces.TimerEntity create(java.lang.Integer pk)
      throws javax.ejb.CreateException,java.rmi.RemoteException;

   public org.jboss.test.timer.interfaces.TimerEntity findByPrimaryKey(java.lang.Integer pk)
      throws javax.ejb.FinderException,java.rmi.RemoteException;

}
