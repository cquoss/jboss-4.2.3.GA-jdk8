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
package org.jboss.test.cmp2.enums.ejb;

/**
 * Remote interface for Facade.
 */
public interface Facade
   extends javax.ejb.EJBObject
{

   public  ColorEnum getColorForId(  IDClass id )
      throws Exception, java.rmi.RemoteException;

   public  AnimalEnum getAnimalForId(  IDClass id )
      throws Exception, java.rmi.RemoteException;

   public void setColor(  IDClass id, ColorEnum color )
      throws Exception, java.rmi.RemoteException;

   public void setAnimal(  IDClass id, AnimalEnum animal )
      throws Exception, java.rmi.RemoteException;

   public void createChild(  IDClass childId )
      throws Exception, java.rmi.RemoteException;

   public void removeChild(  IDClass childId )
      throws Exception, java.rmi.RemoteException;

   public  IDClass findByColor(  ColorEnum color )
      throws Exception, java.rmi.RemoteException;

   public  IDClass findAndOrderByColor(  ColorEnum color )
      throws Exception, java.rmi.RemoteException;

   public  IDClass findByColorDeclaredSql(  ColorEnum color )
      throws Exception, java.rmi.RemoteException;

   public java.util.List findLowColor(  ColorEnum color )
      throws Exception, java.rmi.RemoteException;

}
