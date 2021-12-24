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
package org.jboss.test.cmp2.cmrstress.interfaces;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBObject;

/**
 * Remote interface for Parent.
 */
public interface Parent
   extends EJBObject
{
   /**
    * CMP get method for Id attribute.
    */
   public String getId(  )
      throws RemoteException;

   /**
    * CMP set method for Id attribute.
    */
   public void setId( String id )
      throws RemoteException;

   /**
    * Get Children that apply to this Parent.
    */
   public java.util.Set getChildren(  )
      throws RemoteException;

   /**
    * Set Children.
    */
   public void setChildren( java.util.Set children )
      throws RemoteException;

   /**
    * Get a map of Child values. This is the current axis of evil.
    */
   public java.util.Map getPropertyMap(  )
      throws RemoteException;

   /**
    * Adds a child bean with the given attributes to this bean.
    */
   public void addChild( int k,String field1,String field2 )
      throws CreateException, RemoteException;

}
