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
package org.jboss.test.proxycompiler.beans.interfaces;

/**
 * Remote interface for ProxyCompilerTest.
 */
public interface ProxyCompilerTest
   extends javax.ejb.EJBObject
{

   public Integer getPk(  )
      throws java.rmi.RemoteException;

   public void setPk( Integer pk )
      throws java.rmi.RemoteException;

   public boolean getBool(  )
      throws java.rmi.RemoteException;

   public void setBool( boolean arg )
      throws java.rmi.RemoteException;

   public byte getByte(  )
      throws java.rmi.RemoteException;

   public void setByte( byte arg )
      throws java.rmi.RemoteException;

   public char getChar(  )
      throws java.rmi.RemoteException;

   public void setChar( char arg )
      throws java.rmi.RemoteException;

   public double getDouble(  )
      throws java.rmi.RemoteException;

   public void setDouble( double arg )
      throws java.rmi.RemoteException;

   public float getFloat(  )
      throws java.rmi.RemoteException;

   public void setFloat( float arg )
      throws java.rmi.RemoteException;

   public int getInt(  )
      throws java.rmi.RemoteException;

   public void setInt( int arg )
      throws java.rmi.RemoteException;

   public long getLong(  )
      throws java.rmi.RemoteException;

   public void setLong( long arg )
      throws java.rmi.RemoteException;

   public short getShort(  )
      throws java.rmi.RemoteException;

   public void setShort( short arg )
      throws java.rmi.RemoteException;

   public Object[] getObjectArray(  )
      throws java.rmi.RemoteException;

   public void setObjectArray( Object[] arg )
      throws java.rmi.RemoteException;

   public int[] getIntArray(  )
      throws java.rmi.RemoteException;

   public void setIntArray( int[] arg )
      throws java.rmi.RemoteException;

   public boolean noArgsMethod(  )
      throws java.rmi.RemoteException;

   public String complexSignatureMethod( int i,Object ref,int[] ints,Object[] objectRefs )
      throws java.rmi.RemoteException;

}
