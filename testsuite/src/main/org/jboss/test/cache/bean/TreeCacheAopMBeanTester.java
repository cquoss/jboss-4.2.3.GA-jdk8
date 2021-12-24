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
 * Remote interface for test/TreeCacheAopMBeanTester.
 */
public interface TreeCacheAopMBeanTester
   extends EJBObject
{

   public void createPerson( String key,String name,int age )
      throws Exception, RemoteException;

   public void removePerson( String key )
      throws Exception, RemoteException;

   public void setName( String key,String name )
      throws Exception, RemoteException;

   public String getName( String key )
      throws Exception, RemoteException;

   public void setAge( String key,int age )
      throws Exception, RemoteException;

   public int getAge( String key )
      throws Exception, RemoteException;

   public void setStreet( String key,String street )
      throws Exception, RemoteException;

   public String getStreet( String key )
      throws Exception, RemoteException;

   public void setCity( String key,String city )
      throws Exception, RemoteException;

   public String getCity( String key )
      throws Exception, RemoteException;

   public void setZip( String key,int zip )
      throws Exception, RemoteException;

   public int getZip( String key )
      throws Exception, RemoteException;

   public java.lang.Object getHobby( String key,java.lang.Object hobbyKey )
      throws Exception, RemoteException;

   public void setHobby( String key,java.lang.Object hobbyKey,java.lang.Object value )
      throws Exception, RemoteException;

   public java.lang.Object getLanguage( String key,int index )
      throws Exception, RemoteException;

   public void addLanguage( String key,java.lang.Object language )
      throws Exception, RemoteException;

   public void removeLanguage( String key,java.lang.Object language )
      throws Exception, RemoteException;

   public int getLanguagesSize( String key )
      throws Exception, RemoteException;

   public java.util.Set getSkills( String key )
      throws Exception, RemoteException;

   public void addSkill( String key,String skill )
      throws Exception, RemoteException;

   public void removeSkill( String key,String skill )
      throws Exception, RemoteException;

   public void printPerson( String key )
      throws Exception, RemoteException;

   public void printCache()
      throws RemoteException;

   public java.lang.Object getFieldValue( String key,String name )
      throws RemoteException;

}
