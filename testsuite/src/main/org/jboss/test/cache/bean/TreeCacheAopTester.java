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
package org.jboss.test.cache.bean;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

/**
 * Remote interface for test/TreeCacheAopTester.
 */
public interface TreeCacheAopTester
   extends EJBObject
{

   public void testSetup() throws RemoteException;

   public void createPerson( String key,String name,int age )
      throws RemoteException;

   public void removePerson( String key ) throws RemoteException;

   public void setName( String key,String name )
      throws RemoteException;

   public String getName( String key ) throws RemoteException;

   public void setAge( String key,int age ) throws RemoteException;

   public int getAge( String key ) throws RemoteException;

   public void setStreet( String key,String street ) throws RemoteException;

   public String getStreet( String key ) throws RemoteException;

   public void setCity( String key,String city ) throws RemoteException;

   public String getCity( String key ) throws RemoteException;

   public void setZip( String key,int zip ) throws RemoteException;

   public int getZip( String key ) throws RemoteException;

   public Object getHobby( String key,Object hobbyKey )
      throws RemoteException;

   public void setHobby( String key,Object hobbyKey,Object value )
      throws RemoteException;

   public Object getLanguage( String key,int index )
      throws RemoteException;

   public void addLanguage( String key,Object language )
      throws RemoteException;

   public void removeLanguage( String key,Object language )
      throws RemoteException;

   public int getLanguagesSize( String key ) throws RemoteException;

   public java.util.Set getSkills( String key )
      throws RemoteException;

   public void addSkill( String key,String skill )
      throws RemoteException;

   public void removeSkill( String key,String skill )
      throws RemoteException;

   public Object testSerialization(  ) throws RemoteException;

   public void testDeserialization( String key,Object value )
      throws RemoteException;

   public void printPerson( String key )  throws RemoteException;

   public void printCache() throws RemoteException;

   public Object getFieldValue( String key,String name )
      throws RemoteException;

}
