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
package org.jboss.test.jmx.interfaces;

/**
 * Remote interface for EntityA.
 */
public interface EntityA
   extends javax.ejb.EJBObject
{
   /**
    * Abstract cmp2 field get-set pair for field id Get the value of id
    * @return value of id
    */
   public Integer getId(  )
      throws java.rmi.RemoteException;

   /**
    * Set the value of id
    * @param id Value to assign to id
    */
   public void setId( Integer id )
      throws java.rmi.RemoteException;

   /**
    * Abstract cmp2 field get-set pair for field value Get the value of value
    * @return value of value
    */
   public String getValue(  )
      throws java.rmi.RemoteException;

   /**
    * Set the value of value
    * @param value Value to assign to value
    */
   public void setValue( String value )
      throws java.rmi.RemoteException;

}
