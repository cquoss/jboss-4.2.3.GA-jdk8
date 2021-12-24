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
package org.jboss.test.cmp2.fkmapping.ejb;

/**
 * Remote interface for Manager.
 */
public interface Manager
   extends javax.ejb.EJBObject
{

   public void testStandaloneFKMapping(  )
      throws java.rmi.RemoteException;

   public void testCompleteFKToPKMapping(  )
      throws java.rmi.RemoteException;

   public void testPartialFKToPKMapping(  )
      throws java.rmi.RemoteException;

   public void testFKToCMPMapping(  )
      throws java.rmi.RemoteException;

   public void createParent( Long id,String firstName )
      throws Exception, java.rmi.RemoteException;

   public void createChild( Long id,String firstName )
      throws Exception, java.rmi.RemoteException;

   public void createChild( Long id,String firstName,Long parentId,String parentName )
      throws Exception, java.rmi.RemoteException;

   public void assertChildHasMother( Long childId,Long parentId,String parentName )
      throws Exception, java.rmi.RemoteException;

   public Object createChildUPKWithMother(  )
      throws Exception, java.rmi.RemoteException;

   public void loadChildUPKWithMother( Object pk )
      throws Exception, java.rmi.RemoteException;

   public Object createChildUPKWithFather(  )
      throws Exception, java.rmi.RemoteException;

   public void loadChildUPKWithFather( Object pk )
      throws Exception, java.rmi.RemoteException;

}
